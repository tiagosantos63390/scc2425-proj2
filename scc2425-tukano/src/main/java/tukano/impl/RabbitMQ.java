package tukano.impl;

import com.rabbitmq.client.ConnectionFactory;

import tukano.api.Result;
import tukano.api.User;

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

    RabbitMQ() {}

    public boolean deleteBlob(String blobUrl, String token) {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("rabbitmq");
            factory.setPort(5672);
            factory.setUsername("username");
            factory.setPassword("password");
    
            Connection conn = factory.newConnection();
            Channel chan = conn.createChannel();
    
            chan.queueDeclare("blob.delete", true, false, false, null);
    
            String msg = "{\"blobUrl\":\"" + blobUrl + "\",\"token\":\"" + token + "\"}";
            chan.basicPublish("", "blob.delete", null, msg.getBytes());
            System.out.println("Message sent: " + msg);
    
            chan.close();
            conn.close();
            return true;
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return false;
        }
    }
    
    public boolean deleteAllBlobs(String userId, String token) {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("rabbitmq");
            factory.setPort(5672);
            factory.setUsername("username");
            factory.setPassword("password");
    
            Connection conn = factory.newConnection();
            Channel chan = conn.createChannel();
    
            chan.queueDeclare("blob.deleteAll", true, false, false, null);
    
            String msg = "{\"userId\":\"" + userId + "\",\"token\":\"" + token + "\"}";
            chan.basicPublish("", "blob.deleteAll", null, msg.getBytes());
            System.out.println("Message sent: " + msg);
    
            chan.close();
            conn.close();
            return true;
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return false;
        }
    }


    // Consumer Verify Password from Blobs
    public void consumeVerifyPassword() {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(RABBITMQ_HOST);
        factory.setPort(RABBITMQ_PORT);
        factory.setUsername(RABBITMQ_USERNAME);
        factory.setPassword(RABBITMQ_PASSWORD);
    
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            channel.queueDeclare("user.verify", true, false, false, null);
            channel.queueDeclare("user.verify", true, false, false, null);
    
            channel.basicConsume("user.verify", true, (tag, delivery) -> {
                String request = new String(delivery.getBody());
                Log.info(() -> "Received verifyPassword request: " + request);
    
                JSONObject json = new JSONObject(request);
                String username = json.getString("username");
                String password = json.getString("password");

                Result<User> userResult = JavaUsers.getInstance().getUser(username, password);
                
                boolean isPasswordValid = userResult.isOK();
    
                String response = Boolean.toString(isPasswordValid);

                channel.basicPublish("", "user.verify", null, response.getBytes());
                Log.info(() -> "Sent verifyPassword response: " + response);
            }, consumerTag -> {});
        } catch (Exception e) {
            Log.severe(() -> "Error in consumeVerifyPassword: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
