package ca.tonsaker.orn.extraguis;

import ca.tonsaker.orn.MainFrame;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * TODO "date": "2018:00:03", (Check to see if the month "00" actually changes..)
 * TODO Fix sending SMS message
 * TODO Fix ProgressOrderInfo state turning into FinishedOrderInfo state.
 *
 * A IntelliJ Form that displays information on a placed order.
 *
 * Created by Markus Tonsaker on 2017-12-02.
 */
public abstract class OrderInfo implements ActionListener{
    protected JTextField txtField_orderNumber;
    protected JPanel ordersPanel;
    protected JPanel orderInfoPanel;
    protected JTextField txtField_elapsedTime;
    protected JButton aButton;
    protected JButton bButton;
    protected JColorList orderList;
    protected JTextField txtField_name;
    protected JLabel lbl_orderNumber;
    protected JLabel lbl_name;
    protected JLabel lbl_eTime;
    private JTextField txtField_phoneNumber;

    private ButtonGroup radioGroup;
    private JRadioButton toStayRadioButton;
    private JRadioButton toGoRadioButton;
    private JButton button1;

    protected JPanel parentPanel;
    protected Component invisibleSpace;

    protected DefaultListModel<String> orderListModel;

    protected boolean clockRunning = false;

    protected long elapsedTime;
    protected long beginTime;

    protected String phoneNumber;
    protected String orderNumber;
    protected boolean toStay;
    protected String orderName;

    protected OrderData orderData;

    public class OrderData{

        public static final int STATE_ARCHIVED = -1;
        public static final int STATE_PROGRESS = 0;
        public static final int STATE_FINISHED = 1;
        public static final int STATE_FINISHED_DONE = 2;

        @Expose private String date;
        @Expose private long beginTime;
        @Expose private String customerName;
        @Expose private String orderNumber;
        @Expose private String phoneNumber;
        @Expose private String elapsedTime;
        @Expose private boolean isToStay;
        @Expose private int state;
        @Expose private String[] orderList;

        private String savePath;

        public long getElapsedTimeLong(){
            return parseTime(getElapsedTime());
        }

        private long parseTime(String time){
            int colonIdx = time.indexOf(':');
            long newTime = Long.parseLong(time.substring(0, colonIdx)) * 60000;
            newTime += Long.parseLong(time.substring(colonIdx+1)) * 1000;
            return newTime;
        }

        public DefaultListModel<String> getOrderListModel() {
            DefaultListModel<String> list = new DefaultListModel<>();
            for(String item : getOrderList()) {
                list.addElement(item);
            }
            return list;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public long getBeginTime(){ return this.beginTime; }

        public void setBeginTime(long beginTime){ this.beginTime = beginTime; }

        public String getOrderName() {
            return customerName;
        }

        public void setOrderName(String customerName) {
            this.customerName = customerName;
        }

        public String getOrderNumber() {
            return orderNumber;
        }

        public void setOrderNumber(String orderNumber) {
            this.orderNumber = orderNumber;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }

        public String getElapsedTime() {
            return elapsedTime;
        }

        public void setElapsedTime(String elapsedTime) {
            this.elapsedTime = elapsedTime;
        }

        public int getState(){
            return state;
        }

        public void setState(int state){
            this.state = state;
        }

        public boolean isToStay() {
            return isToStay;
        }

        public void setToStay(boolean toStay) {
            this.isToStay = toStay;
        }

        public String[] getOrderList() {
            return orderList;
        }

        public void setOrderList(String[] orderList) {
            this.orderList = orderList;
        }

        public void setSavePath(String savePath){
            this.savePath = savePath;
        }

        public String getSavePath(){
            return savePath;
        }

    }

