package vn.vnpay.demo.service;

import vn.vnpay.demo.factory.BaseExchange;

import java.util.Map;

public interface ExchangeMessageService {

    void sendMessage(Object message, BaseExchange exchange, String routingKey, String exchangeName, Map<String, Object> mapPropsForHeaders);

    <T> void getMessageFromQueue(String queueName, Class<T> clazz);
}
