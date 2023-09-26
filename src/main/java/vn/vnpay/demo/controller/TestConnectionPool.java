//package vn.vnpay.demo.controller;
//
//import com.rabbitmq.client.Connection;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import vn.vnpay.demo.config.connection.RabbitMqConnectionPool;
//
//public class TestConnectionPool {
//
//    static Logger logger = LoggerFactory.getLogger(TestConnectionPool.class);
//
//    public static void main(String[] args) {
//
//        try {
//            for (int i = 0; i < 5; i++) {
//                new Thread(() -> {
//
//                    RabbitMqConnectionPool rabbitMqConnectionPool = RabbitMqConnectionPool.getInstance();
//                    Connection connection = rabbitMqConnectionPool.getConnection();
//                    rabbitMqConnectionPool.returnConnection(connection);
//                }).start();
//            }
//        } catch (Exception e) {
//            logger.error(" Create connection failed with root cause ", e);
//        }
//
//
//    }
//}
