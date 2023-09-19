package org.example.work;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import org.example.channel.ChannelPool;
import org.example.connection.RabbitMqConnectionPool;

public class Producer {

    private final static String MESSAGE = "Hello  Example";

    public void publish(RabbitMqConnectionPool rabbitMqConnectionPool, ChannelPool channelPool, String exchangeName,String routingKey){
        try{
            Connection conn = rabbitMqConnectionPool.getConnection();
            if(conn != null){
                Channel channel = channelPool.getChannel();
                channel.basicPublish(exchangeName, routingKey, null, MESSAGE.getBytes());
                System.out.println(" Message Sent '" + MESSAGE + "'");
                channelPool.returnChannel(channel);
                rabbitMqConnectionPool.returnConnection(conn);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
