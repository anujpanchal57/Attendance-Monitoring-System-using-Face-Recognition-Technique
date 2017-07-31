/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package SendSms;

/**
 *
 * @author Mahendra
 */
import java.net.HttpURLConnection;

public class ThreadSms implements Runnable {

    String uname = "";
    String pass = "";
    String msg = "";
    String phonenum = "";
    public static int responseCode = -1;
    public static String userCredentials = null;
    public static String cookie = null;
    public static String site = null;
    public static String token = null;
    public static Credentials credentials = new Credentials();

    public ThreadSms(String username, String password, String mobile, String message) {
        uname = username;
        pass = password;
        msg = message;
        phonenum = mobile;
    }

    @Override
    public void run() {
        login(uname, pass);
        credentials.reset();
        credentials.append("Token", token);
        credentials.append("message", msg);
        credentials.append("mobile", phonenum);
        credentials.append("msgLen", "124");
        credentials.append("ssaction", "ss");

        userCredentials = credentials.getUserCredentials();
//        System.out.println("Token=" + token);
        URLConnector.setProperty("Token", token);
        URLConnector.setProperty("message", msg);
        URLConnector.setProperty("mobile", phonenum);
        URLConnector.setProperty("msgLen", "139");
        URLConnector.setProperty("ssaction", "ss");

        URLConnector.connect("http://" + site + "/smstoss.action", true, "POST", cookie, credentials.getUserCredentials());

        responseCode = URLConnector.getResponseCode();
//        System.out.println("IN " + responseCode);
        if (responseCode != HttpURLConnection.HTTP_MOVED_TEMP && responseCode != HttpURLConnection.HTTP_OK) {
            System.out.println("sendSMS failed!");
        }
        credentials.reset();
        URLConnector.disconnect();
        System.out.println("Message has been sent to "+phonenum);
    }

    public static void getSite() {
        URLConnector.connect("http://www.way2sms.com/", false, "GET", null, null);
        responseCode = URLConnector.getResponseCode();
//        System.out.println(responseCode);
        if (responseCode != HttpURLConnection.HTTP_MOVED_TEMP && responseCode != HttpURLConnection.HTTP_OK) {
            System.out.println("getSite failed!");
        } else {
            site = URLConnector.getLocation();
            if (site != null) {
                site = site.substring(7, site.length() - 1);
            }
        }
//        System.out.println(site);
        URLConnector.disconnect();
    }

    public static void preHome() {
        URLConnector.connect("http://" + site + "/content/prehome.jsp", false, "GET", null, null);
        responseCode = URLConnector.getResponseCode();
        System.out.println(responseCode);
        if (responseCode != HttpURLConnection.HTTP_MOVED_TEMP && responseCode != HttpURLConnection.HTTP_OK) {
            System.out.println("preHome failed");
        } else {
            cookie = URLConnector.getCookie();
        }
        token = cookie.substring(cookie.indexOf("~") + 1);
        URLConnector.disconnect();
    }

    public static void login(String uid, String pwd) {
        getSite();
        preHome();

        String location = null;

        credentials.set("username", uid);
        credentials.append("password", pwd);
        credentials.append("button", "Login");
        userCredentials = credentials.getUserCredentials();

        URLConnector.connect("http://" + site + "/Login1.action", false, "POST", cookie, userCredentials);
        responseCode = URLConnector.getResponseCode();
//        System.out.println(responseCode);
        if (responseCode != HttpURLConnection.HTTP_MOVED_TEMP && responseCode != HttpURLConnection.HTTP_OK) {
            System.out.println("authentication failed!");
        } else {
            location = URLConnector.getLocation();
        }
        URLConnector.disconnect();

        URLConnector.connect(location, false, "GET", cookie, null);
        responseCode = URLConnector.getResponseCode();
//        System.out.println(responseCode);
        if (responseCode != HttpURLConnection.HTTP_MOVED_TEMP && responseCode != HttpURLConnection.HTTP_OK) {
            System.out.println("redirection failed!");
        }
        URLConnector.disconnect();
    }
}