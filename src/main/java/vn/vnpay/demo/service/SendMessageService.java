package vn.vnpay.demo.service;

import vn.vnpay.demo.factory.BaseExchange;

public interface SendMessageService {

     void sendMessage(String message, BaseExchange exchange);
     void getMessageFromQueue(String queueName);
}
