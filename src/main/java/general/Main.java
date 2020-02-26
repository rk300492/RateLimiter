package general;

import client.Client;
import server.Server;


public class Main {

    public static final String mode = System.getProperty("rl.mode" , "CLIENT");
    public static final int port = Integer.parseInt(System.getProperty("rl.port" , "8010"));
    public static final String host = System.getProperty("rl.host" , "localhost");

    public static void main (String[] args){

        if("SERVER".equalsIgnoreCase(mode)){
            Server.getInstance(host,port).start();
        }else if ("CLIENT".equalsIgnoreCase(mode)) {
            new Client(host , port).start();
        }

    }
}
