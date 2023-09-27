package vn.vnpay.demo.service.impl;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.vnpay.demo.annotation.CustomValue;
import vn.vnpay.demo.common.CommonConstant;
import vn.vnpay.demo.common.ObjectConverter;
import vn.vnpay.demo.common.PropertiesFactory;
import vn.vnpay.demo.config.channel.ChannelPool;
import vn.vnpay.demo.config.threadpool.ThreadPoolConfig;

import vn.vnpay.demo.domain.Student;
import vn.vnpay.demo.factory.BaseExchange;
import vn.vnpay.demo.factory.DirectExchange;
import vn.vnpay.demo.factory.FanoutExchange;
import vn.vnpay.demo.factory.HeaderExchange;
import vn.vnpay.demo.factory.TopicExchange;
import vn.vnpay.demo.service.ExchangeMessageService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

public class ExchangeMessageServiceImpl implements ExchangeMessageService {
    private final Logger logger = LoggerFactory.getLogger(ExchangeMessageServiceImpl.class);
    static String charSet = PropertiesFactory.getFromProperties("charset.Name");
    AtomicInteger count = new AtomicInteger(0);
    @CustomValue("exchange.direct.name")
    private String exchangeDirect ;
    @CustomValue("exchange.direct.routingKey")
    private String routingKey ;
    @CustomValue("exchange.direct.queueName")
    private String queueName ;



    public void sendMessage(Object message, BaseExchange exchange) {
        long start = System.currentTimeMillis();
        logger.info("Start sendToExchange in ExchangeMessageServiceImpl ");
        Executor executor = ThreadPoolConfig.getExecutor();
        exchange.createExchangeAndQueue();
        for (int i = 0; i < 100; i++) {
            this.sendToExchange(message, exchange, executor);
        }
        long end = System.currentTimeMillis();
        logger.info("Process sendToExchange in ExchangeMessageServiceImpl take {} millisecond", (end - start));
    }

    private void sendToExchange(Object message, BaseExchange exchange, Executor executor) {

        executor.execute(() -> {
            try {
                ChannelPool channelPool = ChannelPool.getInstance();

                Channel channel = channelPool.getChannel();
                if (exchange instanceof HeaderExchange) {
                    logger.info("Process sendToExchange in ExchangeMessageServiceImpl with header Exchange");
                    AMQP.BasicProperties props = new AMQP.BasicProperties();
                    Map<String, Object> map = new HashMap<>();
                    map.put("Fourth", "D");
                    map.put("Second", "B");
                    props = props.builder().headers(map).build();
                    channel.basicPublish(CommonConstant.EXCHANGE_HEADER, CommonConstant.ROUTING_KEY, props, (message+ "  " + count.incrementAndGet()).getBytes());
                }
                if (exchange instanceof DirectExchange) {
                    logger.info("Process sendToExchange in ExchangeMessageServiceImpl with DirectExchange");
                    channel.basicPublish(exchangeDirect, routingKey, null, ObjectConverter.objectToBytes(message));
                    logger.info(" Message Sent from direct exchange : {}", message);

                }
                if (exchange instanceof FanoutExchange) {
                    logger.info("Process sendToExchange in ExchangeMessageServiceImpl with FanoutExchange");
                    channel.basicPublish(CommonConstant.EXCHANGE_FANOUT, CommonConstant.ROUTING_KEY, null,(message+ "  " + count.incrementAndGet()).getBytes());
                    logger.info(" Message Sent from Fanout exchange : {}", message);

                }
                if (exchange instanceof TopicExchange) {
                    logger.info("Process sendToExchange in ExchangeMessageServiceImpl with TopicExchange");
                    channel.basicPublish(CommonConstant.EXCHANGE_TOPIC, CommonConstant.ROUTING_KEY_2, null,(message+ "  " + count.incrementAndGet()).getBytes());
                    logger.info(" Message Sent from Topic exchange : {}", message);
                }
                channelPool.returnChannel(channel);
            } catch (Exception e) {
                logger.error(" Send message to exchange failed with root cause ", e);
            }
        });
    }

    public void getMessageFromQueue(String queueName) {
        long start = System.currentTimeMillis();
        logger.info("Start getMessageFromQueue in ExchangeMessageServiceImpl ");
        Executor executor = ThreadPoolConfig.getExecutor();
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    getMessageFromQueue(queueName, executor);
                } catch (Exception e) {
                    logger.error(" Receiver message from queue {} failed with root cause ", queueName, e);
                }
            });
            futures.add(future);
        }

        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        allOf.join();

        long end = System.currentTimeMillis();
        logger.info("Process getMessageFromQueue in ExchangeMessageServiceImpl take {} millisecond", (end - start));

    }

    private void getMessageFromQueue(String queueName, Executor executor) {
        ChannelPool channelPool = ChannelPool.getInstance();
        executor.execute(() -> {
            try {
                Channel channel = channelPool.getChannel();
                int prefetchCount = 1;
                channel.basicQos(prefetchCount);
                this.getMessageFromQueue(channel, queueName);
                Thread.sleep(300);
                channelPool.returnChannel(channel);
            } catch (Exception e) {
                logger.error(" Receiver message from queue {} failed with root cause ", queueName, e);
            }
        });

    }

    private void getMessageFromQueue(Channel channel, String queueName) throws IOException {
        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
//                String message = new String(body, charSet);
                try {
                    Student student = ObjectConverter.bytesToObject(body,Student.class);
                    channel.basicAck(envelope.getDeliveryTag(), false);
                    logger.info(" Message Received from Queue: {} with Student id=: {}",queueName,student.getId());
                } catch (Exception e) {
                    logger.error(" Receiver message  from queue {} failed with root cause " , queueName, e);
                    channel.basicReject(envelope.getDeliveryTag(), true);
                }

            }
        };
        channel.basicConsume(queueName, false, consumer);

    }
}
