package org.example;

import org.example.commom.CommonConstant;
import org.example.service.TestPoolService;
import org.example.work.FanoutExchange;

public class TestChannelPool {


    public static void main(String[] args) {

        TestPoolService testPoolService = new TestPoolService();

        FanoutExchange exchange = new FanoutExchange();
        testPoolService.sendToExchange("Test send message to fanout Exchange",exchange);
        testPoolService.getMessage(CommonConstant.QUEUE_NAME_FANOUT_2);

    }
}

