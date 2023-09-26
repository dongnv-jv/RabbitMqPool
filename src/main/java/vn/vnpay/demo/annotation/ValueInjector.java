package vn.vnpay.demo.annotation;

import vn.vnpay.demo.common.PropertiesFactory;

import java.lang.reflect.Field;

public class ValueInjector {
    public static void injectValues(Object target) throws IllegalAccessException {
        Field[] fields = target.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(CustomValue.class)) {
                CustomValue customValue = field.getAnnotation(CustomValue.class);
                String key = customValue.value();
                String value = PropertiesFactory.getFromProperties(key);
                field.setAccessible(true);
                if (value.matches("\\d+")) {
                    int number = Integer.parseInt(value);
                    field.set(target, number);
                } else {
                    field.set(target, value);
                }

            }
        }
    }
}