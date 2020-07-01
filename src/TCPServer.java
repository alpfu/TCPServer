import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.*;
import java.util.*;

/**
 *
 * @author urkmez, af 17.11.2018 Last Edited: 27.11.2018 by urkmez, af
 *
 */

public class TCPServer {

    private static Hashtable<String, DataOutputStream> broadcastList;
    private final static int PORT = 7896;

    public static void main(String args[]) throws IOException {

        ServerSocket serverSocket = new ServerSocket(PORT);

        System.out.println("listening for clients at " + InetAddress.getLocalHost() + " on port " + PORT + "...");
        broadcastList = new Hashtable<String, DataOutputStream>();

        while (true) { // listening the clients
            try {
                ServerProcess s = new ServerProcess(serverSocket.accept());
                new Thread(s).start();
            } catch (IOException e) {
                System.out.println(e.getMessage());
                serverSocket.close();
            }
        }

    } // end method main

    private static class ServerProcess implements Runnable {

        private Socket clientSocket;
        private DataInputStream rcvMsg;
        private DataOutputStream sndMsg;
        private String nickname;
        private String msg;
        public DateTimeFormatter dtf;
        public LocalDateTime now;
        public static boolean doesExist;

        public ServerProcess(Socket clientSocket) {

            dtf = DateTimeFormatter.ofPattern("HH:mm");
            now = LocalDateTime.now();
            this.clientSocket = clientSocket;
            try {
                rcvMsg = new DataInputStream(this.clientSocket.getInputStream());
                sndMsg = new DataOutputStream(this.clientSocket.getOutputStream());
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }

        @Override
        public void run() {
            boolean doesExist;
            try {
                do {
                    doesExist = false;

                    sndMsg.writeUTF("write your nickname");
                    nickname = rcvMsg.readUTF();

                    if (broadcastList.contains(nickname)) {
                        System.out.println("nickname is being used, choose another !");
                        doesExist = true;
                    }

                } while (doesExist);

                if (nickname.equals("/quit"))
                    this.clientSocket.close();
                else {
                    broadcastList.put(nickname, sndMsg);
                    broadcast(dtf.format(now) + " " + nickname + " has connected the chat !");
                }

            } catch (IOException e) {
                System.out.println(e.getMessage());
            }

            while (true) {
                try {
                    msg = rcvMsg.readUTF();
                    if (msg.equals("/quit")) {
                        broadcastList.remove(nickname, sndMsg);
                        this.clientSocket.close();
                        broadcast(dtf.format(now) + " " + nickname + " has left the chat !");
                        break;
                    }

                    broadcast(dtf.format(now) + " " + nickname + " --> " + msg);

                } catch (IOException e) { // if socket closed or no longer exist
                    break;
                }
            }
        } // end method run()

        void broadcast(String msg) {
            broadcastList.forEach((key, value) -> {
                try {
                    value.writeUTF(msg);
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            });
        }

    } // end class ServerProcess
} // end class TCPServer