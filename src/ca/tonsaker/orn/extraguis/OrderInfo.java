package ca.tonsaker.orn.extraguis;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * TODO Create a way to make this panel loadable from the DiskDrive
 *
 * A Intellij Form that displays information on a placed order.
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

    /**
     * Creates a populated JPanel that appends itself to the 'parent' parameter.<p>
     * Contains information like Customer's name (optional), phone/order number, ordered items list,
     * and the elapsed time since the order was placed. Also contains 2 stacked buttons that can be
     * overriden.
     *
     * @param parent the JPanel where the OrderInfo panel will be displayed
     * @param list the items ordered
     * @param isPhoneNumber if the 'orderNumber' is a phone number
     * @param orderNumber either a phone number or a order number
     * @param orderName name of the customer who the order belongs to
     * @param elapsedTime the initial or final time elapsed since the order was placed
     */
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

        addToParentPanel();

        setupClock();
    }

    /**
     * Invoke to determine how the OrderInfo is added to the parent panel.
     */
    public abstract void addToParentPanel();

    /**
     * Invoke to determine the action of pressing the top button.
     */
    protected abstract void topButtonAction();

    /**
     * Invoke to determine the action of pressing the bottom button.
     */
    protected abstract void bottomButtonAction();

    /**
     * Changes the elapsed time to 0 and sets up the elapsed time stopwatch.
     */
    public void setupClock(){
        elapsedTime = 0;
        beginTime = System.currentTimeMillis();
        clockRunning = true;
    }

    /**
     * Called to update the elapsed time since the order was placed.
     */
    public void updateClock(){
        if(clockRunning) {
            elapsedTime = System.currentTimeMillis() - beginTime;
            long s = (elapsedTime / 1000) % 60;
            long m = (elapsedTime / (1000 * 60)) % 60;
            long h = (elapsedTime / (1000 * 60 * 60)) % 24;
            txtField_elapsedTime.setText(String.format("%d:%02d:%02d", h, m, s));
        }
    }

    /**
     * Prevents the elapsed time stopwatch from updating.
     */
    public void stopClock(){
        clockRunning = false;
    }

    /**
     * Deletes this panel from existence.
     */
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

    /**
     * Custom creates components.
     */
    protected void createUIComponents() {
        orderListModel = new DefaultListModel<>();
        orderList = new JList<>(orderListModel);
    }

    /**
     * @return whether the 'orderNumber' variable is a phone number
     */
    public boolean isPhoneNumber() {
        return isPhoneNumber;
    }

    /**
     * @return the order/phone number
     */
    public String getOrderNumber() {
        return orderNumber;
    }

    /**
     * Updates the textfield and variable that contains the order/phone number.
     *
     * @param isPhoneNumber whether the 'orderNumber' variable is a phone number
     * @param orderNumber an order number or phone number
     */
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

    /**
     * @return the name of the order owner.  Will return an empty string if there is no name.
     */
    public String getOrderName() {
        return orderName;
    }

    /**
     * @param orderName the name of the order owner.
     */
    public void setOrderName(String orderName) {
        this.orderName = orderName;
        txtField_name.setText(orderName);
    }

    /**
     * @return the elapsed time since the order was placed or since the stopwatch was stopped.
     */
    public long getElapsedTime() {
        return elapsedTime;
    }

    /**
     * Updates the textfield and variable that contains the elapsed time.
     *
     * @param elapsedTime the new elapsed time
     */
    public void setElapsedTime(long elapsedTime) {
        this.elapsedTime = elapsedTime;
        long s = (elapsedTime / 1000) % 60;
        long m = (elapsedTime / (1000 * 60)) % 60;
        long h = (elapsedTime / (1000 * 60 * 60)) % 24;
        txtField_elapsedTime.setText(String.format("%d:%02d:%02d", h, m, s));
    }
}
