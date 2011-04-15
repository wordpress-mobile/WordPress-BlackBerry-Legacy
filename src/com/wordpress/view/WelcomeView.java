package com.wordpress.view;

import net.rim.device.api.system.Display;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.Ui;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.MainScreen;

import com.wordpress.bb.WordPressInfo;
import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.BaseController;
import com.wordpress.controller.MainController;
import com.wordpress.utils.Tools;
import com.wordpress.view.component.ColoredLabelField;
import com.wordpress.view.component.PillButtonField;

public class WelcomeView extends StandardBaseView {

	private MainController mainController = null;

	private  BitmapField wpPromoBitmapField;
	private  EncodedImage promoImg;

	public WelcomeView() {
		super(MainScreen.NO_VERTICAL_SCROLL | Manager.NO_HORIZONTAL_SCROLL | USE_ALL_HEIGHT);
		this.mainController=MainController.getIstance();

		if (Display.getWidth() >= 360  ) {
			promoImg = EncodedImage.getEncodedImageResource("wp_blue-xl.png");
		} else {
			promoImg = EncodedImage.getEncodedImageResource("wp_blue-l.png");
		}
		wpPromoBitmapField =  new BitmapField(promoImg.getBitmap(), Field.FIELD_HCENTER | Field.FIELD_VCENTER | Field.FOCUSABLE) {
			protected void drawFocus(Graphics graphics, boolean on) {
				//disabled the default focus behavior so that blue rectangle isn't drawn
			}
		};

		int width = Display.getWidth();
		add(wpPromoBitmapField);

		Font fnt = Font.getDefault().derive(Font.BOLD);
		int fntHeight = fnt.getHeight();
		fnt = Font.getDefault().derive(Font.BOLD, fntHeight+2, Ui.UNITS_px);

		HorizontalFieldManager taglineManager = new HorizontalFieldManager(Field.FIELD_HCENTER |Field.USE_ALL_WIDTH);
		LabelField lblField = new ColoredLabelField(_resources.getString(WordPressResource.PROMOSCREEN_TAGLINE), 
				 Color.WHITESMOKE, Field.USE_ALL_WIDTH | DrawStyle.HCENTER);
		lblField.setFont(fnt);

		taglineManager.add(lblField);
		if (width > 320)
			lblField.setMargin( 15, 30, 15, 30 );
		else
			lblField.setMargin( 6, 4, 4, 4 );

		add(taglineManager);

		HorizontalFieldManager buttonsManagerOne = new HorizontalFieldManager(Field.FIELD_HCENTER);
		PillButtonField buttonHaveBlog = new PillButtonField(_resources.getString(WordPressResource.PROMOSCREEN_BUTTON_HAVE_A_WPCOM_BLOG));
		buttonHaveBlog.setDrawPosition(PillButtonField.DRAWPOSITION_SINGLE);
		buttonHaveBlog.setChangeListener(new FieldChangeListener() {
			public void fieldChanged(Field field, int context) {
				mainController.addWPCOMBlogs();
			}
		});

		if (width > 320)
			buttonHaveBlog.setMargin( 5, 30, 15, 30 );
		else
			buttonHaveBlog.setMargin( 4, 4, 4, 4 );

		buttonsManagerOne.add(buttonHaveBlog);

		HorizontalFieldManager buttonsManagerTwo = new HorizontalFieldManager(Field.FIELD_HCENTER);
		PillButtonField buttonGetFreeBlog = new PillButtonField(_resources.getString(WordPressResource.PROMOSCREEN_BUTTON_NEW_TO_WP_BLOG));
		buttonGetFreeBlog.setDrawPosition(PillButtonField.DRAWPOSITION_SINGLE);
		buttonGetFreeBlog.setChangeListener(new FieldChangeListener() {
			public void fieldChanged(Field field, int context) {
				/*SignUpBlogController ctrl = new SignUpBlogController();
	    			ctrl.showView();*/
				Tools.openURL(WordPressInfo.BB_APP_SIGNUP_URL);
			}
		});

		if (width > 320)
			buttonGetFreeBlog.setMargin( 5, 30, 15 , 30 );
		else
			buttonGetFreeBlog.setMargin( 4, 4, 4, 4 );

		buttonsManagerTwo.add(buttonGetFreeBlog);


		HorizontalFieldManager buttonsManagerSelfHosted = new HorizontalFieldManager(Field.FIELD_HCENTER);
		PillButtonField buttonSelfHostedBlog = new PillButtonField(_resources.getString(WordPressResource.PROMOSCREEN_BUTTON_HAVE_A_WPORG_BLOG));
		buttonSelfHostedBlog.setDrawPosition(PillButtonField.DRAWPOSITION_SINGLE);
		buttonSelfHostedBlog.setChangeListener(new FieldChangeListener() {
			public void fieldChanged(Field field, int context) {
				mainController.addWPORGBlogs();
			}
		});

		if (width > 320)
			buttonSelfHostedBlog.setMargin( 5, 30, 15 , 30 );
		else
			buttonSelfHostedBlog.setMargin( 4, 4, 4, 4 );

		buttonsManagerSelfHosted.add(buttonSelfHostedBlog);

		add(buttonsManagerTwo);
		add(buttonsManagerOne);
		add(buttonsManagerSelfHosted);


	}

	public BaseController getController() {
		return mainController;
	}
}