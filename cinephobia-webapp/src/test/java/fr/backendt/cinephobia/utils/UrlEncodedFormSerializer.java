package fr.backendt.cinephobia.utils;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class UrlEncodedFormSerializer {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static String serialize(Object object) {
        Map<String, String> objectFields = MAPPER.convertValue(object, Map.class);
        StringBuilder formData = new StringBuilder();

        for(String key : objectFields.keySet()) {
            String encodedKey = URLEncoder.encode(key, StandardCharsets.UTF_8);
            formData.append(encodedKey).append('=');

            Object value = objectFields.get(key);
            if(value != null) {
                String valueString = String.valueOf(value);
                String encodedValue = URLEncoder.encode(valueString, StandardCharsets.UTF_8);
                formData.append(encodedValue);
            }
            formData.append('&');
        }

        formData.deleteCharAt(formData.length() - 1);
        return formData.toString();
    }

}
