package vn.vnpay.demo.config.channelpoolconfig;

import com.rabbitmq.client.Channel;
import java.util.NoSuchElementException;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.vnpay.demo.common.PropertiesFactory;
import vn.vnpay.demo.exception.CommonException;

public class ChannelPool implements Cloneable {


    private volatile static ChannelPool instance;
    private final Logger logger = LoggerFactory.getLogger(ChannelPool.class);
    private GenericObjectPool<Channel> internalPool;

    public static ChannelPool getInstance() {
        if (instance == null) {
            synchronized (ChannelPool.class) {
                if (instance == null) {
                    instance = new ChannelPool();
                }
            }

        }
        return instance;
    }

    public GenericObjectPool<Channel> getInternalPool() {
        return internalPool;
    }

    public ChannelPool() {
        ChannelFactory factory = ChannelFactory.getInstance();
        int maxTotal = 5;
        int minIdle = 5;
        int maxIdle = 5;
        boolean blockWhenExhausted = true;
        if (internalPool != null) {
            try {
                closeInternalPool();
            } catch (Exception e) {
                logger.error("Create InternalPool fail with root cause ", e);
            }
        }
        try {
            maxTotal = Integer.parseInt(PropertiesFactory.getFromProperties("channel.pool.maxTotal"));
            minIdle = Integer.parseInt(PropertiesFactory.getFromProperties("channel.pool.minIdle"));
            maxIdle = Integer.parseInt(PropertiesFactory.getFromProperties("channel.pool.maxIdle"));
            blockWhenExhausted = Boolean.parseBoolean(PropertiesFactory.getFromProperties("channel.pool.blockWhenExhausted"));
        } catch (Exception e) {
            logger.error("Can not read value for ChannelPool from resource with root cause ", e);
            logger.info("Parameters of ChannelPool are used with default values ");
        }
        GenericObjectPoolConfig<Channel> defaultConfig = new GenericObjectPoolConfig<>();
        defaultConfig.setMaxTotal(maxTotal);
        defaultConfig.setMinIdle(minIdle);
        defaultConfig.setMaxIdle(maxIdle);
        defaultConfig.setBlockWhenExhausted(blockWhenExhausted);
        internalPool = new GenericObjectPool<>(factory, defaultConfig);
        try {
            for (int i = 0; i < defaultConfig.getMinIdle(); i++) {
                internalPool.addObject();
            }
        } catch (Exception e) {
            logger.error("Can not add Object to ChannelPool with root cause ", e);
        }
        logger.info("Create InternalPool with {} Channel in Pool", internalPool.getNumIdle());
    }

    private void closeInternalPool() {
        try {
            internalPool.close();
            logger.info("InternalPool is close !");
        } catch (Exception e) {
            logger.error("Could not destroy the pool with root cause ", e);
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
            logger.error("Could not return the channel {} to the pool ith root cause ", channel.getChannelNumber(), e);
        }
    }

    public Channel getChannel() {
        try {
            logger.info(" Quantity Channel is borrowed in Channel pool: {}", internalPool.getBorrowedCount());
            return internalPool.borrowObject();
        } catch (NoSuchElementException nse) {
            if (null == nse.getCause()) {
                logger.error("The exception was caused by an exhausted pool", nse);

            }
            throw new CommonException("Could not get Channel from the pool", nse);
        } catch (Exception e) {
            throw new CommonException("Could not get Channel from the pool", e);
        }
    }

}
