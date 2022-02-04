package view;

import javax.swing.*;
import java.awt.*;

/**
 * 列表和滚动条
 * 列表中的内容可以是任何类型，通过泛型设定
 */
class ListAndScroll<E> extends JPanel {
    private DefaultListModel<E> model;
    private JList<E> list;
    private JScrollPane pane;
    public ListAndScroll(E[] arr, int width){
        /*创建列表的步骤*/
        model = new DefaultListModel<>();
        for(E e : arr){
            model.addElement(e);
        }
        list = new JList<>(model);

        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setLayoutOrientation(JList.VERTICAL);
        list.setFont(new Font("微软雅黑", 0, 18));
        list.setFixedCellWidth(width);
        list.setVisibleRowCount(8);

        //添加滚动条
        pane = new JScrollPane(list, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        this.add(pane);
    }

    //跳到某项
    public void select(int index){
        list.setSelectedIndex(index);
        //人家有这个方法
        list.ensureIndexIsVisible(index);
    }

    public DefaultListModel<E> getModel() {
        return model;
    }

    public void setModel(DefaultListModel<E> model) {
        this.model = model;
    }

    public JList<E> getList() {
        return list;
    }

    public void setList(JList<E> list) {
        this.list = list;
    }

    public JScrollPane getPane() {
        return pane;
    }

    public void setPane(JScrollPane pane) {
        this.pane = pane;
    }
}
