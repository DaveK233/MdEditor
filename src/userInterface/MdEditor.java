package userInterface;

import util.HtmlConvertor;
import util.Parser;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;

public class MdEditor extends MouseAdapter implements ActionListener, DocumentListener, TreeSelectionListener {
    private Parser mParser = new Parser();
    private HtmlConvertor mHtmlcvt = new HtmlConvertor();
    private JFrame mFrame;
    private JEditorPane mEditorPane;
    private JTextArea mTextArea;
    private String mText = "";
    private String mHTML = "";
    private String mCSS = "";

    public MdEditor() {

    }


    @Override
    public void actionPerformed(ActionEvent e) {

    }

    @Override
    public void insertUpdate(DocumentEvent e) {

    }

    @Override
    public void removeUpdate(DocumentEvent e) {

    }

    @Override
    public void changedUpdate(DocumentEvent e) {

    }

    @Override
    public void valueChanged(TreeSelectionEvent e) {

    }
}
