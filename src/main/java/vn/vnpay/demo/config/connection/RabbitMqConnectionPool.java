package vn.vnpay.demo.config.connection;

import com.rabbitmq.client.Connection;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import vn.vnpay.demo.common.PropertiesFactory;
import vn.vnpay.demo.exception.CommonException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.NoSuchElementException;

public class RabbitMqConnectionPool implements Cloneable {

    private final Logger logger = LoggerFactory.getLogger(RabbitMqConnectionPool.class);
    private GenericObjectPool<Connection> internalPool;


    private static final class InstanceHolder {
        private static final RabbitMqConnectionPool instance = new RabbitMqConnectionPool();
    }

    public static RabbitMqConnectionPool getInstance() {
        return InstanceHolder.instance;
    }

    public RabbitMqConnectionPool()  {
        RabbitMqConnectionFactory rabbitMqConnectionFactory = RabbitMqConnectionFactory.getInstance();
        int maxTotal = 5;
        int minIdle = 5;
        int maxIdle = 5;
        boolean blockWhenExhausted = true;

        try {
            maxTotal = Integer.parseInt(PropertiesFactory.getFromProperties("connection.pool.maxTotal"));
            minIdle = Integer.parseInt(PropertiesFactory.getFromProperties("connection.pool.minIdle"));
            maxIdle = Integer.parseInt(PropertiesFactory.getFromProperties("connection.pool.maxIdle"));
            blockWhenExhausted = Boolean.parseBoolean(PropertiesFactory.getFromProperties("connection.pool.blockWhenExhausted"));
        } catch (Exception e) {
            logger.error("Can not read value for ConnectionPool from resource with root cause ", e);
            logger.info("Parameters of ConnectionPool are used with default values ");
        }

        GenericObjectPoolConfig<Connection> defaultConfig = new GenericObjectPoolConfig<>();
        defaultConfig.setMaxTotal(maxTotal);
        defaultConfig.setMaxIdle(maxIdle);
        defaultConfig.setMinIdle(minIdle);
        defaultConfig.setBlockWhenExhausted(blockWhenExhausted);

        if (this.internalPool != null) {
            try {
                closeInternalPool();
            } catch (Exception e) {
                logger.error("Create InternalPool fail with root cause ", e);
            }
        }
        internalPool = new GenericObjectPool<>(rabbitMqConnectionFactory, defaultConfig);
        try {
            internalPool.preparePool(); // Tạo ra số lượng đối tượng bằng minIdle và đưa chúng vào pool
        } catch (Exception e) {
            logger.error("Create InternalPool fail with root cause ", e);
        }
    }

    private void closeInternalPool() {
        try {
            internalPool.close();
        } catch (Exception e) {
            throw new CommonException("Could not destroy the pool", e);
        }
    }

    public void returnConnection(Connection connection) {
        try {
            if (connection.isOpen()) {
                logger.info(" Quantity connection Active in Connection pool: {}", internalPool.getNumActive());
                internalPool.returnObject(connection);
            } else {
                internalPool.invalidateObject(connection);
            }
        } catch (Exception e) {
            throw new CommonException("Could not return the resource to the pool", e);
        }
    }

    public Connection getConnection() {
        try {
            logger.info(" Quantity connection is not active in Connection pool: {}", internalPool.getNumIdle());
            logger.info(" Quantity connection Active in Connection pool: {}", internalPool.getNumActive());
            return internalPool.borrowObject();
        } catch (NoSuchElementException nse) {
            if (null == nse.getCause()) {
                logger.error("The exception was caused by an exhausted pool");
                throw new CommonException("Could not get a resource since the pool is exhausted", nse);
            }
            logger.error("the exception was caused by the implemented activateObject() or ValidateObject()");
            throw new CommonException("Could not get a resource from the pool", nse);
        } catch (Exception e) {
            throw new CommonException("Could not get a resource from the pool", e);
        }
    }

    public GenericObjectPool<Connection> getInternalPool() {
        return internalPool;
    }


}
