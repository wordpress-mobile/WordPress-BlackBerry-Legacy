//#preprocess
package com.wordpress.view;

import java.io.IOException;
import java.util.Vector;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Display;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.component.TextField;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

import com.wordpress.bb.WordPressInfo;
import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.BaseController;
import com.wordpress.controller.StatsController;
import com.wordpress.utils.Tools;
import com.wordpress.utils.csv.StatsParser;
import com.wordpress.utils.log.Log;
import com.wordpress.view.component.ColoredLabelField;
import com.wordpress.view.component.WebBitmapField;
import com.wordpress.view.container.TableLayoutManager;

//#ifdef VER_4.7.0 | BlackBerrySDK5.0.0 | BlackBerrySDK6.0.0 | BlackBerrySDK7.0.0
import net.rim.device.api.ui.Touchscreen;
import com.wordpress.view.touch.BottomBarItem;
import net.rim.device.api.ui.decor.BackgroundFactory;
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
		
		scrollerData = new VerticalFieldManager(VERTICAL_SCROLL | VERTICAL_SCROLLBAR | USE_ALL_WIDTH | FIELD_HCENTER);
		scrollerData.setMargin(2,5,2,5);
		
		statsDesc = new TextField(USE_ALL_WIDTH | READONLY);
		statsDesc.setMargin(0,0,5,0);
		scrollerData.add(statsDesc);
		
		add(scrollerData);
		
		//#ifdef VER_4.7.0 | BlackBerrySDK5.0.0 | BlackBerrySDK6.0.0 | BlackBerrySDK7.0.0
		initUpBottomBar();
		//#endif
		
		addMenuItem(_viewsItem);
		addMenuItem(_topPostAndPageItem);
		addMenuItem(_referrersItem);
		addMenuItem(_searchEngineTermsItem);
		addMenuItem(_clicksItem);
		addMenuItem(_videoPlayItem);
		
		updateSubTitle();
		controller.bumpScreenViewStats("com/wordpress/view/StatsView", "Stats View", "", null, "");
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
		
		case StatsController.TYPE_VIDEO:
			statsDesc.setText(_resources.getString(WordPressResource.MESSAGE_STATS_VIDEO));
			outerTable = new TableLayoutManager(new int[] {
					TableLayoutManager.USE_PREFERRED_SIZE,
					TableLayoutManager.SPLIT_REMAINING_WIDTH,
					TableLayoutManager.USE_PREFERRED_SIZE }, new int[] { 2, 2, 2 }, 5,
					Manager.USE_ALL_WIDTH);
			columnHeader = new String[]{_resources.getString(WordPressResource.LABEL_TITLE),viewsColumnTitle};
			columnName = new String[]{"video_title","views"};
			columnLink = new String[]{null,null};
			break;	
			
		default:
			return;
		}
		//clean the prev stats ui elements: graph and table...
		scrollerData.deleteAll();
		scrollerData.add(statsDesc);
		fillStatsUI(data, columnHeader, columnName, columnLink, outerTable);
	}
	
	private void fillStatsUI(byte[] data, String[] columnsHeader, String[] columnsName, String[] columnLink, TableLayoutManager outerTable) {

		//1. parse the data
		StatsParser statParser = new StatsParser(data);
		try {
			statParser.parseAll();
		} catch (IOException e1) {
			if( e1 instanceof IOException && e1.getMessage() != null && e1.getMessage().equalsIgnoreCase( StatsParser.NO_STATS_DATA_AVAILABLE )) {
				controller.displayMessage("No stats data found.  Please try again later.");
			} else {
				controller.displayError(e1, "");
			}
			return;
		}
		
		//2.fill the stats Table
		if(columnsHeader == null)
			columnsHeader = columnsName;

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
			
			//reverse the order of the row
			Vector reversedData = new Vector();
			if(controller.getType() == StatsController.TYPE_VIEW) {
				while (statParser.hasNext()) {
					String[] nextLine = statParser.next();
					reversedData.addElement(nextLine);
				}
				statParser.reset();
			}
			
			int counter = 1;
			while (statParser.hasNext()) {
				String[] nextLine = statParser.next();

				if(controller.getType() == StatsController.TYPE_VIEW) {
					//reverse the order of the row
					 nextLine = (String[])reversedData.elementAt(reversedData.size()- counter);
				}
				
				LabelField currentLabelField = null;
				
				if(controller.getType() != StatsController.TYPE_VIEW) {
					currentLabelField = GUIFactory.getLabel(String.valueOf(counter), Color.GRAY, LabelField.FOCUSABLE |  DrawStyle.ELLIPSIS);
					outerTable.add(currentLabelField);
					//#ifdef VER_4.7.0 | BlackBerrySDK5.0.0 | BlackBerrySDK6.0.0 | BlackBerrySDK7.0.0
					if(counter % 2 != 0)
						currentLabelField.setBackground(BackgroundFactory.createSolidBackground(0xf5f5f5));
					else
						currentLabelField.setBackground(BackgroundFactory.createSolidBackground(Color.WHITE));
					//#endif
				}
				
				for (int i = 0; i < columnsName.length; i++) {
					String _tmpName = columnsName[i];
					int _tmpIdx = statParser.getColumnIndex(_tmpName);
					String value = nextLine[_tmpIdx];
					
					//format the string if it regard the views
					if(_tmpName.equalsIgnoreCase("views"))
						value = Tools.groupDigits(value, 3, ',');
					
					if(columnLink != null && columnLink[i] != null ) {
						int _tmpIdxLink = statParser.getColumnIndex(columnLink[i]); //retrieve the url
						currentLabelField = GUIFactory.createURLLabelField(value, nextLine[_tmpIdxLink], LabelField.USE_ALL_WIDTH | LabelField.FOCUSABLE | DrawStyle.ELLIPSIS);
						outerTable.add(currentLabelField);	
					} else {
						currentLabelField = new ColoredLabelField(value + " ", Color.BLACK, LabelField.USE_ALL_WIDTH | LabelField.FOCUSABLE |  DrawStyle.ELLIPSIS);
						outerTable.add(currentLabelField);
					}
					
					//#ifdef VER_4.7.0 | BlackBerrySDK5.0.0 | BlackBerrySDK6.0.0 | BlackBerrySDK7.0.0
					if(counter % 2 != 0)
						currentLabelField.setBackground(BackgroundFactory.createSolidBackground(0xf5f5f5));
					else
						currentLabelField.setBackground(BackgroundFactory.createSolidBackground(Color.WHITE));
					//#endif
				}
				counter++;
			}
		} catch (Exception e) {
			controller.displayError(e, "Error while filling stats UI");
		}
		
		//3. build the graphs
		buildChart(columnsName, statParser);	
		scrollerData.add(outerTable);
	}

	private void buildChart(String[] columnName, StatsParser statParser) {
		try {
			statParser.reset();
			if(statParser.hasNext() == false) return;
			
			String chartParametersURL = "";
			
			chartImg = null;

			if(controller.getType() == StatsController.TYPE_VIEW) {
				//draw a line chart
				Vector dateStrings = new Vector();
				StringBuffer dataValues = new StringBuffer("");
				int maxValue = Integer.MIN_VALUE;
				int minValue = Integer.MAX_VALUE;
				while (statParser.hasNext()) {
					String[] nextLine = statParser.next();

					for (int i = 0; i < columnName.length; i++) {
						String _tmpName = columnName[i];
						int _tmpIdx = statParser.getColumnIndex(_tmpName);
						if(i ==  0) {
							dateStrings.addElement(nextLine[_tmpIdx]);
						} else {
							int value = 0;
							if ( _tmpIdx != -1 && nextLine.length > _tmpIdx )
								value = Integer.parseInt(nextLine[_tmpIdx]);
							if (value > maxValue) maxValue = value;
							if (value < minValue) minValue = value;
							dataValues.append(value+",");
						}
					}
				}//end while

				dataValues.deleteCharAt(dataValues.length()-1);
				long maxBuffer = (long) (maxValue +  Tools.round(maxValue * .10));
				//chxl=0:|Jan|Feb|March|April|May|
				String startLabel = (String)dateStrings.elementAt(0);
				String endLabel = startLabel; 
				if(dateStrings.size() > 1)
					endLabel = (String)dateStrings.elementAt((dateStrings.size()-1));
				
				//check if the bar width is not too large.
				//int chartDimensionToContainAllBars = (dateStrings.size()*23) + (dateStrings.size()*4);
				//scrollerData.add( GUIFactory.createURLLabelField("Open in Browser", WordPressInfo.STATS_CHART_URL+chartParametersURL+"&chs="+chartDimensionToContainAllBars+"x"+getPredefinedChartHeigth(), LabelField.FOCUSABLE) );
				
				chartParametersURL = "?cht=bvs" + //chart type
						"&chco=a3bcd3" + //color
						"&chxt=x,y" + //Visible Axes
						"&chxr=1,0,"+maxBuffer+"" + //Axis Range
						"&chds=0,"+maxBuffer + //Custom scaling 
						"&chxl=0:|"+startLabel+"|"+endLabel+ //Custom Axis Labels
						"&chxp=0,0,100" + //Axis Label Positions
						"&chxs=1N*sz0*" + //Axis Label Styles
						"&chbh=a"+ //Bar Width and Spacing
						"&chd=t:"+dataValues.toString();
			} else { 
				
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
							int value = 0;
							if ( _tmpIdx != -1 && nextLine.length > _tmpIdx )
								value = Integer.parseInt(nextLine[_tmpIdx]);
							if (value > max) max = value;
							chd.append(nextLine[_tmpIdx]+",");
						}
					}
					counter++;
				}//end while


				chd.deleteCharAt(chd.length()-1);
				chdl.deleteCharAt(chdl.length()-1);
				long maxBuffer = (long) (max +  Tools.round(max * .10));
				
				chartParametersURL ="?cht=bvs" +
					"&chxt=x,y" +
					"&chxr=1,0,"+maxBuffer+"" +
					"&chds=0,"+maxBuffer+
					"&chxs=1N*sz0*"+
				     chd.toString() + 
				     chdl.toString()+
				     "&chco=a3bcd3"; //color
				     //"&chf=c,lg,90,DADEDA,0,F6FAF6,1";
			}

			chartImg = new ChartBitmap(WordPressInfo.STATS_CHART_URL+chartParametersURL, 
					Bitmap.getBitmapResource("stats.png"),
					Field.FIELD_HCENTER | Field.FOCUSABLE | Field.USE_ALL_WIDTH  | DrawStyle.HCENTER);
			chartImg.setMargin(5,0,5,0);

			scrollerData.add(chartImg);
			
		} catch (Exception e) {
			controller.displayError(e, "Error while creating the chart.");
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
		
		if( controller.getType() == StatsController.TYPE_VIEW) {
			updateSubTitleForTypeView();
			return;
		}
		
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
		case StatsController.TYPE_VIDEO:
			subtitle += _resources.getString(WordPressResource.MENUITEM_STATS_VIDEOPLAYS);
			break;
		default:
			subtitle += _resources.getString(WordPressResource.MENUITEM_STATS_VIEW);
			break;
		}
		this.setSubTitleText(subtitle);
	}
	
	
	private void updateSubTitleForTypeView() {
		String subtitle = null;
		
		switch (controller.getIntervalForTypeView()) {
		case StatsController.INTERVAL_TYPE_VIEW_DAYS:
			subtitle = _resources.getString(WordPressResource.MESSAGE_STATS_VIEWS_DAY);
			break;
		case StatsController.INTERVAL_TYPE_VIEW_WEEKS:
			subtitle = _resources.getString(WordPressResource.MESSAGE_STATS_VIEWS_WEEK);
			break;
		case StatsController.INTERVAL_TYPE_VIEW_MONTHS:
			subtitle = _resources.getString(WordPressResource.MESSAGE_STATS_VIEWS_MONTH);
			break;
		default:
			subtitle = " ";
			break;
		}

		this.setSubTitleText(subtitle);
	}
	
	
	private void changeStatsInterval(int interval) {
		controller.setInterval(interval);
		controller.retrieveStats();
		updateSubTitle();
	}

	private void changeStatsType(int type) {
		controller.setType(type);
		controller.retrieveStats();
		updateSubTitle();
	}

	
	
	// Override the makeMenu method so we can add a custom menu item
	protected void makeMenu(Menu menu, int instance) {
		switch (controller.getType()) {
		case StatsController.TYPE_VIEW:
			menu.add(_daysForTypeViewMenuItem);
			menu.add(_weeksForTypeViewMenuItem);
			menu.add(_monthsForTypeViewMenuItem);
			break;
		default:
			menu.add(_7daysMenuItem);
			menu.add(_30daysMenuItem);
			menu.add(_90daysMenuItem);
			menu.add(_365daysMenuItem);
			menu.add(_AllTimeMenuItem);
			break;
		}

		// Create the default menu.
		super.makeMenu(menu, instance);
	}

	
	private MenuItem _daysForTypeViewMenuItem = new MenuItem(_resources, WordPressResource.LABEL_DAYS, 120, 100) {
		public void run() {
			controller.setIntervalForTypeView(StatsController.INTERVAL_TYPE_VIEW_DAYS);
			controller.retrieveStats();
			updateSubTitle();
		}
	};
	private MenuItem _weeksForTypeViewMenuItem = new MenuItem(_resources, WordPressResource.LABEL_WEEKS, 130, 100) {
		public void run() {
			controller.setIntervalForTypeView(StatsController.INTERVAL_TYPE_VIEW_WEEKS);
			controller.retrieveStats();
			updateSubTitle();			 
		}
	};
	private MenuItem _monthsForTypeViewMenuItem = new MenuItem(_resources, WordPressResource.LABEL_MONTHS, 140, 100) {
		public void run() {
			controller.setIntervalForTypeView(StatsController.INTERVAL_TYPE_VIEW_MONTHS);
			controller.retrieveStats();
			updateSubTitle();			 
		}
	};

	private MenuItem _7daysMenuItem = new MenuItem(_7days, 100, 100) {
		public void run() {
			changeStatsInterval(StatsController.INTERVAL_7DAYS);
		}
	};
	private MenuItem _30daysMenuItem = new MenuItem(_30days, 110, 100) {
		public void run() {
			changeStatsInterval(StatsController.INTERVAL_30DAYS);
		}
	};
	private MenuItem _90daysMenuItem = new MenuItem( _90days, 120, 100) {
		public void run() {
			changeStatsInterval(StatsController.INTERVAL_QUARTER);
		}
	};
	private MenuItem _365daysMenuItem = new MenuItem( _365days, 130, 100) {
		public void run() {
			changeStatsInterval(StatsController.INTERVAL_YEAR);
		}
	};
	private MenuItem _AllTimeMenuItem = new MenuItem( _AllTime, 140, 100) {
		public void run() {
			changeStatsInterval(StatsController.INTERVAL_ALL);
		}
	};
	 
	 private MenuItem _viewsItem = new MenuItem( _resources, WordPressResource.MENUITEM_STATS_VIEW, 100000, 200) {
		 public void run() {
			 changeStatsType(StatsController.TYPE_VIEW);
		 }
	 };
	 private MenuItem _topPostAndPageItem = new MenuItem( _resources, WordPressResource.MENUITEM_STATS_TOP, 100000, 200) {
		 public void run() {
			 changeStatsType(StatsController.TYPE_TOP);
		 }
	 };
	 private MenuItem _referrersItem = new MenuItem( _resources, WordPressResource.MENUITEM_STATS_REFERRERS, 100000, 200) {
		 public void run() {
			 changeStatsType(StatsController.TYPE_REFERRERS);
		 }
	 };
	 private MenuItem _searchEngineTermsItem = new MenuItem( _resources, WordPressResource.MENUITEM_STATS_SEARCH, 100000, 200) {
		 public void run() {
			 changeStatsType(StatsController.TYPE_SEARCH);
		 }
	 };
	 private MenuItem _clicksItem = new MenuItem( _resources, WordPressResource.MENUITEM_STATS_CLICKS, 100000, 200) {
		 public void run() {
			 changeStatsType(StatsController.TYPE_CLICKS);
		 }
	 };
	 private MenuItem _videoPlayItem = new MenuItem( _resources, WordPressResource.MENUITEM_STATS_VIDEOPLAYS, 100000, 200) {
		 public void run() {
			 changeStatsType(StatsController.TYPE_VIDEO);
		 }
	 };
	private VerticalFieldManager scrollerData;
	
	//#ifdef VER_4.7.0 | BlackBerrySDK5.0.0 | BlackBerrySDK6.0.0 | BlackBerrySDK7.0.0
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
			changeStatsType(StatsController.TYPE_VIEW);
			break;
		case 1:
			changeStatsType(StatsController.TYPE_TOP);
			break;
		case 2:
			changeStatsType(StatsController.TYPE_REFERRERS);
			break;
		case 3:
			changeStatsType(StatsController.TYPE_SEARCH);
			break;
		case 4:
			changeStatsType(StatsController.TYPE_CLICKS);
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