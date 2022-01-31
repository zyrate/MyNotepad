package view;

import biz.SimpleHighlighter;
import util.DTUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * 编辑窗口 - 新建文本文档
 */

public class EditWin extends JFrame{
    private JPanel pCenter, pFoot;
    private MyTextPane textPane;//改 - 富文本
    private JScrollPane pane;
    private JMenuBar menuBar;
    private JMenu mFile, mEdit, mTools, mHelp, mHighlight, imEncoding;
    private MyMenuItem iOpen, iSave, iSaveAnother, iFont, iReset, iAbout, iCount, iNew, iDate, iNote,
                        iFind, iReplace, iReOpen;
    private JCheckBoxMenuItem iLineWrap, iNoHL, iCode;
    private JLabel footLabel;
    private String mainMessage = "就绪";//当前主要页脚信息
    private String footMessage = mainMessage;//显示的页脚信息
    private String filePath = null;//打开某个文件的路径
    private String content = null;//打开某个文件的内容

    private Font menuFont = new Font("微软雅黑", 0, 15);
    //这里不同的电脑会不一样 DTUtil.getFontIndex()
    private Font textFont = new Font(FontChooser.fontsName[DTUtil.getFontIndex()], DTUtil.getStyleIndex(), DTUtil.getFontSize()+10);
    private String[] charsets = {"GBK", "UTF-8", "Unicode"};
    private ArrayList<JCheckBoxMenuItem> highlightItems = new ArrayList<>();//高亮菜单项
    private ArrayList<JCheckBoxMenuItem> charsetItems = new ArrayList<>();//字符集菜单项

    /*构造方法里的方法再次模块化有利于代码整洁*/
    public EditWin(){
        init();
        set();
        add();
        initHighlightMenu();
        initEncodingMenu();
        addListener();
        setVisible(true);
        update();
        starAnimation();
    }

