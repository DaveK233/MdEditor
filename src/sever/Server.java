package sever;

import userClient.Board;
import userClient.Client;

import java.net.ServerSocket;
import java.util.HashMap;

public class Server {

    private static final int SERVER_PORT = 3872;
    private static final int ROOM_SERVER_PORT = 8080;
    private static final int MAX_ROOM_NUMBER = 20;
    private int Count = 1000;   // number of virtual IP

    private ServerSocket mServerLocal;
    private ServerSocket mServerOther;

    private HashMap<String, Client> mClients = new HashMap<>();

    private HashMap<String, String> mPasswords = new HashMap<>();

    private HashMap<Integer, Board> mBoards = new HashMap<>();

    public Server() {
        init();
    }

    private void init() {

    }
}
