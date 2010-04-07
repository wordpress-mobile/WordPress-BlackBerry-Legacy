//#preprocess
package com.wordpress.view;

import java.io.IOException;


import net.rim.blackberry.api.browser.URLEncodedPostData;
import net.rim.device.api.system.Display;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.system.GIFEncodedImage;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.TextField;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.BaseController;
import com.wordpress.controller.StatsController;
import com.wordpress.utils.csv.StatsParser;
import com.wordpress.utils.log.Log;
import com.wordpress.view.component.WebBitmapField;
import com.wordpress.view.container.TableLayoutManager;

//#ifdef IS_OS47_OR_ABOVE
import net.rim.device.api.ui.Touchscreen;
import com.wordpress.view.touch.BottomBarItem;
//#endif

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
			//		TableLayoutManager.USE_PREFERRED_SIZE,
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
					//TableLayoutManager.USE_PREFERRED_SIZE,
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
//					TableLayoutManager.USE_PREFERRED_SIZE,
					TableLayoutManager.SPLIT_REMAINING_WIDTH,
					TableLayoutManager.USE_PREFERRED_SIZE }, new int[] { 2, 2, 2 }, 5,
					Manager.USE_ALL_WIDTH);
			columnHeader = new String[]{_resources.getString(WordPressResource.MENUITEM_STATS_SEARCH),viewsColumnTitle};
			columnName = new String[]{"searchterm","views"};
			break;
			
		case StatsController.TYPE_TOP:
			statsDesc.setText(_resources.getString(WordPressResource.MESSAGE_STATS_TOP));
			outerTable = new TableLayoutManager(new int[] {
//					TableLayoutManager.USE_PREFERRED_SIZE,
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
		
		buildStatsTable(data, columnHeader, columnName, columnLink, outerTable);
	}
	
	private void buildStatsTable(byte[] data, String[] columnsHeader, String[] columnName, String[] columnLink, TableLayoutManager outerTable) {
		scrollerData.deleteAll();
		
		if(columnsHeader == null)
			columnsHeader = columnName;

		for (int i = 0; i < columnsHeader.length; i++) {
			outerTable.add(GUIFactory.getLabel(columnsHeader[i], DrawStyle.ELLIPSIS ));
		}
		for (int i = 0; i < columnsHeader.length; i++) {
			outerTable.add(GUIFactory.createSepatorField());
		}
		
		StatsParser statParser = new StatsParser(data);
		
		try {
			statParser.parseAll();
		} catch (IOException e1) {
			controller.displayError(e1, "");
			return;
		}

		try {
			while (statParser.hasNext()) {
				String[] nextLine = statParser.next();

				for (int i = 0; i < columnName.length; i++) {
					String _tmpName = columnName[i];
					int _tmpIdx = statParser.getColumnIndex(_tmpName);
					if(columnLink != null && columnLink[i] != null ) {
						int _tmpIdxLink = statParser.getColumnIndex(columnLink[i]); //retrive the url
						outerTable.add(GUIFactory.createClickableLabel(nextLine[_tmpIdx], nextLine[_tmpIdxLink], LabelField.FOCUSABLE | DrawStyle.ELLIPSIS));	
					} else 
					outerTable.add(GUIFactory.getLabel(nextLine[_tmpIdx], LabelField.FOCUSABLE |  DrawStyle.ELLIPSIS));
				}
			}
			
			//build the url for the chart
			if(controller.getType() == StatsController.TYPE_VIEW) {
				chartImg = null;
			} else { 
				//	http://chart.apis.google.com/chart?chs=500x300&chd=t:200,40,20&chds=0,1000&cht=p3&chdl=Hello|World|pippo&chdlp=bv&chco=FF0000,00FF00,0000FF
				statParser.reset();
				//StringBuffer chdl = new StringBuffer("&chdl=");
				StringBuffer chdl = new StringBuffer("");
				StringBuffer chd = new StringBuffer("&chd=t:");
				int max = 0;
				while (statParser.hasNext()) {
					String[] nextLine = statParser.next();
	
					for (int i = 0; i < columnName.length; i++) {
						String _tmpName = columnName[i];
						int _tmpIdx = statParser.getColumnIndex(_tmpName);
						if(i ==  0) {
							//key
							chdl.append(nextLine[_tmpIdx]+"|");
						} else {
							//value
							int value = Integer.parseInt(nextLine[_tmpIdx]);
							if (value > max) max = value;
							chd.append(nextLine[_tmpIdx]+",");
						}
												
						//outerTable.add(GUIFactory.getLabel(nextLine[_tmpIdx], LabelField.FOCUSABLE |  DrawStyle.ELLIPSIS));
						
					}
				}//end while
				//building the chart url
				chd.deleteCharAt(chd.length()-1);
				chdl.deleteCharAt(chdl.length()-1);
				String chartURL = "http://chart.apis.google.com/chart";
				
				//crate the link
				URLEncodedPostData urlEncoder = new URLEncodedPostData("UTF-8", false);
				urlEncoder.append("chl", chdl.toString());
				int width = Display.getWidth(); 
				int heigth = width/2;
				
				String chartParametersURL = "?chs="+width+"x"+heigth+"&cht=p3" +
				chd.toString() + "&chds=0,"+max+"&"+new String(urlEncoder.getBytes())+"chco=FF0000,00FF00,0000FF";
				chartImg = new WebBitmapField(chartURL+chartParametersURL, EncodedImage.getEncodedImageResource("loading-gif.bin"),
				Field.FIELD_HCENTER);
				scrollerData.add(chartImg);
    		}
		} catch (Exception e) {
			controller.displayError(e, "Error while parsing stats data");
		}
		scrollerData.add(outerTable);
	}
	
	//private String[] chartColors = {"FF0000","00FF00","0000FF", "FFFF66", "FF3399", };
	
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
		items[0] = new BottomBarItem("write.png", "write.png",  _resources.getString(WordPressResource.MENUITEM_STATS_VIEW));
		items[1] = new BottomBarItem("write.png", "write.png", _resources.getString(WordPressResource.MENUITEM_STATS_TOP));
		items[2] = new BottomBarItem("write.png", "write.png", _resources.getString(WordPressResource.MENUITEM_STATS_REFERRERS));
		items[3] = new BottomBarItem("write.png", "write.png",  _resources.getString(WordPressResource.MENUITEM_STATS_SEARCH));
		items[4] = new BottomBarItem("write.png", "write.png",  _resources.getString(WordPressResource.MENUITEM_STATS_CLICKS));

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

    //override onClose() to display a dialog box when the application is closed    
	public boolean onClose()   {
		controller.backCmd();
		return true;
    }
	
	public BaseController getController() {
		return controller;
	}
}