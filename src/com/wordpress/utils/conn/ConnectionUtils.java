package com.wordpress.utils.conn;


import net.rim.device.api.servicebook.ServiceBook;
import net.rim.device.api.servicebook.ServiceRecord;
import net.rim.device.api.system.CoverageInfo;
import net.rim.device.api.system.DeviceInfo;
import net.rim.device.api.system.RadioInfo;

import com.wordpress.utils.StringUtils;
import com.wordpress.utils.log.Log;

/**
 * This class provides informations about Blackberry devices configurations 
 * and allow to access some system properties that are peculiar of Blackberry devices only. 
 */
public class ConnectionUtils {
	
    public static final String WAP_DEFAULT_GWAYPORT = "9201";
    public static final String WAP_DEFAULT_SOURCEIP = "127.0.0.1";
    public static final String WAP_DEFAULT_SOURCEPORT = "8205";
	
    private static final String COVERAGE_CARRIER = "Carrier full Coverage";
    private static final String COVERAGE_MDS = "BES coverage";
    private static final String COVERAGE_NONE = "No coverage";
    private static final String NOT_SUPPORTED_WAF = "Not supported by the device";

    /**
     * Access the net.rim.device.api.system.DeviceInfo class in order to 
     * understand if the running system is a simulator or a real device.
     * @return true if the current application is running on a Blackberry 
     * simulator, false otherwise
     */
    public boolean isSimulator() {
        return DeviceInfo.isSimulator();
    }

    /**
     * Give the information about the presence o a wifi bearer on the device
     * @return true if the wifi communication interface bearer is supported by 
     * the device, false otherwise
     */
    protected static boolean isWifiAvailable() {
        Log.info("Checking WIFI Availability");
        boolean isWifiEnabled;
        if (RadioInfo.areWAFsSupported(RadioInfo.WAF_WLAN)) {
            Log.info("WIFI Supported");
            isWifiEnabled = true;
        } else {
            Log.info("WIFI NOT Supported");
            isWifiEnabled = false;
        }
        return isWifiEnabled;
    }

    /**
     * Give information about the presence of active wifi connections. 
     * @return true if the device is connected to a wifi network with its wifi 
     * bearer, false otherwise
     */
    protected static boolean isWifiActive() {
        Log.info("Checking WIFI Availability");

        int active = RadioInfo.getActiveWAFs();
        int wifi = RadioInfo.WAF_WLAN;

        Log.debug("Active WAFs Found: " + active);
        Log.debug("WIFI WAF DEFINITION: " + wifi);

        return active >= wifi;
    }

    protected static boolean isWapGprsDataBearerOffline() {
        return RadioInfo.getState()==RadioInfo.STATE_OFF ||
               RadioInfo.getSignalLevel() == RadioInfo.LEVEL_NO_COVERAGE;
    }

    public static String getNetworkCoverageReport() {
        StringBuffer sb = new StringBuffer();
        
        sb.append("\n*********************************************************");
        sb.append("\nWireless Access Families:");
        sb.append("\n3GPP: " + getNetworkCoverage(RadioInfo.WAF_3GPP));
        sb.append("\nCDMA: " + getNetworkCoverage(RadioInfo.WAF_CDMA));
        sb.append("\nWLAN: " + getNetworkCoverage(RadioInfo.WAF_WLAN));
        sb.append("\nCDMA: " + getNetworkCoverage(RadioInfo.NETWORK_CDMA));
        sb.append("\nBands:");
        sb.append("\nCDMA_800: " + getNetworkCoverage(RadioInfo.BAND_CDMA_800));
        sb.append("\nCDMA_1900: " + getNetworkCoverage(RadioInfo.BAND_CDMA_1900));
        sb.append("\nNetworks:");
        sb.append("\n802_11: " + getNetworkCoverage(RadioInfo.NETWORK_802_11));
        sb.append("\nGPRS: " + getNetworkCoverage(RadioInfo.NETWORK_GPRS));
        sb.append("\nNetwork services:");
        sb.append("\nVOICE: " + getNetworkCoverage(RadioInfo.NETWORK_SERVICE_VOICE));
        sb.append("\nUMTS: " + getNetworkCoverage(RadioInfo.NETWORK_SERVICE_UMTS));
        sb.append("\nEDGE: " + getNetworkCoverage(RadioInfo.NETWORK_SERVICE_EDGE));
        sb.append("\n*********************************************************");
        return sb.toString();
    }

