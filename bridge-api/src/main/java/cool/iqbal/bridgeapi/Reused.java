package cool.iqbal.bridgeapi;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.util.Map;

public class Reused {

    public static String toQueryParams(Object obj) throws UnsupportedEncodingException {
        StringBuilder queryString = new StringBuilder();
        Field[] fields = obj.getClass().getDeclaredFields();

        for (Field field : fields) {
            field.setAccessible(true);
            Object value;
            try {
                value = field.get(obj);
                if (value != null) {
                    if (queryString.length() > 0) {
                        queryString.append("&");
                    }
                    queryString.append(URLEncoder.encode(field.getName(), "UTF-8"))
                            .append("=")
                            .append(URLEncoder.encode(value.toString(), "UTF-8"));
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return queryString.toString();
    }

    public static String mapToQueryParam(Map<String, String> data) throws UnsupportedEncodingException {
        StringBuilder queryString = new StringBuilder();
        for (Map.Entry<String, String> entry : data.entrySet()){
            String key = entry.getKey();
            String value = entry.getValue();

            queryString.append(URLEncoder.encode(key, "UTF-8"));
            queryString.append("=");
            queryString.append(URLEncoder.encode(value, "UTF-8"));
            queryString.append("&");
        }
        if (queryString.length() > 0) {
            queryString.deleteCharAt(queryString.length() - 1);
        }
        return queryString.toString();
    }
}
