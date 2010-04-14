//#preprocess
package com.wordpress.view;

import java.io.IOException;
import java.util.Vector;


import net.rim.device.api.i18n.MessageFormat;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Display;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.component.TextField;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.BaseController;
import com.wordpress.controller.StatsController;
import com.wordpress.utils.Tools;
import com.wordpress.utils.csv.StatsParser;
import com.wordpress.utils.log.Log;
import com.wordpress.view.component.WebBitmapField;
import com.wordpress.view.container.TableLayoutManager;

//#ifdef IS_OS47_OR_ABOVE
import net.rim.device.api.ui.Touchscreen;
import com.wordpress.view.touch.BottomBarItem;
//#endif
import com.wordpress.xmlrpc.HTTPGetConn;

public class StatsView extends BaseView {
	
	private StatsController controller;
	private TextField statsDesc;
	private WebBitmapField chartImg;
	
	private final String _7days = 7 +" " + _resources.getString(WordPressResource.LABEL_DAYS);
	private final String _30days = 30 +" " + _resources.getString(WordPressResource.LABEL_DAYS);
	private final String _90days = _resources.getString(WordPressResource.LABEL_QUARTER);
	private final String _365days = _resources.getString(WordPressResource.LABEL_YEAR);
	private final String _AllTime = _resources.getString(WordPressResource.LABEL_ALLTIME);
	
	public StatsView(StatsController _controller) {
		super(_resources.getString(WordPressResource.TITLE_STATS) , MainScreen.NO_VERTICAL_SCROLL 
				| Manager.NO_HORIZONTAL_SCROLL	| USE_ALL_HEIGHT | USE_ALL_WIDTH);
		
		this.controller=_controller;
		
		
		scrollerData = new VerticalFieldManager(VERTICAL_SCROLL | VERTICAL_SCROLLBAR | USE_ALL_WIDTH);
		statsDesc = new TextField(USE_ALL_WIDTH | READONLY);
		
		add(statsDesc);
		add(scrollerData);
		
		//#ifdef IS_OS47_OR_ABOVE
		initUpBottomBar();
		//#endif
		
		addMenuItem(_7daysMenuItem);
		addMenuItem(_30daysMenuItem);
		addMenuItem(_90daysMenuItem);
		addMenuItem(_365daysMenuItem);
		addMenuItem(_AllTimeMenuItem);
		
		addMenuItem(_viewsItem);
		addMenuItem(_topPostAndPageItem);
		addMenuItem(_referrersItem);
		addMenuItem(_searchEngineTermsItem);
		addMenuItem(_clicksItem);
		
		updateSubTitle();
	}
	
