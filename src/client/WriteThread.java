package client;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;

import java.io.*;
import java.net.Socket;

public class WriteThread extends Thread {
    private PrintWriter writer;
    private Socket socket;
    private ChatClient client;

    public WriteThread(Socket socket, ChatClient client) {
        this.socket = socket;
        this.client = client;

        try {
            OutputStream output = socket.getOutputStream();
            writer = new PrintWriter(output, true);
        } catch (IOException ex) {
            System.out.println("Error getting output stream: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    @Override
    public void run() {

        try {
            System.out.println("\nEnter your name: ");
            BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
            String userName = bufferRead.readLine();
            client.setUserName(userName);
            writer.println(userName);

            String text;

            do {
                System.out.println("[" + userName + "]: ");
                text = bufferRead.readLine();
                writer.println(text);

            } while (!text.equals("bye"));
        } catch(IOException ex) {
            ex.printStackTrace();
        }

        try {
            socket.close();
        } catch (IOException ex) {

            System.out.println("Error writing to server: " + ex.getMessage());
        }
    }
}
