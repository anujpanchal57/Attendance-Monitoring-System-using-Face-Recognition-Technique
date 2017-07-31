/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package SendSms;

/**
 *
 * @author Mahendra
 */
import java.net.URLEncoder;
import java.util.ArrayList;

public class Credentials {

    public ArrayList<String> list = new ArrayList<>();

    public void set(String name, String value) {
        StringBuilder buffer = new StringBuilder();
        buffer.append(name);
        buffer.append("=");
        buffer.append(getUTF8String(value));
        add(buffer.toString());
    }

    public void append(String name, String value) {
        StringBuilder buffer = new StringBuilder();
        buffer.append("&");
        buffer.append(name);
        buffer.append("=");
        buffer.append(getUTF8String(value));
        add(buffer.toString());
    }

    private void add(String item) {
        list.add(item);
    }

    public String getUTF8String(String value) {
        String encodedValue = null;
        try {
            encodedValue = URLEncoder.encode(value, "UTF-8");
        } catch (Exception e) {
            System.err.println("Encoding error");
        }
        return encodedValue;
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    public void reset() {
        list.clear();
    }

    public String getUserCredentials() {
        StringBuilder buffer = new StringBuilder();
        int size = list.size();
        for (int i = 0; i < size; i++) {
            buffer.append(list.get(i));
        }
        return buffer.toString();
    }
}