package server;

import client.Request;
import throttling.Throttler;
import util.Constants;
import util.Constants.RequestType;
import util.Constants.ResponseStatus;
import util.Constants.ResponseType;
import util.Utility;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;

/*
    This is a simple multi threaded asynchronous server, running with a thread pool of 10. Asynchronous activities are
    carried out by use of Completion Handler. The shared resource among the threads is the throttler map whose
    entries get updated and refreshed by different threads.

    The way I imagined it is to be wrapped on top of any existing java http service that services a http webpage.
    The reason why I thought of that is to keep this as a micro service that can plugged in when required.

    Function of this server are :

    1. Providing an arbitrary client connection id that the client can later use to identify itself.
    2. Getting the no. of times a client wants to ping a webpage.
    3. Check a throttling mechanism prior to pinging the http webpage.
    4. Responding back with the status code on each ping / the time until when the client needs wait to ping back.

    Improvements possible:

    1. Currently, this doesn't do much other than ping a http url and respond back the status code from the ping.
    However, this can be extended to play with the response from the java http service.
    2. For the purpose of this interview question , I kept the client request to be a simple object but we can
    definitely make this server understand a standard http client request instead.

 */
public class Server {

	public static final Logger LOG = Logger.getLogger("Server");
	public static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(10);

	public final Throttler throttler;
	public final InetSocketAddress address;
	private static Server serverInstance = null;

	private Server(String host, int port, int throttleLimit, int minutesToThrottle) {
		this.address = new InetSocketAddress(host, port);
		this.throttler = new Throttler(throttleLimit, minutesToThrottle);
	}

	// Keeping this as singleton - as we can only spawn one server in a application run.
	public static Server getInstance(String host, int port, int throttleLimit, int minutesToThrottle) {
		if (serverInstance == null)
			serverInstance = new Server(host, port, throttleLimit, minutesToThrottle);

		return serverInstance;
	}

	public void start() {

		LOG.info("Beginning the rate limiting module server ...");

		try (final AsynchronousServerSocketChannel asynchronousServerSocketChannel = AsynchronousServerSocketChannel.open()) {
			asynchronousServerSocketChannel.bind(this.address);

			while (true) {

				// Wait for a client to connect and give control to executor thread task to handle this connection
				final Future<AsynchronousSocketChannel> asynchronousSocketChannelFuture = asynchronousServerSocketChannel.accept();
				final ServerSocketTask task = new ServerSocketTask(asynchronousSocketChannelFuture.get());
				EXECUTOR.execute(task);

			}

		} catch (Exception ex) {
			LOG.severe("Exception running server socket:" + ex.getMessage());
			ex.printStackTrace();
		} finally {
			LOG.info("Shutting down rate limiting module server ... BYE!");
			EXECUTOR.shutdown();
		}
	}


	private class ServerSocketTask implements Runnable {

		final AsynchronousSocketChannel asynchronousSocketChannel;

		public ServerSocketTask(AsynchronousSocketChannel socket) {
			this.asynchronousSocketChannel = socket;
		}

		@Override
		public void run() {

			// Step 1: Setup and send a new client ID for this connected client
			final String id = ServerUtil.generateClientId();
			final Response connectionMessage = new Response(id, ResponseStatus.SUCCESS, ResponseType.CONNECT, null);
			sendAsyncResponse(connectionMessage);
			LOG.info("A new client with id:" + id + " has started ! Listening to messages ... ");

			// Step 2: Get the client request and process asynchronously
			final ByteBuffer readBuffer = ByteBuffer.allocate(Constants.ARBITRARY_SIZE);
			readAsyncRequest(readBuffer, new ServerReadHandler());
		}


		/************************************ SUPPORTING READING METHODS **********************************************/
		private void readAsyncRequest(ByteBuffer readBuffer, ServerReadHandler readHandler) {
			asynchronousSocketChannel.read(readBuffer, readBuffer, readHandler);
		}

		private class ServerReadHandler implements CompletionHandler<Integer, ByteBuffer> {

			@Override
			public void completed(Integer requestLength, ByteBuffer readBuffer) {

				// Step 1: Trim the request buffer and get the request after decoding
				final ByteBuffer requestBuf = ByteBuffer.allocate(requestLength);
				requestBuf.put(readBuffer.array(), 0, requestLength);

				final Request request = Utility.decodeRequest(requestBuf);
				LOG.info("Received request from client:" + request.toString());

				// Step 2: Process the response based on its type
				final Response response = processRequest(request);

				// Step 3: Respond to the client
				sendAsyncResponse(response);

				// Step 4: Continue reading other request from this client until the received request type is not GET
				readBuffer.clear();
				if (RequestType.GET.equals(request.getRequestType())) {
					asynchronousSocketChannel.read(readBuffer, readBuffer, this);
				}

			}

