package org.example.channel;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.example.connection.RabbitMqConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChannelFactory implements PooledObjectFactory<Channel> {

    private static ChannelFactory instance;
    private final Connection connection;
    Logger logger = LoggerFactory.getLogger(ChannelFactory.class);

    public static ChannelFactory getInstance() throws Exception {

        RabbitMqConnectionPool rabbitMqConnectionPool = RabbitMqConnectionPool.getInstance();
        Connection connection1 = rabbitMqConnectionPool.getConnection();
        if (instance == null) {
            instance = new ChannelFactory(connection1);
        }
        rabbitMqConnectionPool.returnConnection(connection1);
        return instance;
    }

    public ChannelFactory(Connection connection) {
            this.connection = connection;
    }

    public PooledObject<Channel> makeObject() throws Exception {
        Channel channel= connection.createChannel();
        logger.info(" Object Channel {} is creating ", channel.getChannelNumber());
        return new DefaultPooledObject<>(channel);
    }

    public void destroyObject(PooledObject<Channel> pooledObject) {
        final Channel channel = pooledObject.getObject();
        if (channel.isOpen()) {
            try {
                channel.close();
            } catch (Exception e) {

            }
        }
    }
    public boolean validateObject(PooledObject<Channel> pooledObject) {
        final Channel channel = pooledObject.getObject();
        return channel.isOpen();
    }

    public void activateObject(PooledObject<Channel> pooledObject) {
        logger.info(" Object Channel {} is calling ", pooledObject.getObject().getChannelNumber());
    }

    public void passivateObject(PooledObject<Channel> pooledObject) {
        logger.info(" Object Channel {} is returning ", pooledObject.getObject().getChannelNumber());
    }
}
