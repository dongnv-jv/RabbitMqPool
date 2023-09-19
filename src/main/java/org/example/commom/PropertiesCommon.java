package org.example.commom;

import org.example.connection.RabbitMqConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class PropertiesCommon {
   static Logger logger = LoggerFactory.getLogger(PropertiesCommon.class);

    public static String getFromProperties(String key) {
        Properties properties = new Properties();
        try(FileInputStream input = new FileInputStream("src/main/resources/config.properties")) {
            properties.load(input);
            logger.info(" Get properties from resource success !");
        } catch (IOException e) {
            logger.error(" Get properties from resource falsed with root cause : {}", e.getMessage());
        }

        return properties.getProperty(key);
    }
}
