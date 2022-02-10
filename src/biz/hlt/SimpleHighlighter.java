package biz.hlt;

import entity.Highlight;
import view.MyTextPane;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 高亮具体实现类
 * 实现方法大致描述：
 * > 从配置文件中读取指定文件的高亮样式描述
 * > 转换成Highlight对象存入不同的优先级列表
 * > 将每一个高亮样式应用到相应文本
 */


public class SimpleHighlighter {
    String settingName;
    String fileType;
    StyledDocument styledDocument;
    JTextPane textPane; // 这里改回了JTextPane
    public static final String PATH= "C:\\NotepadData\\highlights";
    private ArrayList<Highlight> normalList = new ArrayList();//按指定顺序的高亮 PART > ALL_LINE > KEYWORD
    private ArrayList<Highlight> importantList = new ArrayList();//优先级高的高亮
    private ArrayList<Highlight> unimportantList = new ArrayList();//优先级低的高亮

    private int[] highlighted;//已经高亮过的 - 记为1
    Style sys = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);

    public SimpleHighlighter(MyTextPane textPane){
        this.textPane = textPane;
        this.styledDocument = textPane.getStyledDocument();
    }

    //准备 - 把配置文件中的高亮设置读取到列表之中
    public void prepare(String settingName, String fileType){
        this.settingName = settingName;
        this.fileType = fileType.toLowerCase();//不管大小写
        HltConfReader conf = new HltDefaltReader(PATH+"\\"+settingName, fileType);
        this.normalList = conf.getNormalList();
        this.importantList = conf.getImportantList();
        this.unimportantList = conf.getUnimportantList();
    }

    //设置默认样式
    public void defaultSetting(){
        this.setCharacterAttributes(0, textPane.getText().length(), sys, true);
    }

    //返回是否准备好高亮
    public boolean hasPrepared(){
        return styledDocument != null && settingName != null;
    }
    //开始高亮
    public void highlight(){
        if(!hasPrepared())//没有准备或无设置就不高亮
            return;

        highlighted = new int[textPane.getText().length()];
        //在前面的优先级高
        action(importantList);
        action(normalList);
        action(unimportantList);
        transDefault();
    }
    //先移除指定位置的高亮样式 - 解决了高亮短暂残留的问题 - 暂时解决了高亮报错问题
    public void highlight(int offset, int length){
        if(!hasPrepared())//没有准备或无设置就不高亮
            return;
        try{

            //Thread.sleep(2);//这里高亮线程 “谦让一下”，不跟setText方法抢运行，就不报错了。但是文件越大sleep时间要越长

            this.setCharacterAttributes(offset, length, sys, true);
        }catch (Exception e){
            System.out.println("Illegal cast to MutableAttributeSet");
        }
        highlight();
    }

    //补画没有高亮的内容
    private void transDefault(){
        int index = 0, len = 0;
        boolean flag = true;
        for(int i = 0; i < highlighted.length; i++){
            if(highlighted[i] == 0){//未高亮
                if(flag) {
                    len = 0;
                    index = i;
                    flag = false;
                }
                len++;
            }else{
                flag = true;
                this.setCharacterAttributes(index, len, sys, true);
            }
        }
        this.setCharacterAttributes(index, len, sys, true);//最后一次
    }

    //这里改成同步方法好像能极大缓解延时和颜色混乱问题 - 多个线程的情况下 - 因为已改成了单线程，所以去掉了同步
    //添加了中断判断
    private void action(ArrayList<Highlight> list) {
        String text = textPane.getText().replaceAll("\\r", "");//这里还是需要把\r去掉
        for (Highlight highlight : list)
            if (highlight.getType() == Highlight.KEYWORD) {//关键字
                //极容易出现编码不一致问题！！！ 而且还有正则特殊字符的问题
                //如果key是正则，就直接用，不是正则，就当作关键词
                String regex;
                if (highlight.getKey1().matches("<.+>")) {//是正则
                    regex = highlight.getKey1().replaceAll("^(\\<)|(\\>)$", "");//去除标记
                } else {//不是正则
                    regex = "\\b" + highlight.getKey1() + "\\b";
                }
                Matcher m = Pattern.compile(regex).matcher(text);
                //高亮
                while (m.find()) {
                    todo(m.start(), m.end(), highlight);
                    //标记
                    for (int i = m.start(); i < m.end(); i++)
                        highlighted[i] = 1;
                }
            } else if (highlight.getType() == Highlight.ALL_LINE) {//整行
                //一定是正则，所以只要求内容
                Matcher m = Pattern.compile(highlight.getKey1()).matcher(text);
                while (m.find()) {
                    int length = 0;
                    for (int i = m.start(); i < text.length() && text.charAt(i) != '\n'; i++) {
                        length++;
                    }

                    todo(m.start(), m.start() + length, highlight);//这里的位置有待斟酌
                    //标记
                    for (int i = m.start(); i < m.start() + length; i++)
                        highlighted[i] = 1;
                }
            } else if (highlight.getType() == Highlight.PART) {//部分
                Matcher m1 = Pattern.compile(highlight.getKey1()).matcher(text);
                Matcher m2 = Pattern.compile(highlight.getKey2()).matcher(text);
                int start = 0, end = 0;
                while (m1.find(start)) {
                    start = m1.start();
                    if (m2.find(m1.end())) {
                        end = m2.end();
                        //要被高亮的部分
                        boolean done = todo(start, end, highlight);
                        if(!done){//被中断了，代表不可分割，就忽略start，从end之前再次开始
                            start = end-1;
                        }else{
                            //标记
                            for (int i = start; i < end; i++)
                                highlighted[i] = 1;

                            start = end;
                        }

                    }else
                        break;
                }
            }
    }

    /**
     * 根据高亮改变指定位置的样式
     * @param start
     * @param end
     * @param highlight
     * @return 返回为false的话代表高亮失败，即没有改变任何样式，可能是因为高亮属性中设置了不可分割，如果被分割，此高亮无效
     */
    private boolean todo(int start, int end, Highlight highlight){
        Style s = styledDocument.addStyle("s", sys);
        //这里如果颜色没设置的话，就默认
        StyleConstants.setForeground(s, highlight.getColor()==null?Color.black:highlight.getColor());
        StyleConstants.setBackground(s, highlight.getBackColor()==null? Color.white:highlight.getBackColor());
        StyleConstants.setBold(s, highlight.isBold());
        StyleConstants.setItalic(s, highlight.isItalic());
        StyleConstants.setUnderline(s, highlight.isUnderline());
        if(highlight.getSize() != -1)
            StyleConstants.setFontSize(s, highlight.getSize());
        if(highlight.getFont() != null)
            StyleConstants.setFontFamily(s, highlight.getFont());

        //下面的代码，避免了重复高亮的情况
        int index = start, len = 0;
        boolean flag = true;
        for(int i = start; i < end; i++){
            if(!hasHighlighted(i)){//未高亮
                if(flag) {
                    len = 0;
                    index = i;
                    flag = false;
                }
                len++;
            }else{
                if(!highlight.isCanDivided())//只要遇到已经高亮的，并且不可分割，return false
                    return false;

                flag = true;
                this.setCharacterAttributes(index, len, styledDocument.getStyle("s"), true);
            }
        }
        this.setCharacterAttributes(index, len, styledDocument.getStyle("s"), true);//最后一次
        return true;
    }

    private boolean hasHighlighted(int index){
        return highlighted[index] == 1;
    }

    /**
     * StyledDocument的方法，有可能有同步问题 - 已同步
     * 多空格的问题出在这个原生方法
     * @param offset
     * @param length
     * @param s
     * @param replace
     */
    private void setCharacterAttributes(int offset, int length, AttributeSet s, boolean replace){
        //这里对Document的修改进行同步
        synchronized (this.styledDocument) {
            styledDocument.setCharacterAttributes(offset, length, s, replace);
        }
    }

}