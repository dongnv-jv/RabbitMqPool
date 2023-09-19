package org.example.channel;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.example.connection.RabbitMqConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChannelFactory implements PooledObjectFactory<Channel> {
    private Connection connection;
    Logger logger = LoggerFactory.getLogger(ChannelFactory.class);

    public ChannelFactory(Connection connection) {
            this.connection = connection;
    }

    public PooledObject<Channel> makeObject() throws Exception {
        logger.info(" Object Channel is creating ... ");
        return new DefaultPooledObject<>(connection.createChannel());
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

    }

    public void passivateObject(PooledObject<Channel> pooledObject) {

    }
}
