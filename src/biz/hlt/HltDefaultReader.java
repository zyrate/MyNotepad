package biz.hlt;

import entity.Highlight;

import java.io.*;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HltDefaultReader implements HltConfReader {
    private ArrayList<Highlight> normalList = new ArrayList();//按指定顺序的高亮 PART > ALL_LINE > KEYWORD
    private ArrayList<Highlight> importantList = new ArrayList();//优先级高的高亮
    private ArrayList<Highlight> unimportantList = new ArrayList();//优先级低的高亮

    /**
     * 读取文件
     * @param settingPath 配置文件路径
     * @param fileType 当前开启高亮的文件类型
     */
    public HltDefaultReader(String settingPath, String fileType){
        //正则一定不能有歧义！
        File file = new File(settingPath+SimpleHighlighter.CONF_TYPE);
        if(file.exists()){//如果文件存在
            String buff = read(file);
            Matcher m = Pattern.compile("\\{[\\s\\S]*?\\}\\n").matcher(buff);//大括号{} 这里正则有待改进
            //这里第二次find时会报栈溢出异常，或跟.|\\n有关，但暂不影响运行 - 已解决(用[\s\S]*匹配所有字符)
            while(m.find()){
                String head = findOne("\\{[a-z0-9. ]+", m.group());//找到{头部
                if(head == null)
                    continue;
                //取出文件类型
                head = head.replaceAll("\\{", "");//先去除{，剩下的是文件后缀
                if(!contains(head.split("\\s+"), fileType)){
                    //不存在此后缀
                    continue;
                }
                Matcher m1 = Pattern.compile("\\(\\n[\\s\\S]*?\\)\\n").matcher(m.group());//括号()
                while(m1.find()){
                    String content = m1.group();
                    content = content.replaceAll("^(\\()|(\\))$", "");//去除()
                    String[] highlights = content.split(";;");//拆分
                    addHighlights(highlights, Highlight.PART);
                }
                Matcher m2 = Pattern.compile("\\<\\n[\\s\\S]*?\\>\\n").matcher(m.group());//<>
                while(m2.find()){
                    String content = m2.group();
                    content = content.replaceAll("^(\\<)|(\\>)$", "");//去除<>
                    String[] highlights = content.split(";;");//拆分
                    addHighlights(highlights, Highlight.ALL_LINE);
                }
                Matcher m3 = Pattern.compile("\\[\\n[\\s\\S]*?\\]\\n").matcher(m.group());//中括号[]
                while(m3.find()){
                    String content = m3.group();
                    content = content.replaceAll("^(\\[)|(\\])$", "");//去除[]
                    String[] highlights = content.split(";;");//拆分 - 用双分号不容易引起歧义
                    addHighlights(highlights, Highlight.KEYWORD);
                }
            }
        }
    }

    private boolean contains(String[] arr, String str){
        for(int i = 0; i < arr.length; i++){
            if(arr[i].equals(str))
                return true;
        }
        return false;
    }

    //查找一次
    private String findOne(String regex, String text){
        Matcher m = Pattern.compile(regex).matcher(text);
        if(m.find()){
            return m.group();
        }
        return null;
    }

    private String read(File file){
        BufferedReader reader;
        String line, buff = "";
        try {
            reader = new BufferedReader(new FileReader(file));
            while((line=reader.readLine()) != null){
                buff += line;
                buff += "\n";
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buff;
    }

    //向列表中添加属性
    private void addHighlights(String[] highlights, int type){
        for (String s:highlights) {
            int index = s.indexOf(" : ");
            if(index == -1)
                continue;
            //把键和值提取到数组中
            String[] keys = s.substring(0, index).trim().split("\\s+");
            String[] values = s.substring(index+3, s.length()).trim().split("\\s+");
            if(values[values.length-1].equals("!")){//重要
                importantList.add(new Highlight(type, keys, values));
            }else if(values[values.length-1].equals("?")){//不重要
                unimportantList.add(new Highlight(type, keys, values));
            }else{//正常
                normalList.add(new Highlight(type, keys, values));
            }
        }
    }

    @Override
    public ArrayList<Highlight> getNormalList() {
        return this.normalList;
    }

    @Override
    public ArrayList<Highlight> getImportantList() {
        return this.importantList;
    }

    @Override
    public ArrayList<Highlight> getUnimportantList() {
        return this.unimportantList;
    }
}
