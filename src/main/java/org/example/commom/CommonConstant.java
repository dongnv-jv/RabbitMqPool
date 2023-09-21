package org.example.commom;

public class CommonConstant {

    public static final String QUEUE_NAME_HEADER = "header-queue";

    private CommonConstant() {
    }

    public static final String EXCHANGE_FANOUT = "fanout-exchange";
    public static final String QUEUE_NAME_FANOUT_1 = "fanout-queue-1";
    public static final String QUEUE_NAME_FANOUT_2 = "fanout-queue-2";
    public static final String QUEUE_NAME_FANOUT_3 = "fanout-queue-3";
    public static final String ROUTING_KEY = "";

    // direct exchange
    public static final String EXCHANGE_DIRECT = "direct-exchange 11111";
    public static final String QUEUE_NAME_DIRECT_1 = "direct-queue-1";
    public static final String QUEUE_NAME_DIRECT_2 = "direct-queue-2";
    public static final String QUEUE_NAME_DIRECT_3 = "direct-queue-3";

    public static final  String ROUTING_KEY_DIRECT_1 = "direct-key-1";
    public static final String ROUTING_KEY_DIRECT_2 = "direct-key-2";
    public static final String ROUTING_KEY_DIRECT_3 = "direct-key-3";

// topic exchange

    public static String ROUTING_KEY_1 = "asia.china.1.e";
    public static final String EXCHANGE_TOPIC = "topic-exchange";
    public static final String QUEUE_NAME_TOPIC_1 = "topic-queue-1";
    public static final String QUEUE_NAME_TOPIC_2 = "topic-queue-2";
    public static final String QUEUE_NAME_TOPIC_3 = "topic-queue-3";
    public static final String ROUTING_PATTERN_1 = "asia.china.#";
    public static final String ROUTING_PATTERN_2 = "asia.*";
    public static final String ROUTING_KEY_2 = "asia.viet.nam";
    public static final String ROUTING_PATTERN_3 = "asia.*.*";



    // Header exchange

    public static final String EXCHANGE_HEADER = "header-exchange";
}
