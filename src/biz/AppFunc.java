package biz;
import util.CompFactory;
import util.DTUtil;
import util.JavaUtil;
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
import java.util.concurrent.CountDownLatch;
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
 *      - 增加用XML方式读取高亮配置
 *
 * >2.50 - 加入运行功能
 *       - 加入文件编码类型判断功能，Opener会自己判断文件编码并打开，这样的话Setting里的charset就是默认编码了
 *       - 加入了当前编码菜单，读取文件时会关联这个菜单，默认编码是新建文件时用的
 *       - 首次使用MyNotepad无需手动配置
 *       - 增加使用谷歌翻译
 *       - 菜单栏菜单项组件快捷键绑定
 *       - 改变了复制和剪切的逻辑
 *       - 加入了文本克隆功能和快捷键
 *
 * >2.51 - 加入显示行号功能
 *       - 重构了Appfunc高亮部分的代码，让每次高亮都保证只有一个线程，打开文件终于不会高亮不全了
 *       - 增加了计时器功能
 *       - 增加选中TAB功能
 *       - Opener打开文件采用StringBuilder，极大提升打开速度
 *
 * >2.52 - 提供对Python的支持
 * >2.53 - 增加自动补全功能
 *
 */
/**
 * BUG
 * 2.0  #在打开一个文件后，用输入法打字，进行撤销时会混乱甚至卡住。... (加了同步，部分解决)
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
 * 2.42 没有实现真正的不自动换行。 - 已解决(2.43)
 * 2.42 在开启高亮的情况下新建文件，此时高亮器就会一直开启，点无也关不掉 - 已解决(pauseHlt)(2.43)
 * 2.42 在开启高亮的情况下，不论是否有高亮内容，不论是否强制折行，只要一行的内容超出长度了，此时在这行的上面打字，下面会莫名地多空格 - 已解决(2.43)
 * 2.42 #在较大文件中，开启高亮时，使用注释快捷键反应较慢，并且若长按快捷键会报错崩溃 - 已部分解决(2.43)
 * 2.42 #现在还会有关于高亮的BUG，是因为目前只是实现了对Document的互斥操作，而没有实现优先文本变动，随后再高亮的同步逻辑
 *       对于这个问题，我目前已经解决了打开文件时高亮BUG，用的是Count锁。之前打开文件(尤其是换文件打开)时会出错是因为，setText()
 *       方法内部会调用多次insertString的方法，导致document变动后启动高亮，导致冲突，用了Count锁之后，会让所有在未打开文件时就
 *       开启的高亮线程阻塞或取消，便不会冲突了。
 *       现在还剩文本变动与高亮的冲突没解决，设想将键盘事件全部阻隔，由自己调用insertString方法，这样就知道什么时候插入完成 - 行不通
 *       目前还是在用 synchronized ()
 * 2.43 复制一段文字后，选中内容直接粘贴替换，这时又复制了被替换的内容 - 已解决
 * 2.43 打开文件后不高亮或高亮不完全 - BUG在焦点监听部分，两个高亮线程冲突 - 已解决(去掉了焦点监听，只会添乱)
 * 2.43 自动换行打开/取消的时候没有重新高亮 - 已解决
 * 2.50 无法输入GBK或UTF8编码的内容，导致奇怪的乱码 (2.50之前无法输入UTF8，2.50无法输入GBK ？？？) - 已解决(Saver处)
 * 2.50 打开窗口首次保存文件每行会多出一个回车 - 已解决(\r去掉)
 * 2.50 笔记界面有时会莫名其妙无法删除和重命名新建的文件 - 已解决(读写文件以后如果不想一直占用，一定要及时关闭流)
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
    //是否刚刚打开文件
    private boolean justOpened = true;
    //右键菜单
    private JPopupMenu popup;
    private JMenuItem iCopy, iPaste, iCut, iClone, iSelectAll, iFomart;
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
    public static final int TIMER = 12;
    public AppFunc(EditWin editWin){
        this.editWin = editWin;
        undo = new UndoManager();
        //弹出菜单用不了快捷键。。。
        iCopy = CompFactory.createMenuItem("复制(C)");
        iPaste = CompFactory.createMenuItem("粘贴(V)");
        iCut = CompFactory.createMenuItem("剪切(X)");
        iClone = CompFactory.createMenuItem("克隆(D)");
        iSelectAll = CompFactory.createMenuItem("全部选中(A)");
        iFomart = CompFactory.createMenuItem("CSS格式化");
        popup = new JPopupMenu();
        popup.add(iCut);
        popup.add(iCopy);
        popup.add(iPaste);
        popup.add(iClone);
        popup.addSeparator();
        popup.add(iSelectAll);
        popup.add(iFomart);
        //这里必须要是area去add
        editWin.getTextPane().add(popup);
        addHandler();
        addListener();
        //其他初始化
        Notes.initPath();
    }
    //处理菜单事件
    private void menuDeal(int event){
        /* 才发现多线程会让同一个方法效果不一样！
         * 在另一个线程中执行open，如果open中只有光标置前的方法话，主线程的滚动条并不会跟着光标走，所以需要设置滚动条
         */
        new Thread(){
            @Override
            public void run() {
                if(event == OPEN)
                    open(null, null);
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
                else if(event == TIMER)
                    MyTimer.showTimer(editWin);
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
    //使用谷歌翻译
    private void googleTranslate(){
        String search;
        String seleted = editWin.getTextPane().getSelectedText();
        if(seleted != null){
            search = seleted;
        }else{
            search = editWin.getTextPane().getText();
        }

        try {
            Desktop.getDesktop().browse(URI.create("https://translate.google.cn/?text="+search));
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
        editWin.prepareHighlight(highlightSettingName, nowFileName.substring(nowFileName.lastIndexOf('.')));
    }


    //高亮
    public void highlight(){
        if(!pauseHlt) {
            //这个锁不如当计数器用。。
            if(JavaUtil.setTextLatch.getCount() != 0){
                return;
            }
            if (t_highlight != null && t_highlight.isAlive())
                t_highlight.stop();
            t_highlight = new Thread() {
                @Override
                public void run() {
                    editWin.highlight();
                    if(justOpened) { //改页脚只在刚打开文件并高亮后显示
                        editWin.changeStatus("就绪");
                        editWin.showStatus("高亮完成");
                        justOpened = false;
                    }
                }
            };
            t_highlight.start();
        }
    }
    //区域高亮
    public void highlight(int offset, int length, String type){
        if(!pauseHlt) {
            //滤掉冗余的情况
            if(editWin.getTextPane().getSHighlighter() == null  ||
                    !editWin.getTextPane().getSHighlighter().hasPrepared()){
                return;
            }
            //这个锁不如当计数器用。。
            if(JavaUtil.setTextLatch.getCount() != 0){
                return;
            }
            if (t_highlight != null && t_highlight.isAlive())
                t_highlight.stop();
            t_highlight = new Thread() {
                @Override
                public void run() {
                    editWin.highlight(offset, length, type);
                }
            };
            t_highlight.start();
        }
    }

    //复制
    public void copy(){
        String content = editWin.getTextPane().getSelectedText();
        if(content == null){//没有选中文字，复制一行
            content = editWin.getTextPane().getLine();
        }
        // 获取系统剪贴板
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        // 封装文本内容
        Transferable trans = new StringSelection(content);
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
                    delete();
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
        copy();
        if(editWin.getTextPane().getSelectedText() == null){//没有选中文字
            editWin.getTextPane().removeLine();//删去一行
        }else {
            editWin.getTextPane().replaceRange("", editWin.getTextPane().getSelectionStart(), editWin.getTextPane().getSelectionEnd());
        }
        editWin.changeStatus("已剪切");
    }
    //克隆一行/一段
    public void cloneText(){
        String content = editWin.getTextPane().getSelectedText();
        int insertPos = editWin.getTextPane().getSelectionEnd();
        if(content == null){//没有选中文字，一行
            content = editWin.getTextPane().getLine();
            insertPos = editWin.getTextPane().getLineEnd(-1);
        }
        editWin.getTextPane().justInsert(content, insertPos);
        editWin.getTextPane().setSelectionStart(insertPos);
        editWin.getTextPane().setSelectionEnd(insertPos+content.length());
    }
    //删除
    private void delete(){
        if(editWin.getTextPane().getSelectedText() == null){//没有选中文字
            return;
        }
        editWin.getTextPane().replaceRange("", editWin.getTextPane().getSelectionStart(), editWin.getTextPane().getSelectionEnd());
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
                new Saver(1, editWin, editWin.getCurrEncoding());
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
    private CountDownLatch downLatch;
    public void open(File file, String charset){
        if(contentChange()){
            int option = JOptionPane.showConfirmDialog(editWin, "是否保存文档？");
            if(option == JOptionPane.OK_OPTION){
                new Saver(1, editWin, editWin.getCurrEncoding());
            }else if(option == JOptionPane.NO_OPTION){
            }else{
                return;
            }
        }
        //传进了文件
        if(file != null){
            downLatch = new Opener(editWin, file, charset).open();
            afterOpen();
            return;
        }
        //自选文件
        downLatch = new Opener(editWin, charset).open();
        afterOpen();
    }
    //打开之后的操作 - 多线程同步真的烦死了
    public void afterOpen(){
        new Thread(){
            @Override
            public void run() {
                try {
                    downLatch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //标记Python
                if(editWin.getFileType().equals(".py"))
                    editWin.getTextPane().setPyFile(true);
                else
                    editWin.getTextPane().setPyFile(false);
                //准备补全
                editWin.getCompleter().collectWords();

                editWin.changeStatus("正在高亮...");
                //开启高亮响应
                pauseHlt = false;
                justOpened = true;
                //准备高亮
                prepareHighlight();
                //高亮
                highlight();
                //撤销器重置
                undo.discardAllEdits();
            }
        }.start();
    }
    //保存
    public void save(){
        new Saver(1, editWin, editWin.getCurrEncoding());
        //开启高亮响应
        pauseHlt = false;
        prepareHighlight();
        highlight();
    }
    //另存为
    public void saveAnother(){
        new Saver(2, editWin, editWin.getCurrEncoding());
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
                new Saver(1, editWin, editWin.getCurrEncoding());
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
    private void textChange(){
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
    private boolean contentChange(){
        //这里如果按以前的代码的话，一样的内容getText()和content里的竟然不一样，好像是因为textPane里的回车是\r\n
        if(editWin.getContent()==null && !editWin.getTextPane().getText().equals("") ||
                editWin.getContent()!=null && !deREquals(editWin.getContent(), editWin.getTextPane().getText())){//这里忽略了\r
            return true;
        }
        return false;
    }
    //忽略\r的判等
    private boolean deREquals(String str1, String str2){
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
    private void addHandler(){
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
                    open(new File(list.get(0).toString()), null);//只取第一个文件
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
    private void docListen(){
        editWin.cursorChange();
        document = editWin.getTextPane().getDocument();
        document.addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                textChange();
                highlight(e.getOffset(), e.getLength(), "insert");
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                textChange();
                highlight(e.getOffset(), e.getLength(), "remove");
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                //这个bug让我找了好长时间，尽量不用这个方法，否则有可能无限循环
            }
        });
    }
    private void addListener(){
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
                //高亮
                highlight();
            }
        });
        editWin.getiLineNum().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(editWin.getiLineNum().getState() == true){
                    editWin.getPane().setRowHeaderView(new TextLineNumber(editWin.getTextPane()));
                    editWin.getCompleter().setxOffset(50);
                    DTUtil.setShowLineNum(true);
                }else{
                    editWin.getPane().setRowHeaderView(null);
                    editWin.getCompleter().setxOffset(0);
                    DTUtil.setShowLineNum(false);
                }
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
                    open(new File(nowPath), editWin.getCurrEncoding()); //重新载入就不识别了
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
        editWin.getiTranslate().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                googleTranslate();
            }
        });
        editWin.getiTimer().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                menuDeal(TIMER);
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
        iClone.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cloneText();
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
                if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
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
                        synchronized (editWin.getTextPane().getDocument()){
                            undo.undo();
                        }
                    }
                }else if(ctrl && shift && e.getKeyCode() == KeyEvent.VK_Z){
                    //回撤
                    if(undo.canRedo()) {
                        synchronized (editWin.getTextPane().getDocument()){
                            undo.redo();
                        }
                    }
                }else if(ctrl && e.getKeyCode() == KeyEvent.VK_D){
                    cloneText();
                }else if(ctrl && e.getKeyCode() == KeyEvent.VK_F12){
                    //测试键
                    editWin.getTextPane().test();
                }else if(ctrl && e.getKeyCode() == KeyEvent.VK_BACK_SLASH) {
                    onCodeModel();
                }

                //下面是鸡肋功能
                else if(ctrl && e.getKeyCode() == KeyEvent.VK_UP){
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
                editWin.selectedChange();
            }
        });

        /************************************
         * 用于特殊键盘事件的监听
         * 都是屏蔽系统的监听器（或者系统不响应），自己处理
         * 代码模式时生效
         * 原先在MyTextPane里面，现在放在这里更方便管理
         */
        editWin.getTextPane().addKeyListener(new KeyAdapter() {
            MyTextPane tp = editWin.getTextPane();
            /**
             * 自动生成字符
             * 字符最好用typed
             * @param e
             */
            @Override
            public void keyTyped(KeyEvent e) {
                if(!tp.isCodeMode())
                    return;
                char ch = e.getKeyChar();//字符要用这个判断，用code无效
                if(ch == '(') {//  - 记住这里不用判断shift
                    e.consume();
                    tp.insert("(");
                    tp.asynInsert(")");
                }else if(ch == ')'){
                    if(tp.getNextChar().equals(")")){
                        e.consume();
                        tp.offsetFromCare(1);
                    }
                }else if(ch == '{') {//  - 记住这里不用判断shift
                    e.consume();
                    tp.insert("{");
                    tp.asynInsert("}");
                }else if(ch == '}'){
                    if(tp.getNextChar().equals("}")){
                        e.consume();
                        tp.offsetFromCare(1);
                    }
                }else if(ch == '[') {//  - 记住这里不用判断shift
                    e.consume();
                    tp.insert("[");
                    tp.asynInsert("]");
                }else if(ch == ']'){
                    if(tp.getNextChar().equals("]")){
                        e.consume();
                        tp.offsetFromCare(1);
                    }
                }else if(ch == '\'') {//  - 记住这里不用判断shift
                    //简易的判断规则
                    if(!tp.getNextChar().equals("'")) {//去掉了!getPreChar().equals("'") ||
                        e.consume();
                        tp.insert("'");
                        tp.asynInsert("'");
                    }else if(tp.getNextChar().equals("'")){
                        e.consume();
                        tp.offsetFromCare(1);
                    }
                }else if(ch == '"') {//  - 记住这里不用判断shift
                    //简易的判断规则
                    if(!tp.getNextChar().equals("\"")) {//去掉了!getPreChar().equals("\"") ||
                        e.consume();
                        tp.insert("\"");
                        tp.asynInsert("\"");
                    }else if(tp.getNextChar().equals("\"")){
                        e.consume();
                        tp.offsetFromCare(1);
                    }
                }

            }
            @Override
            public void keyPressed(KeyEvent e) {
                if(!tp.isCodeMode())
                    return;
                int code = e.getKeyCode();
                char ch = e.getKeyChar();
                boolean ctrl = e.isControlDown();
                boolean shift = e.isShiftDown();
                //这里的consume方法是销毁这个事件，这样系统就不会再自动添加这个键了
                if(!shift && code == KeyEvent.VK_ENTER) {//自动缩进
                    e.consume();
                    if(editWin.getCompleter().isVisible()){
                        //确定补全
                        editWin.getCompleter().complete();
                    }else {
                        editWin.getCompleter().collectWords();
                        tp.insert("\n");
                        tp.autoIndent(-1, true);
                        //如果是在大括号中间回车，把}放到下一行
                        if (tp.getNextChar().equals("}")) {
                            tp.asynInsert("\n");
                            tp.autoIndent(tp.getCaretPosition() + 1, false);
                        }
                    }
                }else if(!shift && code == KeyEvent.VK_TAB){//Tab键默认4个空格
                    e.consume();
                    if(tp.getSelectedText() == null) {
                        tp.insert(tp.TAB);
                    }else{
                        tp.autoTab(); //一起缩进
                    }
                }else if(shift && code == KeyEvent.VK_TAB){
                    e.consume();
                    tp.autoDeTab();
                }else if(code == KeyEvent.VK_BACK_SPACE){//自动删去前面的空白
                    e.consume();
                    tp.autoBackspace();
                }else if(ctrl && ch == '/'){
                    pauseHlt = true;
                    tp.autoComment();
                    pauseHlt = false;
                    //注释完后再高亮所有行
                    highlight(tp.getLineStart(tp.getSelectionStart()),
                            tp.getLineEnd(tp.getSelectionEnd()-tp.getSelectionStart()), "insert");
                }else if(code == KeyEvent.VK_UP){
                    if(editWin.getCompleter().isVisible()) {
                        e.consume();
                        editWin.getCompleter().pre();
                    }
                }else if(code == KeyEvent.VK_DOWN){
                    if(editWin.getCompleter().isVisible()) {
                        e.consume();
                        editWin.getCompleter().next();
                    }
                }else if(code == KeyEvent.VK_LEFT || code == KeyEvent.VK_RIGHT){
                    if(editWin.getCompleter().isVisible()) {
                        editWin.showCompleter();
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if(!tp.isCodeMode())
                    return;
                int code = e.getKeyCode();
                char ch = e.getKeyChar();
                if(JavaUtil.isWordChar(ch)) {
                    //提示框部分
                    editWin.showCompleter();
                }else if(code == KeyEvent.VK_SPACE){
                    editWin.getCompleter().hidePanel();
                    editWin.getCompleter().collectWords();
                }else if(code == KeyEvent.VK_BACK_SPACE){
                    if(editWin.getCompleter().isVisible()){
                        editWin.showCompleter();
                    }
                    editWin.getCompleter().collectWords();
                }
            }
        });

        editWin.getTextPane().addCaretListener(new CaretListener() {
            @Override
            public void caretUpdate(CaretEvent e) {
                editWin.cursorChange();
            }
        });

        //滑动事件
        editWin.getPane().getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {
            @Override
            public void adjustmentValueChanged(AdjustmentEvent e) {
                if(editWin.getCompleter().isVisible())
                    editWin.showCompleter();
            }
        });
        editWin.getPane().getHorizontalScrollBar().addAdjustmentListener(new AdjustmentListener() {
            @Override
            public void adjustmentValueChanged(AdjustmentEvent e) {
                if(editWin.getCompleter().isVisible())
                    editWin.showCompleter();
            }
        });

        //鼠标监听
        editWin.getTextPane().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                //右键
                if(e.getButton() == MouseEvent.BUTTON3){
                    popup.show(editWin.getTextPane(), e.getX(), e.getY());
                }
                editWin.getCompleter().hidePanel();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                editWin.selectedChange();
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
                            justOpened = true;//当做刚打开
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
                    for(JCheckBoxMenuItem other : editWin.getCharsetItems()){
                        other.setState(false);
                    }
                    item.setState(true);
                }
            });
        }
        //当前编码项监听
        for(JCheckBoxMenuItem item : editWin.getCurrCharsetItems()){
            item.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if(editWin.getFilePath() != null) {
                        new Thread(){
                            @Override
                            public void run() {
                                pauseHlt = true;//高亮先暂停
                                open(new File(editWin.getFilePath()), item.getLabel());
                                highlight();
                            }
                        }.start();
                    }else{ //未命名文件
                        editWin.setCurrEncoding(item.getLabel());
                        editWin.update();
                    }
                    for(JCheckBoxMenuItem other : editWin.getCurrCharsetItems()){
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