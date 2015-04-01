package com.gmi.nordborglab.browser.client.editors;

import com.gmi.nordborglab.browser.shared.proxy.AppUserProxy;
import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import org.gwtbootstrap3.client.ui.FormControlStatic;
import org.gwtbootstrap3.client.ui.TextBox;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 31.10.13
 * Time: 14:44
 * To change this template use File | Settings | File Templates.
 */
public class UserEditEditor extends Composite implements Editor<AppUserProxy> {

    private static UserEditEditorUiBinder uiBinder = GWT
            .create(UserEditEditorUiBinder.class);

    interface UserEditEditorUiBinder extends
            UiBinder<Widget, UserEditEditor> {

    }

    public interface ModifyCallback {
        public void onChanged();
    }

    private ModifyCallback callback;

    @UiField
    TextBox firstname;

    @UiField
    TextBox lastname;
    @UiField
    TextBox newPassword;
    @UiField
    TextBox newPasswordConfirm;
    @UiField
    FormControlStatic email;

    public UserEditEditor() {
        initWidget(uiBinder.createAndBindUi(this));
        newPassword.getElement().setAttribute("type", "password");
        newPassword.getElement().setAttribute("type", "password");
    }


    public void setModifyCallback(ModifyCallback callback) {
        this.callback = callback;
    }

    @UiHandler({"firstname", "lastname", "newPassword", "newPasswordConfirm"})
    public void onModified(ValueChangeEvent<String> e) {
        if (callback != null) {
            callback.onChanged();
        }
    }
}
