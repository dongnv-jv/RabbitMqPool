package vn.vnpay.rabbitmq.service.impl;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;
import vn.vnpay.rabbitmq.annotation.Autowire;
import vn.vnpay.rabbitmq.annotation.Component;
import vn.vnpay.rabbitmq.bean.PaymentRecord;
import vn.vnpay.rabbitmq.common.CommonUtil;
import vn.vnpay.rabbitmq.config.database.DatabaseConnectionPool;
import vn.vnpay.rabbitmq.config.redis.RedisConfig;
import vn.vnpay.rabbitmq.factory.PaymentRequest;
import vn.vnpay.rabbitmq.service.IPaymentRecordService;

@Component
public class PaymentRecordServiceImpl implements IPaymentRecordService {

    private final Logger logger = LoggerFactory.getLogger(PaymentRecordServiceImpl.class);
    @Autowire
    private DatabaseConnectionPool connectionPool;
    @Autowire
    private RedisConfig redisConfig;

    public PaymentRecord savePaymentRecord(PaymentRecord paymentRecord, String correlationId) {
        String logId = vn.vnpay.rabbitmq.service.impl.ExchangeMessageServiceImpl.logIdThreadLocal.get();
        SessionFactory sessionFactory = connectionPool.getSessionFactory();
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            session.save(paymentRecord);
            transaction.commit();
            logger.info("[{}] - Save paymentRecord with correlationId:with correlationId: {} successfully with paymentId {}", logId, correlationId, paymentRecord.getId());
            return paymentRecord;
        } catch (HibernateException ex) {
            logger.error("Error while saving payment record to database", ex);
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            return new PaymentRecord();
        }
    }

    public boolean pushRedis(PaymentRequest paymentRequest) {
        Jedis jedis = null;
        String logId = vn.vnpay.rabbitmq.service.impl.ExchangeMessageServiceImpl.logIdThreadLocal.get();
        try {
            jedis = redisConfig.getJedisPool().getResource();
            String keyRedis = paymentRequest.getToken();
            String result = jedis.setex(keyRedis, 120L, CommonUtil.objectToJson(paymentRequest));
            boolean isPushMessageSuccessfully = "OK".equalsIgnoreCase(result);
            if (isPushMessageSuccessfully) {
                logger.info("[{}] - Push paymentRequest with correlationId: {} to Redis successfully !", logId, paymentRequest.getToken());
            }
            return isPushMessageSuccessfully;
        } catch (JedisConnectionException e) {
            logger.error("[{}] - Error connecting to Redis", logId, e);
            return false;
        } catch (Exception e) {
            logger.error("[{}] - Error push message to redis", logId, e);
            return false;
        } finally {
            if (jedis != null) {
                redisConfig.returnConnection(jedis);
            }
        }
    }

}
