package com.gmi.nordborglab.browser.client.ui;

import com.github.gwtbootstrap.client.ui.Label;
import com.github.gwtbootstrap.client.ui.constants.LabelType;
import com.google.gwt.editor.client.LeafValueEditor;
import com.google.gwt.user.client.ui.Composite;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 30.07.13
 * Time: 13:36
 * To change this template use File | Settings | File Templates.
 */
public class AccessLabel extends Composite implements LeafValueEditor<Boolean> {
    private final Label label = new Label();
    private boolean value;

    public AccessLabel() {
        initWidget(label);
    }

    @Override
    public void setValue(Boolean value) {
        label.setType((value ? LabelType.SUCCESS : LabelType.WARNING));
        label.setText((value ? "PUBLIC" : "RESTRICTED"));
        this.value = value;
    }

    @Override
    public Boolean getValue() {
        return value;
    }

}
