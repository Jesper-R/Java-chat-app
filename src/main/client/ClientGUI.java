package main.client;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;
import java.util.stream.Collectors;

public class ClientGUI extends javax.swing.JFrame {
    private String HOST = "127.0.0.1";
    private boolean sentName = false;
    private int PORT = 9000;
    private String messages = "";
    private Socket serverSocket;
    private boolean connected = false;
    DefaultListModel<String> listModel = new DefaultListModel<>();

    private StringBuilder items;

    public JList<String> getUserList() {
        return userList;
    }
    private void connectToServer() {
        try {
            System.out.println("test");
            serverSocket = new Socket("127.0.0.1", 9000);
            System.out.println("Connected to server");
            connected = true;
            textArea.setText("Connected to server\n");
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
        // Check for messages from server
        try {
            InputStream inputStream = socket.getInputStream();
            int data;
            ArrayList<Character> messageArr = new ArrayList<>();
            while (inputStream.available() > 0) {
                data = inputStream.read();
                messageArr.add((char) data);
            }
            if (messageArr.size() > 0) {
                // From a ArrayList of characters to a String
                String encodedMessage = messageArr.stream()
                        .map(Objects::toString)
                        .collect(Collectors.joining());

                String message = new String(Base64.getDecoder().decode(encodedMessage));
                if (message.equals("You have been kicked from the server")) {
                    System.out.println("You have been kicked from the server");
                    textArea.setText("You have been kicked from the server");
                    //button.setText("Connect");
                    sentName = false;
                    serverSocket.close();
                    return;
                }
                if (message.contains(":")) {
                    String[] messageSplitted = message.split(": ");
                    messageSplitted[0] = "" + messageSplitted[0] + "" + ": ";
                    message = String.join("", messageSplitted);

                }
                else {
                    if(message.contains("[USERS]")){
                        //UPDATE USER LIST
                    }
                }
                messages += message + " \n";
                textArea.setText(messages);

                //chatFieldTextArea.setText(message + "\n"); //append
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

    private void sendNameToServer(String name, Socket socket) {
        // Send message to server
        try {
            String encodedMessage = Base64.getEncoder().encodeToString(name.getBytes());
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
            //button.setText("Connect");
            sentName = false;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendMessageAndLogIt() {
        if (!connected) {
            changeServerDetails();
            return;
        }
        String message = sendMessage.getText();
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

        if (!sentName) {
            String name = JOptionPane.showInputDialog("Enter name");
            sendNameToServer(name, serverSocket);
            this.setTitle(name);
            sentName = true;
        }
        messages += "du: " + message + "\n";
        textArea.setText(messages);
        //chatFieldTextArea.setText("du: " + message + "\n"); //append
        sendMessageToServer(message, serverSocket);
        /*for (int i = 0; i<listModel.size(); i++) {

        }*/
        //sendMessageToServer("SELECTED ITEMS: " + items,serverSocket);
        sendMessage.setText("");


    }

    public ClientGUI() {
        initComponents();
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

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        textArea.setColumns(20);
        textArea.setRows(5);
        jScrollPane1.setViewportView(textArea);

        connectBtn.setText("Connect");
        connectBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                connectBtnActionPerformed(evt);
                connectToServer();
            }
        });

        connectedUsersLabel.setText("Connected Users");
        userList.setModel(listModel);
        listModel.addElement("Item 1");
        listModel.addElement("Item 2");
        listModel.addElement("Item 3");
        listModel.addElement("Item 4");
        listModel.addElement("Item 5");
        /*
        userList.setModel(new javax.swing.AbstractListModel<String>() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public String getElementAt(int i) { return strings[i]; }
        });*/
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
                        .addComponent(sendBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 446, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(connectedUsersList)
                        .addGap(16, 16, 16))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 36, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(connectedUsersLabel)
                                .addGap(31, 31, 31))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(connectBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(34, 34, 34))))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(connectedUsersLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(connectedUsersList, javax.swing.GroupLayout.PREFERRED_SIZE, 282, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 304, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(sendBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(connectBtn)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents


    private void connectBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_connectBtnActionPerformed
        // TODO add your handling code here:
    }   //GEN-LAST:event_connectBtnActionPerformed

    private void sendBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sendBtnActionPerformed
        // TODO add your handling code here:
        if (sendBtn.getText().equalsIgnoreCase("connect")) {
            changeServerDetails();
            connectToServer();
        } else {
            sendMessageAndLogIt();
        }

    }//GEN-LAST:event_sendBtnActionPerformed


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
    private javax.swing.JButton connectBtn;
    private javax.swing.JLabel connectedUsersLabel;
    private javax.swing.JScrollPane connectedUsersList;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JButton sendBtn;
    private javax.swing.JTextArea sendMessage;
    private javax.swing.JTextArea textArea;
    private javax.swing.JList<String> userList;
    // End of variables declaration//GEN-END:variables
}
