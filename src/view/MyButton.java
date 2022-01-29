package view;

import javax.swing.*;
import java.awt.*;

public class MyButton extends JButton{
    private Font buttonFont = new Font("微软雅黑", 0, 18);

    public MyButton(String text){
        super(text);
        setFont(buttonFont);
    }
}
