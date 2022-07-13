package entity;

/**
 * 标记一个高亮范围
 */
public class HltToken {
    public int start;
    public int end;
    public HltToken(int start, int end){
        this.start = start;
        this.end = end;
    }
}
