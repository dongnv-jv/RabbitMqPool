package vn.vnpay.rabbitmq.scan;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.vnpay.rabbitmq.annotation.Autowire;
import vn.vnpay.rabbitmq.annotation.Component;
import vn.vnpay.rabbitmq.annotation.ValueInjector;
import vn.vnpay.rabbitmq.config.CommonConfig;

import java.io.FileInputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class ApplicationContext {
    private static final Logger logger = LoggerFactory.getLogger(ApplicationContext.class);
    private final Map<Class<?>, Object> beans = new HashMap<>();
    private final Map<String, Object> configValues = new HashMap<>();

    public ApplicationContext(String basePackage) throws Exception {
        loadConfigValues();
        List<Class<?>> classes = PackageScanner.getClasses(basePackage);
        for (Class<?> clazz : classes) {
            if (clazz.isAnnotationPresent(Component.class)) {
                Constructor<?> constructor = clazz.getConstructor();
                Object instance = constructor.newInstance();
                ValueInjector.injectValues(instance, configValues);
                ValueInjector.injectValues(instance);
                beans.put(clazz, instance);
            }
        }
        CommonConfig commonConfig = getBean(CommonConfig.class);
        commonConfig.configure(beans);
        this.injectAutowire(classes, basePackage);
    }

    private void injectAutowire(List<Class<?>> classes, String basePackage) throws ClassNotFoundException {
        for (Class<?> clazz : classes) {
            Field[] fields = clazz.getDeclaredFields();
            Object clazzInject = getBean(clazz);
            for (Field field : fields) {
                if (field.isAnnotationPresent(Autowire.class)) {
                    if (field.getType().isInterface()) {
                        List<Class<?>> classList = getClassesImplementingInterface(field.getType(), basePackage);
                        classList.stream().findFirst().ifPresent(t -> {
                            Object bean = getBean(classList.get(0));
                            this.injectBeans(field, clazzInject, clazz, bean);
                        });
                    } else {
                        Object bean = getBean(field.getType());
                        this.injectBeans(field, clazzInject, clazz, bean);
                    }
                }
            }
        }
    }

    private void injectBeans(Field field, Object clazzInject, Class<?> clazz, Object bean) {
        if (bean != null) {
            field.setAccessible(true);
            try {
                field.set(clazzInject, bean);
                beans.replace(clazz, clazzInject);
            } catch (IllegalAccessException e) {
                logger.error("Could not set bean {} to class {}", bean, clazz, e);
            }
        }
    }

    public <T> T getBean(Class<T> clazz) {
        return clazz.cast(beans.get(clazz));
    }

    public void setBean(Class<?> clazz, Object bean) {
        this.beans.put(clazz, bean);
    }

    public void loadConfigValues() {
        Properties properties = new Properties();
        try (FileInputStream input = new FileInputStream("src/main/resources/config.properties")) {
            properties.load(input);
            for (String key : properties.stringPropertyNames()) {
                String value = properties.getProperty(key);
                configValues.put(key, value);
            }
        } catch (Exception e) {
            logger.error(" Get properties from resource failed with root cause : ", e);
        }
    }

    public List<Class<?>> getClassesImplementingInterface(Class<?> interfaze, String packageName) throws ClassNotFoundException {
        List<Class<?>> implementers = new ArrayList<>();
        List<Class<?>> classes = PackageScanner.getClasses(packageName);
        for (Class<?> clazz : classes) {
            if (interfaze.isAssignableFrom(clazz) && !clazz.isInterface()) {
                implementers.add(clazz);
            }
        }
        return implementers;
    }
}
