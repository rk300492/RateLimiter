package general;

public class RateLimiterMain {

    public static final String mode = System.getProperty("rl.mode" , "CLIENT");
    public static final int port = Integer.parseInt(System.getProperty("rl.port" , "8010"));

    public static void main (String[] args){

        if("SERVER".equalsIgnoreCase(mode)){
            final int threadCount = Integer.parseInt(System.getProperty("rl.threads" , "10"));
            new RateLimiterServer(port,threadCount).start();
        }else if ("CLIENT".equalsIgnoreCase(mode)) {
            final String host = System.getProperty("rl.host" , "localhost");
            new RateLimiterClient(host , port).start();
        }

    }
}
