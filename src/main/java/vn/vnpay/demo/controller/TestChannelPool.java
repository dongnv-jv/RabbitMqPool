package vn.vnpay.demo.controller;

import vn.vnpay.demo.common.CommonConstant;
import vn.vnpay.demo.config.channelpoolconfig.ChannelPool;
import vn.vnpay.demo.factory.HeaderExchange;
import vn.vnpay.demo.service.SendMessageService;
import vn.vnpay.demo.service.impl.SendMessageServiceImpl;

public class TestChannelPool {


    public static void main(String[] args) {
         ChannelPool.getInstance();
        SendMessageService sendMessageService = new SendMessageServiceImpl();

        HeaderExchange exchange = new HeaderExchange();
        sendMessageService.sendMessage("Test send message to fanout Exchange",exchange);
//        sendMessageService.getMessageFromQueue(CommonConstant.QUEUE_NAME_HEADER);

    }
}

