package view;
import biz.hlt.SimpleHighlighter;
import util.JavaUtil;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.concurrent.CountDownLatch;

/**
 * 自定义的富文本框 - 为了封装一些功能
 * 有高亮方法可供外界调用
 */
public class MyTextPane extends JTextPane {
    //从一开始就更改Kit
    private EditorKit myKit = new MyStyledEditorKit();
    private AttributeSet defAttribute = this.getInputAttributes();
    private SimpleHighlighter highlighter;
    private boolean isCodeMode = true;//是否是代码模式
    private boolean isWrap = false;//是否自动换行
    public MyTextPane(){
        this.setSelectionColor(new Color(89, 116, 171));
        this.setSelectedTextColor(new Color(247, 247, 247));
        addKeyLisener();
    }

    //Ctrl + F12 测试
    public void test(){
        autoTab();
    }

    //高亮前的准备 - 把设置从文件中读出来以提高效率
    public void prepareHighlight(String settingName, String fileType){
        highlighter = new SimpleHighlighter(this);//每次打开新文件、自动换行后都要重新
        highlighter.prepare(settingName, fileType);
    }
    //高亮
    public void highlight(){
        if(highlighter != null)
            highlighter.highlight();
    }
    public void highlight(int offset, int length){
        if(highlighter != null)
            highlighter.highlight(offset, length);
    }
    public void defaultView(){
        if(highlighter != null)
            highlighter.defaultSetting();
    }


    //设置代码模式
    public void setCodeMode(boolean isCodeMode){
        this.isCodeMode = isCodeMode;
    }
    public boolean getCodeMode(){
        return this.isCodeMode;
    }

