package com.gmi.nordborglab.browser.client.editors;

import com.gmi.nordborglab.browser.shared.proxy.GWASResultProxy;
import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import org.gwtbootstrap3.client.ui.TextArea;
import org.gwtbootstrap3.client.ui.TextBox;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 2/25/13
 * Time: 9:38 PM
 * To change this template use File | Settings | File Templates.
 */
public class GWASResultEditEditor extends Composite implements Editor<GWASResultProxy> {

    interface GWASResultEditorUiBinder extends UiBinder<Widget, GWASResultEditEditor> {
    }

    private static GWASResultEditorUiBinder ourUiBinder = GWT.create(GWASResultEditorUiBinder.class);


    @UiField
    TextBox name;
    @UiField
    TextBox type;

    @UiField
    TextArea comments;


    public GWASResultEditEditor() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

}