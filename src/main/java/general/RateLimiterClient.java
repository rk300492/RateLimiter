package general;

import data.CommunicationCodec;
import data.RateLimiterRequest;
import data.RateLimiterResponse;
import util.Constants.RequestType;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.Scanner;
import java.util.concurrent.Future;
import java.util.logging.Logger;

// Single threaded client - Pretty dumb

public class RateLimiterClient {

    public static final Logger log = Logger.getLogger("Client");

    public final String host;
    public final int port;

    public int clientRequestID = 1; //should become a atomic integer ?
    public RateLimiterClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start() {

            try (AsynchronousSocketChannel s = AsynchronousSocketChannel.open()) {
                s.connect(new InetSocketAddress(host,port)).get();

                // Step 1: Grab the connection response from the server to get the client id
                final RateLimiterResponse connectionResponse = CommunicationCodec.decodeResponse(readAsync(s));
                final String clientID = connectionResponse.getClientID();
                log.info("Server set up client connection id:" + clientID);


                do {
                    // Step 1: Prepare and Send the request map

                    final RateLimiterRequest request = prepareRequest(clientID);
                    sendAsync(CommunicationCodec.encodeRequest(request),s);

                    // Step 2: Process the response as they keep coming
                    final RateLimiterResponse response = CommunicationCodec.decodeResponse(readAsync(s));
                    processResponse(response);

                } while (shouldContinue());

                final RateLimiterRequest disconnectRequest = new RateLimiterRequest(clientID,clientRequestID++,RequestType.CLOSE,0);
                sendAsync(CommunicationCodec.encodeRequest(disconnectRequest),s);

            } catch (Exception ex) {
                log.severe("Error trying to setup the client : " + ex.getMessage());
                ex.printStackTrace();
            }


    }


    private boolean shouldContinue() {
        final Scanner in = new Scanner(System.in);
        System.out.println("Do you wish to continue? (Y/N)");
        return "Y".equalsIgnoreCase(in.next());

    }


    private RateLimiterRequest prepareRequest(String clientID) {
        final Scanner in = new Scanner(System.in);

        System.out.println("How many times do you want to ping the http server");
        int repetition= in.nextInt();

        return new RateLimiterRequest(clientID, clientRequestID++,RequestType.GET,repetition);
    }


    private Integer sendAsync(String request, AsynchronousSocketChannel s) throws Exception {
        ByteBuffer buf = ByteBuffer.wrap(request.getBytes());
        final Future<Integer> write = s.write(buf);
        buf.clear();
        return write.get() ;

    }

    private String readAsync ( AsynchronousSocketChannel s) throws Exception {
        ByteBuffer buf = ByteBuffer.allocate(1024);
        final Future<Integer> response = s.read(buf);
        final Integer length = response.get();
        final ByteBuffer responseBuf = ByteBuffer.allocate(length);
        responseBuf.put(buf.array(), 0, length);
        buf.clear();
        return new String(responseBuf.array());
    }



    private void processResponse(RateLimiterResponse response) throws Exception {
        final String data = response.getResponse();
        System.out.println(data);

    }

}
