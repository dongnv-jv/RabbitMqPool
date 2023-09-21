package vn.vnpay.demo.service.impl;


import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.vnpay.demo.commom.CommonConstant;
import vn.vnpay.demo.commom.PropertiesFactory;
import vn.vnpay.demo.config.channelpoolconfig.ChannelPool;
import vn.vnpay.demo.service.SendMessageService;
import vn.vnpay.demo.factory.FanoutExchange;
import vn.vnpay.demo.factory.TopicExchange;
import vn.vnpay.demo.factory.DirectExchange;
import vn.vnpay.demo.factory.BaseExchange;
import vn.vnpay.demo.factory.HeaderExchange;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SendMessageServiceImpl implements SendMessageService {
    private final Logger logger = LoggerFactory.getLogger(SendMessageServiceImpl.class);
    static String charSet = PropertiesFactory.getFromProperties("charset.Name");

    public void sendMessage(String message, BaseExchange exchange) {
        long start = System.currentTimeMillis();
        logger.info("Start sendToExchange in SendMessageServiceImpl ");

        exchange.createExchangeAndQueue();
        for (int i = 0; i < 100; i++) {
            this.sendToExchange( message, exchange);
        }
        long end = System.currentTimeMillis();
        logger.info("Process sendToExchange in TestPoolService take {} millisecond", (end - start));
    }

    private void sendToExchange( String message, BaseExchange exchange) {
        new Thread(() -> {
            try {
                ChannelPool channelPool = ChannelPool.getInstance();
                Channel channel = channelPool.getChannel();
                if (exchange instanceof HeaderExchange) {
                    logger.info("Process sendToExchange in TestPoolService with header Exchange");
                    AMQP.BasicProperties props = new AMQP.BasicProperties();
                    Map<String, Object> map = new HashMap<>();
                    map.put("Fourth", "D");
                    map.put("Second", "B");
                    props = props.builder().headers(map).build();

                    channel.basicPublish(CommonConstant.EXCHANGE_HEADER, CommonConstant.ROUTING_KEY, props, message.getBytes());

                }
                if (exchange instanceof DirectExchange) {
                    logger.info("Process sendToExchange in TestPoolService with DirectExchange");
                    channel.basicPublish(CommonConstant.EXCHANGE_DIRECT, CommonConstant.ROUTING_KEY_DIRECT_1, null, message.getBytes());
                    logger.info(" Message Sent from direct exchange : {}", message);

                }
                if (exchange instanceof FanoutExchange) {
                    logger.info("Process sendToExchange in TestPoolService with FanoutExchange");
                    channel.basicPublish(CommonConstant.EXCHANGE_FANOUT, CommonConstant.ROUTING_KEY, null, message.getBytes());
                    logger.info(" Message Sent from Fanout exchange : {}", message);

                }
                if (exchange instanceof TopicExchange) {
                    logger.info("Process sendToExchange in TestPoolService with TopicExchange");
                    channel.basicPublish(CommonConstant.EXCHANGE_TOPIC, CommonConstant.ROUTING_KEY_2, null, message.getBytes());
                    logger.info(" Message Sent from Topic exchange : {}", message);
                }
                channelPool.returnChannel(channel);
            } catch (Exception e) {
                logger.error(" Send message to exchange failed with root cause {}", e.getMessage());
            }
        }).start();
    }

    public void getMessageFromQueue(String queueName) {
        long start = System.currentTimeMillis();
        logger.info("Start getMessage in TestPoolService .");
        try {
            ChannelPool channelPool = ChannelPool.getInstance();
            Channel channel = channelPool.getChannel();
            Consumer consumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    String message = new String(body, charSet);
                    try {

                        channel.basicAck(envelope.getDeliveryTag(), false);
                        logger.info(" Message Received Queue topic 1: {}", message);
                    } catch (Exception e) {

                        logger.error(" Receiver message {} from queue {} failed with root cause {}", message, queueName, e.getMessage());

                        channel.basicReject(envelope.getDeliveryTag(), true);
                    }

                }
            };

            channel.basicConsume(queueName, false, consumer);
            channelPool.returnChannel(channel);
            long end = System.currentTimeMillis();
            logger.info("Process getMessage in TestPoolService take {} millisecond", (end - start));
        } catch (Exception e) {
            logger.error(" Receiver message from queue {} failed with root cause {}", queueName, e.getMessage());
        }
    }

}
