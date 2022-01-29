package util;
import javax.swing.*;
import java.awt.*;
/**
 * 组件工具类
 */
public class CompFactory {
    public static final Font font1 = new Font("微软雅黑", 0, 18);
    public static JTextArea createTextArea(int x, int y, int width, int height){
        JTextArea ta = new JTextArea();
        ta.setEditable(false);
        ta.setLineWrap(true);
        ta.setBackground(new Color(240, 240, 240));
        ta.setBounds(x, y, width, height);
        return ta;
    }
    public static JTextField createTextField(int cols){
        JTextField tf = new JTextField(cols);
        tf.setFont(font1);
        return tf;
    }
    public static JButton createButton(String name){
        JButton b = new JButton(name);
        b.setFont(font1);
        return b;
    }
    public static JLabel createLabel(String name){
        JLabel l = new JLabel(name);
        l.setFont(font1);
        return l;
    }
}
