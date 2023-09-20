package org.example.work;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import org.example.channel.ChannelPool;
import org.example.commom.CommonConstant;
import org.example.connection.RabbitMqConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FanoutExchange {

    Logger logger = LoggerFactory.getLogger(FanoutExchange.class);

    public void createExchangeAndQueue(RabbitMqConnectionPool rabbitMqConnectionPool, ChannelPool channelPool) {
        try {
            Connection conn = rabbitMqConnectionPool.getConnection();
            logger.info(" Quantity connection waiter in Connection pool: {}",rabbitMqConnectionPool.getInternalPool().getNumWaiters());
            logger.info(" Quantity connection Active in Connection pool: {}",rabbitMqConnectionPool.getInternalPool().getNumActive());
            logger.info(" Quantity connection waiter in Channel pool: {}",channelPool.getInternalPool().getNumWaiters());
            logger.info(" Quantity connection Active in Channel pool: {}",channelPool.getInternalPool().getNumActive());

            if (conn != null) {
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
                rabbitMqConnectionPool.returnConnection(conn);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
