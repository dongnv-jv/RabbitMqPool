package org.example.service;

import com.rabbitmq.client.*;
import org.example.channel.ChannelPool;
import org.example.commom.CommonConstant;
import org.example.commom.PropertiesCommon;
import org.example.work.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class TestPoolService {
    static Logger logger = LoggerFactory.getLogger(TestPoolService.class);
    static String charSet = PropertiesCommon.getFromProperties("charset.Name");

    public void sendToExchange(String message, BaseExchange exchange) {
        long start = System.currentTimeMillis();
        logger.info("Start sendToExchange in TestPoolService .");
        try {
            ChannelPool channelPool = ChannelPool.getInstance();
            exchange.createExchangeAndQueue();
            for (int i = 0; i < 100; i++) {
                new Thread(() -> {
                    try {

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
                    } catch (Exception ignored) {

                    }
                }).start();
            }
            long end = System.currentTimeMillis();
            logger.info("Process sendToExchange in TestPoolService take {} milisecond",(end-start));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getMessage(String queueName) {
        long start = System.currentTimeMillis();
        logger.info("Start getMessage in TestPoolService .");
        try {
            ChannelPool channelPool = ChannelPool.getInstance();
            Channel channel = channelPool.getChannel();
            Consumer consumer2 = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException, UnsupportedEncodingException {
                    String message = new String(body, charSet);
                    logger.info(" Message Received Queue topic 1: {}", message);
                }
            };

            channel.basicConsume(queueName, true, consumer2);
            channelPool.returnChannel(channel);
            long end = System.currentTimeMillis();
            logger.info("Process getMessage in TestPoolService take {} milisecond",(end-start));
        } catch (Exception e) {

        }
    }

}