    /** TODO add rest of order details
     * Creates a populated JPanel that appends itself to the 'parent' parameter.<p>
     * Contains information like Customer's name (optional), phone/order number, ordered items list,
     * and the elapsed time since the order was placed. Also contains 2 stacked buttons that can be
     * overriden.
     *
     * @param parent the JPanel where the OrderInfo panel will be displayed
     * @param list the items ordered
     * @param isToStay is the order to stay or to go
     * @param state state of order as of OrderInfo
     * @param orderNumber order number of the order
     * @param phoneNumber order phone number of customer
     * @param orderName name of the customer who the order belongs to
     * @param elapsedTime the final time elapsed since the order was placed
     * @param beginTime system time of when the order was placed
     * @param savePath file location of save
     */
    public OrderInfo(JPanel parent, DefaultListModel<String> list, boolean isToStay, int state, String orderNumber, String phoneNumber,
                     String orderName, long elapsedTime, long beginTime, String savePath){
        orderListModel = new DefaultListModel<>();
        orderList.setModel(orderListModel);
        for(int idx = 0; idx < list.getSize(); idx++) {
            orderListModel.addElement(list.getElementAt(idx));
        }

        parentPanel = parent;

        setOrderNumber(orderNumber);
        if(phoneNumber != null && !phoneNumber.isEmpty()) this.setPhoneNumber(phoneNumber);
        setOrderName(orderName);
        setElapsedTime(elapsedTime);

        bButton.addActionListener(this);
        aButton.addActionListener(this);

        radioGroup = new ButtonGroup();
        radioGroup.add(toStayRadioButton);
        radioGroup.add(toGoRadioButton);

        setIsToStay(isToStay);

        Calendar cal = Calendar.getInstance();

        orderData = new OrderData();
        orderData.setSavePath(savePath);
        orderData.setToStay(isToStay);
        orderData.setOrderName(getOrderName());
        orderData.setElapsedTime(String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(getElapsedTime()),
                TimeUnit.MILLISECONDS.toSeconds(getElapsedTime()) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(getElapsedTime()))));
        orderData.setBeginTime(beginTime);
        orderData.setOrderNumber(getOrderNumber());
        orderData.setPhoneNumber(getPhoneNumber());
        orderData.setDate(String.format("%02d-%02d-%02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)));
        String[] menuItems = new String[orderListModel.toArray().length];
        for(int idx = 0; idx < menuItems.length; idx++){
            menuItems[idx] = orderListModel.toArray()[idx].toString();
        }
        orderData.setState(state);
        orderData.setOrderList(menuItems);

        setupClock();
        try{
            saveData();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    /**
     * Creates a populated JPanel that appends itself to the 'parent' parameter.<p>
     * Contains information like Customer's name (optional), phone/order number, ordered items list,
     * and the elapsed time since the order was placed. Also contains 2 stacked buttons that can be
     * overriden.
     *
     * @param oData the data object to build the order info from
     * @param parentPanel the JPanel where the OrderInfo panel will be displayed
     */
    public OrderInfo(JPanel parentPanel, OrderData oData, String savePath){
        this(parentPanel, oData.getOrderListModel(), oData.isToStay(), oData.getState(), oData.getOrderNumber(), oData.getPhoneNumber(), oData.getOrderName(), oData.getElapsedTimeLong(), oData.getBeginTime(), savePath);
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
     * Loads OrderInfo panels onto the frame.
     *
     * @param frame to load OrderInfos into.
     */
    public static void loadData(MainFrame frame){
        String path = System.getenv("APPDATA") + "\\ORN\\orders\\";
        Reader reader;
        File fileDir = new File(path);
        if(!fileDir.exists()){
            fileDir.mkdirs();
            return;
        }
        Gson gson = new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().create();
        try{
            System.out.println(fileDir.listFiles().length);
            for(File f : fileDir.listFiles()){
                if(f.getName().contains("order_") && f.getName().contains(".json")){
                    reader = new InputStreamReader(new FileInputStream(f));
                    OrderData orderData = gson.fromJson(reader, OrderData.class);

                    if(orderData.state == OrderData.STATE_PROGRESS){
                        MainFrame.addProgressOrderInfo(new ProgressOrderInfo(frame.getProgressOrderPanel(), orderData, f.getPath()));
                    }else if(orderData.state == OrderData.STATE_FINISHED || orderData.state == OrderData.STATE_FINISHED_DONE){
                        MainFrame.addFinishedOrderInfo(new FinishedOrderInfo(frame.getFinishedOrderPanel(), orderData, f.getPath()));
                    }
                }
            }
        }catch(FileNotFoundException e){
            e.printStackTrace();
        }

        System.out.println("Config file successfully loaded from \"" + path + "\"");
    }

    /**
     * Save orderinfo to %appdata%\ORN\order\order_?????.json
     * ????? = date and time of order.
     *
     * @throws IOException if file cannot be saved.
     */
    public void saveData() throws IOException{
        orderData.setElapsedTime(String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(getElapsedTime()),
                TimeUnit.MILLISECONDS.toSeconds(getElapsedTime()) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(getElapsedTime()))));

        if(orderData.getSavePath() == null || orderData.getSavePath().isEmpty()) {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HHmmss");
            Date date = new Date();
            orderData.setSavePath(System.getenv("APPDATA") + "\\ORN\\orders\\order_" + dateFormat.format(date) + ".json");
        }

        File f = new File(orderData.getSavePath());
        if(!f.exists()) f.getParentFile().mkdirs();

        Writer writer = new OutputStreamWriter(new FileOutputStream(orderData.getSavePath()));
        Gson gson = new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().create();

        writer.write(gson.toJson(orderData));
        writer.flush();
        writer.close();

        System.out.println("Order file successfully saved to \"" + orderData.getSavePath() + "\"");
    }

    /**
     * Changes the elapsed time to 0 and sets up the elapsed time stopwatch.
     */
    public void setupClock(){
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
            txtField_elapsedTime.setText(String.format("%02d:%02d:%02d", h, m, s));
        }
    }

    /**
     * Prevents the elapsed time stopwatch from updating.
     */
    public void stopClock(){
        clockRunning = false;
    }

    /**
     * Archives file. OrderInfo panel will not show up when program started. File will still exist for record purposes.
     */
    public void archiveFile(){
        this.orderData.setState(OrderData.STATE_ARCHIVED);
        try{
            this.saveData();
        }catch(IOException e){
            e.printStackTrace();
        }
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

    /**
     * Button action events.
     *
     * @param e triggering action event
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if(src.equals(bButton)) bottomButtonAction(); else
        if(src.equals(aButton)) topButtonAction();
    }

    /**
     * @return True if the order is to stay. False if to go.
     */
    public boolean isToStay(){
        return toStay;
    }

    /**
     * @param toStay True if the order is "to stay". False if "to go".
     */
    public void setIsToStay(boolean toStay){
        if(toStay){
           toStayRadioButton.setSelected(true);
           radioGroup.setSelected(toStayRadioButton.getModel(), true);
           toStayRadioButton.setBackground(new Color(187, 94, 0));
           toGoRadioButton.setBackground(new Color(60, 63, 65));
        }else{
            toGoRadioButton.setSelected(true);
            radioGroup.setSelected(toGoRadioButton.getModel(), true);
            toGoRadioButton.setBackground(new Color(187, 94, 0));
            toStayRadioButton.setBackground(new Color(60, 63, 65));
        }
        this.toStay = toStay;
    }

    /**
     *
     * @return phone number of customer. Null if no phone number.
     */
    public String getPhoneNumber(){
        return phoneNumber;
    }

    //TODO Improve number formatting for non 10 digit numbers

    /**
     * Formatting example: (111)-111-1111
     *
     * @param phoneNumber String representation of phone number.
     */
    public void setPhoneNumber(String phoneNumber){
        String t = phoneNumber;
        t = "(" + phoneNumber.substring(0,3) + ")-" + phoneNumber.substring(3,6) + "-" +
                phoneNumber.substring(6);
        this.phoneNumber = phoneNumber;
        txtField_phoneNumber.setText(t);
    }

    /**
     * @return the order number
     */
    public String getOrderNumber(){
        return orderNumber;
    }

    /**
     * Updates the textfield and variable that contains the order/phone number.
     *
     * @param orderNumber an order number or phone number
     */
    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
        txtField_orderNumber.setText(orderNumber);
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
        txtField_elapsedTime.setText(String.format("%02d:%02d:%02d", h, m, s));
    }

    public String getSavePath(){
        return orderData.getSavePath();
    }
}