	public void setStatsData(byte[] data) {
		String[] columnName = null;
		String[] columnLink = null;
		String[] columnHeader = null;
		TableLayoutManager outerTable = null;
		String dateColumnTitle = _resources.getString(WordPressResource.LABEL_DATE);
		String viewsColumnTitle = _resources.getString(WordPressResource.LABEL_VIEWS);
		
		switch (controller.getType()) {
		
		case StatsController.TYPE_VIEW:
			statsDesc.setText(_resources.getString(WordPressResource.MESSAGE_STATS_VIEW));
			outerTable = new TableLayoutManager(new int[] {
					TableLayoutManager.SPLIT_REMAINING_WIDTH,
					TableLayoutManager.SPLIT_REMAINING_WIDTH }, new int[] { 2, 2 }, 5,
					Manager.USE_ALL_WIDTH);
			columnHeader = new String[]{dateColumnTitle, viewsColumnTitle};
			columnName = new String[]{"date","views"};
			break;
			
		case StatsController.TYPE_CLICKS:
			statsDesc.setText(_resources.getString(WordPressResource.MESSAGE_STATS_CLICKS));
			outerTable = new TableLayoutManager(new int[] {
					TableLayoutManager.USE_PREFERRED_SIZE,
					TableLayoutManager.SPLIT_REMAINING_WIDTH,
					TableLayoutManager.USE_PREFERRED_SIZE }, new int[] { 2, 2, 2 }, 5,
					Manager.USE_ALL_WIDTH);
			columnHeader = new String[]{_resources.getString(WordPressResource.MENUITEM_STATS_CLICKS),viewsColumnTitle};
			columnName = new String[]{"click","views"};
			columnLink = new String[]{"click",null};
			break;
			
		case StatsController.TYPE_REFERRERS:
			statsDesc.setText(_resources.getString(WordPressResource.MESSAGE_STATS_REFFERERS));
			outerTable = new TableLayoutManager(new int[] {
					TableLayoutManager.USE_PREFERRED_SIZE,
					TableLayoutManager.SPLIT_REMAINING_WIDTH,
					TableLayoutManager.USE_PREFERRED_SIZE }, new int[] { 2, 2, 2 }, 5,
					Manager.USE_ALL_WIDTH);
			columnHeader = new String[]{_resources.getString(WordPressResource.MENUITEM_STATS_REFERRERS),viewsColumnTitle};
			columnName = new String[]{"referrer","views"};
			columnLink = new String[]{"referrer",null};
			break;
			
		case StatsController.TYPE_SEARCH:
			statsDesc.setText(_resources.getString(WordPressResource.MESSAGE_STATS_SEARCH_ENGINE_TERMS));
			outerTable = new TableLayoutManager(new int[] {
					TableLayoutManager.USE_PREFERRED_SIZE,
					TableLayoutManager.SPLIT_REMAINING_WIDTH,
					TableLayoutManager.USE_PREFERRED_SIZE }, new int[] { 2, 2, 2 }, 5,
					Manager.USE_ALL_WIDTH);
			columnHeader = new String[]{_resources.getString(WordPressResource.MENUITEM_STATS_SEARCH),viewsColumnTitle};
			columnName = new String[]{"searchterm","views"};
			break;
			
		case StatsController.TYPE_TOP:
			statsDesc.setText(_resources.getString(WordPressResource.MESSAGE_STATS_TOP));
			outerTable = new TableLayoutManager(new int[] {
					TableLayoutManager.USE_PREFERRED_SIZE,
					TableLayoutManager.SPLIT_REMAINING_WIDTH,
					TableLayoutManager.USE_PREFERRED_SIZE }, new int[] { 2, 2, 2 }, 5,
					Manager.USE_ALL_WIDTH);
			columnHeader = new String[]{_resources.getString(WordPressResource.LABEL_TITLE),viewsColumnTitle};
			columnName = new String[]{"post_title", "views"};
			columnLink = new String[]{"post_permalink",null};
			break;
		default:
			return;
		}
		//clean the prev stats ui elements: graph and table...
		scrollerData.deleteAll();
		fillStatsUI(data, columnHeader, columnName, columnLink, outerTable);
	}
	
	private void fillStatsUI(byte[] data, String[] columnsHeader, String[] columnName, String[] columnLink, TableLayoutManager outerTable) {

		//1. parse the data
		StatsParser statParser = new StatsParser(data);
		try {
			statParser.parseAll();
		} catch (IOException e1) {
			controller.displayError(e1, "");
			return;
		}
		
		//2.fill the stats Table
		if(columnsHeader == null)
			columnsHeader = columnName;

		//adding header 
		if(controller.getType() != StatsController.TYPE_VIEW)
			outerTable.add(GUIFactory.getLabel("", DrawStyle.ELLIPSIS ));	
		for (int i = 0; i < columnsHeader.length; i++) {
			outerTable.add(GUIFactory.getLabel(columnsHeader[i], DrawStyle.ELLIPSIS ));
		}
		
		//adding separator
		if(controller.getType() != StatsController.TYPE_VIEW)
			outerTable.add(GUIFactory.createSepatorField());
		for (int i = 0; i < columnsHeader.length; i++) {
			outerTable.add(GUIFactory.createSepatorField());
		}

		//fill the table
		try {
			int counter = 1;
			while (statParser.hasNext()) {
				String[] nextLine = statParser.next();
				
				if(controller.getType() != StatsController.TYPE_VIEW)
					outerTable.add(GUIFactory.getLabel(String.valueOf(counter), LabelField.FOCUSABLE |  DrawStyle.ELLIPSIS));

				for (int i = 0; i < columnName.length; i++) {
					String _tmpName = columnName[i];
					int _tmpIdx = statParser.getColumnIndex(_tmpName);
					String value = nextLine[_tmpIdx];
					
					//format the string if it regard the views
					if(_tmpName.equalsIgnoreCase("views"))
						value = Tools.groupDigits(value, 3, ',');
					
					if(columnLink != null && columnLink[i] != null ) {
						int _tmpIdxLink = statParser.getColumnIndex(columnLink[i]); //retrive the url
						outerTable.add(GUIFactory.createClickableLabel(value, nextLine[_tmpIdxLink], LabelField.FOCUSABLE | DrawStyle.ELLIPSIS));	
					} else 
					outerTable.add(GUIFactory.getLabel(value, LabelField.FOCUSABLE |  DrawStyle.ELLIPSIS));
				}
				counter++;
			}
		} catch (Exception e) {
			controller.displayError(e, "Error while filling stats UI");
		}
		
		//3. build the graphs
		buildChart(columnName, statParser);	
		scrollerData.add(outerTable);
	}

