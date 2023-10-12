package vn.vnpay.rabbitmq.service;

import vn.vnpay.rabbitmq.bean.PaymentRecord;
import vn.vnpay.rabbitmq.factory.PaymentRequest;

import java.util.Optional;

public interface IPaymentRecordService {

    PaymentRecord savePaymentRecord(PaymentRecord paymentRecord);

    boolean pushRedis(PaymentRequest paymentRequest);

    Optional<PaymentRecord> getById(int id);
}
