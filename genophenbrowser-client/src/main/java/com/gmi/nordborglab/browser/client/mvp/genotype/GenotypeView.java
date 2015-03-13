package com.gmi.nordborglab.browser.client.mvp.genotype;

import com.google.common.collect.ImmutableMap;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewImpl;

/**
 * Created by uemit.seren on 3/3/15.
 */
public class GenotypeView extends ViewImpl implements
        GenotypePresenter.MyView {

    public interface Binder extends UiBinder<Widget, GenotypeView> {
    }

    public interface MyStyle extends CssResource {
        String header_section_active();
    }

    @UiField
    SimpleLayoutPanel searchContainer;
    @UiField
    SimpleLayoutPanel container;

    @UiField
    MyStyle style;
    @UiField
    Label titleLabel;
    @UiField
    FlowPanel breadcrumbs;
    @UiField
    Hyperlink genomeBrowserMenu;
    @UiField
    Hyperlink snpViewerMenu;

    private ImmutableMap<MENU_ITEM, Hyperlink> menuItems;

    public enum MENU_ITEM {GENOMEBROWSER, SNPVIEWER}

    @Inject
    public GenotypeView(Binder binder) {
        initWidget(binder.createAndBindUi(this));
        menuItems = ImmutableMap.<MENU_ITEM, Hyperlink>builder()
                .put(MENU_ITEM.GENOMEBROWSER, genomeBrowserMenu)
                .put(MENU_ITEM.SNPVIEWER, snpViewerMenu).build();
    }

    @Override
    public void setInSlot(Object slot, IsWidget content) {
        if (slot == GenotypePresenter.TYPE_SetMainContent) {
            setMainContent(content);
        } else if (slot == GenotypePresenter.TYPE_SearchPresenterContent) {
            if (content == null) {
                searchContainer.clear();
            } else {
                searchContainer.add(content);
            }
        } else {
            super.setInSlot(slot, content);
        }
    }

    private void setMainContent(IsWidget content) {
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
    public void setActiveMenuItem(MENU_ITEM menuItem) {
        for (Hyperlink panel : menuItems.values()) {
            panel.removeStyleName(style.header_section_active());
        }
        if (menuItem == null)
            return;
        if (menuItems.containsKey(menuItem)) {
            menuItems.get(menuItem).addStyleName(style.header_section_active());
        }
    }

}