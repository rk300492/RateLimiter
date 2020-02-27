package general;

import client.Client;
import server.Server;


public class Main {

    public static final String mode = System.getProperty("rl.mode" , "CLIENT");
    public static final int port = Integer.parseInt(System.getProperty("rl.port" , "8010"));
    public static final String host = System.getProperty("rl.host" , "localhost");

    public static void main (String[] args){

        // Depending on the VM args - the server or the client section of the code executes.
        if("SERVER".equalsIgnoreCase(mode)){
            final int throttleLimit = Integer.parseInt(System.getProperty("rl.throttleLimit" , "100"));
            final int minutesToThrottle = Integer.parseInt(System.getProperty("rl.minutesToThrottle" , "60"));
            Server.getInstance(host,port,throttleLimit,minutesToThrottle).start();
        }else if ("CLIENT".equalsIgnoreCase(mode)) {
            new Client(host , port).start();
        }

    }
}
