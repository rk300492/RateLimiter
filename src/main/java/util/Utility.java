package util;

import client.Request;
import server.Response;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class Utility {

    public static ByteBuffer encodeRequest(Request request) {
        final String requestString = request.toString();
        ByteBuffer requestBuffer = ByteBuffer.wrap(requestString.getBytes());
        return requestBuffer;
    }

    public static ByteBuffer encodeResponse(Response response) {
        final String responseString = response.toString();
        ByteBuffer responseBuffer = ByteBuffer.wrap(responseString.getBytes());
        return responseBuffer;
    }

    // ClientID=<cid>,RequestID=<rid>,RequestType=<type>,Repetition=<repetitions>
    public static Request decodeRequest(ByteBuffer requestBuffer) {

        final String request = new String(requestBuffer.array());
        return new Request(convertToMap(request));

    }

    // ClientID=<cid>,RequestID=<rid>,ResponseStatus=<status>,ResponseType=<type>,Response=<Response String>
    public static Response decodeResponse(ByteBuffer responseBuffer) {
        final String response = new String(responseBuffer.array());
        return new Response(convertToMap(response));
    }


    private static Map<String,String> convertToMap(String input) {

        final Map<String, String> inputMap = new HashMap<>();

        final String[] inputParts = input.split(",");

        for (String subpart : inputParts) {
            String[] element = subpart.split("=");
            inputMap.put(element[0], element[1]);
        }
        return inputMap;
    }


}

