package util;


import java.io.*;
import java.util.concurrent.CountDownLatch;

public class JavaUtil {
    public static CountDownLatch setTextLatch = null; //文本变动的锁，即先文本变动后高亮。

    /**
     * 是否是空白字符
     * @param ch
     * @return
     */
    public static boolean isBlank(char ch){
        return ch==' ' || ch=='\t' || ch=='\n' || ch=='\r';
    }


    /**
     * 得到文件类型
     * 一律小写
     * @param fileName
     * @return
     */
    public static String getFileType(String fileName){
        return fileName == null ? fileName : fileName.substring(fileName.lastIndexOf('.')+1, fileName.length()).toLowerCase();
    }

    /**
     * 将根目录下的文件复制到其他处
     * @param fileName 源文件名(项目根目录)
     * @param tarPath 目标路径
     */
    public static void copyFile(String fileName, String tarPath){
        try(InputStream is = new FileInputStream(fileName);
            OutputStream os = new FileOutputStream(tarPath+"\\"+fileName, true)){
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len=is.read(buffer)) != -1){
                os.write(buffer, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 判断文件编码格式 - CSDN
     * @param file
     * @return
     * @throws Exception
     */
    public static String detectCharset(File file){
        String charset = "GBK";
        byte[] first3Bytes = new byte[3];
        try {
            boolean checked = false;
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
            bis.mark(0);
            int read = bis.read(first3Bytes, 0, 3);
            if (read == -1) {
                return charset; //文件编码为 ANSI
            } else if (first3Bytes[0] == (byte) 0xFF
                    && first3Bytes[1] == (byte) 0xFE) {
                charset = "Unicode"; //文件编码为 UTF-16LE
                checked = true;
            } else if (first3Bytes[0] == (byte) 0xFE
                    && first3Bytes[1] == (byte) 0xFF) {
                charset = "Unicode"; //文件编码为 UTF-16BE
                checked = true;
            } else if (first3Bytes[0] == (byte) 0xEF
                    && first3Bytes[1] == (byte) 0xBB
                    && first3Bytes[2] == (byte) 0xBF) {
                charset = "UTF-8"; //文件编码为 UTF-8
                checked = true;
            }
            bis.reset();
            if (!checked) {
                int loc = 0;
                while ((read = bis.read()) != -1) {
                    loc++;
                    if (read >= 0xF0)
                        break;
                    if (0x80 <= read && read <= 0xBF) // 单独出现BF以下的，也算是GBK
                        break;
                    if (0xC0 <= read && read <= 0xDF) {
                        read = bis.read();
                        if (0x80 <= read && read <= 0xBF) // 双字节 (0xC0 - 0xDF)
                            // (0x80
                            // - 0xBF),也可能在GB编码内
                            continue;
                        else
                            break;
                    } else if (0xE0 <= read && read <= 0xEF) {// 也有可能出错，但是几率较小
                        read = bis.read();
                        if (0x80 <= read && read <= 0xBF) {
                            read = bis.read();
                            if (0x80 <= read && read <= 0xBF) {
                                charset = "UTF-8";
                                break;
                            } else
                                break;
                        } else
                            break;
                    }
                }
            }
            bis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return charset;
    }
}
