package client;

import server.Response;
import util.Constants.RequestType;
import util.Utility;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.Scanner;
import java.util.concurrent.Future;
import java.util.logging.Logger;

// Single threaded client

public class Client {

	public static final Logger LOG = Logger.getLogger("Client");

	public final String host;
	public final int port;

	public int clientRequestID = 1;

	public Client(String host, int port) {
		this.host = host;
		this.port = port;
	}

	public void start() {

		try (AsynchronousSocketChannel s = AsynchronousSocketChannel.open()) {
			s.connect(new InetSocketAddress(host, port)).get();

			// Step 1: Grab the connection response from the server to get the client id
			final Response connectionResponse = Utility.decodeResponse(readAsync(s));
			final String clientID = connectionResponse.getClientID();
			LOG.info("Client connection has been established!. ID:" + clientID);

			do {
				// Step 2: Prepare and Send the request map
				final Request request = prepareRequest(clientID);
				sendAsync(Utility.encodeRequest(request), s);

				// Step 3: Process the response as they keep coming
				final Response response = Utility.decodeResponse(readAsync(s));
				processResponse(response);

			} while (shouldContinue());

			final Request disconnectRequest = new Request(clientID, clientRequestID++, RequestType.CLOSE, 0);
			sendAsync(Utility.encodeRequest(disconnectRequest), s);

		} catch (Exception ex) {
			LOG.severe("Error trying to setup the client : " + ex.getMessage());
			ex.printStackTrace();
		}


	}


	private boolean shouldContinue() {
		final Scanner in = new Scanner(System.in);
		System.out.println("Do you wish to continue? (Y/N)");
		return "Y".equalsIgnoreCase(in.next());

	}


	private Request prepareRequest(String clientID) {
		final Scanner in = new Scanner(System.in);

		System.out.println("How many times do you want to ping the http server");
		int repetition = in.nextInt();

		return new Request(clientID, clientRequestID++, RequestType.GET, repetition);
	}


	private Integer sendAsync(ByteBuffer requestBuffer, AsynchronousSocketChannel s) throws Exception {
		//Todo : Buffer allocate fix

		final Future<Integer> write = s.write(requestBuffer);
        requestBuffer.clear();
		return write.get();

	}

	private ByteBuffer readAsync(AsynchronousSocketChannel s) throws Exception {

        //Todo : Buffer allocate fix
        ByteBuffer buf = ByteBuffer.allocate(1024);
		final Future<Integer> response = s.read(buf);
		final Integer length = response.get();
		final ByteBuffer responseBuf = ByteBuffer.allocate(length);
		responseBuf.put(buf.array(), 0, length);
		buf.clear();
		return responseBuf;
	}


	private void processResponse(Response response) throws Exception {
		final String data = response.getResponse();
		System.out.println(data);

	}

}
