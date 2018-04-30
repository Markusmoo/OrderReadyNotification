package ca.tonsaker.orn.extraguis;

import ca.tonsaker.orn.MainFrame;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 *
 * Created by Markus Tonsaker on 2017-12-04.
 */
public class ProgressOrderInfo extends OrderInfo{

    public ProgressOrderInfo(JPanel parent, DefaultListModel<String> list, boolean toStay, String orderNumber, String phoneNumber, String orderName){
        super(parent, list, toStay, orderNumber, phoneNumber, orderName, 0, System.currentTimeMillis());

        aButton.setText("Order Ready");
        aButton.setForeground(new Color(7,40,0));
        aButton.setBackground(new Color(7,40,0));
        bButton.setText("Cancel Order");
        bButton.setForeground(new Color(65,12,13));
        bButton.setBackground(new Color(65,12,13));
    }

    public ProgressOrderInfo(JPanel parent, OrderData orderData){
        super(parent, orderData);

        aButton.setText("Order Ready");
        aButton.setForeground(new Color(7,40,0));
        aButton.setBackground(new Color(7,40,0));
        bButton.setText("Cancel Order");
        bButton.setForeground(new Color(65,12,13));
        bButton.setBackground(new Color(65,12,13));

        beginTime = orderData.getBeginTime();
    }

    @Override
    public void addToParentPanel() {
        parentPanel.add(orderInfoPanel);
        invisibleSpace = Box.createVerticalStrut(10);
        parentPanel.add(invisibleSpace);
        orderInfoPanel.setMaximumSize(new Dimension(900,160));
        orderInfoPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
    }

    protected void topButtonAction(){
        orderReady();
    }

    protected void bottomButtonAction(){
        orderCancel();
    }

    public void orderReady(){
        if(!phoneNumber.isEmpty()) {
            int i = JOptionPane.showOptionDialog(parentPanel, "Would you like to send a SMS Notification to "+txtField_orderNumber.getText()+"?",
                    "SMS Option Window", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
            if(i == 2 || i == -1){
                return;
            }else if(i == 1){
                sendToFinishedOrders();
            }else if(i == 0) {
                MainFrame.twilioHandler.sendNotification(orderName, orderNumber, orderListModel);
                sendToFinishedOrders();
            }
        }else{
            if(JOptionPane.showConfirmDialog(orderInfoPanel.getParent(), "Are you sure this order is ready?",
                    "Order Ready?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) != 0) return;
            sendToFinishedOrders();
        }
    }

    public void orderCancel(){
        if(JOptionPane.showConfirmDialog(orderInfoPanel.getParent(), "Are you sure you want to cancel this order?",
                "Cancel Order?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) != 0) return;
        //TODO Delete save file
        selfDestruct();
    }

    private void sendToFinishedOrders(){
        MainFrame.addFinishedOrderInfo(new FinishedOrderInfo(MainFrame.getFinishedOrderPanel(), this));
        selfDestruct();
    }

    /**
     * Deletes this panel from existence.
     */
    public void selfDestruct(){
        super.selfDestruct();
        if(MainFrame.progressOrderInfoArrayList.contains(this)) MainFrame.progressOrderInfoArrayList.remove(this);
    }
}
