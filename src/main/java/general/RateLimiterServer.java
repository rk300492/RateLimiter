package general;

import data.CommunicationCodec;
import data.RateLimiterRequest;
import data.RateLimiterResponse;
import throttling.RateLimiterThrottling;
import util.Constants.*;

import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;

// Multi threaded server (not async!!)
public class RateLimiterServer {

    public static final Logger log = Logger.getLogger("Server");
    public static final RateLimiterThrottling throttling = new RateLimiterThrottling();

    public final ExecutorService executor;
    public final int port;

    public RateLimiterServer(int port, int threadCount) {
        this.port = port;
        this.executor = Executors.newFixedThreadPool(10);
    }

    public void start() {

        try (AsynchronousServerSocketChannel ss = AsynchronousServerSocketChannel.open()) {//new ServerSocket(port)) {
            ss.bind(new InetSocketAddress(port));

            while (true) {

                // Wait for some one to connect to the server and give control to executor thread task to handle this connection
                final Future<AsynchronousSocketChannel> asynchronousSocketChannelFuture = ss.accept();
                final ServerSocketTask task = new ServerSocketTask(asynchronousSocketChannelFuture.get());
                executor.execute(task);

            }

        } catch (Exception ex) {
            log.severe("Exception : Setting up server socket" + ex.getMessage());
            ex.printStackTrace();
        } finally {
            executor.shutdown();

        }
    }

    public String getFailure() {
        final String status = "429 MOFOS !!!";
        return status;
    }

    public String getStatus() {
        final String url = "https://airtasker.com";
        String result = "";
        int code = 200;
        try {
            final URL siteURL = new URL(url);
            final HttpURLConnection connection = (HttpURLConnection) siteURL.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(3000);
            connection.connect();

            code = connection.getResponseCode();
            if (code == 200) {
                result = "-> Green <-\t" + "Code: " + code;

            } else {
                result = "-> Yellow <-\t" + "Code: " + code;
            }
        } catch (Exception e) {
            result = "-> Red <-\t" + "Wrong domain - Exception: " + e.getMessage();

        }
        final String targetResult = url + "\t\tStatus:" + result;
        System.out.println(targetResult);
        return targetResult;
    }

    public String generateClientId() {
        return UUID.randomUUID().toString();
    }


    private class ServerSocketTask implements Runnable {

        final AsynchronousSocketChannel s;

        public ServerSocketTask(AsynchronousSocketChannel socket) {
            this.s = socket;
        }

        @Override
        public void run() {
                final String id = generateClientId();

                // Step 1: Setup and send a new client ID for this connected client
                log.info("A new client with id:" + id + " has started ! Sending connection info to client and starting communication...");
                final RateLimiterResponse connectionMessage = new RateLimiterResponse(id, ResponseStatus.SUCCESS, ResponseType.CONNECT, null);
                sendAsyncResponse(connectionMessage);

                // Step 2: Get the client request and process asynchronously
                final ByteBuffer byteBuffer = ByteBuffer.allocate(100);
                readAsyncRequest(byteBuffer, new ServerReadHandler());
        }


        public String prepareResponse(String clientID, int repetition) {
            final StringBuilder builder = new StringBuilder();

            log.info("Client wants to ping Airtasker website with a repetition of:" + repetition);

            for (int i = 0; i < repetition; i++) {
                // Step 1: ping the http url to get the status
                final String status = throttling.shouldThrottle(clientID, LocalDateTime.now()) ? getFailure() : getStatus();

                // Step 2: Append with the url status
                builder.append("/n" + status);
            }


            return builder.toString();
        }


        // READING
        private void readAsyncRequest(ByteBuffer byteBuffer, ServerReadHandler readHandler) {
            s.read(byteBuffer, byteBuffer, readHandler);
        }

        private class ServerReadHandler implements CompletionHandler<Integer, ByteBuffer> {

            @Override
            public void completed(Integer requestLength, ByteBuffer readBuffer) {

                final ByteBuffer requestBuf = ByteBuffer.allocate(requestLength);
                requestBuf.put(readBuffer.array(), 0, requestLength);

                // Process request
                final RateLimiterRequest request = CommunicationCodec.decodeRequest(new String(requestBuf.array()));
                System.out.println("Received request from client:" + request.toString());
                final RateLimiterResponse response = processRequest(request); //BLOCKING
                sendAsyncResponse(response);

                // Continue reading the client until client request type is GET
                readBuffer.clear();
                if(RequestType.GET.equals(request.getRequestType())) {
                    s.read(readBuffer, readBuffer, this);
                }

            }

            @Override
            public void failed(Throwable throwable, ByteBuffer byteBuffer) {
                // Throw an exception here that leads to closing the connection
            }
        }

        // PROCESSING
        public RateLimiterResponse processRequest(RateLimiterRequest request) {

            RateLimiterResponse response = null;

            if (RequestType.CLOSE.equals(request.getRequestType())) {
                // Step 1: If client wants to close - server will respond async with a close message and close the connection socket
                log.info("Client has requested to close this connection with id:" + request.getClientID());
                final RateLimiterResponse closeResponse =
                        new RateLimiterResponse(request.getClientID(), request.getRequestID(), ResponseStatus.SUCCESS, ResponseType.CLOSE, "Closing the connection!");
                response = closeResponse;

            } else if (RequestType.GET.equals(request.getRequestType())) {

                // Step 2: If client requests to ping the web page - server pings it asynchronously and send the client with the response once it has it
                final String responseString = prepareResponse(request.getClientID(), request.getRepetition());
                final RateLimiterResponse postResponse =
                        new RateLimiterResponse(request.getClientID(), request.getRequestID(), ResponseStatus.SUCCESS, ResponseType.POST, responseString);
                response = postResponse;

            } else {
                // Step 3: If client sends an unrecognized request - server will respond that it doesnt understand and close the connection socket
                log.warning("Client sent an unrecognized request.");

                final String failureMessage = "Server does not understand the received request. Request: " + request;
                final RateLimiterResponse failureResponse =
                        new RateLimiterResponse(request.getClientID(), request.getRequestID(), ResponseStatus.FAILURE, ResponseType.CLOSE, failureMessage);

                response = failureResponse;
            }

            return response;
        }

        // WRITING
        public void sendAsyncResponse(RateLimiterResponse response) {
            String responseString = CommunicationCodec.encodeResponse(response);

            final ByteBuffer writeBuffer = ByteBuffer.wrap(responseString.getBytes());
            s.write(writeBuffer, writeBuffer, new ServerWriteHandler());
        }

        private class ServerWriteHandler implements CompletionHandler<Integer, ByteBuffer> {

            @Override
            public void completed(Integer integer,ByteBuffer writeBuffer) {
                writeBuffer.clear();
            }

            @Override
            public void failed(Throwable throwable, ByteBuffer writeBuffer) {
                // Throw an exception here that leads to closing the connection

            }
        }
    }


}
