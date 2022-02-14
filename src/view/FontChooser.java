package view;

import util.DTUtil;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * 字体选择器
 * 字体选择的逻辑是：每次在列表中选择字体、样式或字号(包括输入)都会生成一个Font对象称为chosenFont（被选中
 * 字体），首先把这个字体用于预览区预览，当用户按下确定时，currFont（当前编辑字体）会被赋值为chosenFont，
 * 然后会将编辑区内的字体设置为currFont，接着把选择字体的字体下标、样式下标、字号写入设置文件中以便下次读取。
 * $在这里currFont有些鸡肋，原本的设想是，本类不负责设置字体，只负责选择并返回字体。
 * $有可能会有bug，最后写入文件的是字体下标，系而如果下次打开时系统多了一种字体，可能会乱套。
 *
 * 本类的难点有两个：
 * 1、UI设计。各个组件的大小、位置、相对位置、形状都要协调，简单但费力。
 * 2、用户体验优化。所谓优化是指用户在文本框中输入内容可以起到搜索作用，一旦匹配便会迅速跳转到匹配项，而字号
 * 的输入则会实时改变字体预览。难点在于监听器的协调。监听器是“一丝不苟”的，你让它监听某个组件，只要有动静，
 * 它就会反应，而很多时候，程序内部对组件的操作不希望被反应，只希望监听用户的操作。像是文本监听，用户输入了
 * 关键字，列表跳到某一项，这时列表监听器会认为用户点了这一项，那么文本框中的内容要改变，之后会形成死循环，
 * 事实上，这一步会报异常，文本监听器不允许在监听方法中改变文本内容。我引入了ua_变量来解决这个问题，ua意为
 * User Action（用户操作），是布尔变量，标记的是：对于某个组件来说，当前改变是否为用户操作。这样一来，经过
 * 代码设计，解决了这一问题。
 */

public class FontChooser extends JDialog{
    /*获取系统环境*/
    public static GraphicsEnvironment e = GraphicsEnvironment.getLocalGraphicsEnvironment();
    /*得到系统字体数组*/
    public static String[] fontsName = e.getAvailableFontFamilyNames();
    public static String[] stylesName = {"常规", "粗体", "倾斜", "粗体倾斜"};
    Integer[] sizes = {8, 9, 10, 12, 14, 16, 18, 20, 22, 24, 26, 28, 30, 40, 50};
    EditWin editWin;

    private Font currFont;//当前字体
    private Font chosenFont;//选择的字体
    private Font font1 = new Font("微软雅黑", 0, 18);
    private Font font2 = new Font("微软雅黑", 0, 16);
    //为了方便，把列表和滚动条合并在了一起，封装为JPanel，简化操作
    private ListAndScroll<String> fontsList;//字体列表
    private ListAndScroll<String> stylesList;//样式列表
    private ListAndScroll<Integer> sizesList;//字号列表
    private JTextField tFont;
    private JTextField tStyle;
    private JTextField tSize;
    private JLabel lFont;
    private JLabel lStyle;
    private JLabel lSize;
    private JLabel lShowEn;//英文预览
    private JLabel lShowCn;//中文预览
    private JButton bOK;
    private JButton bCancel;
    //ua代表是否是用户的动作，这是为了协调各个监听器与程序逻辑的关系，简单说就是，让监听器在非用户动作时不响应
    //待定
    private boolean ua_list;
    private boolean ua_text;

    public FontChooser(){
        setModal(true);
    }

