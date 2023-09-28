package vn.vnpay.demo.config.connection;

import com.rabbitmq.client.Connection;
import java.util.NoSuchElementException;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.vnpay.demo.exception.CommonException;

public class RabbitMqConnectionPool implements Cloneable {
    private volatile static RabbitMqConnectionPool instance;
    private final Logger logger = LoggerFactory.getLogger(RabbitMqConnectionPool.class);
    private GenericObjectPool<Connection> internalPool;

    public static RabbitMqConnectionPool getInstance(
            int maxTotal, int minIdle, int maxIdle, boolean blockWhenExhausted,
            RabbitMqConnectionFactory rabbitMqConnectionFactory) {
        if (instance == null) {
            synchronized (RabbitMqConnectionPool.class) {
                if (instance == null) {
                    instance = new RabbitMqConnectionPool(maxTotal, minIdle, maxIdle, blockWhenExhausted, rabbitMqConnectionFactory);
                }
            }
        }
        return instance;
    }

    public RabbitMqConnectionPool(int maxTotal, int minIdle, int maxIdle, boolean blockWhenExhausted,
                                  RabbitMqConnectionFactory rabbitMqConnectionFactory) {
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
            logger.error("Could not destroy the pool", e);
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
            logger.error("Could not return the Connection {} to the pool", connection.getId(), e);
        }
    }

    public Connection getConnection() {
        try {
            logger.info(" Quantity connection is not active in Connection pool: {}", internalPool.getNumIdle());
            logger.info(" Quantity connection Active in Connection pool: {}", internalPool.getNumActive());
            return internalPool.borrowObject(5000);
        } catch (NoSuchElementException nse) {
            logger.error("The exception was caused by an exhausted pool");
            throw new CommonException("Could not get a resource from the Connection pool", nse);
        } catch (Exception e) {
            throw new CommonException("Could not get a resource from the Connection pool", e);
        }
    }

}
