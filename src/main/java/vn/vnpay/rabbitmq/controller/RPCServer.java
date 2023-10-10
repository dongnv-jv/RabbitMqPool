package vn.vnpay.rabbitmq.controller;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import vn.vnpay.rabbitmq.annotation.ValueInjector;
import vn.vnpay.rabbitmq.common.ObjectConverter;
import vn.vnpay.rabbitmq.config.CommonConfig;
import vn.vnpay.rabbitmq.config.channel.ChannelPool;
import vn.vnpay.rabbitmq.factory.Response;

public class RPCServer {
    private static final String REQUEST_QUEUE_NAME = "rpc_request_queue";

    public static void main(String[] argv) throws Exception {


        try {
            CommonConfig appConfig = new CommonConfig();
            ValueInjector.injectValues(appConfig);
            appConfig.configure();
            ChannelPool channelPool = ChannelPool.getInstance();
            Channel channel = channelPool.getChannel();


            channel.queueDeclare(REQUEST_QUEUE_NAME, false, false, false, null);

            System.out.println(" [*] Đang chờ yêu cầu RPC");

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                        .Builder()
                        .correlationId(delivery.getProperties().getCorrelationId())
                        .build();


                Response response = new Response();


                channel.basicPublish("", delivery.getProperties().getReplyTo(), replyProps, ObjectConverter.objectToBytes(response));
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            };

            channel.basicConsume(REQUEST_QUEUE_NAME, false, deliverCallback, consumerTag -> {
            });
        } catch (Exception e) {
            // do nothing
        }
    }
}
