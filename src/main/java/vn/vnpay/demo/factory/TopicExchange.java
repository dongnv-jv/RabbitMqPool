package vn.vnpay.demo.factory;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.vnpay.demo.annotation.CustomValue;
import vn.vnpay.demo.config.channel.ChannelPool;

public class TopicExchange implements BaseExchange {

    Logger logger = LoggerFactory.getLogger(TopicExchange.class);

    @CustomValue("exchange.topic.name")
    private String exchangeTopic;
    @CustomValue("exchange.topic.routing.pattern-1")
    private String routingPattern;
    @CustomValue("exchange.topic.queueName")
    private String queueName;

    @Override
    public void createExchangeAndQueue() {
        Long start = System.currentTimeMillis();
        logger.info("Start createExchangeAndQueue in TopicExchange ");
        ChannelPool channelPool = ChannelPool.getInstance();
        Channel channel = null;
        try {
            channel = channelPool.getChannel();
            channel.exchangeDeclare(exchangeTopic, BuiltinExchangeType.TOPIC, true);
            channel.queueDeclare(queueName, true, false, false, null);
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
}
