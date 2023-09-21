package vn.vnpay.demo.controller;

import vn.vnpay.demo.commom.CommonConstant;
import vn.vnpay.demo.config.channelpoolconfig.ChannelPool;
import vn.vnpay.demo.factory.HeaderExchange;
import vn.vnpay.demo.service.SendMessageService;
import vn.vnpay.demo.service.impl.SendMessageServiceImpl;
import vn.vnpay.demo.factory.FanoutExchange;

public class TestChannelPool {


    public static void main(String[] args) {
         ChannelPool.getInstance();
        SendMessageService sendMessageService = new SendMessageServiceImpl();

        HeaderExchange exchange = new HeaderExchange();
        sendMessageService.sendMessage("Test send message to fanout Exchange",exchange);
        sendMessageService.getMessageFromQueue(CommonConstant.QUEUE_NAME_FANOUT_3);

    }
}

