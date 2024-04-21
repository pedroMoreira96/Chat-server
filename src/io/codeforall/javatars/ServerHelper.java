package io.codeforall.javatars;

import java.io.*;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

public class ServerHelper implements Runnable {

    private Server server;
    private Socket clientsocket;
    private BufferedReader in;
    private PrintWriter out;
    private String username;
    private Set<ServerHelper> blockedUsers;

    public ServerHelper(Server server, Socket clientsocket) {
        this.server = server;
        this.clientsocket = clientsocket;
        this.blockedUsers = new HashSet<>();

        try {
            this.in = new BufferedReader(new InputStreamReader(clientsocket.getInputStream()));
            this.out = new PrintWriter(clientsocket.getOutputStream(), true);
            out.flush();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void run() {

        try {
            startingMessages();

            while (true) {
                String input = in.readLine();

                if (input.equals("help") || input.equals("Help")) {
                    help();
                    continue;
                }
                if ((input.split(" ")[0].equals("whisper")) || (input.split(" ")[0].equals("Whisper"))) {
                    whisper(input);
                    continue;
                }

                if ((input.split(" ")[0].equals("block")) || (input.split(" ")[0].equals("Block"))) {
                    block(input);
                    continue;
                }
                if ((input.split(" ")[0].equals("unblock")) || (input.split(" ")[0].equals("Unblock"))) {
                    unBlock(input);
                    continue;
                }

                if ((input.split(" ")[0].equals("Users")) || (input.split(" ")[0].equals("users"))) {
                    out.println(loggedUsers());
                    continue;
                }

                if (input.equals("exit") || input.equals("Exit")) {
                    exit();
                    break;
                }

                broadcast(username + ": " + input);

            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private void whisper(String message) {


        String whisperUsername = "\u001b[32m" + message.split(" ")[1] + "\u001b[0m";
        this.out.println("You entered a private chat with " + whisperUsername);

        ServerHelper whisperUser = null;

        for (ServerHelper client : server.getClients()) {

            if (whisperUsername.equals(client.username)) {
                whisperUser = client;
            }
        }

        while (true) {

            try {
                String input = in.readLine();

                if (input.equals("help") || input.equals("Help")) {
                    help();
                    continue;
                }

                if (input.equals("exit whisper") || input.equals("Exit whisper")) {
                    break;
                }

                whisperUser.out.println("Private message from " + username + ": " + input);

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void block(String message) {
        String blockedUsername = "\u001b[32m" + message.split(" ")[1] + "\u001b[0m";

        for (ServerHelper client : server.getClients()) {
            if (blockedUsername.equals(client.username)) {
                client.blockedUsers.add(this);
                client.out.println(this.username + " blocked you. You can no longer send messages to this user.");
                blockedUsers.add(client);
                out.println("You blocked " + client + ". You can no longer send messages to this user.");
                break;
            }
        }
    }

    private void unBlock(String message) {
        String unBlockedUsername = "\u001b[32m" + message.split(" ")[1] + "\u001b[0m";

        for (ServerHelper client : blockedUsers) {
            if (unBlockedUsername.equals(client.username)) {
                client.blockedUsers.remove(this);
                client.out.println(this.username + " unblocked you. You can now send messages to this user.");
                blockedUsers.remove(client);
                out.println("You unblocked " + client + ". You send messages to this user.");
                break;
            }
        }
    }

    private void broadcast(String message) {

        for (ServerHelper client : server.getClients()) {
            if (client == this || blockedUsers.contains(client)) {
                continue;
            }
            client.out.println(message);
            client.out.flush();
        }
    }

    private String askUsername() throws IOException {

        out.println("Input username: ");
        int size = server.getUsernames().size();
        username = "\u001b[32m" + in.readLine() + "\u001b[0m";
        server.setUsernames(username);

        while (server.getUsernames().size() != size+1){
            out.println("Username already used.");
            askUsername();
        }
        return username;
    }

    private void startingMessages() throws IOException {

        username = askUsername();

        out.println("Welcome " + username + ". Type \u001b[32m'Help'\u001b[0m if you need to know the controls.");

        if (server.getClients().size() > 1) {
            out.println(loggedUsers());
        }

        broadcast(username + " has entered the chat.");
    }

    private void help() {

        out.println("\u001b[32m\nControls:\nHelp - controls\nWhisper [username] - private chat\n" +
                "Exit whisper - exit private chat\nBlock [username] - block user\nUnblock [username] - unblock user\n" +
                "Users - list of online users\nExit - exit chat\n\u001b[0m");
    }

    private void exit() {

        try {

            in.close();
            out.close();
            clientsocket.close();
            broadcast(username + " has left the chat.");
            server.setClients(username);

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private String loggedUsers() {
        String users = "Logged in users: ";
        for (ServerHelper client : server.getClients()) {
            if (!(client.username.equals(this.username))) {
                users += client.username + " | ";
            }
        }
        return users;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public String toString() {
        return username;
    }

}

