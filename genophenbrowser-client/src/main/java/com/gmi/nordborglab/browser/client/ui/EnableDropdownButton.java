package com.gmi.nordborglab.browser.client.ui;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.base.DropdownBase;
import com.github.gwtbootstrap.client.ui.base.IconAnchor;
import com.github.gwtbootstrap.client.ui.constants.BaseIconType;
import com.github.gwtbootstrap.client.ui.constants.ButtonType;
import com.github.gwtbootstrap.client.ui.constants.IconPosition;
import com.github.gwtbootstrap.client.ui.constants.IconSize;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.github.gwtbootstrap.client.ui.resources.ButtonSize;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 22.10.13
 * Time: 18:15
 * To change this template use File | Settings | File Templates.
 */
public class EnableDropdownButton extends DropdownBase {

    private Button trigger;

    /**
     * Creates a DropdownButton without a caption.
     */
    public EnableDropdownButton() {
        super("div");
        addStyleName("btn-group");
    }

    /**
     * Creates a DropdownButton with the given caption.
     *
     * @param caption the button's caption
     */
    public EnableDropdownButton(String caption) {
        this();
        setText(caption);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected IconAnchor createTrigger() {
        trigger = new Button();
        trigger.setCaret(true);
        return trigger;
    }

    /**
     * Sets the button's size.
     *
     * @param size the button's size
     */
    public void setSize(ButtonSize size) {
        trigger.setSize(size);
    }

    /**
     * Sets the button's type.
     *
     * @param type the button's type
     */
    public void setType(ButtonType type) {
        trigger.setType(type);
    }

    /**
     * Sets the button's icon.
     *
     * @param type the icon's type
     */
    @Override
    public void setIcon(IconType type) {
        setBaseIcon(type);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setBaseIcon(BaseIconType type) {
        trigger.setBaseIcon(type);
    }

    @Override
    public HandlerRegistration addClickHandler(ClickHandler handler) {
        return trigger.addClickHandler(handler);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setIconSize(IconSize size) {
        trigger.setIconSize(size);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCustomIconStyle(String customIconStyle) {
        trigger.setCustomIconStyle(customIconStyle);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setIconPosition(IconPosition position) {
        trigger.setIconPosition(position);
    }

    public void setEnable(boolean enable) {
        trigger.setEnabled(enable);
        trigger.setCaret(enable);
    }
}

