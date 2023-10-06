package vn.vnpay.rabbitmq.factory;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import vn.vnpay.rabbitmq.common.ObjectConverter;

import java.io.IOException;

public class ConsumerHandle<T> extends DefaultConsumer {
    T data;
    Channel channel;

    public ConsumerHandle(Channel channel) {
        super(channel);
        this.channel= channel;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }


    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {

        try {
            data = (T) ObjectConverter.bytesToObject(body, data.getClass());

            channel.basicAck(envelope.getDeliveryTag(), false);
        } catch (Exception e) {
            channel.basicReject(envelope.getDeliveryTag(), false);
        }
    }

}
