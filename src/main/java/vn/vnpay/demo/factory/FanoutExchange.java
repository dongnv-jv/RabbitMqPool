package vn.vnpay.demo.factory;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.vnpay.demo.annotation.CustomValue;
import vn.vnpay.demo.annotation.ValueKeyMap;
import vn.vnpay.demo.config.channel.ChannelPool;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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


    private  Map<String, Object> argumentsDeadLetterQueue() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-message-ttl", 20000);
        arguments.put("x-dead-letter-exchange", deadLetterExchange);
        arguments.put("x-dead-letter-routing-key", deadLetterRoutingKey);
        return arguments;
    }

    @Override
    public void createExchangeAndQueue() {
        Long start = System.currentTimeMillis();
        logger.info("Start createExchangeAndQueue in FanoutExchange");
        ChannelPool channelPool = ChannelPool.getInstance();
        Channel channel = null;
        try {
            channel = channelPool.getChannel();
            final Channel finalChannel = channel;
            channel.exchangeDeclare(exchangeFanout, BuiltinExchangeType.FANOUT, true );
            listQueueFanout.forEach((key, value) -> {
                try {
                    finalChannel.queueDeclare(value, true, false, false, argumentsDeadLetterQueue());
                    finalChannel.queueBind(value, exchangeFanout, "");
                    logger.info("Successfully created and bound queue {} to exchange {}", value, exchangeFanout);
                } catch (IOException e) {
                    logger.error("Failed to create or bind queue {} to exchange {}", value, exchangeFanout, e);
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
