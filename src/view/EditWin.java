package view;

import biz.hlt.SimpleHighlighter;
import util.CompFactory;
import util.DTUtil;
import util.JavaUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
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
    private JPanel pCenter, pFoot, pFootLeft, pFootRight;
    private MyTextPane textPane;//改 - 富文本
    private JScrollPane pane;
    private JMenuBar menuBar;
    private JMenu mFile, mEdit, mTools, mHelp, mHighlight, imEncoding, imCurrEncoding, mRun;
    private JMenuItem iOpen, iSave, iSaveAnother, iFont, iReset, iAbout, iCount, iNew, iDate, iNote,
                        iFind, iReplace, iReOpen, iPrint, iBaidu, iRun, iTranslate, iTimer;
    private JCheckBoxMenuItem iLineWrap, iNoHL, iCode, iLineNum;
    private JLabel lFoot1, lFoot2, lFoot3, lFoot4; //底部的各个信息标签，1-介绍，2-编码，3-位置，4-字数
    private String mainMessage = "就绪";//当前主要页脚信息
    private String footMessage = mainMessage;//显示的页脚信息
    private String filePath = null;//打开某个文件的路径
    private String content = null;//打开某个文件的内容
    private String fileName = null;//文件名
    private String pureFileName = null;//文件名,不带后缀
    private String currEncoding = DTUtil.getCharset();//当前编码类型，未打开文件时是默认编码

    private Font menuFont = new Font("微软雅黑", 0, 15);
    //这里不同的电脑会不一样 DTUtil.getFontIndex()
    private Font textFont = new Font(FontChooser.fontsName[DTUtil.getFontIndex()], DTUtil.getStyleIndex(), DTUtil.getFontSize()+10);
    private String[] charsets = {"GBK", "UTF-8", "Unicode"};
    private ArrayList<JCheckBoxMenuItem> highlightItems = new ArrayList<>();//高亮菜单项
    private ArrayList<JCheckBoxMenuItem> charsetItems = new ArrayList<>();//字符集菜单项
    private ArrayList<JCheckBoxMenuItem> currCharsetItems = new ArrayList<>();//当前字符集菜单项

    /*构造方法里的方法再次模块化有利于代码整洁*/
    public EditWin(){
        init();
        set();
        add();
        initHighlightMenu();
        initEncodingMenu();
        initOtherMenu();
        addListener();
        setVisible(true);
        update();
        starAnimation();
    }

    private void init(){
        pCenter = new JPanel();
        pFoot = new JPanel();
        pFootRight = new JPanel();
        pFootLeft = new JPanel();
        menuBar = new JMenuBar();
        mFile = CompFactory.createMenu("文件(F)", KeyEvent.VK_F);
        mEdit = CompFactory.createMenu("编辑(E)", KeyEvent.VK_E);
        mTools = CompFactory.createMenu("工具(T)", KeyEvent.VK_T);
        mHelp = CompFactory.createMenu("帮助(H)", KeyEvent.VK_H);
        mHighlight = CompFactory.createMenu("高亮(L)", KeyEvent.VK_L);
        mRun = CompFactory.createMenu("运行(R)", KeyEvent.VK_R);
        textPane = new MyTextPane();
        pane = new JScrollPane(textPane, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        lFoot1 = new JLabel(footMessage);
        lFoot2 = new JLabel("UTF8");
        lFoot3 = new JLabel("第 0 行，第 0 列");
        lFoot4 = new JLabel("共 0 字");
        iOpen = CompFactory.createMenuItem("打开(O)...", "control O");
        iSave = CompFactory.createMenuItem("保存(S)", "control S");
        iSaveAnother = CompFactory.createMenuItem("另存为(A)...");
        iFont = CompFactory.createMenuItem("字体(T)...", "control T");
        iReset = CompFactory.createMenuItem("恢复默认设置");
        iAbout = CompFactory.createMenuItem("关于记事本");
        iCount = CompFactory.createMenuItem("字数统计", "F1");
        iDate = CompFactory.createMenuItem("日期", "F2");
        iNote = CompFactory.createMenuItem("笔记", "F3");
        iNew = CompFactory.createMenuItem("新建(N)", "control N");
        iLineWrap = CompFactory.createCheckMenuItem("自动换行");
        iNoHL = CompFactory.createCheckMenuItem("无");
        iFind = CompFactory.createMenuItem("查找(F)...", "control F");
        iReplace = CompFactory.createMenuItem("替换(R)...", "control R");
        iCode = CompFactory.createCheckMenuItem("代码模式 (\\)");
        iLineNum = CompFactory.createCheckMenuItem("显示行号");
        imEncoding = CompFactory.createMenu("默认编码方式");
        imCurrEncoding = CompFactory.createMenu("当前编码方式");
        iReOpen = CompFactory.createMenuItem("重新载入(U)", "control U");
        iPrint = CompFactory.createMenuItem("打印(P)...", "control P");
        iBaidu = CompFactory.createMenuItem("使用百度搜索", "control E");
        iRun = CompFactory.createMenuItem("运行(B)", "control R");
        iTranslate = CompFactory.createMenuItem("使用谷歌翻译", "control G");
        iTimer = CompFactory.createMenuItem("计时器", "F4");
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
        pFoot.setLayout(new BorderLayout());
        pFoot.setPreferredSize(new Dimension(1, 22));
        pFootLeft.setLayout(new FlowLayout(FlowLayout.LEFT,5,0));//这句话是流式布局的垂直居中和水平边距
        pFootRight.setLayout(new FlowLayout(FlowLayout.RIGHT, 30, 0));
        lFoot1.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lFoot2.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lFoot3.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lFoot4.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        menuBar.setBackground(Color.WHITE);
        textPane.setBorder(BorderFactory.createLineBorder(Color.WHITE));
        textPane.setFont(textFont);
        iNoHL.setState(true);

        //文件读取
        textPane.setLineWrap(DTUtil.getLineWrap());
        iLineWrap.setState(DTUtil.getLineWrap());
        iCode.setState(DTUtil.getCodeMode());
        textPane.setCodeMode(DTUtil.getCodeMode());
        pane.setRowHeaderView(DTUtil.getShowLineNum()?new TextLineNumber(textPane):null);
    }
    private void add(){
        menuBar.add(mFile);
        menuBar.add(mEdit);
        menuBar.add(mTools);
        menuBar.add(mHighlight);
        menuBar.add(mRun);
        menuBar.add(mHelp);
        pCenter.add(pane);//滚动条

        pFoot.add(pFootLeft, BorderLayout.WEST);
        pFoot.add(pFootRight, BorderLayout.EAST);
        pFootLeft.add(lFoot1);
        pFootRight.add(lFoot4);
        pFootRight.add(lFoot3);
        pFootRight.add(lFoot2);

        mFile.add(iNew);
        mFile.add(iOpen);
        mFile.add(iSave);
        mFile.add(iSaveAnother);
        mFile.add(iReOpen);
        mFile.addSeparator();
        mFile.add(iPrint);
        mFile.addSeparator();
        mFile.add(iReset);
        mEdit.add(iFont);
        mEdit.add(iLineWrap);
        mEdit.addSeparator();
        mEdit.add(iCode);
        mEdit.add(iLineNum);
        mEdit.add(imCurrEncoding);
        mEdit.add(imEncoding);
        mEdit.addSeparator();
        mEdit.add(iBaidu);
        mEdit.add(iTranslate);
        mEdit.addSeparator();
        mEdit.add(iFind);
        mEdit.add(iReplace);
        mTools.add(iCount);
        mTools.add(iDate);
        mTools.add(iNote);
        mTools.add(iTimer);
        mRun.add(iRun);
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
            JavaUtil.copyFile("default"+SimpleHighlighter.CONF_TYPE, path.getPath());//拷贝配置文件
        }
        String[] filesName = path.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.matches(".+"+SimpleHighlighter.CONF_TYPE);//指定的文件名
            }
        });
        for(String name : filesName){
            String settingName = name.replaceAll(SimpleHighlighter.CONF_TYPE, "");
            JCheckBoxMenuItem item = new JCheckBoxMenuItem(settingName);
            item.setFont(menuFont);
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
            //默认
            JCheckBoxMenuItem item = new JCheckBoxMenuItem(charsets[i]);
            charsetItems.add(item);
            imEncoding.add(item);
            if(charsets[i].equals(DTUtil.getCharset())){
                item.setState(true);
            }
            //当前
            JCheckBoxMenuItem item2 = new JCheckBoxMenuItem(charsets[i]);
            currCharsetItems.add(item2);
            imCurrEncoding.add(item2);
            if(charsets[i].equals(currEncoding)){
                item2.setState(true);
            }
        }
    }

    //初始化其他菜单
    private void initOtherMenu(){
        if(DTUtil.getShowLineNum()){
            iLineNum.setState(true);
        }else{
            iLineNum.setState(false);
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
        content = null;
        filePath = null;
        textPane.setText("");
        currEncoding = DTUtil.getCharset();//新建文件用默认编码
        changeStatus("就绪");
        update();
    }
    //更新显示
    public void update(){
        if(filePath == null)
            setTitle("未命名 - 记事本");
        else
            setTitle(filePath+" - 记事本");
        textPane.setFont(textFont);
        lFoot1.setText(footMessage);
        lFoot2.setText(currEncoding);
        //更新编码菜单
        for(JCheckBoxMenuItem item : getCurrCharsetItems()){
            if(item.getLabel().equals(currEncoding)){
                item.setState(true);
            }else {
                item.setState(false);
            }
        }
    }
    //这两个更新分开写是因为更新文本好像会让光标移动，有时不想这样
    //更新内容
    public void updateContent(){
        textPane.setText(content);
    }
    //光标变动后，需要更新的内容 - 需要在文本、键盘和鼠标监听器调用
    public void cursorChange(){
        lFoot3.setText("第 "+textPane.getCursorLine()+" 行，第 "+textPane.getCursorColumn()+" 列");
    }
    //文本变动后，需要更新的内容
    public void textChange(){
        lFoot4.setText("共 "+textPane.getCharCount()+" 字");
    }
    //选中内容变动后，需要更新的内容
    private String nowSelected = null;
    private String lastSelected = null;
    public void selectedChange(){
        //选中后显示选中的字数，不选中时复原
        nowSelected = textPane.getSelectedText();
        if(nowSelected != null){
            lFoot4.setText("选中 "+textPane.getSelectedCharCount()+" 字");
        }else if(lastSelected != null){
            lFoot4.setText("共 "+textPane.getCharCount()+" 字");
        }
        lastSelected = nowSelected;
    }
    //更改主要状态信息
    public void changeStatus(String mainMessage){
        this.mainMessage = mainMessage;
        showStatus(mainMessage);
    }
    //状态显示
    public void showStatus(String footMessage){
        this.footMessage = footMessage;
        lFoot1.setText(footMessage);
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
        addFootTipListener(textPane, null);
        addFootTipListener(mFile, "打开、保存、恢复默认设置等");
        addFootTipListener(mEdit, "关于文本的编辑、输入、显示等");
        addFootTipListener(mHelp, "有关该记事本的帮助信息");
        addFootTipListener(mHighlight, "设置是否启用高亮功能以及其样式");
        addFootTipListener(mTools, "实用小工具");
        addFootTipListener(mRun, "运行代码");
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
        addFootTipListener(iSaveAnother, "另存为本文件，Ctrl + Shift + A");
        addFootTipListener(imEncoding, "设置默认的编码方式");
        addFootTipListener(imCurrEncoding, "设置当前的编码方式以正确读取和保存文件");
        addFootTipListener(iReOpen, "重新载入当前文件");
        addFootTipListener(iPrint, "打印当前文件内容，Ctrl + P");
        addFootTipListener(iBaidu, "使用百度搜索当前内容或选中内容，Ctrl + E");
        addFootTipListener(iTranslate, "使用谷歌翻译当前内容或选中内容，Ctrl + G");
        addFootTipListener(iRun, "运行当前文件，Ctrl + B");
        addFootTipListener(iLineNum, "是否显示行号");
        addFootTipListener(iTimer, "正计时或倒计时");

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
                showStatus(tip==null?mainMessage:tip);//如果tip是null就显示mainMessage
            }
        });
    }



    //getter and setter

    public JCheckBoxMenuItem getiLineNum() {
        return iLineNum;
    }

    public JMenuItem getiPrint() {
        return iPrint;
    }

    public JMenuItem getiReOpen() {
        return iReOpen;
    }

    public ArrayList<JCheckBoxMenuItem> getCharsetItems() {
        return charsetItems;
    }

    public JScrollPane getPane() {
        return pane;
    }

    public JMenuItem getiNote() {
        return iNote;
    }

    public JMenuItem getiDate() {
        return iDate;
    }

    public JMenuItem getiNew() {
        return iNew;
    }

    public JMenuItem getiCount() {
        return iCount;
    }

    public JMenuItem getiAbout() {
        return iAbout;
    }

    public JCheckBoxMenuItem getiLineWrap() {
        return iLineWrap;
    }

    public JMenuItem getiReset() {
        return iReset;
    }

    public JMenuItem getiFont() {
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
        setFileName(new File(filePath).getName());
        setPureFileName(getFileName().replaceAll("\\.\\w+$", ""));
    }

    public JMenuItem getiTimer() {
        return iTimer;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getPureFileName() {
        return pureFileName;
    }

    public void setPureFileName(String pureFileName) {
        this.pureFileName = pureFileName;
    }

    public String getCurrEncoding() {
        return currEncoding;
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

    public JMenuItem getiOpen() {
        return iOpen;
    }

    public JMenuItem getiSave() {
        return iSave;
    }

    public JMenuItem getiSaveAnother() {
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

    public JMenu getmRun() {
        return mRun;
    }

    public JMenuItem getiRun() {
        return iRun;
    }

    public JMenuItem getiFind() {
        return iFind;
    }

    public JMenuItem getiReplace() {
        return iReplace;
    }

    public JMenuItem getiBaidu() {
        return iBaidu;
    }

    public ArrayList<JCheckBoxMenuItem> getCurrCharsetItems() {
        return currCharsetItems;
    }

    public void setCurrEncoding(String currEncoding) {
        this.currEncoding = currEncoding;
    }

    public JCheckBoxMenuItem getiCode() {
        return iCode;
    }

    public JMenuItem getiTranslate() {
        return iTranslate;
    }
}
