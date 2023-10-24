package vn.vnpay.rabbitmq.service;

import vn.vnpay.rabbitmq.bean.PaymentRecord;
import vn.vnpay.rabbitmq.factory.PaymentRequest;

public interface IPaymentRecordService {

    PaymentRecord savePaymentRecord(PaymentRecord paymentRecord, String correlationId);

    boolean pushRedis(PaymentRequest paymentRequest);

}
