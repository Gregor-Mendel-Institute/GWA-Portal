package com.gmi.nordborglab.browser.client.ui;

import com.github.gwtbootstrap.client.ui.base.DropdownBase;
import com.github.gwtbootstrap.client.ui.base.IconAnchor;
import com.github.gwtbootstrap.client.ui.constants.BaseIconType;
import com.github.gwtbootstrap.client.ui.constants.IconPosition;
import com.github.gwtbootstrap.client.ui.constants.IconSize;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;

/**
 * Created by uemit.seren on 3/16/15.
 */
public class IconDropdown extends DropdownBase {

    private IconAnchor trigger;

    /**
     * Creates a DropdownButton without a caption.
     */
    public IconDropdown() {
        super("div");
        getElement().getStyle().setPosition(Style.Position.RELATIVE);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected IconAnchor createTrigger() {
        trigger = new IconAnchor();
        return trigger;
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


}

