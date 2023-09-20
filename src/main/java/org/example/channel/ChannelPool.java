package org.example.channel;

import com.rabbitmq.client.Channel;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.example.commom.PropertiesCommon;
import org.example.exception.CommonException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.NoSuchElementException;

public class ChannelPool implements Cloneable {


    private static ChannelPool instance;
    Logger logger = LoggerFactory.getLogger(ChannelPool.class);

    // Số lượng channel tối đa được tạo ra
    private int maxTotal = Integer.parseInt(PropertiesCommon.getFromProperties("channel.pool.maxTotal"));
    // Số lượng Channel tối thiểu trong pool
    private int minIdle = Integer.parseInt(PropertiesCommon.getFromProperties("channel.pool.minIdle"));
    // Số lượng Channel tối đa trong pool
    private int maxIdle = Integer.parseInt(PropertiesCommon.getFromProperties("channel.pool.maxIdle"));
    //
    private boolean blockWhenExhausted = Boolean.parseBoolean(PropertiesCommon.getFromProperties("channel.pool.blockWhenExhausted"));


    private GenericObjectPool<Channel> internalPool;
    public GenericObjectPoolConfig<Channel> defaultConfig;
    public static ChannelPool getInstance() throws Exception {
        if (instance == null) {
            instance = new ChannelPool();
        }
        return instance;
    }

    public GenericObjectPool<Channel> getInternalPool() {
        return internalPool;
    }

    public ChannelPool() throws Exception {
        ChannelFactory factory = ChannelFactory.getInstance();

        if (internalPool != null) {
            try {
                closeInternalPool();
            } catch (Exception e) {
                logger.error("Create InternalPool fail with root cause {}", e.getMessage());
            }
        }
        defaultConfig = new GenericObjectPoolConfig<>();
        defaultConfig.setMaxTotal(maxTotal);
        defaultConfig.setMinIdle(minIdle);
        defaultConfig.setMaxIdle(maxIdle);
        defaultConfig.setBlockWhenExhausted(blockWhenExhausted);
        internalPool = new GenericObjectPool<>(factory, defaultConfig);
        for (int i = 0; i < defaultConfig.getMinIdle(); i++) {
            internalPool.addObject();
        }
        logger.info("Create InternalPool with {} Channel in Pool", internalPool.getNumIdle());
    }

    private void closeInternalPool() {
        try {
            internalPool.close();
            logger.info("InternalPool is close !");
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
            logger.info(" Quantity Channel is borrowed in Channel pool: {}", internalPool.getBorrowedCount());
            return internalPool.borrowObject();
        } catch (NoSuchElementException nse) {
            if (null == nse.getCause()) {
                logger.error("The exception was caused by an exhausted pool");
                throw new CommonException("Could not get a resource since the pool is exhausted", nse);
            }
            throw new CommonException("Could not get a resource from the pool", nse);
        } catch (Exception e) {
            throw new CommonException("Could not get a resource from the pool", e);
        }
    }

}
