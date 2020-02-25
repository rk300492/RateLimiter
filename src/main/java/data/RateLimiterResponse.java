package data;

import util.Constants.*;

import java.util.Map;

// A typical server response
public class RateLimiterResponse {

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

    public RateLimiterResponse(String clientID, ResponseStatus responseStatus, ResponseType responseType, String response) {
        this(clientID, DEFAULT_RID, responseStatus, responseType, response);
    }

    public RateLimiterResponse(String clientID, int requestID, ResponseStatus responseStatus, ResponseType responseType, String response) {
        this.clientID = clientID;
        this.requestID = requestID;
        this.responseStatus = responseStatus;
        this.responseType = responseType;
        this.response = response;
    }

    public RateLimiterResponse(Map<String, String> inputMap) {

        this.clientID = inputMap.get("ClientID");
        this.requestID = Integer.parseInt(inputMap.get("RequestID"));
        this.responseStatus = ResponseStatus.valueOf(inputMap.get("ResponseStatus"));
        this.responseType = ResponseType.valueOf(inputMap.get("ResponseType"));
        this.response = inputMap.get("Response");

    }

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


    // Todo : encode and decode method could be useful
    // ClientID=<cid>,RequestID=<rid>,ResponseStatus=<status>,ResponseType=<type>,Response=<response string>
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();

        builder.append("ClientID=" + clientID);
        builder.append(",RequestID=" + String.valueOf(requestID));
        builder.append(",ResponseStatus=" + responseStatus.toString());
        builder.append(",ResponseType=" + responseType.toString());
        builder.append(",Response=");
        builder.append(response);

        return builder.toString();
    }
}

