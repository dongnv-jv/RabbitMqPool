package vn.vnpay.rabbitmq.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.vnpay.rabbitmq.annotation.Component;
import vn.vnpay.rabbitmq.annotation.CustomValue;
import vn.vnpay.rabbitmq.config.channel.ChannelFactory;
import vn.vnpay.rabbitmq.config.channel.ChannelPool;
import vn.vnpay.rabbitmq.config.connection.RabbitMqConnectionFactory;
import vn.vnpay.rabbitmq.config.connection.RabbitMqConnectionPool;
import vn.vnpay.rabbitmq.config.redis.RedisConfig;

@Component
public class CommonConfig {
    Logger logger = LoggerFactory.getLogger(CommonConfig.class);
    @CustomValue("rabbitMq.host")
    private String host;
    @CustomValue("rabbitMq.port")
    private int port;
    @CustomValue("rabbitMq.username")
    private String username;
    @CustomValue("rabbitMq.password")
    private String password;
    @CustomValue("rabbitMq.virtualHost")
    private String virtualHost;
    @CustomValue("connection.pool.maxTotal")
    private int maxTotalConnPool;
    @CustomValue("connection.pool.maxIdle")
    private int minIdleConnPool;
    @CustomValue("connection.pool.minIdle")
    private int maxIdleConnPool;
    @CustomValue("connection.pool.blockWhenExhausted")
    private boolean blockWhenExhaustedConnPool;
    @CustomValue("channel.pool.maxTotal")
    private int maxTotalChannelPool;
    @CustomValue("channel.pool.maxIdle")
    private int minIdleChannelPool;
    @CustomValue("channel.pool.minIdle")
    private int maxIdleChannelPool;
    @CustomValue("channel.pool.blockWhenExhausted")
    private boolean blockWhenExhaustedChannelPool;

    @CustomValue("redis.host")
    private String redisHost;
    @CustomValue("redis.port")
    private int redisPort;
    @CustomValue("redis.username")
    private String redisUsername;
    @CustomValue("redis.password")
    private String redisPassword;
    @CustomValue("connection.pool.redis.maxTotal")
    private int redisMaxTotalConnPool;
    @CustomValue("connection.pool.redis.maxIdle")
    private int redisMinIdleConnPool;
    @CustomValue("connection.pool.redis.minIdle")
    private int redisMaxIdleConnPool;

    public void configure() {
        try {
            RedisConfig.initRedisConfig(redisHost, redisPort, redisUsername, redisPassword, redisMaxTotalConnPool, redisMinIdleConnPool, redisMaxIdleConnPool);

            RabbitMqConnectionFactory connectionFactoryFactory = RabbitMqConnectionFactory.getInstance(host, port, username, password, virtualHost);
            RabbitMqConnectionPool.initConnectionPool(maxTotalConnPool, minIdleConnPool, maxIdleConnPool, blockWhenExhaustedConnPool, connectionFactoryFactory);
            RabbitMqConnectionPool rabbitMqConnectionPool = RabbitMqConnectionPool.getInstance();
            ChannelFactory channelFactory = ChannelFactory.getInstance(rabbitMqConnectionPool);
            ChannelPool.initChannelPool(maxTotalChannelPool, minIdleChannelPool, maxIdleChannelPool, blockWhenExhaustedChannelPool, channelFactory);
        } catch (Exception e) {
            logger.error("Failed to configure RabbitMQ ", e);
        }

    }

}
// cấu hình config động
// Hàm config không trả về void => có thể không câ hình được nhưng code vẫn chạy
//