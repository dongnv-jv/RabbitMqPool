package vn.vnpay.rabbitmq.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.vnpay.rabbitmq.annotation.Autowire;
import vn.vnpay.rabbitmq.annotation.Component;
import vn.vnpay.rabbitmq.annotation.CustomValue;
import vn.vnpay.rabbitmq.service.IExchangeMessageService;
import vn.vnpay.rabbitmq.service.impl.ExchangeMessageServiceImpl;

@Component
public class RabbitMqController {
    private static Logger logger = LoggerFactory.getLogger(ExchangeMessageServiceImpl.class);
    @CustomValue("exchange.direct.queueName")
    private static String queueName;
    @CustomValue("exchange.fanout.name")
    private static String exchangeName;
    @CustomValue("exchange.topic.routingKey")
    private static String routingKey;
    @Autowire
    IExchangeMessageService iExchangeMessageService;

    public void create() {
        iExchangeMessageService.processRPCServer();
    }
}

