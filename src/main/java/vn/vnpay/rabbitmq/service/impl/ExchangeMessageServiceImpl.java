package vn.vnpay.rabbitmq.service.impl;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConfirmListener;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.RpcServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.vnpay.rabbitmq.annotation.CustomValue;
import vn.vnpay.rabbitmq.bean.PaymentRecord;
import vn.vnpay.rabbitmq.common.ObjectConverter;
import vn.vnpay.rabbitmq.config.channel.ChannelPool;
import vn.vnpay.rabbitmq.config.threadpool.ThreadPoolConfig;
import vn.vnpay.rabbitmq.bean.Student;
import vn.vnpay.rabbitmq.factory.PaymentRequest;
import vn.vnpay.rabbitmq.factory.Response;
import vn.vnpay.rabbitmq.service.IExchangeMessageService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

public class ExchangeMessageServiceImpl implements IExchangeMessageService {
    private final Logger logger = LoggerFactory.getLogger(ExchangeMessageServiceImpl.class);
    @CustomValue("charset.Name")
    private String charSet;
    @CustomValue("exchange.direct.queueName")
    private String queueName;
    @CustomValue("exchange.dead.letter.name")
    private String deadLetterExchange;
    @CustomValue("exchange.dead.letter.routingKey")
    private String deadLetterRoutingKey;
    @CustomValue("exchange.dead.letter.queueName")
    private String deadLetterQueueName;
    @CustomValue("consumer.prefetchCount")
    private int prefetchCount;
    @CustomValue("exchange.rpc.queueName")
    private static String rpcQueueName;
    @CustomValue("exchange.rpc.replyQueueName")
    private static String replyQueueName;
    private volatile boolean hasFailedMessage = false;
    PaymentRecordServiceImpl paymentRecordService = new PaymentRecordServiceImpl();

    private synchronized void handleSendFailedMessage(Channel channel) {

        if (!hasFailedMessage) {
            channel.addReturnListener((replyCode, replyText, exchange, routingKey, properties, body) -> {
// Khi không đúng routing key thì chuyển vào DLX
                channel.basicPublish(deadLetterExchange, deadLetterRoutingKey, false, null, body);
                logger.error("Message send failed because wrong routing key : {} with exchange : {}", routingKey, exchange);
                hasFailedMessage = true;
            });
// Xác nhận rabbitServer có nhân message thành công hay không
            channel.addConfirmListener(new ConfirmListener() {
                @Override
                public void handleAck(long deliveryTag, boolean multiple) {
                    // do nothing
                }

                @Override
                public void handleNack(long deliveryTag, boolean multiple) {
                    logger.info("Failed to send message to Rabbit server with deliveryTag: {} ", deliveryTag);
                }
            });
        }

    }

    public void sendMessage(Object message, String routingKey, String exchangeName, Map<String, Object> mapPropsForHeaders) {
        long start = System.currentTimeMillis();
        logger.info("Start sendToExchange in ExchangeMessageServiceImpl ");
        Executor executor = ThreadPoolConfig.getExecutor();
        ChannelPool channelPool = ChannelPool.getInstance();
        for (int i = 0; i < 30; i++) {
            executor.execute(() -> this.sendToExchange(message, channelPool, routingKey, exchangeName, mapPropsForHeaders));
        }
        long end = System.currentTimeMillis();
        logger.info("Process sendToExchange in ExchangeMessageServiceImpl take {} millisecond", (end - start));
    }

    private void sendToExchange(Object message, ChannelPool channelPool, String routingKey, String exchangeName, Map<String, Object> mapPropsForHeaders) {

        Channel channel = null;
        try {
            channel = channelPool.getChannel();
            channel.confirmSelect();
            this.handleSendFailedMessage(channel);
            AMQP.BasicProperties props = new AMQP.BasicProperties();
            props = props.builder().headers(mapPropsForHeaders).build();
            channel.basicPublish(exchangeName, routingKey, true, props, ObjectConverter.objectToBytes(message));
        } catch (Exception e) {
            logger.error(" Send message to exchange failed with root cause ", e);
        } finally {
            if (channel != null) {
                channelPool.returnChannel(channel);
            }

        }
    }

    public <T> void getMessageFromQueue(String queueName, Class<T> clazz) {
        long start = System.currentTimeMillis();
        logger.info("Start getMessageFromQueue in ExchangeMessageServiceImpl ");
        Executor executor = ThreadPoolConfig.getExecutor();
        ChannelPool channelPool = ChannelPool.getInstance();
//        for (int i = 0; i < 10; i++) {
//            executor.execute(() -> {
        this.getMessageFromQueue(queueName, clazz, channelPool);
//            });
//        }
        long end = System.currentTimeMillis();
        logger.info("Process getMessageFromQueue in ExchangeMessageServiceImpl take {} millisecond", (end - start));
    }

