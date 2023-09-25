package vn.vnpay.demo.factory;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import vn.vnpay.demo.config.channel.ChannelPool;
import vn.vnpay.demo.common.CommonConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DirectExchange extends BaseExchange {

    Logger logger = LoggerFactory.getLogger(DirectExchange.class);

    @Override
    public void createExchangeAndQueue() {
        Long start = System.currentTimeMillis();
        logger.info("Start createExchangeAndQueue in DirectExchange");
        try {
            ChannelPool channelPool = ChannelPool.getInstance();

            Channel channel = channelPool.getChannel();
            channel.exchangeDeclare(CommonConstant.EXCHANGE_DIRECT, BuiltinExchangeType.DIRECT, true);

            channel.queueDeclare(CommonConstant.QUEUE_NAME_DIRECT_1, true, false, false, null);
            channel.queueBind(CommonConstant.QUEUE_NAME_DIRECT_1, CommonConstant.EXCHANGE_DIRECT, CommonConstant.ROUTING_KEY_DIRECT_1);

            channel.queueDeclare(CommonConstant.QUEUE_NAME_DIRECT_2, true, false, false, null);
            channel.queueBind(CommonConstant.QUEUE_NAME_DIRECT_2, CommonConstant.EXCHANGE_DIRECT, CommonConstant.ROUTING_KEY_DIRECT_2);

            channel.queueDeclare(CommonConstant.QUEUE_NAME_DIRECT_3, true, false, false, null);
            channel.queueBind(CommonConstant.QUEUE_NAME_DIRECT_3, CommonConstant.EXCHANGE_DIRECT, CommonConstant.ROUTING_KEY_DIRECT_3);

            channelPool.returnChannel(channel);
            Long end = System.currentTimeMillis();
            logger.info(" Process createExchangeAndQueue in DirectExchange take {} milliSecond ", (end - start));

        } catch (Exception e) {
            logger.error("CreateExchangeAndQueue in DirectExchange failed with root cause ",e);
        }
    }


}
