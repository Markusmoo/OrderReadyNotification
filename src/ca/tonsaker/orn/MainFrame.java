package ca.tonsaker.orn;

import ca.tonsaker.orn.extraguis.FinishedOrderInfo;
import ca.tonsaker.orn.extraguis.OrderOptionsGUI;
import ca.tonsaker.orn.extraguis.ProgressOrderInfo;
import com.bulenkov.darcula.DarculaLaf;
import ca.tonsaker.orn.extraguis.OrderInfo;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by Markus Tonsaker on 2017-12-02.
 */
public class MainFrame extends JFrame implements ActionListener, WindowListener{

    public static final String TITLE = "ReadySteadyPOS";
    public static final String VERSION = "1.0";

    public static final int BUTTON_WIDTH = 200;
    public static final int BUTTON_HEIGHT = 200;

    public static int AUTOSAVE_CLOCK_MAX = 30;

    public int autosaveClockCount = 30;

    public JPanel mainPanel;
    public JTabbedPane tabbedPane;
    private JButton btn_saveOrder;
    private JButton btn_removeItemOrder;
    private ca.tonsaker.orn.extraguis.JColorList list_itemsPlacedOrder;
    private JPanel orderPlacePanel;
    private JPanel orderProgressPanel;
    private JPanel orderFinishedPanel;
    private JPanel placeOrderButtonsPanel;
    public JScrollPane scrollPane_finished;
    public JScrollPane scrollPane_progress;
    private ca.tonsaker.orn.extraguis.JColorList list_settingsMenuItems;
    private JTextField txtField_settingsNewItem;
    private JButton btn_settingsAddNewItem;
    private JButton btn_settingsClearAllOrders;
    private JButton btn_settingsRemoveSelectedItem;
    private JButton btn_settingsRemoveAllItemsButton;
    private JButton btn_settingsSave;
    private JTextField txtField_nameCompany;
    private JTextField txtField_twilioSID;
    private JTextField txtField_twilioAPISecret;
    private JTextField txtField_twilioPhoneNumber;
    private JTextField txtField_twilioAPIKey;
    private JCheckBox chkBX_displayOrder;

    private CFGData cfgData;

    public DefaultListModel<String> placedItemOrderModel = new DefaultListModel<>();;
    public DefaultListModel<String> settingsMenuItemsModel = new DefaultListModel<>();;

    private Timer stopwatchUpdater;

    public static TwilioHandler twilioHandler;

    public static ArrayList<JButton> orderButtons = new ArrayList<>();

    private static JPanel accessOrderProgressPanel;
    private static JPanel accessOrderFinishedPanel;

    public static ArrayList<ProgressOrderInfo> progressOrderInfoArrayList = new ArrayList<>();
    public static ArrayList<FinishedOrderInfo> finishedOrderInfoArrayList = new ArrayList<>();

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
     * Creates and opens the POS window.
     */
    public MainFrame(){
        super(MainFrame.TITLE);
        this.setContentPane(mainPanel);
        this.setBounds(100,100,1000,600);
        this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(this);
        this.pack();
        this.setExtendedState( this.getExtendedState()|JFrame.MAXIMIZED_BOTH );

        orderProgressPanel.setLayout(new BoxLayout(orderProgressPanel, BoxLayout.Y_AXIS));
        orderFinishedPanel.setLayout(new BoxLayout(orderFinishedPanel, BoxLayout.Y_AXIS));
        placeOrderButtonsPanel.setLayout(new GridLayout(0, 3,5,5));//placeOrderButtonsPanel.getWidth()/MainFrame.BUTTON_WIDTH, 5, 5));

        btn_removeItemOrder.addActionListener(this);
        btn_saveOrder.addActionListener(this);
        btn_settingsAddNewItem.addActionListener(this);
        btn_settingsClearAllOrders.addActionListener(this);
        btn_settingsRemoveAllItemsButton.addActionListener(this);
        btn_settingsRemoveSelectedItem.addActionListener(this);
        btn_settingsSave.addActionListener(this);

        list_settingsMenuItems.setModel(settingsMenuItemsModel);
        list_itemsPlacedOrder.setModel(placedItemOrderModel);

        orderFinishedPanel.add(Box.createVerticalStrut(10));
        orderProgressPanel.add(Box.createVerticalStrut(10));

        accessOrderProgressPanel = orderProgressPanel;
        accessOrderFinishedPanel = orderFinishedPanel;

        cfgData = new CFGData();
        cfgData.loadCFG();
        OrderInfo.loadData(this);

        syncData();

        createStopwatchUpdater();
    }

