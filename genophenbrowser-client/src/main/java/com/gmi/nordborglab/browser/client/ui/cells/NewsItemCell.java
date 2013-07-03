package com.gmi.nordborglab.browser.client.ui.cells;

import com.gmi.nordborglab.browser.client.CurrentUser;
import com.gmi.nordborglab.browser.client.NameTokens;
import com.gmi.nordborglab.browser.client.ParameterizedPlaceRequest;
import com.gmi.nordborglab.browser.shared.proxy.NewsItemProxy;
import com.gmi.nordborglab.browser.shared.proxy.ontology.Term2TermProxy;
import com.gmi.nordborglab.browser.shared.proxy.ontology.TermProxy;
import com.google.common.collect.ImmutableMap;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.shared.*;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiRenderer;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;

import java.util.Date;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 25.06.13
 * Time: 12:47
 * To change this template use File | Settings | File Templates.
 */
public class NewsItemCell extends AbstractCell<NewsItemProxy> {


    interface NewsCellUiBinder extends UiBinder<DivElement, NewsItemCell> {
    }

    public interface MyStyle extends CssResource {
        String unread();
    }

    public interface Renderer extends UiRenderer {
        MyStyle getStyle();

        void render(SafeHtmlBuilder sb, SafeUri link, SafeHtml icon, String iconClass, String title, String content, String date, String author);
    }

    private final Renderer uiRenderer;
    private final PlaceManager placeManager;
    private final CurrentUser currentUser;
    private static Map<String, String> type2Icon = ImmutableMap.<String, String>builder()
            .put("site", "&#8962;")
            .put("downtime", "&#128712;")
            .put("visualization", "&#59146;")
            .put("database", "&#128248;")
            .put("function", "&#128640;")
            .build();

    @Inject
    public NewsItemCell(final Renderer uiRenderer, PlaceManager placeManager, final CurrentUser currentUser) {
        super();
        this.uiRenderer = uiRenderer;
        this.placeManager = placeManager;
        this.currentUser = currentUser;
    }

    @Override
    public void render(Context context, NewsItemProxy value, SafeHtmlBuilder sb) {
        PlaceRequest request = new ParameterizedPlaceRequest(NameTokens.news).with("id", value.getId().toString());
        SafeUri link = UriUtils.fromSafeConstant("#" + placeManager.buildHistoryToken(request));
        String title = value.getTitle();
        String content = value.getContent();
        String date = value.getCreateDate().toString();
        String author = "";
        String iconClass = "";
        if (!value.isRead()) {
            iconClass = uiRenderer.getStyle().unread();
        }
        if (value.getAuthor() != null) {
            author = value.getAuthor().getFirstname() + " " + value.getAuthor().getLastname();
        }
        uiRenderer.render(sb, link, getIconFromType(value.getType()), iconClass, title, content, date, author);
    }


    private SafeHtml getIconFromType(String type) {

        String icon = null;
        if (type != null && !type.equalsIgnoreCase("")) {
            icon = type2Icon.get(type.toLowerCase());
        }
        if (icon == null) {
            icon = "&#8505;";
        }
        return SafeHtmlUtils.fromSafeConstant(icon);
    }
}