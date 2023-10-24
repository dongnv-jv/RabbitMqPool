package vn.vnpay.rabbitmq.controller;

import vn.vnpay.rabbitmq.annotation.Autowire;
import vn.vnpay.rabbitmq.annotation.Component;
import vn.vnpay.rabbitmq.service.IExchangeMessageService;

@Component
public class RabbitMqController {
    @Autowire
    IExchangeMessageService iExchangeMessageService;

    public void create() {
        iExchangeMessageService.processRPCServer();
    }
}

