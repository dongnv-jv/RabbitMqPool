package vn.vnpay.rabbitmq;


import vn.vnpay.rabbitmq.config.CommonConfig;
import vn.vnpay.rabbitmq.controller.RabbitMqController;
import vn.vnpay.rabbitmq.scan.ApplicationContext;

public class Main {
    public static void main(String[] args) {
        try {
            ApplicationContext context = new ApplicationContext("vn.vnpay.rabbitmq");
            CommonConfig commonConfig = context.getBean(CommonConfig.class);
            boolean isConfig = commonConfig.configure();
            if (!isConfig) {
                throw new RuntimeException("Configure failed");
            }
            RabbitMqController rabbitMqController = context.getBean(RabbitMqController.class);
            rabbitMqController.create();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}