package vn.vnpay.demo.commom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.util.Properties;

public class PropertiesFactory {

    private static Logger logger = LoggerFactory.getLogger(PropertiesFactory.class);

    private PropertiesFactory() {
    }

    public static String getFromProperties(String key) {
        Properties properties = new Properties();
        try (FileInputStream input = new FileInputStream("src/main/resources/config.properties")) {
            properties.load(input);
        } catch (Throwable e) {
            logger.error(" Get properties from resource failed with root cause : {}", e.getMessage());
        }

        return properties.getProperty(key);
    }
}
