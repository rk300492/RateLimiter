package util;

// a bunch of constants shared across code
public class Constants {

	public enum RequestType {
		GET,
		CLOSE
	}

	public enum ResponseStatus {
		SUCCESS,
		FAILURE
	}

	public enum ResponseType {
		CONNECT,
		POST,
		CLOSE
	}

	public static final String URL = "https://airtasker.com";
	public static final int ARBITRARY_SIZE = 1024;

}
