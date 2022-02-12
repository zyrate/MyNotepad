package biz.hlt;

import entity.Highlight;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * XML配置文件SAX解析处理器
 */
public class HltSAXHandler extends DefaultHandler {
    private boolean canDivided;
    private int type;
    private String key1, key2;
    private String color;
    private String backColor;
    private boolean bold;
    private int size;
    private String font;
    private boolean italic;
    private boolean underline;
    private boolean isRegex;//key1是否是正则


    //如果要规范的话，每个优先级内部都应该确保 PART > ALL_LINE > KEYWORD
    private ArrayList<Highlight> normalList = new ArrayList();
    private ArrayList<Highlight> importantList = new ArrayList();
    private ArrayList<Highlight> unimportantList = new ArrayList();


    //当前打开的文件类型(不带首部的点号)
    private String fileType;
    private String nowContent="";//当前遍历到的标签里内容
    private StringBuilder sb;//避坑
    private boolean typeMatch;//该高亮文件类型符合当前文件
    private int prior;//高亮优先级 0低 1正常 2高

    public HltSAXHandler(String fileType){
        this.fileType = fileType.substring(1);//去掉点号
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        super.characters(ch, start, length);
        sb.append(new String(ch, start, length));//这里不能直接等于，因为一个标签有可能多次调用该方法 - 大坑！
    }
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);
        sb = new StringBuilder();
        switch (qName){
            case "hlt":
                //样式重置
                type = Highlight.UNKNOWN;
                color = "NULL"; backColor = "NULL"; //最终转换成Color就是null
                bold = false;
                size = -1;
                font = null;
                italic = false;
                underline = false;
                break;
            case "props":
                break;
            case "applyTo":
                for(int i = 0; i < attributes.getLength(); i++) {
                    if(attributes.getQName(i).equals("hltType")) {
                        String t = attributes.getValue(i);
                        if(t.equals("kw")) type = Highlight.KEYWORD;
                        else if(t.equals("pt")) type = Highlight.PART;
                        else if(t.equals("al")) type = Highlight.ALL_LINE;
                    } else if(attributes.getQName(i).equals("fileType")) {
                        String ts = attributes.getValue(i);
                        if(ts.contains(fileType)) typeMatch = true;
                        else typeMatch = false;
                    }
                }
                break;
            case "key":
            case "keys":
                //属性重置
                prior = 1;
                isRegex = false;
                canDivided = true;
                key1 = null; key2 = null;

                for(int i = 0; i < attributes.getLength(); i++) {
                    if(attributes.getQName(i).equals("prior")) {
                        prior = Integer.valueOf(attributes.getValue(i));
                    } else if(attributes.getQName(i).equals("canDivided")) {
                        canDivided = Boolean.valueOf(attributes.getValue(i));
                    } else if(attributes.getQName(i).equals("isRegex")) {
                        isRegex = Boolean.valueOf(attributes.getValue(i));
                    }
                }
                break;
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        super.endElement(uri, localName, qName);
        nowContent = sb.toString();
        switch (qName){
            case "hlt":
                break;
            case "props":
                break;
            case "color":
                color = nowContent;
                break;
            case "backColor":
                backColor = nowContent;
                break;
            case "bold":
                bold = Boolean.valueOf(nowContent);
                break;
            case "font":
                font = nowContent;
                break;
            case "size":
                size = Integer.valueOf(nowContent);
                break;
            case "italic":
                italic = Boolean.valueOf(nowContent);
                break;
            case "underline":
                underline = Boolean.valueOf(nowContent);
                break;
            case "key":
                key1 = nowContent;
                //遇到key尾就可生成高亮
                addHighlight();
                break;
            case "key1":
                key1 = nowContent;
                break;
            case "key2":
                key2 = nowContent;
                break;
            case "keys":
                //遇到keys尾就可生成高亮
                addHighlight();
                break;
        }
    }

    private void addHighlight(){
        if(!typeMatch) return;//文件类型不符合

        Highlight highlight = new Highlight(type, key1, key2, color, backColor,
                bold, size, font, italic, underline, isRegex, canDivided);
        if(prior == 1){
            normalList.add(highlight);
        }else if(prior == 0){
            unimportantList.add(highlight);
        }else if(prior == 2){
            importantList.add(highlight);
        }

    }

    //排序以保证类别顺序 - 设计不太好
    public ArrayList<Highlight> sort(ArrayList<Highlight> list){
        Collections.sort(list, new Comparator<Highlight>() {
            @Override
            public int compare(Highlight o1, Highlight o2) {
                return o1.getType()>o2.getType() ? -1 : 1;
            }
        });
        return list;
    }


    public ArrayList<Highlight> getNormalList() {
        //排序以保证顺序
        Collections.sort(this.normalList, new Comparator<Highlight>() {
            @Override
            public int compare(Highlight o1, Highlight o2) {
                return o1.getType()>o2.getType() ? -1 : 1;
            }
        });
        return this.normalList;
    }

    public ArrayList<Highlight> getImportantList() {
        //排序以保证顺序
        Collections.sort(this.importantList, new Comparator<Highlight>() {
            @Override
            public int compare(Highlight o1, Highlight o2) {
                return o1.getType()>o2.getType() ? -1 : 1;
            }
        });
        return this.importantList;
    }

    public ArrayList<Highlight> getUnimportantList() {
        //排序以保证顺序
        Collections.sort(this.unimportantList, new Comparator<Highlight>() {
            @Override
            public int compare(Highlight o1, Highlight o2) {
                return o1.getType()>o2.getType() ? -1 : 1;
            }
        });
        return this.unimportantList;
    }
}
