package biz.hlt;

import entity.Highlight;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.util.ArrayList;

public class HltXmlReader implements HltConfReader {
    private SAXParser parser;
    private HltSAXHandler handler;

    public HltXmlReader(String settingPath, String fileType){
        SAXParserFactory spf = SAXParserFactory.newInstance();
        try {
            parser = spf.newSAXParser();
            handler = new HltSAXHandler(fileType);
            parser.parse(settingPath+SimpleHighlighter.CONF_TYPE, handler);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public ArrayList<Highlight> getNormalList() {
        return handler.getNormalList();
    }

    @Override
    public ArrayList<Highlight> getImportantList() {
        return handler.getImportantList();
    }

    @Override
    public ArrayList<Highlight> getUnimportantList() {
        return handler.getUnimportantList();
    }
}
