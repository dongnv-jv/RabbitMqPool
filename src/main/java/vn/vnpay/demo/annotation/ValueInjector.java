package vn.vnpay.demo.annotation;

import vn.vnpay.demo.common.PropertiesFactory;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class ValueInjector {
    public static void injectValues(Object target) throws IllegalAccessException {
        Field[] fields = target.getClass().getDeclaredFields();
        for (Field field : fields) {
            if(field.isAnnotationPresent(ValueKeyMap.class)){
                ValueKeyMap valueKeyMap = field.getAnnotation(ValueKeyMap.class);
                String key = valueKeyMap.value();
                field.setAccessible(true);
                Map<String, String> valueMap = PropertiesFactory.readKeysFromPropertiesFile(key);
                if (!valueMap.isEmpty()) {
                    field.set(target, valueMap);
                }
            }
            if (field.isAnnotationPresent(CustomValue.class)) {
                CustomValue customValue = field.getAnnotation(CustomValue.class);
                String key = customValue.value();
                field.setAccessible(true);
                String value = PropertiesFactory.getFromProperties(key);
                if (value.matches("\\d+")) {
                    int number = Integer.parseInt(value);
                    field.set(target, number);
                } else if (value.matches("^(true|false)$")) {
                    boolean input = Boolean.parseBoolean(value);
                    field.set(target, input);
                } else {
                    field.set(target, value);
                }

            }
        }
    }


//    public static void injectProperties(List<Class<?>> classes) throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
//        for (Class<?> clazz : classes) {
//            if (clazz.isAnnotationPresent(Component.class)) {
//                Constructor<?> constructor = clazz.getConstructor();
//                Object instance = constructor.newInstance();
//                injectValues(instance);
//            }
//        }
//    }
}