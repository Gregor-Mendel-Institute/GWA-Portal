package com.gmi.nordborglab.browser.client.ui.cells;

import com.gmi.nordborglab.browser.shared.proxy.AccessControlEntryProxy;
import com.google.gwt.cell.client.AbstractEditableCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.*;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiRenderer;
import com.google.gwt.user.client.Window;
import com.google.inject.Inject;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 3/1/13
 * Time: 10:20 AM
 * To change this template use File | Settings | File Templates.
 */

@SuppressWarnings("GwtUiHandlerErrors")
public class PermissionSelectionCell extends AbstractEditableCell<AccessControlEntryProxy,Integer>{


    public interface MyStyle extends CssResource {
        String hide();
        String access_container_opened();
        String not_selectable();

    }

    public interface Renderer extends UiRenderer {
        void render(SafeHtmlBuilder sb,String access,String iconClass,String accessClass,String editCheckBox,String viewCheckBox,String privateCheckBox,String publicCheckBox);
        MyStyle getStyle();
        DivElement getMenu(Element parent);
        UListElement getPermMenu(Element parent);
        UListElement getAccessMenu(Element parent);
        DivElement getPrivateItemCb(Element parent);
        DivElement getPublicItemCb(Element parent);
        DivElement getEditItemCb(Element parent);
        DivElement getViewItemCb(Element parent);
        void onBrowserEvent(PermissionSelectionCell permissionSelectionCell, NativeEvent event, Element parent, AccessControlEntryProxy value,ValueUpdater<AccessControlEntryProxy> valueUpdater);
    }
    private static String ICON_OK = "icon-ok";
    private final Renderer uiRenderer;
    private boolean menuOpened = false;

    @Inject
    public PermissionSelectionCell(final Renderer uiRenderer) {
        super("click");
        this.uiRenderer = uiRenderer;
    }


    @Override
    public void onBrowserEvent(Context context, Element parent, AccessControlEntryProxy value, NativeEvent event, ValueUpdater<AccessControlEntryProxy> valueUpdater) {
        uiRenderer.onBrowserEvent(this,event,parent,value,valueUpdater);
    }

    @Override
    public boolean isEditing(Context context, Element parent, AccessControlEntryProxy value) {
        return menuOpened;
    }

    @Override
    public void render(Context context, AccessControlEntryProxy value, SafeHtmlBuilder sb) {
        if (value == null)
            return;
        String access = "";
        String iconClass = "";
        String accessClass = "";
        String editCheckBox = "";
        String viewCheckBox="";
        String privateCheckBox= "";
        String publicCheckBox= "";
        if (value.getPrincipal().getIsOwner()) {
            access = "Is owner";
            iconClass = uiRenderer.getStyle().hide();
            accessClass = uiRenderer.getStyle().not_selectable();
        }
        else if (!value.getPrincipal().getIsUser()) {
            if (value.getMask() == 0) {
                access = "Private";
                privateCheckBox = ICON_OK;
            }
            else {
                access = "Public";
                publicCheckBox = ICON_OK;
            }
        }
        else {
            if (canEdit(value.getMask())) {
                access = "Can edit";
                editCheckBox = ICON_OK;
            }
            else {
                access = "Can view";
                viewCheckBox = ICON_OK;
            }
        }
        uiRenderer.render(sb,access,iconClass,accessClass,editCheckBox,viewCheckBox,privateCheckBox,publicCheckBox);
    }

    private boolean canEdit(int mask) {
        return ((mask & AccessControlEntryProxy.EDIT) == AccessControlEntryProxy.EDIT);
    }

