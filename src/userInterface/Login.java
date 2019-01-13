package userInterface;

import userClient.Client;
import util.FunType;
import util.Onlined;
import util.Utility;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Login extends JFrame implements ActionListener {

    JButton login = new JButton("登录");
    JButton register = new JButton("注册");
    JLabel  name = new JLabel("用户名：");
    private Font mFont = new Font("SimHei", Font.PLAIN, 15);
    JLabel password = new JLabel("密码：");
    JTextField nameIn = new JTextField(10);
    JPasswordField passwordIn = new JPasswordField(10);

    private Client mClient = null;
    private MdEditor mEditor;
    private Thread mCallThread;

    public Login(MdEditor context, Thread thread) {
        init();
        mEditor = context;
        mCallThread = thread;
    }

    private void init() {
        login.setFont(mFont);
        register.setFont(mFont);
        name.setFont(mFont);
        password.setFont(mFont);
        nameIn.setFont(mFont);
        passwordIn.setFont(mFont);

        JPanel inputPanel = new JPanel();
        JPanel buttonPanel = new JPanel();

        inputPanel.setLayout(new GridLayout(2,2));
        inputPanel.add(name);
        inputPanel.add(nameIn);
        inputPanel.add(password);
        inputPanel.add(passwordIn);
        login.addActionListener(this);
        register.addActionListener(this);
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));

        buttonPanel.add(login);
        buttonPanel.add(register);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));

        setLayout(new BorderLayout());
        getContentPane().add(inputPanel, BorderLayout.NORTH);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        setTitle("用户");
        setLocationRelativeTo(inputPanel);
        setPreferredSize(new Dimension(210, 160));
        this.setLocation(500, 300);
        pack();
    }

    public Client getmClient() {
        return mClient;
    }

    private boolean validStringCheck(String name) {
        if(name.equals("") || name.equals("")
                || name.contains("*") || name.contains("*")) {
            return false;
        }

        return true;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        //登录
        if(source == register) {
            try {
                //登录成功，就唤醒主界面线程
                if( attemptRegister() ) {
                    mCallThread.interrupt();
                    setVisible(false);

                    Utility.info("注册成功！");
                };
            } catch(Exception exc) {
                Utility.error(exc.getMessage());
            }
        }
        //登录
        else {
            try {
                //登录成功，就唤醒主界面线程
                if( attemptLogin() ) {
                    mCallThread.interrupt();
                    setVisible(false);

                    Utility.info("登录成功！");
                };
            } catch(Exception exc) {
                Utility.error(exc.getMessage());
            }
        }
    }

    private boolean attemptLogin() throws Exception {
        String name = nameIn.getText();
        String password = new String(passwordIn.getPassword());

        //用户名或密码格式有误，不能包含 &
        if(!validStringCheck(name) || !validStringCheck(password)) {
            throw new Exception(Onlined.INVALID_VALUE.toString());
        }

        //试图连接服务器
        mClient = new Client();

        try {
            mClient.connectServer();
        } catch(Exception e) {
            e.printStackTrace();
            throw new Exception("连接服务器失败！");
        }

        mClient.sendRequest(FunType.LOGIN, name, password);

        return true;
    }

    private boolean attemptRegister() throws Exception {
        String name = nameIn.getText();
        String password = new String(passwordIn.getPassword());

        if(!validStringCheck(name) || !validStringCheck(password)) {
            throw new Exception(Onlined.INVALID_VALUE.toString());
        }

        mClient = new Client();

        try {
            mClient.connectServer();
        } catch(Exception e) {
            e.printStackTrace();
            throw new Exception("连接服务器失败！");
        }

        String request = FunType.REGISTER.ordinal() + "&" + name + password;
        mClient.sendRequest(FunType.REGISTER, name, password);

        return true;
    }
}
