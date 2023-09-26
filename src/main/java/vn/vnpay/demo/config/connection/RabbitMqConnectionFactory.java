package vn.vnpay.demo.config.connection;

import com.rabbitmq.client.BlockedListener;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.vnpay.demo.common.PropertiesFactory;
import vn.vnpay.demo.config.channel.ChannelFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

public class RabbitMqConnectionFactory implements PooledObjectFactory<Connection> {
    private  String host ;
    private int port ;
    private String username ;
    private String password ;
    private String virtualHost ;
   private static RabbitMqConnectionFactory instance;
    private final Logger logger = LoggerFactory.getLogger(RabbitMqConnectionFactory.class);

    public RabbitMqConnectionFactory(String host, int port, String username, String password, String virtualHost) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.virtualHost = virtualHost;
    }

    public RabbitMqConnectionFactory() {

    }

    public ConnectionFactory connectionFactory() {
        ConnectionFactory factory = new ConnectionFactory();
        try {
            host = PropertiesFactory.getFromProperties("rabbitMq.host");
            port = Integer.parseInt(PropertiesFactory.getFromProperties("rabbitMq.port"));
            username = PropertiesFactory.getFromProperties("rabbitMq.username");
            password = PropertiesFactory.getFromProperties("rabbitMq.password");
            virtualHost = PropertiesFactory.getFromProperties("rabbitMq.virtualHost");
        } catch (Exception e) {
            logger.error("Can not read value for connectionFactory from resource with root cause ", e);
            logger.info("Parameters of connectionFactory are used with default values ");
        }
        factory.setUsername(username);
        factory.setPassword(password);
        factory.setVirtualHost(virtualHost);
        factory.setHost(host);
        factory.setPort(port);
        return factory;
    }

//    private static final class InstanceHolder {
//        private static final RabbitMqConnectionFactory instance = new RabbitMqConnectionFactory(host, port, username, password, virtualHost);
//    }
    public static RabbitMqConnectionFactory getInstance(String host, int port, String username, String password, String virtualHost) {

        if (instance == null) {
            synchronized (ChannelFactory.class) {
                if (instance == null) {
                    instance = new RabbitMqConnectionFactory(host, port, username, password, virtualHost);
                }
            }
        }

        return instance;
    }
//    public static RabbitMqConnectionFactory getInstance() {
//        return InstanceHolder.instance;
//    }

    @Override
    public PooledObject<Connection> makeObject() throws URISyntaxException, NoSuchAlgorithmException, KeyManagementException, IOException, TimeoutException {

        Connection connection = connectionFactory().newConnection();
        connection.addBlockedListener(new BlockedListener() {
            public void handleBlocked(String reason) {
                // Connection is now blocked
                logger.error(" Object Connection {} is now blocked ", connection.getId());
            }

            public void handleUnblocked() {
                // Connection is now unblocked
                logger.error(" Object Connection {} is now unblocked ", connection.getId());
            }
        });
        String id = connection.getId();
        if (id == null) {
            connection.setId(UUID.randomUUID().toString());
        }
        logger.info(" Object Connection {} is creating ", connection.getId());
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
                logger.error("Destroy Object Connection is  failed with root cause  ", e);
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
        logger.info(" Object Connection {} is returning !", connection.getId());
    }
}
