package util;

// a bunch of constants shared across code
public class Constants {
    public static  enum RequestType {
        GET,
        CLOSE
    };

    public static enum ResponseStatus {
        SUCCESS,
        FAILURE
    }

    public static enum ResponseType {
        CONNECT,
        POST,
        CLOSE
    }

}
