package vn.vnpay.demo.controller;

import vn.vnpay.demo.common.CommonConstant;
import vn.vnpay.demo.config.channel.ChannelPool;
import vn.vnpay.demo.factory.DirectExchange;
import vn.vnpay.demo.factory.FanoutExchange;
import vn.vnpay.demo.factory.HeaderExchange;
import vn.vnpay.demo.factory.TopicExchange;
import vn.vnpay.demo.service.ExchangeMessageService;
import vn.vnpay.demo.service.impl.ExchangeMessageServiceImpl;

public class TestChannelPool {


    public static void main(String[] args) {
         ChannelPool.getInstance();
        ExchangeMessageService exchangeMessageService = new ExchangeMessageServiceImpl();

        HeaderExchange exchange = new HeaderExchange();
        FanoutExchange fanoutExchange = new FanoutExchange();
        TopicExchange topicExchange = new TopicExchange();
        DirectExchange directExchange = new DirectExchange();
        exchangeMessageService.sendMessage("Test send message to fanout Exchange",fanoutExchange);
        exchangeMessageService.sendMessage("Test send message to topic Exchange",topicExchange);
        exchangeMessageService.sendMessage("Test send message to direct Exchange",directExchange);
//        exchangeMessageService.getMessageFromQueue(CommonConstant.QUEUE_NAME_HEADER);

    }
}

