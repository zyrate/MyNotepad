package biz;

import util.DTUtil;
import view.EditWin;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.*;

/**
 * 文件打开器
 */

public class Opener{
    private File file;
    EditWin editWin;
    private String charset;//按指定字符集读取文件 - null为默认

    public Opener(EditWin editWin, File file, String charset){
        this.editWin = editWin;
        this.file = file;
        this.charset = charset;
        if(file != null){
            editWin.changeStatus("正在打开："+file.getName());
            open();
        }
        editWin.changeStatus("就绪");
    }

    public Opener(EditWin editWin, String charset){
        this.editWin = editWin;
        this.charset = charset;
        editWin.showStatus("选择打开文件");
        file = select();
        if(file != null){
            editWin.changeStatus("正在打开："+file.getName());
            open();
        }
        editWin.changeStatus("就绪");
    }

    public File select(){
        JFileChooser fileChooser = new JFileChooser(DTUtil.getLastPath());
        int option = fileChooser.showOpenDialog(null);
        if(option == JFileChooser.APPROVE_OPTION) {
            //更新最后路径
            DTUtil.setLastPath(fileChooser.getSelectedFile().getPath());
            return fileChooser.getSelectedFile();
        }
        else
            return null;
    }

    public void open(){
        String buff = "";
        String line;
        try {
            BufferedReader reader;
            if(charset != null)
                reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), charset));//按照指定字符集解码
            else
                reader = new BufferedReader(new FileReader(file));//默认字符集

            //readLine方法不读\n
            while((line=reader.readLine()) != null){
                if(line.equals(""))//这里的莫名其妙的空字符串会让首次打开的文件每行多一个回车\n，要忽略
                    continue;
                buff += line;
                buff += "\n";
                //这里有个bug，就是不管原文末尾有没有回车，打开后都会至少有一个回车。
                //原来的空行会消失
            }
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "找不到指定文件！可能文件名错误或者已经被移除！", "错误", JOptionPane.OK_OPTION);
            return;
        } catch (IOException e) {
            e.printStackTrace();
        }
        editWin.setContent(buff);
        editWin.setFilePath(file.getPath());
        editWin.update();
        editWin.updateContent();
        //打开后，光标、滚动条置前
        editWin.getTextPane().setCaretPosition(0);
        editWin.getPane().getVerticalScrollBar().setValue(0);
    }

}
