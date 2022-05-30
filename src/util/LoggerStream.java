package util;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Date;

/**
 * 用于生成日志
 */
public class LoggerStream extends PrintStream {
    private String type;    //日志类型
    public LoggerStream(String path, String type) throws FileNotFoundException {
        super(new FileOutputStream(path, true));
        this.type = type==null?"":type;
    }
    //包裹信息
    private String wrapMsg(String x){
        if(x.startsWith("\t")) return x;
        switch (type){
            case "INFO": x = " INFO "+x; break;
            case "WARN": x = " WARN "+x; break;
            case "ERROR": x = " ERROR "+x; break;
        }
        x = new Date().toLocaleString() + " " + x;
        return x;
    }

    @Override
    public void print(String s) {
        //为了屏蔽不和谐的字符串
        if(s.startsWith("Exception in thread")) return;
        super.print(s);
    }

    @Override
    public void println(String x) {
        x = wrapMsg(x);
        super.println(x);
    }

    @Override
    public void println(Object x) {
        String s = String.valueOf(x);
        s = wrapMsg(s);
        super.println(s);
    }
}
