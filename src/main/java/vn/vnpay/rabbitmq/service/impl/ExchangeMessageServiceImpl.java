package vn.vnpay.rabbitmq.service.impl;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConfirmListener;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.Delivery;
import com.rabbitmq.client.Envelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.vnpay.rabbitmq.annotation.Autowire;
import vn.vnpay.rabbitmq.annotation.Component;
import vn.vnpay.rabbitmq.annotation.CustomValue;
import vn.vnpay.rabbitmq.bean.PaymentRecord;
import vn.vnpay.rabbitmq.common.CommonUtil;
import vn.vnpay.rabbitmq.common.ResponseCode;
import vn.vnpay.rabbitmq.config.channel.ChannelPool;
import vn.vnpay.rabbitmq.factory.PaymentRequest;
import vn.vnpay.rabbitmq.factory.Response;
import vn.vnpay.rabbitmq.factory.ResponsePayment;
import vn.vnpay.rabbitmq.service.IExchangeMessageService;
import vn.vnpay.rabbitmq.service.IPaymentRecordService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Component
public class ExchangeMessageServiceImpl implements IExchangeMessageService {
    public static final ThreadLocal<String> logIdThreadLocal = new ThreadLocal<>();
    @CustomValue("exchange.rpc.queueName")
    private static String rpcQueueName;
    @CustomValue("exchange.rpc.replyQueueName")
    private static String replyQueueName;
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
    @CustomValue("server.time.sleep")
    private int sleepingTime;
    private volatile boolean hasFailedMessage = false;
    @Autowire
    private IPaymentRecordService iPaymentRecordService;
    @Autowire
    private ChannelPool channelPool;

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
        this.sendToExchange(message, channelPool, routingKey, exchangeName, mapPropsForHeaders);
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
            channel.basicPublish(exchangeName, routingKey, true, props, CommonUtil.objectToBytes(message));
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
        this.getMessageFromQueue(queueName, clazz, channelPool);
        long end = System.currentTimeMillis();
        logger.info("Process getMessageFromQueue in ExchangeMessageServiceImpl take {} millisecond", (end - start));
    }

    private <T> void getMessageFromQueue(String queueName, Class<T> clazz, ChannelPool channelPool) {
        Channel channel = null;
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
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
                                       byte[] body) throws IOException {

                try {
                    future.complete((PaymentRequest) CommonUtil.bytesToObject(body, clazz));
                    channel.basicAck(envelope.getDeliveryTag(), false);
                } catch (Exception e) {
                    logger.error(" Receiver message  from queue {} failed with root cause ", queueName, e);
                    channel.basicReject(envelope.getDeliveryTag(), false);
                }
            }
        };
        channel.basicConsume(queueName, false, consumer);

    }

    public void processRPCServer() {
        long start = System.currentTimeMillis();
        logger.info(" Start processRPCServer in ExchangeMessageServiceImpl ");
        Channel channel = null;
        try {
            channel = channelPool.getChannel();
            this.declareQueue(channel);
            this.processDeliveries(channel);
            long end = System.currentTimeMillis();
            logger.info("Process processRPCServer in ExchangeMessageServiceImpl take {} millisecond", (end - start));
        } catch (Exception e) {
            logger.error("Process processRPCServer in ExchangeMessageServiceImpl from queue {} failed with root cause ", rpcQueueName, e);
        } finally {
            if (channel != null) {
                channelPool.returnChannel(channel);
            }
        }
    }

    private void declareQueue(Channel channel) throws IOException {
        channel.queueDeclare(rpcQueueName, false, false, false, null);
    }

    private void processDeliveries(Channel channel) throws IOException {
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            long start = System.currentTimeMillis();
            String logId = delivery.getProperties().getMessageId();
            logIdThreadLocal.set(logId);
            Response<ResponsePayment> response = new Response<>();
            PaymentRequest paymentRequest = null;
            Date dateExpire = delivery.getProperties().getTimestamp();
            logger.info("[{}] - Receiver messages from queue {} with dateExpire {}", logId, rpcQueueName, dateExpire);
            Date dateNow = new Date();
            long expiration = 60;
            boolean isExpired = CommonUtil.compareDate(dateExpire, dateNow, expiration);
            if (isExpired) {
                logger.info("[{}] - Start processing messages from queue {}", logId, rpcQueueName);
                paymentRequest = this.getPaymentRequest(delivery).orElseThrow(() -> new RuntimeException("Payment request not found"));
            }
            response = this.processPayment(paymentRequest, isExpired);
            this.publishResponse(channel, delivery, response);
            this.acknowledgeDelivery(channel, delivery);
            long end = System.currentTimeMillis();
            logger.info("[{}] - End processing processDeliveries in ExchangeMessageServiceImpl takes {} milliseconds",
                    logId, (end - start));
        };
        channel.basicConsume(rpcQueueName, false, deliverCallback, consumerTag -> {
        });
    }

    private Optional<PaymentRequest> getPaymentRequest(Delivery delivery) {
        String logId = logIdThreadLocal.get();
        PaymentRequest paymentRequest;
        try {
            paymentRequest = CommonUtil.bytesToObject(delivery.getBody(), PaymentRequest.class);
            paymentRequest.setToken(delivery.getProperties().getCorrelationId());
            logger.info("[{}] - Received payment request successfully with correlationId: {}", logId,
                    delivery.getProperties().getCorrelationId());
            return Optional.of(paymentRequest);
        } catch (Exception e) {
            logger.error("Received payment request fail ", e);
            return Optional.empty();
        }
    }

    private Response<ResponsePayment> processPayment(PaymentRequest paymentRequest, boolean isExpired) {
        String logId = logIdThreadLocal.get();
        Response<ResponsePayment> response = new Response<>();
        ResponsePayment responsePayment = new ResponsePayment();
        String responseLog;
        try {
            if (isExpired) {
                boolean isProcessPaymentSuccessfully = this.processPayment(paymentRequest, responsePayment);
                responsePayment.setToken(paymentRequest.getToken());
                if (isProcessPaymentSuccessfully) {
                    responseLog = this.setResponse(response, ResponseCode.SUCCESS.getCode(),
                            ResponseCode.SUCCESS.getMessage(), responsePayment);
                    logger.info("[{}] - Process Payment successfully with response {}", logId, responseLog);
                } else {
                    responseLog = this.setResponse(response, ResponseCode.FAILURE.getCode(), ResponseCode.FAILURE.getMessage(), null);
                    logger.error("[{}] - Process Payment failed due to processPayment message with response {}", logId, responseLog);
                }
            } else {
                responseLog = this.setResponse(response, ResponseCode.FAILURE.getCode(), "Message expired", null);
                logger.error("[{}] - Process payment failed due to expired message with response {}", logId, responseLog);
            }

        } catch (IOException e) {
            response.setCode(ResponseCode.FAILURE.getCode());
            response.setMessage("An error occurred while processing");
            logger.error("[{}] - Process Payment failed ", logId, e);
        }
        return response;
    }

    private <T> String setResponse(Response<T> response, String code, String message, T data) throws IOException {
        response.setCode(code);
        response.setMessage(message);
        response.setData(data);
        return CommonUtil.objectToJson(response);
    }

    private boolean processPayment(PaymentRequest paymentRequest, ResponsePayment responsePayment) throws IOException {
        boolean isPushRedis = iPaymentRecordService.pushRedis(paymentRequest);
        this.sleep();
        if (isPushRedis) {
            PaymentRecord paymentRecord = this.savePaymentRecord(paymentRequest, responsePayment);
            return paymentRecord.getId() != null;
        } else {
            return false;
        }
    }

    private void sleep() {
        String logId = logIdThreadLocal.get();
        try {
            Thread.sleep(sleepingTime);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("[{}] - Failed to sleep ", logId, e);
        }
    }

    private PaymentRecord savePaymentRecord(PaymentRequest paymentRequest, ResponsePayment responsePayment) throws IOException {
        PaymentRecord paymentRecord = this.convertRequest(paymentRequest);
        paymentRecord = iPaymentRecordService.savePaymentRecord(paymentRecord, paymentRequest.getToken());
        if (paymentRecord.getId() != null) {
            responsePayment.setId(paymentRecord.getId());
        }
        return paymentRecord;
    }

    private PaymentRecord convertRequest(PaymentRequest request) throws IOException {
        String logId = logIdThreadLocal.get();
        PaymentRecord paymentRecord = new PaymentRecord();
        paymentRecord.setCustomerName(request.getCustomerName());
        paymentRecord.setAmount(request.getAmount());
        paymentRecord.setDebitAmount(Double.parseDouble(request.getDebitAmount()));
        paymentRecord.setRealAmount(Double.parseDouble((request.getRealAmount())));
        paymentRecord.setJsonData(CommonUtil.objectToJson(request));
        paymentRecord.setRescode(request.getRescode());
        paymentRecord.setPayDate(CommonUtil.convertStringToDateTime(request.getPayDate()));
        paymentRecord.setLocalDate(LocalDateTime.now());
        logger.info("[{}] - Convert PaymentRequest to paymentRecord successfully ", logId);
        return paymentRecord;
    }

    private void publishResponse(Channel channel, Delivery delivery, Response<ResponsePayment> response) throws IOException {
        String logId = logIdThreadLocal.get();
        AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                .Builder()
                .correlationId(delivery.getProperties().getCorrelationId())
                .build();
        channel.basicPublish("", delivery.getProperties().getReplyTo(), replyProps, CommonUtil.objectToBytes(response));
        logger.info("[{}] - Publish Response to rabbitMq successfully !", logId);
    }

    private void acknowledgeDelivery(Channel channel, Delivery delivery) throws IOException {
        channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
    }

}
