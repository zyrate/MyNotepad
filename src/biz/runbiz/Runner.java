package biz.runbiz;


import view.MessageDialog;

import java.io.*;
import java.util.ArrayList;

/**
 * 负责执行命令和显示输出
 * 要调用Windows系统命令请添加cmd /c
 * 注意Java运行目标文件不能加路径和后缀
 */
public class Runner {
    private ArrayList<String> cmds;//默认最后一条命令是运行命令
    private String path;//执行路径
    private Runtime runtime;
    private Process process;//当前进程
    private InputStream in, err;
    private OutputStream out;
    private int exitCode;//退出码
    private boolean isPythonFile = false;

    private MessageDialog errDlg = null;

    public Runner(ArrayList<String> cmds, String path){
        this.cmds = cmds;
        this.path = path;
        runtime = Runtime.getRuntime();
        if(errDlg == null)
            errDlg = new MessageDialog("错误输出", 800, 300);
        if(cmds.get(0).trim().startsWith("python")){
            isPythonFile = true;
        }else{
            isPythonFile = false;
        }
    }

    public void run(){
        try {
            for(int i = 0; i < cmds.size(); i++){
                if(i != cmds.size()-1) {
                    System.out.println(cmds.get(i));
                    process = runtime.exec(cmds.get(i), null, new File(path));
                    in = process.getInputStream();
                    err = process.getErrorStream();
                    showError(err);
                    showError(in);
                    exitCode = process.waitFor();
                }else{
                    //最终还是用bat解决了问题
                    String batPath = path + "\\run.bat";
                    File batFile = new File(batPath);
                    batFile.createNewFile();
                    FileWriter writer = new FileWriter(batFile);
                    //如果是Python的话不要加 pause和exit
                    String cmdStr;
                    if(isPythonFile){
                        cmdStr = " && echo.";
                    }else{
                        cmdStr = " && echo. && pause\nexit";
                    }
                    writer.write("cls && cmd /c "+cmds.get(i)+cmdStr); //后面是换行+停留+退出
                    writer.flush();
                    writer.close();
                    process = runtime.exec("cmd /c start "+ batPath, null, new File(path));
                }
                if(exitCode != 0) {
                    throw new IllegalStateException("Runtime exec error, exitCode: "+exitCode);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 结束线程
     */
    public void shutdown(){
        process.destroy();
    }

    /**
     * 显示报错信息
     */
    public void showError(InputStream err){
        new Thread(){
            @Override
            public void run() {
                BufferedReader errOut = new BufferedReader(new InputStreamReader(err));
                try {
                    String buff;
                    while((buff = errOut.readLine()) != null){
                        errDlg.setVisible(true);
                        errDlg.append(buff);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }


}
