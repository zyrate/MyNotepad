package view;

import biz.AppFunc;
import util.DTUtil;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 笔记预览
 */

public class Notes extends JDialog{
    AppFunc appFunc;
    private MyButton bNew, bOpen, bDelete, bCancel;
    private DefaultListModel<String> model;
    private JList list;
    private JScrollPane pane;
    private JPanel p;
    private JLabel lCnt;
    private JCheckBox cExt;//是否扩展名
    private JPopupMenu popup;//右键菜单
    private MyMenuItem iRename;//重命名

    private File[] files;

    public Notes(AppFunc appFunc){
        this.appFunc = appFunc;
        init();
        updateNotes(DTUtil.getNotesExt());
        addListener();
        setVisible(true);
    }


    public void updateNotes(boolean notesExt){//笔记是否有扩展名
        model.clear();
        files = getFiles();
        for(File file:files){
            if(!notesExt)
                model.addElement("("+new SimpleDateFormat("yy-MM-dd").format(new Date(file.lastModified()))+")  "+file.getName().replaceAll("\\.\\w+$", ""));
            else
                model.addElement("("+new SimpleDateFormat("yy-MM-dd").format(new Date(file.lastModified()))+")  "+file.getName());
        }
        //不为空就默认选第一个
        if(!model.isEmpty()){
            list.setSelectedIndex(0);
        }
        lCnt.setText("共 "+files.length+" 篇笔记");
    }

    private void init(){
        setModal(true);
        bNew = new MyButton("+ 新建");
        bOpen = new MyButton("打开");
        bDelete = new MyButton("删除");
        bCancel = new MyButton("取消");
        bNew.setBounds(100, 420, 120, 40);
        bOpen.setBounds(230, 420, 120, 40);
        bDelete.setBounds(360, 420, 120, 40);
        bCancel.setBounds(490, 420, 120, 40);
        lCnt = new JLabel("共 0 篇笔记");
        lCnt.setBounds(35, 20, 200, 20);
        lCnt.setFont(new Font("楷体", 0, 20));
        cExt = new JCheckBox("扩展名");
        cExt.setSelected(DTUtil.getNotesExt());
        cExt.setBounds(593, 18, 100, 20);
        cExt.setFont(new Font("楷体", 0, 20));
        cExt.setFocusable(false);
        iRename = new MyMenuItem("重命名");
        popup = new JPopupMenu();
        popup.add(iRename);

        model = new DefaultListModel<>();
        list = new JList(model);
        pane = new JScrollPane(list, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        p = new JPanel();
        p.add(pane);
        p.setBounds(22, 40, 670, 450);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setLayoutOrientation(JList.VERTICAL);
        list.setFont(new Font("微软雅黑", 0, 18));
        list.setFixedCellWidth(620);
        list.setFixedCellHeight(30);
        list.setVisibleRowCount(12);
        list.setSelectionBackground(new Color(182, 226, 232));
        list.setSelectionForeground(Color.black);


        setLayout(null);
        setResizable(false);
        setTitle("我的笔记");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(720, 510);
        add(bCancel);
        add(bDelete);
        add(bNew);
        add(bOpen);
        add(p);
        add(lCnt);
        add(cExt);
        setLocationRelativeTo(appFunc.editWin);
    }

    public void open(){
        if(!model.isEmpty()){
            appFunc.open(files[list.getSelectedIndex()], null);//调用封装好的打开方法，更安全
            dispose();
        }
    }
    public void delete(){
        if(!model.isEmpty()){
            int option = JOptionPane.showConfirmDialog(this, "真的要删除此笔记吗？");
            if(option != JOptionPane.OK_OPTION)
                return;
            files[list.getSelectedIndex()].delete();
            updateNotes(DTUtil.getNotesExt());
        }
    }
    public void newNote(){
        File file;
        String name = JOptionPane.showInputDialog(this, "标题：", "新建笔记", JOptionPane.INFORMATION_MESSAGE);
        if(name != null && !name.equals("")){
            if(!name.contains(".")){
                file = new File("C:\\NotepadData\\notes\\"+name+".txt");
            }else{
                file = new File("C:\\NotepadData\\notes\\"+name);
            }
            //比对标题是否重复
            for(File temp:files){
                if(temp.getName().equals(file.getName())){
                    int option = JOptionPane.showConfirmDialog(this, "此标题已存在，是否覆盖？", "命名重复", JOptionPane.INFORMATION_MESSAGE);
                    if(option != JOptionPane.OK_OPTION){
                        newNote();
                        return;
                    }
                }
            }
            //不重复
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            appFunc.open(file, DTUtil.getCharset()); //新建编码用默认的
            dispose();
        }
    }

    //重命名
    public void rename(){
        String oldName = model.elementAt(list.getSelectedIndex()).replaceAll("^\\([0-9-]+\\)  ", "");
        String newName = new RenameDialog(appFunc.editWin, oldName).showRenameDialog();
        if(newName != null && !newName.equals(oldName)){
            File file;
            if(!newName.contains(".")){
                file = new File("C:\\NotepadData\\notes\\"+newName+".txt");
            }else{
                file = new File("C:\\NotepadData\\notes\\"+newName);
            }
            //比对标题是否重复
            for(File temp:files){
                if(temp.getName().equals(file.getName())){
                    JOptionPane.showMessageDialog(this, "此标题已存在，请重新命名！", "命名重复", JOptionPane.INFORMATION_MESSAGE);
                    rename();
                    return;
                }
            }
            //不重复
            files[list.getSelectedIndex()].renameTo(file);
            updateNotes(DTUtil.getNotesExt());
            appFunc.editWin.setFilePath(file.getPath());
            appFunc.editWin.update();
            appFunc.prepareHighlight();
        }
    }

    public File[] getFiles(){
        File notesDir = new File("C:\\NotepadData\\notes");
        if(!notesDir.exists()){
            notesDir.mkdir();
        }
        String[] filesName = notesDir.list();
        File[] files = new File[filesName.length];
        for(int i = 0; i < filesName.length; i++){
            files[i] = new File(notesDir.getPath()+"\\"+filesName[i]);
        }
        //按修改先后顺序排序
        for(int i = 0; i < files.length; i++){
            for(int j = 0; j < files.length-i-1; j++){
                if(files[j].lastModified() < files[j+1].lastModified()){
                    File temp = files[j];
                    files[j] = files[j+1];
                    files[j+1] = temp;
                }
            }
        }
        return files;
    }

    public void addListener(){
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1){
                    open();
                }else if(e.getButton() == MouseEvent.BUTTON3){
                    //这里单击剩余空白部分会认为最后一个
                    list.setSelectedIndex(list.locationToIndex(e.getPoint()));
                    popup.show(list, e.getX(), e.getY());
                }
            }
        });
        list.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                open();
            }
        });
        bCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        bOpen.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                open();
            }
        });
        bDelete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                delete();
            }
        });
        bNew.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                newNote();
            }
        });
        cExt.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if(cExt.isSelected()){
                    updateNotes(true);
                    DTUtil.setNotesExt(true);
                }else{
                    updateNotes(false);
                    DTUtil.setNotesExt(false);
                }
            }
        });
        iRename.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                rename();
            }
        });
    }
}

