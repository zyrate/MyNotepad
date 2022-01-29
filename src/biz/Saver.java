package biz;

import view.EditWin;

import javax.swing.*;
import java.io.*;
import java.util.Date;

/**
 * 存储器
 */

public class Saver {
    private File file;
    EditWin editWin;

    public Saver(EditWin editWin){
        this.editWin = editWin;
        if(editWin.getFilePath() != null)
            file = new File(editWin.getFilePath());
        else {
            editWin.showStatus("选择保存路径");
            file = select();
            editWin.showStatus("就绪");
        }
        if(file != null){
            editWin.changeStatus("正在保存："+file.getName());
            save(1);
            editWin.changeStatus(new Date().toLocaleString()+" 已保存");
        }
    }
    //另存为
    public Saver(EditWin editWin, Object nullObj){
        this.editWin = editWin;
        editWin.showStatus("选择另存为路径");
        file = select();
        editWin.showStatus("就绪");
        if(file != null){
            editWin.changeStatus("正在保存："+file.getName());
            save(2);
            editWin.changeStatus(new Date().toLocaleString()+" 已另存至："+file.getPath());
        }
    }

    public File select(){
        JFileChooser fileChooser = new JFileChooser("C:\\");
        int option = fileChooser.showSaveDialog(null);
        if(option == JFileChooser.APPROVE_OPTION) {
            File temp = fileChooser.getSelectedFile();
            File file;
            if(!temp.getName().contains(".")){//没有扩展名，就默认txt
                file = new File(temp.getPath()+".txt");
            }else
                file = temp;
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
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            String str = editWin.getTextPane().getText();
            for(int i = 0; i < str.length(); i++){//注意回车的处理，有点烦
                if(str.charAt(i) == '\n')
                    writer.write("\r\n");
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
