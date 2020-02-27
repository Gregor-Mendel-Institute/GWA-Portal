package com.gmi.nordborglab.browser.client.ui;

import com.gmi.nordborglab.browser.shared.proxy.ontology.TermProxy;
import com.google.gwt.editor.client.LeafValueEditor;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.Composite;
import org.gwtbootstrap3.extras.typeahead.client.base.Dataset;
import org.gwtbootstrap3.extras.typeahead.client.events.TypeaheadSelectedEvent;
import org.gwtbootstrap3.extras.typeahead.client.events.TypeaheadSelectedHandler;
import org.gwtbootstrap3.extras.typeahead.client.ui.Typeahead;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 31.07.13
 * Time: 18:48
 * To change this template use File | Settings | File Templates.
 */
public class OntologyTypeahead extends Composite implements LeafValueEditor<TermProxy> {

    private TermProxy value;
    private ChangeHandler changeHandler;
    private final Typeahead<TermProxy> typeahead;

    public OntologyTypeahead(Dataset<TermProxy> dataset) {
        super();
        typeahead = new Typeahead<>(dataset);
        typeahead.setPlaceholder("Type to search..");
        initWidget(typeahead);
        typeahead.setMinLength(0);
        typeahead.addTypeaheadSelectedHandler(new TypeaheadSelectedHandler<TermProxy>() {
            @Override
            public void onSelected(TypeaheadSelectedEvent<TermProxy> typeaheadSelectedEvent) {
                value = typeaheadSelectedEvent.getSuggestion().getData();
                if (changeHandler != null) {
                    changeHandler.onChange(null);
                }
            }
        });
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
            typeahead.setValue(value.getName() + " (" + value.getAcc() + ")");
        } else {
            typeahead.setValue(null);
        }
    }

    @Override
    public TermProxy getValue() {
        return value;
    }


}
