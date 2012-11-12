package com.gmi.nordborglab.browser.client.ui;

import java.util.Iterator;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.layout.client.Layout.AnimationCallback;
import com.google.gwt.layout.client.Layout.Layer;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.gwt.user.client.ui.Widget;

public class VerticalNavSplitLayoutPanel extends Composite implements HasWidgets {

	private static VerticalNavSplitLayoutPanelUiBinder uiBinder = GWT
			.create(VerticalNavSplitLayoutPanelUiBinder.class);

	interface VerticalNavSplitLayoutPanelUiBinder extends
			UiBinder<Widget, VerticalNavSplitLayoutPanel> {
	}

	public VerticalNavSplitLayoutPanel() {
		initWidget(uiBinder.createAndBindUi(this));
	}
	
	interface MyStyle extends CssResource {
		String splitter();
		String splitter_min();
		String focusPanel();
	}
	
	private double size = 17.923;
	private double minSize = 0.615;
	private boolean isHidden = false;
	@UiField MyStyle style;
	@UiField LayoutPanel container;
	@UiField HTMLPanel splitter;
	@UiField SimpleLayoutPanel content;
	
	@UiHandler("splitter_container")
	void handleClick(ClickEvent e) {
		setLayout();
	}
	
	
	private void setLayout() {
		final DockLayoutPanel layoutPanel = (DockLayoutPanel)getParent();
		if (isHidden) {
			layoutPanel.setWidgetSize(this.asWidget(), size);
			splitter.removeStyleName(style.splitter_min());
		}
		else {
			layoutPanel.setWidgetSize(this.asWidget(), minSize);
			splitter.addStyleName(style.splitter_min());
		}
		isHidden = !isHidden;
		/*workaround for bug http://code.google.com/p/google-web-toolkit/issues/detail?id=7188*/
		layoutPanel.animate(0,new AnimationCallback() {
			
			@Override
			public void onLayout(Layer layer, double progress) {
			}
			
			@Override
			public void onAnimationComplete() {
				layoutPanel.forceLayout();
			}
		});;
	}
	
	@Override
	public void add(Widget w) {
		content.add(w);
	}

	@Override
	public void clear() {
		content.clear();
		
	}

	@Override
	public Iterator<Widget> iterator() {
		return content.iterator();
	}

	@Override
	public boolean remove(Widget w) {
		return content.remove(w);
	}

}
