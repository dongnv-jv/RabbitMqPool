package vn.vnpay.rabbitmq.service;

import java.util.Map;

public interface IExchangeMessageService {

    void sendMessage(Object message, String routingKey, String exchangeName, Map<String, Object> mapPropsForHeaders);

    <T> void getMessageFromQueue(String queueName, Class<T> clazz);

     void processRPCServer();
}
