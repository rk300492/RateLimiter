package server;

import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class ServerUtil {

	public static String generateClientId() {
		return UUID.randomUUID().toString();
	}

	public static String getThrottleMessage(LocalDateTime clientEndTime) {

        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd, HH:mm:ss a");
        final String formatDateTime = clientEndTime.format(formatter);
        return ("Too many request has been received from this client, please try your requests after " + formatDateTime);
	}

	public static int getStatus(String url) {
		String result;
		int code;

		try {
			final URL siteURL = new URL(url);
			final HttpURLConnection connection = (HttpURLConnection) siteURL.openConnection();
			connection.setRequestMethod("GET");
			connection.setConnectTimeout(3000);
			connection.connect();

			code = connection.getResponseCode();
			if (code == 200) {
				result = "-> Green <-\t" + "Code: " + code;

			} else {
				result = "-> Yellow <-\t" + "Code: " + code;
			}
		} catch (Exception e) {
			result = "-> Red <-\t" + "Wrong domain - Exception: " + e.getMessage();
			code = 500; // internal server error
		}
		final String targetResult = url + "\t\tStatus:" + result;
		System.out.println(targetResult);

		return code;
	}

}
