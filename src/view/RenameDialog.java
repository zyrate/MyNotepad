package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * 重命名小对话框
 */
class RenameDialog extends JDialog {
    JLabel l;
    JTextField t;
    JButton b1, b2;
    String newName = null;

    public RenameDialog(Component parent, String oldName){
        setModal(true);

        setSize(240, 155);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setTitle("重命名");
        setLayout(null);
        l = new JLabel("改为：");
        l.setFont(new Font("微软雅黑", 0, 17));
        t = new JTextField(8);
        t.setFont(new Font("微软雅黑", 0, 18));
        t.setText(oldName);
        t.selectAll();
        b1 = new JButton("确定");
        b2 = new JButton("取消");
        b1.setFont(new Font("微软雅黑", 0, 15));
        b2.setFont(new Font("微软雅黑", 0, 15));
        l.setBounds(10, 7, 80, 20);
        t.setBounds(10, 30, 200, 30);
        b1.setBounds(30, 70, 70, 30);
        b2.setBounds(120, 70, 70, 30);
        add(l);
        add(t);
        add(b1);
        add(b2);
        setLocationRelativeTo(parent);
        addListener();
    }

    public String showRenameDialog(){
        setVisible(true);
        return newName;
    }

    private void addListener(){
        b1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(!t.getText().equals("")){
                    newName = t.getText();
                }
                dispose();
            }
        });
        b2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        t.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (!t.getText().equals("")) {
                        newName = t.getText();
                    }
                    dispose();
                }
            }
        });
    }
}
