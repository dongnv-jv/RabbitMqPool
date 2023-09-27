package vn.vnpay.demo.factory;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import vn.vnpay.demo.annotation.CustomValue;
import vn.vnpay.demo.annotation.ValueKeyMap;
import vn.vnpay.demo.config.channel.ChannelPool;
import vn.vnpay.demo.common.CommonConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FanoutExchange implements BaseExchange {

    Logger logger = LoggerFactory.getLogger(FanoutExchange.class);
    @ValueKeyMap("exchange.fanout.queue.")
    Map<String, String> listQueueFanout;
    @CustomValue("exchange.fanout.name")
    private String exchangeFanout;

    @Override
    public void createExchangeAndQueue() {
        Long start = System.currentTimeMillis();
        logger.info("Start createExchangeAndQueue in FanoutExchange");
        ChannelPool channelPool = ChannelPool.getInstance();
        Channel channel = null;
        try {
            channel = channelPool.getChannel();
            final Channel finalChannel = channel;
            channel.exchangeDeclare(exchangeFanout, BuiltinExchangeType.FANOUT, true);
            listQueueFanout.forEach((key, value) -> {
                try {
                    finalChannel.queueDeclare(value, true, false, false, null);
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
