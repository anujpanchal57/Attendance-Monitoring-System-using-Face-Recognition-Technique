/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package SendSms;

/**
 *
 * @author Mahendra
 */
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;

public class URLConnector {

    public static HttpURLConnection connection;
    public static Proxy proxy;

    public static void setProxy(String host, int port) {
        proxy = new Proxy(Proxy.Type.HTTP, java.net.InetSocketAddress.createUnresolved(host, port));
    }

    public static void connect(String urlPath, boolean redirect, String method, String cookie, String credentials) {
        try {
            URL url = new URL(urlPath);

            if (null != proxy) {
                connection = (HttpURLConnection) url.openConnection(proxy);
            } else {
                connection = (HttpURLConnection) url.openConnection();
            }
            connection.setInstanceFollowRedirects(redirect);
            if (cookie != null) {
                connection.setRequestProperty("Cookie", cookie);
            }
            if (method != null && method.equalsIgnoreCase("POST")) {
                connection.setRequestMethod(method);
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            }
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:10.0.4) Gecko/20100101 Firefox/10.0.4");
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            if (credentials != null) {
                DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
                wr.writeBytes(credentials);
                wr.flush();
                wr.close();
            }
        } catch (Exception e) {
            System.out.println("Connection error");
        }
    }

    public static void setProperty(String key, String val) {
//        connection.addRequestProperty(key, val);
    }

    public static String getCookie() {
        String cookie = null;

        if (connection != null) {
            String headerName = null;

            for (int i = 1; (headerName = connection.getHeaderFieldKey(i)) != null; i++) {
                if (headerName.equals("Set-Cookie")) {
                    cookie = connection.getHeaderField(i).split(";")[0];
                    break;
                }
            }
        }
        return cookie;
    }

    public static String getLocation() {
        String location = null;

        if (connection != null) {
            String headerName = null;

            for (int i = 1; (headerName = connection.getHeaderFieldKey(i)) != null; i++) {
                if (headerName.equals("Location")) {
                    location = connection.getHeaderField(i).split(";")[0];
                    break;
                }
            }
        }
        return location;
    }

    public static int getResponseCode() {
        int responseCode = -1;

        if (connection != null) {
            try {
                responseCode = connection.getResponseCode();
            } catch (Exception e) {
                System.err.println("Response code error");
            }
        }
        return responseCode;
    }

    public static String getResponse() {
        StringBuilder response = new StringBuilder();

        if (connection != null) {
            try {
                InputStream is = connection.getInputStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(is));

                String line;
                while ((line = rd.readLine()) != null) {
                    response.append(line);
                    response.append('\r');
                }
                rd.close();
            } catch (Exception e) {
                System.err.println("Response error");
            }
        }
        return response.toString();
    }

    public static String getErrorMessage() {
        StringBuilder errorMessage = new StringBuilder();

        if (connection != null) {
            try {
                InputStream es = connection.getErrorStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(es));

                String line;
                while ((line = rd.readLine()) != null) {
                    errorMessage.append(line);
                    errorMessage.append('\r');
                }
                rd.close();
            } catch (Exception e) {
                System.err.println("Error in getting error message");
            }
        }
        return errorMessage.toString();
    }

    public static void disconnect() {
        if (connection != null) {
            connection.disconnect();
        }
    }
}