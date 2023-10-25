package vn.vnpay.rabbitmq.config.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisConfig {

    private volatile static RedisConfig instance;
    private final JedisPool jedisPool;

    public RedisConfig(String host, int port, String username, String password, int maxTotal, int minIdle, int maxIdle) {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(maxTotal);
        poolConfig.setMinIdle(minIdle);
        poolConfig.setMaxIdle(maxIdle);
        jedisPool = new JedisPool(poolConfig, host,
                port);
        jedisPool.addObjects(minIdle);
    }

    public static void initRedisConfig(String host, int port, String username, String password, int maxTotal, int minIdle, int maxIdle) {
        if (instance == null) {
            synchronized (RedisConfig.class) {
                if (instance == null) {
                    instance = new RedisConfig(host, port, username, password, maxTotal, minIdle, maxIdle);
                }
            }
        }
    }

    public static RedisConfig getInstance() {
        if (instance == null) {
            throw new IllegalStateException("JedisPool not initialized. Call init() before getInstance()");
        }
        return instance;
    }

    public JedisPool getJedisPool() {
        return jedisPool;
    }

    public void returnConnection(Jedis jedis) {
        jedis.close();
    }

}
