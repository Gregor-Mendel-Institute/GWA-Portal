package com.gmi.nordborglab.browser.client.ui;

import com.github.gwtbootstrap.client.ui.ValueListBox;
import com.github.gwtbootstrap.client.ui.base.StyleHelper;
import com.github.gwtbootstrap.client.ui.constants.ControlGroupType;
import com.google.gwt.dom.client.Element;
import com.google.gwt.editor.client.EditorError;
import com.google.gwt.editor.client.HasEditorErrors;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ProvidesKey;

import java.util.List;

/**
 * Created by uemit.seren on 1/24/15.
 */
public class ValidationValueListBox<T> extends ValueListBox<T> implements HasEditorErrors<T> {

    private Widget errorLabel;// could be a HelpInline widget
    private Widget controlGroup;// could be a ControlGroup widget

    public ValidationValueListBox(Renderer<T> renderer) {
        super(renderer);
    }

    public ValidationValueListBox(Renderer<T> renderer, ProvidesKey<T> keyProvider) {
        super(renderer, keyProvider);
    }

    @Override
    public void showErrors(List<EditorError> errors) {
        Widget decoratedWidget = controlGroup != null ? controlGroup : this;
        if (errors != null && !errors.isEmpty()) {
            StyleHelper.addStyle(decoratedWidget, ControlGroupType.ERROR);
            SafeHtmlBuilder sb = new SafeHtmlBuilder();
            for (EditorError error : errors) {
                if (error.getEditor() == this) {
                    error.setConsumed(true);
                    sb.appendEscaped(error.getMessage());
                    sb.appendHtmlConstant("<br />");
                }
            }
            setErrorLabelText(sb.toSafeHtml().asString());
        } else {
            StyleHelper.removeStyle(decoratedWidget, ControlGroupType.ERROR);
            setErrorLabelText("");
        }
    }

    /**
     * The widget that will be decorated on <code>EditorError</code>s will be added de <code>ControlGroupType.ERROR</code> style.
     * It can be a ControlGroup or any widget.
     *
     * @param controlGroup
     */
    public void setControlGroup(Widget controlGroup) {
        this.controlGroup = controlGroup;
    }

    /**
     * Widget where <code>EditorError</code>s messages will be placed.
     * It can be a HelpBlock or any other widget.
     *
     * @param errorLabel
     */
    public void setErrorLabel(Widget errorLabel) {
        this.errorLabel = errorLabel;
    }

    /**
     * Sets the content of the <code>EditorError</code>s messages inside de <code>errorLabel</code>.
     * This implementation uses {@link Element#setInnerHTML(String)} to set the content.
     *
     * @param errorMessage
     */
    protected void setErrorLabelText(String errorMessage) {
        if (errorLabel != null) {
            errorLabel.getElement().setInnerHTML(errorMessage);
        }
    }


}
