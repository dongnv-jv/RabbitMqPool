package vn.vnpay.rabbitmq.controller;

import vn.vnpay.rabbitmq.bean.PaymentRecord;
import vn.vnpay.rabbitmq.service.impl.PaymentRecordServiceImpl;

import java.time.LocalDateTime;

public class testDB {
    public static void main(String[] args) {
        PaymentRecordServiceImpl paymentRecordService = new PaymentRecordServiceImpl();
        PaymentRecord paymentRecord = new PaymentRecord();
        paymentRecord.setCustomerName("Nguyễn Văn A");
        paymentRecord.setAmount(1000000.00);
        paymentRecord.setDebitAmount(1000000.00);
        paymentRecord.setRealAmount(88888.00);
        paymentRecord.setJsonData("{\n" +
                "\t\"userName\": \"NGUY T T HA\",\n" +
                "\t\"customerName\": \"NGUY T T HA\",\n" +
                "\t\"tranxId\": \"700056569\",\n" +
                "\t\"mobileNo\": \"0336371711\",\n" +
                "\t\"accountNo\": \"4129770000000139\",\n" +
                "\t\"cusName\": \"NGUY T T HA\",\n" +
                "\t\"invoiceNo\": \"INV-E3R487X8CYUQVFB4\",\n" +
                "\t\"amount\": 200000,\n" +
                "\t\"status\": \"3\",\n" +
                "\t\"rescode\": \"00\",\n" +
                "\t\"bankCode\": \"970436\",\n" +
                "\t\"tranxNote\": \"Tru tien thanh cong, so trace 028977\",\n" +
                "\t\"tranxDate\": \"20211012171434\",\n" +
                "\t\"tipAndFee\": \"0\",\n" +
                "\t\"type\": \"2\",\n" +
                "\t\"item\": [\n" +
                "\t\t{\n" +
                "\t\t\t\"note\": \"\",\n" +
                "\t\t\t\"quantity\": \"1\",\n" +
                "\t\t\t\"qrInfor\": \"0002010102110216412977000000013952045812530370458037046005HANOI6304CFD9\"\n" +
                "\t\t}\n" +
                "\t],\n" +
                "\t\"qrInfo\": \"{\\\"version\\\":\\\"5.2.3\\\",\\\"PM\\\":\\\"Pixel 2 XL\\\",\\\"OV\\\":\\\"11\\\",\\\"PS\\\":\\\"IS_SAFE\\\",\\\"DT\\\":\\\"ANDROID\\\",\\\"IMEI\\\":\\\"###77fb26dca19ada4b###ffffffff-f15a-3d94-ffff-ffffef05ac4a\\\"}\",\n" +
                "\t\"orderCode\": \"700056569\",\n" +
                "\t\"quantity\": \"1\",\n" +
                "\t\"checkSum\": \"018bdd0b131b6f623bbff42c436b23dd\",\n" +
                "\t\"qrVersion\": \"2\",\n" +
                "\t\"mobile\": \"0336371711\",\n" +
                "\t\"respCode\": \"00\",\n" +
                "\t\"respDesc\": \"Tru tien thanh cong, so trace 028977\",\n" +
                "\t\"traceTransfer\": \"028977\",\n" +
                "\t\"messageType\": \"1\",\n" +
                "\t\"debitAmount\": \"200000.0\",\n" +
                "\t\"payDate\": \"20211012171434\",\n" +
                "\t\"realAmount\": \"200000.0\",\n" +
                "\t\"promotionCode\": \"\",\n" +
                "\t\"Url\": \"http://10.20.27.18:8080/QRCodePaymentAPIRest/rest/QrcodePayment/paymentServiceCard\",\n" +
                "\t\"mobileId\": \"111001655\",\n" +
                "\t\"clientId\": \"210012299\",\n" +
                "\t\"device\": \"Pixel 2 XL\",\n" +
                "\t\"ipAddress\": \"10.22.7.11\",\n" +
                "\t\"imei\": \"###77fb26dca19ada4b###ffffffff-f15a-3d94-ffff-ffffef05ac4a\",\n" +
                "\t\"totalAmount\": \"200000.0\",\n" +
                "\t\"feeAmount\": \"0\",\n" +
                "\t\"pcTime\": \"171432\",\n" +
                "\t\"tellerId\": \"5078\",\n" +
                "\t\"tellerBranch\": \"06800\",\n" +
                "\t\"typeSource\": \"02\",\n" +
                "\t\"bankCard\": \"02\"\n" +
                "}");

        paymentRecord.setRescode("00");
        paymentRecord.setPayDate(LocalDateTime.of(2023, 12, 23, 11, 23, 31));
        paymentRecord.setLocalDate(LocalDateTime.now());
        paymentRecordService.save(paymentRecord);
    }
}
