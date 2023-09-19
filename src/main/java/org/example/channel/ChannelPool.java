package org.example.channel;

import com.rabbitmq.client.Channel;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.example.exception.CommonException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.NoSuchElementException;

public class ChannelPool implements Cloneable {

    Logger logger = LoggerFactory.getLogger(ChannelPool.class);
    private GenericObjectPool<Channel> internalPool;
    public static GenericObjectPoolConfig defaultConfig;

    static {
        defaultConfig = new GenericObjectPoolConfig();
        defaultConfig.setMaxTotal(25);
        defaultConfig.setMinIdle(7);
        defaultConfig.setMaxIdle(10);
        defaultConfig.setBlockWhenExhausted(false);
    }

    public GenericObjectPool<Channel> getInternalPool() {
        return internalPool;
    }

    public ChannelPool(ChannelFactory channelFactory) {
        if (this.internalPool != null) {
            try {
                closeInternalPool();
            } catch (Exception e) {
            }
        }

        this.internalPool = new GenericObjectPool<Channel>(channelFactory, defaultConfig);
    }

    private void closeInternalPool() {
        try {
            internalPool.close();
        } catch (Exception e) {
            throw new CommonException("Could not destroy the pool", e);
        }
    }

    public void returnChannel(Channel channel) {
        try {
            if (channel.isOpen()) {
                internalPool.returnObject(channel);
            } else {
                internalPool.invalidateObject(channel);
            }
        } catch (Exception e) {
            throw new CommonException("Could not return the resource to the pool", e);
        }
    }

    public Channel getChannel() {
        try {
            logger.info(" Quantity Channel waiter in Channel pool: {}",internalPool.getNumWaiters());
            logger.info(" Quantity Channel Active in Channel pool: {}",internalPool.getNumActive());
            return internalPool.borrowObject();
        } catch (NoSuchElementException nse) {
            if (null == nse.getCause()) {
                logger.error("The exception was caused by an exhausted pool");
                throw new CommonException("Could not get a resource since the pool is exhausted", nse);
            }
            // Otherwise, the exception was caused by the implemented activateObject() or ValidateObject()
            throw new CommonException("Could not get a resource from the pool", nse);
        } catch (Exception e) {
            throw new CommonException("Could not get a resource from the pool", e);
        }
    }
}
