package vn.vnpay.rabbitmq.common;

public enum ResponseCode {
    SUCCESS("00", "Success"),
    FAILURE("01", "Fail"),
    EXPIRED("402", "Expired");
    final String code;
    final String message;

    ResponseCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
