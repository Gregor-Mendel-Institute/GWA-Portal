package com.gmi.nordborglab.browser.client.editors;

import com.gmi.nordborglab.browser.shared.proxy.PublicationProxy;
import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DateLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 5/3/13
 * Time: 2:33 PM
 * To change this template use File | Settings | File Templates.
 */
public class PublicationDisplayEditor extends Composite implements Editor<PublicationProxy> {

    private static PublicationDisplayEditorUiBinder uiBinder = GWT
            .create(PublicationDisplayEditorUiBinder.class);

    interface PublicationDisplayEditorUiBinder extends
            UiBinder<Widget, PublicationDisplayEditor> {
    }


    public PublicationDisplayEditor() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    @UiField
    Label title;
    @UiField
    Label firstAuthor;
    @UiField
    Label DOI;
    @UiField
    Label volume;
    @UiField
    Label issue;
    @UiField
    DateLabel pubDate;
    @UiField
    Label page;
    @UiField
    Label URL;
    @UiField
    Label journal;


}
