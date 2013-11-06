package com.gmi.nordborglab.browser.client.ui.cells;

import com.gmi.nordborglab.browser.client.CurrentUser;
import com.gmi.nordborglab.browser.client.NameTokens;
import com.gmi.nordborglab.browser.client.ParameterizedPlaceRequest;
import com.gmi.nordborglab.browser.shared.proxy.AppUserProxy;
import com.gmi.nordborglab.browser.shared.proxy.SecureEntityProxy;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.user.cellview.client.Column;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 06.11.13
 * Time: 14:21
 * To change this template use File | Settings | File Templates.
 */
public class OwnerLinkColumn<T extends SecureEntityProxy> extends HyperlinkPlaceManagerColumn<SecureEntityProxy> {

    public OwnerLinkColumn(PlaceManager placeManager) {
        super(new HyperlinkCell(), placeManager);
    }

    @Override
    public HyperlinkPlaceManagerColumn.HyperlinkParam getValue(SecureEntityProxy object) {
        String name = "";
        String link = null;
        PlaceRequest request = new ParameterizedPlaceRequest(NameTokens.profile);
        if (checkNormalUser(object.getOwnerUser())) {
            request.with("id", object.getOwnerUser().getId().toString());
        } else {
            request = null;
        }
        if (object.isOwner()) {
            name = "me";
        } else if (object.getOwnerUser() != null) {
            name = object.getOwnerUser().getFirstname() + " " + object.getOwnerUser().getLastname();
        } else {
            name = "N/A";
        }

        if (request != null) {
            link = "#" + placeManager.buildHistoryToken(request.with("id", object.getOwnerUser().getId().toString()));
        }
        HyperlinkParam param = new HyperlinkParam(name, link);
        return param;
    }

    private boolean checkNormalUser(AppUserProxy user) {
        if (user != null && user.getId() != null) {
            return true;
        }
        return false;
    }
}
