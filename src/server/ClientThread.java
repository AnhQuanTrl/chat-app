package server;


import java.io.*;
import java.net.Socket;

public class ClientThread extends Thread {
    private Socket socket;
    private ChatServer server;
    private PrintWriter writer;
    private ClientThread connectedUser;

    public ClientThread getConnectedUser() {
        return connectedUser;
    }

    public String getUserName() {
        return userName;
    }

    private String userName;

    public ClientThread(Socket socket, ChatServer server) {
        this.socket = socket;
        this.server = server;
        connectedUser = null;
        userName = "";
    }

    @Override
    public void run() {
        try {
            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));

            OutputStream output = socket.getOutputStream();
            writer = new PrintWriter(output, true);
            printUsers();

            while (true) {
                String newUserName = reader.readLine();
                if (server.getUserNames().contains(newUserName)) {
                    writer.println("Username already registered");
                } else {
                    server.addUserName(newUserName);
                    userName = newUserName;
                    break;
                }
            }

            String serverMessage = "New user connected: " + userName;
            server.broadcast(serverMessage, this);

            String command;
            boolean exit = false;
            while (!exit) {
                command = reader.readLine();
                String[] args = command.split("\\s+");
                switch (args[0]) {
                    case "/CONNECT":
                        if (args.length < 2) {
                            writer.println("USAGE: /CONNECT <username>");
                        } else {
                            connectUser(args[1]);
                        }
                        break;
                    case "/MESSAGE":
                        if (args.length < 2) {
                            writer.println("USAGE: /MESSAGE <message>");
                        } else {
                            messageOther(args[1]);
                        }
                        break;
                    case "/DISCONNECT":
                        disconnect();
                        break;
                    case "/EXIT":
                        exit = true;
                    default:
                        writer.println("ERROR: Illegal Command");
                }
            }

            server.removeUser(this);
            socket.close();

            serverMessage = userName + " has quitted.";
            server.broadcast(serverMessage, this);

        } catch (IOException ex) {
            System.out.println("Error in UserThread: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    void printUsers() {
        if (server.hasUsers()) {
            writer.println("Connected users: " + server.getUserNames());
        } else {
            writer.println("No other users connected");
        }
    }

    void sendMessage(String message) {
        writer.println(message);
    }
    void connectUser(String other) {
        ClientThread user;
        if ((user = server.findThread(other)) == null) {
            writer.println("ERROR: User not found");
        } else {
            connectedUser = user;
        }
    }

    void messageOther(String mess) {
        if (connectedUser == null) {
            writer.println("ERROR: No connected user");
            return;
        }
        connectedUser.sendMessage("[" + userName + "]" +  mess);
    }

    void disconnect() {
        connectedUser = null;
    }
}
