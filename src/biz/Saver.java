package biz;

import util.DTUtil;
import view.EditWin;

import javax.swing.*;
import java.io.*;
import java.util.Date;

/**
 * 存储器
 */

public class Saver {
    private File file;
    private String charset;//按指定字符集读取文件 - 不能为null
    EditWin editWin;

    public Saver(int type, EditWin editWin, String charset){
        this.editWin = editWin;
        this.charset = charset;
        if(type == 1) {//保存
            if (editWin.getFilePath() != null)
                file = new File(editWin.getFilePath());
            else {
                editWin.showStatus("选择保存路径");
                file = select();
                editWin.showStatus("就绪");
            }
            if (file != null) {
                editWin.changeStatus("正在保存：" + file.getName());
                save(1);
                editWin.changeStatus(new Date().toLocaleString() + " 已保存");
            }
        }else if(type == 2) {//另存
            editWin.showStatus("选择另存为路径");
            file = select();
            editWin.showStatus("就绪");
            if(file != null){
                editWin.changeStatus("正在保存："+file.getName());
                save(2);
                editWin.changeStatus(new Date().toLocaleString()+" 已另存至："+file.getPath());
            }
        }
    }

    public File select(){
        JFileChooser fileChooser = new JFileChooser(DTUtil.getLastSavePath());
        int option = fileChooser.showSaveDialog(null);
        if(option == JFileChooser.APPROVE_OPTION) {
            File temp = fileChooser.getSelectedFile();
            File file;
            if(!temp.getName().contains(".")){//没有扩展名，就默认txt
                file = new File(temp.getPath()+".txt");
            }else
                file = temp;

            //更新最后路径
            DTUtil.setLastSavePath(file.getPath());
            return file;
        }
        else
            return null;
    }
    //1保存， 2另存
    public void save(int type){
        try {
            if(!file.exists())
                file.createNewFile();
            //同样要注意编码
            BufferedWriter writer;
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), charset));
            String str = editWin.getTextPane().getText().replaceAll("\\r", "");//把所有的\r去掉
            for(int i = 0; i < str.length(); i++){//注意回车的处理，有点烦
                if(str.charAt(i) == '\n')
                    writer.newLine();
                else
                    writer.write(str.charAt(i));
            }
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(type == 1) {
            editWin.setContent(editWin.getTextPane().getText());
            editWin.setFilePath(file.getPath());
            editWin.update();
            //save的时候不需要更新文本框，因为这样会移动光标
        }
    }
}
