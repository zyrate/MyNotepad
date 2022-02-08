package biz;

import util.JavaUtil;
import view.EditWin;

import javax.swing.*;
import java.awt.*;

/**
 * 计数器 - 统计字数等
 * 中文句子个数以句号、问号、感叹号的个数统计，若无句号但内容不为空，认为1句
 * 英文句子个数以点、英文问号、感叹号的个数统计，有几个是几句
 * 英文单词个数以连续的英文块数统计，'符号有连续性
 */

public class Counter extends JDialog{
    private int total;//总数
    private int nonSymbol;//非符号字数
    private int numCnt;//数字个数
    private int cnSentence;//中文句子个数
    private int enSentence;//英文句子个数
    private int words;//英文单词个数
    private String text;//文本

    private JTextArea ta;

    public Counter(EditWin editWin){
        text = editWin.getTextPane().getText();
        init();
        setLocationRelativeTo(editWin);
        count();
        ta.setText("总字数："+total+"\n"+"非符号字数："+nonSymbol+"\n"+"数字个数："+numCnt+"\n"+"句子个数："+cnSentence+"\n"+"英文单词个数："+words+"\n"+"英文句子个数："+enSentence+"\n");
        setVisible(true);
    }
    public void init(){
        setModal(true);
        setTitle("字数统计");
        setSize(300, 260);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(null);
        ta = new JTextArea(10, 50);
        ta.setBounds(55, 30, 200, 150);
        ta.setEnabled(false);
        ta.setFont(new Font("微软雅黑", 0, 17));
        ta.setBackground(new Color(240, 240, 240));
        add(ta);
    }

    public void count(){
        /*这两个布尔变量的作用是判断连续的一串字符的属性*/
        boolean isNum = false, isWord = false;
        for(int i = 0; i < text.length(); i++){
            char ch = text.charAt(i);
            //空白
            if(JavaUtil.isBlank(ch)) {
                isNum = false;
                isWord = false;
                continue;
            }
            //非符号字数
            if(!(ch >= 33 && ch <= 47 || ch >= 58 && ch <= 64 || ch >= 91 && ch <= 96 ||
                    ch >= 123 && ch <= 126 || ch  == '，' || ch == '。' || ch == '？' || ch == '；' ||
                    ch == '：' || ch == '‘' || ch == '’' || ch == '“' || ch == '”' || ch == '【' ||
                    ch == '】' || ch == '！' || ch == '·' || ch == '、' || ch == '（' || ch == '）')){//不是符号
                nonSymbol++;
            }
            //数字
            if(ch >= '0' && ch <= '9'){
                if(!isNum)
                    numCnt++;
                isNum = true;
            }else{
                //不是小数
                if(!(i+1 <= text.length()-1 && ch == '.' && text.charAt(i+1) >= '0' && text.charAt(i+1) <= '9'))
                    isNum = false;
            }
            //中文句子
            if(ch == '。' || ch == '？' || ch == '！'){
                cnSentence++;
            }
            //英文句子
            if(ch == '.' || ch == '?' || ch == '!'){
                enSentence++;
            }
            //英文单词
            if(ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z'){
                if(!isWord)
                    words++;
                isWord = true;
            }else{
                if(ch != '\'')//不是撇
                    isWord = false;
            }
            //总共
            total++;
        }
        //如果整篇文章无句号，则句子数为一
        if(cnSentence == 0 && !text.equals(""))
            cnSentence = 1;
    }
}
