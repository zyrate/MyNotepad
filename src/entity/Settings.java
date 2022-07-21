package entity;
import java.io.Serializable;
/**
 * 保存的设置
 */
public class Settings implements Serializable{
    private int x=400, y=130, height=700, width=1150;
    private int fontIndex=115, styleIndex=0, fontSize=16;
    private boolean lineWrap=true;
    private boolean isMaxFrame=false;
    private boolean notesExt = false;//笔记是否有扩展名
    private boolean codeMode = true;//代码模式
    private boolean darkMode = false;//暗色模式
    private boolean showLineNum = false;//是否显示行号
    private String highlightName = null;
    private String lastOpenPath = "C:\\";//上一次打开的文件目录
    private String lastSavePath = "C:\\";//上一次保存的文件目录
    private String charset = "UTF-8";//编码
    private boolean hasAnimation = true;//是否有过渡动画


    public boolean isHasAnimation() {
        return hasAnimation;
    }

    public void setHasAnimation(boolean hasAnimation) {
        this.hasAnimation = hasAnimation;
    }

    public boolean isDarkMode() {
        return darkMode;
    }

    public void setDarkMode(boolean darkMode) {
        this.darkMode = darkMode;
    }

    public String getLastSavePath() {
        return lastSavePath;
    }

    public boolean isShowLineNum() {
        return showLineNum;
    }

    public void setShowLineNum(boolean showLineNum) {
        this.showLineNum = showLineNum;
    }

    public void setLastSavePath(String lastSavePath) {
        this.lastSavePath = lastSavePath;
    }
    public String getCharset() {
        return charset;
    }
    public void setCharset(String charset) {
        this.charset = charset;
    }
    public String getLastOpenPath() {
        return lastOpenPath;
    }
    public void setLastOpenPath(String lastOpenPath) {
        this.lastOpenPath = lastOpenPath;
    }
    public boolean isCodeMode() {
        return codeMode;
    }
    public void setCodeMode(boolean codeMode) {
        this.codeMode = codeMode;
    }
    public String getHighlightName() {
        return highlightName;
    }
    public void setHighlightName(String highlightName) {
        this.highlightName = highlightName;
    }
    public boolean isNotesExt() {
        return notesExt;
    }
    public void setNotesExt(boolean notesExt) {
        this.notesExt = notesExt;
    }
    public boolean isMaxFrame() {
        return isMaxFrame;
    }
    public void setMaxFrame(boolean maxFrame) {
        isMaxFrame = maxFrame;
    }
    public int getX() {
        return x;
    }
    public void setX(int x) {
        this.x = x;
    }
    public int getY() {
        return y;
    }
    public void setY(int y) {
        this.y = y;
    }
    public int getHeight() {
        return height;
    }
    public void setHeight(int height) {
        this.height = height;
    }
    public int getWidth() {
        return width;
    }
    public void setWidth(int width) {
        this.width = width;
    }
    public int getFontIndex() {
        return fontIndex;
    }
    public void setFontIndex(int fontIndex) {
        this.fontIndex = fontIndex;
    }
    public int getStyleIndex() {
        return styleIndex;
    }
    public void setStyleIndex(int styleIndex) {
        this.styleIndex = styleIndex;
    }
    public int getFontSize() {
        return fontSize;
    }
    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }
    public boolean isLineWrap() {
        return lineWrap;
    }
    public void setLineWrap(boolean lineWrap) {
        this.lineWrap = lineWrap;
    }
}
