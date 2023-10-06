package vn.vnpay.rabbitmq.factory.impl;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.vnpay.rabbitmq.annotation.CustomValue;
import vn.vnpay.rabbitmq.annotation.ValueKeyMap;
import vn.vnpay.rabbitmq.common.CommonConstant;
import vn.vnpay.rabbitmq.config.channel.ChannelPool;
import vn.vnpay.rabbitmq.factory.BaseExchange;

import java.util.HashMap;
import java.util.Map;

public class HeaderExchange implements BaseExchange {
    Logger logger = LoggerFactory.getLogger(HeaderExchange.class);
    @ValueKeyMap("exchange.header.arguments.")
    Map<String, Object> arguments;
    @CustomValue("exchange.header.name")
    private String exchangeHeader;
    @CustomValue("exchange.header.queueName")
    private String queueName;
    @CustomValue("exchange.header.x-match")
    private String xMatch;
    @CustomValue("exchange.dead.letter.name")
    private String deadLetterExchange;
    @CustomValue("exchange.dead.letter.routingKey")
    private String deadLetterRoutingKey;

    public void createExchangeAndQueue() {
        Long start = System.currentTimeMillis();
        logger.info("Start createExchangeAndQueue in HeaderExchange");
        ChannelPool channelPool = ChannelPool.getInstance();
        Channel channel = null;
        try {
            channel = channelPool.getChannel();
            channel.exchangeDeclare(exchangeHeader, BuiltinExchangeType.HEADERS, true);
            arguments.put(CommonConstant.X_MATCH, xMatch);
            arguments.forEach((key, value) -> logger.info("CreateExchangeAndQueue in HeaderExchange with arguments {} : {}", key, value));
            channel.queueDeclare(queueName, true, false, false, this.argumentsDeadLetterQueue());
            channel.queueBind(queueName, exchangeHeader, "", arguments);
            Long end = System.currentTimeMillis();
            logger.info(" Process createExchangeAndQueue in HeaderExchange take {} milliSecond ", (end - start));
        } catch (Exception e) {
            logger.error("CreateExchangeAndQueue in HeaderExchange failed with root cause ", e);
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
