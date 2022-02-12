package biz.runbiz;

import view.About;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 运行脚本阅读器
 */
public class ScriptReader {
    public static final String PATH = "C:\\NotepadData\\scripts";
    public static final String FILE = About.VERSION+"run.script";
    private HashMap<String, String[]> cmdsMap = new HashMap<>();//类型与脚本Map

    public ScriptReader(){
        deal();
    }

    public String[] getCmdsByType(String type){
        return cmdsMap.get(type);
    }

    public String[] getAllType(){
        String[] types = new String[cmdsMap.size()];
        int i = 0;
        for(Map.Entry<String, String[]> e:cmdsMap.entrySet()){
            types[i++] = e.getKey();
        }
        return types;
    }

    //将内容处理成cmdsMap
    private void deal(){
        String filePath = PATH +"\\"+FILE;
        String buff = read(new File(filePath));

        Matcher m = Pattern.compile("\\[.+\\]").matcher(buff);
        while(m.find()){
            String one = m.group().replaceAll("[\\[\\]]", "");
            String[] arr = one.split(";");
            if(arr.length == 0)
                continue;
            String[] cmds = new String[arr.length-1];
            for(int i = 0; i < arr.length-1; i++){
                cmds[i] = arr[i+1];
            }
            cmdsMap.put(arr[0], cmds);
        }
    }

    private String read(File file){
        String buff = "";
        String line;
        try {
            if(!file.exists())
                file.createNewFile();

            BufferedReader reader = new BufferedReader(new FileReader(file));
            while((line = reader.readLine()) != null){
                if(line.equals(""))
                    continue;
                buff += line;
                buff += "\n";
            }
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buff;
    }
}
