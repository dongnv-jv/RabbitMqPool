package vn.vnpay.demo.domain;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

public class RabbitMQSetupStuff {

    private static String QUEUE1 = "myqueue1";
    private static String DLQ1 = "dlq1";
    private static String EXCHANGE1 = "myexchange1";
    private static String DLX1 = "dlx1";
    private static String DLROUTINGKEY = "dlrk";
    private static String ROUTINGKEY = "myrk";


    public static void main(String[] args) throws IOException, TimeoutException {
        ConnectionFactory connectionFactory = connectionFactory();
        Connection connection = connectionFactory.newConnection();
        Channel channel = channel(connection);
//        Queue 1 and exchange created
        q1(channel);
        exchange1(channel);
//        Dead Letter handling
        deadLetterQueueAndExchange(channel);
        sendMessage(channel);
        connection.close();
    }

    private static void sendMessage(Channel channel) throws IOException {
        AMQP.BasicProperties basicProperties = new AMQP.BasicProperties();
        channel.basicPublish(EXCHANGE1, ROUTINGKEY + ".mymessage1.33", basicProperties,
                "Hello this is a message created from Java! yaaaayy...".getBytes());
    }

    private static void deadLetterQueueAndExchange(Channel channel) throws IOException {
        channel.queueDelete(DLQ1);
        channel.queueDeclare(DLQ1, true, false, false,new HashMap<>());

        channel.exchangeDelete(DLX1);
        channel.exchangeDeclare(DLX1, "topic");

        channel.queueBind(DLQ1,DLX1,DLROUTINGKEY + ".#");
    }

    private static void exchange1(Channel channel) throws IOException {
        channel.exchangeDelete(EXCHANGE1);
        channel.exchangeDeclare(EXCHANGE1, "topic");
        channel.queueBind(QUEUE1, EXCHANGE1, ROUTINGKEY + ".#");
    }

    private static void q1(Channel channel) throws IOException {
        Map<String, Object> arguments = argumentsDeadletterQueue();
        channel.queueDelete(QUEUE1);
        channel.queueDeclare(QUEUE1, true, false, false,arguments);
    }

    private static Map<String, Object> argumentsDeadletterQueue() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-message-ttl", 20000);
        arguments.put("x-dead-letter-exchange", DLX1);
        arguments.put("x-dead-letter-routing-key", DLROUTINGKEY + ".test1");
        return arguments;
    }

    private static Channel channel(Connection connection) throws IOException {
        return connection.createChannel();
    }

    private static ConnectionFactory connectionFactory() {
        ConnectionFactory connectionFactory  = new ConnectionFactory();
        connectionFactory.setHost("dingo.rmq.cloudamqp.com");
        connectionFactory.setPort(5672);
        connectionFactory.setUsername("wqmrgbxx");
        connectionFactory.setVirtualHost("wqmrgbxx");
        connectionFactory.setPassword("xV0hehlkpcdRD8FxZN4OrAFgizHRpCqS");
        return connectionFactory;
    }
}
