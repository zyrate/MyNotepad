package biz.hlt;

import entity.Highlight;
import entity.HltToken;
import util.CompFactory;
import view.MyTextPane;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 高亮具体实现类
 * 实现方法大致描述：
 * > 从配置文件中读取指定文件的高亮样式描述
 * > 转换成Highlight对象存入不同的优先级列表
 * > 将每一个高亮样式应用到相应文本
 *
 * 目前大文件实时高亮慢的解决办法：
 * 打开文件时的一次性高亮逻辑不变
 * 文本变动时的高亮策略变为：
 *      只高亮变动的行，
 *      但是这样会出现跨行高亮不生效的情况
 *      所以在样式实体里添加了是否跨行的属性
 *      每次文本变动都会再对所有的跨行样式进行高亮
 *      但是这样在跨行样式失效时无法触发transDefault（因为这个方法是全局的，而highlighted里已无全局标记）
 *      所以想到了用token形式
 */


public class SimpleHighlighter {
    String settingName;
    String fileType;
    StyledDocument styledDocument;
    MyTextPane textPane; // 这里改回了JTextPane
    public static final String PATH= "C:\\NotepadData\\highlights";
    public static final String CONF_TYPE = ".hlts";//当前使用的配置文件类型 .xml 或 .hlts

    //如果要规范的话，每个优先级内部都应该确保 PART > ALL_LINE > KEYWORD
    private ArrayList<Highlight> normalList = new ArrayList();//按指定顺序的高亮 PART > ALL_LINE > KEYWORD
    private ArrayList<Highlight> importantList = new ArrayList();//优先级高的高亮
    private ArrayList<Highlight> unimportantList = new ArrayList();//优先级低的高亮

