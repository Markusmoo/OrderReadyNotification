package ca.tonsaker.orn.extraguis;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Markus Tonsaker on 2017-12-04.
 */
public class FinishedOrderInfo extends OrderInfo{

    public FinishedOrderInfo(JPanel parent, DefaultListModel<String> list, boolean isPhoneNumber, String orderNumber, String orderName, long elapsedTime){
        super(parent, list, isPhoneNumber, orderNumber, orderName);

        this.elapsedTime = elapsedTime;
        orderReadyButton.setText("Done");
        cancelOrderButton.setText("Send SMS");
        cancelOrderButton.setForeground(new Color(5,0,65));
        cancelOrderButton.setBackground(new Color(5,0,65));
        txtField_elapsedTime.setEnabled(false); //TODO Test
        if(!isPhoneNumber) {
            cancelOrderButton.setEnabled(false);
        }
    }

    public FinishedOrderInfo(OrderInfo info){
        this(info.parentPanel, info.orderListModel, info.isPhoneNumber, info.orderNumber, info.orderName, info.elapsedTime);
    }

    /**
     * Overrides orderReady() in OrderInfo.java class and serves this function as a 'order done' type function.\n\n
     *
     * Greys out the OrderInfo panel to indicate that the order has been dealt with.
     */
    @Override
    public void orderReady() {
        Color defaultColor = new Color(60,63,65);
        cancelOrderButton.setBackground(defaultColor);
        cancelOrderButton.setForeground(defaultColor);
        orderReadyButton.setBackground(defaultColor);
        orderReadyButton.setBackground(defaultColor);
        cancelOrderButton.setEnabled(false);
        orderReadyButton.setEnabled(false);
        txtField_elapsedTime.setEnabled(false);
        txtField_orderNumber.setEnabled(false);
        txtField_name.setEnabled(false);
        orderList.setEnabled(false);
        lbl_eTime.setEnabled(false);
        lbl_name.setEnabled(false);
        lbl_orderNumber.setEnabled(false);
    }

    /**
     * Overrides orderCancel() in OrderInfo.java class and serves this function as a 'send SMS' type function.\n\n
     *
     * Sends a text message reminder to the customer.
     */
    @Override
    public void orderCancel() {

    }
}
