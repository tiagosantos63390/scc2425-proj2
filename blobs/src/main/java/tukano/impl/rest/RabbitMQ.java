package main.java.tukano.impl.rest;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import java.util.logging.Logger;
import static java.lang.String.format;

public class RabbitMQ {
    private static Logger Log = Logger.getLogger(RabbitMQ.class.getName());
    public RabbitMQ(){}

    private void listenForUserDeletedEvents() {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("rabbitmq");
        factory.setPort(5672);
        factory.setUsername("username");
        factory.setPassword("password");

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            String queueName = "user.events";
            channel.queueDeclare(queueName, true, false, false, null);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