    private static String getNetworkCoverage(int networkType) {
        if (RadioInfo.areWAFsSupported(networkType)) {
            int status = CoverageInfo.getCoverageStatus(networkType, false);
            switch (status) {
                case CoverageInfo.COVERAGE_CARRIER: //TODO if we switch back to < 4.5 we must use CARRIER
                    return COVERAGE_CARRIER;
                case CoverageInfo.COVERAGE_MDS:
                    return COVERAGE_MDS;
                case CoverageInfo.COVERAGE_NONE:
                    return COVERAGE_NONE;
                default:
                    break;
            }
        } 
        return NOT_SUPPORTED_WAF;
    }
    
    /**
     * Validate the given ServiceRecord entry: in order to be validated it must 
     * be a WAP or WAP2 transport entry 
     * @param sr is the ServiceRecord to be checked
     * @return true if 
     */
    public static boolean isWapTransportServiceRecord(ServiceRecord sr) {
        //TODO: use a table to store this data
        return ( 
            // wind, tim & US
            (StringUtils.equalsIgnoreCase(sr.getCid(), "WPTCP") && StringUtils.equalsIgnoreCase(sr.getUid(), "WAP2 trans")) ||
            // Vodafone it
            (StringUtils.equalsIgnoreCase(sr.getCid(), "WAP") && StringUtils.equalsIgnoreCase(sr.getUid(), "vfit WAPtrans")));
    }
    
    /**
     * Retrieves the WAP/WAP2 Transport APN from service book
     * @return the String formatted WAP/WAP2 Transport APN. This entry is 
     * unique for every ServiceBook.
     */
    public static String getServiceBookWapTransportApn() {
        //get only active service records
        ServiceBook sb = ServiceBook.getSB();
        ServiceRecord[] records = sb.findRecordsByType(ServiceRecord.SRT_ACTIVE);
        String apn = null;
        //Obtain WAP2 ServiceBook Record
        for (int i = 0; i <records.length; i++) {
            //get the record
            ServiceRecord sr = records[i];
            //check if CID is WPTCP and UID. UID could be different per carrier. 
            //TODO: We could build a list. 
            if (ConnectionUtils.isWapTransportServiceRecord(sr)) {
                apn = records[i].getAPN();
            }
        }
        return apn;
    }

    /**
     * Retrieves only ACTIVE ServiceRecords from the native device's 
     * ServiceBook checking if their CID is WPTCP and UID
     * @return String[] with the active APN found into the device's ServiceBook
     */
    public static String[] getAllActiveServiceBookAPNs() {
        ServiceBook sb = ServiceBook.getSB();
        ServiceRecord[] records = sb.findRecordsByType(ServiceRecord.SRT_ACTIVE);

        String[] apn = new String[records.length];
        
        //Retrieve "WAP2" ServiceBook Record
        for (int i = 0; i < records.length; i++) {
            //get the record
            ServiceRecord sr = records[i];
            //check if CID is WPTCP and UID. UID can be different 
            //per carrier. We could build a list. 
            apn[i] = sr.getAPN();
        }
        return apn;
    }
    
    /**
     * Get the Wireless service provider WAP 2.0 gateway from the device ServiceBooks
     * 
     * Get the options to use the list of APN included into the device
     * ServiceBook
     * @return a string that should be added to the url parameters
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
    
    /**
     * Give global information about the data connection bearer activity
     * @return true if there is an active bearer including the check for 
     * WIFI and WAP/GPRS bearer
     */
    public static boolean isDataConnectionAvailable() {
        boolean ret = 
                (isWifiAvailable()&&isWifiActive())||
                !isWapGprsDataBearerOffline();
        Log.debug("[BlackberryUtils.isDataConnectionAvailable]Data connection availability: " + ret);
        return ret;
    }
    
  // Return Wireless service provider WAP 1.x gateway connection string
    static String buildWapConnectionString(Gateway gateway){
    	 StringBuffer options = new StringBuffer("");
    	 if (gateway != null) {
    		 if(gateway.getApn() != null) {
             //options.append(";apn=" + gateway.getApn());
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
