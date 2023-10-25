package vn.vnpay.rabbitmq.factory.impl;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.vnpay.rabbitmq.annotation.Autowire;
import vn.vnpay.rabbitmq.annotation.Component;
import vn.vnpay.rabbitmq.annotation.CustomValue;
import vn.vnpay.rabbitmq.annotation.ValueKeyMap;
import vn.vnpay.rabbitmq.common.CommonConstant;
import vn.vnpay.rabbitmq.config.channel.ChannelPool;
import vn.vnpay.rabbitmq.factory.BaseExchange;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class FanoutExchange implements BaseExchange {

    Logger logger = LoggerFactory.getLogger(FanoutExchange.class);
    @ValueKeyMap("exchange.fanout.queue.")
    Map<String, String> listQueueFanout;
    @CustomValue("exchange.fanout.name")
    private String exchangeFanout;
    @CustomValue("exchange.dead.letter.name")
    private String deadLetterExchange;
    @CustomValue("exchange.dead.letter.routingKey")
    private String deadLetterRoutingKey;
    @CustomValue("exchange.fanout.ttl")
    private int messageTTL;
    @Autowire
    private ChannelPool channelPool;

    private Map<String, Object> argumentsDeadLetterQueue() {
        Map<String, Object> argumentsDeadLetter = new HashMap<>();
        argumentsDeadLetter.put(CommonConstant.X_MESSAGE_TTL, messageTTL);
        argumentsDeadLetter.put(CommonConstant.X_DEAD_LETTER_EXCHANGE, deadLetterExchange);
        argumentsDeadLetter.put(CommonConstant.X_DEAD_LETTER_ROUTING_KEY, deadLetterRoutingKey);
        return argumentsDeadLetter;
    }

    @Override
    public void createExchangeAndQueue() {
        Long start = System.currentTimeMillis();
        logger.info("Start createExchangeAndQueue in FanoutExchange");
        Channel channel = null;
        try {
            channel = channelPool.getChannel();
            final Channel finalChannel = channel;
            channel.exchangeDeclare(exchangeFanout, BuiltinExchangeType.FANOUT, true);
            listQueueFanout.forEach((key, queueName) -> {
                try {
                    finalChannel.queueDeclare(queueName, true, false, false, argumentsDeadLetterQueue());
                    finalChannel.queueBind(queueName, exchangeFanout, "");
                    logger.info("Successfully created and bound queue {} to exchange {}", queueName, exchangeFanout);
                } catch (IOException e) {
                    logger.error("Failed to create or bind queue {} to exchange {}", queueName, exchangeFanout, e);
                    throw new RuntimeException(e);
                }
            });
            Long end = System.currentTimeMillis();
            logger.info(" Process createExchangeAndQueue in FanoutExchange take {} milliSecond ", (end - start));

        } catch (Exception e) {
            logger.error("CreateExchangeAndQueue in FanoutExchange failed with root cause ", e);
        } finally {
            if (channel != null) {
                channelPool.returnChannel(channel);
            }
        }
    }
}
