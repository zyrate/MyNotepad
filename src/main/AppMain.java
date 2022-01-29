package main;

import biz.AppFunc;
import util.DTUtil;
import view.EditWin;
import view.FindAndReplace;

import javax.swing.*;
import java.io.*;

/**
 * 应用主入口
 */

public class AppMain {
    //args的第一个参数就是被打开的那个文件(如果存在的话)
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        } catch (Exception e){}

        EditWin editWin = new EditWin();
        AppFunc appFunc = new AppFunc(editWin);
        if(args.length != 0){//直接打开文件
            appFunc.open(new File(args[0]));
        }
    }
}
