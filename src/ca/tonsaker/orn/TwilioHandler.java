package ca.tonsaker.orn;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.rest.api.v2010.account.MessageCreator;
import com.twilio.type.PhoneNumber;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by Markus Tonsaker on 2017-12-04.
 */
public class TwilioHandler implements ActionListener{

    private Timer statusChecker;
    private Timer statusChecker2;

    private CFGData cfgData;
    private MainFrame frame;
    private Message lastMessage;

    public TwilioHandler(MainFrame frame, CFGData data){
        this.frame = frame;
        this.cfgData = data;
        statusChecker = new Timer(10000, this);
        statusChecker2 = new Timer(1000, this);
        Twilio.init(data.API_KEY, data.API_SECRET, data.ACCOUNT_SID);
    }

    public void sendNotification(String name, String toNumber, DefaultListModel<String> list){
        String body;
        if(name.equals("")){
            body = "Your order is ready!!!\n\n";
        }else{
            body = "Hey " + name + "! Your order is ready!!!:\n* * * * * *\n";
        }
        StringBuilder orderList = new StringBuilder();
        for(Object s : list.toArray()){
            orderList.append(s.toString()+"\n");
        }
        body += orderList.toString();
        body += "* * * * * *\nSee you soon! :)\n" +
                "- "+ cfgData.COMPANY_NAME;

        //TODO Format with Country code ie +1
        MessageCreator msgCreator = Message.creator(new PhoneNumber("+1"+toNumber), new PhoneNumber(cfgData.PHONE_NUMBER), body);
        Message msg = msgCreator.create();
        lastMessage = msg;
        printSMSDetails(msg);
        statusChecker.start();
        statusChecker2.start();
    }

    /**
     * TODO Test to see if timers.stop() resets delay.
     *
     */
    private void resetTimers(){
        statusChecker.stop();
        statusChecker2.stop();
    }

    public void printSMSDetails(Message msg){
        System.out.println("*******Notifcation SMS Sent*******\n" +
                "      SID: " + msg.getSid() + "\n" +
                " Segments: " + msg.getNumSegments() + "\n" +
                "   From #: " + msg.getFrom() + "\n" +
                "     To #: " + msg.getTo() + "\n" +
                "   Status: " + msg.getStatus().toString() + "\n" +
                "************************************");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource().equals(statusChecker2)){
            if(lastMessage.getStatus() == Message.Status.DELIVERED){
                resetTimers();
                JOptionPane.showMessageDialog(frame, "SMS Delivered to "+lastMessage.getTo()+" successfully!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        }else if(e.getSource().equals(statusChecker)){
            resetTimers( );
            JOptionPane.showMessageDialog(frame, "SMS failed to deliver to "+lastMessage.getTo()+"\n" +
                            "Status: "+lastMessage.getStatus().toString(),
                    "Failed", JOptionPane.ERROR_MESSAGE);
        }
    }
}
