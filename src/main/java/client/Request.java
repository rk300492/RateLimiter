package client;

import util.Constants.RequestType;

import java.util.Map;

// A typical client request structure

public class Request {

	//Header

	final String clientID;
	final int requestID;
	final RequestType requestType;

	// Body
	/* For this use case, I needed only the repetition value in the body - but it can be extended to have other
	values */
	final int repetition;


	public Request(String cid, int rid, RequestType type, int repetition) {
		this.clientID = cid;
		this.requestID = rid;
		this.requestType = type;

		this.repetition = repetition;
	}

	public Request(Map<String, String> inputMap) {

		this.clientID = inputMap.get("ClientID");
		this.requestID = Integer.parseInt(inputMap.get("RequestID"));
		this.requestType = RequestType.valueOf(inputMap.get("RequestType"));
		this.repetition = Integer.parseInt(inputMap.get("Repetition"));

	}

	public String getClientID() {
		return clientID;
	}

	public int getRequestID() {
		return requestID;
	}

	public RequestType getRequestType() {
		return requestType;
	}

	public int getRepetition() {
		return repetition;
	}


	// Helps with creating a standard way to send and receive messages.
	// A better way to do this would be to establish a proper encoding and decoding technique - but for now this should do.

	@Override
	public String toString() {
		// String => ClientID=<cid>,RequestID=<rid>,RequestType=<type>,Repetition=<repetitions>
		final StringBuilder builder = new StringBuilder();

		builder.append("ClientID=" + clientID);
		builder.append(",RequestID=" + String.valueOf(requestID));
		builder.append(",RequestType=" + requestType.toString());
		builder.append(",Repetition=" + String.valueOf(repetition));

		return builder.toString();
	}
}
