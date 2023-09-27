package vn.vnpay.demo.controller;

import vn.vnpay.demo.annotation.ValueInjector;
import vn.vnpay.demo.common.CommonConstant;
import vn.vnpay.demo.config.CommonConfig;
import vn.vnpay.demo.config.channel.ChannelPool;
import vn.vnpay.demo.domain.Student;
import vn.vnpay.demo.factory.DirectExchange;
import vn.vnpay.demo.factory.FanoutExchange;
import vn.vnpay.demo.factory.HeaderExchange;
import vn.vnpay.demo.factory.TopicExchange;
import vn.vnpay.demo.scan.PackageScanner;
import vn.vnpay.demo.service.ExchangeMessageService;
import vn.vnpay.demo.service.impl.ExchangeMessageServiceImpl;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TestChannelPool {

    static AtomicInteger count = new AtomicInteger(0);
    public static void main(String[] args) throws IllegalAccessException, ClassNotFoundException, InstantiationException, InvocationTargetException, NoSuchMethodException {

//         ChannelPool.getInstance();
//        ExchangeMessageService exchangeMessageService = new ExchangeMessageServiceImpl();
//
//        HeaderExchange exchange = new HeaderExchange();
//        FanoutExchange fanoutExchange = new FanoutExchange();
//        TopicExchange topicExchange = new TopicExchange();
//        DirectExchange directExchange = new DirectExchange();
//        exchangeMessageService.sendMessage("Test send message to fanout Exchange",fanoutExchange);
//        exchangeMessageService.sendMessage("Test send message to topic Exchange",topicExchange);
//        exchangeMessageService.sendMessage("Test send message to direct Exchange",directExchange);
//        exchangeMessageService.getMessageFromQueue(CommonConstant.QUEUE_NAME_HEADER);






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
        exchangeMessageService.sendMessage(new Student(count.incrementAndGet(), "Nguyễn Văn A",23),directExchange);

        exchangeMessageService.getMessageFromQueue(CommonConstant.QUEUE_NAME_DIRECT_1);
    }
}

