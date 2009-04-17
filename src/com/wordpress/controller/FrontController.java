package com.wordpress.controller;


/*
 * a soft of generic front controller.
 */

public class FrontController extends BaseController{
	private static FrontController singletonObject;
	
	
	public static FrontController getIstance() {
		if (singletonObject == null) {
			singletonObject = new FrontController();
		}
		return singletonObject;
	}
    
    //singleton
    private FrontController() {

    }
    
	public void showView(String nextScreen){
		if("AddBlogsView".equals(nextScreen)){
			new AddBlogsController();
		}else if("AboutView".equals(nextScreen)){
			new AboutController();
		} else {
			displayError("Action Error");
		}
	}
}