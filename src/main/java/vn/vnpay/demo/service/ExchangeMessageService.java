package vn.vnpay.demo.service;

import vn.vnpay.demo.factory.BaseExchange;

public interface ExchangeMessageService {

     void sendMessage(String message, BaseExchange exchange);
     void getMessageFromQueue(String queueName);
}
