package vn.vnpay.demo.factory;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import vn.vnpay.demo.config.channel.ChannelPool;
import vn.vnpay.demo.common.CommonConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.Map;

public class HeaderExchange extends BaseExchange {
    Logger logger = LoggerFactory.getLogger(HeaderExchange.class);

    public void createExchangeAndQueue() {
        Long start = System.currentTimeMillis();
        logger.info("Start createExchangeAndQueue in HeaderExchange");
        try {
            ChannelPool channelPool = ChannelPool.getInstance();
            Map<String, Object> arguments = new HashMap<>();
            Channel channel = channelPool.getChannel();
            channel.exchangeDeclare(CommonConstant.EXCHANGE_HEADER, BuiltinExchangeType.HEADERS, true);
            arguments.put(CommonConstant.X_MATCH, CommonConstant.X_MATCH_ANY);
            arguments.put("First", "A");
            arguments.put("Second", "B");
            arguments.forEach((key, value) -> logger.info("CreateExchangeAndQueue in HeaderExchange with arguments {} : {}", key, value));
            channel.queueDeclare(CommonConstant.QUEUE_NAME_HEADER, true, false, false, null);
            channel.queueBind(CommonConstant.QUEUE_NAME_HEADER, CommonConstant.EXCHANGE_HEADER, CommonConstant.ROUTING_KEY, arguments);
            channelPool.returnChannel(channel);
            Long end = System.currentTimeMillis();
            logger.info(" Process createExchangeAndQueue in HeaderExchange take {} milliSecond ", (end - start));
        } catch (Exception e) {
            logger.error("CreateExchangeAndQueue in HeaderExchange failed with root cause ", e);
        }
    }

}
