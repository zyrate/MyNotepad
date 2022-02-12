package view;

import javax.swing.*;
import java.awt.*;

/**
 * 消息对话框
 */
public class MessageDialog extends JDialog{
    JTextArea ta = new JTextArea();
    JScrollPane pane = new JScrollPane(ta);

    public MessageDialog(String title, int width, int height){
        this.setTitle(title);
        this.setSize(width, height);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.setLayout(new BorderLayout());
        ta.setLineWrap(false);
        ta.setEditable(false);
        ta.setFont(new Font("宋体", 0, 18));
        this.add(pane);
    }

    public void append(String text){
        ta.append(text+"\n");
    }
}
