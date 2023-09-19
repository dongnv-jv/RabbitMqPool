package org.example.commom;

public enum ExchangeType {

    DIRECT("direct"),
    TOPIC("topic"),
    FANOUT("fanout"),
    HEADER("headers");

    private final String exchangeName;

    ExchangeType(String exchangeName) {
        this.exchangeName = exchangeName;
    }

    public String getExchangeName() {
        return this.exchangeName;
    }
}
