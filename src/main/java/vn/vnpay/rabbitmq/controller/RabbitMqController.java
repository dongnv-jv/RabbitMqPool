package vn.vnpay.rabbitmq.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.vnpay.rabbitmq.annotation.Autowire;
import vn.vnpay.rabbitmq.annotation.Component;
import vn.vnpay.rabbitmq.service.IExchangeMessageService;

@Component
public class RabbitMqController {
    private static Logger logger = LoggerFactory.getLogger(RabbitMqController.class);
    @Autowire
    IExchangeMessageService iExchangeMessageService;

    public void create() {
        iExchangeMessageService.processRPCServer();
    }
}

