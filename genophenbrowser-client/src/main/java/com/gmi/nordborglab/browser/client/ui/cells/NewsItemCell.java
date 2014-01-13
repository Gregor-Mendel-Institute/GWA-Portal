package com.gmi.nordborglab.browser.client.ui.cells;

import com.gmi.nordborglab.browser.client.place.NameTokens;
import com.gmi.nordborglab.browser.client.security.CurrentUser;
import com.gmi.nordborglab.browser.shared.proxy.NewsItemProxy;
import com.google.common.collect.ImmutableMap;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiRenderer;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;

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

        void render(SafeHtmlBuilder sb, SafeUri link, String icon, String iconClass, String title, SafeHtml content, String date, String author);
    }

    private final Renderer uiRenderer;
    private final PlaceManager placeManager;
    private final CurrentUser currentUser;
    private static Map<String, String> type2Icon = ImmutableMap.<String, String>builder()
            .put("site", "e_icon-home")
            .put("downtime", "e_icon-traffic-cone")
            .put("visualization", "e_icon-eye")
            .put("database", "e_icon-database")
            .put("function", "e_icon-rocket")
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
        PlaceRequest request = new PlaceRequest.Builder().nameToken(NameTokens.news).with("id", value.getId().toString()).build();
        SafeUri link = UriUtils.fromSafeConstant("#" + placeManager.buildHistoryToken(request));
        String title = value.getTitle();
        SafeHtml content = SafeHtmlUtils.fromTrustedString(value.getContent());
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


    private String getIconFromType(String type) {

        String icon = null;
        if (type != null && !type.equalsIgnoreCase("")) {
            icon = type2Icon.get(type.toLowerCase());
        }
        if (icon == null) {
            icon = "e_icon-info;";
        }
        return icon;
    }
}