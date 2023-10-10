package vn.vnpay.rabbitmq.service.impl;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import vn.vnpay.rabbitmq.bean.PaymentRecord;
import vn.vnpay.rabbitmq.common.ObjectConverter;
import vn.vnpay.rabbitmq.config.database.DatabaseConnectionPool;
import vn.vnpay.rabbitmq.config.redis.RedisConfig;
import vn.vnpay.rabbitmq.factory.PaymentRequest;

import java.io.IOException;
import java.util.Optional;

public class PaymentRecordServiceImpl {

    Logger logger = LoggerFactory.getLogger(PaymentRecordServiceImpl.class);

    public boolean save(PaymentRecord paymentRecord) {

        DatabaseConnectionPool connectionPool = DatabaseConnectionPool.getInstance();
        SessionFactory sessionFactory = connectionPool.getSessionFactory();
        Session session = null;
        Transaction transaction = null;
        try {
            session = sessionFactory.openSession();
            transaction = session.getTransaction();
            transaction.begin();
            session.save(paymentRecord);
            transaction.commit();
            return true;
        } catch (Exception ex) {
            if (transaction != null) {
                transaction.rollback();
            }
            return false;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    public boolean update(PaymentRecord paymentRecord) {
        return true;
    }

    public Optional<PaymentRecord> getById(int id) {
        DatabaseConnectionPool connectionPool = DatabaseConnectionPool.getInstance();
        SessionFactory sessionFactory = connectionPool.getSessionFactory();
        Optional<PaymentRecord> paymentRecordOptional = Optional.empty();
        try (Session session = sessionFactory.openSession()) {
            paymentRecordOptional = Optional.ofNullable(session.get(PaymentRecord.class, id));

        } catch (Exception ex) {
            logger.error("Error getting payment", ex);
        }
        return paymentRecordOptional;
    }

    public void pushRedis(PaymentRequest paymentRequest) {

        RedisConfig redisConfig = RedisConfig.getInstance();
        Jedis jedis = null;
        try {
            jedis = redisConfig.getJedisPool().getResource();
            if (!jedis.isConnected() || !jedis.ping().equals("PONG")) {
                jedis.close();
                jedis = redisConfig.getJedisPool().getResource();
            }
            jedis.setex(paymentRequest.getToken(), 120L, ObjectConverter.objectToJson(paymentRequest));
        } catch (Exception e) {
            logger.error("Error push message to redis", e);
        } finally {
            if (jedis != null) {
                redisConfig.returnConnection(jedis);
            }

        }

    }
}
