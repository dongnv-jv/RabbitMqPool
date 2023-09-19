package org.example;

import com.rabbitmq.client.*;
import org.example.channel.ChannelFactory;
import org.example.channel.ChannelPool;
import org.example.commom.CommonConstant;
import org.example.commom.ExchangeType;
import org.example.commom.PropertiesCommon;
import org.example.connection.RabbitMqConnectionFactory;
import org.example.connection.RabbitMqConnectionPool;
import org.example.work.DirectExchange;
import org.example.work.TopicExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;

public class TestChannelPool {

    static Logger logger = LoggerFactory.getLogger(TestChannelPool.class);
    static String charSet = PropertiesCommon.getFromProperties("charset.Name");

    public static void main(String[] args) throws URISyntaxException, NoSuchAlgorithmException, KeyManagementException, IOException, TimeoutException {


        RabbitMqConnectionFactory rabbitMqConnectionFactory = new RabbitMqConnectionFactory();
        final String message = "Hello Example";

        try {
            RabbitMqConnectionPool rabbitMqConnectionPool = new RabbitMqConnectionPool(
                    10, 3, 5, false, rabbitMqConnectionFactory);

            Connection connection = rabbitMqConnectionPool.getConnection();
            ChannelFactory channelFactory = new ChannelFactory(rabbitMqConnectionPool.getConnection());
            ChannelPool channelPool = new ChannelPool(channelFactory);

            for (int i = 0; i < 5; i++) {
                new Thread(() -> {
                    try {
                        while (true) {
                            Channel channel = channelPool.getChannel();
// Create Exchange and Queue direct

                            DirectExchange directExchange = new DirectExchange();
                            directExchange.createExchangeAndQueue(rabbitMqConnectionPool, channelPool);
                            channel.basicPublish(CommonConstant.EXCHANGE_DIRECT, CommonConstant.ROUTING_KEY_DIRECT_1, null, message.getBytes());
                            logger.info(" Message Sent from direct exchange : {}", message);

// Create Exchange and Queue
                            channel.exchangeDeclare(CommonConstant.EXCHANGE_FANOUT, ExchangeType.FANOUT.getExchangeName(), true);
                            channel.queueDeclare(CommonConstant.QUEUE_NAME_FANOUT_1, true, false, false, null);
                            channel.queueBind(CommonConstant.QUEUE_NAME_FANOUT_1, CommonConstant.EXCHANGE_FANOUT, CommonConstant.ROUTING_KEY);
// Publish message EXCHANGE_FANOUT
                            channel.basicPublish(CommonConstant.EXCHANGE_FANOUT, CommonConstant.ROUTING_KEY, null, message.getBytes());
                            // Create Exchange and Queue topic
                            TopicExchange topicExchange = new TopicExchange();
                            topicExchange.createExchangeAndQueue(rabbitMqConnectionPool, channelPool);
// Publish message EXCHANGE_TOPIC

                            channel.basicPublish(CommonConstant.EXCHANGE_TOPIC, CommonConstant.ROUTING_PATTERN_1, null, message.getBytes());
                            logger.info(" Message Sent from EXCHANGE_TOPIC {} 1", message);

                            channel.basicPublish(CommonConstant.EXCHANGE_TOPIC, CommonConstant.ROUTING_PATTERN_2, null, message.getBytes());
                            logger.info(" Message Sent EXCHANGE_TOPIC {} 2", message);

                            channel.basicPublish(CommonConstant.EXCHANGE_TOPIC, CommonConstant.ROUTING_PATTERN_3, null, message.getBytes());
                            logger.info(" Message Sent EXCHANGE_TOPIC {} 3", message);

// Return connection and Channel to pool
                            rabbitMqConnectionPool.returnConnection(connection);
                            channelPool.returnChannel(channel);
                        }
                    } catch (Exception ignored) {

                    }
                }).start();
            }

// Consumer reading from queue fanout 1
            Channel channel = channelPool.getChannel();
            Consumer consumerCustom1 = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException, UnsupportedEncodingException {
                    String message = new String(body, charSet);
                    logger.info(" Message Received Queue fanout 1: {}", message);
                }
            };

            channel.basicConsume(CommonConstant.QUEUE_NAME_FANOUT_1, true, consumerCustom1);

// Consumer reading from queue direct 1
            Consumer consumer2 = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException, UnsupportedEncodingException {
                    String message = new String(body, charSet);
                    logger.info(" Message Received Queue direct 1: {}", message);
                }
            };
            channel.basicConsume(CommonConstant.QUEUE_NAME_TOPIC_1, true, consumer2);


        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}