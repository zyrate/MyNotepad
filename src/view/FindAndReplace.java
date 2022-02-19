package view;
import util.CompFactory;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * 查找和替换
 */
public class FindAndReplace extends JDialog{
    EditWin editWin;
    private JLabel lFind = CompFactory.createLabel("查找内容："),
            lReplace = CompFactory.createLabel("替换文本：");
    private JTextField tFind = CompFactory.createTextField(15),
            tReplace = CompFactory.createTextField(15);
    private JButton bNext = CompFactory.createButton("下一个"),
            bPre = CompFactory.createButton("上一个"),
            bReplace = CompFactory.createButton("替换"),
            bReplaceAll = CompFactory.createButton("替换全部");
    private JTextArea taTip = CompFactory.createTextArea(290, 32, 120, 50);
    private static final int WIDTH = 408, HEIGHT = 230;
    private int currIndex = 0;
    private String currContent = "";
    private int start = 0;
    private int end = 0;
    //单例模式
    private static FindAndReplace instance = null;
    public static FindAndReplace getInstance(String type, EditWin editWin){
        if(instance == null){
            instance = new FindAndReplace();
            instance.editWin = editWin;
            //靠右
            instance.setLocation(editWin.getX()+editWin.getWidth()-WIDTH-30, editWin.getY()+65);
        }
        if(type.equals("find")){
            instance.setTitle("查找");
            instance.lReplace.setEnabled(false);
            instance.tReplace.setEditable(false);
            instance.tReplace.setFocusable(false);
            instance.bReplace.setEnabled(false);
            instance.bReplaceAll.setEnabled(false);
        }else if(type.equals("replace")){
            instance.setTitle("替换");
            instance.lReplace.setEnabled(true);
            instance.tReplace.setEditable(true);
            instance.tReplace.setFocusable(true);
            instance.bReplace.setEnabled(true);
            instance.bReplaceAll.setEnabled(true);
        }else{
            return null;
        }
        //自动添加选中的文本
        String content = instance.editWin.getTextPane().getSelectedText();
        instance.tFind.setText(content==null?"":content);
        instance.tFind.selectAll();
        instance.setVisible(true);
        return instance;
    }
    private FindAndReplace(){
        setSize(WIDTH, HEIGHT);
        setResizable(false);
        setLayout(null);
        lFind.setBounds(8,8,100,30);
        tFind.setBounds(8,40,280, 30);
        lReplace.setBounds(8, 75, 100, 30);
        tReplace.setBounds(8, 107, 280, 30);
        bNext.setBounds(304,150,90,35);
        bPre.setBounds(212, 150, 90, 35);
        bReplace.setBounds(8,150,90,35);
        bReplaceAll.setBounds(100, 150, 110, 35);
        taTip.setText("支持正则\n以/开头,/结尾");
        taTip.setFont(new Font("楷体",1, 15));
        add(lFind);
        add(tFind);
        add(bPre);
        add(bNext);
        add(lReplace);
        add(tReplace);
        add(bReplace);
        add(bReplaceAll);
        add(taTip);
        addListener();
    }
    private void addListener(){
        bNext.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                next();
            }
        });
        bPre.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pre();
            }
        });
        bReplace.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                replace();
            }
        });
        bReplaceAll.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                replaceAll();
            }
        });
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                reset();
                dispose();
            }
        });
        tFind.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if(e.isShiftDown() && e.getKeyCode() == KeyEvent.VK_ENTER){
                    pre();
                }else if(e.getKeyCode() == KeyEvent.VK_ENTER){
                    next();
                }else if(e.getKeyCode() == KeyEvent.VK_ESCAPE){
                    reset();
                    dispose();
                }
            }
        });
        tReplace.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if(e.isShiftDown() && e.getKeyCode() == KeyEvent.VK_ENTER){
                    next();//上回车查找下一个
                }else if(e.isControlDown() && e.getKeyCode() == KeyEvent.VK_ENTER){
                    replaceAll();//ctrl回车替换全部
                }else if(e.getKeyCode() == KeyEvent.VK_ENTER){
                    replace();
                }else if(e.getKeyCode() == KeyEvent.VK_ESCAPE){
                    reset();
                    dispose();
                }
            }
        });
    }
    private void next(){
        if(tFind.equals(""))
            return;
        String buff = editWin.getTextPane().getText().replaceAll("\\r", "");
        String content = transform(tFind.getText());
        if(!currContent.equals(content)){
            currContent = content;
            currIndex = 0;
        }
        Matcher m = Pattern.compile(content).matcher(buff);
        int index = 0;
        while(m.find()){
            index++;
            if(index == currIndex+1){
                break;
            }
        }
        if(index == currIndex){//没有下一个了
            JOptionPane.showMessageDialog(null, "没有下一个了！", "提示", JOptionPane.INFORMATION_MESSAGE);
        }else{
            start = m.start();
            end = m.end();
            editWin.getTextPane().choose(start, end);
            currIndex++;
        }
    }
    private void pre(){
        if(tFind.equals(""))
            return;
        if(currIndex <= 1){//没有上一个了
            JOptionPane.showMessageDialog(null, "没有上一个了！", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String buff = editWin.getTextPane().getText().replaceAll("\\r", "");
        String content = transform(tFind.getText());
        if(!currContent.equals(content)){
            currContent = content;
            currIndex = 0;
        }
        Matcher m = Pattern.compile(content).matcher(buff);
        int index = 0;
        while(m.find()){
            index++;
            if(index == currIndex-1){
                break;
            }
        }
        start = m.start();
        end = m.end();
        editWin.getTextPane().choose(start, end);
        currIndex--;
    }
    private void replace(){
        if(tFind.equals(""))
            return;
        if(start == end) {
            JOptionPane.showMessageDialog(null, "请先查找！", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        editWin.getTextPane().replaceRange(tReplace.getText(), start, end);
        editWin.getTextPane().setSelectionStart(start);
        editWin.getTextPane().setSelectionEnd(start+tReplace.getText().length());
        start = 0;
        end = 0;
        currIndex--;
    }
    private void replaceAll(){
        if(tFind.equals(""))
            return;
        String buff = editWin.getTextPane().getText().replaceAll("\\r", "");
        String content = transform(tFind.getText());
        Matcher m = Pattern.compile(content).matcher(buff);
        String replace = tReplace.getText();
        //这里只用来计数，没办法替换
        int cnt = 0;
        while(m.find()){
            cnt++;
            start = m.start();
            end = m.end();
        }
        if(cnt == 0){
            JOptionPane.showMessageDialog(null, "没有找到内容", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        //这样替换
        new Thread(){
            @Override
            public void run() {
                editWin.getTextPane().setText(buff.replaceAll(content, replace));
            }
        }.start();
        currIndex = 0;
        start = 0;
        end = 0;
        JOptionPane.showMessageDialog(null, "一共替换 "+cnt+" 处", "提示", JOptionPane.INFORMATION_MESSAGE);
    }
    private void reset(){
        currContent = "";
        currIndex = 0;
        tFind.setText("");
        tReplace.setText("");
        start = 0;
        end = 0;
    }
    /**
     * 将要查找的字符串进行转换，如果两边以//包围的话就认为是正则，不做处理
     * 否则将正则的元字符转换为原意字符
     * @param content
     * @return
     */
    private String transform(String content){
        if(content.matches("^/.+/$")){//是正则
            return content.substring(1, content.length()-1);
        }
        content = content.replaceAll("\\\\", "\\\\\\\\");
        content = content.replaceAll("\\.", "\\\\.");
        content = content.replaceAll("\\^", "\\\\^");
        content = content.replaceAll("\\$", "\\\\\\$");//$有特殊含义
        content = content.replaceAll("\\?", "\\\\?");
        content = content.replaceAll("\\+", "\\\\+");
        content = content.replaceAll("\\*", "\\\\*");
        content = content.replaceAll("\\{", "\\\\{");
        content = content.replaceAll("\\}", "\\\\}");
        content = content.replaceAll("\\[", "\\\\[");
        content = content.replaceAll("\\]", "\\\\]");
        content = content.replaceAll("\\(", "\\\\(");
        content = content.replaceAll("\\)", "\\\\)");
        content = content.replaceAll("\\|", "\\\\|");
        return content;
    }
}
