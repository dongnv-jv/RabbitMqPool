package vn.vnpay.demo.common;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertiesFactory {

    private static final Logger logger = LoggerFactory.getLogger(PropertiesFactory.class);

    private PropertiesFactory() {
    }

    public static String getFromProperties(String key) {
        Properties properties = new Properties();
        try (FileInputStream input = new FileInputStream("src/main/resources/config.properties")) {
            properties.load(input);
        } catch (Throwable e) {
            logger.error(" Get properties from resource failed with root cause : ", e);
        }
        return properties.getProperty(key);
    }

    public static Map<String, String> readKeysFromPropertiesFile(String keyRaw) {
        Map<String, String> keyValueMap = new HashMap<>();
        Properties properties = new Properties();
        try (FileInputStream input = new FileInputStream("src/main/resources/config.properties")) {
            properties.load(input);
            for (String key : properties.stringPropertyNames()) {
                if (key.startsWith(keyRaw)) {
                    String value = properties.getProperty(key);
                    String mapKey = key.replace(keyRaw, "");
                    keyValueMap.put(mapKey, value);
                }
            }
        } catch (IOException e) {
            logger.error(" Get properties from resource failed with root cause : ", e);
        }
        return keyValueMap;
    }
}
