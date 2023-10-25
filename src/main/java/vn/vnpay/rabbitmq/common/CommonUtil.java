package vn.vnpay.rabbitmq.common;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class CommonUtil {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private CommonUtil() {
    }

    public static boolean compareDate(Date dateExpire, Date dateNow, long expire) {
        Instant instantExpire = dateExpire.toInstant();
        Instant instantNow = dateNow.toInstant();
        long diffInSeconds = instantNow.getEpochSecond() - instantExpire.getEpochSecond();
        return diffInSeconds <= expire;
    }

    public static byte[] objectToBytes(Object object) throws IOException {
        return objectMapper.writeValueAsBytes(object);
    }

    public static <T> T bytesToObject(byte[] bytes, Class<T> clazz) throws IOException {
        return objectMapper.readValue(bytes, clazz);
    }

    public static String objectToJson(Object object) throws IOException {
        return objectMapper.writeValueAsString(object);
    }

    public static LocalDateTime convertStringToDateTime(String dateString) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        return LocalDateTime.parse(dateString, formatter);
    }
}
