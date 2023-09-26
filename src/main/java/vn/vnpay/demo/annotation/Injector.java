package vn.vnpay.demo.annotation;

import java.lang.reflect.Field;

public class Injector {
    public static void inject(Object target, Object objectToInject) throws IllegalAccessException {
        Class<?> targetClass = target.getClass();
        Field[] fields = targetClass.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(InjectObject.class)) {
                field.setAccessible(true);
                field.set(target, objectToInject);
            }
        }
    }
}