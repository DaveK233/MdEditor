package util;

import org.markdown4j.Markdown4jProcessor;

import java.io.IOException;

public class Translator {

    private String mHtml = null;
    private String mTextMd = null;
    private Markdown4jProcessor mParser = new Markdown4jProcessor();

    public Translator() {}

    public String transMdToHtml(String md) throws IOException {
        mTextMd = md;
        String tStr = mParser.process(mTextMd);
        tStr = "<html>\n"
                + "<head>\n"
                + "<style>\n"
                + "</style>\n"
                + "</head>\n"
                + "<body>\n"
                + tStr
                + "</body>\n"
                + "</html>\n";
        mHtml = tStr;
        return mHtml;
    }
}
