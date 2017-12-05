package ca.tonsaker.orn.extraguis;

import ca.tonsaker.orn.MainFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 *
 * Created by Markus Tonsaker on 2017-12-02.
 */
public abstract class OrderInfo implements ActionListener{
    protected JTextField txtField_orderNumber;
    protected JPanel ordersPanel;
    protected JPanel orderInfoPanel;
    protected JTextField txtField_elapsedTime;
    protected JButton topButton;
    protected JButton bottomButton;
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

    public OrderInfo(JPanel parent, DefaultListModel<String> list, boolean isPhoneNumber, String orderNumber, String orderName, long elapsedTime){
        orderList.setModel(orderListModel);
        for(int idx = 0; idx < list.getSize(); idx++) {
            orderListModel.addElement(list.getElementAt(idx));
        }

        this.parentPanel = parent;

        this.setOrderNumber(isPhoneNumber, orderNumber);
        this.setOrderName(orderName);
        this.setElapsedTime(elapsedTime);

        this.bottomButton.addActionListener(this);
        this.topButton.addActionListener(this);

        addToMainFrame();

        setupClock();
    }

    public abstract void addToMainFrame();

    protected abstract void topButtonAction();

    protected abstract void bottomButtonAction();

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

    public void selfDestruct(){
        orderInfoPanel.removeAll();
        orderInfoPanel.setVisible(false);
        parentPanel.remove(orderInfoPanel);
        parentPanel.remove(invisibleSpace);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if(src.equals(bottomButton)) bottomButtonAction(); else
        if(src.equals(topButton)) topButtonAction();
    }

    protected void createUIComponents() {
        orderListModel = new DefaultListModel<>();
        orderList = new JList<>(orderListModel);
    }

    public boolean isPhoneNumber() {
        return isPhoneNumber;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(boolean isPhoneNumber, String orderNumber) {
        this.orderNumber = orderNumber;
        this.isPhoneNumber = isPhoneNumber;
        String t = orderNumber;
        if(isPhoneNumber){
            t = "(" + orderNumber.substring(0,3) + ")-" + orderNumber.substring(3,6) + "-" +
                    orderNumber.substring(6);
        }
        txtField_orderNumber.setText(t);
    }

    public String getOrderName() {
        return orderName;
    }

    public void setOrderName(String orderName) {
        this.orderName = orderName;
        txtField_name.setText(orderName);
    }

    public long getElapsedTime() {
        return elapsedTime;
    }

    public void setElapsedTime(long elapsedTime) {
        this.elapsedTime = elapsedTime;
        long s = (elapsedTime / 1000) % 60;
        long m = (elapsedTime / (1000 * 60)) % 60;
        long h = (elapsedTime / (1000 * 60 * 60)) % 24;
        txtField_elapsedTime.setText(String.format("%d:%02d:%02d", h, m, s));
    }
}