    private <T> void getMessageFromQueue(String queueName, Class<T> clazz, ChannelPool channelPool) {
        Channel channel = null;
        Executor executor = ThreadPoolConfig.getExecutor();
        CompletableFuture<PaymentRequest> paymentRequestFuture = CompletableFuture.completedFuture(new PaymentRequest());
        try {
            channel = channelPool.getChannel();
            channel.basicQos(prefetchCount); // Giới hạn số tin nhắn gửi đến consumer chưa được xác nhận
            this.getMessageFromQueue(channel, queueName, clazz);


        } catch (Exception e) {
            logger.error(" Receiver message from queue {} failed with root cause ", queueName, e);
        } finally {
            if (channel != null) {
                channelPool.returnChannel(channel);
            }
        }
    }

    private <T> void getMessageFromQueue(Channel channel, String queueName, Class<T> clazz) throws IOException {
        CompletableFuture<PaymentRequest> future = new CompletableFuture<>();
        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {

                try {
                    future.complete((PaymentRequest) ObjectConverter.bytesToObject(body, clazz));
//                    PaymentRequest paymentRequest = future.get();
//                    PaymentRecord paymentRecord = convertRequest(paymentRequest);
//                    paymentRecordService.pushRedis(paymentRequest);
//                    paymentRecordService.save(paymentRecord);
//                    processPayment(future);
//                    logger.info("Received payment request " + paymentRecord.getCustomerName());
                    channel.basicAck(envelope.getDeliveryTag(), false);
                } catch (Exception e) {
                    logger.error(" Receiver message  from queue {} failed with root cause ", queueName, e);
                    channel.basicReject(envelope.getDeliveryTag(), false);
                }
            }
        };
        channel.basicConsume(queueName, false, consumer);

    }

    private PaymentRecord convertRequest(PaymentRequest request) throws IOException {
        PaymentRecord paymentRecord = new PaymentRecord();
        paymentRecord.setCustomerName(request.getCustomerName());
        paymentRecord.setAmount(request.getAmount());
        paymentRecord.setDebitAmount(Double.parseDouble(request.getDebitAmount()));
        paymentRecord.setRealAmount(Double.parseDouble((request.getRealAmount())));
        paymentRecord.setJsonData(ObjectConverter.objectToJson(request));
        paymentRecord.setRescode(request.getRescode());
        paymentRecord.setPayDate(ObjectConverter.convertStringToDateTime(request.getPayDate()));
        paymentRecord.setLocalDate(LocalDateTime.now());
        return paymentRecord;
    }

    private boolean processPayment(PaymentRequest paymentRequest) {
        try {
            PaymentRecord paymentRecord = convertRequest(paymentRequest);
            paymentRecordService.pushRedis(paymentRequest);
            paymentRecordService.save(paymentRecord);
            logger.info("PaymentRecord is processed successfully !");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void processRPCServer() {
        ChannelPool channelPool = ChannelPool.getInstance();
        Channel channel = null;
        try {
            channel = channelPool.getChannel();
            channel.queueDeclare(rpcQueueName, false, false, false, null);
            Channel finalChannel = channel;
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                        .Builder()
                        .correlationId(delivery.getProperties().getCorrelationId())
                        .build();
                PaymentRequest paymentRequest = ObjectConverter.bytesToObject(delivery.getBody(), PaymentRequest.class);
                boolean checkResponse = processPayment(paymentRequest);
                Response<String> response = new Response<>();
                if (checkResponse) {
                    response.setCode("00");
                    response.setMessage("Success");
                    response.setData(paymentRequest.getToken());
                } else {
                    response.setCode("01");
                    response.setMessage("Fail");
                    response.setData(paymentRequest.getToken());
                }
                finalChannel.basicPublish("", delivery.getProperties().getReplyTo(), replyProps, ObjectConverter.objectToBytes(response));
                finalChannel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            };
            channel.basicConsume(rpcQueueName, false, deliverCallback, consumerTag -> {
            });
        } catch (Exception e) {
            logger.error(" Receiver message  from queue {} failed with root cause ", rpcQueueName, e);
        }finally {
            if (channel != null) {
                channelPool.returnChannel(channel);
            }
        }
    }

}
