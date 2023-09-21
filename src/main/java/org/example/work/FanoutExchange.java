package org.example.work;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import org.example.channel.ChannelPool;
import org.example.commom.CommonConstant;
import org.example.connection.RabbitMqConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FanoutExchange extends BaseExchange {

    Logger logger = LoggerFactory.getLogger(FanoutExchange.class);

    @Override
    public void createExchangeAndQueue() {
        Long start = System.currentTimeMillis();
        try {
            ChannelPool channelPool = ChannelPool.getInstance();
            logger.info(" Quantity connection waiter in Channel pool: {}", channelPool.getInternalPool().getNumWaiters());
            logger.info(" Quantity connection Active in Channel pool: {}", channelPool.getInternalPool().getNumActive());


            Channel channel = channelPool.getChannel();
            channel.exchangeDeclare(CommonConstant.EXCHANGE_FANOUT, BuiltinExchangeType.FANOUT, true);
            // First Queue
            channel.queueDeclare(CommonConstant.QUEUE_NAME_FANOUT_1, true, false, false, null);
            channel.queueBind(CommonConstant.QUEUE_NAME_FANOUT_1, CommonConstant.EXCHANGE_FANOUT, CommonConstant.ROUTING_KEY);

            // Second Queue
            channel.queueDeclare(CommonConstant.QUEUE_NAME_FANOUT_2, true, false, false, null);
            channel.queueBind(CommonConstant.QUEUE_NAME_FANOUT_2, CommonConstant.EXCHANGE_FANOUT, CommonConstant.ROUTING_KEY);

            // Third Queue
            channel.queueDeclare(CommonConstant.QUEUE_NAME_FANOUT_3, true, false, false, null);
            channel.queueBind(CommonConstant.QUEUE_NAME_FANOUT_3, CommonConstant.EXCHANGE_FANOUT, CommonConstant.ROUTING_KEY);

            channelPool.returnChannel(channel);

            Long end = System.currentTimeMillis();
            logger.info(" Process createExchangeAndQueue in FanoutExchange take {} miliSecond ", (end - start));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
