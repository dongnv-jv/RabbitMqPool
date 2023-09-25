package vn.vnpay.demo.controller;

import vn.vnpay.demo.config.channel.ChannelPool;
import vn.vnpay.demo.factory.HeaderExchange;
import vn.vnpay.demo.service.ExchangeMessageService;
import vn.vnpay.demo.service.impl.ExchangeMessageServiceImpl;

public class TestChannelPool {


    public static void main(String[] args) {
         ChannelPool.getInstance();
        ExchangeMessageService exchangeMessageService = new ExchangeMessageServiceImpl();

        HeaderExchange exchange = new HeaderExchange();
        exchangeMessageService.sendMessage("Test send message to fanout Exchange",exchange);
//        sendMessageService.getMessageFromQueue(CommonConstant.QUEUE_NAME_HEADER);

    }
}

