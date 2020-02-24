import java.io.*;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Logger;

public class Server {

    public static final Logger log = Logger.getLogger(Server.class.getSimpleName());
    public static final ExecutorService executor = Executors.newFixedThreadPool(10);

    public static void main(String[] args) {

        try (ServerSocket ss = new ServerSocket(8010)) {
            while (true) {

                Socket s = ss.accept();
                SocketTask task = new SocketTask(s);
                executor.execute(task);

//                        final InputStream socketInputStream = s.getInputStream();
//                        final OutputStream socketOutputStream = s.getOutputStream();
//
//
//                        // Step 1: Get the client request
//                        final String request = readRequest(socketInputStream);
//
//                        // Step 2: Process the request
//                        final Map<String, String> requestMap = processRequest(request);
//
//                        // Step 3: Send Response
//                        sendResponse(requestMap, socketOutputStream);
            }

        } catch (Exception ex) {
            log.severe("Exception at setting up server socket" + ex.getMessage());
        } finally {
            executor.shutdown();

        }
    }


    private static class SocketTask implements Runnable {

        final Socket s ;

        public SocketTask(Socket socket){
            this.s = socket;
        }

        @Override
        public void run() {
            try {

                if(s.isConnected()) {
                    final InputStream socketInputStream = s.getInputStream();
                    final OutputStream socketOutputStream = s.getOutputStream();


                    // Step 1: Get the client request
                    final String request = readRequest(socketInputStream);

                    // Step 2: Process the request
                    final Map<String, String> requestMap = processRequest(request);

                    // Step 3: Send Response
                    sendResponse(requestMap, socketOutputStream);

                    // Step 4: Close the client
                    s.close();
                }

            }catch (Exception e){
                log.severe("Exception running socket task : " + e.getMessage());
            }
        }
    }

    private static String readRequest(InputStream socketInputStream) throws Exception {
        BufferedReader in = new BufferedReader(new InputStreamReader(socketInputStream));
        return in.readLine();
    }

    private static Map<String, String> processRequest(String request) {

        if (request != null) {
            final Map<String, String> targetMap = new HashMap<>();
            final String[] parts = request.split(";");

            for (String part : parts) {
                final String[] subparts = part.split(":");
                if (subparts.length == 2) {
                    targetMap.put(subparts[0], subparts[1]);
                }
            }
            return targetMap;
        }
        return null;
    }

    public static void sendResponse(Map<String, String> requestMap, OutputStream socketOutputStream) throws Exception {

        if (requestMap != null) {
            final PrintWriter bw = new PrintWriter(new OutputStreamWriter(socketOutputStream), true);
            final int repetition = Integer.parseInt(requestMap.get("repetition"));
            final String id = requestMap.get("id");

            if ("GET".equalsIgnoreCase(requestMap.get("action"))) {

                for (int i = 0; i < repetition; i++) {
                    // Step 1: ping the http url to get the status
                    final String status = getStatus();
                    // Step 2: Respond with the url status
                    bw.println(status);
                    bw.flush();
                }
            }
        }


    }


    public static String getStatus() throws Exception {
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
                ;
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
}
