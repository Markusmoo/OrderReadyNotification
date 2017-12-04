package ca.tonsaker.orn;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import javax.swing.*;

/**
 * Created by Markus Tonsaker on 2017-12-04.
 */
public class TwilioHandler {

    private CFGData cfgData;

    public TwilioHandler(CFGData data){

        this.cfgData = data;

        Twilio.init(data.ACCOUNT_SID, data.AUTH_TOKEN);
    }

    public void sendNotification(String name, String toNumber, String fromNumber, DefaultListModel<String> list){
        String body;
        if(!name.equals("")){
            body = "Your order is ready!!!\n\n";
        }else{
            body = "Hey " + name + "! Your order is ready!!!:\n\n";
        }
        StringBuilder orderList = new StringBuilder();
        for(Object s : list.toArray()){
            orderList.append(s.toString()+"\n");
        }
        body += orderList.toString();
        body += "\nSee you soon! :)\n" +
                "- "+ cfgData.COMPANY_NAME;

        Message msg = Message.creator(new PhoneNumber(toNumber), new PhoneNumber(fromNumber), body).create();
        System.out.println(msg.getSid());
    }

}
