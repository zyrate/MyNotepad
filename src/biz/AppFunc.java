package biz;
import util.DTUtil;
import view.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.Document;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.*;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
/*本项目以UTF-8编码*/
/**
 * 应用功能
 * >2.0
 * >增加了拖拽打开文件功能
 * >自动换行菜单项不应该用ChangeListener而应该用Action
 * >使用了忽略\r的判等
 * >加入了高亮功能
 * >加入了回撤功能
 * >撤销/回撤功能忽略了样式改变，只记录文本变动
 * >修复了打开另一个文件仍然能撤销上个文件内容的bug
 *
 * >在文本变化时加入了是否高亮判断，避免大量无用线程
 * >2.1 - 可以直接在文件的打开方式中设置以notepad打开
 *      - 改变了选中颜色
 *      - 加入了查找替换功能
 * >2.2 - 增加了unimportant高亮的优先级，标置为?，是最不重要的队列，提高了高亮的丰富性
 *      - 将TAB键规定为4个空格
 *      - 增加了自动缩进功能：回车后与上一个非空行对齐，如果在大括号中间的话...你懂的
 *      - 增加了()[]{}''""符号辅助添加的功能
 *      - 增加了自动退格功能，跟idea类似
 *      - 高亮中添加了canDivided属性(配置文件中以单独~号标识)，即是否可被分割，在todo和action中均加入了中断高亮的判断
 *        不过在action中，不可分割属性目前只适用于PART类型的高亮，如''号。解决了在注释中的'号造成高亮混乱的问题。
 *      - 增加了背景颜色的高亮
 *      - 丰富状态栏提示信息
 *      - 查找替换 界面靠右上 - 每次打开都是
 * >2.3 - 去掉了文件打开器的文件过滤
 *      - 记录上次打开文件路径(3.1新增)
 *      - 加入了快捷注释//功能(3.1新增)
 *      - 查找支持了正则(3.0新增)
 *      - 在Opener中加入了用不同字符集读取文件的功能
 *      - 选择高亮延迟问题优化
 *
 * >2.4 - 增加窗口快捷缩放移动功能
 *      - 统一版本号常量
 *      - 改变单双引号的自动跳过策略
 *      - 将删除方法改为选中某一行
 *      - 增加“重新载入”功能
 *      (2.43)
 *      - 增加底部行列计数和编码显示
 *      - 增加底部字数显示
 *      - 增加打印接口 Ctrl+P
 *      - 增加使用百度搜索
 */
/**
 * BUG
 * 2.0  #在打开一个文件后，用输入法打字，进行撤销时会混乱甚至卡住。...
 * 2.1  不高亮问题 - 少了个!号 - 已解决
 * 2.2  修复了首次打开文件会每行多一个回车的bug，具体在Opener
 * 2.2  查找时焦点可以移到替换框内，并用快捷键进行替换 - 已解决
 * 2.2  在代码模式下，选中一段内容后，触发自动生成的键，生成的内容不会替换所选内容，甚至出BUG。- 已解决
 * 2.2  背景高亮只会在有前景样式时显示 - 已解决
 * 2.3  高亮文件夹缺失时打不开 - 已解决
 * 2.3  修复点“无”时勾会消失的bug
 * 2.4  每次打开文件里面多余的空行会消失 - 某种回车方式无法读取 - 已解决(2.43)
 * 2.42 目前最严重的的问题就是在打开高亮的情况下，文本变动（尤其是长文本）经常会出现 Illegal cast to MutableAttributeSet
 *      的报错。我估计原因就是setText的线程和highlighter线程冲突了。高亮应该永远在文本变动后开始，这里可能需要线程同步。
 *      - 已部分解决：
 *      我在SimpleHighlighter的部分highlight方法里添加了sleep(2)，不跟setTest抢，居然不报错了，就差这2ms，
 *      目前测试打开100KB的java文件不报错，并且2ms的高亮延时用户也感觉不到，但这个方法终究不是长久之计，最优解还是线程同步。
 *      - 已解决：(线程同步)
 *      归根结底是SimpleHighlighter和MyTextPane的高亮和setText方法在争夺Document对象，所以在这两个方法里加入了对这个对象的
 *      同步代码块，便不报错了。
 * 2.42 #但发现一个新问题，就是生成一对符号的时候，有时候highlighter的action方法里会报空指针或ConcurrentModificationException
 * 2.42 没有实现真正的不自动换行。 - 已解决
 * 2.42 在开启高亮的情况下新建文件，此时高亮器就会一直开启，点无也关不掉 - 已解决(pauseHlt)
 * 2.42 在开启高亮的情况下，不论是否有高亮内容，不论是否强制折行，只要一行的内容超出长度了，此时在这行的上面打字，下面会莫名地多空格 - 已解决
 * 2.42 #在较大文件中，开启高亮时，使用注释快捷键反应较慢，并且若长按快捷键会报错崩溃 - 已部分解决
 * 2.42 #现在还会有关于高亮的BUG，是因为目前只是实现了对Document的互斥操作，而没有实现优先文本变动，随后再高亮的同步逻辑
 */
