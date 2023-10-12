package vn.vnpay.rabbitmq.factory.impl;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.vnpay.rabbitmq.annotation.Autowire;
import vn.vnpay.rabbitmq.annotation.Component;
import vn.vnpay.rabbitmq.annotation.CustomValue;
import vn.vnpay.rabbitmq.config.channel.ChannelPool;

import java.util.HashMap;
import java.util.Map;

@Component
public class DeadLetterExchange {
    Logger logger = LoggerFactory.getLogger(DeadLetterExchange.class);

    @CustomValue("exchange.dead.letter.name")
    private String deadLetterExchange;
    @CustomValue("exchange.dead.letter.routingKey")
    private String deadLetterRoutingKey;
    @CustomValue("exchange.dead.letter.queueName")
    private String deadLetterQueueName;
    @Autowire
    private ChannelPool channelPool;

    public void createExchangeAndQueue() {
        Long start = System.currentTimeMillis();
        logger.info("Start createExchangeAndQueue in DeadLetterExchange");
        Channel channel = null;
        try {
            channel = channelPool.getChannel();
            channel.exchangeDeclare(deadLetterExchange, BuiltinExchangeType.DIRECT);
            Map<String, Object> args = new HashMap<>();
            args.put("x-dead-letter-exchange", deadLetterExchange);
            args.put("x-dead-letter-routing-key", deadLetterRoutingKey);
            channel.queueDeclare(deadLetterQueueName, false, false, false, null);
            channel.queueBind(deadLetterQueueName, deadLetterExchange, deadLetterRoutingKey);
            Long end = System.currentTimeMillis();
            logger.info(" Process createExchangeAndQueue in DeadLetterExchange take {} milliSecond ", (end - start));
        } catch (Exception e) {
            logger.error("CreateExchangeAndQueue in DeadLetterExchange failed with root cause ", e);
        } finally {
            if (channel != null) {
                channelPool.returnChannel(channel);
            }
        }
    }
}
