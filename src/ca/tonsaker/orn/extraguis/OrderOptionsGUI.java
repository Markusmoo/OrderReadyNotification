package ca.tonsaker.orn.extraguis;

import ca.tonsaker.orn.MainFrame;
import ca.tonsaker.orn.utility.NumberFilter;

import javax.swing.*;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by Markus Tonsaker on 2017-12-12.
 *
 */
public class OrderOptionsGUI implements ActionListener{

    private static final int PHONE_NUMBER_LENGTH = 10;

    private JPanel holderPanel;
    private JTextField txtField_orderNumber;
    private JTextField txtField_phoneNumber;
    private JTextField txtField_name;
    private JButton toGoButton;
    private JButton toStayButton;
    private JButton doneButton;
    private JButton cancelButton;

    private MainFrame frame;

    private boolean deliveryMethodSelected = false;
    private boolean isToStay = true;

    public OrderOptionsGUI(MainFrame frame){
        this.frame = frame;

        PlainDocument doc = (PlainDocument) txtField_phoneNumber.getDocument();
        doc.setDocumentFilter(new NumberFilter(false, PHONE_NUMBER_LENGTH));

        PlainDocument doc2 = (PlainDocument) txtField_orderNumber.getDocument();
        doc2.setDocumentFilter(new NumberFilter(false, -1));

        toGoButton.addActionListener(this);
        toStayButton.addActionListener(this);
        doneButton.addActionListener(this);
        cancelButton.addActionListener(this);

        frame.setContentPane(getPanel());
        frame.revalidate();
    }

    public JPanel getPanel() {
        return holderPanel;
    }

    public void done(){
        if(txtField_orderNumber.getText().isEmpty()){
            JOptionPane.showMessageDialog(frame,
                    "Please include an order number!", "Error!", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if(!deliveryMethodSelected){
            JOptionPane.showMessageDialog(frame,
                    "Please include a delivery method!", "Error!", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if(txtField_phoneNumber.getText().length() > 0 && txtField_phoneNumber.getText().length() < PHONE_NUMBER_LENGTH){
            JOptionPane.showMessageDialog(frame,
                    "Phone number needs to contain "+ PHONE_NUMBER_LENGTH + " digits!", "Error!", JOptionPane.ERROR_MESSAGE);
            return;
        }
        frame.addProgressOrderInfo(new ProgressOrderInfo(frame.getProgressOrderPanel(), frame.placedItemOrderModel, isToStay, txtField_orderNumber.getText(),
                txtField_phoneNumber.getText(), txtField_name.getText()));
        frame.placedItemOrderModel.clear();
        frame.setContentPane(frame.mainPanel);
        frame.tabbedPane.setSelectedComponent(frame.scrollPane_progress);
        holderPanel.removeAll();
    }

    public void cancel(){
        frame.setContentPane(frame.mainPanel);
        holderPanel.removeAll();
    }

    public void setDeliveryMethod(boolean isToStay){
        if(isToStay){
            toGoButton.setBackground(new Color(60,63,65));
            toStayButton.setBackground(Color.GREEN);
        }else{
            toStayButton.setBackground(new Color(60,63,65));
            toGoButton.setBackground(Color.GREEN);
        }
        this.isToStay = isToStay;
        this.deliveryMethodSelected = true;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if(src.equals(doneButton)){
            done();
        }else if(src.equals(cancelButton)){
            cancel();
        }else if(src.equals(toGoButton)){
            setDeliveryMethod(false);
        }else if(src.equals(toStayButton)){
            setDeliveryMethod(true);
        }
    }
}
