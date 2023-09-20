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

    public static void main(String[] args) {


        final String message = "Hello Example";

        try {
            ChannelPool channelPool = ChannelPool.getInstance();
//
            for (int i = 0; i < 100; i++) {
                new Thread(() -> {
                    try {

// Create header Exchnage
                        HeaderExchange headerExchange = new HeaderExchange();
                        headerExchange.createExchangeAndQueue();

                        BasicProperties props = new BasicProperties();
                        Map<String, Object> map = new HashMap<String, Object>();
//                        map.put("First","A");
                        map.put("Fourth", "D");
                        map.put("Second", "B");
                        props = props.builder().headers(map).build();
                        Channel channel = channelPool.getChannel();
                        channel.basicPublish(CommonConstant.EXCHANGE_HEADER, CommonConstant.ROUTING_KEY, props, (message + " Header Exchange").getBytes());

// Return  Channel to pool
                        channelPool.returnChannel(channel);

                    } catch (Exception ignored) {

                    }
                }).start();
            }
// Consumer reading from queue topic 1
            Channel channel = channelPool.getChannel();
            Consumer consumer2 = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException, UnsupportedEncodingException {
                    String message = new String(body, charSet);
                    logger.info(" Message Received Queue topic 1: {}", message);
//                    channel.basicAck(envelope.getDeliveryTag(), true);
                }
            };
            channel.basicConsume(CommonConstant.QUEUE_NAME_HEADER, true, consumer2);
            channelPool.returnChannel(channel);


        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}