    @UiHandler({"menu"})
    public void onClickMenu(ClickEvent e,Element parent,AccessControlEntryProxy value,ValueUpdater<AccessControlEntryProxy> valueUpdater) {
        if (value.getPrincipal().getIsOwner())
            return;
        UListElement permMenu  = uiRenderer.getPermMenu(parent.getParentElement());
        UListElement accessMenu = uiRenderer.getAccessMenu(parent.getParentElement());
        accessMenu.getStyle().setDisplay(Style.Display.NONE);
        permMenu.getStyle().setDisplay(Style.Display.NONE);
        if (value.getPrincipal().getIsUser()) {
            if (!menuOpened)
                permMenu.getStyle().setDisplay(Style.Display.BLOCK);
            else {
                permMenu.getStyle().setDisplay(Style.Display.NONE);
            }
        }
        else {
            if (!menuOpened)
                accessMenu.getStyle().setDisplay(Style.Display.BLOCK);
            else
                accessMenu.getStyle().setDisplay(Style.Display.NONE);
        }
        DivElement menuItem = uiRenderer.getMenu(parent);
        if (!menuOpened)
            menuItem.addClassName(uiRenderer.getStyle().access_container_opened());
        else
            menuItem.removeClassName(uiRenderer.getStyle().access_container_opened());
        menuOpened = !menuOpened;
    }


    @UiHandler({"privateItem"})
    public void onClickPrivateItem(ClickEvent e,Element parent, AccessControlEntryProxy value,ValueUpdater<AccessControlEntryProxy> valueUpdater) {
        if (value.getPrincipal().getIsUser())
            return;
        value.setMask(0);
        doUpdate(value, parent, valueUpdater);
    }

    @UiHandler({"publicItem"})
    public void onClickPublicItem(ClickEvent e,Element parent, AccessControlEntryProxy value,ValueUpdater<AccessControlEntryProxy> valueUpdater) {
        if (value.getPrincipal().getIsUser())
            return;
        value.setMask(value.getMask() | AccessControlEntryProxy.READ);
        doUpdate(value, parent, valueUpdater);
    }

    @UiHandler({"editItem"})
    public void onClickEditItem(ClickEvent e,Element parent, AccessControlEntryProxy value,ValueUpdater<AccessControlEntryProxy> valueUpdater) {
        if (!value.getPrincipal().getIsUser() || value.getPrincipal().getIsOwner())
            return;
        value.setMask(AccessControlEntryProxy.EDIT | AccessControlEntryProxy.READ);
        doUpdate(value, parent, valueUpdater);
    }

    @UiHandler({"viewItem"})
    public void onClickViewItem(ClickEvent e,Element parent, AccessControlEntryProxy value,ValueUpdater<AccessControlEntryProxy> valueUpdater) {
        if (!value.getPrincipal().getIsUser() || value.getPrincipal().getIsOwner())
            return;
        value.setMask(AccessControlEntryProxy.READ);
        doUpdate(value, parent, valueUpdater);
    }

    private void doUpdate(AccessControlEntryProxy value, Element parent, ValueUpdater<AccessControlEntryProxy> valueUpdater) {
        DivElement menuItem = uiRenderer.getMenu(parent);
        if (value.getPrincipal().getIsUser()) {
           DivElement editItem = uiRenderer.getEditItemCb(parent);
           DivElement viewItem = uiRenderer.getViewItemCb(parent);
           editItem.removeClassName(ICON_OK);
           viewItem.removeClassName(ICON_OK);
           if (canEdit(value.getMask())) {
               editItem.addClassName(ICON_OK);
               menuItem.getFirstChildElement().setInnerText("Can edit");
           }
           else {
               viewItem.addClassName(ICON_OK);
               menuItem.getFirstChildElement().setInnerText("Can view");
           }
        }
        else {
            DivElement publicItem = uiRenderer.getPublicItemCb(parent);
            DivElement privateItem = uiRenderer.getPrivateItemCb(parent);
            publicItem.removeClassName(ICON_OK);
            privateItem.removeClassName(ICON_OK);
            if (value.getMask() == 0) {
                privateItem.addClassName(ICON_OK);
                menuItem.getFirstChildElement().setInnerText("Private");
            }
            else {
                publicItem.addClassName(ICON_OK);
                menuItem.getFirstChildElement().setInnerText("Public");
            }
        }
        menuItem.removeClassName(uiRenderer.getStyle().access_container_opened());
        UListElement permMenu  = uiRenderer.getPermMenu(parent);
        UListElement accessMenu = uiRenderer.getAccessMenu(parent);
        permMenu.getStyle().setDisplay(Style.Display.NONE);
        accessMenu.getStyle().setDisplay(Style.Display.NONE);
        menuOpened = false;
        if (valueUpdater != null)
            valueUpdater.update(value);
    }
}