package vn.vnpay.rabbitmq.service.impl;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;
import vn.vnpay.rabbitmq.annotation.Component;
import vn.vnpay.rabbitmq.bean.PaymentRecord;
import vn.vnpay.rabbitmq.common.ObjectConverter;
import vn.vnpay.rabbitmq.config.database.DatabaseConnectionPool;
import vn.vnpay.rabbitmq.config.redis.RedisConfig;
import vn.vnpay.rabbitmq.factory.PaymentRequest;
import vn.vnpay.rabbitmq.service.IPaymentRecordService;

import java.util.Optional;

@Component
public class PaymentRecordServiceImpl implements IPaymentRecordService {

    Logger logger = LoggerFactory.getLogger(PaymentRecordServiceImpl.class);

    public PaymentRecord savePaymentRecord(PaymentRecord paymentRecord) {
        DatabaseConnectionPool connectionPool = DatabaseConnectionPool.getInstance();
        SessionFactory sessionFactory = connectionPool.getSessionFactory();
        Transaction transaction = null;

        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            session.save(paymentRecord);
            transaction.commit();
            logger.info("Payment Record saved successfully with paymentId {}", paymentRecord.getId());
            return paymentRecord;
        } catch (HibernateException ex) {
            logger.error("Error while saving payment record to database", ex);
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            return new PaymentRecord();
        }
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

    public boolean pushRedis(PaymentRequest paymentRequest) {
        RedisConfig redisConfig = RedisConfig.getInstance();
        Jedis jedis = null;
        try {
            jedis = redisConfig.getJedisPool().getResource();
            String result = jedis.setex(paymentRequest.getToken(), 120L, ObjectConverter.objectToJson(paymentRequest));
            return "OK".equalsIgnoreCase(result);
        } catch (JedisConnectionException e) {
            logger.error("Error connecting to Redis", e);
            return false;
        } catch (Exception e) {
            logger.error("Error push message to redis", e);
            return false;
        } finally {
            if (jedis != null) {
                redisConfig.returnConnection(jedis);
            }
        }
    }

}
