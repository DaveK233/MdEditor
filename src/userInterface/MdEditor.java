package userInterface;

import sever.Server;
import userClient.Client;
import util.*;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MdEditor extends MouseAdapter implements ActionListener, DocumentListener, TreeSelectionListener {

    // define global varieties
    private Translator mTranslator = new Translator();
    private JFrame mFrame;
    private JEditorPane mEditorPane;
    private JTextArea mTextArea;
    private StyleSheet mStyleSheet;
    private boolean mChanged = true, mMaster;
    private Font mMenuFont = new Font("SimHei", Font.PLAIN, 15);
    private String mText = new String();
    private String mHTML = new String();
    private String mCSS = new String();
    private String mTreeRegrex = "<h(\\d)>(.*?)</h(\\d)>";

    // define menu bar
    private JMenuBar mMenuBar;
    private JMenu mFileMenu, mClientMenu;
    private JMenuItem mOpenItem, mSaveItem, mExportHTMLItem;
    private JMenuItem mLoginItem;
    private JMenu mMultiClientMenu;
    private JMenuItem mCreateBoardItem, mJoinBoardItem, mExitBoardItem;

    // directory column
    private DefaultTreeModel mTreeModel;
    private JTree mDir;
    private DefaultMutableTreeNode mTreeRoot;

    // Client
    private Client mClient = null;

    public static void main(String[] args) {
        MdEditor editor = new MdEditor();
        editor.showEditor();
        Server server = new Server();	// Run the server
    }

    public MdEditor() {
        initFrame();
        initMenu();
    }

    public void refreshEditor() {
        mChanged = true;
        mText = mTextArea.getText();

        try {
            mHTML = mTranslator.transMdToHtml(mText);
            mEditorPane.setText(mHTML);
        } catch(Exception e) {
            e.printStackTrace();
        }

        setDirTree();
        System.out.println("Editor Refreshed.");
    }

    public void setDirTree() {
        Pattern mPattern = Pattern.compile(mTreeRegrex, Pattern.CANON_EQ);
        Matcher mMatcher = mPattern.matcher(mHTML);

        mTreeRoot.removeAllChildren();
        while(mMatcher.find()) {
            int rank = mMatcher.group(1).charAt(0) - '0';
            String title = mMatcher.group(2);
            DefaultMutableTreeNode target = mTreeRoot;
            for(int i = 1; i < rank; i++) {
                target = (DefaultMutableTreeNode)target.getChildAt(target.getChildCount() - 1);
            }
            mTreeModel.insertNodeInto(new DefaultMutableTreeNode(title), target, target.getChildCount());
        }
        mDir.updateUI();
    }

    private void createTextArea() {
        mTextArea = new JTextArea();
        mTextArea.setLineWrap(true);
        mTextArea.addMouseListener(this);
        mTextArea.setFont(new Font("Microsoft YaHei", Font.PLAIN, 18));
        mTextArea.getDocument().addDocumentListener(this);
    }

    private void createTreeColumn() {
        mTreeRoot = new DefaultMutableTreeNode("目录");
        mDir = new JTree(mTreeRoot);
        mDir.addTreeSelectionListener(this);
        mTreeModel = (DefaultTreeModel) mDir.getModel();
    }

    private void createRenderingPane() {
        mEditorPane = new JEditorPane();
        mEditorPane.setContentType("text/html");
        mEditorPane.setEditable(false);

        HTMLEditorKit ed = new HTMLEditorKit();
        mEditorPane.setEditorKit(ed);

        mStyleSheet = ed.getStyleSheet();
        mStyleSheet.addRule("body {font-family:\"Microsoft YaHei\", Monaco}");
        mStyleSheet.addRule("p {font-size: 14px}");

        try {
            mHTML = mTranslator.transMdToHtml(mText);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void initFrame() {
        createTextArea();
        createRenderingPane();
        createTreeColumn();

        mFrame = new JFrame();
        mFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        mFrame.setLayout(new GridBagLayout());

        JScrollPane s1 = new JScrollPane(mDir);
        GridBagConstraints g1 = new GridBagConstraints(0,0,1,1,0,1, GridBagConstraints.NORTH, GridBagConstraints.VERTICAL, new Insets(0,0,0,0), 150, 100);
        mFrame.add(s1, g1);

        JScrollPane s2 = new JScrollPane(mTextArea);
        GridBagConstraints g2 = new GridBagConstraints(1,0,1,1,1,1,GridBagConstraints.NORTH, GridBagConstraints.BOTH, new Insets(0,0,0,0), 250, 100);
        mFrame.add(s2, g2);

        JScrollPane s3 = new JScrollPane(mEditorPane);
        GridBagConstraints g3 = new GridBagConstraints(2,0,1,1,1,1,GridBagConstraints.NORTH,GridBagConstraints.BOTH,new Insets(0,0,0,0), 250, 100);
        mFrame.add(s3, g3);

        mFrame.setTitle("Markdown协同编辑器（登陆后可进行协同操作）");
        mFrame.setSize(960, 640);

        mFrame.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                if(mClient != null) {
                    try {
                        mClient.sendRequest(FunType.DISCONNECT);
                    } catch (Exception e1) {
                        e1.printStackTrace();
                        Utility.error("关闭窗口失败！");
                    }
                }
            }
        });
    }

    public void initMenu() {
        mMenuBar = new JMenuBar();
        mFrame.setJMenuBar(mMenuBar);

        // file menu
        mFileMenu = new JMenu("文件");
        mFileMenu.setFont(mMenuFont);
        mMenuBar.add(mFileMenu);

        mOpenItem = new JMenuItem("打开");
        mOpenItem.setFont(mMenuFont);
        mFileMenu.add(mOpenItem);
        mOpenItem.addActionListener(this);

        mSaveItem = new JMenuItem("保存");
        mSaveItem.setFont(mMenuFont);
        mFileMenu.add(mSaveItem);
        mSaveItem.addActionListener(this);

        mExportHTMLItem = new JMenuItem("导出HTML");
        mExportHTMLItem.setFont(mMenuFont);
        mFileMenu.add(mExportHTMLItem);
        mExportHTMLItem.addActionListener(this);

        // log in or register on server
        mClientMenu = new JMenu("账户");
        mClientMenu.setFont(mMenuFont);
        mMenuBar.add(mClientMenu);

        mLoginItem = new JMenuItem("登录/注册");
        mLoginItem.setFont(mMenuFont);
        mClientMenu.add(mLoginItem);
        mLoginItem.addActionListener(this);
    }

    public void showEditor() {
        mFrame.setVisible(true);
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        Object item = e.getSource();

        if(item == mOpenItem) {
            String tmp = Utility.getContentFromExternalFile();
            if(tmp != null) {
                mText = tmp;
                mTextArea.setText(mText);
            }
        }

        else if(item == mSaveItem) {
            if(mChanged) {
                if(Utility.saveContent(mText, "md"))
                    mChanged = false;
            }
        }

        else if(item == mExportHTMLItem) {
            Utility.saveContent(Utility.getHTML(mHTML, mCSS), "html");
        }

        else if(item == mLoginItem) {
            if(mClient != null) {
                Utility.info("你已经登录！");
            }
            else login();
        }

        else if(item == mCreateBoardItem) {
            if(mClient.getID() != -1) {
                Utility.info("你已在协作状态！");
            }
            else newConnection();
        }

        else if(item == mJoinBoardItem) {
            if(mClient.getID() != -1) {
                Utility.info("你已在协作状态！");
            }
            else joinConnection();
        }

        else if(item == mExitBoardItem) {
            if(mClient.getID() != -1) {
                exitConnection();
            }
        }
    }

    private Thread storedThread = null;
    private void delayRefreshing() {
        new Thread(() -> {
            Thread last = storedThread;
            storedThread = Thread.currentThread();

            try {
                //阻塞上一个更新的线程
                if(last != null) {
                    last.interrupt();
                }
                Thread.sleep(1000);
            } catch(InterruptedException exc) {
                return;
            }

            if(Thread.currentThread().isInterrupted()) return;
            SwingUtilities.invokeLater(() -> {refreshEditor();});

            if(mMaster) {
                String updation = mTextArea.getText();
                try {
                    mClient.sendRequest(FunType.SEND_REFRESH, updation);
                } catch (Exception e) {
                    e.printStackTrace();
//                    Utility.error("与服务器端连接出现错误！");
                }
            }
        }).start();
    }

    private void monitor() {
        new Thread(() -> {
            try {
                while(true) {
                    String refreshInfo = mClient.toEditorMonitor();
                    SwingUtilities.invokeLater(() -> {
                        mTextArea.setText(refreshInfo);
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
//                Utility.info("可能发生连接中断！");
                return;
            }
        }).start();
    }

    private void exitConnection() {
        new Thread(() -> {
            try {
                mClient.sendRequest(FunType.DISCONNECT);
            } catch (Exception e) {
                e.printStackTrace();
                Utility.error(e.getMessage());
                return;
            }

//            Utility.info("创建白板成功!你已创建协作白板： " + mClient.getID());
            mMaster = false;
            mClient.setRoomID(-1);
            SwingUtilities.invokeLater(() -> {
                mFrame.setTitle("Markdown协同编辑器（登陆后可进行协同操作）");
            });

        }).start();
    }

    private void joinConnection() {
        new Thread(() -> {
            String idString = JOptionPane.showInputDialog("请输入你要加入的协作白板id：");

            try {
                mClient.sendRequest(FunType.JOIN, idString);
            } catch(Exception e) {
                e.printStackTrace();
                Utility.error(e.getMessage());
                return;
            }

            Utility.info("加入协作成功！你已加入协作白板： " + mClient.getID());
            mMaster = true;
            SwingUtilities.invokeLater(() -> {
                mFrame.setTitle(mFrame.getTitle() + "(你已在协作白板： " + mClient.getID() + ")");
                mTextArea.setEditable(true);
            });

            monitor();
        }).start();
    }

    private void newConnection() {
        new Thread(() -> {
            try {
                mClient.sendRequest(FunType.CREATE);
            } catch (Exception e) {
                e.printStackTrace();
                Utility.error(e.getMessage());
                return;
            }

            Utility.info("创建白板成功!你已创建协作白板： " + mClient.getID());
            mMaster = true;
            SwingUtilities.invokeLater(() -> {
                mFrame.setTitle(mFrame.getTitle() + "(你已在协作白板： " + mClient.getID() + ")");
            });

            monitor();
        }).start();
    }

    private void login() {
        new Thread(() -> {
            Login loginGUI = new Login(MdEditor.this, Thread.currentThread());
            loginGUI.setVisible(true);

            try {
                Thread.sleep(1000000000);
            } catch (Exception e) {
                mClient = loginGUI.getmClient();
                SwingUtilities.invokeLater(() -> {
                    mFrame.setTitle("欢迎： " + mClient.getName());
                    setBoardMenu();
                });
            }

        }).start();
    }

    private void setBoardMenu() {
        mMultiClientMenu = new JMenu("协作");
        mMultiClientMenu.setFont(new Font("Microsoft YaHei", Font.PLAIN, 15));
        mMenuBar.add(mMultiClientMenu);

        mCreateBoardItem = new JMenuItem("创建白板");
        mCreateBoardItem.setFont(new Font("Microsoft YaHei", Font.PLAIN, 15));
        mMultiClientMenu.add(mCreateBoardItem);
        mCreateBoardItem.addActionListener(this);

        mJoinBoardItem = new JMenuItem("加入白板");
        mJoinBoardItem.setFont(new Font("Microsoft YaHei", Font.PLAIN, 15));
        mMultiClientMenu.add(mJoinBoardItem);
        mJoinBoardItem.addActionListener(this);

        mExitBoardItem = new JMenuItem("退出白板");
        mExitBoardItem.setFont(new Font("Microsoft YaHei", Font.PLAIN, 15));
        mMultiClientMenu.add(mExitBoardItem);
        mExitBoardItem.addActionListener(this);

        mFrame.repaint();
        mFrame.revalidate();
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        delayRefreshing();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        delayRefreshing();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {

    }

    @Override
    public void mouseClicked(MouseEvent e) {
        Object item = e.getSource();

        if(item == mTextArea) {
            int position = mTextArea.getCaretPosition();
        }
    }

    @Override
    public void valueChanged(TreeSelectionEvent e) {
        DefaultMutableTreeNode selectedNode=(DefaultMutableTreeNode)
                mDir.getLastSelectedPathComponent();//返回最后选定的节点

        String title = selectedNode.toString();
        int level = selectedNode.getLevel();
        System.out.println(level);
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < level; i++)
            sb.append("&");
        sb.append(title);

        int pos = mText.indexOf(sb.toString());
        mTextArea.setSelectionStart(pos);
        mTextArea.setSelectionEnd(pos);
    }
}
