import java.io.*;
import java.net.Socket;
import java.nio.Buffer;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Logger;

public class Client {

    public static final Logger log = Logger.getLogger(Client.class.getSimpleName());

    public static void main (String[] args){

        try (Socket s = new Socket( "localhost" , 8010)){
            final InputStream socketInputStream = s.getInputStream();
            final OutputStream socketOutputStream = s.getOutputStream();

            // Step 1: Prepare and Send the request map
            final String request = prepareRequest ();
            sendRequest(request, socketOutputStream);

            // Step 2: Process the response as they keep coming
            processResponse(socketInputStream);

        }catch (Exception ex){
            log.severe("Error trying to setup the client : " + ex.getMessage());
        }
    }



    private static String prepareRequest() {
        final Scanner in = new Scanner(System.in);

        System.out.println("How many times do you want to ping the http server") ;
        final int repetition = in.nextInt();

        final String request = "action:GET;id:1;repetition:" + repetition;
        return request;
    }


    private static void sendRequest(String request, OutputStream socketOutputStream) throws Exception{

        final PrintWriter pw = new PrintWriter(new OutputStreamWriter(socketOutputStream),true);
        pw.println(request);



    }

    private static void processResponse(InputStream socketInputStream) throws Exception{
        BufferedReader in = new BufferedReader(new InputStreamReader(socketInputStream));

        String response ;
        while((response=in.readLine())!=null){

            System.out.println(response);
        }
    }

}
