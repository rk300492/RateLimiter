package server;

import util.Constants.ResponseStatus;
import util.Constants.ResponseType;

import java.util.Map;

// A typical server response

public class Response {

	public static final int DEFAULT_RID = Integer.MIN_VALUE;

	// Header
	final String clientID;
	final int requestID;
	final ResponseStatus responseStatus;
	final ResponseType responseType;

	// Body
    /* At the moment , this is just a simple string, but we can make it much more complicated based on the application
    scenario.
     */
	final String response;

	public Response(String clientID, ResponseStatus responseStatus, ResponseType responseType, String response) {
		this(clientID, DEFAULT_RID, responseStatus, responseType,response);
	}

	public Response(String clientID, int requestID, ResponseStatus responseStatus, ResponseType responseType,
					String response) {
		this.clientID = clientID;
		this.requestID = requestID;
		this.responseStatus = responseStatus;
		this.responseType = responseType;
		this.response = response;
	}

	public Response(Map<String, String> inputMap) {

		this.clientID = inputMap.get("ClientID");
		this.requestID = Integer.parseInt(inputMap.get("RequestID"));
		this.responseStatus = ResponseStatus.valueOf(inputMap.get("ResponseStatus"));
		this.responseType = ResponseType.valueOf(inputMap.get("ResponseType"));
		this.response = inputMap.get("Response");

	}

	// GETTERS
	public String getClientID() {
		return clientID;
	}

	public int getRequestID() {
		return requestID;
	}

	public ResponseStatus getResponseStatus() {
		return responseStatus;
	}

	public ResponseType getResponseType() {
		return responseType;
	}

	public String getResponse() {
		return response;
	}

	// Helps with creating a standard way to send and receive messages.
	// A better way to do this would be to establish a proper encoding and decoding technique - but for now this should do.
	@Override
	public String toString() {
		// String => ClientID=<cid>,RequestID=<rid>,ResponseStatus=<status>,ResponseType=<type>,
		// Response=<response string>
		final StringBuilder builder = new StringBuilder();

		builder.append("ClientID=" + clientID);
		builder.append(",RequestID=" + String.valueOf(requestID));
		builder.append(",ResponseStatus=" + responseStatus.toString());
		builder.append(",ResponseType=" + responseType.toString());
		builder.append(",Response="); builder.append(response);


		return builder.toString();
	}
}

