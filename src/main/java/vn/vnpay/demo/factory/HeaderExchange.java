package vn.vnpay.demo.factory;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.vnpay.demo.annotation.CustomValue;
import vn.vnpay.demo.annotation.ValueKeyMap;
import vn.vnpay.demo.common.CommonConstant;
import vn.vnpay.demo.config.channel.ChannelPool;

public class HeaderExchange implements BaseExchange {
    Logger logger = LoggerFactory.getLogger(HeaderExchange.class);
    @ValueKeyMap("exchange.header.arguments.")
    Map<String, Object> arguments;
    @CustomValue("exchange.header.name")
    private String exchangeHeader;
    @CustomValue("exchange.header.queueName")
    private String queueName;

    public void createExchangeAndQueue() {
        Long start = System.currentTimeMillis();
        logger.info("Start createExchangeAndQueue in HeaderExchange");
        ChannelPool channelPool = ChannelPool.getInstance();
        Channel channel = null;
        try {
            channel = channelPool.getChannel();
            channel.exchangeDeclare(exchangeHeader, BuiltinExchangeType.HEADERS, true);
            arguments.put(CommonConstant.X_MATCH, CommonConstant.X_MATCH_ANY);
            arguments.forEach((key, value) -> logger.info("CreateExchangeAndQueue in HeaderExchange with arguments {} : {}", key, value));
            channel.queueDeclare(queueName, true, false, false, null);
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

}
