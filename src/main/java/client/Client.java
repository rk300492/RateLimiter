package client;

import server.Response;
import util.Constants;
import util.Constants.RequestType;
import util.Utility;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.Scanner;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

// Single threaded client

public class Client {

	public static final Logger LOG = Logger.getLogger("Client");

	public final String host;
	public final int port;

	public AtomicInteger clientRequestID = new AtomicInteger(1);

	public Client(String host, int port) {
		this.host = host;
		this.port = port;
	}

	public void start() {

		LOG.info("Client has begun processing ...");
		try (AsynchronousSocketChannel asynchronousSocketChannel = AsynchronousSocketChannel.open()) {
			asynchronousSocketChannel.connect(new InetSocketAddress(host, port)).get();

			// Step 1: As soon as connection is made - server sends a connection message with the client Id, pick
			// that up.
			final Response connectionResponse = Utility.decodeResponse(readAsync(asynchronousSocketChannel));
			final String clientID = connectionResponse.getClientID();
			LOG.info("Client connection has been established!. ID:" + clientID);

			do {
				// Step 2: Prepare and Send the GET request to ping the html servers
				final Request request = prepareRequest(clientID);
				sendAsync(Utility.encodeRequest(request), asynchronousSocketChannel);

				// Step 3: Process the response
				final Response response = Utility.decodeResponse(readAsync(asynchronousSocketChannel));
				processResponse(response);

			} while (shouldContinue()); // Continue until client wants to close.

			// Step 4: Send a disconnection message to the server to say that this client has completed.
			final Request disconnectRequest = new Request(clientID, clientRequestID.getAndIncrement(), RequestType.CLOSE, 0);
			sendAsync(Utility.encodeRequest(disconnectRequest), asynchronousSocketChannel);

		} catch (Exception ex) {
			LOG.severe("Error trying to setup the client : " + ex.getMessage());
			ex.printStackTrace();
		}

		LOG.info("Client is exiting... BYE !");


	}


	/************************************ SUPPORTING METHODS **********************************************************/
	private boolean shouldContinue() {
		final Scanner input = new Scanner(System.in);
		System.out.println("Do you wish to continue? (Y/N)");
		return "Y".equalsIgnoreCase(input.next());
	}


	private Request prepareRequest(String clientID) {
		final Scanner input = new Scanner(System.in);
		System.out.println("How many times do you want to ping the http server? (Enter a number)");
		return new Request(clientID, clientRequestID.getAndIncrement(), RequestType.GET, input.nextInt());
	}

	private void processResponse(Response response) {
		final String data = response.getResponse();
		System.out.println(data);
	}

	private Integer sendAsync(ByteBuffer sendBuffer, AsynchronousSocketChannel asynchronousSocketChannel) throws Exception {
		final Future<Integer> write = asynchronousSocketChannel.write(sendBuffer);
		sendBuffer.clear();
		return write.get();

	}

	private ByteBuffer readAsync(AsynchronousSocketChannel asynchronousSocketChannel) throws Exception {

		// Step 1: We set a arbitrary size because we cannot predetermine the no. of bytes that will be filled.
		final ByteBuffer buf = ByteBuffer.allocate(Constants.ARBITRARY_SIZE);
		final Future<Integer> response = asynchronousSocketChannel.read(buf);
		final Integer responseLength = response.get();

		// Step 2: Get the actual bytes that are written
		final ByteBuffer responseBuf = ByteBuffer.allocate(responseLength);
		responseBuf.put(buf.array(), 0, responseLength);
		buf.clear();

		return responseBuf;
	}


}