    //初始化窗口
    private void initWin(Component parent){
        currFont = new Font(FontChooser.fontsName[DTUtil.getFontIndex()], DTUtil.getStyleIndex(), DTUtil.getFontSize());//从文件读取
        init();
        set();
        add();
        addListener();
        showFont();
        //add之后再设置位置
        this.setLocationRelativeTo(parent);
        this.setVisible(true);
    }
    private void init(){
        fontsList = new ListAndScroll<>(fontsName, 190);
        stylesList = new ListAndScroll<>(stylesName, 150);
        sizesList = new ListAndScroll<>(sizes, 70);
        tFont = new JTextField(10);
        tStyle = new JTextField(10);
        tSize = new JTextField(5);
        lFont = new JLabel("字体");
        lStyle = new JLabel("样式");
        lSize = new JLabel("字号");
        lShowEn = new JLabel();
        lShowCn = new JLabel();
        bOK = new JButton("确定");
        bCancel = new JButton("取消");
    }
    private void set(){
        this.setTitle("选择字体");
        this.setSize(550, 600);
        this.setResizable(false);
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.setLayout(null);

        tFont.setFont(font1);
        tStyle.setFont(font1);
        tSize.setFont(font1);
        bOK.setFont(font2);
        bCancel.setFont(font2);

//        lShowEn.setBorder(BorderFactory.createTitledBorder("英文预览"));
//        lShowCn.setBorder(BorderFactory.createTitledBorder("中文预览"));
        lShowEn.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY,1), "英文预览"));
        lShowCn.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY,1), "中文预览"));

        fontsList.setBounds(13, 65,220, 230);
        stylesList.setBounds(240, 65, 180, 230);
        sizesList.setBounds(428, 65, 100, 230);
        tFont.setBounds(17, 35, 213, 30);
        tStyle.setBounds(244, 35, 173, 30);
        tSize.setBounds(432, 35, 93, 30);
        lFont.setBounds(18, 10, 30, 30);
        lStyle.setBounds(245, 10, 30, 30);
        lSize.setBounds(433, 10, 30, 30);
        lShowEn.setBounds(13, 300, 220, 150);
        lShowCn.setBounds(240, 300, 288, 150);
        bOK.setBounds(300, 510, 110, 35);
        bCancel.setBounds(417, 510, 110, 35);

        fontsList.select(DTUtil.getFontIndex());
        stylesList.select(DTUtil.getStyleIndex());

        tFont.setText(fontsName[fontsList.getList().getSelectedIndex()]);
        tStyle.setText(stylesName[stylesList.getList().getSelectedIndex()]);
        tSize.setText(String.valueOf(DTUtil.getFontSize()));
        ua_text = true;
        sizeChange();
    }
    private void add(){
        this.add(fontsList);
        this.add(stylesList);
        this.add(sizesList);
        this.add(tFont);
        this.add(tStyle);
        this.add(tSize);
        this.add(lFont);
        this.add(lStyle);
        this.add(lSize);
        this.add(lShowEn);
        this.add(lShowCn);
        this.add(bOK);
        this.add(bCancel);
    }

    //显示选择器
    public void showChooser(EditWin editWin){
        this.editWin = editWin;
        editWin.showStatus("选择字体");
        initWin(editWin);
        editWin.showStatus("就绪");
    }
    //显示预览
    public void showFont(){
        chosenFont = new Font(fontsList.getList().getSelectedValue(), stylesList.getList().getSelectedIndex(), Integer.valueOf(tSize.getText())+10);//加数字
        lShowCn.setFont(chosenFont);
        lShowCn.setText("示例文字 123");
        lShowEn.setFont(chosenFont);
        lShowEn.setText("main(String[] a){");
    }

    //如果在文本监听中修改了文本，会出异常
    //用户输入了字号
    public void sizeChange(){
        if(!ua_text){
            ua_text = true;
            return;
        }
        int size;
        try {
            size = Integer.valueOf(tSize.getText());
        } catch (Exception e) {
            return;
        }
        //用户输入了列表中的字号
        int index;
        if ((index = contains(sizes, size)) != -1) {
            ua_list = false;
            sizesList.select(index);
        }else
            if(!sizesList.getList().isSelectionEmpty())
                sizesList.getList().clearSelection();
        showFont();
    }
    public void fontChange(){
        if(!ua_text){
            ua_text = true;
            return;
        }
        int index;
        if((index = contains(fontsName, tFont.getText())) != -1){
            ua_list = false;
            fontsList.select(index);
            showFont();
        }
    }
    public void styleChange(){
        if(!ua_text){
            ua_text = true;
            return;
        }
        int index;
        if((index = contains(stylesName, tStyle.getText())) != -1){
            ua_list = false;
            stylesList.select(index);
            showFont();
        }
    }

    public void addListener(){
        //列表监听
        fontsList.getList().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if(ua_list){
                    ua_text = false;
                    tFont.setText(fontsName[fontsList.getList().getSelectedIndex()]);
                    showFont();
                }
                ua_list = true;
            }
        });
        stylesList.getList().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if(ua_list){
                    ua_text = false;
                    tStyle.setText(stylesName[stylesList.getList().getSelectedIndex()]);
                    showFont();
                }
                ua_list = true;
            }
        });
        sizesList.getList().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if(sizesList.getList().isSelectionEmpty())
                    return;
                if(ua_list) {
                    ua_text = false;
                    //被选中之后，会设置文本框内容，但是程序自动跳转的不应执行这句，会发生异常
                    tSize.setText(String.valueOf(sizes[sizesList.getList().getSelectedIndex()]));
                    showFont();
                }
                ua_list = true;
            }
        });
        //按钮监听
        bOK.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currFont = chosenFont;
                editWin.getTextPane().setFont(currFont);//在这里设置了文本框的字体
                editWin.setTextFont(currFont);//这里也不能丢
                DTUtil.setFontIndex(fontsList.getList().getSelectedIndex());
                DTUtil.setStyleIndex(stylesList.getList().getSelectedIndex());
                DTUtil.setFontSize(currFont.getSize()-10);
                dispose();
            }
        });
        bCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        //文本框监听
        tSize.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                sizeChange();
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        });
        tFont.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                fontChange();
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        });
        tStyle.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                styleChange();
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        });
    }
    //是否存在元素 - 返回下标
    public int contains(String[] arr, String s){
        int minIndex = Integer.MAX_VALUE;
        int arrIndex = -1;
        for(int i = 0; i < arr.length; i++){
            int index = arr[i].indexOf(s);
            if(index < minIndex && index != -1) {
                minIndex = index;
                arrIndex = i;
            }
            if(arr[i].equals(s))
                return i;
        }
        if(minIndex != Integer.MAX_VALUE)
            return arrIndex;
        return -1;
    }
    public int contains(Integer[] arr, Integer in){
        for(int i = 0; i < arr.length; i++){
            if(arr[i] == in)
                return i;
        }
        return -1;
    }
}


