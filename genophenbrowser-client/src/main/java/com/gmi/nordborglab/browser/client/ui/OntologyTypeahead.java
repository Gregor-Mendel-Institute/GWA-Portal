package com.gmi.nordborglab.browser.client.ui;

import com.github.gwtbootstrap.client.ui.Typeahead;
import com.github.gwtbootstrap.client.ui.base.TextBoxBase;
import com.gmi.nordborglab.browser.shared.proxy.ontology.TermProxy;
import com.google.gwt.editor.client.LeafValueEditor;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.user.client.ui.SuggestOracle;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 31.07.13
 * Time: 18:48
 * To change this template use File | Settings | File Templates.
 */
public class OntologyTypeahead extends Typeahead implements LeafValueEditor<TermProxy> {

    private TermProxy value;
    private ChangeHandler changeHandler;

    public OntologyTypeahead(OntologyTermSuggestOracle oracle) {
        super(oracle);
        setMinLength(0);
        setUpdaterCallback(new UpdaterCallback() {
            @Override
            public String onSelection(SuggestOracle.Suggestion selectedSuggestion) {
                value = ((OntologyTermSuggestOracle.OntologySuggestion) selectedSuggestion).getTerm();
                if (changeHandler != null) {
                    changeHandler.onChange(null);
                }
                return selectedSuggestion.getReplacementString();

            }
        });
        reconfigure();
    }

    public void setChangeHandler(ChangeHandler changeHandler) {
        this.changeHandler = changeHandler;
    }

    @Override
    public void setValue(TermProxy value) {
        this.value = value;
        if (getWidget() == null)
            return;
        if (value != null) {
            ((TextBoxBase) getWidget()).setValue(value.getName() + " (" + value.getAcc() + ")");
        } else {
            ((TextBoxBase) getWidget()).setValue(null);
        }
    }

    @Override
    public TermProxy getValue() {
        return value;
    }


}
