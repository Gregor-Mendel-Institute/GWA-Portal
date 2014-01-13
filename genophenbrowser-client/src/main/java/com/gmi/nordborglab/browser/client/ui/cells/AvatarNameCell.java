package com.gmi.nordborglab.browser.client.ui.cells;

import com.gmi.nordborglab.browser.client.place.NameTokens;
import com.gmi.nordborglab.browser.client.security.CurrentUser;
import com.gmi.nordborglab.browser.shared.proxy.AppUserProxy;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiRenderer;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 07.11.13
 * Time: 15:52
 * To change this template use File | Settings | File Templates.
 */
public class AvatarNameCell extends AbstractCell<AppUserProxy> {

    interface AvatarCellUiBinder extends UiBinder<DivElement, AvatarNameCell> {
    }

    public interface Renderer extends UiRenderer {
        void render(SafeHtmlBuilder sb, String name, String type, SafeUri link, SafeUri avatarUrl);
    }

    private final Renderer uiRenderer;
    private final PlaceManager placeManager;
    private int size = 40;

    @Inject
    public AvatarNameCell(final Renderer uiRenderer, PlaceManager placeManager) {
        super();
        this.uiRenderer = uiRenderer;
        this.placeManager = placeManager;
    }

    @Override
    public void render(Context context, AppUserProxy value, SafeHtmlBuilder sb) {
        SafeUri link = UriUtils.fromSafeConstant("javascript:;");
        String name = "N/A";
        String type = "N/A";
        SafeUri avatarUrl = UriUtils.fromSafeConstant("javascript:;");
        if (value != null) {
            if (value.getId() != null) {
                PlaceRequest request = new PlaceRequest.Builder().nameToken(NameTokens.profile).with("id", value.getId().toString()).build();
                link = UriUtils.fromSafeConstant("#" + placeManager.buildHistoryToken(request));
            }
            name = value.getFirstname() + " " + value.getLastname();
            type = CurrentUser.isAdmin(value) ? "Admin" : "User";
            avatarUrl = UriUtils.fromSafeConstant(CurrentUser.getGravatarUrl(value, size, true));
        }
        uiRenderer.render(sb, name, type, link, avatarUrl);
    }

    public void setSize(int size) {
        this.size = size;
    }
}