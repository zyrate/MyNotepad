package util;


public class JavaUtil {
    /**
     * 是否是空白字符
     * @param ch
     * @return
     */
    public static boolean isBlank(char ch){
        return ch==' ' || ch=='\t' || ch=='\n' || ch=='\r';
    }
}
