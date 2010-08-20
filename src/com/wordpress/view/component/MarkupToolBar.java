package com.wordpress.view.component;

import com.wordpress.view.GUIFactory;
import com.wordpress.view.component.MarkupToolBarTextFieldMediator.ButtonState;
import com.wordpress.view.container.JustifiedEvenlySpacedHorizontalFieldManager;

import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.component.ButtonField;

public class MarkupToolBar {
		
	private MarkupToolBarTextFieldMediator mediator;
	private JustifiedEvenlySpacedHorizontalFieldManager toolbarOne;
	private ButtonState[] buttonStateList = null;

	public MarkupToolBar(MarkupToolBarTextFieldMediator mediator) {

		toolbarOne = new JustifiedEvenlySpacedHorizontalFieldManager();	
		this.mediator = mediator;
		buttonStateList = this.mediator.getButtonStateList();
		
		for (int i = 0; i < buttonStateList.length; i++) {
			ButtonState tmp = buttonStateList[i];
			BaseButtonField tmpButton= GUIFactory.createButton(tmp.getLabel(), ButtonField.CONSUME_CLICK | ButtonField.USE_ALL_WIDTH | DrawStyle.ELLIPSIS);
			final int tempIndex = i;
			tmpButton.setChangeListener(
				new FieldChangeListener() {
					public void fieldChanged(Field field, int context) {
						MarkupToolBar.this.mediator.actionPerformed(tempIndex);
					}
				}
			);
			toolbarOne.add(tmpButton);
		}
	}
	
	public void  changeButtonLabel(int i, String newLabel) {
		Field field = toolbarOne.getField(i);
		if(field != null && field instanceof EmbossedButtonField) {
			((EmbossedButtonField)field).setText(newLabel);
			field.getManager().invalidate();
		}
	}
	
	public void attachTo(Manager screen) {
		screen.add(toolbarOne);
	}

}
