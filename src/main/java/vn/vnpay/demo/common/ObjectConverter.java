package vn.vnpay.demo.common;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

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
}
