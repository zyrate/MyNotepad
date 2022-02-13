package biz.runbiz;

import util.JavaUtil;
import view.About;
import view.EditWin;

import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;

/**
 * 编译、运行的业务逻辑
 * 运行时直接弹出命令行
 */
public class RunBiz {
    EditWin editWin;
    private Runner runner;


    private final String BUILD_PATH = "C:\\NotepadData\\build"; //输出路径
    private final String SCRIPT_PATH = "C:\\NotepadData\\scripts"; //脚本路径
    private String file; //源文件路径
    private String name; //源文件名(无后缀)

    public RunBiz(EditWin editWin){
        this.editWin = editWin;

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
        file = editWin.getFilePath();
        name = editWin.getPureFileName();

        File path = new File(SCRIPT_PATH);
        //scripts路径不存在
        if(!path.exists()){
            path.mkdir();
            JavaUtil.copyFile(About.VERSION+"run.script", path.getPath());
        }
        //build路径不存在
        path = new File(BUILD_PATH);
        if(!path.exists()){
            path.mkdir();
        }


        if (file == null) {
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
            String cmd = cmdArr[i].replaceAll("%PATH%", BUILD_PATH.replaceAll("\\\\", "\\\\\\\\"));//后面的必须有
            cmd = cmd.replaceAll("%FILE%", file.replaceAll("\\\\", "\\\\\\\\"));
            cmd = cmd.replaceAll("%NAME%", name);
            if(i != cmdArr.length-1){
                cmds.add(cmd);
            }else{
                cmds.add(cmd);
            }
        }

        runner = new Runner(cmds, BUILD_PATH);
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
