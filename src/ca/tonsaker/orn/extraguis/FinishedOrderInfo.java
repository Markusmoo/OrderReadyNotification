package ca.tonsaker.orn.extraguis;

import ca.tonsaker.orn.MainFrame;

import javax.swing.*;
import java.awt.*;

/**
 * TODO Fix when pressing DONE, carryout/toStay stays orange.
 *
 * Created by Markus Tonsaker on 2017-12-04.
 */
public class FinishedOrderInfo extends OrderInfo{

    public FinishedOrderInfo(JPanel parent, DefaultListModel<String> list, boolean toStay, String orderNumber, String phoneNumber, String orderName, long elapsedTime){
        super(parent, list, toStay, orderNumber, phoneNumber, orderName, elapsedTime);

        this.elapsedTime = elapsedTime;
        aButton.setText("Done");
        aButton.setForeground(new Color(60, 60, 0));
        aButton.setBackground(new Color(60, 60, 0));
        bButton.setText("Send SMS");
        if(!phoneNumber.equals("")) {
            bButton.setForeground(new Color(5, 0, 65));
            bButton.setBackground(new Color(5, 0, 65));
        }
        txtField_elapsedTime.setEnabled(false); //TODO Test
        if(phoneNumber.equals("")) {
            bButton.setForeground(new Color(60, 63, 65));
            bButton.setEnabled(false);
        }
    }

    public FinishedOrderInfo(JPanel newPanel, OrderInfo info){
        this(newPanel, info.orderListModel, info.toStay, info.orderNumber, info.phoneNumber, info.orderName, info.elapsedTime);
    }

    @Override
    public void addToParentPanel() {
        parentPanel.add(orderInfoPanel, 1);
        invisibleSpace = Box.createVerticalStrut(10);
        parentPanel.add(invisibleSpace, 2);
        orderInfoPanel.setMaximumSize(new Dimension(900,160));
        orderInfoPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
    }

    /**
     * Greys out the OrderInfo panel to indicate that the order has been dealt with.
     */
    public void orderDone(){
        if(JOptionPane.showConfirmDialog(orderInfoPanel.getParent(), "Are you sure you are done with this order?",
                "Cancel Order?", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE) != 0) return;
        Color defaultColor = new Color(60,63,65);
        bButton.setBackground(defaultColor);
        bButton.setForeground(defaultColor);
        aButton.setBackground(defaultColor);
        aButton.setForeground(defaultColor);
        bButton.setEnabled(false);
        aButton.setEnabled(false);
        txtField_elapsedTime.setEnabled(false);
        txtField_orderNumber.setEnabled(false);
        txtField_name.setEnabled(false);
        orderList.setEnabled(false);
        lbl_eTime.setEnabled(false);
        lbl_name.setEnabled(false);
        lbl_orderNumber.setEnabled(false);
    }

    /**
     * Sends a text message reminder to the customer.
     */
    public void orderSendSMS(){
        if(JOptionPane.showConfirmDialog(orderInfoPanel.getParent(), "Are you sure you would like to resend a SMS Notification?",
                "Cancel Order?", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE) != 0) return;
        MainFrame.twilioHandler.sendNotification(orderName, orderNumber, orderListModel);
    }

    /**
     * Deletes this panel from existence.
     */
    public void selfDestruct(){
        super.selfDestruct();
        if(MainFrame.finishedOrderInfoArrayList.contains(this)) MainFrame.finishedOrderInfoArrayList.remove(this);
    }

    /**
     * Overrides topButtonAction() in OrderInfo.java class and calls the orderDone() function.
     */
    protected void topButtonAction() {
        orderDone();
    }

    /**
     * Overrides bottomButtonAction() in OrderInfo.java class and calls the orderSendSMS() function.\n\n
     */
    protected void bottomButtonAction() {
        orderSendSMS();
    }
}