    public static JPanel getProgressOrderPanel(){
        return accessOrderProgressPanel;
    }

    public static JPanel getFinishedOrderPanel(){
        return accessOrderFinishedPanel;
    }

    private void syncData(){
        txtField_nameCompany.setText(cfgData.COMPANY_NAME);
        txtField_twilioSID.setText(cfgData.ACCOUNT_SID);
        txtField_twilioAPIKey.setText(cfgData.API_KEY);
        txtField_twilioAPISecret.setText(cfgData.API_SECRET);
        txtField_twilioPhoneNumber.setText(cfgData.PHONE_NUMBER);
        chkBX_displayOrder.setSelected(cfgData.SEND_ORDER);
        settingsMenuItemsModel.clear();
        placeOrderButtonsPanel.removeAll(); //TODO May remove it self
        for(String s : cfgData.MENU_ITEMS){
            settingsMenuItemsModel.addElement(s);
            addOrderButton(s);
        }
        twilioHandler = new TwilioHandler(this, cfgData);
    }

    public void removeAllOrderButtons(){
        for(Iterator<JButton> i = orderButtons.iterator(); i.hasNext();){
            JButton b = i.next();
            b.removeAll();
            b.setVisible(false);
            i.remove();
        }
    }

    public void addOrderButton(String name){
        JButton b = new JButton(name);
        b.addActionListener(this);
        b.setFont(new Font(null,Font.BOLD,20));
        b.setForeground(Utilities.randomColorWord(name));
        b.setBackground(Color.GRAY);
        placeOrderButtonsPanel.add(b);
        orderButtons.add(b);
    }

    private void createStopwatchUpdater(){
        stopwatchUpdater = new Timer(500, this);
        stopwatchUpdater.start();
    }

    private void updateAutosaveClock(){
        autosaveClockCount--;
        if(autosaveClockCount <= 0){
            for(OrderInfo info : progressOrderInfoArrayList){
                try{ info.saveData(); }catch(IOException e){ e.printStackTrace(); }
            }
            for(OrderInfo info : finishedOrderInfoArrayList){
                try{ info.saveData(); }catch(IOException e){ e.printStackTrace(); }
            }
            autosaveClockCount = AUTOSAVE_CLOCK_MAX;
        }
    }

    private void updateStopwatchTimers(){
        for(OrderInfo info : progressOrderInfoArrayList){
            info.updateClock();
        }
        updateAutosaveClock();
    }

    /**
     * TODO Make this the proper way to add to parent panel
     * @param progressOrderInfo
     */
    public static void addProgressOrderInfo(ProgressOrderInfo progressOrderInfo){
        progressOrderInfoArrayList.add(progressOrderInfo);
        progressOrderInfo.addToParentPanel();
        System.out.println("Moved "+progressOrderInfo.getOrderNumber()+" to PROGRESS");
    }

    /**
     * TODO Make this the proper way to add to parent panel
     * @param finishedOrderInfo
     */
    public static void addFinishedOrderInfo(FinishedOrderInfo finishedOrderInfo){
        finishedOrderInfoArrayList.add(finishedOrderInfo);
        finishedOrderInfo.addToParentPanel();
        System.out.println("Moved "+finishedOrderInfo.getOrderNumber()+" to FINISHED");
    }

    /**
     * TODO Combine items
     * Calling this will add the item to the order list.
     *
     * @param item the name of the item to add to the order
     */
    public void addItemToOrder(String item){
        //placedItemOrderModel.contains() TODO Change all order items from strings to objects
        placedItemOrderModel.addElement(item);
    }

