package entity;

import java.awt.*;

/**
 * 高亮类
 */


public class Highlight {
    public static final int UNKNOWN  = 0;
    public static final int KEYWORD  = 1;
    public static final int ALL_LINE = 2;
    public static final int PART = 3;
    private boolean canDivided = true;//此高亮是否可被分割
    private int type;
    private String key1, key2;//只有部分样式有两个键
    private Color color;
    private Color backColor;//背景颜色
    private boolean bold;
    private int size = -1;
    private String font;
    private boolean italic;
    private boolean underline;
    //关键字KEYWORD是否是正则，为了兼容自定义高亮配置以<>区分是否是正则的做法。
    //如果此字段为null，表明用的是自定义配置；否则是xml配置，在xml中以key的属性设置该字段
    private Boolean keyWordRegex = null;

    String[] keys, values;

    //该构造方法供自定义配置用
    public Highlight(int type, String[] keys, String[] values){
        this.type = type;
        this.keys = keys;
        this.values = values;
        init();
    }

    //该构造方法供xml配置用
    public Highlight(int type, String key1, String key2, String color, String backColor, boolean bold,
                     int size, String font, boolean italic, boolean underline, boolean keyWordRegex, boolean canDivided){
        this.type = type;
        this.key1 = key1;
        this.key2 = key2;
        this.color = transfer(color);
        this.backColor = transfer(backColor);
        this.bold = bold;
        this.size = size;
        this.font = font;
        this.italic = italic;
        this.underline = underline;
        this.keyWordRegex = keyWordRegex;
        this.canDivided = canDivided;
    }

    private Color transfer(String color){
        if(!color.matches("[bB]?#[0-9a-fA-F]{6}")){
            return null;
        }
        int r, g, b;
        if(color.charAt(0) != 'b' && color.charAt(0) != 'B') {
            r = Integer.parseInt(color.substring(1, 3), 16);
            g = Integer.parseInt(color.substring(3, 5), 16);
            b = Integer.parseInt(color.substring(5, 7), 16);
        }else{
            r = Integer.parseInt(color.substring(2, 4), 16);
            g = Integer.parseInt(color.substring(4, 6), 16);
            b = Integer.parseInt(color.substring(6, 8), 16);
        }
        return new Color(r, g, b);
    }

    private void init(){
        for(String k:keys){
            k = k.replaceAll("^(\\\')|(\\\')$", "");//去引号
            //这里的k会有空字符，不算就好了
            if(k.equals(""))
                continue;
            if(key1 == null)
                key1 = k;
            else if(key2 == null)
                key2 = k;
        }
        //这样就不用考虑顺序了
        int index;
        if((index = contains("[bB]{0}#[0-9a-fA-F]{6}")) != -1){
            this.color = transfer(values[index]);
        }
        if((index = contains("[bB]#[0-9a-fA-F]{6}")) != -1){//背景颜色
            this.backColor = transfer(values[index]);
        }
        if((index = contains("bold")) != -1){
            this.bold = true;
        }
        if((index = contains("[0-9]+")) != -1){
            this.size = Integer.parseInt(values[index]);
        }
        if((index = contains("\\\".+\\\"")) != -1){
            this.font = values[index].replaceAll("^(\\\")|(\\\")$", "");
        }
        if((index = contains("italic")) != -1){
            this.italic = values[index].equals("italic");
        }
        if((index = contains("underline")) != -1){
            this.underline = values[index].equals("underline");
        }
        if(contains("~") != -1){//此高亮不可分割
            this.canDivided = false;
        }
    }

    public int contains(String regex){
        for(int i = 0; i < values.length; i++){
            if(values[i].matches(regex))
                return i;
        }
        return -1;
    }

    public boolean isCanDivided() {
        return canDivided;
    }

    public void setCanDivided(boolean canDivided) {
        this.canDivided = canDivided;
    }

    public Color getBackColor() {
        return backColor;
    }

    public void setBackColor(Color backColor) {
        this.backColor = backColor;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getKey1() {
        return key1;
    }

    public void setKey1(String key1) {
        this.key1 = key1;
    }

    public String getKey2() {
        return key2;
    }

    public void setKey2(String key2) {
        this.key2 = key2;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public boolean isBold() {
        return bold;
    }

    public void setBold(boolean bold) {
        this.bold = bold;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getFont() {
        return font;
    }

    public void setFont(String font) {
        this.font = font;
    }

    public boolean isItalic() {
        return italic;
    }

    public void setItalic(boolean italic) {
        this.italic = italic;
    }

    public boolean isUnderline() {
        return underline;
    }

    public void setUnderline(boolean underline) {
        this.underline = underline;
    }

    public Boolean isKeyWordRegex() {
        return keyWordRegex;
    }

    public void setKeyWordRegex(Boolean keyWordRegex) {
        this.keyWordRegex = keyWordRegex;
    }
}
