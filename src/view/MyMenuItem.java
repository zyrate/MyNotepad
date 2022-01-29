package view;

import javax.swing.*;
import java.awt.*;

/**
 * 菜单项
 */

public class MyMenuItem extends JMenuItem{
    private Font menuFont = new Font("微软雅黑", 0, 15);

    public MyMenuItem(String label){
        super(label);
        setFont(menuFont);
    }
}
