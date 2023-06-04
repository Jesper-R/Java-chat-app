package main.client;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;
import java.util.stream.Collectors;

public class ClientGUI extends javax.swing.JFrame {
    private String HOST = "127.0.0.1";
    private boolean sentName = false;
    private int PORT = 4192;
    private String messages = "";
    private Socket serverSocket;
    private boolean connected = false;
    private String name;
    DefaultListModel<String> listModel = new DefaultListModel<>();
    private void connectToServer() {
        try {
            // Adds shutdown hook so upon exit the server will be notified
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                public void run() {
                    System.out.println("In shutdown hook");
                    if (connected) {
                        sendMessageToServer("/dc", serverSocket);
                        sendMessageToServer("Someone just fucking disconnected", serverSocket);
                        connected = false;
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    System.out.println("After shutdown hook");
                    sendMessageToServer("/dc", serverSocket);
                    disconnectFromServer();
                }
            }, "Shutdown-thread"));
            System.out.println("hook created");

            serverSocket = new Socket(HOST, PORT);
            connected = true;
            connectBtn.setEnabled(false);
            advanceConnectBtn.setEnabled(false);
            disconnectBtn.setEnabled(true);
            sendBtn.setEnabled(true);

            new Thread(() -> {
                while (true){
                    checkForMessages(serverSocket);
                }
            }).start();

        } catch (IOException e) {
            System.out.println("Failed to connect to server");
            System.out.println("Error: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private void checkForMessages(Socket socket) {
        // Check for messages
        try {
            // Gets message
            InputStream inputStream = socket.getInputStream();
            boolean addMessage = true;
            int data;
            ArrayList<Character> messageArr = new ArrayList<>();
            // Reads message into array
            while (inputStream.available() > 0) {
                data = inputStream.read();
                messageArr.add((char) data);
            }

            if (messageArr.size() > 0) {
                // Decodes message
                String encodedMessage = messageArr.stream()
                        .map(Objects::toString)
                        .collect(Collectors.joining());
                String message = new String(Base64.getDecoder().decode(encodedMessage));

                if (message.contains("[USERS]")) {
                    // Gets user update from server and updates the list
                    System.out.println("GOT USERS FROM SERVER");
                    System.out.println(message);
                    String[] users = message.split(";");
                    // Removes the first part of the array which is the command
                    users = Arrays.copyOfRange(users, 1, users.length);
                    listModel.clear();
                    for (String user : users) {
                        listModel.addElement(user);
                    }
                    message = "";
                    addMessage = false;
                }
                // Adds message to chat if it is supposed to
                if (addMessage) {messages += message + " \n";}
                textArea.setText(messages);
            }
        } catch (IOException e) {
            System.out.println("Closed connection to server");
            connected = false;
        }
    }

    private void sendMessageToServer(String message, Socket socket) {
        // Send message to server
        try {
            String encodedMessage = Base64.getEncoder().encodeToString(message.getBytes());
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.print(encodedMessage);
            out.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void changeServerDetails() {
        String host = JOptionPane.showInputDialog("Enter host");
        String port = JOptionPane.showInputDialog("Enter port");
        if (host != null && !host.isEmpty()) {
            HOST = host;
        }
        if (port != null && !port.isEmpty()) {
            try {
                PORT = Integer.parseInt(port);
            } catch (NumberFormatException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void disconnectFromServer() {
        try {
            serverSocket.close();
            connected = false;
            sentName = false;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendMessage() {
        if (!connected) {
            changeServerDetails();
            return;
        }
        String message = sendMessage.getText();

        // If statements to catch commands and execute them

        if (message.equalsIgnoreCase("/dc")){
            sendMessageToServer("/dc", serverSocket);
            textArea.setText("Disconnected from server");
            disconnectFromServer();
            return;
        }

        if(message.equalsIgnoreCase("/clear")){
            textArea.setText("");
            messages = "";
            sendMessage.setText("");
            return;
        }

        if (message.split(" ")[0].equalsIgnoreCase("/rename") && message.split(" ").length == 2) {
            this.setTitle(message.split(" ")[1]);
        }

        // Updates GUI and sends message to server
        messages += "You: " + message + "\n";
        textArea.setText(messages);
        sendMessageToServer(message, serverSocket);
        sendMessage.setText("");
    }

    public void setName() {
        if (sentName) {return;}
        if (!sentName) {
            name = JOptionPane.showInputDialog("Enter name");
            // Formats name
            name = name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
            this.setTitle(name);
            sentName = true;
        }
    }

    public ClientGUI() {
        initComponents();
        sendBtn.setEnabled(false);
        disconnectBtn.setEnabled(false);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        textArea = new javax.swing.JTextArea();
        connectBtn = new javax.swing.JButton();
        connectedUsersLabel = new javax.swing.JLabel();
        connectedUsersList = new javax.swing.JScrollPane();
        userList = new javax.swing.JList<>();
        sendBtn = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        sendMessage = new javax.swing.JTextArea();
        // Sets up the enter key to send messages
        sendMessage.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    e.consume();
                    System.out.println("ENTER PRESSED");
                    sendMessage();
                }
            }
        });
        advanceConnectBtn = new javax.swing.JButton();
        disconnectBtn = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);

        textArea.setEditable(false);
        textArea.setColumns(20);
        textArea.setLineWrap(true);
        textArea.setRows(5);
        textArea.setWrapStyleWord(true);
        textArea.setFocusable(false);
        textArea.setRequestFocusEnabled(false);
        jScrollPane1.setViewportView(textArea);

        connectBtn.setText("Connect");
        connectBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                connectBtnActionPerformed(evt);
            }
        });

        connectedUsersLabel.setText("Connected Users");
        userList.setModel(listModel);
        listModel.addElement("Not connected");
        connectedUsersList.setViewportView(userList);

        sendBtn.setText("Send");
        sendBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sendBtnActionPerformed(evt);
            }
        });

        sendMessage.setColumns(20);
        sendMessage.setLineWrap(true);
        sendMessage.setRows(5);
        sendMessage.setWrapStyleWord(true);
        jScrollPane2.setViewportView(sendMessage);

        advanceConnectBtn.setText("Advanced Connect");
        advanceConnectBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                advanceConnectBtnActionPerformed(evt);
            }
        });

        disconnectBtn.setText("Disconnect");
        disconnectBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                disconnectBtnActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 317, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(sendBtn, javax.swing.GroupLayout.DEFAULT_SIZE, 118, Short.MAX_VALUE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 446, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(connectedUsersLabel)
                        .addGap(35, 35, 35))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                .addComponent(advanceConnectBtn))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(connectedUsersList)
                                    .addComponent(connectBtn, javax.swing.GroupLayout.DEFAULT_SIZE, 131, Short.MAX_VALUE)))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                .addComponent(disconnectBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap(12, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(disconnectBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(connectedUsersLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(connectedUsersList))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 304, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(sendBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(connectBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(advanceConnectBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents


    private void connectBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_connectBtnActionPerformed
        try {
            setName();
            connectToServer();
            sendMessageToServer(name, serverSocket);
        } catch (Exception ex) {
            textArea.setText("Error connecting to server, make sure its online!");
        }
    }//GEN-LAST:event_connectBtnActionPerformed

    private void sendBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sendBtnActionPerformed
        if(sendMessage.getText().equals("")) {
            return;
        } else {
            sendMessage();
        }
    }//GEN-LAST:event_sendBtnActionPerformed

    private void advanceConnectBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_advanceConnectBtnActionPerformed
        try {
            setName();
            changeServerDetails();
            connectToServer();
            sendMessageToServer(name, serverSocket);
        } catch(Exception e) {
            textArea.setText("Error connecting to server. Make sure its on and that the server is on the right port");
        }
    }//GEN-LAST:event_advanceConnectBtnActionPerformed

    private void restart() {
        ClientGUI c2 = new ClientGUI();
        c2.setVisible(true);
        this.dispose();
    }

    private void disconnectBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_disconnectBtnActionPerformed
        sendMessageToServer("/dc", serverSocket);
        connected = false;
        disconnectFromServer();
        restart();
    }//GEN-LAST:event_disconnectBtnActionPerformed

    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(ClientGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ClientGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ClientGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ClientGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new ClientGUI().setVisible(true);
            }
        });

    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton advanceConnectBtn;
    private javax.swing.JButton connectBtn;
    private javax.swing.JLabel connectedUsersLabel;
    private javax.swing.JScrollPane connectedUsersList;
    private javax.swing.JButton disconnectBtn;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JButton sendBtn;
    private javax.swing.JTextArea sendMessage;
    private javax.swing.JTextArea textArea;
    private javax.swing.JList<String> userList;
    // End of variables declaration//GEN-END:variables
}
