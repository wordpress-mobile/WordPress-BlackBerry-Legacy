//#preprocess
package com.wordpress.view;

import java.util.Enumeration;
import java.util.Hashtable;

import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.Menu;

import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.AccountsController;
import com.wordpress.controller.BaseController;
import com.wordpress.controller.MainController;
import com.wordpress.io.AppDAO;
import com.wordpress.utils.log.Log;
import com.wordpress.view.component.ListActionListener;
import com.wordpress.view.component.PostsListField;

//#ifdef IS_OS47_OR_ABOVE
import net.rim.device.api.ui.Touchscreen;
import com.wordpress.view.touch.BottomBarItem;
//#endif

public class AccountsView extends BaseView  implements ListActionListener {
	
    private AccountsController controller= null;
	private Hashtable accounts = null;
	private PostsListField listaAccounts;
	
	 public AccountsView(AccountsController  _controller) {
			super(_resources.getString(WordPressResource.TITLE_VIEW_ACCOUNTS), Manager.NO_VERTICAL_SCROLL | Manager.NO_VERTICAL_SCROLLBAR);
	    	this.controller=_controller;
    		this.accounts = MainController.getIstance().getApplicationAccounts();
			addMenuItem(_newAccountItem);
	    	buildList();
	 }
	 
	 //#ifdef IS_OS47_OR_ABOVE
	 private void initUpBottomBar(int size) {
		 if (Touchscreen.isSupported() == false) return;

		 int numberOfButtons = 1;
		 if( size > 1 ){
			 numberOfButtons = 2;
		 }
		 BottomBarItem items[] = new BottomBarItem[numberOfButtons];
		 items[0] = new BottomBarItem("bottombar_add.png", "bottombar_add.png", _resources.getString(WordPressResource.MENUITEM_NEW));
		 if(numberOfButtons == 2)
			 items[1] = new BottomBarItem("bottombar_delete.png", "bottombar_delete.png", _resources.getString(WordPressResource.MENUITEM_DELETE));

		 initializeBottomBar(items);
	 }

	 protected void bottomBarActionPerformed(int mnuItem) {
		 switch (mnuItem) {
		 case 0:
			 newAccount();
			 break;
		 case 1:
			 int selectedPost = listaAccounts.getSelectedIndex();
			 deleteAccount(selectedPost); 
			 break;
		 default:
			 break;
		 }
	 }
	 //#endif
	 

	 private Object[] adaptAccountHashtableToList() {
		 if (accounts == null) return new Object[0];
		 Object[] listFieldItems = new Object[accounts.size()];
		 Enumeration k = accounts.keys();
		 int i = 0;
		 while (k.hasMoreElements()) {
			 String key = (String) k.nextElement();
			 Hashtable currentAccount = (Hashtable)accounts.get(key);
			 String blogsNo = (String)currentAccount.get(AppDAO.BLOGNUMBER_KEY);

			 Hashtable currentListItem = new Hashtable();
			 currentListItem.put("title", key);
			 currentListItem.put("images_number", 
					 (blogsNo == null ? "" : blogsNo+ " " + _resources.getString(WordPressResource.LABEL_BLOGS)) );

			 listFieldItems[i] = currentListItem;
			 i++;
		 }
		 return listFieldItems;
	 }


	 private Hashtable getAccountHashtable(int selected) {
		 Enumeration k = accounts.keys();
		 Hashtable tmp =null;
		 int i = 0;
		 while (k.hasMoreElements()) {
			 String key = (String) k.nextElement();
			 if(selected == i) {
				 tmp = (Hashtable) accounts.get(key);
			 }
			 i++;
		 }
		 return tmp;
	 }
	 
	private void buildList() {
		
		listaAccounts = new PostsListField(); 	        
		listaAccounts.setEmptyString(_resources.getString(WordPressResource.MESSAGE_NO_ACCOUNTS), DrawStyle.LEADING);
		listaAccounts.setDefautActionListener(this);
		
        if( (accounts != null) && accounts.size() > 0 ){
        	listaAccounts.set(adaptAccountHashtableToList());
        } else {
        	//listaPost.set(new Object[0]);
        }
        	
        //#ifdef IS_OS47_OR_ABOVE
		int size = 0;
		if ((accounts != null)  && accounts.size() > 0)
			size = 3;
    	initUpBottomBar(size);
    	//#endif
		
		add(listaAccounts);
	}
     
	public boolean onClose()   {
		controller.backCmd();
		return true;
	}
    
	private void newAccount() {
		MainController.getIstance().addWPCOMBlogs();
		UiApplication.getUiApplication().popScreen(this);
	}
	
	private void deleteAccount(int selectedAccount) {
		if (selectedAccount == -1) {
			return;
		}
		int result = controller.askQuestion(_resources.getString(WordPressResource.MESSAGE_DELETE_ACCOUNT));   
		if(Dialog.YES != result) {
			return;
		}

		result = controller.askQuestion(_resources.getString(WordPressResource.MESSAGE_DELETE_ACCOUNT_2));   
		if(Dialog.YES != result) {
			return;
		}		

		if(MainController.getIstance().isLoadingBlogs()) {
			controller.displayMessage(_resources.getString(WordPressResource.MESSAGE_LOADING_BLOGS));
			return;
		}

		Log.trace("selected account " + selectedAccount);
		Hashtable accountHashtable = getAccountHashtable(selectedAccount);
		String username =  (String)accountHashtable.get("username");
		accounts.remove(username);
		try {
			AppDAO.storeAccounts(accounts);
		} catch (Exception e) {
			controller.displayError(e, "Error while removing accounts info");
		}

		//removing all associated blog
		MainController.getIstance().deleteBlogsByAccount(username);
		this.delete(listaAccounts);
		buildList();
	}
	
	private void editAccount(int selectedAccount) {
		if (selectedAccount == -1) {
			return;
		}
		Log.trace("selected account " + selectedAccount);
		Hashtable accountHashtable = getAccountHashtable(selectedAccount);
        AccountDetailView accountView = new AccountDetailView(controller, accountHashtable);
  		UiApplication.getUiApplication().pushScreen(accountView);
	}
	
    private MenuItem _deleteAccountItem = new MenuItem( _resources, WordPressResource.MENUITEM_DELETE, 220, 10) {
        public void run() {
           int selectedPost = listaAccounts.getSelectedIndex();
           deleteAccount(selectedPost);    
        }
    };
    
    private MenuItem _editAccountItem = new MenuItem( _resources, WordPressResource.MENUITEM_EDIT, 200, 10) {
        public void run() {
            int selectedPost = listaAccounts.getSelectedIndex();
            editAccount(selectedPost);
        }
    };
    
    private MenuItem _newAccountItem = new MenuItem( _resources, WordPressResource.MENUITEM_NEW, 210, 10) {
        public void run() {
        	newAccount();    
        }
    };
    
    //Override the makeMenu method so we can add a custom menu item
    protected void makeMenu(Menu menu, int instance)
    {
    	if( (accounts != null) && accounts.size() > 0 ){
    		menu.add(_editAccountItem);
    		menu.add(_deleteAccountItem);
    	}
    	//Create the default menu.
    	super.makeMenu(menu, instance);
    }
    
	public BaseController getController() {
		return controller;
	}

	public void actionPerformed() {
		int selectedPost = listaAccounts.getSelectedIndex();
		if (selectedPost == -1) {
			return;
		}
		editAccount(selectedPost);
	}
}