	private void buildChart(String[] columnName, StatsParser statParser) {
		try {
			statParser.reset();
			if(statParser.hasNext() == false) return;
			
			String chartURL = "http://chart.apis.google.com/chart";
			String chartParametersURL = "";
			
			chartImg = null;

			if(controller.getType() == StatsController.TYPE_VIEW) {
				//draw a line chart
				Vector xAxisValues = new Vector();
				StringBuffer chd_y = new StringBuffer("");
				int max = 0;
				while (statParser.hasNext()) {
					String[] nextLine = statParser.next();

					for (int i = 0; i < columnName.length; i++) {
						String _tmpName = columnName[i];
						int _tmpIdx = statParser.getColumnIndex(_tmpName);
						if(i ==  0) {
							xAxisValues.addElement(nextLine[_tmpIdx]);
						} else {
							int value = Integer.parseInt(nextLine[_tmpIdx]);
							if (value > max) max = value;
							chd_y.append(value+",");
						}
					}
				}//end while

				chd_y.deleteCharAt(chd_y.length()-1);

				//chxl=0:|Jan|Feb|March|April|May|
				String startLabel = (String)xAxisValues.elementAt(0);
				String endLabel = startLabel; 
				if(xAxisValues.size() > 1)
					endLabel = (String)xAxisValues.elementAt((xAxisValues.size()-1));
				

				String axisLabels= "&chxl=0:|"+startLabel+"|"+endLabel;
				chartParametersURL = "?cht=lc" +
				"&chxt=x,y&chxr=1,0,"+max+"&chxs=1N*sz0*&chds=0,"+max+axisLabels+
				"&chd=t:"+chd_y.toString()+"&chco=FF0000&chf=bg,s,EFEFEF";
				
			} else { 
				//building a pie chart
				//	http://chart.apis.google.com/chart?chs=500x300&chd=t:200,40,20&chds=0,1000&cht=p3&chdl=Hello|World|pippo&chdlp=bv&chco=FF0000,00FF00,0000FF
				StringBuffer chdl = new StringBuffer("&chl=");
				StringBuffer chd = new StringBuffer("&chd=t:");
				int max = 0;
				int counter = 1;
				while (statParser.hasNext()) {
					String[] nextLine = statParser.next();

					for (int i = 0; i < columnName.length; i++) {
						String _tmpName = columnName[i];
						int _tmpIdx = statParser.getColumnIndex(_tmpName);
						if(i ==  0) {
							//labels
							chdl.append(String.valueOf(counter)+"|");
						} else {
							//value
							int value = Integer.parseInt(nextLine[_tmpIdx]);
							if (value > max) max = value;
							chd.append(nextLine[_tmpIdx]+",");
						}
					}
					counter++;
				}//end while

				//building the chart url
				chd.deleteCharAt(chd.length()-1);
				chdl.deleteCharAt(chdl.length()-1);



				chartParametersURL ="?cht=p3" +
				chd.toString() + "&chds=0,"+max+"&"+chdl.toString()+"&chco=FF0000,00FF00,0000FF";
			}

			chartImg = new ChartBitmap(chartURL+chartParametersURL, 
					Bitmap.getBitmapResource("stats.png"),
					Field.FIELD_HCENTER | Field.FOCUSABLE);

			scrollerData.add(new LabelField(""));
			scrollerData.add(chartImg);
			scrollerData.add(new LabelField(""));

		} catch (Exception e) {
			controller.displayError(e, "Error while building stats chart");
		}
	}

