package util;
import entity.Settings;
import view.About;

import java.io.*;
/**
 * 数据操作工具类
 * 封装了对对象的操作、文件读写操作
 * 外界只需调用get set
 */
public class DTUtil {
    public static final String PATH = "C:\\NotepadData\\"+ About.VERSION +"setting.sav";
    private static File file = new File(PATH);
    private static Settings settings;
    private static ObjectOutputStream oos;
    private static ObjectInputStream ois;
    public static void initFile(){
        try {
            oos = new ObjectOutputStream(new FileOutputStream(file));
            oos.writeObject(new Settings());
            oos.flush();
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static void read(){
        try {
            if(!file.exists()) {
                new File("C:\\NotepadData").mkdir();
                file.createNewFile();
                initFile();
            }
            ois = new ObjectInputStream(new FileInputStream(file));
            settings = (Settings) ois.readObject();
            ois.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    private static void write(Settings settings){
        try {
            oos = new ObjectOutputStream(new FileOutputStream(file));
            oos.writeObject(settings);
            oos.flush();
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void setAnimation(boolean hasAnimation){
        read();
        settings.setHasAnimation(hasAnimation);
        write(settings);
    }
    public static boolean getAnimation(){
        read();
        return settings.isHasAnimation();
    }

    public static void setDarkMode(boolean isDarkMode){
        read();
        settings.setDarkMode(isDarkMode);
        write(settings);
    }
    public static boolean getDarkMode(){
        read();
        return settings.isDarkMode();
    }
    public static void setCharset(String charset){
        read();
        settings.setCharset(charset);
        write(settings);
    }
    public static String getCharset(){
        read();
        return settings.getCharset();
    }
    public static void setShowLineNum(boolean isShow){
        read();
        settings.setShowLineNum(isShow);
        write(settings);
    }
    public static boolean getShowLineNum(){
        read();
        return settings.isShowLineNum();
    }

    public static void setLastOpenPath(String lastPath){
        read();
        settings.setLastOpenPath(lastPath);
        write(settings);
    }
    public static String getLastOpenPath(){
        read();
        return settings.getLastOpenPath();
    }
    public static void setLastSavePath(String lastPath){
        read();
        settings.setLastSavePath(lastPath);
        write(settings);
    }
    public static String getLastSavePath(){
        read();
        return settings.getLastSavePath();
    }
    public static void setHighlightName(String name){
        read();
        settings.setHighlightName(name);
        write(settings);
    }
    public static boolean getCodeMode(){
        read();
        return settings.isCodeMode();
    }
    public static void setCodeMode(boolean codeMode){
        read();
        settings.setCodeMode(codeMode);
        write(settings);
    }
    public static String getHighlightName(){
        read();
        return settings.getHighlightName();
    }
    public static void setNotesExt(boolean notesExt){
        read();
        settings.setNotesExt(notesExt);
        write(settings);
    }
    public static boolean getNotesExt(){
        read();
        return settings.isNotesExt();
    }
    public static void setMaxFrame(boolean maxFrame){
        read();
        settings.setMaxFrame(maxFrame);
        write(settings);
    }
    public static void setX(int x){
        read();
        settings.setX(x);
        write(settings);
    }
    public static void setY(int y){
        read();
        settings.setY(y);
        write(settings);
    }
    public static void setWidth(int width){
        read();
        settings.setWidth(width);
        write(settings);
    }
    public static void setHeight(int height){
        read();
        settings.setHeight(height);
        write(settings);
    }
    public static void setFontIndex(int index){
        read();
        settings.setFontIndex(index);
        write(settings);
    }
    public static void setStyleIndex(int index){
        read();
        settings.setStyleIndex(index);
        write(settings);
    }
    public static void setFontSize(int fontSize){
        read();
        settings.setFontSize(fontSize);
        write(settings);
    }
    public static void setLineWrap(boolean lineWrap){
        read();
        settings.setLineWrap(lineWrap);
        write(settings);
    }
    public static boolean getMaxFrame(){
        read();
        return settings.isMaxFrame();
    }
    public static int getX(){
        read();
        return settings.getX();
    }
    public static int getY(){
        read();
        return settings.getY();
    }
    public static int getHeight(){
        read();
        return settings.getHeight();
    }
    public static int getWidth(){
        read();
        return settings.getWidth();
    }
    public static int getFontIndex(){
        read();
        return settings.getFontIndex();
    }
    public static int getStyleIndex(){
        read();
        return settings.getStyleIndex();
    }
    public static int getFontSize(){
        read();
        return settings.getFontSize();
    }
    public static boolean getLineWrap(){
        read();
        return settings.isLineWrap();
    }
}
