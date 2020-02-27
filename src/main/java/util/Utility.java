package util;

import client.Request;
import server.Response;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class Utility {

    // Encode methods convert the Request and Response methods to corresponding ByteBuffer
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

    // Decode methods convert the byte buffer to the corresponding Request and Response methods.
    // ClientID=<cid>,RequestID=<rid>,RequestType=<type>,Repetition=<repetitions>
	public static Request decodeRequest(ByteBuffer requestBuffer) {
		final String request = new String(requestBuffer.array());
		return new Request(convertStringToMap(request));

	}

	// ClientID=<cid>,RequestID=<rid>,ResponseStatus=<status>,ResponseType=<type>,Response=<Response String>
	public static Response decodeResponse(ByteBuffer responseBuffer) {
		final String response = new String(responseBuffer.array());
		return new Response(convertStringToMap(response));
	}


	private static Map<String, String> convertStringToMap(String input) {

		final Map<String, String> inputMap = new HashMap<>();

		final String[] inputParts = input.split(",",5);
		for (String subpart : inputParts) {
			String[] element = subpart.split("=",2);
			inputMap.put(element[0], element[1]);
		}

		return inputMap;
	}


}

