package general;

import data.CommunicationCodec;
import data.RateLimiterRequest;
import data.RateLimiterResponse;
import util.Constants.*;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
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

            try (Socket s = new Socket(host, port)) {
                final OutputStream outputStream = s.getOutputStream();
                final InputStream inputStream = s.getInputStream();

                // Step 1: Grab the connection response from the server to get the client id
                final RateLimiterResponse connectionResponse = CommunicationCodec.decodeResponse(readResponse(inputStream));
                final String clientID = connectionResponse.getClientID();

                do {
                    // Step 1: Prepare and Send the request map

                    final RateLimiterRequest request = prepareRequest(clientID);
                    sendRequest(CommunicationCodec.encodeRequest(request), outputStream);

                    // Step 2: Process the response as they keep coming
                    processResponse(readResponse(inputStream));

                } while (shouldContinue());

                final RateLimiterRequest disconnectRequest = new RateLimiterRequest(clientID,clientRequestID++,RequestType.CLOSE,0);
                sendRequest(CommunicationCodec.encodeRequest(disconnectRequest),outputStream);

            } catch (Exception ex) {
                log.severe("Error trying to setup the client : " + ex.getMessage());
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


    private void sendRequest(String request, OutputStream socketOutputStream) throws Exception {
        final PrintWriter pw = new PrintWriter(new OutputStreamWriter(socketOutputStream), true);
        pw.println(request);
        pw.flush();


    }

    private String readResponse (InputStream socketInputStream) throws Exception{
        final BufferedReader in = new BufferedReader(new InputStreamReader(socketInputStream));
        return in.readLine();
    }

    private void processResponse(String responseString) throws Exception {
        final RateLimiterResponse response = CommunicationCodec.decodeResponse(responseString);

        final String data = response.getResponse();
        System.out.println(data);

    }

}
