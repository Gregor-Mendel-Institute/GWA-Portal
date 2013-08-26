package com.gmi.nordborglab.browser.client.ui;

import com.github.gwtbootstrap.client.ui.Typeahead;
import com.github.gwtbootstrap.client.ui.base.TextBoxBase;
import com.gmi.nordborglab.browser.shared.proxy.ontology.TermProxy;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.EditorDelegate;
import com.google.gwt.editor.client.LeafValueEditor;
import com.google.gwt.editor.client.ValueAwareEditor;
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

    public OntologyTypeahead(OntologyTermSuggestOracle oracle) {
        super(oracle);
        setMinLength(0);
        setUpdaterCallback(new UpdaterCallback() {
            @Override
            public String onSelection(SuggestOracle.Suggestion selectedSuggestion) {
                value = ((OntologyTermSuggestOracle.OntologySuggestion) selectedSuggestion).getTerm();
                return selectedSuggestion.getReplacementString();

            }
        });
        reconfigure();
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
