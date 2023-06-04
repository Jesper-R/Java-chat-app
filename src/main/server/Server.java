package main.server;
import main.client.ClientGUI;

import javax.swing.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.stream.Collectors;
public class Server{
    private boolean done = false;
    private int PORT = 4192;

    public static void main(String[] args) {
        // Starts server
        Server server = new Server();
        server.startServer();
    }

    private void startServer() {
        done = false;
        try (ServerSocket serverSocket = new ServerSocket(PORT)){
            // Start a thread to check for messages from clients
            new Thread (() -> {
                while (!done) {
                    try {
                        checkForMessages();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

            // Create a server user
            User serverUser = new User(null, null);
            serverUser.setAdmin(true);

            // Wait for clients to connect and handle connections
            System.out.println("Waiting for clients to connect");
            while (!done) {
                UserControl.addUser(new User(serverSocket.accept(), null));
                System.out.println("Client connected from " + UserControl.getUsers()
                        .get(UserControl.getUsers().size() - 1).getSocket().getInetAddress());
                UserControl.sendMessageToEveryone("New user connected, say hi!");
            }
        } catch (Exception e) {
            // Exits if an exception is thrown
            done = true;
        }
    }

    private synchronized void checkForMessages() throws IOException {
        // Check for messages from clients
        List<User> users = UserControl.getUsers();
        for (User user : users){
            try {
                // Gets message from user
                InputStream inputStream = user.getSocket().getInputStream();
                int data;
                ArrayList<Character> messageArr = new ArrayList<>();
                // Reads message into messageArr
                while (inputStream.available() > 0) {
                    data = inputStream.read();
                    messageArr.add((char) data);
                }
                // If there is a message look at it
                if (messageArr.size() > 0) {
                    // Decodes message
                    String encodedMessage = messageArr.stream()
                            .map(Objects::toString)
                            .collect(Collectors.joining());
                    String message = new String(Base64.getDecoder().decode(encodedMessage));

                    //First message from user is going to be name. This takes it and puts it in usercontrol then updates all userslists
                    if (!user.isNameSent()){
                        user.setName(message);
                        user.setNameSent(true);
                        UserControl.updateUsers();
                        UserControl.updateUserLists();
                        return;
                    }

                    // Command to get all users
                    if (message.startsWith("/users")){
                        UserControl.updateUsers();
                        UserControl.updateUserLists();
                        UserControl.sendMessageToUser(user, UserControl.getAllUsers());
                        return;
                    }

                    // Command to disconnect (kind of useless but some people might prefer this way)
                    if (message.startsWith("/dc")) {
                        user.getSocket().close();
                        UserControl.updateUsers();
                        UserControl.updateUserLists();
                        System.out.println(user.getName() + " disconnected");
                        UserControl.sendMessageToEveryone(user.getName() + " disconnected");
                        return;
                    }
                    System.out.println(user.getName() + ": " + message);

                    // Sends message to everyone except for the user who wrote it
                    UserControl.getUsers().forEach(client -> {
                        if (client.getSocket() != user.getSocket()){
                            UserControl.sendMessageToUser(client, user.getName() + ": " + message);
                        }
                    });
                }
            } catch (IOException e) {
                System.out.println("User " + user.getName() + " disconnected suddenly");
            } catch (Exception e) {}
        }
    }
}