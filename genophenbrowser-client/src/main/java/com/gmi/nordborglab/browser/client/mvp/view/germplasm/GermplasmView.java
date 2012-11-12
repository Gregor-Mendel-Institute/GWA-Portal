package com.gmi.nordborglab.browser.client.mvp.view.germplasm;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.github.gwtbootstrap.client.ui.Accordion;
import com.github.gwtbootstrap.client.ui.AccordionGroup;
import com.github.gwtbootstrap.client.ui.base.ListItem;
import com.github.gwtbootstrap.client.ui.base.UnorderedList;
import com.gmi.nordborglab.browser.client.NameTokens;
import com.gmi.nordborglab.browser.client.ParameterizedPlaceRequest;
import com.gmi.nordborglab.browser.client.mvp.presenter.germplasm.GermplasmPresenter;
import com.gmi.nordborglab.browser.client.resources.MainResources;
import com.gmi.nordborglab.browser.shared.proxy.AlleleAssayProxy;
import com.gmi.nordborglab.browser.shared.proxy.TaxonomyProxy;
import com.google.common.collect.ImmutableList;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.InlineHyperlink;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewImpl;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;

public class GermplasmView extends ViewImpl implements
		GermplasmPresenter.MyView {

	private final Widget widget;

	public interface Binder extends UiBinder<Widget, GermplasmView> {
	}
	
	public interface MyStyle extends CssResource {
		String header_section();
		String accordion_group_top();
		String header_section_active();
		String subitem_list();
		String subitem();
		String subitem_active();
		String icon();
	}
	
	
	@UiField
	SimpleLayoutPanel container;
	
	@UiField
	FlowPanel breadcrumbs;
	
	@UiField 
	FlowPanel menuContainer;
	
	@UiField MyStyle style;
	
	@UiField Label titleLabel;
	private boolean menuInitialized = false;
	private MainResources mainRes;
	
	private final PlaceManager placeManager;
	private Accordion accordion= null;
	private Map<Long,AccordionGroup> accordionGroups = new HashMap<Long,AccordionGroup>();
	private Map<Long,UnorderedList> subMenuItems = new HashMap<Long, UnorderedList>();

	@Inject
	public GermplasmView(final Binder binder, final PlaceManager placeManager, final MainResources mainRes) {
		widget = binder.createAndBindUi(this);
		this.placeManager = placeManager;
		this.mainRes = mainRes;
	}

	@Override
	public Widget asWidget() {
		return widget;
	}
	
	@Override
	public void setInSlot(Object slot, Widget content) {
		if (slot == GermplasmPresenter.TYPE_SetMainContent) {
			setMainContent(content);
		} else {
			super.setInSlot(slot, content);
		}
	}

	private void setMainContent(Widget content) {
		if (content != null) {
			container.setWidget(content);
		}
	}
	
	@Override
	public void setTitle(String title) {
		if (title != null)
			titleLabel.setText(title);
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
	public void initMenu(ImmutableList<TaxonomyProxy> taxonomies) {
		if (menuInitialized)
			return;
		accordionGroups.clear();
		subMenuItems.clear();
		InlineHyperlink subItem = null;
		ListItem li = null;
		PlaceRequest request = new ParameterizedPlaceRequest(NameTokens.taxonomy);
		PlaceRequest requestPassports = new ParameterizedPlaceRequest(NameTokens.passports);
		accordion = new Accordion();
		int i = 0;
		for (TaxonomyProxy taxonomy:taxonomies) {
			request.with("id", taxonomy.getId().toString());
			requestPassports.with("id", taxonomy.getId().toString());
			AccordionGroup menuItem = new AccordionGroup();
			accordionGroups.put(taxonomy.getId(),menuItem);
			HTMLPanel header = new HTMLPanel("");
			header.setStylePrimaryName(style.header_section());
			Label icon = new Label();
			icon.addStyleName(mainRes.style().plant_icon());
			icon.addStyleName(style.icon());
			InlineLabel l = new InlineLabel();
			l.setText(taxonomy.getGenus() +" " + taxonomy.getSpecies());
			//Label l = new Label();
			//l.setStylePrimaryName(style.header_section());
			header.add(icon);
			header.add(l);
			
			if (i == 0) {
				if (taxonomies.size() > 1)
					menuItem.addStyleName(style.accordion_group_top());
				menuItem.setDefaultOpen(true);
			}
			menuItem.addCustomTrigger(header);
			UnorderedList ul = new UnorderedList();
			subMenuItems.put(taxonomy.getId(), ul);
			ul.setStyleName(style.subitem_list());
			subItem = new InlineHyperlink();
			subItem.setStylePrimaryName(style.subitem());
			subItem.setText("Overview");
			subItem.setTargetHistoryToken(placeManager.buildHistoryToken(request));
			li = new ListItem(subItem);
			ul.add(li);
			
			subItem = new InlineHyperlink();
			subItem.setStylePrimaryName(style.subitem());
			subItem.setText("All Accessions");
			requestPassports.with("alleleAssayId", "0");
			subItem.setTargetHistoryToken(placeManager.buildHistoryToken(requestPassports));
			subItem.getElement().setAttribute("alleleAssayId", "0");
			li = new ListItem(subItem);
			ul.add(li);
			
			if (taxonomy.getAlleleAssays() != null) {
				for (AlleleAssayProxy alleleAssay:taxonomy.getAlleleAssays())  {
					subItem = new InlineHyperlink();
					subItem.setStylePrimaryName(style.subitem());
					subItem.setText(alleleAssay.getName());
					subItem.setTargetHistoryToken(placeManager.buildHistoryToken(requestPassports.with("alleleAssayId", alleleAssay.getId().toString())));
					subItem.getElement().setAttribute("alleleAssayId", alleleAssay.getId().toString());
					requestPassports.with("alleleAssayId", alleleAssay.getId().toString());
					ul.add(new ListItem(subItem));
				}
			}
			menuItem.add(ul);
			accordion.add(menuItem);
			i = i+1;
		}
		menuContainer.add(accordion);
		menuInitialized = true;
	}

	@Override
	public void setActiveMenuItem(Long selectedTaxonomyId,Long alleleAssayId) {
		for (Entry<Long,AccordionGroup> group: accordionGroups.entrySet()) {
			Widget header = group.getValue().getWidget(0);
			header.removeStyleName(style.header_section_active());
			if (selectedTaxonomyId != null) {
				if (selectedTaxonomyId == group.getKey()) {
					header.addStyleName(style.header_section_active());
				}
			}
		}
		
		for (Entry<Long,UnorderedList> listEntry: subMenuItems.entrySet()) {
			UnorderedList list = listEntry.getValue();
			for (int i =0;i<list.getWidgetCount();i++) {
				ListItem item = (ListItem)list.getWidget(i);
				item.removeStyleName(style.subitem_active());
				if (selectedTaxonomyId != null && selectedTaxonomyId == listEntry.getKey()) {
					if (alleleAssayId == null && i==0) {
						item.addStyleName(style.subitem_active());
					}
					else if (i> 0 && alleleAssayId != null && alleleAssayId == Long.parseLong(item.getWidget(0).getElement().getAttribute("alleleAssayId"))){
						item.addStyleName(style.subitem_active());
					}
				}
			}
		}
	}
}
