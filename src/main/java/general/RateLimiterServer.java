package general;

import data.CommunicationCodec;
import data.RateLimiterRequest;
import data.RateLimiterResponse;
import util.Constants.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

// Multi threaded server (not async!!)
public class RateLimiterServer {

    public static final Logger log = Logger.getLogger("Server");
    public final ExecutorService executor;
    public final int port;
    public final Map<String, Integer> clientCounter;

    public RateLimiterServer(int port, int threadCount) {
        this.port = port;
        this.executor = Executors.newFixedThreadPool(threadCount);
        this.clientCounter = new HashMap<>();
    }

    public void start() {

        try (ServerSocket ss = new ServerSocket(port)) {
            while (true) {
                final ServerSocketTask task = new ServerSocketTask(ss.accept());
                executor.execute(task);
            }

        } catch (Exception ex) {
            log.severe("Exception : Setting up server socket" + ex.getMessage());
        } finally {
            executor.shutdown();

        }
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

        final Socket s;

        public ServerSocketTask(Socket socket) {
            this.s = socket;
        }

        @Override
        public void run() {
            try (s) {
                final String id = generateClientId();

                // Step 1: Send the new assigned ID.
                log.info("A new client connection id:" + id + " has started ! Sending connection info to client and starting communication...");
                final RateLimiterResponse connectionMessage = new RateLimiterResponse(id, ResponseStatus.SUCCESS, ResponseType.CONNECT, null);
                sendResponse(CommunicationCodec.encodeResponse(connectionMessage));

                // Keep doing until client requests close of connection saying its done
                while (!s.isClosed()) {

                    // Step 2: Get the client request and decode it
                    final RateLimiterRequest request = CommunicationCodec.decodeRequest(readRequest());

                    if (RequestType.CLOSE.equals(request.getRequestType())) {

                        // Step 3: If client wants to close - we close the socket.
                        log.info("Client has requested to close this connection with id:" + id);
                        final RateLimiterResponse closeResponse =
                                new RateLimiterResponse(id, request.getRequestID(), ResponseStatus.SUCCESS, ResponseType.CLOSE, "Closing the connection!");
                        sendResponse(CommunicationCodec.encodeResponse(closeResponse));
                        s.close();

                    } else if (RequestType.GET.equals(request.getRequestType())) {

                        // Step 4: Prepare and Send a post response
                        final String response = prepareResponse(request.getRepetition());
                        final RateLimiterResponse postResponse =
                                new RateLimiterResponse(id, request.getRequestID(), ResponseStatus.SUCCESS, ResponseType.POST, response);
                        sendResponse(CommunicationCodec.encodeResponse(postResponse));

                    } else {
                        // Step 5: Handle unrecognized request
                        log.warning("Client sent an unrecognized request.");

                        final String failureMessage = "Server does not understand the received request. Request: " + request;
                        final RateLimiterResponse failureResponse =
                                new RateLimiterResponse(id, request.getRequestID(), ResponseStatus.FAILURE, ResponseType.CLOSE, failureMessage);

                        sendResponse(CommunicationCodec.encodeResponse(failureResponse));
                        s.close();

                    }
                }

            } catch (Exception e) {
                log.severe("Exception running socket task : " + e.getMessage());
                e.printStackTrace();
            }
        }

        private String readRequest() throws Exception {
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            return in.readLine();
        }


        public String prepareResponse(int repetition) throws Exception {
            final StringBuilder builder = new StringBuilder();

            log.info("Client wants to ping Airtasker website with a repetition of:" + repetition);

            for (int i = 0; i < repetition; i++) {
                // Step 1: ping the http url to get the status
                final String status = getStatus();

                // Step 2: Append with the url status
                builder.append("/n" + status);
            }


            return builder.toString();
        }

        public void sendResponse(String response) throws Exception {
            final PrintWriter pw = new PrintWriter(new OutputStreamWriter(s.getOutputStream()), true);
            pw.println(response);
            pw.flush();
        }
    }


}
