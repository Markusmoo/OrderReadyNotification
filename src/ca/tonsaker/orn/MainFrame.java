package ca.tonsaker.orn;

import ca.tonsaker.orn.extraguis.FinishedOrderInfo;
import ca.tonsaker.orn.extraguis.ProgressOrderInfo;
import com.bulenkov.darcula.DarculaLaf;
import ca.tonsaker.orn.extraguis.OrderInfo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Markus Tonsaker on 2017-12-02.
 */
public class MainFrame extends JFrame implements ActionListener{
    private JPanel mainPanel;
    private JTabbedPane tabbedPane;
    private JButton btn_addItemOrder;
    private JSpinner spnr_qty;
    private JList list_placeOrder;
    private JButton btn_saveOrder;
    private JButton btn_removeItemOrder;
    private JList list_itemsPlacedOrder;
    private JPanel orderPlacePanel;
    private JPanel orderProgressPanel;
    private JPanel orderFinishedPanel;
    private JScrollPane scrollPane_finished;
    private JScrollPane scrollPane_progress;
    private JList list_settingsMenuItems;
    private JTextField txtField_settingsNewItem;
    private JButton btn_settingsAddNewItem;
    private JButton btn_settingsClearAllOrders;
    private JButton btn_settingsRemoveSelectedItem;
    private JButton btn_settingsRemoveAllItemsButton;
    private JButton btn_settingsSave;
    private JTextField txtField_nameCompany;
    private JTextField txtField_twilioSID;
    private JTextField txtField_name;
    private JTextField txtField_twilioAPISecret;
    private JTextField txtField_twilioPhoneNumber;
    private JTextField txtField_twilioAPIKey;

    private CFGData cfgData;

    private DefaultListModel<String> placeOrderModel = new DefaultListModel<>();;
    private DefaultListModel<String> placedItemOrderModel = new DefaultListModel<>();;
    private DefaultListModel<String> settingsMenuItemsModel = new DefaultListModel<>();;

    private Timer stopwatchUpdater;

    public static TwilioHandler twilioHandler;

    public static ArrayList<ProgressOrderInfo> progressOrderInfoArrayList = new ArrayList<>();
    public static JPanel accessOrderProgressPanel;

    public static ArrayList<FinishedOrderInfo> finishedOrderInfoArrayList = new ArrayList<>();
    public static JPanel accessOrderFinishedPanel;

