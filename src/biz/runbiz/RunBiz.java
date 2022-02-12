package biz.runbiz;

import util.JavaUtil;
import view.EditWin;

import java.awt.event.*;
import java.util.ArrayList;

/**
 * 编译、运行的业务逻辑
 * 运行时直接弹出命令行
 */
public class RunBiz {
    EditWin editWin;
    private Runner runner;

    public RunBiz(EditWin editWin){
        this.editWin = editWin;

        if(editWin.getTextPane().getCodeMode()){
            editWin.getiRun().setEnabled(true);
        }else{
            editWin.getiRun().setEnabled(false);
        }

        addListener();
    }

    /**
     * 菜单事件处理
     */
    public static final int RUN = 1;
    private void dealMenu(int event){
        new Thread(){
            @Override
            public void run() {
                switch (event){
                    case RUN:
                        runFile();
                        break;
                }
            }
        }.start();
    }

    private void runFile(){
        final String PATH = "C:\\NotepadData\\build";
        final String FILE = editWin.getFilePath();
        final String NAME = editWin.getPureFileName();

        if (FILE == null) {
            editWin.changeStatus("当前文件不可运行");
            return;
        }

        ArrayList<String> cmds = new ArrayList<>();
        ScriptReader sr = new ScriptReader();
        String[] cmdArr = sr.getCmdsByType(JavaUtil.getFileType(editWin.getFileName()));//取出类型
        if(cmdArr == null) {
            editWin.changeStatus("当前文件不可运行");
            return;
        }
        for(int i = 0; i < cmdArr.length; i++){
            String cmd = cmdArr[i].replaceAll("%PATH%", PATH.replaceAll("\\\\", "\\\\\\\\"));//后面的必须有
            cmd = cmd.replaceAll("%FILE%", FILE.replaceAll("\\\\", "\\\\\\\\"));
            cmd = cmd.replaceAll("%NAME%", NAME);
            if(i != cmdArr.length-1){
                cmds.add(cmd);
            }else{
                cmds.add(cmd);
            }
        }

        runner = new Runner(cmds, PATH);
        runner.run();
        editWin.showStatus("运行："+editWin.getFileName());

    }

    private void addListener(){
        editWin.getTextPane().addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if(!editWin.getTextPane().getCodeMode())//不是代码模式
                    return;

                boolean ctrl = e.isControlDown();
                int code = e.getKeyCode();
                if(ctrl && code == KeyEvent.VK_B){
                    //运行
                    runFile();
                }
            }
        });
        editWin.getiRun().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dealMenu(RUN);
            }
        });
        editWin.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if(runner != null)
                    runner.shutdown();
            }
        });
    }
}
