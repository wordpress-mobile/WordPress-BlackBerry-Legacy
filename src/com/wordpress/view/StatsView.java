//#preprocess
package com.wordpress.view;

import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.component.TextField;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.BaseController;
import com.wordpress.controller.StatsController;
import com.wordpress.view.component.PillButtonField;
import com.wordpress.view.container.PillButtonSet;

//#ifdef IS_OS47_OR_ABOVE
import net.rim.device.api.ui.Touchscreen;
import com.wordpress.view.touch.BottomBarItem;
//#endif


public class StatsView extends BaseView {
	
	private StatsController controller;
	private TextField statsDataTextField;
	
	private PillButtonSet topBarButton;
	private PillButtonField _7daysBtn;
	private PillButtonField _30daysBtn;
	private PillButtonField _90daysBtn;
	private PillButtonField _365daysBtn;
	private PillButtonField _allTimeBtn;
			
	private final String _7days = 7 +" " + _resources.getString(WordPressResource.LABEL_DAYS);
	private final String _30days = 30 +" " + _resources.getString(WordPressResource.LABEL_DAYS);
	private final String _90days = _resources.getString(WordPressResource.LABEL_QUARTER);
	private final String _365days = _resources.getString(WordPressResource.LABEL_YEAR);
	private final String _AllTime = _resources.getString(WordPressResource.LABEL_ALLTIME);
	
	public StatsView(StatsController _controller) {
		super(_resources.getString(WordPressResource.TITLE_STATS) , MainScreen.NO_VERTICAL_SCROLL 
				| Manager.NO_HORIZONTAL_SCROLL	| USE_ALL_HEIGHT | USE_ALL_WIDTH);
		
		this.controller=_controller;
		
		topBarButton = new PillButtonSet();
		_7daysBtn = new PillButtonField( "7" );
		_30daysBtn = new PillButtonField( "30" );
		_90daysBtn = new PillButtonField( "90" );
		_365daysBtn = new PillButtonField( "365" );
		_allTimeBtn = new PillButtonField( _AllTime );
		topBarButton.add( _7daysBtn );
		topBarButton.add( _30daysBtn );
		topBarButton.add( _90daysBtn );
		topBarButton.add( _365daysBtn );
		topBarButton.add(_allTimeBtn);
		topBarButton.setMargin( 5, 15, 5, 15 );
		//add(topBarButton);
		topBarButton.setSelectedField(_7daysBtn);
		
		VerticalFieldManager scrollerData = new VerticalFieldManager(VERTICAL_SCROLL | VERTICAL_SCROLLBAR | USE_ALL_WIDTH);
		statsDataTextField = new TextField(USE_ALL_WIDTH | READONLY);
		scrollerData.add(statsDataTextField);

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
	
	public void setStatsData() {
		statsDataTextField.setText(controller.getLastStatsData());
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