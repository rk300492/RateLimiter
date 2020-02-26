package server;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

public class ServerUtil {

    public static String generateClientId() {
        return UUID.randomUUID().toString();
    }

    // Todo: Fix a proper failure message here.
    public static String getFailure() {
        final String status = "429 MOFOS !!!";
        return status;
    }
    // Todo: Fix a proper ping message here.
    public static String getStatus() {
        final String url = "https://airtasker.com";
        String result = "";
        int code = 200;
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

        }
        final String targetResult = url + "\t\tStatus:" + result;
        System.out.println(targetResult);
        return targetResult;
    }

}
