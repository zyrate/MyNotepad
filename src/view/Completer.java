package view;

import util.JavaUtil;

import javax.swing.*;
import javax.swing.text.Caret;
import java.awt.*;
import java.util.*;

/**
 * 补全提示框
 */
public class Completer extends ListAndScroll<String>{
    JScrollPane pane;
    MyTextPane textPane;
    private int width = 280;
    private int height = 140;
    private int xOffset = 0;//行号的宽度
    private int yOffset = 32; //一行的高度
    private String halfTyped;//要补全的字符串
    private String completion;//补全字符串
    private ArrayList<String> words; //当前所有单词
    private String[] candidates; //候选补全单词
    private int currIndex = 0;
    private int fontSize;

    public Completer(JScrollPane pane){
        this.pane = pane;
        this.setBackground(Color.white);
        this.setVisible(false);
        textPane = (MyTextPane) pane.getViewport().getView();
        this.initList(250);
        collectWords();
    }

    //调整提示框位置 - 生成列表
    public void adjustCompleter(){
        //调整参数
        fontSize = textPane.getFont().getSize();
        height = yOffset*4;
        yOffset = fontSize+10;
        currIndex = 0;
        this.getList().setVisibleRowCount(4);
        this.getList().setFont(new Font("Consolas", 0, fontSize));
        this.findCandidates();
        this.refreshList(candidates);

        Caret caret = textPane.getCaret();
        if(candidates.length != 0) {
            Point p = caret.getMagicCaretPosition();
            if (p != null) {
                int x = p.x + xOffset - pane.getHorizontalScrollBar().getValue();
                int y = p.y + yOffset - pane.getVerticalScrollBar().getValue();
                boolean isCoverdByTop = false;
                if(y + height > pane.getHeight()){//提示框被下面挡住了
                    y = y - height - yOffset ; //放到光标上面去
                    if(y < 0){//被上面挡住了
                        y += height;
                        x += fontSize*2;
                        isCoverdByTop = true;
                    }
                }
                if(x + width > pane.getWidth()){//提示框被右面挡住了
                    x = x - width ; //放到光标左面去
                    if(isCoverdByTop){//被上面挡住了
                        x -= fontSize*4;
                    }
                }
                this.setBounds(x, y, width, height);
            }
            this.setVisible(true);
        }else if(candidates.length == 0){
            this.setVisible(false);
            return;
        }

        this.select(currIndex);
        completion = candidates[currIndex>=candidates.length?0:currIndex].substring(halfTyped.length());
    }

    //是否可以分隔单词
    private boolean isSep(char ch){
        return !JavaUtil.isWordChar(ch);
    }

    //寻找候选词
    private void findCandidates(){
        String text = textPane.getLine();//光标所在行
        TreeSet<String> set = new TreeSet<>();//临时集合
        int pos = textPane.getCursorColumn() - 1;
        int i=pos-1;
        while(text.length()!=0 && i>=0 && !isSep(text.charAt(i))) i--;
        halfTyped = text.substring(i+1, pos); //要补全的字符串
        for(String s:words){
            //这里要注意完全相等的字符串也不要，因为是补全
            if(!halfTyped.equals("") && !s.equals(halfTyped) && s.startsWith(halfTyped)){
                set.add(s);
            }
        }
        candidates = new String[set.size()];
        set.toArray(candidates);
    }

    //收集单词
    public void collectWords(){
        words = new ArrayList<>();
        String text = textPane.getText();
        String[] split = text.split("\\W+");
        for(String s:split){
            if(!words.contains(s) && !s.equals("")){
                words.add(s);
            }
        }
    }

    public void hidePanel(){
        this.setVisible(false);
    }

    public void complete(){
        textPane.insert(completion);
        this.setVisible(false);
    }

    public void next(){
        currIndex = (currIndex+1) % candidates.length;
        this.select(currIndex);
        completion = candidates[currIndex].substring(halfTyped.length());
    }

    public void pre(){
        currIndex = (candidates.length+currIndex-1) % candidates.length;
        this.select(currIndex);
        completion = candidates[currIndex].substring(halfTyped.length());
    }

    public int getxOffset() {
        return xOffset;
    }

    public void setxOffset(int xOffset) {
        this.xOffset = xOffset;
    }

    public int getyOffset() {
        return yOffset;
    }

    public void setyOffset(int yOffset) {
        this.yOffset = yOffset;
    }
}
