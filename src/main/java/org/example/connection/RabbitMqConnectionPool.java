package org.example.connection;

import com.rabbitmq.client.Connection;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.example.exception.CommonException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.NoSuchElementException;

public class RabbitMqConnectionPool implements Cloneable {

    Logger logger = LoggerFactory.getLogger(RabbitMqConnectionPool.class);
    // Số lượng tối đa được tạo ra
    private int maxTotal;
    // Số lượng connection tối thiểu trong pool
    private int minIdle;
    // Số lượng connection tối đa trong pool
    private int maxIdle;
    //
    private boolean blockWhenExhausted;

    private GenericObjectPool<Connection> internalPool;
    public static GenericObjectPoolConfig defaultConfig;

    public RabbitMqConnectionPool(int maxTotal,
                                  int minIdle,
                                  int maxIdle,
                                  boolean blockWhenExhausted,
                                  RabbitMqConnectionFactory factory
    ) {
        this.maxTotal = maxTotal;
        this.minIdle = minIdle;
        this.maxIdle = maxIdle;
        this.blockWhenExhausted = blockWhenExhausted;
        this.defaultConfig = new GenericObjectPoolConfig();
        this.defaultConfig.setMaxTotal(this.maxTotal);
        this.defaultConfig.setMaxIdle(this.maxIdle);
        this.defaultConfig.setMinIdle(this.minIdle);
        this.defaultConfig.setBlockWhenExhausted(false);

        if (this.internalPool != null) {
            try {
                closeInternalPool();
            } catch (Exception e) {
            }
        }
        this.internalPool = new GenericObjectPool<>(factory, defaultConfig);

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
            logger.info(" Quantity connection waiter in Connection pool: {}",internalPool.getNumWaiters());
            logger.info(" Quantity connection Active in Connection pool: {}",internalPool.getNumActive());
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