    /**
     * Calling this will remove the item selected from the order list. If no item is selected, nothing
     * will happen.
     */
    public void removeItemFromOrder(){
        if(list_itemsPlacedOrder.isSelectionEmpty()) return;
        placedItemOrderModel.remove(list_itemsPlacedOrder.getSelectedIndex());
    }

    /**
     * TODO Find out what this does
     */
    public void loadOrders(){
        String path = System.getenv("APPDATA")+"\\ORN\\config.json";
        Reader reader;
        try{
            reader = new InputStreamReader(new FileInputStream(path));
        }catch(FileNotFoundException e){
            File f = new File(path);
            f.getParentFile().mkdirs();
            try{
                f.createNewFile();
            }catch(IOException e2){
                e2.printStackTrace();
            }
            return;
        }
        Gson gson = new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().create();
        CFGData cfgData = gson.fromJson(reader, CFGData.class);

        System.out.println("Config file successfully loaded from \"" + path + "\"");
    }

    public void saveOrder(){
        if(list_itemsPlacedOrder.getModel().getSize() <= 0) return;
        /*String orderNumber = JOptionPane.showInputDialog(this,
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
        }*/
        new OrderOptionsGUI(this);
        /*addProgressOrderInfo(new ProgressOrderInfo(orderProgressPanel, placedItemOrderModel, orderNumber,  //TODO Moved to OrderOptionsGUI.java
                txtField_name.getText()));
        txtField_name.setText("");
        placedItemOrderModel.clear();
        tabbedPane.setSelectedComponent(scrollPane_progress);*/
    }

    public void settingsAddNewItem(){
        settingsMenuItemsModel.addElement(txtField_settingsNewItem.getText());
        txtField_settingsNewItem.setText("");
    }

    public void settingsClearAllOrders(){
        for(Iterator<FinishedOrderInfo> i = finishedOrderInfoArrayList.iterator(); i.hasNext();){
            FinishedOrderInfo o = i.next();
            i.remove();
            o.selfDestruct();
        }
        for(Iterator<ProgressOrderInfo> i = progressOrderInfoArrayList.iterator(); i.hasNext();){
            ProgressOrderInfo o = i.next();
            i.remove();
            o.selfDestruct();
        }
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
                    txtField_twilioPhoneNumber.getText(), txtField_nameCompany.getText(), chkBX_displayOrder.isSelected(), menuItems);
            removeAllOrderButtons();
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
        if(src.equals(stopwatchUpdater)){
            updateStopwatchTimers();
            return;
        }
        for(Iterator<JButton> i = orderButtons.iterator(); i.hasNext();){
            JButton b = i.next();
            if(src.equals(b)){
                this.addItemToOrder(b.getText());
                return;
            }
        }
        if(src.equals(btn_removeItemOrder)) removeItemFromOrder(); else
        if(src.equals(btn_saveOrder)) saveOrder(); else
        if(src.equals(btn_settingsAddNewItem)) settingsAddNewItem(); else
        if(src.equals(btn_settingsClearAllOrders)) settingsClearAllOrders(); else
        if(src.equals(btn_settingsRemoveAllItemsButton)) settingsRemoveAllItems(); else
        if(src.equals(btn_settingsRemoveSelectedItem)) settingsRemoveSelectedItem(); else
        if(src.equals(btn_settingsSave)) settingsSave();
    }

    @Override
    public void windowOpened(WindowEvent e) {}

    @Override
    public void windowClosing(WindowEvent e) {
        String objButtons[] = {"Yes","No"};
        int PromptResult = JOptionPane.showOptionDialog(this,"Are you sure you want to exit?",
                "Are you sure?",JOptionPane.DEFAULT_OPTION,JOptionPane.WARNING_MESSAGE,null,objButtons,objButtons[1]);
        if(PromptResult == JOptionPane.YES_OPTION){
            System.exit(0);
        }
    }

    @Override
    public void windowClosed(WindowEvent e) {}

    @Override
    public void windowIconified(WindowEvent e) {}

    @Override
    public void windowDeiconified(WindowEvent e) {}

    @Override
    public void windowActivated(WindowEvent e) {}

    @Override
    public void windowDeactivated(WindowEvent e) {}
}
