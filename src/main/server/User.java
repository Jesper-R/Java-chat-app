package main.server;
import javax.swing.*;
import java.net.Socket;
import java.util.UUID;
public class User {
    private Socket socket;
    private String name;
    private boolean isAdmin;
    private boolean isNameSent = false;
    private UUID uuid;

    private JList<String> userList;
    public JList<String> getUserList(){
        return userList;
    }
    public User(Socket socket, String name, JList<String> userList) {
        this.socket = socket;
        this.name = name;
        this.userList = userList;
        this.uuid = UUID.randomUUID();
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public UUID getUuid() {
        return uuid;
    }

    public boolean isNameSent() {
        return isNameSent;
    }

    public void setNameSent(boolean nameSent) {
        this.isNameSent = nameSent;
    }
}