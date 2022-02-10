package biz;

import util.DTUtil;
import view.EditWin;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.*;
import java.util.concurrent.CountDownLatch;

/**
 * 文件打开器
 */

public class Opener{
    private File file;
    EditWin editWin;
    private String charset;//按指定字符集读取文件 - null为默认
    private CountDownLatch downLatch;//同步辅助

    public Opener(EditWin editWin, File file, String charset){
        this.editWin = editWin;
        this.file = file;
        this.charset = charset;
    }

    public Opener(EditWin editWin, String charset){
        this.editWin = editWin;
        this.charset = charset;
        editWin.showStatus("选择打开文件");
        file = select();
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

    /**
     * 这里要用另一个线程打开，否则页脚不更新
     */
    public CountDownLatch open(){
        if(file == null){
            return null;
        }
        editWin.changeStatus("正在打开："+file.getName());
        downLatch = new CountDownLatch(1);

        new Thread(){
            @Override
            public void run() {
                String buff = "";
                String line;
                try {
                    BufferedReader reader;
                    if(charset != null)
                        reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), charset));//按照指定字符集解码
                    else
                        reader = new BufferedReader(new FileReader(file));//默认字符集
                    //readLine方法不读\n
                    boolean firstLine = true; //第一行
                    while((line=reader.readLine()) != null){
                        if(firstLine){
                            buff += line;
                            firstLine = false;
                            continue;
                        }
                        buff += "\n";
                        buff += line;
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
                editWin.changeStatus("就绪");
                System.out.println("down");
                downLatch.countDown();
            }
        }.start();

        return downLatch;
    }

}