    private int[] highlighted;//已经高亮过的 - 记为1
    private int hltStart = 0, hltEnd = -1;//高亮区间，默认代表全部高亮
    private LinkedList<HltToken> spanLinesTokens;//跨行高亮标记
    Style sys = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);

    public SimpleHighlighter(MyTextPane textPane){
        this.textPane = textPane;
        this.styledDocument = textPane.getStyledDocument();
    }

    //准备 - 把配置文件中的高亮设置读取到列表之中
    public void prepare(String settingName, String fileType){
        this.settingName = settingName;
        this.fileType = fileType.toLowerCase();//不管大小写
        if(settingName == null) return;
        //可读取不同类型的配置文件
        HltConfReader conf = new HltDefaultReader(PATH+"\\"+settingName, fileType, textPane.getDarkMode());
        this.normalList = conf.getNormalList();
        this.importantList = conf.getImportantList();
        this.unimportantList = conf.getUnimportantList();

        spanLinesTokens = new LinkedList<>();
    }

    //设置默认样式
    public void defaultSetting(){
        //加入了hltEnd
        this.setCharacterAttributes(0, getHltEnd()-hltStart, sys, true);
    }

    //返回是否准备好高亮
    public boolean hasPrepared(){
        return styledDocument != null && settingName != null;
    }
    //开始高亮（所有部分）
    public void highlight(){
        if(!hasPrepared())//没有准备或无设置就不高亮
            return;

        highlighted = new int[getHltEnd()-getHltStart()];
        //在前面的优先级高
        matchHighlight(importantList, false);
        matchHighlight(normalList, false);
        matchHighlight(unimportantList, false);
        transDefault();
    }
    //高亮跨行部分
    public void highlightSpanLines(){
        if(!hasPrepared())//没有准备或无设置就不高亮
            return;

        setHltStart(0);
        setHltEnd(-1);
        highlighted = new int[getHltEnd()-getHltStart()];
        matchHighlight(importantList, true);
        matchHighlight(normalList, true);
        matchHighlight(unimportantList, true);
    }

    //补画没有高亮的内容
    protected void transDefault(){
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
                this.setCharacterAttributes(hltStart+index, len, sys, true);
            }
        }
        this.setCharacterAttributes(hltStart+index, len, sys, true);//最后一次
    }

    /**
     * 匹配高亮 - 将高亮列表中的所有作用域进行匹配，如符合则渲染样式（render）
     * 这里改成同步方法好像能极大缓解延时和颜色混乱问题 - 多个线程的情况下 - 因为已改成了单线程，所以去掉了同步
     * 添加了中断判断
     * @param list
     * @param onlySpan 是否只为跨行高亮
     */

    protected void matchHighlight(ArrayList<Highlight> list, boolean onlySpan) {
        String text = textPane.getText().replaceAll("\\r", "");//这里还是需要把\r去掉
        text = text.substring(hltStart, getHltEnd()); //截取高亮区间
        for (Highlight highlight : list) {
            if(onlySpan && !highlight.isCanSpanLines())
                continue;
            if (highlight.getType() == Highlight.KEYWORD) {//关键字
            //极容易出现编码不一致问题！！！ 而且还有正则特殊字符的问题
            //如果key是正则，就直接用，不是正则，就当作关键词
            String regex;

            Boolean isRegex = highlight.isKeyWordRegex();//是否是正则
            if (isRegex == null) {/*自定义配置*/
                if (highlight.getKey1().matches("<.+>")) {//是正则
                    regex = highlight.getKey1().replaceAll("^(\\<)|(\\>)$", "");//去除标记
                } else {//不是正则
                    regex = "\\b" + highlight.getKey1() + "\\b";
                }
            } else {/*兼容 V2.43 XML配置*/
                if (isRegex) {
                    regex = highlight.getKey1();
                } else {
                    regex = "\\b" + highlight.getKey1() + "\\b";
                }
            }
            Matcher m = Pattern.compile(regex).matcher(text);
            //高亮
            while (m.find()) {
                render(m.start(), m.end(), highlight);
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

                render(m.start(), m.start() + length, highlight);//这里的位置有待斟酌
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
                        boolean done = render(start, end, highlight);
                        if (!done) {//被中断了，代表不可分割，就忽略start，从end之前再次开始
                            start = end - 1;
                        } else {
                            //标记
                            for (int i = start; i < end; i++)
                                highlighted[i] = 1;

                            start = end;
                        }

                    } else
                        break;
                }
            }
        }
    }

    /**
     * 渲染 - 根据高亮改变指定位置的样式
     * @param start
     * @param end
     * @param highlight
     * @return 返回为false的话代表高亮失败，即没有改变任何样式，可能是因为高亮属性中设置了不可分割，如果被分割，此高亮无效
     */
    protected boolean render(int start, int end, Highlight highlight){
        Style s = styledDocument.addStyle("s", sys);
        //这里如果颜色没设置的话，就默认
        if(textPane.getDarkMode()){ //暗色模式
            StyleConstants.setForeground(s, highlight.getColor()==null?Color.white:highlight.getColor());
            StyleConstants.setBackground(s, highlight.getBackColor()==null? CompFactory.color1 :highlight.getBackColor());
        }else{
            StyleConstants.setForeground(s, highlight.getColor()==null?Color.black:highlight.getColor());
            StyleConstants.setBackground(s, highlight.getBackColor()==null? Color.white:highlight.getBackColor());
        }
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
                this.setCharacterAttributes(hltStart+index, len, styledDocument.getStyle("s"), true);
            }
        }
        this.setCharacterAttributes(hltStart+index, len, styledDocument.getStyle("s"), true);//最后一次
        //如果是跨行高亮，记录它的位置信息 token
        if(highlight.isCanSpanLines()){
            spanLinesTokens.add(new HltToken(start, end));
        }
        return true;
    }

    protected boolean hasHighlighted(int index){
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
    protected void setCharacterAttributes(int offset, int length, AttributeSet s, boolean replace){
        //这里对Document的修改进行同步
        synchronized (this.styledDocument) {
            //从高亮起始位置
            styledDocument.setCharacterAttributes(offset, length, s, replace);
        }
    }


    public int getHltStart() {
        return hltStart;
    }

    public void setHltStart(int hltStart) {
        this.hltStart = hltStart;
    }

    public int getHltEnd() {
        return hltEnd==-1?textPane.getText().length():hltEnd;
    }

    public void setHltEnd(int hltEnd) {
        this.hltEnd = hltEnd;
    }
}