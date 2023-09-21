package org.example.work;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import org.example.channel.ChannelPool;
import org.example.commom.CommonConstant;
import org.example.connection.RabbitMqConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DirectExchange extends BaseExchange {

    Logger logger = LoggerFactory.getLogger(DirectExchange.class);

    @Override
    public void createExchangeAndQueue() {
        Long start = System.currentTimeMillis();
        try {
            ChannelPool channelPool = ChannelPool.getInstance();

            Channel channel = channelPool.getChannel();
            channel.exchangeDeclare(CommonConstant.EXCHANGE_DIRECT, BuiltinExchangeType.DIRECT, true);
            // First Queue
            channel.queueDeclare(CommonConstant.QUEUE_NAME_DIRECT_1, true, false, false, null);
            channel.queueBind(CommonConstant.QUEUE_NAME_DIRECT_1, CommonConstant.EXCHANGE_DIRECT, CommonConstant.ROUTING_KEY_DIRECT_1);

            // Second Queue

            channel.queueDeclare(CommonConstant.QUEUE_NAME_DIRECT_2, true, false, false, null);
            channel.queueBind(CommonConstant.QUEUE_NAME_DIRECT_2, CommonConstant.EXCHANGE_DIRECT, CommonConstant.ROUTING_KEY_DIRECT_2);


            // Third Queue
            channel.queueDeclare(CommonConstant.QUEUE_NAME_DIRECT_3, true, false, false, null);
            channel.queueBind(CommonConstant.QUEUE_NAME_DIRECT_3, CommonConstant.EXCHANGE_DIRECT, CommonConstant.ROUTING_KEY_DIRECT_3);

            channelPool.returnChannel(channel);
            Long end = System.currentTimeMillis();
            logger.info(" Process createExchangeAndQueue in DirectExchange take {} miliSecond ", (end - start));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
