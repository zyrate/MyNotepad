package view;

import util.CompFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 计时器
 * 单例模式
 */
public class MyTimer extends JDialog {
    EditWin editWin;
    private JLabel lTime, lHour, lMin, lSec;
    private JTextField tfHour, tfMin, tfSec;
    private JButton bStart, bPause, bStop, bConvert;

    private static final int WIDTH = 408, HEIGHT = 250;
    private boolean isCountDown = false; //是否是倒计时
    private boolean isTiming = false; //是否在计时
    private boolean isPausing = false; //是否暂停
    private int hour = 0, min = 0, sec = 0;

    //timer不准时！换成了它
    ScheduledExecutorService service;
    private static MyTimer myTimer = null;
    private MyTimer(EditWin editWin){
        this.editWin = editWin;
        setTitle("计时器");
        setSize(WIDTH, HEIGHT);
        setResizable(false);
        setLayout(null);
        lTime = new JLabel("00:00:00");
        tfHour = CompFactory.createTextField(1);
        tfMin = CompFactory.createTextField(1);
        tfSec = CompFactory.createTextField(1);
        lHour = CompFactory.createLabel("小时");
        lMin = CompFactory.createLabel("分钟");
        lSec = CompFactory.createLabel("秒");
        bConvert = CompFactory.createButton("倒计时");
        bStart = CompFactory.createButton("开始");
        bPause = CompFactory.createButton("暂停");
        bStop = CompFactory.createButton("停止");

        tfHour.setHorizontalAlignment(SwingConstants.RIGHT);
        tfMin.setHorizontalAlignment(SwingConstants.RIGHT);
        tfSec.setHorizontalAlignment(SwingConstants.RIGHT);

        tfHour.setBounds(55, 110, 60, 35);
        lHour.setBounds(120, 110, 50, 35);
        tfMin.setBounds(165, 110, 60, 35);
        lMin.setBounds(230, 110, 50, 35);
        tfSec.setBounds(275, 110, 60, 35);
        lSec.setBounds(340, 110, 50, 35);

        bConvert.setBounds(10, 160, 90, 35);
        bStart.setBounds(105, 160, 90, 35);
        bPause.setBounds(200, 160, 90, 35);
        bStop.setBounds(295, 160, 90, 35);

        lTime.setFont(new Font("微软雅黑", 0, 80));
        lTime.setHorizontalAlignment(SwingConstants.CENTER);//居中
        lTime.setBounds(5,5,390,100);

        this.add(lTime);
        this.add(tfHour);
        this.add(tfMin);
        this.add(tfSec);
        this.add(lHour);
        this.add(lMin);
        this.add(lSec);
        this.add(bStart);
        this.add(bConvert);
        this.add(bPause);
        this.add(bStop);
        update();
        addListener();

    }

    public static void showTimer(EditWin editWin){
        if(myTimer == null){
            myTimer = new MyTimer(editWin);
        }
        myTimer.setLocationRelativeTo(editWin);
        myTimer.setVisible(true);
        if(myTimer.isTiming) myTimer.bPause.grabFocus();
        else myTimer.bStart.grabFocus();
    }

    private void update(){
        if(isTiming){
            tfHour.setEnabled(false);
            tfMin.setEnabled(false);
            tfSec.setEnabled(false);
            bStart.setEnabled(false);
            bConvert.setEnabled(false);
            bPause.setEnabled(true);
            bStop.setEnabled(true);
            if(isPausing){
                bPause.setText("继续");
            }else{
                bPause.setText("暂停");
            }
        }else{
            bStart.setEnabled(true);
            bConvert.setEnabled(true);
            bPause.setEnabled(false);
            bStop.setEnabled(false);
            bStart.grabFocus();
            if(isCountDown){
                lTime.setBorder(BorderFactory.createTitledBorder("倒计时"));
                bConvert.setText("正计时");
                tfHour.setEnabled(true);
                tfMin.setEnabled(true);
                tfSec.setEnabled(true);
                lHour.setEnabled(true);
                lMin.setEnabled(true);
                lSec.setEnabled(true);
            }else{
                lTime.setBorder(BorderFactory.createTitledBorder("正计时"));
                bConvert.setText("倒计时");
                tfHour.setEnabled(false);
                tfMin.setEnabled(false);
                tfSec.setEnabled(false);
                lHour.setEnabled(false);
                lMin.setEnabled(false);
                lSec.setEnabled(false);
            }
        }
    }

    private void start(){
        service = Executors.newScheduledThreadPool(1);
        if(isCountDown){
            try {
                hour = Integer.valueOf(tfHour.getText().equals("")?"0":tfHour.getText());
                min = Integer.valueOf(tfMin.getText().equals("")?"0":tfMin.getText());
                sec = Integer.valueOf(tfSec.getText().equals("")?"0":tfSec.getText());
            }catch (NumberFormatException e){
                end();
                editWin.showStatus("请输入数字！");
                return;
            }
            if(hour == 0 && min == 0 && sec == 0 || hour < 0 || min < 0 || sec < 0){
                end();
                editWin.showStatus("请输入正数！");
                return;
            }
        }

        showTime();
        service.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if(!isPausing) {
                    if(isCountDown)
                        countDown();
                    else
                        countUp();
                }
            }
        }, 1, 1, TimeUnit.SECONDS);
    }
    //结束
    public void end(){
        try{
            service.shutdownNow();
        }catch (Exception e){
            //空指针也没事
        }
        isTiming = false;
        isPausing = false;
        update();
        this.setVisible(true);
    }

    //正数一秒
    private void countUp(){
        sec++;
        if(sec == 60){
            sec = 0;
            min++;
            if(min == 60){
                min = 0;
                hour++;
                if(hour == 1000){//屏幕装不下了
                    end();
                    return;
                }
            }
        }
        showTime();
    }
    //倒数一秒
    private void countDown(){
        sec--;
        if(hour == 0 && min == 0 && sec == 0){
            end();
        }else if(sec == -1){
            sec = 59;
            min--;
            if(min == -1){
                min = 59;
                hour--;
            }
        }
        showTime();
    }

    private void showTime(){
        String time = String.format("%02d", hour)+":"+String.format("%02d", min)+":"+String.format("%02d", sec);
        lTime.setText(time);
        if(isCountDown) editWin.changeStatus("倒计时："+time);
        else editWin.changeStatus("正计时："+time);
        if(!isTiming){
            editWin.changeStatus("就绪");
            editWin.showStatus("计时器：结束");
        }
    }

    private void addListener(){

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });
        bStart.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                isTiming = true;
                update();
                start();
            }
        });
        bConvert.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                isCountDown = isCountDown?false:true;
                update();
            }
        });
        bPause.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                isPausing = isPausing?false:true;
                update();
            }
        });
        bStop.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                isTiming = false;
                end();
                hour = 0;
                min = 0;
                sec = 0;
                showTime();
                update();
            }
        });
        tfSec.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                tfSec.selectAll();
            }
        });
        tfMin.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                tfMin.selectAll();
            }
        });
        tfHour.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                tfHour.selectAll();
            }
        });
    }

}
