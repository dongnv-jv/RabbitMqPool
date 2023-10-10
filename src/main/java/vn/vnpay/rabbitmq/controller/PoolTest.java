package vn.vnpay.rabbitmq.controller;

import vn.vnpay.rabbitmq.annotation.CustomValue;
import vn.vnpay.rabbitmq.annotation.ValueInjector;
import vn.vnpay.rabbitmq.config.CommonConfig;
import vn.vnpay.rabbitmq.factory.BaseExchange;
import vn.vnpay.rabbitmq.factory.PaymentRequest;
import vn.vnpay.rabbitmq.factory.impl.FanoutExchange;
import vn.vnpay.rabbitmq.service.IExchangeMessageService;
import vn.vnpay.rabbitmq.service.impl.ExchangeMessageServiceImpl;

import java.util.HashMap;
import java.util.Map;

public class PoolTest {

    @CustomValue("exchange.direct.queueName")
    private static String queueName;
    @CustomValue("exchange.fanout.name")
    private static String exchangeName;
    @CustomValue("exchange.topic.routingKey")
    private static String routingKey;


    public static void main(String[] args) throws IllegalAccessException {

        // Inject values to TestChannelPool
        PoolTest poolTest = new PoolTest();
        ValueInjector.injectValues(poolTest);

        // Inject values to CommonConfig
        CommonConfig appConfig = new CommonConfig();
        ValueInjector.injectValues(appConfig);
        appConfig.configure();

        // Inject values to Exchange
//        BaseExchange exchange = new FanoutExchange();
//        ValueInjector.injectValues(exchange);
//        exchange.createExchangeAndQueue();

        // Inject values to ExchangeMessageService
        IExchangeMessageService iExchangeMessageService = new ExchangeMessageServiceImpl();
        ValueInjector.injectValues(iExchangeMessageService);

//        List<Student> studentList = Arrays.asList(new Student(1, "Nguyen Van A", 24), new Student(2, "Nguyen Van B", 25), new Student(3, "Nguyen Van C", 26));
        Map<String, Object> mapPropsForHeaders = new HashMap<>();
//        IExchangeMessageService.sendMessage(" ", "", exchangeName, mapPropsForHeaders);

        // Receiver message from queue
//        IExchangeMessageService.getMessageFromQueue(queueName, PaymentRequest.class);

        iExchangeMessageService.processRPCServer();
    }
}