public class AppFunc {
    public EditWin editWin;
    private Document document;
    private UndoManager undo;
    private boolean hasReset = false;
    //高亮线程
    private Thread t_highlight;
    //高亮设置文件
    private String highlightSettingName = DTUtil.getHighlightName();
    //暂停高亮响应
    private boolean pauseHlt = false;
    //右键菜单
    private JPopupMenu popup;
    private MyMenuItem iCopy, iPaste, iCut, iDelete, iSelectAll, iFomart;
    /*菜单事件*/
    public static final int OPEN = 1;
    public static final int SAVE = 2;
    public static final int SAVE_ANOTHER = 3;
    public static final int FONT = 4;
    public static final int NEW = 5;
    public static final int ABOUT = 6;
    public static final int COUNT = 7;
    public static final int NOTES = 8;
    public static final int FIND = 9;
    public static final int REPLACE = 10;
    public static final int PRINT = 11;
    public AppFunc(EditWin editWin){
        this.editWin = editWin;
        undo = new UndoManager();
        iCopy = new MyMenuItem("复制(C)");
        iPaste = new MyMenuItem("粘贴(V)");
        iCut = new MyMenuItem("剪切(X)");
        iDelete = new MyMenuItem("删除(D)");
        iSelectAll = new MyMenuItem("全部选中(A)");
        iFomart = new MyMenuItem("CSS格式化");
        popup = new JPopupMenu();
        popup.add(iCut);
        popup.add(iCopy);
        popup.add(iPaste);
        popup.add(iDelete);
        popup.addSeparator();
        popup.add(iSelectAll);
        popup.add(iFomart);
        //这里必须要是area去add
        editWin.getTextPane().add(popup);
        addHandler();
        addListener();
    }
    //处理菜单事件
    public void menuDeal(int event){
        /* 才发现多线程会让同一个方法效果不一样！
         * 在另一个线程中执行open，如果open中只有光标置前的方法话，主线程的滚动条并不会跟着光标走，所以需要设置滚动条
         */
        new Thread(){
            @Override
            public void run() {
                if(event == OPEN)
                    open(null);
                else if(event == SAVE)
                    save();
                else if(event == SAVE_ANOTHER)
                    saveAnother();
                else if(event == FONT)
                    chooseFont();
                else if(event == NEW)
                    newOne();
                else if(event == ABOUT)
                    about();
                else if(event == COUNT)
                    count();
                else if(event == NOTES)
                    notes();
                else if(event == FIND)
                    find();
                else if(event == REPLACE)
                    replace();
                else if(event == PRINT)
                    print();
            }
        }.start();
    }

