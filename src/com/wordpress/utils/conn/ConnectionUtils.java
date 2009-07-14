package com.wordpress.utils.conn;


import net.rim.device.api.servicebook.ServiceBook;
import net.rim.device.api.servicebook.ServiceRecord;
import net.rim.device.api.system.RadioInfo;

import com.wordpress.utils.StringUtils;
import com.wordpress.utils.log.Log;


public class ConnectionUtils {
	
    public static final String WAP_DEFAULT_GWAYPORT = "9201";
    public static final String WAP_DEFAULT_SOURCEIP = "127.0.0.1";
    public static final String WAP_DEFAULT_SOURCEPORT = "8205";
	
   /**
    * BlackBerry smartphones running BlackBerry Device Software 4.2.1 and later may or may not support
    * Wi-Fi capabilities. To determine if a BlackBerry smartphone supports Wi-Fi, call the following
    * method.
    * This functionality is used to determine if Wi-Fi capabilities are present on the BlackBerry 
    * smartphone. It cannot be used to determine if the BlackBerry smartphone is in Wi-Fi coverage.
    * @return
    */
    public static boolean isWifiAvailable() {
        Log.info("Checking WIFI Availability");
        boolean isWifiEnabled;
        if (RadioInfo.areWAFsSupported(RadioInfo.WAF_WLAN)) {
            Log.info("WI-FI Supported");
            isWifiEnabled = true;
        } else {
            Log.info("WI-FI NOT Supported");
            isWifiEnabled = false;
        }
        return isWifiEnabled;
    }

    /**
     * 
     * @return
     */
    protected static boolean isWifiActive() {
        int active = RadioInfo.getActiveWAFs();
        Log.trace("The currently active Wireless Access Families : " + active);

        int wifi = RadioInfo.WAF_WLAN;
        Log.trace("WLAN Wireless Access Family: " + wifi);

        return active >= wifi;
    }

    
    protected static boolean isDataBearerOffline() {
        return RadioInfo.getState()==RadioInfo.STATE_OFF ||
               RadioInfo.getSignalLevel() == RadioInfo.LEVEL_NO_COVERAGE;
    }
   
    
  /** 
   *  Wireless service provider WAP 2.0 gateway
   *   BlackBerry Device Software 4.2.0 and later includes the ability to connect through a WAP 2.0 gateway.
   *   This is done by locating the service record on the BlackBerry for the WAP 2.0 gateway and using its
   *   UID when making the connection.
   *   
   */
    public static String getServiceBookOptions() {
        ServiceBook sb = ServiceBook.getSB();
        
        ServiceRecord[] records = sb.findRecordsByType(ServiceRecord.SRT_ACTIVE);

        //Search through all service records to find the
        //valid non-Wi-Fi and non-MMS 
        //WAP 2.0 Gateway Service Record.         	
        for (int i = 0; i < records.length; i++) {
            //get the record
            ServiceRecord sr = records[i];
            
            //check if CID is WPTCP and UID.
            //UID could be different per carrier.        
            if (StringUtils.equalsIgnoreCase(sr.getCid(), "WPTCP") &&
                    StringUtils.equalsIgnoreCase(sr.getUid(), "WAP2 trans")) {
                if (records[i].getAPN() != null) {
                    return ";ConnectionUID=" + records[i].getUid();
                }
            }
        }
        return "";
    }
    
    public static boolean isDataConnectionAvailable() {
        return ( !isDataBearerOffline() || isWifiAvailable()&&isWifiActive() );
    }
    
  // Return Wireless service provider WAP 1.x gateway connection string
    static String buildWapConnectionString(Gateway gateway){
    	 StringBuffer options = new StringBuffer("");
    	 if (gateway != null) {
    		 if(gateway.getApn() != null) {
             options.append(";WapGatewayAPN=" + gateway.getApn());
    		 } else {//exit immediately
    			 return ""; 
    		 }
             if (gateway.getUsername() != null) {
                 options.append(";TunnelAuthUsername=" + gateway.getUsername());
             }
             if (gateway.getPassword() != null) {
                 options.append(";TunnelAuthPassword=" + gateway.getPassword());
             }
             if (gateway.getGatewayIP() != null) {
                 options.append(";WapGatewayIP=" + gateway.getGatewayIP());
             }
             if (gateway.getGatewayPort() != null) {
                 options.append(";WapGatewayPort=" + gateway.getGatewayPort());
             }
             if (gateway.getSourceIP() != null) {
            	 options.append(";WapSourceIP=" + gateway.getSourceIP());
             }
             if (gateway.getSourcePort() != null) {
                 options.append(";WapSourcePort=" + gateway.getSourcePort());
             }
         }
    	 return options.toString();
    }
}
