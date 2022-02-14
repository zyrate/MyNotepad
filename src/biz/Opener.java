package biz;

import util.DTUtil;
import util.JavaUtil;
import view.EditWin;

import javax.swing.*;
import java.io.*;
import java.util.concurrent.CountDownLatch;

/**
 * 文件打开器
 */

public class Opener{
    private File file;
    EditWin editWin;
    private String charset;//按指定字符集读取文件 - null为自动识别
    private String autoCharset;//自动识别的字符集
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
        autoCharset = JavaUtil.detectCharset(file);
        //加锁
        downLatch = new CountDownLatch(1);

        new Thread(){
            @Override
            public void run() {
                //显示打开进度
                long length = file.length();
                long buffered = 3;//已读的 (每次都差3到不了100%)

                String buff = "";
                String line;

                BufferedReader reader = null;
                InputStreamReader isr = null;
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(file);
                    if(charset != null)
                        isr = new InputStreamReader(fis, charset);//按照指定字符集解码
                    else
                        isr = new InputStreamReader(fis, autoCharset);//自动识别字符集
                    reader = new BufferedReader(isr);

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
                        buffered += line.getBytes().length+1;
                        editWin.changeStatus("正在打开："+file.getName()+"  ("+buffered*100/length+"%)");
                    }

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(null, "找不到指定文件！可能文件名错误或者已经被移除！", "错误", JOptionPane.OK_OPTION);
                    return;
                } catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    try {
                        fis.close();
                        isr.close();
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if(buff.equals("")) charset = DTUtil.getCharset(); //如果是空文件，则用默认编码
                editWin.setCurrEncoding(charset==null?autoCharset:charset);
                editWin.setContent(buff);
                editWin.setFilePath(file.getPath());
                editWin.update();
                editWin.updateContent();
                //打开后，光标、滚动条置前
                editWin.getTextPane().setCaretPosition(0);
                editWin.getPane().getVerticalScrollBar().setValue(0);
                editWin.changeStatus("就绪");
                downLatch.countDown();
            }
        }.start();

        return downLatch;
    }

}