			@Override
			public void failed(Throwable throwable, ByteBuffer byteBuffer) {

				LOG.severe("Exception occurred trying to read the request from client:" + throwable.getMessage());
				throwable.printStackTrace();

			}
		}

		/********************************* SUPPORTING PROCESSING METHODS **********************************************/
		private Response processRequest(Request request) {

			Response response;

			if (RequestType.CLOSE.equals(request.getRequestType())) {
				// Step 1: If client wants to close - server will respond async with a close message
				// and close the connection socket

				LOG.info("Client:" + request.getClientID() + " has requested to close this connection with id:"
						+ request.getClientID());

				final Response closeResponse =
						new Response(request.getClientID(), request.getRequestID(), ResponseStatus.SUCCESS,
								ResponseType.CLOSE, "Closing the connection!");
				response = closeResponse;

				throttler.clearClientThrottle(request.getClientID());

			} else if (RequestType.GET.equals(request.getRequestType())) {

				// Step 2: If client requests to ping the web page - server pings it asynchronously and send the client
				// with the response once it has it

				LOG.info("Client" + request.getClientID() + "  has requested to ping the airtasker webpage :"
						+ request.getRepetition() + " times");

				final String responseString = prepareResponse(request.getClientID(), request.getRepetition());
				final Response postResponse =
						new Response(request.getClientID(), request.getRequestID(), ResponseStatus.SUCCESS,
								ResponseType.POST, responseString);
				response = postResponse;

			} else {

				// Step 3: If client sends an unrecognized request - server will respond that it doesnt understand
				// and close the connection socket

				LOG.warning("Client" + request.getClientID() + " has sent an unrecognized request!!");

				final String failureMessage = "Server does not understand the received request. Request: " + request;
				final Response failureResponse =
						new Response(request.getClientID(), request.getRequestID(), ResponseStatus.FAILURE,
								ResponseType.CLOSE, failureMessage);
				response = failureResponse;

				throttler.clearClientThrottle(request.getClientID());
			}

			return response;
		}

		private String prepareResponse(String clientID, int repetition) {
			Map<Integer, Integer> pingStatus = new HashMap<>();
			String responseMessage = "";
			for (int i = 0; i < repetition; i++) {
				int statusCode;

				// Step 1: Check to throttle
				if (throttler.shouldThrottle(clientID, LocalDateTime.now())) {
					// Step 1a: If throttle is satisfied - send a 429
					responseMessage = ServerUtil.getThrottleMessage(throttler.getClientEndTime(clientID));
					statusCode = 429;
				} else {
					// Step 1b: else ping the http and send back the response.
					statusCode = ServerUtil.getStatus(Constants.URL);
				}

				// Step 2:  Collect the ping status count
				if (!pingStatus.containsKey(statusCode)) {
					pingStatus.put(statusCode, 0);
				}

				final int addedCount = pingStatus.get(statusCode) + 1;
				pingStatus.put(statusCode, addedCount);
			}

			//Step 3: Prepare a response string to the client
			final StringBuilder builder = new StringBuilder();
			builder.append("Ping Status (status code : no.of occurences) : ")
					.append(pingStatus.toString());
			if (!responseMessage.isEmpty())
				builder.append(" ; Message : " + responseMessage);

			return builder.toString();
		}

		/*********************************** SUPPORTING WRITING METHODS **********************************************/
		private void sendAsyncResponse(Response response) {
			final ByteBuffer responseBuffer = Utility.encodeResponse(response);
			asynchronousSocketChannel.write(responseBuffer, responseBuffer, new ServerWriteHandler());
		}

		private class ServerWriteHandler implements CompletionHandler<Integer, ByteBuffer> {

			@Override
			public void completed(Integer integer, ByteBuffer writeBuffer) {
				writeBuffer.clear();
			}

			@Override
			public void failed(Throwable throwable, ByteBuffer writeBuffer) {
				LOG.severe("Exception occurred trying to send the response to client:" + throwable.getMessage());
				throwable.printStackTrace();

			}
		}


	}


}
