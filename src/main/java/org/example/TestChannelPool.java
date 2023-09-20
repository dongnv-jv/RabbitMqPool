package org.example;

import com.rabbitmq.client.*;
import com.rabbitmq.client.AMQP.BasicProperties;
import org.example.channel.ChannelFactory;
import org.example.channel.ChannelPool;
import org.example.commom.CommonConstant;
import org.example.commom.PropertiesCommon;
import org.example.connection.RabbitMqConnectionFactory;
import org.example.connection.RabbitMqConnectionPool;
import org.example.work.HeaderExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class TestChannelPool {

    static Logger logger = LoggerFactory.getLogger(TestChannelPool.class);
    static String charSet = PropertiesCommon.getFromProperties("charset.Name");

    public static void main(String[] args)  {


        RabbitMqConnectionFactory rabbitMqConnectionFactory = new RabbitMqConnectionFactory();
        final String message = "Hello Example";

        try {
            RabbitMqConnectionPool rabbitMqConnectionPool = new RabbitMqConnectionPool(rabbitMqConnectionFactory);

            Connection connection = rabbitMqConnectionPool.getConnection();
            ChannelFactory channelFactory = new ChannelFactory(connection);
            ChannelPool channelPool = new ChannelPool(channelFactory);

            for (int i = 0; i < 5; i++) {
                new Thread(() -> {
                    try {
                        Channel channel = channelPool.getChannel();
// Create Exchange and Queue direct

//                            DirectExchange directExchange = new DirectExchange();
//                            directExchange.createExchangeAndQueue(rabbitMqConnectionPool, channelPool);
//                            channel.basicPublish(CommonConstant.EXCHANGE_DIRECT, CommonConstant.ROUTING_KEY_DIRECT_1, null, (message+ " Direct exchange").getBytes());
//                            logger.info(" Message Sent from direct exchange : {}", message);

// Create Exchange and Queue
//                        FanoutExchange fanoutExchange = new FanoutExchange();
//                        fanoutExchange.createExchangeAndQueue(rabbitMqConnectionPool, channelPool);
//                        channel.basicPublish(CommonConstant.EXCHANGE_FANOUT, CommonConstant.ROUTING_KEY, null, (message+ " Direct exchange").getBytes());

//// Create Exchange and Queue topic
//                            TopicExchange topicExchange = new TopicExchange();
//                            topicExchange.createExchangeAndQueue(rabbitMqConnectionPool, channelPool);
//// Publish message EXCHANGE_TOPIC
//
//                            channel.basicPublish(CommonConstant.EXCHANGE_TOPIC, CommonConstant.ROUTING_KEY_1, null, message.getBytes());
//                        channel.basicPublish(CommonConstant.EXCHANGE_TOPIC, CommonConstant.ROUTING_KEY_2, null, (message+" 2").getBytes());
//
//                            logger.info(" Message Sent from EXCHANGE_TOPIC {} 1", message);

//                            channel.basicPublish(CommonConstant.EXCHANGE_TOPIC+ "11", CommonConstant.ROUTING_PATTERN_2, null, message.getBytes());
//                            logger.info(" Message Sent EXCHANGE_TOPIC {} 2", message);
//
//                            channel.basicPublish(CommonConstant.EXCHANGE_TOPIC+ "11", CommonConstant.ROUTING_PATTERN_3, null, message.getBytes());
//                            logger.info(" Message Sent EXCHANGE_TOPIC {} 3", message);

// Create header Exchnage
                        HeaderExchange headerExchange = new HeaderExchange();
                        headerExchange.createExchangeAndQueue(rabbitMqConnectionPool, channelPool);
                        BasicProperties props  = new BasicProperties();
                        Map<String ,Object> map = new HashMap<String ,Object>();
//                        map.put("First","A");
                        map.put("Fourth","D");
                        map.put("Second","B");
                        props = props.builder().headers(map).build();
                        channel.basicPublish(CommonConstant.EXCHANGE_HEADER, CommonConstant.ROUTING_KEY, props, (message+" Header Exchange").getBytes());


// Return connection and Channel to pool

                            rabbitMqConnectionPool.returnConnection(connection);
                            channelPool.returnChannel(channel);


                    } catch (Exception ignored) {

                    }
                }).start();
            }

// Consumer reading from queue fanout 1

//            Consumer consumerCustom1 = new DefaultConsumer(channel) {
//                @Override
//                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException, UnsupportedEncodingException {
//                    String message = new String(body, charSet);
//                    logger.info(" Message Received Queue fanout 1: {}", message);
//                }
//            };
//
//            channel.basicConsume(CommonConstant.QUEUE_NAME_FANOUT_1, true, consumerCustom1);
//
// Consumer reading from queue topic 1
            Channel channel = channelPool.getChannel();
            Consumer consumer2 = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException, UnsupportedEncodingException {
                    String message = new String(body, charSet);
                    logger.info(" Message Received Queue topic 1: {}", message);
                    channel.basicAck(envelope.getDeliveryTag(), true);
                }
            };
            channel.basicConsume(CommonConstant.QUEUE_NAME_FANOUT_3, true, consumer2);
// Consumer reading from queue direct 1
//            Consumer consumer3 = new DefaultConsumer(channel) {
//                @Override
//                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException, UnsupportedEncodingException {
//                    String message = new String(body, charSet);
//                    logger.info(" Message Received Queue direct 1: {}", message);
//                }
//            };
//            channel.basicConsume(CommonConstant.QUEUE_NAME_DIRECT_1, true, consumer3);


        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}