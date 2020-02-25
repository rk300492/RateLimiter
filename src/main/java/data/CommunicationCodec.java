package data;

import java.util.HashMap;
import java.util.Map;

// Defines how to encode and decode the request and response object over the wire - I do it by basic string
public class CommunicationCodec {

    public static String encodeRequest(RateLimiterRequest request) {
        return request.toString();
    }

    public static String encodeResponse(RateLimiterResponse response) {
        return response.toString();
    }

    // ClientID=<cid>,RequestID=<rid>,RequestType=<type>,Repetition=<repetitions>
    public static RateLimiterRequest decodeRequest(String input) {
        return new RateLimiterRequest(convertToMap(input));

    }

    // ClientID=<cid>,RequestID=<rid>,ResponseStatus=<status>,ResponseType=<type>,Response=<Response String>
    public static RateLimiterResponse decodeResponse(String input) {
        return new RateLimiterResponse(convertToMap(input));
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

