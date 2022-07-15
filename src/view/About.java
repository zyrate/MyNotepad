package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * 关于界面
 */

public class About extends JDialog{
    //统一版本号，只需修改这里
    public static final String VERSION = "V2.53";
    //更新时间
    public static final String UPDATE = "2022-"+"7-14";

    public About(EditWin editWin){
        setModal(true);
        //setUndecorated(true);//隐藏标题栏 - 只用于JFrame
        setTitle("关于记事本");
        setSize(500, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(editWin);
        setLayout(null);

        ImageIcon icon = new ImageIcon(About.class.getResource("/icons/notepad.png"));
        icon.setImage(icon.getImage().getScaledInstance(100,100,Image.SCALE_DEFAULT));
        JLabel label = new JLabel(icon);
        label.setBounds(190, 20, 100, 100);
        JSeparator separator = new JSeparator();
        separator.setBounds(10, 120, 460, 5);
        JTextArea ta = new JTextArea(10, 300);
        ta.setBounds(70, 150, 360, 240);
        ta.setBackground(new Color(240, 240, 240));
        ta.setFont(new Font("微软雅黑", 0, 15));
        ta.setEnabled(false);
        ta.setLineWrap(true);
        ta.setText("版本：记事本"+VERSION+" (UTF8)\n\n作者：郑云瑞\n开发语言：Java\n开发环境：IntelliJ IDEA\n更新时间："+UPDATE+"\n\n简易记事本，记录每一天！\n日积月累，点击进步。");
        JButton button = new JButton("确定");
        button.setBounds(380, 400, 80, 30);
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        add(label);
        add(separator);
        add(ta);
        add(button);
        setVisible(true);
    }
}
