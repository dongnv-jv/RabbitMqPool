package vn.vnpay.demo.factory;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import vn.vnpay.demo.config.channel.ChannelPool;
import vn.vnpay.demo.common.CommonConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FanoutExchange extends BaseExchange {

    Logger logger = LoggerFactory.getLogger(FanoutExchange.class);

    @Override
    public void createExchangeAndQueue() {
        Long start = System.currentTimeMillis();
        logger.info("Start createExchangeAndQueue in FanoutExchange");
        try {
            ChannelPool channelPool = ChannelPool.getInstance();

            Channel channel = channelPool.getChannel();
            channel.exchangeDeclare(CommonConstant.EXCHANGE_FANOUT, BuiltinExchangeType.FANOUT, true);

            channel.queueDeclare(CommonConstant.QUEUE_NAME_FANOUT_1, true, false, false, null);
            channel.queueBind(CommonConstant.QUEUE_NAME_FANOUT_1, CommonConstant.EXCHANGE_FANOUT, CommonConstant.ROUTING_KEY);

            channel.queueDeclare(CommonConstant.QUEUE_NAME_FANOUT_2, true, false, false, null);
            channel.queueBind(CommonConstant.QUEUE_NAME_FANOUT_2, CommonConstant.EXCHANGE_FANOUT, CommonConstant.ROUTING_KEY);

            channel.queueDeclare(CommonConstant.QUEUE_NAME_FANOUT_3, true, false, false, null);
            channel.queueBind(CommonConstant.QUEUE_NAME_FANOUT_3, CommonConstant.EXCHANGE_FANOUT, CommonConstant.ROUTING_KEY);

            channelPool.returnChannel(channel);

            Long end = System.currentTimeMillis();
            logger.info(" Process createExchangeAndQueue in FanoutExchange take {} milliSecond ", (end - start));

        } catch (Exception e) {
            logger.error("CreateExchangeAndQueue in FanoutExchange failed with root cause ",e);
        }
    }


}
