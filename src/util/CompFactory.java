package util;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * 组件工具类
 */
public class CompFactory {
    public static final Font font1 = new Font("微软雅黑", 0, 18);//label字体
    public static final Font font2 = new Font("微软雅黑", 0, 17);//菜单字体
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
    public static JButton createButton(String text){
        JButton b = new JButton(text);
        b.setFont(font1);
        return b;
    }
    public static JLabel createLabel(String name){
        JLabel l = new JLabel(name);
        l.setFont(font1);
        return l;
    }
    //菜单可直接设置快捷键
    public static JMenu createMenu(String label, int key){
        JMenu menu = new JMenu(label);
        menu.setFont(font2);
        menu.setMnemonic(key); //Alt + key
        return menu;
    }
    public static JMenuItem createMenuItem(String label, String keys){
        JMenuItem item = new JMenuItem(label);
        item.setFont(font2);
        item.setAccelerator(KeyStroke.getKeyStroke(keys)); //这里可以写组合键
        return item;
    }
    //也可不设置
    public static JMenuItem createMenuItem(String label){
        JMenuItem item = new JMenuItem(label);
        item.setFont(font2);
        return item;
    }
    public static JMenu createMenu(String label){
        JMenu menu = new JMenu(label);
        menu.setFont(font2);
        return menu;
    }
    //单选框不设快捷键
    public static JCheckBoxMenuItem createCheckMenuItem(String label){
        JCheckBoxMenuItem item = new JCheckBoxMenuItem(label);
        item.setFont(font2);
        return item;
    }
}
