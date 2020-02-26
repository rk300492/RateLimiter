package util;

// a bunch of constants shared across code
public class Constants {

    public enum RequestType {
        GET,
        CLOSE
    };

    public enum ResponseStatus {
        SUCCESS,
        FAILURE
    }

    public enum ResponseType {
        CONNECT,
        POST,
        CLOSE
    }

    public static final int REQUEST_SIZE = 1024;
    public static final int RESPONSE_SIZE = 1024;

}