    public static void main(String[] args){
        EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                try{
                    UIManager.setLookAndFeel(new DarculaLaf());
                }catch(UnsupportedLookAndFeelException e){
                    System.err.println("This operating system does not support the look and feel!");
                    e.printStackTrace();
                }
                MainFrame window = new MainFrame();
                window.setVisible(true);
            }

        });
    }

    /**
     * TODO Default to maximized window
     * Creates and opens the POS window.
     */
    public MainFrame(){
        super("Window");
        this.setContentPane(mainPanel);
        this.setBounds(100,100,1000,600);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.pack();

        orderProgressPanel.setLayout(new BoxLayout(orderProgressPanel, BoxLayout.Y_AXIS));
        orderFinishedPanel.setLayout(new BoxLayout(orderFinishedPanel, BoxLayout.Y_AXIS));

        btn_addItemOrder.addActionListener(this);
        btn_removeItemOrder.addActionListener(this);
        btn_saveOrder.addActionListener(this);
        btn_settingsAddNewItem.addActionListener(this);
        btn_settingsClearAllOrders.addActionListener(this);
        btn_settingsRemoveAllItemsButton.addActionListener(this);
        btn_settingsRemoveSelectedItem.addActionListener(this);
        btn_settingsSave.addActionListener(this);

        list_settingsMenuItems.setModel(settingsMenuItemsModel);
        list_placeOrder.setModel(placeOrderModel);
        list_itemsPlacedOrder.setModel(placedItemOrderModel);

        MainFrame.accessOrderFinishedPanel = orderFinishedPanel;
        MainFrame.accessOrderProgressPanel = orderProgressPanel;
        orderFinishedPanel.add(Box.createVerticalStrut(10));
        orderProgressPanel.add(Box.createVerticalStrut(10));

        cfgData = new CFGData();
        cfgData.loadCFG();
        twilioHandler = new TwilioHandler(this, cfgData);

        syncData();

        createStopwatchUpdater();
    }

    private void syncData(){
        txtField_nameCompany.setText(cfgData.COMPANY_NAME);
        txtField_twilioSID.setText(cfgData.ACCOUNT_SID);
        txtField_twilioAPIKey.setText(cfgData.API_KEY);
        txtField_twilioAPISecret.setText(cfgData.API_SECRET);
        txtField_twilioPhoneNumber.setText(cfgData.PHONE_NUMBER);
        settingsMenuItemsModel.clear();
        placeOrderModel.clear();
        for(String s : cfgData.MENU_ITEMS){
            settingsMenuItemsModel.addElement(s);
            placeOrderModel.addElement(s);
        }
    }

    private void createStopwatchUpdater(){
        stopwatchUpdater = new Timer(500, this);
        stopwatchUpdater.start();
    }

    private void updateStopwatchTimers(){
        for(OrderInfo info : progressOrderInfoArrayList){
            info.updateClock();
        }
    }

    public void addProgressOrderInfo(ProgressOrderInfo progressOrderInfo){
        progressOrderInfoArrayList.add(progressOrderInfo);
    }

    public void addFinishedOrderInfo(FinishedOrderInfo finishedOrderInfo){
        finishedOrderInfoArrayList.add(finishedOrderInfo);
    }

    /**
     * Calling this will add the item selected, in the menu, to the order list. If no item is selected, only the
     * quantity spinner will reset to 1.
     */
    public void addItemToOrder(){
        int quantity = (int) spnr_qty.getValue();
        spnr_qty.setValue(1);
        if(list_placeOrder.isSelectionEmpty()) return;
        String item = quantity + " - " + list_placeOrder.getSelectedValue().toString();
        placedItemOrderModel.addElement(item);
        list_placeOrder.clearSelection();
    }

    /**
     * Calling this will remove the item selected from the order list. If no item is selected, nothing
     * will happen.
     */
    public void removeItemFromOrder(){
        if(list_itemsPlacedOrder.isSelectionEmpty()) return;
        placedItemOrderModel.remove(list_itemsPlacedOrder.getSelectedIndex());
    }

    public void saveOrder(){
        if(list_itemsPlacedOrder.getModel().getSize() <= 0) return;
        String orderNumber = JOptionPane.showInputDialog(this,
                "Please enter Order Number OR Phone Number\n" +
                "\nPlease type a # before phone number.\nExample: \"#604 123 1234\"", "Confirm Dialog",
                JOptionPane.QUESTION_MESSAGE);
        if(orderNumber == null) return;
        boolean isPhoneNumber = orderNumber.contains("#");
        if(isPhoneNumber) orderNumber = orderNumber.replace("#","");
        orderNumber = orderNumber.replaceAll(" ", "");
        if(!orderNumber.matches("[0-9]+")){
            JOptionPane.showMessageDialog(this, "Please enter only numbers! Try again.",
                    "Error: Numbers only", JOptionPane.ERROR_MESSAGE);
            saveOrder();
            return;
        }
        addProgressOrderInfo(new ProgressOrderInfo(orderProgressPanel, placedItemOrderModel, isPhoneNumber, orderNumber,
                txtField_name.getText()));
        txtField_name.setText("");
        placedItemOrderModel.clear();
        tabbedPane.setSelectedComponent(scrollPane_progress);
    }

    public void settingsAddNewItem(){
        settingsMenuItemsModel.addElement(txtField_settingsNewItem.getText());
    }

    public void settingsClearAllOrders(){
        //TODO
    }

    public void settingsRemoveAllItems(){
        settingsMenuItemsModel.clear();
    }

    public void settingsRemoveSelectedItem(){
        if(list_settingsMenuItems.isSelectionEmpty()) return;
        settingsMenuItemsModel.removeElement(list_settingsMenuItems.getSelectedValue());
    }

    public void settingsSave(){
        try{
            Object[] objectArray = settingsMenuItemsModel.toArray();
            String[] menuItems = new String[objectArray.length];
            for(int idx = 0; idx < menuItems.length; idx++){
                menuItems[idx] = objectArray[idx].toString();
            }

            cfgData = new CFGData(txtField_twilioSID.getText(), txtField_twilioAPIKey.getText(), txtField_twilioAPISecret.getText(),
                    txtField_twilioPhoneNumber.getText(), txtField_nameCompany.getText(),menuItems);
            placeOrderModel.clear();
            cfgData.saveCFG();
        }catch (IOException e){
            System.err.println("Error occurred when saving files.");
            e.printStackTrace();
        }
        syncData();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if(src.equals(stopwatchUpdater)) updateStopwatchTimers(); else
        if(src.equals(btn_addItemOrder)) addItemToOrder(); else
        if(src.equals(btn_removeItemOrder)) removeItemFromOrder(); else
        if(src.equals(btn_saveOrder)) saveOrder(); else
        if(src.equals(btn_settingsAddNewItem)) settingsAddNewItem(); else
        if(src.equals(btn_settingsClearAllOrders)) settingsClearAllOrders(); else
        if(src.equals(btn_settingsRemoveAllItemsButton)) settingsRemoveAllItems(); else
        if(src.equals(btn_settingsRemoveSelectedItem)) settingsRemoveSelectedItem(); else
        if(src.equals(btn_settingsSave)) settingsSave();
    }

    private void createUIComponents() {
        SpinnerModel sModel = new SpinnerNumberModel(1,1,99,1);
        spnr_qty = new JSpinner(sModel);
    }
}
