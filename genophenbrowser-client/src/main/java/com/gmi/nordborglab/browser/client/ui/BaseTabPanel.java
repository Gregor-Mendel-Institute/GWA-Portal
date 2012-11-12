package com.gmi.nordborglab.browser.client.ui;

import java.util.ArrayList;
import java.util.List;

import com.gmi.nordborglab.browser.client.TabDataDynamic;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ProvidesResize;
import com.google.gwt.user.client.ui.ResizeComposite;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.gwtplatform.mvp.client.Tab;
import com.gwtplatform.mvp.client.TabData;
import com.gwtplatform.mvp.client.TabPanel;

public abstract class BaseTabPanel extends ResizeComposite implements TabPanel,ProvidesResize {

	Tab currentActiveTab;

	@UiField 
	FlowPanel tabPanel;
	
	@UiField
	SimpleLayoutPanel tabContentPanel;

	private final List<BaseTab> tabList = new ArrayList<BaseTab>();

	@Override
	public Tab addTab(TabData tabData, String historyToken) {
		BaseTab newTab = createNewTab(tabData);
		int beforeIndex;
		for (beforeIndex = 0; beforeIndex < tabList.size(); ++beforeIndex) {
			if (newTab.getPriority() < tabList.get(beforeIndex).getPriority()) {
				break;
			}
		}
		tabPanel.insert(newTab.asWidget(), beforeIndex);
		tabList.add(beforeIndex, newTab);
		newTab.setTargetHistoryToken(historyToken);
		setTabVisibility(newTab);
		return newTab;
	}

	@Override
	public void removeTab(Tab tab) {
		tabPanel.remove(tab.asWidget());
		tabList.remove(tab);
	}

	@Override
	public void removeTabs() {
		tabPanel.clear();
		tabList.clear();
	}

	@Override
	public void setActiveTab(Tab tab) {
		if (currentActiveTab != null) {
			currentActiveTab.deactivate();
		}
		if (tab != null) {
			tab.activate();
		}
		currentActiveTab = tab;
		setTabVisibility((BaseTab)tab);
	}

	@Override
	public void changeTab(Tab tab, TabData tabData, String historyToken) {
		tab.setText(tabData.getLabel());
		tab.setTargetHistoryToken(historyToken);
		if (tabData instanceof TabDataDynamic) {
			((BaseTab)tab).setCanUserAccess(((TabDataDynamic)tabData).hasAccess());
		}
		setTabVisibility((BaseTab)tab,true);
	}

	public void refreshTabs() {
		for (BaseTab tab : tabList) {
			setTabVisibility(tab);
		}
	}

	private void setTabVisibility(BaseTab tab) {
		setTabVisibility(tab, false);
	}
	
	private void setTabVisibility(BaseTab tab,boolean overrideActiveTabCheck) {
		boolean visible = ((tab == currentActiveTab) && !overrideActiveTabCheck)  || tab.canUserAccess();
		tab.setVisible(visible);
	}

	protected abstract BaseTab createNewTab(TabData tabData);
	
	public void setPanelContent(Widget panelContent) {
	    tabContentPanel.clear();
	    if (panelContent != null) {
	    	tabContentPanel.add(panelContent);
	    }
	  }
}
