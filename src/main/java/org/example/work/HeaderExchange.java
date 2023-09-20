package org.example.work;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import org.example.channel.ChannelPool;
import org.example.commom.CommonConstant;
import org.example.connection.RabbitMqConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class HeaderExchange {
    Logger logger = LoggerFactory.getLogger(HeaderExchange.class);

    public void createExchangeAndQueue(RabbitMqConnectionPool rabbitMqConnectionPool, ChannelPool channelPool){
        Long start = System.currentTimeMillis();
        try{
            Connection conn = rabbitMqConnectionPool.getConnection();
            Map<String,Object> map = null;
            if(conn != null){
                Channel channel = channelPool.getChannel();
                channel.exchangeDeclare(CommonConstant.EXCHANGE_HEADER, BuiltinExchangeType.HEADERS, true);
                map = new HashMap<>();
                map.put("x-match","any");
                map.put("First","A");
                map.put("Second","B");
                channel.queueDeclare(CommonConstant.QUEUE_NAME_HEADER, true, false, false, null);
                channel.queueBind(CommonConstant.QUEUE_NAME_HEADER,CommonConstant.EXCHANGE_HEADER, CommonConstant.ROUTING_KEY ,map);
                rabbitMqConnectionPool.returnConnection(conn);
                channelPool.returnChannel(channel);
                Long end = System.currentTimeMillis();
                logger.info(" Process createExchangeAndQueue in HeaderExchange take {} miliSecond ", (end-start));

            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

}
