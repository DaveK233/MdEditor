package sever;

import userClient.Board;
import userClient.Client;
import util.FunType;
import util.Onlined;
import util.Utility;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class Server {

    private static final int SERVER_PORT = 8081;
    private static final int ROOM_SERVER_PORT = 8082;
    private static final int MAX_ROOM_NUMBER = 20;
    private int Count = 1000;   // number of virtual IP

    private ServerSocket mServerLocal = null;
    private ServerSocket mServerOther = null;

    private HashMap<String, Client> mClients = new HashMap<>();

    private HashMap<String, String> mPasswords = new HashMap<>();

    private HashMap<Integer, Board> mBoards = new HashMap<>();

    public class InvokeThread extends Thread {

        private Client mClient;
        private Socket mSocket;
        private InputStream mStreamReader;
        private OutputStream mStreamWriter;
        private PrintWriter mPrintWriter;
        private BufferedReader mBufferedReader;

        public InvokeThread(Client client, int choose) throws IOException {
            mClient = client;
            mSocket = (choose == 1) ? client.getSocket() : client.getSocketRefresh();

            mStreamReader = mSocket.getInputStream();
            mStreamWriter = mSocket.getOutputStream();

            OutputStreamWriter output = new OutputStreamWriter(mStreamWriter);
            BufferedWriter bw = new BufferedWriter(output);
            mPrintWriter = new PrintWriter(bw, true);

            InputStreamReader input = new InputStreamReader(mStreamReader);
            mBufferedReader = new BufferedReader(input);
        }

        public void run() {
            try {
                String requestFromClient;
                boolean exit = false;

                while(!exit) {
                    requestFromClient = mBufferedReader.readLine();

                    String[] s = requestFromClient.split("&");
                    FunType type = FunType.valueOf(s[0]);

                    if (type == FunType.DISCONNECT) {
                        exit = true;
                    } else if (type == FunType.LOGIN) {
                        sendLogin(s[1], s[2]);
                    } else if (type == FunType.REGISTER) {
                        sendRegister(s[1], s[2]);
                    } else if (type == FunType.CREATE) {
                        sendCreateRoom();
                    } else if (type == FunType.JOIN) {
                        sendJoinRoom(s[1]);
                    } else if (type == FunType.CONNECT) {
                        sendConnectRoom(s[1], mSocket);
                    } else if (type == FunType.SEND_REFRESH) {
                        sendUpdate(Utility.parseFullString(mBufferedReader),
                                Integer.parseInt(s[1]));
                    }
                }

                mClients.remove(mClient);
                mPrintWriter.close();
                mBufferedReader.close();
                mSocket.close();
            }
            catch (Exception e) {
                e.printStackTrace();
                Utility.error("连接错误或网络超时！");
                return;
            }
        }

        private void sendLogin(String name, String password) throws Exception {

            String pwd = mPasswords.get(name);
            if(pwd == null) {
                mPrintWriter.println("用户不存在！");
            }
            else if(pwd.equals(password)) {

                int ip = Count++;
                mPrintWriter.println(Onlined.SUCCESS + "&" + ip);
                mClient.setIP(Integer.toString(ip));
                mClient.setName(name);

                mClients.put(name, mClient);
            }
            //密码错误
            else {
                mPrintWriter.println("密码错误！");
            }
        }
        
        private void sendRegister(String name, String password) throws Exception {
            if(mPasswords.get(name) != null) {
                mPrintWriter.println("用户已存在！");
            }
            else {
                mPasswords.put(name, password);

                int ip = Count++;
                mPrintWriter.println(Onlined.SUCCESS + "&" + ip);
                mClient.setIP(Integer.toString(ip));
                mClient.setName(name);

                mClients.put(name, mClient);
            }
        }
        
        private void sendCreateRoom() throws Exception {
            if(mBoards.size() == MAX_ROOM_NUMBER) {
                mPrintWriter.println("服务器已达容量上限！");
            }
            else {
                int id = 0;
                for( ; id < MAX_ROOM_NUMBER; id++) {
                    if(mBoards.get(id) == null)
                        break;
                }

                Board board = new Board(id);
                board.setHost(mClient);
                mClient.setRoomID(id);
                mBoards.put(id, board);

                mPrintWriter.println(Onlined.SUCCESS + "&" + id);
            }
        }
        
        private void sendJoinRoom(String idString) throws Exception {
            int id;
            try {
                id = Integer.parseInt(idString);
            } catch(NumberFormatException e) {
                mPrintWriter.println("房间id必须是0~99的数字！");
                return;
            }
            Board board = mBoards.get(id);
            if(board == null) {
                mPrintWriter.println("您要加入的房间不存在！");
            }
            else {
                board.add(mClient);
                mClient.setRoomID(id);

                mPrintWriter.println(Onlined.SUCCESS + "&");
            }
        }


        synchronized private void sendConnectRoom(String name, Socket socket) throws Exception {
            Client client = mClients.get(name);
            client.setSocketRefresh(socket);

            client.getPrintWriterRefresh().println(Onlined.SUCCESS + "&");
        }
        
        private void sendUpdate(String updation, int boardID) throws Exception {
            Board board = mBoards.get(boardID);

            board.updateAllMember(updation);
        }
    }

    public Server() {
        init();
    }

    private void init() {
        new Thread(() -> {
            try {
                mServerLocal = new ServerSocket(SERVER_PORT);

            } catch (IOException e) {
                e.printStackTrace();
//                Utility.error("协同服务器创建失败！");
            }

            try {
                while(true) {
                    Socket socket = mServerLocal.accept();
                    Client client = new Client();
                    client.setSocket(socket);
                    // new thread
                    new InvokeThread(client, 1).start();
                }
            } catch(Exception e) {
                e.printStackTrace();
//                Utility.error("网络错误：连接超时！");
            }
        }).start();

        new Thread(() -> {
            try {
                mServerOther = new ServerSocket(ROOM_SERVER_PORT);

            } catch(IOException e) {
                e.printStackTrace();
//                Utility.error("协作白板创建失败！");
            }

            try {
                while(true) {
                    Socket socket = mServerOther.accept();
                    Client client = new Client();
                    client.setSocketRefresh(socket);
                    // new thread
                    new InvokeThread(client, 2).start();
                }
            } catch(Exception e) {
                e.printStackTrace();
//                Utility.error("网络错误：连接超时！");
            }
        }).start();
    }
}
