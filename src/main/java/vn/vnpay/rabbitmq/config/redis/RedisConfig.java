package vn.vnpay.rabbitmq.config.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisConfig {

    private JedisPool jedisPool;
    private volatile static RedisConfig instance;

    public RedisConfig(String host, int port, String username, String password, int maxTotal, int minIdle, int maxIdle) {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(maxTotal);
        poolConfig.setMinIdle(minIdle);
        poolConfig.setMaxIdle(maxIdle);
        jedisPool = new JedisPool(poolConfig, host,
                port, username, password);
    }

    public JedisPool getJedisPool() {
        return jedisPool;
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

    public void returnConnection(Jedis jedis) {
        jedisPool.returnResource(jedis);
    }

    public static RedisConfig getInstance() {
        if (instance == null) {
            throw new IllegalStateException("JedisPool not initialized. Call init() before getInstance()");
        }
        return instance;
    }

}
