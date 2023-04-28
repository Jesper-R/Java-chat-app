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

    /**
     * @deprecated Use {@link #getUserByEitherNameOrUuid(String)} instead
     * @param name the name of the user to find
     * @return the user with the given name, or null if no user with that name exists
     */
    public static User getUserByName(String name){
        synchronized (users){
            for (User user : users) {
                if (user.getName().equalsIgnoreCase(name)){
                    return user;
                }
            }
        }
        return null;
    }

    public static User getUserByEitherNameOrUuid(String nameOrUuid){
        synchronized (users){
            for (User user : users) {
                if (user.getName().equalsIgnoreCase(nameOrUuid) || user.getUuid().toString().equals(nameOrUuid)){
                    return user;
                }
            }
        }
        return null;
    }


    /**
     * @deprecated Use {@link #getUserByEitherNameOrUuid(String)} instead
     * @param uuid the uuid of the user to find
     * @return the user with the given uuid, or null if no user with that uuid exists
     */
    public static User getUserByUuid(UUID uuid){
        synchronized (users){
            for (User user : users) {
                if (user.getUuid().toString().equals(uuid.toString())){
                    return user;
                }
            }
        }
        return null;
    }

    public synchronized static void deleteUser(User user) {
        try {
            user.getSocket().close();
            users.removeIf(users -> true);
        }
        catch (Exception e){
            //throw new UserNotFoundException();
        }
    }

    public static void kickAllUsers() {
        users.forEach(user -> {
            try {
                UserControl.sendMessageToUser(user, "You have been kicked from the server");
                user.getSocket().close();
                deleteUser(user);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
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

    @SuppressWarnings("unused")
    public synchronized static void sendMessageToEveryone(String message){
        UserControl.getUsers().forEach(user -> sendMessageToUser(user, message));
    }

    public static String getAllUsers() {
        System.out.println(users.stream()
                .map(User::getName)
                .map(s -> s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase())
                .collect(Collectors.joining(",")));
        return users.stream()
                .map(User::getName)
                .map(s -> s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase())
                .collect(Collectors.joining(","));
    }
}
