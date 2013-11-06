package com.gmi.nordborglab.browser.client.mvp.view.main;

import com.gmi.nordborglab.browser.client.editors.UserEditEditor;
import com.gmi.nordborglab.browser.client.mvp.handlers.AccountUiHandlers;
import com.gmi.nordborglab.browser.client.mvp.presenter.main.AccountPresenter;
import com.google.gwt.dom.client.*;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;
import com.google.web.bindery.requestfactory.gwt.client.RequestFactoryEditorDriver;
import com.gwtplatform.mvp.client.ViewImpl;
import com.gmi.nordborglab.browser.shared.proxy.AppUserProxy;
import com.google.gwt.i18n.client.Dictionary;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

public class AccountView extends ViewWithUiHandlers<AccountUiHandlers> implements AccountPresenter.MyView {

    private final Widget widget;

    public interface Binder extends UiBinder<Widget, AccountView> {
    }

    public interface UserEditDriver extends RequestFactoryEditorDriver<AppUserProxy, UserEditEditor> {
    }


    @UiField
    UserEditEditor userEditEditor;
    @UiField
    ImageElement avatarImg;
    @UiField
    ImageElement identiconImg;
    @UiField
    ImageElement gravatarImg;
    @UiField
    Element identiconImgCheck;
    @UiField
    Element gravatarImgCheck;
    @UiField
    FocusPanel gravatarPanel;
    @UiField
    FocusPanel identiconPanel;
    @UiField
    AnchorElement gravatarLink;

    private final UserEditDriver userEditDriver;

    @Inject
    public AccountView(final Binder binder, final UserEditDriver userEditDriver) {
        widget = binder.createAndBindUi(this);
        this.userEditDriver = userEditDriver;
        userEditDriver.initialize(userEditEditor);
        userEditEditor.setModifyCallback(new UserEditEditor.ModifyCallback() {
            @Override
            public void onChanged() {
                getUiHandlers().onValidate();
            }
        });
    }

    @Override
    public Widget asWidget() {
        return widget;
    }

    @Override
    public UserEditDriver getUserEditDriver() {
        return userEditDriver;
    }

    @UiHandler("saveBtn")
    public void onClickSaveBtn(ClickEvent e) {
        getUiHandlers().onSave();
    }

    @UiHandler("cancelBtn")
    public void onClickCancelBtn(ClickEvent e) {
        getUiHandlers().onCancel();
    }

    @Override
    public void setAvatarUrl(String url) {
        avatarImg.setSrc(url);
    }

    @Override
    public void setGravatarImgUrl(String url) {
        gravatarImg.setSrc(url);
    }

    @Override
    public void setIdenticonUrl(String url) {
        identiconImg.setSrc(url);
    }

    @Override
    public void setActiveAvatarSource(AppUserProxy.AVATAR_SOURCE source) {
        identiconImgCheck.getStyle().setVisibility(Style.Visibility.HIDDEN);
        gravatarImgCheck.getStyle().setVisibility(Style.Visibility.HIDDEN);
        switch (source) {
            case GRAVATAR:
                gravatarImgCheck.getStyle().setVisibility(Style.Visibility.VISIBLE);
                break;
            case IDENTICON:
                identiconImgCheck.getStyle().setVisibility(Style.Visibility.VISIBLE);
                break;
        }
    }

    @UiHandler("gravatarPanel")
    public void onClickGravatarPanel(ClickEvent e) {
        Element targetElem = Element.as(e.getNativeEvent().getEventTarget());
        if (targetElem != gravatarLink) {
            getUiHandlers().onSelectAvatarSource(AppUserProxy.AVATAR_SOURCE.GRAVATAR);
        }
    }

    @UiHandler("identiconPanel")
    public void onClickIdenticonPanel(ClickEvent e) {
        getUiHandlers().onSelectAvatarSource(AppUserProxy.AVATAR_SOURCE.IDENTICON);
    }

}
