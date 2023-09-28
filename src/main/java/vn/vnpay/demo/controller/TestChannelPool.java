package vn.vnpay.demo.controller;

import vn.vnpay.demo.annotation.CustomValue;
import vn.vnpay.demo.annotation.ValueInjector;
import vn.vnpay.demo.config.CommonConfig;
import vn.vnpay.demo.domain.Student;
import vn.vnpay.demo.factory.BaseExchange;
import vn.vnpay.demo.factory.DeadLetterExchange;
import vn.vnpay.demo.factory.DirectExchange;
import vn.vnpay.demo.factory.FanoutExchange;
import vn.vnpay.demo.service.ExchangeMessageService;
import vn.vnpay.demo.service.impl.ExchangeMessageServiceImpl;

import java.util.Collections;
import java.util.Map;

public class TestChannelPool {

    @CustomValue("exchange.header.queueName")
    private static String queueName;
    @CustomValue("exchange.direct.name")
    private static String exchangeName;
    @CustomValue("exchange.topic.routingKey")
    private static String routingKey;

    public static void main(String[] args) throws IllegalAccessException {

// Inject values to TestChannelPool
        TestChannelPool testChannelPool = new TestChannelPool();
        ValueInjector.injectValues(testChannelPool);

// Inject values to CommonConfig
        CommonConfig appConfig = new CommonConfig();
        ValueInjector.injectValues(appConfig);


// Inject values to Exchange
        BaseExchange exchange = new DirectExchange();
        ValueInjector.injectValues(exchange);

        appConfig.configure();

        exchange.createExchangeAndQueue();
        ExchangeMessageService exchangeMessageService = new ExchangeMessageServiceImpl();
        ValueInjector.injectValues(exchangeMessageService);
        Map<String, Object> mapPropsForHeaders = Collections.emptyMap();
        exchangeMessageService.sendMessage(new Student(1, "Nguyễn Văn A", 23), exchange, routingKey, exchangeName, mapPropsForHeaders);
// Receiver message from queue
//        exchangeMessageService.getMessageFromQueue(queueName, String.class);
    }
}

