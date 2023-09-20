package org.example;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import org.example.connection.RabbitMqConnectionFactory;
import org.example.connection.RabbitMqConnectionPool;

public class TestConnectionPool {


    public static void main(String[] args) {

        RabbitMqConnectionFactory rabbitMqConnectionFactory = new RabbitMqConnectionFactory();
        try {
            for (int i = 0; i < 15; i++) {
                new Thread(() -> {
                    try {
                            RabbitMqConnectionPool rabbitMqConnectionPool = new RabbitMqConnectionPool(rabbitMqConnectionFactory);

                            Connection connection = rabbitMqConnectionPool.getConnection();

                            Channel channel = connection.createChannel();

                            rabbitMqConnectionPool.returnConnection(connection);

                    } catch (Exception e) {

                    }
                }).start();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
