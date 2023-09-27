package vn.vnpay.demo.controller;

import vn.vnpay.demo.annotation.CustomValue;
import vn.vnpay.demo.annotation.ValueInjector;
import vn.vnpay.demo.config.CommonConfig;
import vn.vnpay.demo.domain.Student;
import vn.vnpay.demo.factory.DirectExchange;
import vn.vnpay.demo.factory.FanoutExchange;
import vn.vnpay.demo.service.ExchangeMessageService;
import vn.vnpay.demo.service.impl.ExchangeMessageServiceImpl;

import java.lang.reflect.InvocationTargetException;

public class TestChannelPool {

    @CustomValue("exchange.fanout.queue.name-2")
    private static String queueName;

    public static void main(String[] args) throws IllegalAccessException, ClassNotFoundException, InstantiationException, InvocationTargetException, NoSuchMethodException {


        TestChannelPool testChannelPool = new TestChannelPool();
        ValueInjector.injectValues(testChannelPool);
        CommonConfig appConfig = new CommonConfig();
        ValueInjector.injectValues(appConfig);
        DirectExchange directExchange = new DirectExchange();
        ValueInjector.injectValues(directExchange);
//
//        HeaderExchange headerExchange = new HeaderExchange();
//        ValueInjector.injectValues(headerExchange);

        FanoutExchange fanoutExchange = new FanoutExchange();
        ValueInjector.injectValues(fanoutExchange);

        appConfig.configure();

        directExchange.createExchangeAndQueue();
        ExchangeMessageService exchangeMessageService = new ExchangeMessageServiceImpl();
        ValueInjector.injectValues(exchangeMessageService);
        exchangeMessageService.sendMessage(new Student(1, "Nguyễn Văn A", 23), directExchange);

        exchangeMessageService.getMessageFromQueue(queueName,String.class);
    }
}

