package com.wordpress.bb;

import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.CodeModuleManager;

import com.webtrends.mobile.analytics.rim.WebtrendsConfig;

public class AnalyticsConfig extends WebtrendsConfig {

	public String wt_dc_app_name() {
		return ApplicationDescriptor.currentApplicationDescriptor().getName();
	}

	public String wt_dc_app_version() {
		return ApplicationDescriptor.currentApplicationDescriptor().getVersion();
	}

	public String wt_dc_dcsid() {
		return ""; 
	}
	
    // Set this value to "true" if you want to enable application logging.
    public String wt_dc_debug() {
        return "false";
    }
    
    // Percent of battery life remaining when event send is paused.
    public String wt_dc_charge_threshold_minimum() {
        return "30";
    }
    
    // Set this value to "false" if you want to disable the Webtrends library.
    public String wt_dc_enabled() {
        return "true";
    }

	public String wt_dc_timezone() {
		return "0";
	}

	public String wt_dc_url() {
		return "http://dc.webtrends.com/v1";
	}

	public String wt_dc_app_category() {
		return "IM and Social Networking ";
	}

	public String wt_dc_app_publisher() {
		return CodeModuleManager.getModuleVendor(ApplicationDescriptor.currentApplicationDescriptor().getModuleHandle());
	}
}