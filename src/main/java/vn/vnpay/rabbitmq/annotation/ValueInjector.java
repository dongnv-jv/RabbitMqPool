package vn.vnpay.rabbitmq.annotation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.vnpay.rabbitmq.common.PropertiesFactory;

import java.lang.reflect.Field;
import java.util.Map;

public class ValueInjector {
    private static final Logger logger = LoggerFactory.getLogger(ValueInjector.class);
    private ValueInjector() {
    }
    public static void injectValues(Object target, Map<String, Object> configValues) throws IllegalAccessException {
        Field[] fields = target.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(CustomValue.class)) {
                CustomValue customValue = field.getAnnotation(CustomValue.class);
                String key = customValue.value();
                field.setAccessible(true);
                Class<?> fieldType = field.getType();
                if (!configValues.containsKey(key)) {
                    throw new RuntimeException(String.format("Could not find key : %s of class : %s in file config.properties", key, target.getClass()));
                }
                String value = (String) configValues.get(key);
                if (value.matches("\\d+") && (fieldType.isAssignableFrom(int.class) || fieldType.isAssignableFrom(Integer.class))) {
                    int number = Integer.parseInt(value);
                    field.set(target, number);
                } else if (value.matches("^(true|false)$") && (fieldType.isAssignableFrom(boolean.class) || fieldType.isAssignableFrom(Boolean.class))) {
                    boolean input = Boolean.parseBoolean(value);
                    field.set(target, input);
                } else {
                    field.set(target, value);
                }
            }
        }
    }

    public static void injectValues(Object target) throws IllegalAccessException {
        Field[] fields = target.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(ValueKeyMap.class)) {
                ValueKeyMap valueKeyMap = field.getAnnotation(ValueKeyMap.class);
                String key = valueKeyMap.value();
                field.setAccessible(true);
                Map<String, String> valueMap = PropertiesFactory.readKeysFromPropertiesFile(key);
                if (!valueMap.isEmpty()) {
                    field.set(target, valueMap);
                } else {
                    logger.error("Failed to inject value for field {}", field.getName());
                }
            }
        }
    }

}