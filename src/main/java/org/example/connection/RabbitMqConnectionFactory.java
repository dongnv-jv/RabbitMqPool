package org.example.connection;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.example.commom.PropertiesCommon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

public class RabbitMqConnectionFactory implements PooledObjectFactory<Connection> {

    private static RabbitMqConnectionFactory instance;

    Logger logger = LoggerFactory.getLogger(RabbitMqConnectionFactory.class);

    public ConnectionFactory connectionFactory() throws URISyntaxException, NoSuchAlgorithmException, KeyManagementException {
        ConnectionFactory factory = new ConnectionFactory();
        URI uriConnect = new URI(PropertiesCommon.getFromProperties("rabbitMq.uri"));
        factory.setUri(uriConnect);
        return factory;

    }

    public static RabbitMqConnectionFactory getInstance() {
        if (instance == null) {
            instance = new RabbitMqConnectionFactory();
        }
        return instance;
    }

    @Override
    public PooledObject<Connection> makeObject() throws URISyntaxException, NoSuchAlgorithmException, KeyManagementException, IOException, TimeoutException {

        Connection connection = connectionFactory().newConnection();
        String id = connection.getId();
        if (id == null) {
            connection.setId(UUID.randomUUID().toString());
        }
        logger.info(" Object Connection {} is creating ",connection.getId());
        return new DefaultPooledObject<>(connection);
    }

    @Override
    public void destroyObject(PooledObject<Connection> pooledObject) {
        final Connection connection = pooledObject.getObject();
        if (connection.isOpen()) {
            try {
                logger.info(" Object Connection is destroying  ... ");
                connection.close();
            } catch (Exception e) {
                logger.error("Destroy Object Connection is  failed with root cause {} ", e.getMessage());
            }
        }
    }

    @Override
    public boolean validateObject(PooledObject<Connection> pooledObject) {
        final Connection connection = pooledObject.getObject();
        return connection.isOpen();
    }

    @Override
    public void activateObject(PooledObject<Connection> pooledObject) {
        Connection connection = pooledObject.getObject();
        logger.info(" Object Connection {} is calling !", connection.getId());
    }

    @Override
    public void passivateObject(PooledObject<Connection> pooledObject) {
        Connection connection = pooledObject.getObject();
        logger.info(" Object Connection {} is returnning !", connection.getId());
    }
}