    private void init(){
        pCenter = new JPanel();
        pFoot = new JPanel();
        menuBar = new JMenuBar();
        mFile = new JMenu("文件(F)");
        mEdit = new JMenu("编辑(E)");
        mTools = new JMenu("工具(T)");
        mHelp = new JMenu("帮助(H)");
        mHighlight = new JMenu("高亮(L)");
        textPane = new MyTextPane();
        pane = new JScrollPane(textPane, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        footLabel = new JLabel(footMessage);
        iOpen = new MyMenuItem("打开(O)");
        iSave = new MyMenuItem("保存(S)");
        iSaveAnother = new MyMenuItem("另存为(P)");
        iFont = new MyMenuItem("字体(T)");
        iReset = new MyMenuItem("恢复默认设置");
        iAbout = new MyMenuItem("关于记事本");
        iCount = new MyMenuItem("F1 字数统计");
        iNew = new MyMenuItem("新建(N)");
        iDate = new MyMenuItem("F2 日期");
        iNote = new MyMenuItem("F3 笔记");
        iLineWrap = new JCheckBoxMenuItem("自动换行");
        iNoHL = new JCheckBoxMenuItem("无");
        iFind = new MyMenuItem("查找(F)");
        iReplace = new MyMenuItem("替换(R)");
        iCode = new JCheckBoxMenuItem("代码模式 (\\)");
        imEncoding = new JMenu("编码方式");
        iReOpen = new MyMenuItem("重新载入(U)");
    }
    private void set(){
        //从文件中读取窗口位置大小
        this.setBounds(DTUtil.getX()-50, DTUtil.getY(), DTUtil.getWidth(), DTUtil.getHeight());
        //设置最大最小化
        this.setExtendedState(DTUtil.getMaxFrame()?JFrame.MAXIMIZED_BOTH:JFrame.NORMAL);
        //默认关闭操作不做任何事情，因为关闭时要进行保存判断
        this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        ImageIcon imageIcon = new ImageIcon(EditWin.class.getResource("/icons/notepad.png"));
        this.setIconImage(imageIcon.getImage());
        pCenter.setLayout(new BorderLayout());
        pFoot.setLayout(new FlowLayout(FlowLayout.LEFT));
        pFoot.setPreferredSize(new Dimension(1, 22));
        menuBar.setBackground(Color.WHITE);
        textPane.setBorder(BorderFactory.createLineBorder(Color.WHITE));
        mFile.setFont(menuFont);
        mEdit.setFont(menuFont);
        mTools.setFont(menuFont);
        mHelp.setFont(menuFont);
        mHighlight.setFont(menuFont);
        textPane.setFont(textFont);
        iNoHL.setState(true);
        //文件读取
        textPane.setLineWrap(DTUtil.getLineWrap());
        iLineWrap.setState(DTUtil.getLineWrap());
        iCode.setState(DTUtil.getCodeMode());
        textPane.setCodeMode(DTUtil.getCodeMode());
    }
    private void add(){
        menuBar.add(mFile);
        menuBar.add(mEdit);
        menuBar.add(mTools);
        menuBar.add(mHighlight);
        menuBar.add(mHelp);
        pCenter.add(pane);//滚动条
        pFoot.add(footLabel);
        mFile.add(iNew);
        mFile.add(iOpen);
        mFile.add(iSave);
        mFile.add(iSaveAnother);
        mFile.add(iReOpen);
        mFile.addSeparator();
        mFile.add(iReset);
        mEdit.add(iFont);
        mEdit.add(iLineWrap);
        mEdit.addSeparator();
        mEdit.add(iCode);
        mEdit.add(imEncoding);
        mEdit.add(iFind);
        mEdit.add(iReplace);
        mTools.add(iCount);
        mTools.add(iDate);
        mTools.add(iNote);
        mHelp.add(iAbout);
        mHighlight.add(iNoHL);
        this.add(menuBar, BorderLayout.NORTH);
        this.add(pCenter, BorderLayout.CENTER);
        this.add(pFoot, BorderLayout.SOUTH);
    }

    //初始化高亮菜单项
    private void initHighlightMenu(){
        File path = new File(SimpleHighlighter.PATH);
        if(!path.exists()){
            path.mkdir();
        }
        String[] filesName = path.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.matches(".+\\.highlights");//指定的文件名
            }
        });
        for(String name : filesName){
            String settingName = name.replaceAll("\\.highlights", "");
            JCheckBoxMenuItem item = new JCheckBoxMenuItem(settingName);
            mHighlight.add(item);
            highlightItems.add(item);
            if(settingName.equals(DTUtil.getHighlightName())){
                item.setState(true);
                iNoHL.setState(false);
            }
        }
    }

    //初始化编码菜单
    private void initEncodingMenu(){
        for(int i = 0; i < charsets.length; i++){
            JCheckBoxMenuItem item = new JCheckBoxMenuItem(charsets[i]);
            charsetItems.add(item);
            imEncoding.add(item);
            if(charsets[i].equals(DTUtil.getCharset())){
                item.setState(true);
            }
        }
    }

    //准备高亮
    public void prepareHighlight(String settingName, String fileType){
        textPane.prepareHighlight(settingName, fileType);
    }

    //开始高亮
    public void highlight(){
        textPane.highlight();
    }
    public void highlight(int offset, int length){
        textPane.highlight(offset, length);
    }
    //开始动画
    private void starAnimation(){
        if(!DTUtil.getMaxFrame())//没有最大化才有动画
            for(int i = 50; i >= 0; i--){
               setLocation(DTUtil.getX()-i, DTUtil.getY());
            }
    }
    //关闭动画
    public void closeAnimation(){
        if(!DTUtil.getMaxFrame())
            for(int i = DTUtil.getWidth(), j = 0; i >= 0; i-=10){
                setSize(i, 0);
                if(j<=50) {
                    setLocation(DTUtil.getX() - j, DTUtil.getY());
                    j++;
                }
            }
    }
    //初始界面
    public void reBegin(){
        footMessage = mainMessage;
        content = null;
        filePath = null;
        textPane.setText("");
        update();
    }
    //更新显示
    public void update(){
        if(filePath == null)
            setTitle("未命名 - 记事本");
        else
            setTitle(filePath+" - 记事本");
        textPane.setFont(textFont);
        footLabel.setText(footMessage);
    }
    //这两个更新分开写是因为更新文本好像会让光标移动，有时不想这样
    //更新内容
    public void updateContent(){
        textPane.setText(content);
    }
    //更改主要状态信息
    public void changeStatus(String mainMessage){
        this.mainMessage = mainMessage;
        showStatus(mainMessage);
    }
    //状态显示
    public void showStatus(String footMessage){
        this.footMessage = footMessage;
        footLabel.setText(footMessage);
    }
    //快速换行
    public void quickWrap(){
        textPane.quickWrap();
    }

    //格式化 - 很不完善，目前只适用于CSS
    public void format(){
        new Thread(){
            @Override
            public void run() {
                String buff = textPane.getText();
                String formattedText = "";
                int length = buff.length();
                for(int i = 0; i < length; i++){
                    char ch = buff.charAt(i);
                    char ch2 = ch;
                    if(i+1 < length)
                        ch2 = buff.charAt(i+1);
                    if(ch == '{' || ch == '}' || ch == ';'){
                        if(ch == '}')
                            if(ch2 != '\n')//后边没有才加
                                formattedText += '\n';
                        formattedText += ch;
                        if(ch2 != '\n')//后边没有才加
                            formattedText += '\n';
                        if(ch == '{' || ch == ';')
                            if(ch2 != '\t')//后边没有才加
                                formattedText += '\t';
                        continue;
                    }

                    formattedText += ch;
                    changeStatus("格式化进度："+new DecimalFormat("#").format(i*1.0/length*100)+"%");
                }
                textPane.setText(formattedText);
                changeStatus("就绪");
            }
        }.start();
    }

    /*为菜单等添加提示信息监听*/
    private void addListener(){
        addFootTipListener(textPane, mainMessage);
        addFootTipListener(mFile, "打开、保存、恢复默认设置等");
        addFootTipListener(mEdit, "关于文本的编辑、输入、显示等");
        addFootTipListener(mHelp, "有关该记事本的帮助信息");
        addFootTipListener(mHighlight, "设置是否启用高亮功能以及其样式");
        addFootTipListener(mTools, "实用小工具");
        addFootTipListener(iCode, "是否进入代码模式，Ctrl + \\");
        addFootTipListener(iReplace, "替换文本，Ctrl + R");
        addFootTipListener(iFind, "查找文本，Ctrl + F");
        addFootTipListener(iAbout, "关于记事本");
        addFootTipListener(iCount, "统计中英文字数、句数，F1");
        addFootTipListener(iDate, "追加当前日期，F2");
        addFootTipListener(iFont, "选择字体，Ctrl + T");
        addFootTipListener(iLineWrap, "是否自动换行");
        addFootTipListener(iNew, "新建未命名文件，Ctrl + N");
        addFootTipListener(iNoHL, "不高亮");
        addFootTipListener(iNote, "打开保存的笔记，F3");
        addFootTipListener(iOpen, "打开新文件，Ctrl + O");
        addFootTipListener(iReset, "恢复字体、界面大小、选择等默认设置");
        addFootTipListener(iSave, "保存本文件，Ctrl + S");
        addFootTipListener(iSaveAnother, "另存为本文件，Ctrl + P");
        addFootTipListener(imEncoding, "设置编码方式以正确读取文件");
        addFootTipListener(iReOpen, "重新载入当前文件");

        for(int i = 0; i < highlightItems.size(); i++){
            JCheckBoxMenuItem item = highlightItems.get(i);
            item.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    showStatus("选择高亮样式："+item.getText());
                }
            });
        }
    }
    /*为组件添加页脚提示监听器*/
    private void addFootTipListener(JComponent comp, String tip){
        comp.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                showStatus(tip);
            }
        });
    }



    //getter and setter

    public MyMenuItem getiReOpen() {
        return iReOpen;
    }

    public ArrayList<JCheckBoxMenuItem> getCharsetItems() {
        return charsetItems;
    }

    public JScrollPane getPane() {
        return pane;
    }

    public MyMenuItem getiNote() {
        return iNote;
    }

    public MyMenuItem getiDate() {
        return iDate;
    }

    public MyMenuItem getiNew() {
        return iNew;
    }

    public MyMenuItem getiCount() {
        return iCount;
    }

    public MyMenuItem getiAbout() {
        return iAbout;
    }

    public JCheckBoxMenuItem getiLineWrap() {
        return iLineWrap;
    }

    public MyMenuItem getiReset() {
        return iReset;
    }

    public MyMenuItem getiFont() {
        return iFont;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public MyTextPane getTextPane() {
        return textPane;
    }

    public JMenu getmFile() {
        return mFile;
    }

    public JMenu getmEdit() {
        return mEdit;
    }

    public JMenu getmTools() {
        return mTools;
    }

    public JMenu getmHelp() {
        return mHelp;
    }

    public JMenu getmHighlight() {
        return mHighlight;
    }

    public MyMenuItem getiOpen() {
        return iOpen;
    }

    public MyMenuItem getiSave() {
        return iSave;
    }

    public MyMenuItem getiSaveAnother() {
        return iSaveAnother;
    }

    public void setTextFont(Font textFont) {
        this.textFont = textFont;
    }

    public ArrayList<JCheckBoxMenuItem> getHighlightItems() {
        return highlightItems;
    }

    public JCheckBoxMenuItem getiNoHL() {
        return iNoHL;
    }


    public MyMenuItem getiFind() {
        return iFind;
    }

    public MyMenuItem getiReplace() {
        return iReplace;
    }


    public JCheckBoxMenuItem getiCode() {
        return iCode;
    }

}
