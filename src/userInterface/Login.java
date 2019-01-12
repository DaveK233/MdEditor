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
    private Font mFont = new Font("Microsoft YaHei", Font.PLAIN, 15);
    JLabel password = new JLabel("密码：");
    JTextField JName = new JTextField(10);
    JPasswordField JPassword = new JPasswordField(10);

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
        JName.setFont(mFont);
        JPassword.setFont(mFont);

        JPanel mPanel = new JPanel();
        GridLayout mLayout = new GridLayout(3,2);
        mPanel.setLayout(mLayout);

        name.setHorizontalAlignment(SwingConstants.RIGHT);
        password.setHorizontalAlignment(SwingConstants.RIGHT);

        mPanel.add(name);
        mPanel.add(JName);
        mPanel.add(password);
        mPanel.add(JPassword);
        mPanel.add(login);
        mPanel.add(register);

        login.addActionListener(this);
        register.addActionListener(this);

        this.add(mPanel,BorderLayout.CENTER);   // put the register/login dialog center

        this.setTitle("登录");
        this.pack();
        this.setLocation(500,300);
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
        String name = JName.getText();
        String password = new String(JPassword.getPassword());

        //用户名或密码格式有误，不能包含 #
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
        String name = JName.getText();
        String password = new String(JPassword.getPassword());

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
