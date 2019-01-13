package userClient;

import util.FunType;
import util.Onlined;
import util.Utility;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {

    // network settings args
    private static final String SERVER_IP = "127.0.0.1";    // server ip
    private static final int SERVER_PORT = 8081;    // server port
    private static final int CLIENT_PORT = 8082;   // client port
    private static final int CONNECT_TIMEOUT = 20000;	// connection wait time: 20s

    // socket settings
    private int clientID = -1;
    private String virtualIP;   // virtual IP for client
    private Socket socket;
    private Socket socketRefresh;
    private String name;

    // stream to invoke socket
    private InputStream mStreamReader = null;
    private OutputStream mStreamWriter = null;
    private PrintWriter mPrintWriter = null;
    private BufferedReader mBufferedReader = null;

    // stream to refresh content
    private InputStream mStreamReaderRefresh = null;
    private OutputStream mStreamWriterRefresh = null;
    private PrintWriter mPrintWriterRefresh = null;
    private BufferedReader mBufferedReaderRefresh = null;

    public Client() {
        virtualIP = null;
        socket = new Socket();
//        socket = null;
    }

    /*
    methods to set and get vars
    because of the private type of vars
    */
    public void setSocket(Socket socket) throws IOException {
        this.socket = socket;
        mStreamReader = socket.getInputStream();
        mStreamWriter = socket.getOutputStream();

        OutputStreamWriter output = new OutputStreamWriter(mStreamWriter);
        BufferedWriter bw = new BufferedWriter(output);
        mPrintWriter = new PrintWriter(bw, true);

        InputStreamReader input = new InputStreamReader(mStreamReader);
        mBufferedReader = new BufferedReader(input);
    }

    public void setSocketRefresh(Socket socket) throws IOException {
        this.socketRefresh = socket;
        mStreamReaderRefresh = socket.getInputStream();
        mStreamWriterRefresh = socket.getOutputStream();

        OutputStreamWriter output = new OutputStreamWriter(mStreamWriterRefresh);
        BufferedWriter bw = new BufferedWriter(output);
        mPrintWriterRefresh = new PrintWriter(bw, true);

        InputStreamReader input = new InputStreamReader(mStreamReaderRefresh);
        mBufferedReaderRefresh = new BufferedReader(input);
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setIP(String IP) {
        this.virtualIP = IP;
    }

    public void setRoomID(int ID) {
        this.clientID = ID;
    }

    public Socket getSocket() {
        return socket;
    }

    public Socket getSocketRefresh() {
        return socketRefresh;
    }

    public String getName() {
        return name;
    }

    public int getID() {
        return clientID;
    }

    public String getIP() {
        return virtualIP;
    }

    // method to send requests to server in certain format
    public void sendRequest(FunType fun, String ... args) throws Exception {

        if (fun == FunType.LOGIN) {
            sendLogin(fun, args[0], args[1]);
        } else if (fun == FunType.REGISTER) {
            sendLogin(fun, args[0], args[1]);
        } else if (fun == FunType.DISCONNECT) {
            sendDisconnect(fun);
        } else if (fun == FunType.CREATE) {
            sendCreate(fun);
        } else if (fun == FunType.JOIN) {
            sendJoin(fun, args[0]);
        } else if (fun == FunType.SEND_REFRESH) {
            sendRefresh(fun, args[0]);
        }
    }

    public InputStream getStreamReader() {
        return mStreamReader;
    }

    public OutputStream getStreamWriter() {
        return mStreamWriter;
    }

    public InputStream getStreamReaderRefresh() {
        return mStreamReaderRefresh;
    }

    public OutputStream getStreamWriterRefresh() {
        return mStreamWriterRefresh;
    }

    public PrintWriter getPrintWriter() {
        return mPrintWriter;
    }
    public BufferedReader getBufferedReader() {
        return mBufferedReader;
    }

    public PrintWriter getPrintWriterRefresh() {
        return mPrintWriterRefresh;
    }
    public BufferedReader getBufferedReaderRefresh() {
        return mBufferedReaderRefresh;
    }

    public void connectServer() throws UnknownHostException, IOException {
        socket = new Socket();
        socket.connect(new InetSocketAddress(SERVER_IP, SERVER_PORT), CONNECT_TIMEOUT);

        mStreamReader = socket.getInputStream();
        mStreamWriter = socket.getOutputStream();

        OutputStreamWriter output = new OutputStreamWriter(mStreamWriter);
        BufferedWriter bw = new BufferedWriter(output);
        mPrintWriter = new PrintWriter(bw, true);

        InputStreamReader input = new InputStreamReader(mStreamReader);
        mBufferedReader = new BufferedReader(input);
    }

    public void connectServerRefresh() throws UnknownHostException, IOException {
        socketRefresh = new Socket();
        socketRefresh.connect(new InetSocketAddress(SERVER_IP, CLIENT_PORT), CONNECT_TIMEOUT);

        mStreamReaderRefresh = socketRefresh.getInputStream();
        mStreamWriterRefresh = socketRefresh.getOutputStream();

        OutputStreamWriter output = new OutputStreamWriter(mStreamWriterRefresh);
        BufferedWriter bw = new BufferedWriter(output);
        mPrintWriterRefresh = new PrintWriter(bw, true);

        InputStreamReader input = new InputStreamReader(mStreamReaderRefresh);
        mBufferedReaderRefresh = new BufferedReader(input);
    }



    private void sendRefresh(FunType fun, String refresh) throws Exception {
        String request = fun + "&" + clientID + "\n" + refresh + "\n" + Utility.END_MARK;
        mPrintWriterRefresh.println(request);
    }

    private void sendJoin(FunType fun, String id) throws Exception {
        String request = fun + "&" + id;
        mPrintWriter.println(request);

        String respond = mBufferedReader.readLine();
        String[] s = respond.split("&");

        if(s[0].equals(Onlined.SUCCESS.toString())) {
            this.clientID = Integer.parseInt(id);
        }
        else throw new Exception(respond);

        try {
            connectServerRefresh();

            request = FunType.CONNECT + "&" + name;
            mPrintWriterRefresh.println(request);

            respond = mBufferedReaderRefresh.readLine();
            s = respond.split("&");

            if(s[0].equals(Onlined.SUCCESS.toString())) {
                this.clientID = Integer.parseInt(id);
            }
            else throw new Exception();
        } catch(Exception e) {
            e.printStackTrace();
            throw new Exception("客户端未能连接到服务器！");
        }
    }

    private void sendCreate(FunType fun) throws Exception {
        String request = fun + "&";
        mPrintWriter.println(request);

        String respond = mBufferedReader.readLine();
        String[] s = respond.split("&");

        if(!s[0].equals(Onlined.SUCCESS.toString()))
            throw new Exception(respond);

        int id = Integer.parseInt(s[1]);

        try {
            connectServerRefresh();
            request = FunType.CONNECT + "&" + name;
            mPrintWriterRefresh.println(request);

            respond = mBufferedReaderRefresh.readLine();
            s = respond.split("&");

            if(s[0].equals(Onlined.SUCCESS.toString())) {
                this.clientID = id;
            }
            else throw new Exception();
        } catch(Exception e) {
            e.printStackTrace();
            throw new Exception("可能出现服务器连接错误！\n（若为打开多个程序，请忽略此消息）");
        }
    }

    private void sendDisconnect(FunType fun) throws IOException {

    }

    //
    private void sendLogin(FunType fun, String name, String password) throws Exception {

        String request = fun + "&" + name + "&" + password;
        mPrintWriter.println(request);

        String respond = mBufferedReader.readLine();
        String[] s = respond.split("&");

        if(s[0].equals(Onlined.SUCCESS.toString())) {
            System.out.println("Success.");
            this.name = name;
            this.virtualIP = s[1];
        }
        else throw new Exception(respond);
    }

    public String toEditorMonitor() throws Exception {
        return Utility.parseFullString(mBufferedReaderRefresh);
    }
}
