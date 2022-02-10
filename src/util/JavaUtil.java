package util;


import java.util.concurrent.CountDownLatch;

public class JavaUtil {
    /**
     * 是否是空白字符
     * @param ch
     * @return
     */
    public static boolean isBlank(char ch){
        return ch==' ' || ch=='\t' || ch=='\n' || ch=='\r';
    }

    public static CountDownLatch setTextLatch = null; //文本变动的锁，即先文本变动后高亮。
}
