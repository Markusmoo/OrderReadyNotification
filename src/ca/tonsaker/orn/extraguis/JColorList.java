package ca.tonsaker.orn.extraguis;

import ca.tonsaker.orn.Utilities;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Markus Tonsaker on 2017-12-08.
 */
public class JColorList extends JList{

    public JColorList(){
        super();
        setCellRenderer(new ColorRenderer());
    }

    public JColorList(ListModel dataModel){
        super(dataModel);
        setCellRenderer(new ColorRenderer());
    }

    private class ColorRenderer extends DefaultListCellRenderer{//} implements ListCellRenderer{

        public ColorRenderer(){}

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list,value,index,isSelected,cellHasFocus);
            setText(value.toString());
            setFont(new Font(null,Font.BOLD,22));
            setForeground(Utilities.randomColorWord(value.toString()));
            return this;
        }

    }

}
