package vn.vnpay.demo.config;

import vn.vnpay.demo.annotation.CustomValue;
import vn.vnpay.demo.config.connection.RabbitMqConnectionFactory;
import vn.vnpay.demo.config.connection.RabbitMqConnectionPool;

public class CommonConfig {
    @CustomValue("rabbitMq.host")
    private String host;
    @CustomValue("rabbitMq.port")
    private int port;
    @CustomValue("rabbitMq.username")
    private String username;
    @CustomValue("rabbitMq.password")
    private String password;
    @CustomValue("rabbitMq.virtualHost")
    private String virtualHost;

    public void configure() {
        RabbitMqConnectionFactory factory = RabbitMqConnectionFactory.getInstance(host, port, username, password, virtualHost);
        RabbitMqConnectionPool rabbitMqConnectionPool =  RabbitMqConnectionPool.getInstance(factory);

    }

}
