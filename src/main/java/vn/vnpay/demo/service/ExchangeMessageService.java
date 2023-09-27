package vn.vnpay.demo.service;

import vn.vnpay.demo.factory.BaseExchange;

public interface ExchangeMessageService {

     void sendMessage(Object message, BaseExchange exchange);
     <T> void getMessageFromQueue(String queueName,Class<T> clazz);
}
