package biz.hlt;

import entity.Highlight;

import java.util.ArrayList;

/**
 * 高亮配置文件读取器，设想可用读取多种类型的配置文件，都返回ArrayList<Highlight>
 * 这样的话SimpleHighlighter就不管文件读取了
 */
public interface HltConfReader {
    ArrayList<Highlight> getNormalList();
    ArrayList<Highlight> getImportantList();
    ArrayList<Highlight> getUnimportantList();
}
