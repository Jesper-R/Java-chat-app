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

    public static void main(String[] args) {
        new Server().startServer();
    }

    private void startServer() {
        done = false;
        // Make this on a separate thread
        try (ServerSocket serverSocket = new ServerSocket(9000)){
            new Thread (() -> {
                while (!done) {
                    try {
                        checkForMessages();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

            User serverUser = new User(null, null, null);
            serverUser.setAdmin(true);

            // Wait for client to connect
            System.out.println("Waiting for clients to connect");
            while (!done) {
                UserControl.addUser(new User(serverSocket.accept(), null, null));
                System.out.println("Client connected from " + UserControl.getUsers()
                        .get(UserControl.getUsers().size() - 1).getSocket().getInetAddress());
                //UserControl.sendMessageToEveryone("[USERS]: ");
            }
        } catch (Exception e) {
            //logger.fatal("Error: " + e.getMessage());
            done = true;
        }
    }

    private void sendMessageToClient(String sendingUser, String message, Socket currentClientSocket) {
        UserControl.getUsers().forEach(user -> {
            if (user.getSocket() != currentClientSocket) {
                UserControl.sendMessageToUser(user, sendingUser + ": " + message);
            }
        });
    }

    private synchronized void checkForMessages() throws IOException {
        // Check for messages from clients
        List<User> users = UserControl.getUsers();
        for (User user : users){
            //user.getUserList().add();
            try {
                InputStream inputStream = user.getSocket().getInputStream();
                int data;
                ArrayList<Character> messageArr = new ArrayList<>();
                while (inputStream.available() > 0) {
                    data = inputStream.read();
                    messageArr.add((char) data);
                }
                if (messageArr.size() > 0) {
                    String encodedMessage = messageArr.stream()
                            .map(Objects::toString)
                            .collect(Collectors.joining());
                    String message = new String(Base64.getDecoder().decode(encodedMessage));

                    //if the User hasn't sent their name
                    if (!user.isNameSent()){
                        if (message.contains(" ")) {
                            user.setName(message.split(" ")[0]);
                        } else {
                            user.setName(message);
                        }
                        user.setNameSent(true);
                        UserControl.updateUsers();
                        UserControl.sendMessageToEveryone("[USERS];" + UserControl.getAllUsers());
                        // TODO: add to disconnect aswell
                        return;
                    }

                    // Get the first character of the message
                    if (message.startsWith("/users")){
                        UserControl.updateUsers();
                        UserControl.sendMessageToEveryone("[USERS];" + UserControl.getAllUsers());
                        // Remove the first character
                        /*message = message.substring(1);
                        Command command = CommandFactory.getCommand(message, user);
                        command.execute();
                        logger.info(user.getName() + " executed " + command.getCommand());*/
                        return;
                    }
                    if (message.startsWith("/dc")) {
                        user.getSocket().close();
                        UserControl.updateUsers();
                        UserControl.sendMessageToEveryone("[USERS];" + UserControl.getAllUsers());
                        System.out.println(user.getName() + " disconnected");
                        return;
                    }
                    //logger.info(user.getName() + ": " + message);
                    System.out.println(user.getName() + ": " + message);
                    //UserControl.sendMessageToUser(user, user.getName() + ": " + message);
                    UserControl.getUsers().forEach(client -> {
                        if (client.getSocket() != user.getSocket()){
                            UserControl.sendMessageToUser(client, user.getName() + ": " + message);
                        }
                    });
                    //sendMessageToClient(user.getName(), message, user.getSocket());
                    //UserControl.sendMessageToEveryone("reeeeeeee");
                }
            } catch (IOException e) {
                //logger.warn("User " + user.getName() + " has probably disconnected");
                System.out.println("User " + user.getName() + " has probably disconnected");
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            } catch (Exception e) {}
        }
    }


    /*
    private ArrayList<ConnectionHandler> connections;
    private ServerSocket server;
    private boolean done;
    private ExecutorService pool;

    public Server(){
        connections = new ArrayList<>();
        done = false;
    }
    String nickname;
    public void setNick(String nick) {
            this.nickname = nick;
    }
    @Override
    public void run() {
        try {
            server = new ServerSocket(9000);
            pool = Executors.newCachedThreadPool();
            while (!done){
                Socket client = server.accept();
                ConnectionHandler handler = new ConnectionHandler(client, nickname);
                connections.add(handler);
                pool.execute(handler);
            }

        } catch (Exception e) {
            shutdown();
        }
    }
    
    public void broadcast(String message) {
        for (ConnectionHandler ch: connections) {
            if (ch != null){
                
                ch.sendMessage(message);
                //ch.out.println("s");
                
            }
        }
    }

    public void shutdown() {
        try {
            done = true;
            pool.shutdown();
            if (!server.isClosed()) {
                server.close();
            }
            for (ConnectionHandler ch : connections) {
                ch.shutdown();
            }
        } catch (IOException e) {
            // ignore
        }

    }
    class ConnectionHandler implements Runnable{

        private Socket client;
        private BufferedReader in;
        private PrintWriter out;
        private String nickname;

        public ConnectionHandler(Socket client, String nickname) {
            this.client = client;
            this.nickname = nickname;
        }
        

        @Override
        public void run(){
            try {
                out = new PrintWriter(client.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                //out.println("Enter nickname: ");
                //nickname = in.readLine();
                // can and handling with if statements
                //System.out.println(nickname + " connected!");
                System.out.println(nickname + " connected!");
                broadcast(nickname + " joined the chat!");
                String message;
                while ((message = in.readLine()) != null){
                    if (message.startsWith("/nick ")) {
                        String[] messageSplit = message.split(" ", 2);
                        if (messageSplit.length == 2) {
                            broadcast(nickname + " renamed themselves to " + messageSplit[1]);
                            System.out.println(nickname + " renamed themselves to " + messageSplit[1]);
                            nickname = messageSplit[1];
                            out.println("Successfully changed nickname to " + nickname);
                        } else {
                            out.println("No nickname provided");
                        }
                    } else if (message.startsWith("/quit")){
                        broadcast(nickname + " left the chat");
                        System.out.println(nickname + " disconnected");
                        shutdown();
                    } else {
                        broadcast(nickname + ": " + message);
                        out.println(message);
                    }
                }
            } catch (IOException e){
                shutdown();
            }
        }

        public void sendMessage(String message) {
            System.out.println(message);
            //return message;
            //jTextArea1.setText(message);
        }

        public void shutdown() {
            try {
                in.close();
                out.close();
                if (!client.isClosed()) {
                    client.close();
                }
            } catch (IOException e) {
                //ignore
            }
        }
    }
    */

}
