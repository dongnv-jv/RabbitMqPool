package org.example.work;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import org.example.channel.ChannelPool;
import org.example.commom.CommonConstant;
import org.example.commom.ExchangeType;
import org.example.connection.RabbitMqConnectionPool;

public class TopicExchange {



    public void createExchangeAndQueue(RabbitMqConnectionPool rabbitMqConnectionPool, ChannelPool channelPool) {
        try {
            Connection conn = rabbitMqConnectionPool.getConnection();
            if (conn != null) {
                Channel channel = channelPool.getChannel();
                channel.exchangeDeclare(CommonConstant.EXCHANGE_TOPIC, ExchangeType.TOPIC.getExchangeName(), true);
                // First Queue
                channel.queueDeclare(CommonConstant.QUEUE_NAME_TOPIC_1, true, false, false, null);
                channel.queueBind(CommonConstant.QUEUE_NAME_TOPIC_1, CommonConstant.EXCHANGE_TOPIC, CommonConstant.ROUTING_PATTERN_1);

                // Second Queue
                channel.queueDeclare(CommonConstant.QUEUE_NAME_TOPIC_2, true, false, false, null);
                channel.queueBind(CommonConstant.QUEUE_NAME_TOPIC_2, CommonConstant.EXCHANGE_TOPIC, CommonConstant.ROUTING_PATTERN_2);

                // Third Queue
                channel.queueDeclare(CommonConstant.QUEUE_NAME_TOPIC_3, true, false, false, null);
                channel.queueBind(CommonConstant.QUEUE_NAME_TOPIC_3, CommonConstant.EXCHANGE_TOPIC, CommonConstant.ROUTING_PATTERN_3);

                channelPool.returnChannel(channel);
                rabbitMqConnectionPool.returnConnection(conn);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
