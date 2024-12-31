package main.java.tukano.impl.rest;

import com.rabbitmq.client.ConnectionFactory;

import main.java.tukano.impl.JavaBlobs;
import main.java.tukano.impl.Token;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import java.util.logging.Logger;
import static java.lang.String.format;

import java.nio.charset.StandardCharsets;

public class RabbitMQ {
    private static Logger Log = Logger.getLogger(RabbitMQ.class.getName());

    public RabbitMQ(){}

    public boolean verifyUserPassword(String username, String password) {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("rabbitmq");
        factory.setPort(5672);
        factory.setUsername("username");
        factory.setPassword("password");

        try {
            Connection connection = factory.newConnection(); // Open connection
            Channel channel = connection.createChannel(); // Open channel
    
            channel.queueDeclare("user.verify", true, false, false, null);
            channel.queueDeclare("user.verify.response", true, false, false, null);
    
            String message = "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}";
            channel.basicPublish("", "user.verify", null, message.getBytes(StandardCharsets.UTF_8));
            Log.info("Message sent to user.verify queue: " + message);
    
            boolean[] result = new boolean[1]; // Array to store result
            channel.basicConsume("user.verify.response", true, (tag, delivery) -> {
                String response = new String(delivery.getBody());
                Log.info("Response received from user.verify.response queue: " + response);
                result[0] = Boolean.parseBoolean(response);
            }, tag -> {});
    
            Thread.sleep(1000); // Wait for response
            return result[0];
    
        } catch (Exception e) {
            Log.severe("Error during RabbitMQ interaction: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Consumer Delete Blob from Tukano
    public void consumeDeleteBlob() {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("rabbitmq");
        factory.setPort(5672);
        factory.setUsername("username");
        factory.setPassword("password");
    
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            channel.queueDeclare(QUEUE_DELETE_BLOB, true, false, false, null);
    
            channel.basicConsume(QUEUE_DELETE_BLOB, true, (tag, delivery) -> {
                String message = new String(delivery.getBody());
                Log.info(() -> "Received deleteBlob message: " + message);
    
                JSONObject json = new JSONObject(message);
                String blobUrl = json.getString("blobUrl");
                String token = json.getString("token");

                JavaBlobs.getInstance().delete(BlobUrl, token );

            }, consumerTag -> {});
        } catch (Exception e) {
            Log.severe(() -> "Error in consumeDeleteBlob: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Consumer Delete All Blobs from Tukano
    public void consumeDeleteAllBlobs() {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("rabbitmq");
        factory.setPort(5672);
        factory.setUsername("username");
        factory.setPassword("password");
    
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            channel.queueDeclare(QUEUE_DELETE_ALL_BLOBS, true, false, false, null);
    
            channel.basicConsume(QUEUE_DELETE_ALL_BLOBS, true, (tag, delivery) -> {
                String message = new String(delivery.getBody());
                Log.info(() -> "Received deleteAllBlobs message: " + message);
    
                JSONObject json = new JSONObject(message);
                String userId = json.getString("userId");
                String token = json.getString("token");

                JavaBlobs.getInstance().deleteAllBlobs(userId, token);

            }, consumerTag -> {});
        } catch (Exception e) {
            Log.severe(() -> "Error in consumeDeleteAllBlobs: " + e.getMessage());
            e.printStackTrace();
        }
    }





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