    //使用百度搜索
    private void searchBaidu(){
        String search;
        String seleted = editWin.getTextPane().getSelectedText();
        if(seleted != null){
            search = seleted;
        }else{
            search = editWin.getTextPane().getText();
        }

        try {
            Desktop.getDesktop().browse(URI.create("https://baidu.com/s?wd="+search));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //打印
    private void print(){
        try {
            editWin.getTextPane().print();
        } catch (PrinterException e) {
            e.printStackTrace();
        }
    }

    //代码模式
    private void onCodeModel(){
        if(!editWin.getTextPane().getCodeMode()){
            editWin.getTextPane().setCodeMode(true);
            DTUtil.setCodeMode(true);
            editWin.getiCode().setState(true);
            editWin.changeStatus("代码模式");
        }else{
            editWin.getTextPane().setCodeMode(false);
            DTUtil.setCodeMode(false);
            editWin.getiCode().setState(false);
            editWin.changeStatus("退出代码模式");
        }
    }
    //查找
    private void find(){
        FindAndReplace.getInstance("find", editWin);
    }
    //替换
    private void replace(){
        FindAndReplace.getInstance("replace", editWin);
    }
    //准备高亮
    public void prepareHighlight(){
        if(editWin.getFilePath() == null)
            return;
        File nowFile = new File(editWin.getFilePath());
        if(nowFile == null)
            return;
        String nowFileName = nowFile.getName();
        editWin.prepareHighlight(highlightSettingName, nowFileName.substring(nowFileName.lastIndexOf('.'), nowFileName.length()));
    }
    //高亮
    public void highlight(){
        if(!pauseHlt)
            editWin.highlight();
    }
    //先消除样式的高亮
    public void highlight(int offset, int length){
        if(!pauseHlt)
            editWin.highlight(offset, length);
    }
    //高亮线程工作
    public void onHighlight(int offset, int length){
        //滤掉冗余的情况
        if(editWin.getTextPane().getSHighlighter() == null  ||
                !editWin.getTextPane().getSHighlighter().hasPrepared()){
            return;
        }
        //在这里进行了文本变动与高亮的同步
        synchronized (editWin.getTextPane().getDocument()) {
            if (t_highlight != null && t_highlight.isAlive())
                t_highlight.stop();
            t_highlight = new Thread() {
                @Override
                public void run() {
                    highlight(offset, length);
                }
            };
            t_highlight.start();
        }
    }
    //复制
    public void copy(){
        if(editWin.getTextPane().getSelectedText() == null){//没有选中文字
            editWin.showStatus("请先选中内容！");
            return;
        }
        // 获取系统剪贴板
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        // 封装文本内容
        Transferable trans = new StringSelection(editWin.getTextPane().getSelectedText());
        // 把文本内容设置到系统剪贴板
        clipboard.setContents(trans, null);
        editWin.changeStatus("已复制");
    }
    //粘贴
    public void paste(){
        // 获取系统剪贴板
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        // 获取剪贴板中的内容
        Transferable trans = clipboard.getContents(null);
        if (trans != null) {
            // 判断剪贴板中的内容是否支持文本
            if (trans.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                try {
                    // 获取剪贴板中的文本内容
                    String text = (String) trans.getTransferData(DataFlavor.stringFlavor);
                    //这里要先删除选中内容
                    cut();
                    //插入
                    editWin.getTextPane().insert(text, editWin.getTextPane().getCaretPosition());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    //剪切
    public void cut(){
        if(editWin.getTextPane().getSelectedText() == null){//没有选中文字
            editWin.showStatus("请先选中内容！");
            return;
        }
        copy();
        editWin.getTextPane().replaceRange("", editWin.getTextPane().getSelectionStart(), editWin.getTextPane().getSelectionEnd());
        editWin.changeStatus("已剪切");
    }
    //选中某一行
    public void choose(){
        editWin.getTextPane().chooseLine();
    }
    //笔记
    public void notes(){
        new Notes(this);
    }
    //字数统计
    public void count(){
        new Counter(editWin);
    }
    //关于
    public void about(){
        new About(editWin);
    }
    //新建
    public void newOne(){
        if(contentChange()){
            int option = JOptionPane.showConfirmDialog(editWin, "是否保存文档？");
            if(option == JOptionPane.OK_OPTION){
                new Saver(editWin);
            }else if(option == JOptionPane.NO_OPTION){
            }else{
                return;
            }
        }
        //暂停高亮响应
        pauseHlt = true;
        //新建界面
        editWin.reBegin();
        //撤销器重置
        undo.discardAllEdits();
    }
    //打开
    public void open(File file){
        if(contentChange()){
            int option = JOptionPane.showConfirmDialog(editWin, "是否保存文档？");
            if(option == JOptionPane.OK_OPTION){
                new Saver(editWin);
            }else if(option == JOptionPane.NO_OPTION){
            }else{
                return;
            }
        }
        //传进了文件
        if(file != null){
            new Opener(editWin, file, DTUtil.getCharset());
            afterOpen();
            return;
        }
        //自选文件
        new Opener(editWin, DTUtil.getCharset());
        afterOpen();
    }
    //打开之后的操作
    public void afterOpen(){
        //开启高亮响应
        pauseHlt = false;
        //准备高亮
        prepareHighlight();
        //高亮
        highlight();
        //撤销器重置
        undo.discardAllEdits();
    }
    //保存
    public void save(){
        new Saver(editWin);
        //开启高亮响应
        pauseHlt = false;
        prepareHighlight();
        highlight();
    }
    //另存为
    public void saveAnother(){
        new Saver(editWin, null);
        //开启高亮响应
        pauseHlt = false;
        prepareHighlight();
    }
    //选择字体
    public void chooseFont(){
        new FontChooser().showChooser(editWin);
    }
    public void closing(){
        if(contentChange()){
            //有改动
            int option = JOptionPane.showConfirmDialog(editWin, "是否保存文档？");
            if(option == JOptionPane.OK_OPTION){
                new Saver(editWin);
            }else if(option == JOptionPane.NO_OPTION){
            }else{
                return;
            }
        }
        exit();
    }
    //退出
    public void exit(){
        //如果没有点重置，那么就保存以下设置
        if(!hasReset) {
            //保存是否最大化
            if(editWin.getExtendedState()==JFrame.MAXIMIZED_BOTH){//最大化了
                DTUtil.setMaxFrame(true);
            }else{
                DTUtil.setMaxFrame(false);
                DTUtil.setX(editWin.getX());
                DTUtil.setY(editWin.getY());
                DTUtil.setWidth(editWin.getWidth());
                DTUtil.setHeight(editWin.getHeight());
            }
        }
        editWin.closeAnimation();
        System.exit(0);
    }
    //重置
    public void reset(){
        editWin.getTextPane().setFont(new Font(FontChooser.fontsName[DTUtil.getFontIndex()], DTUtil.getStyleIndex(), DTUtil.getFontSize()+10));//加数字
        editWin.getiLineWrap().setState(DTUtil.getLineWrap());
    }
    //文本变动
    public void textChange(){
        //editWin.changeStatus("就绪");
        //是否改动
        if(editWin.getContent() != null){//content等于null代表目前没有打开任何已存在文件
            if(contentChange()){
                editWin.setTitle("*"+editWin.getFilePath()+" - 记事本");
            }else{
                editWin.setTitle(editWin.getFilePath()+" - 记事本");
            }
        }
        editWin.textChange();
    }
    //内容是否变动
    public boolean contentChange(){
        //这里如果按以前的代码的话，一样的内容getText()和content里的竟然不一样，好像是因为textPane里的回车是\r\n
        if(editWin.getContent()==null && !editWin.getTextPane().getText().equals("") ||
                editWin.getContent()!=null && !deREquals(editWin.getContent(), editWin.getTextPane().getText())){//这里忽略了\r
            return true;
        }
        return false;
    }
    //忽略\r的判等
    public boolean deREquals(String str1, String str2){
        //先把\r都去掉
        str1 = str1.replaceAll("\r", "");
        str2 = str2.replaceAll("\r", "");
        int len1 = str1.length();
        int len2 = str2.length();
        if(len1 != len2)
            return false;
        for(int i = 0; i < len1; i++){
            if(str1.charAt(i) != str2.charAt(i))
                return false;
        }
        return true;
    }
    public void addHandler(){
        //为文本框添加数据传输器（拖拽功能）
        //实现后，swing原有的支持剪切、复制和粘贴的键盘绑定的功能会失效，只需自己监听即可
        editWin.getTextPane().setTransferHandler(new TransferHandler(){
            @Override
            public boolean importData(JComponent comp, Transferable t) {
                try {
                    //这里加判断是因为实现拖拽功能后再进行粘贴等键盘操作时会出异常，而异常就是UnsupportedDataFlavor
                    if(!t.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
                        return false;
                    Object o = t.getTransferData(DataFlavor.javaFileListFlavor);
                    List list = (List) o;//文件列表
                    open(new File(list.get(0).toString()));//只取第一个文件
                    return true;
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }
            @Override
            public boolean canImport(JComponent comp, DataFlavor[] flavors) {
                for (int i = 0; i < flavors.length; i++) {
                    if (DataFlavor.javaFileListFlavor.equals(flavors[i])) {
                        return true;
                    }
                }
                return false;
            }
        });
    }
    //文本监听，之所以单独列出来是因为换行策略更改后document会随之变化，之前的监听器将失效，需要再次注册
    //这里每次开始新的高亮线程之前都停止之前的线程，保证了同一时间内只有一个高亮线程
    public void docListen(){
        editWin.cursorChange();
        document = editWin.getTextPane().getDocument();
        document.addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                textChange();
                onHighlight(e.getOffset(), e.getLength());
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                textChange();
                onHighlight(e.getOffset(), e.getLength());
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                //这个bug让我找了好长时间，尽量不用这个方法，否则有可能无限循环
            }
        });
    }
    public void addListener(){
        //文本监听
        docListen();
        //菜单监听
        editWin.getiOpen().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                menuDeal(OPEN);
            }
        });
        editWin.getiSave().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                menuDeal(SAVE);
            }
        });
        editWin.getiSaveAnother().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                menuDeal(SAVE_ANOTHER);
            }
        });
        editWin.getiFont().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                menuDeal(FONT);
            }
        });
        editWin.getiLineWrap().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(editWin.getiLineWrap().getState() == true){//被勾选了
                    editWin.getTextPane().setLineWrap(true);
                    DTUtil.setLineWrap(true);
                }else {
                    editWin.getTextPane().setLineWrap(false);
                    DTUtil.setLineWrap(false);
                }
                //再次注册监听器
                docListen();
                //准备高亮
                prepareHighlight();
            }
        });
        editWin.getiReset().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DTUtil.initFile();
                reset();
                hasReset = true;
                editWin.showStatus("已恢复所有默认设置");
            }
        });
        editWin.getiNew().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                menuDeal(NEW);
            }
        });
        editWin.getiAbout().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                menuDeal(ABOUT);
            }
        });
        editWin.getiCount().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                menuDeal(COUNT);
            }
        });
        editWin.getiDate().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                editWin.getTextPane().append(new SimpleDateFormat("yyyy/MM/dd").format(new Date()));
            }
        });
        editWin.getiNote().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                menuDeal(NOTES);
            }
        });
        editWin.getiFind().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                menuDeal(FIND);
            }
        });
        editWin.getiReplace().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                menuDeal(REPLACE);
            }
        });
        editWin.getiCode().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //代码模式
                onCodeModel();
            }
        });
        editWin.getiReOpen().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String nowPath = editWin.getFilePath();
                if(nowPath != null)
                    open(new File(nowPath));
            }
        });
        editWin.getiPrint().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                menuDeal(PRINT);
            }
        });
        editWin.getiBaidu().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchBaidu();
            }
        });
        iCopy.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                copy();
            }
        });
        iPaste.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                paste();
            }
        });
        iCut.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cut();
            }
        });
        iDelete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                choose();
            }
        });
        iSelectAll.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                editWin.getTextPane().selectAll();
            }
        });
        iFomart.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                editWin.format();
            }
        });
        //窗口监听
        editWin.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                //关闭
                closing();
            }
        });
        //键盘监听
        editWin.getTextPane().addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                boolean ctrl = e.isControlDown();
                boolean shift = e.isShiftDown();
                boolean alt = e.isAltDown();
                if(ctrl && e.getKeyCode() == KeyEvent.VK_S) {//Ctrl组合键的写法
                    save();
                }else if(ctrl && e.getKeyCode() == KeyEvent.VK_O) {
                    open(null);
                }else if(ctrl && shift && e.getKeyCode() == KeyEvent.VK_A) { //ctrl+shift+A
                    saveAnother();
                }else if(ctrl && e.getKeyCode() == KeyEvent.VK_T) {
                    chooseFont();
                }else if(ctrl && e.getKeyCode() == KeyEvent.VK_N) {
                    newOne();
                }else if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    closing();
                }else if(ctrl && e.getKeyCode() == KeyEvent.VK_X) {
                    cut();
                }else if(ctrl && e.getKeyCode() == KeyEvent.VK_C) {
                    copy();
                }else if(ctrl && e.getKeyCode() == KeyEvent.VK_V) {
                    paste();
                }else if(shift && e.getKeyCode() == KeyEvent.VK_ENTER) {//快速换行
                    editWin.quickWrap();
                }else if(ctrl && !shift && e.getKeyCode() == KeyEvent.VK_Z){
                    //撤销
                    if(undo.canUndo()) {
                        undo.undo();
                    }
                }else if(ctrl && shift && e.getKeyCode() == KeyEvent.VK_Z){
                    //回撤
                    if(undo.canRedo()) {
                        undo.redo();
                    }
                }else if(ctrl && e.getKeyCode() == KeyEvent.VK_D){
                    choose();
                }else if(e.getKeyCode() == KeyEvent.VK_F1){
                    count();
                }else if(e.getKeyCode() == KeyEvent.VK_F2){
                    editWin.getTextPane().append(new SimpleDateFormat("yyyy/MM/dd").format(new Date()));
                }else if(e.getKeyCode() == KeyEvent.VK_F3){
                    notes();
                }else if(ctrl && e.getKeyCode() == KeyEvent.VK_F12){
                    //测试键
                    editWin.getTextPane().test();
                }else if(ctrl && e.getKeyCode() == KeyEvent.VK_F){
                    find();
                }else if(ctrl && e.getKeyCode() == KeyEvent.VK_R) {
                    replace();
                }else if(ctrl && e.getKeyCode() == KeyEvent.VK_BACK_SLASH) {
                    onCodeModel();
                }else if(ctrl && e.getKeyCode() == KeyEvent.VK_P) {
                    print();
                }else if(ctrl && e.getKeyCode() == KeyEvent.VK_E) {
                    searchBaidu();
                }else if(ctrl && e.getKeyCode() == KeyEvent.VK_UP){
                    e.consume();
                    editWin.setSize(editWin.getWidth(), editWin.getHeight()+12);
                    editWin.setLocation(editWin.getX(), editWin.getY()-6);
                }else if(ctrl && e.getKeyCode() == KeyEvent.VK_DOWN){
                    e.consume();
                    editWin.setSize(editWin.getWidth(), editWin.getHeight()-12);
                    editWin.setLocation(editWin.getX(), editWin.getY()+6);
                }else if(ctrl && e.getKeyCode() == KeyEvent.VK_LEFT){
                    e.consume();
                    editWin.setSize(editWin.getWidth()-12, editWin.getHeight());
                    editWin.setLocation(editWin.getX()+6, editWin.getY());
                }else if(ctrl && e.getKeyCode() == KeyEvent.VK_RIGHT){
                    e.consume();
                    editWin.setSize(editWin.getWidth()+12, editWin.getHeight());
                    editWin.setLocation(editWin.getX()-6, editWin.getY());
                }else if(alt && e.getKeyCode() == KeyEvent.VK_UP){
                    editWin.setLocation(editWin.getX(), editWin.getY()-10);
                }else if(alt && e.getKeyCode() == KeyEvent.VK_DOWN){
                    editWin.setLocation(editWin.getX(), editWin.getY()+10);
                }else if(alt && e.getKeyCode() == KeyEvent.VK_LEFT){
                    editWin.setLocation(editWin.getX()-10, editWin.getY());
                }else if(alt && e.getKeyCode() == KeyEvent.VK_RIGHT){
                    editWin.setLocation(editWin.getX()+10, editWin.getY());
                }

            }
            @Override
            public void keyReleased(KeyEvent e) {
                //这里调用不会有先后问题
                editWin.cursorChange();

            }
        });
        //鼠标监听
        editWin.getTextPane().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                editWin.cursorChange();
                //右键
                if(e.getButton() == MouseEvent.BUTTON3){
                    popup.show(editWin.getTextPane(), e.getX(), e.getY());
                }
            }
        });
        //焦点监听
        editWin.getTextPane().addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                highlight();
            }
            @Override
            public void focusLost(FocusEvent e) {
            }
        });
        //高亮菜单监听
        for(JCheckBoxMenuItem item : editWin.getHighlightItems()){
            item.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    highlightSettingName = item.getLabel();
                    DTUtil.setHighlightName(highlightSettingName);
                    //其他的取消勾
                    for(JCheckBoxMenuItem other : editWin.getHighlightItems()){
                        other.setState(false);
                    }
                    item.setState(true);
                    //“无”取消
                    editWin.getiNoHL().setState(false);
                    new Thread(){
                        @Override
                        public void run() {
                            prepareHighlight();
                            highlight();
                        }
                    }.start();
                }
            });
        }
        editWin.getiNoHL().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                highlightSettingName = null;
                DTUtil.setHighlightName(null);
                //其他的取消勾
                for(JCheckBoxMenuItem other : editWin.getHighlightItems()){
                    other.setState(false);
                }
                editWin.getiNoHL().setState(true);//自己不取消
                prepareHighlight();
                editWin.getTextPane().defaultView();
            }
        });
        //编码项监听
        for(JCheckBoxMenuItem item : editWin.getCharsetItems()){
            item.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    DTUtil.setCharset(item.getLabel());
                    if(editWin.getFilePath() != null) {
                        new Thread(){
                            @Override
                            public void run() {
                                pauseHlt = true;//高亮先暂停
                                open(new File(editWin.getFilePath()));
                                highlight();
                            }
                        }.start();
                    }
                    for(JCheckBoxMenuItem other : editWin.getCharsetItems()){
                        other.setState(false);
                    }
                    item.setState(true);
                }
            });
        }
        //撤销监听
        editWin.getTextPane().getDocument().addUndoableEditListener(new UndoableEditListener() {
            @Override
            public void undoableEditHappened(UndoableEditEvent e) {
                //只撤销添加和删除操作
                if(e.getEdit().getPresentationName().equals("添加") || e.getEdit().getPresentationName().equals("删除"))
                    undo.addEdit(e.getEdit());
            }
        });
    }
}