    /**
     * 快速换行
     */
    public void quickWrap(){
        int length = 0;
        int pos = this.getCaretPosition();
        String text = this.getText();//一定要去\r
        for(int i = pos; i < text.length(); i++){
            if(text.charAt(i) == '\n')
                break;
            length++;
        }
        insert("\n", pos+length);
        setCaretPosition(pos+length+1);
        autoIndent(-1, true);//自动缩进
    }
    public static final String TAB = "    ";//Tab默认4个空格
    /**
     * 自动缩进 - 逻辑和实现都没问题了
     * 这里只管缩进，不管换行
     *
     * @param offset =-1代表从光标所在位置
     * @param isAnother =true代表第一个非空白字符是{的话就再缩进一个TAB
     */
    public void autoIndent(int offset, boolean isAnother){
        int pos = offset>0 ? offset : this.getCaretPosition();
        String text = this.getText();//一定要去\r
        String indent = "";
        char firstNonBlank = ' ';//记录第一个非空白字符
        boolean flag = false;//标记是否可以记录空白个数
        for(int i = pos-1; i >= 0; i--){
            char ch = text.charAt(i);
            //1.这里的意思是当出现一个非空白字符时，indent就准备累加同时flag=true，一旦出现空白字符就累加
            //2.累加的过程中如果遇到'\n'就说明到了一行的开头，此时indent就是要缩进的空白字符串
            //3.而累加的过程中如果又遇到了非空白字符，证明之前的累加无效，所以再次转到第一步
            if(!JavaUtil.isBlank(ch)){
                if(!flag)
                    firstNonBlank = ch;
                indent = "";
                flag = true;
            }
            if(ch == '\n' && flag) {
                break;
            }
            if(JavaUtil.isBlank(ch) && flag){
                indent += ch;
            }
        }
        insert(indent, pos);
        if(firstNonBlank == '{' && isAnother){//第一个非空白字符是{的话就再缩进一个TAB
            insert(TAB, pos+indent.length());
        }
        //在当前位置插入的时候。系统的光标好像是会自动跟进，所以不用再setpostion了
    }
    /**
     * 得到本行预期的缩进行数，类比autoIndent方法
     */
    private int getExpectedIndent(int offset, boolean isAnother){
        int pos = offset>0 ? offset : this.getCaretPosition();
        String text = this.getText();//一定要去\r
        int indentL = 0;
        char firstNonBlank = ' ';//记录第一个非空白字符
        boolean flag = false;//标记是否可以记录空白个数
        for(int i = pos-1; i >= 0; i--){
            char ch = text.charAt(i);
            if(!JavaUtil.isBlank(ch)){
                if(!flag)
                    firstNonBlank = ch;
                indentL = 0;
                flag = true;
            }
            if(ch == '\n' && flag) {
                break;
            }
            if(JavaUtil.isBlank(ch) && flag){
                indentL++;
            }
        }
        if(firstNonBlank == '{' && isAnother){//第一个非空白字符是{的话就再缩进一个TAB
            indentL += TAB.length();
        }
        return indentL;
    }
    /**
     * 自定义的退格
     */
    public void backSpace(){
        int pos = this.getCaretPosition();
        String preChar = getPreChar();
        String nextChar = getNextChar();
        if(this.getSelectedText() != null){//选中了内容
            replaceRange("", this.getSelectionStart(), this.getSelectionEnd());
        }else if(//优化了效率(2.42)
                preChar.equals("(") && nextChar.equals(")") ||
                preChar.equals("{") && nextChar.equals("}")  ||
                preChar.equals("[") && nextChar.equals("]")  ||
                preChar.equals("'") && nextChar.equals("'")  ||
                preChar.equals("\"") && nextChar.equals("\"")){
            replaceRange("", pos-1,pos+1);
        }else
            replaceRange("", pos-1,pos);
    }
    /**
     * 自动退格 - 如果光标前面一行都空白的话，就删到缩进处
     *         - 如果是成对的中间的话，就都删除 如[]
     */
    public void autoBackspace(){
        if(this.getSelectedText() != null) {//选中了内容
            replaceRange("", this.getSelectionStart(), this.getSelectionEnd());
            return;
        }
        String text = this.getText();//一定要去\r
        int pos = this.getCaretPosition();
        int length = 0;
        int expected = getExpectedIndent(-1, true);
        for(int i = pos-1; i >= 0; i--){
            char ch = text.charAt(i);
            length++;
            if(ch == '\n')
                break;
            if(!JavaUtil.isBlank(ch)) {//普通退格的作用
                backSpace();
                return;
            }
        }
        //别忘了-1
        if(length-1 > expected){
            replaceRange("", pos-(length-expected)+1, pos);
        }else{
            replaceRange("", pos-length, pos);
        }
    }
    /**
     * 在某一行的开始加上...
     * @param str
     */
    private void addAtBeginOfLine(String str, int pos){
        pos = pos == -1 ? getCaretPosition() : pos;//从外界还是光标
        String text = this.getText();
        int i;
        for(i = pos-1; i >= 0; i--){
            if(text.charAt(i) == '\n')
                break;
        }
        justInsert(str, i+1);
    }
    /**
     * 去掉某一行开头的...
     * 空白不算开头
     * @param str 要去掉的
     * @return 是否成功去掉了
     */
    private boolean rmAtBeginOfLine(String str, int pos){
        pos = pos == -1 ? getCaretPosition() : pos;//从外界还是光标
        String text = this.getText();
        int start = 0, end = text.length();//这一行的开始和结尾
        for(int i = pos-1; i >= 0; i--){
            if(text.charAt(i) == '\n'){
                start = i+1;
                break;
            }
        }
        for(int i = pos; i < text.length(); i++){
            if(text.charAt(i) == '\n'){
                end = i;
                break;
            }
        }
        String sub = text.substring(start, end);
        int index = sub.indexOf(str);
        if(index != -1){
            for(int i = index-1; i >= 0; i--){
                if(!JavaUtil.isBlank(sub.charAt(i)))//如果不是开头的话
                    return false;
            }
            //这里注意just
            justReplaceRange("", start+index, start+index+str.length());
            return true;
        }
        return false;
    }
    /**
     * ...是否在某行的开头
     * @param str
     * @param pos
     * @return
     */
    private boolean isAtBeginOfLine(String str, int pos){
        pos = pos == -1 ? getCaretPosition() : pos;//从外界还是光标
        String text = this.getText();
        int start = 0, end = text.length();//这一行的开始和结尾
        for(int i = pos-1; i >= 0; i--){
            if(text.charAt(i) == '\n'){
                start = i+1;
                break;
            }
        }
        for(int i = pos; i < text.length(); i++){
            if(text.charAt(i) == '\n'){
                end = i;
                break;
            }
        }
        String sub = text.substring(start, end);
        int index = sub.indexOf(str);
        if(index != -1){
            for(int i = index-1; i >= 0; i--){
                if(!JavaUtil.isBlank(sub.charAt(i)))//如果不是开头的话
                    return false;
            }
            return true;
        }
        return false;
    }

