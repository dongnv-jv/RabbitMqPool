package org.example;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import org.example.connection.RabbitMqConnectionFactory;
import org.example.connection.RabbitMqConnectionPool;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;

public class TestConnectionPool {


    public static void main(String[] args) throws URISyntaxException, NoSuchAlgorithmException, KeyManagementException, IOException, TimeoutException {


        RabbitMqConnectionFactory rabbitMqConnectionFactory = new RabbitMqConnectionFactory();


        try {


            for (int i = 0; i < 15; i++) {
                new Thread(() -> {
                    try {
                        while (true) {
                            RabbitMqConnectionPool rabbitMqConnectionPool = new RabbitMqConnectionPool(
                                    10, 3, 5, false, rabbitMqConnectionFactory);

                            Connection connection = rabbitMqConnectionPool.getConnection();
//                            ChannelFactory channelFactory = new ChannelFactory(rabbitMqConnectionPool.getConnection());
//                            ChannelPool channelPool = new ChannelPool(channelFactory);
//                            Channel channel = channelPool.getChannel();
//                            Channel channel = connection.createChannel();
//                            final String EXCHANGE_NAME = "dyh";
//                            channel.exchangeDeclare(EXCHANGE_NAME, "direct");
//
//
//                            channel.basicPublish(EXCHANGE_NAME, "1", null, "s".getBytes());

                            rabbitMqConnectionPool.returnConnection(connection);
//                            channelPool.returnChannel(channel);
                        }
                    } catch (Exception e) {

                    }
                }).start();
            }


//            Channel channel = channelPool.getChannel();
//// Consumer reading from queue 1
//            Consumer consumerCustom1 = new DefaultConsumer(channel) {
//
//                @Override
//                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException, UnsupportedEncodingException {
//                    String message = new String(body, "UTF-8");
//                    System.out.println(" Message Received Queue 1 '" + message + "'");
//                }
//            };
//            final String EXCHANGE_NAME = "dyh";
//            channel.basicConsume(EXCHANGE_NAME, true, consumerCustom1);


//            FanoutExchange ex = new FanoutExchange();
//            ex.createExchangeAndQueue(rabbitMqConnectionPool,channelPool);
//
//            // Publish
//            Producer produce = new Producer();
//            produce.publish(rabbitMqConnectionPool,channelPool);
//
//
//            // Consume
//            ConsumerCustom receive = new ConsumerCustom();
//            receive.receive();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
