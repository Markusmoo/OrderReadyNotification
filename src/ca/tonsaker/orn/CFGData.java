package ca.tonsaker.orn;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

import java.io.*;

/**
 * Created by Marku on 2017-12-04.
 */
public class CFGData {

    @Expose public String ACCOUNT_SID = "";
    @Expose public String AUTH_TOKEN = "";

    @Expose public String COMPANY_NAME = "";
    @Expose public String[] MENU_ITEMS = {};

    public CFGData(String accountSID, String authToken, String companyName, String[] menuItems){
        ACCOUNT_SID = accountSID;
        AUTH_TOKEN = authToken;
        COMPANY_NAME = companyName;
        MENU_ITEMS = menuItems;
    }

    public CFGData(){}

    public void loadCFG(){
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

        this.ACCOUNT_SID = cfgData.ACCOUNT_SID;
        this.AUTH_TOKEN = cfgData.AUTH_TOKEN;
        this.COMPANY_NAME = cfgData.COMPANY_NAME;
        this.MENU_ITEMS = cfgData.MENU_ITEMS;

        System.out.println("Config file successfully loaded from \"" + path + "\"");
    }

    public void saveCFG() throws IOException{
        String path = System.getenv("APPDATA")+"\\ORN\\config.json";
        Writer writer = new OutputStreamWriter(new FileOutputStream(path));
        Gson gson = new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().create();

        writer.write(gson.toJson(this));
        writer.flush();
        writer.close();

        System.out.println("Config file successfully saved to \"" + path + "\"");
    }
}
