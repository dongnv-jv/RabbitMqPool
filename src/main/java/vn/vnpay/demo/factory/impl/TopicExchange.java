package vn.vnpay.demo.factory.impl;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.vnpay.demo.annotation.CustomValue;
import vn.vnpay.demo.common.CommonConstant;
import vn.vnpay.demo.config.channel.ChannelPool;
import vn.vnpay.demo.factory.BaseExchange;

import java.util.HashMap;
import java.util.Map;

public class TopicExchange implements BaseExchange {

    Logger logger = LoggerFactory.getLogger(TopicExchange.class);

    @CustomValue("exchange.topic.name")
    private String exchangeTopic;
    @CustomValue("exchange.topic.routing.pattern-1")
    private String routingPattern;
    @CustomValue("exchange.topic.queueName")
    private String queueName;
    @CustomValue("exchange.dead.letter.name")
    private String deadLetterExchange;
    @CustomValue("exchange.dead.letter.routingKey")
    private String deadLetterRoutingKey;

    @Override
    public void createExchangeAndQueue() {
        Long start = System.currentTimeMillis();
        logger.info("Start createExchangeAndQueue in TopicExchange ");
        ChannelPool channelPool = ChannelPool.getInstance();
        Channel channel = null;
        try {
            channel = channelPool.getChannel();
            channel.exchangeDeclare(exchangeTopic, BuiltinExchangeType.TOPIC, true);
            channel.queueDeclare(queueName, true, false, false, argumentsDeadLetterQueue());
            channel.queueBind(queueName, exchangeTopic, routingPattern);
            Long end = System.currentTimeMillis();
            logger.info(" Process createExchangeAndQueue in TopicExchange take {} millisecond ", (end - start));

        } catch (Exception e) {
            logger.error("CreateExchangeAndQueue in TopicExchange failed with root cause ", e);
        } finally {
            if (channel != null) {
                channelPool.returnChannel(channel);
            }

        }
    }

    private Map<String, Object> argumentsDeadLetterQueue() {
        Map<String, Object> argumentsDeadLetter = new HashMap<>();
        argumentsDeadLetter.put(CommonConstant.X_DEAD_LETTER_EXCHANGE, deadLetterExchange);
        argumentsDeadLetter.put(CommonConstant.X_DEAD_LETTER_ROUTING_KEY, deadLetterRoutingKey);
        return argumentsDeadLetter;
    }
}
