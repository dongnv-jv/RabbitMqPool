package vn.vnpay.rabbitmq.factory;

public class ResponsePayment {
    private long id;
    private String token;

    public ResponsePayment() {
    }

    private ResponsePayment(Builder builder) {
        this.id = builder.id;
        this.token = builder.token;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public static class Builder {
        private long id;
        private String token;

        public Builder id(long id) {
            this.id = id;
            return this;
        }

        public Builder token(String token) {
            this.token = token;
            return this;
        }

        public ResponsePayment build() {
            return new ResponsePayment(this);
        }
    }
}
