package main.server;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Base64;
import java.util.stream.Collectors;

public class UserControl{
    private static final List<User> users = new CopyOnWriteArrayList<>();

    public static List<User> getUsers() {
        synchronized (users){
            return users;
        }
    }

    public static void addUser(User user){
        synchronized (users){
            users.add(user);
        }
    }

    public static void updateUsers() {
        synchronized (users){
            users.forEach(user -> {//System.out.println("usersocket" + user.getSocket().isClosed());
                if (user.getSocket().isClosed()){
                    users.remove(user);
                }
            });
        }
        System.out.println("Users: " + users.stream().map(User::getName).collect(Collectors.toList()));
    }

    // Not used, but could be useful
    public static User getUserByNameOrUuid(String nameOrUuid){
        synchronized (users){
            for (User user : users) {
                if (user.getName().equalsIgnoreCase(nameOrUuid) || user.getUuid().toString().equals(nameOrUuid)){
                    return user;
                }
            }
        }
        return null;
    }

    public synchronized static void sendMessageToUser(User user, String message) {
        PrintWriter out;
        if (user == null) {
            System.out.println(message);
            return;
        }

        try {
            if (user.getSocket() == null) {
                System.out.println(message);
                return;
            }
            out = new PrintWriter(user.getSocket().getOutputStream(), true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String encodedMessage = Base64.getEncoder().encodeToString(message.getBytes());
        out.print(encodedMessage);
        out.flush();
    }

    public synchronized static void sendMessageToEveryone(String message){
        UserControl.getUsers().forEach(user -> sendMessageToUser(user, message));
    }

    public static String getAllUsers() {
        System.out.println(users.stream()
                .map(User::getName)
                .map(s -> s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase())
                .collect(Collectors.joining(";")));
        return users.stream()
                .map(User::getName)
                .map(s -> s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase())
                .collect(Collectors.joining(";"));
    }

    public synchronized static void updateUserLists() {
        UserControl.updateUsers();
        UserControl.sendMessageToEveryone("[USERS];" + UserControl.getAllUsers());
    }
}