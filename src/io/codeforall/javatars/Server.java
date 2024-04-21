package io.codeforall.javatars;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    private int PORT = 9090;
    private ExecutorService cachedPool;
    private ArrayList<ServerHelper> clients;
    private Set<String> usernames;

    public Server() {
        this.cachedPool = Executors.newCachedThreadPool();
        this.clients = new ArrayList<>();
        this.usernames = new HashSet<>();
        connect();
    }

    public void connect() {

        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(PORT);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }


        while (true) {

            try {
                Socket clientSocket = serverSocket.accept();
                ServerHelper serverHelper = new ServerHelper(this, clientSocket);
                clients.add(serverHelper);
                cachedPool.submit(serverHelper);

            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    public ArrayList<ServerHelper> getClients() {
        return clients;
    }

    public void setClients(String username) {
        for (ServerHelper client : clients) {
            if (username.equals(client.getUsername())) {
                usernames.remove(client.getUsername());
                clients.remove(client);
            }
        }
    }

    public Set<String> getUsernames() {
        return usernames;
    }

    public void setUsernames(String username) {
        usernames.add(username);
    }

    public static void main(String[] args) {
        Server server1 = new Server();
    }

}
