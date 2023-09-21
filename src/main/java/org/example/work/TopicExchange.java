package org.example.work;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import org.example.channel.ChannelPool;
import org.example.commom.CommonConstant;
import org.example.connection.RabbitMqConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TopicExchange extends BaseExchange {

    Logger logger = LoggerFactory.getLogger(TopicExchange.class);

    @Override
    public void createExchangeAndQueue() {
        Long start = System.currentTimeMillis();
        try {
            ChannelPool channelPool = ChannelPool.getInstance();
            Channel channel = channelPool.getChannel();
            channel.exchangeDeclare(CommonConstant.EXCHANGE_TOPIC, BuiltinExchangeType.TOPIC, true);
            // First Queue
            channel.queueDeclare(CommonConstant.QUEUE_NAME_TOPIC_1, true, false, false, null);
            channel.queueBind(CommonConstant.QUEUE_NAME_TOPIC_1, CommonConstant.EXCHANGE_TOPIC, CommonConstant.ROUTING_PATTERN_1);

//                 Second Queue
            channel.queueDeclare(CommonConstant.QUEUE_NAME_TOPIC_2, true, false, false, null);
            channel.queueBind(CommonConstant.QUEUE_NAME_TOPIC_2, CommonConstant.EXCHANGE_TOPIC, CommonConstant.ROUTING_PATTERN_2);

            // Third Queue
            channel.queueDeclare(CommonConstant.QUEUE_NAME_TOPIC_3, true, false, false, null);
            channel.queueBind(CommonConstant.QUEUE_NAME_TOPIC_3, CommonConstant.EXCHANGE_TOPIC, CommonConstant.ROUTING_PATTERN_3);

            channelPool.returnChannel(channel);

            Long end = System.currentTimeMillis();
            logger.info(" Process createExchangeAndQueue in TopicExchange take {} miliSecond ", (end - start));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
