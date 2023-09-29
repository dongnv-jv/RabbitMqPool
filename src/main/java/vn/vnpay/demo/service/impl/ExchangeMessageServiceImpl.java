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
import vn.vnpay.demo.config.channel.ChannelPool;
import vn.vnpay.demo.config.threadpool.ThreadPoolConfig;
import vn.vnpay.demo.domain.Student;
import vn.vnpay.demo.factory.BaseExchange;
import vn.vnpay.demo.service.ExchangeMessageService;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Executor;

public class ExchangeMessageServiceImpl implements ExchangeMessageService {
    private final Logger logger = LoggerFactory.getLogger(ExchangeMessageServiceImpl.class);
    @CustomValue("charset.Name")
    private String charSet;
    @CustomValue("exchange.direct.queueName")
    private String queueName;
    @CustomValue("exchange.dead.letter.name")
    private String deadLetterExchange;
    @CustomValue("exchange.dead.letter.routingKey")
    private String deadLetterRoutingKey;
    @CustomValue("exchange.dead.letter.queueName")
    private String deadLetterQueueName;
    @CustomValue("consumer.prefetchCount")
    private int prefetchCount;
    private volatile boolean hasFailedMessage = false;

    private synchronized void handleSendFailedMessage(Channel channel) {

        if (!hasFailedMessage) {
            channel.addReturnListener((replyCode, replyText, exchange, routingKey, properties, body) -> {
                channel.basicPublish(deadLetterExchange, deadLetterRoutingKey, false, null, body);
                logger.error("Message send failed because wrong routing key : {} with exchange : {}", routingKey, exchange);
                hasFailedMessage = true;
            });
// Xác nhận rabbitServer có nhân message thành công hay không
            channel.addConfirmListener(new ConfirmListener() {
                @Override
                public void handleAck(long deliveryTag, boolean multiple) {
                    // do nothing
                }

                @Override
                public void handleNack(long deliveryTag, boolean multiple) {
                    logger.info("Failed to send message to Rabbit server with deliveryTag: {} ", deliveryTag);
                }
            });
        }

    }

    public void sendMessage(Object message, BaseExchange exchange, String routingKey, String exchangeName, Map<String, Object> mapPropsForHeaders) {
        long start = System.currentTimeMillis();
        logger.info("Start sendToExchange in ExchangeMessageServiceImpl ");
        Executor executor = ThreadPoolConfig.getExecutor();
        exchange.createExchangeAndQueue();
        ChannelPool channelPool = ChannelPool.getInstance();
        for (int i = 0; i < 100; i++) {
            executor.execute(() -> this.sendToExchange(message, channelPool, routingKey, exchangeName, mapPropsForHeaders));
        }
        long end = System.currentTimeMillis();
        logger.info("Process sendToExchange in ExchangeMessageServiceImpl take {} millisecond", (end - start));
    }

    private void sendToExchange(Object message, ChannelPool channelPool, String routingKey, String exchangeName, Map<String, Object> mapPropsForHeaders) {

        Channel channel = channelPool.getChannel();
        try {

            channel.confirmSelect();
            this.handleSendFailedMessage(channel);
            AMQP.BasicProperties props = new AMQP.BasicProperties();
            props = props.builder()
                    .headers(mapPropsForHeaders).build();
            channel.basicPublish(exchangeName, routingKey, true, props, ObjectConverter.objectToBytes(message));

        } catch (Exception e) {
            logger.error(" Send message to exchange failed with root cause ", e);
        } finally {
            channelPool.returnChannel(channel);

        }
    }

    public <T> void getMessageFromQueue(String queueName, Class<T> clazz) {
        long start = System.currentTimeMillis();
        logger.info("Start getMessageFromQueue in ExchangeMessageServiceImpl ");
        Executor executor = ThreadPoolConfig.getExecutor();
        ChannelPool channelPool = ChannelPool.getInstance();
        for (int i = 0; i < 10; i++) {
            executor.execute(() -> {
                try {
                    this.getMessageFromQueue(queueName, clazz, channelPool);
                } catch (Exception e) {
                    logger.error(" Receiver message from queue {} failed with root cause ", queueName, e);
                }
            });
            long end = System.currentTimeMillis();
            logger.info("Process getMessageFromQueue in ExchangeMessageServiceImpl take {} millisecond", (end - start));

        }
    }

    private <T> void getMessageFromQueue(String queueName, Class<T> clazz, ChannelPool channelPool) {
        Channel channel = null;
        try {
            channel = channelPool.getChannel();
            channel.basicQos(prefetchCount); // Giới hạn số tin nhắn gửi đến consumer chưa được xác nhận
            this.getMessageFromQueue(channel, queueName, clazz);
        } catch (Exception e) {
            logger.error(" Receiver message from queue {} failed with root cause ", queueName, e);
        } finally {
            if (channel != null) {
                channelPool.returnChannel(channel);
            }
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
                        logger.info(" Message Received from Queue: {} with Student id=: {} ", queueName, student.getId());
                    }
                } catch (Exception e) {
                    logger.error(" Receiver message  from queue {} failed with root cause ", queueName, e);
                    channel.basicReject(envelope.getDeliveryTag(), false);
                }
            }

            @Override
            public void handleConsumeOk(String consumerTag) {
                // do nothing
            }

            @Override
            public void handleCancelOk(String consumerTag) {
                logger.info("Consumer {} has been cancelled successfully", consumerTag);
            }
        };
        channel.basicConsume(queueName, false, consumer);

    }
}
