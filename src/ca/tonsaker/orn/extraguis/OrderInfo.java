package ca.tonsaker.orn.extraguis;

import ca.tonsaker.orn.MainFrame;
import com.google.gson.annotations.Expose;
import sun.applet.Main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 *
 * Created by Markus Tonsaker on 2017-12-02.
 */
public class OrderInfo implements ActionListener{
    protected JTextField txtField_orderNumber;
    protected JPanel ordersPanel;
    protected JPanel orderInfoPanel;
    protected JTextField txtField_elapsedTime;
    protected JButton orderReadyButton;
    protected JButton cancelOrderButton;
    protected JList orderList;
    protected JTextField txtField_name;
    protected JLabel lbl_orderNumber;
    protected JLabel lbl_name;
    protected JLabel lbl_eTime;

    protected JPanel parentPanel;
    protected Component invisibleSpace;

    protected DefaultListModel<String> orderListModel;

    protected boolean clockRunning = false;
    protected long elapsedTime;
    protected long beginTime;

    protected boolean isPhoneNumber;
    protected String orderNumber;
    protected String orderName;

    public OrderInfo(JPanel parent, DefaultListModel<String> list, boolean isPhoneNumber, String orderNumber, String orderName){
        orderList.setModel(orderListModel);
        for(int idx = 0; idx < list.getSize(); idx++) {
            orderListModel.addElement(list.getElementAt(idx));
            System.out.println("Added: " + list.getElementAt(idx)); //TODO
            System.out.println(orderListModel.getSize()); //TODO
        }

        this.parentPanel = parent;
        this.isPhoneNumber = isPhoneNumber;
        this.orderNumber = orderNumber;
        this.orderName = orderName;

        if(isPhoneNumber) orderNumber = "(" + orderNumber.substring(0,3) + ")-" + orderNumber.substring(3,6) + "-" +
                orderNumber.substring(6);

        txtField_orderNumber.setText(orderNumber);
        txtField_name.setText(orderName);

        this.cancelOrderButton.addActionListener(this);
        this.orderReadyButton.addActionListener(this);

        parent.add(orderInfoPanel);
        invisibleSpace = Box.createVerticalStrut(10);
        parent.add(invisibleSpace);
        orderInfoPanel.setMaximumSize(new Dimension(600,200));
        orderInfoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        setupClock();
    }

    public void setupClock(){
        elapsedTime = 0;
        beginTime = System.currentTimeMillis();
        clockRunning = true;
    }

    public void updateClock(){
        if(clockRunning) {
            elapsedTime = System.currentTimeMillis() - beginTime;
            long s = (elapsedTime / 1000) % 60;
            long m = (elapsedTime / (1000 * 60)) % 60;
            long h = (elapsedTime / (1000 * 60 * 60)) % 24;
            txtField_elapsedTime.setText(String.format("%d:%02d:%02d", h, m, s));
        }
    }

    public void stopClock(){
        clockRunning = false;
    }

    public void orderReady(){
        //TODO
        if(isPhoneNumber) MainFrame.twilioHandler.sendNotification(orderName, "+1"+orderNumber, "+16042655644",orderListModel);
    }

    public void orderCancel(){
        int option = JOptionPane.showConfirmDialog(orderInfoPanel.getParent(), "Are you sure you want to cancel this order?",
                "Cancel Order?", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
        if(option != 0) return;
        orderInfoPanel.removeAll();
        orderInfoPanel.setVisible(false);
        parentPanel.remove(orderInfoPanel);
        parentPanel.remove(invisibleSpace);
        MainFrame.orderInfoArrayList.remove(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if(src.equals(cancelOrderButton)) orderCancel(); else
        if(src.equals(orderReadyButton)) orderReady();
    }

    protected void createUIComponents() {
        orderListModel = new DefaultListModel<>();
        orderList = new JList<>(orderListModel);
    }
}
