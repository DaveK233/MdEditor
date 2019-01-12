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
//    private HtmlConvertor mHtmlcvt = new HtmlConvertor();
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
    private JMenu mFileMenu, mCSSMenu, mClientMenu;
    private JMenuItem mOpenItem, mSaveItem, mExportHTMLItem, mExportDocItem, mExportPdfItem;
    private JMenuItem mLoginItem;
    private JMenuItem mEditCSSItem, mExternalCSSItem;
    private JMenu mMultiClientMenu;
    private JMenuItem mCreateRoomItem, mJoinRoomItem, mExitRoomItem;

    // directory column
    private DefaultTreeModel mTreeModel;
    private JTree mTree;
    private DefaultMutableTreeNode mTreeRoot;

    //客户端
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
        mTree.updateUI();
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
        mTree = new JTree(mTreeRoot);
        mTree.addTreeSelectionListener(this);
        mTreeModel = (DefaultTreeModel)mTree.getModel();
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

        {
            JScrollPane s1 = new JScrollPane(mTree);
            GridBagConstraints g1 = new GridBagConstraints();
            g1.gridx = 0;
            g1.gridy = 0;
            g1.weightx = 0;
            g1.weighty = 1;
            g1.ipadx = 150;
            g1.fill = GridBagConstraints.VERTICAL;
            mFrame.add(s1, g1);
        }

        {
            JScrollPane s2 = new JScrollPane(mTextArea);
            GridBagConstraints g2 = new GridBagConstraints();
            g2.gridx = 1;
            g2.gridy = 0;
            g2.weightx = 1;
            g2.weighty = 1;
            g2.ipadx = 250;
            g2.fill = GridBagConstraints.BOTH;
            mFrame.add(s2, g2);
        }

        {
            JScrollPane s3 = new JScrollPane(mEditorPane);
            GridBagConstraints g3 = new GridBagConstraints();
            g3.gridx = 2;
            g3.gridy = 0;
            g3.weightx = 1;
            g3.weighty = 1;
            g3.ipadx = 250;
            g3.fill = GridBagConstraints.BOTH;
            mFrame.add(s3, g3);
        }

        mFrame.setTitle("Markdown协同编辑器");
        mFrame.setSize(800, 600);

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

//            mExportPdfItem = new JMenuItem("导出pdf");
//            mExportPdfItem.setFont(mMenuFont);
//            mFileMenu.add(mExportPdfItem);
//            mExportPdfItem.addActionListener(this);

        // log in or register on server
        mClientMenu = new JMenu("登录/新建账户");
        mClientMenu.setFont(mMenuFont);
        mMenuBar.add(mClientMenu);

        mLoginItem = new JMenuItem("登录/新建账户");
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

        //打开markdown文件
        if(item == mOpenItem) {
            String tmp = Utility.getContentFromExternalFile();
            if(tmp != null) {
                mText = tmp;
                mTextArea.setText(mText);
            }
        }

        //保存
        else if(item == mSaveItem) {
            if(mChanged) {
                if(Utility.saveContent(mText, "md"))
                    mChanged = false;
            }
        }

        //导出HTML
        else if(item == mExportHTMLItem) {
            Utility.saveContent(Utility.getHTML(mHTML, mCSS), "html");
        }

//        //导出docx
//        else if(item == mExportDocItem) {
//            try {
//                mHtmlcvt.saveHtmlToDocx(Utility.getHTML(mHTML, mCSS));
//            } catch (Exception e) {
////				e.printStackTrace();
//                System.out.println("保存docx失败！");
//            }
//        }

        // export PDF
//        else if(item == mExportPdfItem) {
//	    	try {
//				mHtmlConverter.saveHtmlToPdf(Utility.getHTML(mHTML, mCSS));
//			} catch (Exception e) {
//				e.printStackTrace();
//				System.out.println("导出pdf失败！");
//			}
//        }

        //添加CSS
        else if(item == mEditCSSItem) {
            String css = JOptionPane.showInputDialog(null, "输入你要添加的CSS样式");
            mStyleSheet.addRule(css);
            mEditorPane.setText(mHTML);
            mCSS += css + "\n";
        }

        //导入外部CSS
        else if(item == mExternalCSSItem) {
            String rule = Utility.getContentFromExternalFile();
            if(rule != null) {
                mStyleSheet.addRule(rule);
                mEditorPane.setText(mHTML);
                mCSS += rule + "\n";
            }
        }

        //登录或注册
        else if(item == mLoginItem) {
            if(mClient != null) {
                Utility.info("你已经登录！");
            }
            else login();
        }

        //创建房间
        else if(item == mCreateRoomItem) {
            if(mClient.getID() != -1) {
                Utility.info("你已在协作状态！");
            }
            else newConnection();
        }

        //加入房间
        else if(item == mJoinRoomItem) {
            if(mClient.getID() != -1) {
                Utility.info("你已在协作状态！");
            }
            else joinConnection();
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
                    Utility.error("与服务器端连接出现错误！");
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
                Utility.info("可能发生连接中断！");
                return;
            }
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
            mMaster = false;
            SwingUtilities.invokeLater(() -> {
                mFrame.setTitle(mFrame.getTitle() + "(你已在协作白板： " + mClient.getID() + ")");
                mTextArea.setEditable(false);
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
                    setRoomMenu();
                });
            }

        }).start();
    }

    private void setRoomMenu() {
        mMultiClientMenu = new JMenu("协作");
        mMultiClientMenu.setFont(new Font("Microsoft YaHei", Font.PLAIN, 15));
        mMenuBar.add(mMultiClientMenu);

        mCreateRoomItem = new JMenuItem("创建白板");
        mCreateRoomItem.setFont(new Font("Microsoft YaHei", Font.PLAIN, 15));
        mMultiClientMenu.add(mCreateRoomItem);
        mCreateRoomItem.addActionListener(this);

        mJoinRoomItem = new JMenuItem("加入白板");
        mJoinRoomItem.setFont(new Font("Microsoft YaHei", Font.PLAIN, 15));
        mMultiClientMenu.add(mJoinRoomItem);
        mJoinRoomItem.addActionListener(this);

        mExitRoomItem = new JMenuItem("退出白板");
        mExitRoomItem.setFont(new Font("Microsoft YaHei", Font.PLAIN, 15));
        mMultiClientMenu.add(mExitRoomItem);
        mExitRoomItem.addActionListener(this);

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
                mTree.getLastSelectedPathComponent();//返回最后选定的节点

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
