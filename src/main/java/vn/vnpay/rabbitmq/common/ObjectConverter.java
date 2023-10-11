package vn.vnpay.rabbitmq.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ObjectConverter {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private ObjectConverter() {
    }

    public static byte[] objectToBytes(Object object) throws IOException {
        return objectMapper.writeValueAsBytes(object);
    }

    public static <T> T bytesToObject(byte[] bytes, Class<T> clazz) throws IOException {
        return objectMapper.readValue(bytes, clazz);
    }

    public static <T> T convertStringToObject(String jsonString, Class<T> targetClass) throws JsonSyntaxException {
        Gson gson = new Gson();
        return gson.fromJson(jsonString, targetClass);
    }

    public static String objectToJson(Object object) throws IOException {
        return objectMapper.writeValueAsString(object);
    }

    public static LocalDateTime convertStringToDateTime(String dateString) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        return LocalDateTime.parse(dateString, formatter);
    }
}