    /**
     * 从from到to的每一行的首部添加str
     * @param from
     * @param to
     * @param str
     */
    public void addForEachLine(int from, int to, String str){
        if(from > to) {
            int temp = from;
            from = to;
            to = temp;
        }
        String text = this.getText();
        boolean newLine = true;
        for(int i = from; i < to || newLine; i++){
            if(newLine){
                addAtBeginOfLine(str, i);
                i += str.length();
                to += str.length();
                text = this.getText();
            }
            if(i >= to || i < 0 || text.charAt(i) != '\n'){
                newLine = false;
            }else{
                newLine = true;
            }
        }
    }

    /**
     * 从from到to的每一行的首部移除str
     * 移除和添加的逻辑还真不能一样
     * 因为移除涉及到选中位置的问题(遍历时)
     * 解决办法是先将from和to扩展到首尾
     * @param from
     * @param to
     * @param str
     */
    public void rmForEachLine(int from, int to, String str){
        if(from > to) {
            int temp = from;
            from = to;
            to = temp;
        }else if(from == to){
            from --;
        }
        String text = this.getText();
        StringBuilder sb = new StringBuilder();
        while(from >= 0 && text.charAt(from) != '\n') from --;
        while(to < text.length() && text.charAt(to) != '\n') to ++;
        from++;
        boolean newLine = true;
        for(int i = from; i < to || newLine; i++){
            if(newLine){
                rmAtBeginOfLine(str, i);
                to -= str.length();
                text = this.getText();
            }
            if(i >= to || i < 0 || text.charAt(i) != '\n'){
                newLine = false;
            }else{
                newLine = true;
            }
        }

    }

    /**
     * 自动注释
     */
    public void autoComment(){
        if(this.getSelectedText() == null){//无选中
            boolean removed = rmAtBeginOfLine("//", -1);
            if(!removed)//没有//就加上
                addAtBeginOfLine("//", -1);
        }else{//选中
            int start = getSelectionStart();
            int end = getSelectionEnd();
            boolean allCommented = true;//标记是否所有行都已注释，这样的话才去掉
            String text = this.getText();
            for(int i = start; i < end; i++){
                if(text.charAt(i) == '\n' || i == end-1){
                    if(!isAtBeginOfLine("//", i)){
                        allCommented = false;
                        break;
                    }
                }
            }
            if(allCommented){//所有行均注释
                rmForEachLine(start, end, "//");
            }else{
                addForEachLine(start, end, "//");
            }
        }
    }

    /**
     * 自动添加缩进
     */
    public void autoTab(){
        int start = getSelectionStart();
        int end = getSelectionEnd();
        addForEachLine(start, end, TAB);
    }

    /**
     * 自动取消缩进
     */
    public void autoDeTab(){
        int start = getSelectionStart();
        int end = getSelectionEnd();
        rmForEachLine(start, end, TAB);
    }



    /**
     * 得到光标所在行数，从1计数
     * @return
     */
    public int getCursorLine(){
        String text = this.getText();
        int pos = getCaretPosition();
        int line = 0;
        for(int i=0; i<pos; i++){
            if(text.charAt(i) == '\n') line++;
        }
        return line+1;
    }