	private int getPredefinedChartWidth(){
		return Display.getWidth() - 10;
	}
	
	private int getPredefinedChartHeigth(){
		return (Display.getWidth() - 10)/2;
	}

	private class ChartBitmap extends WebBitmapField {
	
		public ChartBitmap(String url, Bitmap imgLoading, long style) {
			super(url+"&chs="+getPredefinedChartWidth()+"x"+getPredefinedChartHeigth(), imgLoading, style);
		}
		
		protected void layout(int width, int height) {
			Log.trace("ChartBitmap Layout");
			
			if(bitmap != null) {
				if ( bitmap.getWidth() == getPredefinedChartWidth() )
				{
					Log.trace("dimensione chart NON cambiata");
					super.layout(width, height);
					return;
				}
			}
			
			//stops the http connection if already active
			if(connection != null) {
				connection.stopConnWork();
			}
			
			super.layout(width, height); 
	        try  
	        {  
	    		connection = new HTTPGetConn(URL+"&chs="+getPredefinedChartWidth()+"x"+getPredefinedChartHeigth(), "", "");
		        connection.addObserver(this);  	       
		        connection.startConnWorkBackground(); //starts connection
	        }  
	        catch (Exception e) {} 
			
		}
	}
	
	private void updateSubTitle() {
		String subtitle = null;
		
		switch (controller.getInterval()) {
		case StatsController.INTERVAL_7DAYS:
			subtitle = _7days +" ";
			//topBarButton.setSelectedField(_7daysBtn);
			break;
		case StatsController.INTERVAL_30DAYS:
			subtitle = _30days +" ";
			//topBarButton.setSelectedField(_30daysBtn);
			break;
		case StatsController.INTERVAL_QUARTER:
			subtitle = _90days +" ";
			//topBarButton.setSelectedField(_90daysBtn);
			break;
		case StatsController.INTERVAL_YEAR:
			subtitle = _365days +" ";
			//topBarButton.setSelectedField(_365daysBtn);
			break;
		case StatsController.INTERVAL_ALL:
			subtitle = _AllTime +" ";
			//topBarButton.setSelectedField(_allTimeBtn);
			break;
		default:
			subtitle = _7days +" ";
			break;
		}
		
		subtitle +="- ";
		
		switch (controller.getType()) {
		case StatsController.TYPE_VIEW:
			subtitle += _resources.getString(WordPressResource.MENUITEM_STATS_VIEW);
			break;
		case StatsController.TYPE_CLICKS:
			subtitle += _resources.getString(WordPressResource.MENUITEM_STATS_CLICKS);
			break;
		case StatsController.TYPE_REFERRERS:
			subtitle += _resources.getString(WordPressResource.MENUITEM_STATS_REFERRERS);
			break;
		case StatsController.TYPE_SEARCH:
			subtitle += _resources.getString(WordPressResource.MENUITEM_STATS_SEARCH);
			break;
		case StatsController.TYPE_TOP:
			subtitle += _resources.getString(WordPressResource.MENUITEM_STATS_TOP);
			break;
		default:
			subtitle += _resources.getString(WordPressResource.MENUITEM_STATS_VIEW);
			break;
		}
		this.setSubTitleText(subtitle);
	}
	
	private void retriveStatsInterval(int interval) {
		controller.setInterval(interval);
		controller.retriveStats();
		updateSubTitle();
	}
	
	private void retriveStatsType(int type) {
		controller.setType(type);
		controller.retriveStats();
		updateSubTitle();
	}

