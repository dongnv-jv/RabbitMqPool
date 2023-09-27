package vn.vnpay.demo.factory;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import vn.vnpay.demo.config.channelpoolconfig.ChannelPool;
import vn.vnpay.demo.common.CommonConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TopicExchange extends BaseExchange {

    Logger logger = LoggerFactory.getLogger(TopicExchange.class);

    @Override
    public void createExchangeAndQueue() {
        Long start = System.currentTimeMillis();
        logger.info("Start createExchangeAndQueue in TopicExchange ");
        try {
            ChannelPool channelPool = ChannelPool.getInstance();
            Channel channel = channelPool.getChannel();
            channel.exchangeDeclare(CommonConstant.EXCHANGE_TOPIC, BuiltinExchangeType.TOPIC, true);

            channel.queueDeclare(CommonConstant.QUEUE_NAME_TOPIC_1, true, false, false, null);
            channel.queueBind(CommonConstant.QUEUE_NAME_TOPIC_1, CommonConstant.EXCHANGE_TOPIC, CommonConstant.ROUTING_PATTERN_1);

            channel.queueDeclare(CommonConstant.QUEUE_NAME_TOPIC_2, true, false, false, null);
            channel.queueBind(CommonConstant.QUEUE_NAME_TOPIC_2, CommonConstant.EXCHANGE_TOPIC, CommonConstant.ROUTING_PATTERN_2);

            channel.queueDeclare(CommonConstant.QUEUE_NAME_TOPIC_3, true, false, false, null);
            channel.queueBind(CommonConstant.QUEUE_NAME_TOPIC_3, CommonConstant.EXCHANGE_TOPIC, CommonConstant.ROUTING_PATTERN_3);

            channelPool.returnChannel(channel);

            Long end = System.currentTimeMillis();
            logger.info(" Process createExchangeAndQueue in TopicExchange take {} millisecond ", (end - start));

        } catch (Exception e) {
            logger.error("CreateExchangeAndQueue in TopicExchange failed with root cause ",e);
        }
    }
}