    /**
     * 得到光标所在列数，从1计数
     * @return
     */
    public int getCursorColumn(){
        String text = this.getText();
        int pos = getCaretPosition();
        int column = 0;
        for(int i=pos-1; i>=0 && text.charAt(i)!='\n'; i--){
            column++;
        }
        return column+1;
    }

    /**
     * 得到总字数(不含空白）
     * @return
     */
    public int getCharCount(){
        return getText().replaceAll("\\s+", "").length();
    }

    /**
     * 得到选中内容的总字数
     * @return
     */
    public int getSelectedCharCount(){
        return getSelectedText().replaceAll("\\s+", "").length();
    }



    /************************************
     * 用于特殊键盘事件的监听
     * 都是屏蔽系统的监听器（或者系统不响应），自己处理
     * 代码模式时生效
     */
    private void addKeyLisener(){
        this.addKeyListener(new KeyAdapter() {
            /**
             * 自动生成字符
             * 字符最好用typed
             * @param e
             */
            @Override
            public void keyTyped(KeyEvent e) {
                if(!isCodeMode)
                    return;
                char ch = e.getKeyChar();//字符要用这个判断，用code无效
                if(ch == '(') {//  - 记住这里不用判断shift
                    e.consume();
                    insert("(");
                    asynInsert(")");
                }else if(ch == ')'){
                    if(getNextChar().equals(")")){
                        e.consume();
                        offsetFromCare(1);
                    }
                }else if(ch == '{') {//  - 记住这里不用判断shift
                    e.consume();
                    insert("{");
                    asynInsert("}");
                }else if(ch == '}'){
                    if(getNextChar().equals("}")){
                        e.consume();
                        offsetFromCare(1);
                    }
                }else if(ch == '[') {//  - 记住这里不用判断shift
                    e.consume();
                    insert("[");
                    asynInsert("]");
                }else if(ch == ']'){
                    if(getNextChar().equals("]")){
                        e.consume();
                        offsetFromCare(1);
                    }
                }else if(ch == '\'') {//  - 记住这里不用判断shift
                    //简易的判断规则
                    if(!getNextChar().equals("'")) {//去掉了!getPreChar().equals("'") ||
                        e.consume();
                        insert("'");
                        asynInsert("'");
                    }else if(getNextChar().equals("'")){
                        e.consume();
                        offsetFromCare(1);
                    }
                }else if(ch == '"') {//  - 记住这里不用判断shift
                    //简易的判断规则
                    if(!getNextChar().equals("\"")) {//去掉了!getPreChar().equals("\"") ||
                        e.consume();
                        insert("\"");
                        asynInsert("\"");
                    }else if(getNextChar().equals("\"")){
                        e.consume();
                        offsetFromCare(1);
                    }
                }
            }
            @Override
            public void keyPressed(KeyEvent e) {
                if(!isCodeMode)
                    return;
                int code = e.getKeyCode();
                char ch = e.getKeyChar();
                boolean ctrl = e.isControlDown();
                boolean shift = e.isShiftDown();
                //这里的consume方法是销毁这个事件，这样系统就不会再自动添加这个键了
                if(!shift && code == KeyEvent.VK_ENTER) {//自动缩进
                    e.consume();
                    insert("\n");
                    autoIndent(-1, true);
                    //如果是在大括号中间回车，把}放到下一行
                    if(getNextChar().equals("}")){
                        asynInsert("\n");
                        autoIndent(getCaretPosition()+1, false);
                    }
                }else if(!shift && code == KeyEvent.VK_TAB){//Tab键默认4个空格
                    e.consume();
                    if(getSelectedText() == null) {
                        insert(TAB);
                    }else{
                        autoTab(); //一起缩进
                    }
                }else if(shift && code == KeyEvent.VK_TAB){
                    e.consume();
                    autoDeTab();
                }else if(code == KeyEvent.VK_BACK_SPACE){//自动删去前面的空白
                    e.consume();
                    autoBackspace();
                }else if(ctrl && ch == '/'){
                    autoComment();
                }
            }
        });
    }
    /**************准备工作**************/
    //自动换行
    public void setLineWrap(boolean lineWrap){
        //这里换行改变之后原来的内容会丢失，所以要重新加文本
        String text = this.getText();
        //这里有改动(2.42)
        this.isWrap = lineWrap;
        this.setEditorKit(myKit);
        this.setText(text);
    }
    //插入文本
    public void insert(String content, int offset){
        if(this.getSelectedText() != null) {//选中了内容
            replaceRange("", this.getSelectionStart(), this.getSelectionEnd());
        }
        this.insertString(offset, content, defAttribute);
    }
    //在当前光标位置插入文本
    public void insert(String content){
        if(this.getSelectedText() != null) {//选中了内容
            replaceRange("", this.getSelectionStart(), this.getSelectionEnd());
        }
        this.insertString(this.getCaretPosition(), content, defAttribute);
    }
    //异步插入 - 在当前光标位置插入文本，但不改变光标位置
    public void asynInsert(String content){
        if(this.getSelectedText() != null) {//选中了内容
            replaceRange("", this.getSelectionStart(), this.getSelectionEnd());
        }
        int pos = this.getCaretPosition();
        this.insertString(this.getCaretPosition(), content, defAttribute);
        this.setCaretPosition(pos);

    }
    //不管选中的插入
    public void justInsert(String content, int offset){
        this.insertString(offset, content, defAttribute);
    }
    //从当前位置偏移
    public void offsetFromCare(int offset){
        this.setCaretPosition(this.getCaretPosition()+offset);
    }
    //得到光标下一个字符
    public String getNextChar(){
        int pos = this.getCaretPosition();
        try {
            return this.getDocument().getText(pos, 1);
        } catch (BadLocationException e) {
            //没有下一个
            return "";
        }
    }
    //得到光标前一个字符
    public String getPreChar(){
        int pos = this.getCaretPosition();
        try {
            return this.getDocument().getText(pos-1, 1);
        } catch (BadLocationException e) {
            //没有前一个
            return "";
        }
    }
    //得到指定位置的字符
    public String getCharOfIndex(int index){
        try {
            return this.getDocument().getText(index, 1);
        } catch (BadLocationException e) {
            //没有
            return "";
        }
    }
    //替换文本
    public void replaceRange(String replaceText, int start, int end){
        this.removeString(start, end-start);
        insert(replaceText, start);
    }
    //替换文本 - 不管选中
    public void justReplaceRange(String replaceText, int start, int end){
        this.removeString(start, end-start);
        justInsert(replaceText, start);
    }
    //追加文本
    public void append(String content){
        insert(content, this.getDocument().getLength());
    }
    //选中文本
    public void choose(int start, int end){
        this.setSelectionStart(start);
        this.setSelectionEnd(end);
    }
    //一行开始
    public int getLineStart(){
        int pos = this.getCaretPosition();
        int start;
        //向前
        for (start = pos-1; !getCharOfIndex(start).equals("\n") && start >= 0; start--);
        //调整
        if(start < 0) start = 0;
        else start++;
        return start;
    }
    //一行结束
    public int getLineEnd(){
        int pos = this.getCaretPosition();
        String text = this.getText();
        int end;
        //向后
        for (end = pos; !getCharOfIndex(end).equals("\n") && end < text.length(); end++);
        //调整
        if(end < text.length()) end++;
        return end;
    }
    //返回光标所在行
    public String getLine(){
        int start = getLineStart(), end = getLineEnd();
        try {
            return getText(start, end-start);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
        return null;
    }
    //删除光标所在行
    public void removeLine(){
        int start = getLineStart(), end = getLineEnd();
        removeString(start, end-start);
    }

    @Override
    public String getText(){
        synchronized (getDocument()){
            return super.getText().replaceAll("\\r", "");//一定要去\r
        }
    }

    /**
     * 重写的setText方法，这个方法和高亮有线程冲突，加了个锁
     * @param t
     */
    public void setText(String t){
        JavaUtil.setTextLatch = new CountDownLatch(1);
        super.setText(t);
        JavaUtil.setTextLatch.countDown();
    }
    //以下解决由于文本变动导致的同步问题 (转移到了在AppFunc的onHighlight里添加)
    public void insertString(int offset, String str, AttributeSet a){
        try {
            this.getDocument().insertString(offset, str, defAttribute);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }
    public void removeString(int offs, int len){
        try {
            this.getDocument().remove(offs, len);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    /**
     * 以下两方法用于实现不换行
     * @return
     */
    @Override
    public boolean getScrollableTracksViewportWidth() {
        if(isWrap) return super.getScrollableTracksViewportWidth();
        return getUI().getPreferredSize(this).width
                <= getParent().getSize().width;
    };

    @Override
    public Dimension getPreferredSize() {
        if(isWrap) return super.getPreferredSize();
        return getUI().getPreferredSize(this);
    };

    /**
     * 以下类用于解决JTextPane奇怪的换行问题
     */
    class MyStyledEditorKit extends StyledEditorKit {
        private MyFactory factory;

        public ViewFactory getViewFactory() {
            if (factory == null) {
                factory = new MyFactory();
            }
            return factory;
        }
    }

    class MyFactory implements ViewFactory {
        public View create(Element elem) {
            String kind = elem.getName();
            if (kind != null) {
                if (kind.equals(AbstractDocument.ContentElementName)) {
                    return new MyLabelView(elem);
                } else if (kind.equals(AbstractDocument.ParagraphElementName)) {
                    return new MyParagraphView(elem);
                } else if (kind.equals(AbstractDocument.SectionElementName)) {
                    return new BoxView(elem, View.Y_AXIS);
                } else if (kind.equals(StyleConstants.ComponentElementName)) {
                    return new ComponentView(elem);
                } else if (kind.equals(StyleConstants.IconElementName)) {
                    return new IconView(elem);
                }
            }

            // default to text display
            return new LabelView(elem);
        }
    }

    class MyParagraphView extends ParagraphView {

        public MyParagraphView(Element elem) {
            super(elem);
        }
        public void removeUpdate(DocumentEvent e, Shape a, ViewFactory f) {
            super.removeUpdate(e, a, f);
            resetBreakSpots();
        }
        public void insertUpdate(DocumentEvent e, Shape a, ViewFactory f) {
            super.insertUpdate(e, a, f);
            resetBreakSpots();
        }

        private void resetBreakSpots() {
            for (int i=0; i<layoutPool.getViewCount(); i++) {
                View v=layoutPool.getView(i);
                if (v instanceof MyLabelView) {
                    ((MyLabelView)v).resetBreakSpots();
                }
            }
        }

    }

    class MyLabelView extends LabelView {

        boolean isResetBreakSpots=false;

        public MyLabelView(Element elem) {
            super(elem);
        }
        //强制折行
        @Override
        public float getMinimumSpan(int axis) {
            if(!isWrap) return super.getMinimumSpan(axis);
            switch (axis) {
                case View.X_AXIS:
                    return 0;
                case View.Y_AXIS:
                    return super.getMinimumSpan(axis);
                default:
                    throw new IllegalArgumentException("Invalid axis: " + axis);
            }
        }

        public View breakView(int axis, int p0, float pos, float len) {
            if (axis == View.X_AXIS) {
                resetBreakSpots();
            }
            return super.breakView(axis, p0, pos, len);
        }

        public void resetBreakSpots() {
            isResetBreakSpots=true;
            removeUpdate(null, null, null);
            isResetBreakSpots=false;
        }

        public void removeUpdate(DocumentEvent e, Shape a, ViewFactory f) {
            super.removeUpdate(e, a, f);
        }

        public void preferenceChanged(View child, boolean width, boolean height) {
            if (!isResetBreakSpots) {
                super.preferenceChanged(child, width, height);
            }
        }
    }
    /**************准备工作**************/
    public SimpleHighlighter getSHighlighter() {
        return highlighter;
    }
}
