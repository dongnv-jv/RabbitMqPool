package vn.vnpay.demo.service.impl;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConfirmListener;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.vnpay.demo.annotation.CustomValue;
import vn.vnpay.demo.common.ObjectConverter;
import vn.vnpay.demo.common.PropertiesFactory;
import vn.vnpay.demo.config.channel.ChannelPool;
import vn.vnpay.demo.config.threadpool.ThreadPoolConfig;
import vn.vnpay.demo.domain.Student;
import vn.vnpay.demo.factory.BaseExchange;
import vn.vnpay.demo.service.ExchangeMessageService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

public class ExchangeMessageServiceImpl implements ExchangeMessageService {
    private final Logger logger = LoggerFactory.getLogger(ExchangeMessageServiceImpl.class);
    static String charSet = PropertiesFactory.getFromProperties("charset.Name");
    AtomicInteger count = new AtomicInteger(0);

    @CustomValue("exchange.direct.queueName")
    private String queueName;

    @CustomValue("exchange.dead.letter.name")
    private String deadLetterExchange;
    @CustomValue("exchange.dead.letter.routingKey")
    private String deadLetterRoutingKey;
    @CustomValue("exchange.dead.letter.queueName")
    private String deadLetterQueueName;

    private void handleSendFaileMessage(Channel channel) throws IOException {
        channel.addReturnListener((replyCode, replyText, exchange, routingKey, properties, body) -> {
            logger.error("Message send failed ! " );
            channel.basicPublish(deadLetterExchange, deadLetterRoutingKey,false ,null, body);
        });



        channel.confirmSelect();
        channel.addConfirmListener(new ConfirmListener() {
            @Override
            public void handleAck(long deliveryTag, boolean multiple) {
                logger.info("Message sent to Rabbit server successfully with deliveryTag: {}" , deliveryTag);
            }
            @Override
            public void handleNack(long deliveryTag, boolean multiple) {
                logger.info("Failed to send message to Rabbit server with deliveryTag: {} " ,deliveryTag);
            }
        });

    }


    public void sendMessage(Object message, BaseExchange exchange, String routingKey, String exchangeName, Map<String, Object> mapPropsForHeaders) {
        long start = System.currentTimeMillis();
        logger.info("Start sendToExchange in ExchangeMessageServiceImpl ");
        Executor executor = ThreadPoolConfig.getExecutor();
        exchange.createExchangeAndQueue();
        ChannelPool channelPool = ChannelPool.getInstance();
        for (int i = 0; i < 100; i++) {
            this.sendToExchange(message, executor, channelPool, routingKey, exchangeName, mapPropsForHeaders);
        }
        long end = System.currentTimeMillis();
        logger.info("Process sendToExchange in ExchangeMessageServiceImpl take {} millisecond", (end - start));
    }

    private void sendToExchange(Object message, Executor executor, ChannelPool channelPool, String routingKey, String exchangeName, Map<String, Object> mapPropsForHeaders) {

        executor.execute(() -> {
            Channel channel = null;
            try {
                channel = channelPool.getChannel();
                handleSendFaileMessage(channel);
                AMQP.BasicProperties props = new AMQP.BasicProperties();
                props = props.builder().headers(mapPropsForHeaders).build();
                channel.basicPublish(exchangeName, routingKey,true, props, ObjectConverter.objectToBytes(message));
                channel.waitForConfirmsOrDie(1000);

            } catch (Exception e) {
                logger.error(" Send message to exchange failed with root cause ", e);
            } finally {
                if (channel != null) {
                    channelPool.returnChannel(channel);
                }
            }
        });
    }

    public <T> void getMessageFromQueue(String queueName, Class<T> clazz) {
        long start = System.currentTimeMillis();
        logger.info("Start getMessageFromQueue in ExchangeMessageServiceImpl ");
        Executor executor = ThreadPoolConfig.getExecutor();
        ChannelPool channelPool = ChannelPool.getInstance();
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> executor.execute(() -> {
                try {
                    getMessageFromQueue(queueName, clazz, channelPool);
                } catch (Exception e) {
                    logger.error(" Receiver message from queue {} failed with root cause ", queueName, e);
                }
            }));
            futures.add(future);
        }
        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        allOf.join();

        long end = System.currentTimeMillis();
        logger.info("Process getMessageFromQueue in ExchangeMessageServiceImpl take {} millisecond", (end - start));

    }

    private <T> void getMessageFromQueue(String queueName, Class<T> clazz, ChannelPool channelPool) {
        try {
            Channel channel = channelPool.getChannel();
            int prefetchCount = 1;
            channel.basicQos(prefetchCount);
            this.getMessageFromQueue(channel, queueName, clazz);
            channelPool.returnChannel(channel);
        } catch (Exception e) {
            logger.error(" Receiver message from queue {} failed with root cause ", queueName, e);
        }
    }

    private <T> void getMessageFromQueue(Channel channel, String queueName, Class<T> clazz) throws IOException {
        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {

                try {
                    if (clazz.isAssignableFrom(String.class)) {
                        String message = new String(body, charSet);
                        channel.basicAck(envelope.getDeliveryTag(), false);
                        logger.info(" Message Received from Queue: {} with message : {}", queueName, message);
                    } else {
                        Student student = (Student) ObjectConverter.bytesToObject(body, clazz);
                        channel.basicAck(envelope.getDeliveryTag(), false);
                        logger.info(" Message Received from Queue: {} with Student id=: {}", queueName, student.getId());
                    }
                } catch (Exception e) {
                    logger.error(" Receiver message  from queue {} failed with root cause ", queueName, e);
                    channel.basicReject(envelope.getDeliveryTag(), true);
                }

            }

            @Override
            public void handleConsumeOk(String consumerTag) {
                logger.info("Consumer {} registered successfully", consumerTag);
            }

            @Override
            public void handleCancelOk(String consumerTag) {
                logger.info("Consumer {} has been cancelled successfully", consumerTag);
            }
        };

        channel.basicConsume(queueName, false, consumer);

    }
}