	 private MenuItem _7daysMenuItem = new MenuItem(_7days, 100, 100) {
		 public void run() {
			 retriveStatsInterval(StatsController.INTERVAL_7DAYS);
		 }
	 };
	 private MenuItem _30daysMenuItem = new MenuItem(_30days, 110, 100) {
		 public void run() {
			 retriveStatsInterval(StatsController.INTERVAL_30DAYS);
		 }
	 };
	 private MenuItem _90daysMenuItem = new MenuItem( _90days, 120, 100) {
		 public void run() {
			 retriveStatsInterval(StatsController.INTERVAL_QUARTER);
		 }
	 };
	 private MenuItem _365daysMenuItem = new MenuItem( _365days, 130, 100) {
		 public void run() {
			 retriveStatsInterval(StatsController.INTERVAL_YEAR);
		 }
	 };
	 private MenuItem _AllTimeMenuItem = new MenuItem( _AllTime, 140, 100) {
		 public void run() {
			 retriveStatsInterval(StatsController.INTERVAL_ALL);
		 }
	 };
	 
	 private MenuItem _viewsItem = new MenuItem( _resources, WordPressResource.MENUITEM_STATS_VIEW, 100000, 200) {
		 public void run() {
			 retriveStatsType(StatsController.TYPE_VIEW);
		 }
	 };
	 private MenuItem _topPostAndPageItem = new MenuItem( _resources, WordPressResource.MENUITEM_STATS_TOP, 100000, 200) {
		 public void run() {
			 retriveStatsType(StatsController.TYPE_TOP);
		 }
	 };
	 private MenuItem _referrersItem = new MenuItem( _resources, WordPressResource.MENUITEM_STATS_REFERRERS, 100000, 200) {
		 public void run() {
			 retriveStatsType(StatsController.TYPE_REFERRERS);
		 }
	 };
	 private MenuItem _searchEngineTermsItem = new MenuItem( _resources, WordPressResource.MENUITEM_STATS_SEARCH, 100000, 200) {
		 public void run() {
			 retriveStatsType(StatsController.TYPE_SEARCH);
		 }
	 };
	 private MenuItem _clicksItem = new MenuItem( _resources, WordPressResource.MENUITEM_STATS_CLICKS, 100000, 200) {
		 public void run() {
			 retriveStatsType(StatsController.TYPE_CLICKS);
		 }
	 };
	private VerticalFieldManager scrollerData;
	
	//#ifdef IS_OS47_OR_ABOVE
	private void initUpBottomBar() {
		if (Touchscreen.isSupported() == false) return;

		BottomBarItem items[] = new BottomBarItem[5];
		items[0] = new BottomBarItem("stats_view.png", "stats_view.png",  _resources.getString(WordPressResource.MENUITEM_STATS_VIEW));
		items[1] = new BottomBarItem("stats_top.png", "stats_top.png", _resources.getString(WordPressResource.MENUITEM_STATS_TOP));
		items[2] = new BottomBarItem("stats_referrers.png", "stats_referrers.png", _resources.getString(WordPressResource.MENUITEM_STATS_REFERRERS));
		items[3] = new BottomBarItem("stats_search.png", "stats_search.png",  _resources.getString(WordPressResource.MENUITEM_STATS_SEARCH));
		items[4] = new BottomBarItem("stats_clicks.png", "stats_clicks.png",  _resources.getString(WordPressResource.MENUITEM_STATS_CLICKS));

		initializeBottomBar(items);
	}

	protected void bottomBarActionPerformed(int mnuItem) {
		switch (mnuItem) {
		case 0:
			retriveStatsType(StatsController.TYPE_VIEW);
			break;
		case 1:
			retriveStatsType(StatsController.TYPE_TOP);
			break;
		case 2:
			retriveStatsType(StatsController.TYPE_REFERRERS);
			break;
		case 3:
			retriveStatsType(StatsController.TYPE_SEARCH);
			break;
		case 4:
			retriveStatsType(StatsController.TYPE_CLICKS);
			break;
		default:
			break;
		}
	}
	//#endif

	public boolean onMenu(int instance) {
		boolean result;
		// Prevent the context menu from being shown if focus is on a labels
		if (getLeafFieldWithFocus() instanceof LabelField  && instance == Menu.INSTANCE_CONTEXT) {
			result = false;
		} else {
			result = super.onMenu(instance);
		}
		return result;
	}
	
	public boolean onClose()   {
		controller.backCmd();
		return true;
    }
	
	public BaseController getController() {
		return controller;
	}
}