package util;

import javax.swing.*;
import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utility {

    public static final String END_MARK = "$$END$$";

    private Utility() {

    }

    public static void error(String error) {
        JOptionPane.showMessageDialog(null, error, "错误！", JOptionPane.ERROR_MESSAGE);
    }

    public static String getContentFromExternalFile() {
        JFileChooser jf = new JFileChooser();
        jf.showOpenDialog(new JLabel());
        File file = jf.getSelectedFile();
        if(file == null) return null;
        String text = null;

        try(BufferedReader in = new BufferedReader(
                new InputStreamReader(
                        new FileInputStream(file), "utf-8"))) {
            StringBuilder sb = new StringBuilder();
            String tmp;
            while((tmp=in.readLine()) != null) {
                sb.append(tmp);
                sb.append("\n");
            }

            text = sb.toString();
        } catch (IOException e) {
            System.out.println("Read File Error!");
        };
        return text;
    }

    public static void info(String info) {
        JOptionPane.showMessageDialog(null, info, "提示",JOptionPane.INFORMATION_MESSAGE);
    }

    public static String parseFullString(BufferedReader br) throws Exception {
        String str;
        StringBuilder builder = new StringBuilder();
        while(!(str = br.readLine()).equals(END_MARK)) {
            builder.append(str + "\n");
            System.out.println("Read String result： " + str);
        }
        str = builder.toString();
        if(str.length() == 1) return "";
        else return str.substring(0, str.length() - 1);
    }

    public static String getHTML(String html, String css) {
        Pattern pattern = Pattern.compile("(<style>[\\s\\S]*)</style>");
        Matcher matcher = pattern.matcher(html);
        StringBuffer sb = new StringBuffer();
        while(matcher.find()) {
            System.out.println("matcher_find: yes");
            matcher.appendReplacement(sb, matcher.group(1) +"\n" + css + "</style>");
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    public static boolean saveContent(String textContent, String post) {
        JFileChooser jf = new JFileChooser();
        jf.setSelectedFile(new File("./未命名." + post));
        int option = jf.showSaveDialog(new JLabel());

        if(option == JFileChooser.CANCEL_OPTION)
            return false;

        File file = jf.getSelectedFile();

        try( PrintWriter writer = new PrintWriter(file) ) {
            writer.write(textContent);
        } catch(IOException e) {
            System.out.println("write error");
        }
        return true;
    }
}
