package com.gmi.nordborglab.browser.client.mvp.view.diversity;

import java.util.Map.Entry;

import com.github.gwtbootstrap.client.ui.AccordionGroup;
import com.github.gwtbootstrap.client.ui.base.DivWidget;
import com.github.gwtbootstrap.client.ui.base.ListItem;
import com.github.gwtbootstrap.client.ui.base.UnorderedList;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.DiversityPresenter;
import com.google.common.collect.ImmutableMap;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineHyperlink;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewImpl;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;

public class DiversityView extends ViewImpl implements
		DiversityPresenter.MyView {

	private final Widget widget;

	public interface Binder extends UiBinder<Widget, DiversityView> {
	}

	@UiField
	SimpleLayoutPanel container;
	@UiField
	FlowPanel breadcrumbs;
	@UiField Label titleLabel;
	@UiField MyStyle style;
	@UiField AccordionGroup experimentAccGroup;
	@UiField AccordionGroup phenotypeAccGroup;
	@UiField AccordionGroup studyAccGroup;
	@UiField AccordionGroup ontologiesAccGroup;
	@UiField SimpleLayoutPanel searchContainer;
	private ImmutableMap<MENU_ITEM, AccordionGroup> menuItems;
	private MENU_ITEM isOpenMenuItem;
	
	public interface MyStyle extends CssResource {
		String header_section_active();
		String subitem_active();
	}
	
	
	public enum MENU_ITEM  {EXPERIMENT,PHENOTYPE,STUDY,ONTOLOGY}
	
	private final PlaceManager placeManager;

	@Inject
	public DiversityView(final Binder binder,final PlaceManager placeManager) {
		this.placeManager = placeManager;
		widget = binder.createAndBindUi(this);
		menuItems = ImmutableMap.<MENU_ITEM, AccordionGroup>builder()
				.put(MENU_ITEM.EXPERIMENT, experimentAccGroup)
				.put(MENU_ITEM.PHENOTYPE,phenotypeAccGroup)
				.put(MENU_ITEM.STUDY,studyAccGroup)
				.put(MENU_ITEM.ONTOLOGY,ontologiesAccGroup).build();
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void setInSlot(Object slot, Widget content) {
		if (slot == DiversityPresenter.TYPE_SetMainContent) {
			setMainContent(content);
		}
		else if (slot == DiversityPresenter.TYPE_SearchPresenterContent) {
			if (content == null) {
				searchContainer.clear();
			}
			else {
				searchContainer.add(content);
			}
		}
		else {
			super.setInSlot(slot, content);
		}
	}

	private void setMainContent(Widget content) {
		if (content != null) {
			container.setWidget(content);
		}
	}

	@Override
	public void clearBreadcrumbs(int breadcrumbSize) {
		breadcrumbs.clear();
		if (breadcrumbSize > 0)
			breadcrumbs.add(new InlineHyperlink("Loading title...",""));
		for (int i = 0; i < breadcrumbSize; ++i) {
			breadcrumbs.add(new InlineLabel(" > "));
			breadcrumbs.add(new InlineHyperlink("Loading title...",""));
		}
	}

	@Override
	public void setBreadcrumbs(int index, String title,String historyToken) {
		InlineHyperlink hyperlink = null;
		if (index ==0)
			hyperlink = (InlineHyperlink) breadcrumbs.getWidget(0);
		else
		    hyperlink = (InlineHyperlink) breadcrumbs
			 	.getWidget((index *2));
		if (title == null) {
			hyperlink.setText("Unknown title");
		} else {
			hyperlink.setText(title);
		}
		hyperlink.setTargetHistoryToken(historyToken);
	}

	@Override
	public void setTitle(String title) {
		if (title != null)
			titleLabel.setText(title);
	}
	
	@Override
	public void setActiveMenuItem(MENU_ITEM menuItem, PlaceRequest request) {
		for (Entry<MENU_ITEM, AccordionGroup> entry:menuItems.entrySet()) {
			AccordionGroup accordionGroup = entry.getValue();
			Widget header = accordionGroup.getWidget(0);
			header.removeStyleName(style.header_section_active());
			setActiveSubMenuItem(accordionGroup, null);
			if (entry.getKey() == menuItem) {
				header.addStyleName(style.header_section_active());
				setActiveSubMenuItem(accordionGroup, request);
				if (isOpenMenuItem != menuItem) {
					accordionGroup.show();
					isOpenMenuItem = menuItem;
				}
			}
		}
	}
	
	private void setActiveSubMenuItem(AccordionGroup accordionGroup,PlaceRequest request) {
		try  {
			DivWidget innerBody= (DivWidget)((DivWidget)accordionGroup.getWidget(1)).getWidget(0);
			UnorderedList ul = (UnorderedList)innerBody.getWidget(0);
			for (int i = 0;i<ul.getWidgetCount();i++) {
				ListItem li = (ListItem)ul.getWidget(0);
				InlineHyperlink link = (InlineHyperlink)li.getWidget(0);
				if (request.matchesNameToken(link.getTargetHistoryToken())) {
					link.addStyleName(style.subitem_active());
				}
				else {
					link.removeStyleName(style.subitem_active());
				}
			}
		}
		catch (Exception e) {}
	}
	
}
