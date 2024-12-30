package tukano.impl;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import java.util.logging.Logger;
import static java.lang.String.format;

public class RabbitMQ {
    private static Logger Log = Logger.getLogger(RabbitMQ.class.getName());

    // RabbitMQ configurations
    private static final String RABBITMQ_HOST = "rabbitmq";
    private static final int RABBITMQ_PORT = 5672;
    private static final String RABBITMQ_USERNAME = "username";
    private static final String RABBITMQ_PASSWORD = "password";
    private static final String QUEUE_NAME = "user.events";

    RabbitMQ() {}

    public void publishUserDeletedEvent(String userId) {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(RABBITMQ_HOST);
        factory.setPort(RABBITMQ_PORT);
        factory.setUsername(RABBITMQ_USERNAME);
        factory.setPassword(RABBITMQ_PASSWORD);

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            // Declare a queue (idempotent operation, won't re-declare if it exists)
            String queueName = QUEUE_NAME;
            channel.queueDeclare(queueName, true, false, false, null);

            // Create the event message
            String message = format("{\"event\": \"user.deleted\", \"userId\": \"%s\"}", userId);

            // Publish the message to the queue
            channel.basicPublish("", queueName, null, message.getBytes());
            Log.info(() -> format("Published user.deleted event for userId: %s\n", userId));
        } catch (Exception e) {
            Log.severe(() -> format("Failed to publish user.deleted event for userId: %s. Error: %s\n", userId, e.getMessage()));
            e.printStackTrace();
        }
    }
}
