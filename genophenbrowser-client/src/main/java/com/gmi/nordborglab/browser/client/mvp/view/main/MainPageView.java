package com.gmi.nordborglab.browser.client.mvp.view.main;

import com.gmi.nordborglab.browser.client.mvp.handlers.MainUiHandlers;
import com.gmi.nordborglab.browser.client.mvp.presenter.main.MainPagePresenter;
import com.gmi.nordborglab.browser.client.mvp.presenter.main.MainPagePresenter.MENU;
import com.gmi.nordborglab.browser.client.resources.MainResources;
import com.gmi.nordborglab.browser.client.security.CurrentUser;
import com.gmi.nordborglab.browser.client.ui.NotificationPopup;
import com.gmi.nordborglab.browser.client.ui.favicon.Favico;
import com.gmi.nordborglab.browser.client.ui.favicon.FavicoOptions;
import com.gmi.nordborglab.browser.client.util.DateUtils;
import com.gmi.nordborglab.browser.shared.proxy.AppUserProxy;
import com.gmi.nordborglab.browser.shared.proxy.UserNotificationProxy;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.i18n.client.Dictionary;
import com.google.gwt.query.client.Function;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.InlineHyperlink;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

import java.util.List;

import static com.google.gwt.query.client.GQuery.$;

public class MainPageView extends ViewWithUiHandlers<MainUiHandlers> implements MainPagePresenter.MyView {

    private final Widget widget;

    public interface Binder extends UiBinder<Widget, MainPageView> {
    }

    interface MyStyle extends CssResource {
        String current_page_item();

        String circle();

        String circle_red();

        String notification_icon();

        String unread_notification();
    }

    @UiField
    SimpleLayoutPanel container;
    @UiField
    Anchor userLink;
    @UiField
    HTMLPanel userInfoContainer;
    @UiField
    Label userName;
    @UiField
    Label userEmail;
    @UiField
    MyStyle style;
    @UiField
    InlineHyperlink homeLink;
    @UiField
    InlineHyperlink diversityLink;
    @UiField
    InlineHyperlink germplasmLink;
    @UiField
    InlineHyperlink genotypeLink;
    @UiField
    DivElement loadingIndicator;
    @UiField
    SpanElement loginTextLb;
    @UiField
    SpanElement arrorIcon;
    @UiField
    SpanElement notifyBubble;

    private final Favico favIco;

    @UiField
    DivElement footerContentPanel;
    @UiField
    HTMLPanel footerPanel;
    @UiField
    DockLayoutPanel mainContainer;
    @UiField
    ImageElement avatarImg;
    //@UiField FlowPanel userInfoContainer;
    protected final NotificationPopup notificationPopup = new NotificationPopup();
    private final PlaceManager placeManager;
    private final MainResources resources;
    private AppUserProxy user;

    private Function onHoverAccountHandler = new Function() {
        @Override
        public boolean f(com.google.gwt.user.client.Event e) {
            updateCheckNotificationDate();
            e.stopPropagation();
            return false;
        }
    };

    private Function onHoverEndAccountHandler = new Function() {
        @Override
        public boolean f(com.google.gwt.user.client.Event e) {
            getUiHandlers().onCloseAccountInfo();
            e.stopPropagation();
            return false;
        }
    };


    @Inject
    public MainPageView(final Binder binder, final MainResources resources, final PlaceManager placeManager) {
        widget = binder.createAndBindUi(this);
        this.resources = resources;
        loadingIndicator.getStyle().setDisplay(Display.NONE);
        this.placeManager = placeManager;
        FavicoOptions options = FavicoOptions.create();
        options.setAnimation(FavicoOptions.ANIMATION.SLIDE).setPosition(FavicoOptions.POSITION.UP);
        this.favIco = new Favico(options);
    }

    @Override
    public Widget asWidget() {
        return widget;
    }

    @Override
    public void setInSlot(Object slot, IsWidget content) {
        if (slot == MainPagePresenter.TYPE_SetMainContent) {
            setMainContent(content);
        } else {
            super.setInSlot(slot, content);
        }
    }

    private void setMainContent(IsWidget content) {
        container.setWidget(content);
    }

    @Override
    public String getUserData() {
        String user = null;
        try {
            Dictionary data = Dictionary.getDictionary("data");
            if (data != null) {
                user = data.get("user");
            }
        } catch (Exception e) {
        }
        return user;
    }

    @Override
    public void showUserInfo(AppUserProxy user) {
        this.user = user;
        $(userLink).unbind("mouseenter mouseleave");
        if (user == null) {
            userLink.setHref("");
            loginTextLb.setInnerText("Log In");
            //userLink.setText("Log In");
            arrorIcon.getStyle().setDisplay(Display.NONE);
            userInfoContainer.setVisible(false);
            avatarImg.setSrc("");

        } else {
            userLink.setHref("");
            avatarImg.setSrc(CurrentUser.getGravatarUrl(user, 40, true));
            //userLink.setHTML("My Account<span class=\""+resources.style().arrow_down()+"\" />");
            loginTextLb.setInnerText("My Account");
            arrorIcon.getStyle().setDisplay(Display.INLINE);
            userInfoContainer.setVisible(true);
            userEmail.setText(user.getEmail());
            userName.setText(user.getFirstname() + " " + user.getLastname());
            $(userLink).mouseenter(onHoverAccountHandler).mouseleave(onHoverEndAccountHandler);
        }
    }

    @Override
    public void setActiveNavigationItem(MENU menu) {
        String currentPageItemStyleName = style.current_page_item();
        homeLink.getElement().getParentElement().setClassName("");
        diversityLink.getElement().getParentElement().setClassName("");
        germplasmLink.getElement().getParentElement().setClassName("");
        genotypeLink.getElement().getParentElement().setClassName("");
        /*homeLink.removeStyleName(currentPageItemStyleName);
        diversityLink.removeStyleName(style.current_page_item());
		germplasmLink.removeStyleName(style.current_page_item());
		genotypeLink.removeStyleName(style.current_page_item());*/
        InlineHyperlink currentLink = null;
        boolean showFooter = false;
        switch (menu) {
            case DIVERSITY:
                currentLink = diversityLink;
                break;
            case GENOTYPE:
                currentLink = genotypeLink;
                break;
            case GERMPLASM:
                currentLink = germplasmLink;
                break;
            case HOME:
            default:
                showFooter = true;
                currentLink = homeLink;
        }
        showFooter(showFooter);
        if (currentLink != null)
            currentLink.getElement().getParentElement().addClassName(currentPageItemStyleName);
    }

    private void showFooter(boolean showFooter) {
        mainContainer.setWidgetSize(footerPanel, showFooter ? 4.423 : 0.5);
        footerContentPanel.getStyle().setDisplay(showFooter ? Display.BLOCK : Display.NONE);
    }

    @Override
    public void showNotification(String caption, String message, int level,
                                 int duration) {
        notificationPopup.setNotificatonContent(caption, message, level);
        notificationPopup.show();
        notificationPopup.center();
    }

    @Override
    public void showLoadingIndicator(boolean show, String text) {
        //text = text + "test";
        loadingIndicator.setInnerText(text);
        loadingIndicator.getStyle().setDisplay(show ? Display.BLOCK : Display.NONE);
    }

    @Override
    public void refreshNotifications(List<UserNotificationProxy> notifications, boolean isRead) {
        int newCount = 0;
        String notificationTable = "<tr class=\"success\" style=\"text-align:center;\"><td>No notifications!</td></tr>";
        clearNotificationTable();
        if (notifications != null && notifications.size() > 0) {
            StringBuilder builder = new StringBuilder();
            for (UserNotificationProxy notification : notifications) {
                if (!notification.isRead() && !isRead) {
                    newCount += 1;
                }
                builder.append(getHTMLFromNotification(notification, isRead));
            }
            notificationTable = builder.toString();
        }
        $("#notificationTable > tbody").append(notificationTable);
        notifyBubble.removeClassName("wiggle");
        notifyBubble.getStyle().setDisplay(newCount > 0 ? Display.INLINE : Display.NONE);
        if (newCount > 0) {
            $(notifyBubble).animate("", 1, new Function() {
                public void f(Element e) {
                    e.addClassName("wiggle");
                }
            });
            favIco.badge(newCount);
        }
        notifyBubble.setInnerText(String.valueOf(newCount));
    }

    @Override
    public void resetNotificationBubble() {
        notifyBubble.setInnerText("0");
        notifyBubble.getStyle().setDisplay(Display.NONE);
        notifyBubble.removeClassName("wiggle");
        favIco.reset();
    }

    @UiHandler("userLink")
    public void onLogin(ClickEvent e) {
        if (user == null) {
            PlaceRequest request = placeManager.getCurrentPlaceRequest();
            Window.Location.assign("login?url=" + placeManager.buildHistoryToken(request));
        }
        e.preventDefault();
    }

    private void clearNotificationTable() {
        $("#notificationTable > tbody > tr").remove();
    }

    private String getHTMLFromNotification(UserNotificationProxy notification, boolean isRead) {
        if (!isRead)
            isRead = notification.isRead();
        StringBuilder builder = new StringBuilder("<tr class=\"" + (isRead ? "" : style.unread_notification()) + "\">");
        String icon = getNotificationIconFromType(notification.getType());
        builder.append("<td><div class=\"" + style.circle() + " " + (!isRead ? style.circle_red() : "") + "\"></div></td>");
        builder.append("<td class=\"" + style.notification_icon() + "\"><i class=\"" + icon + "\" /></td>");
        builder.append("<td>" + notification.getText() + "</td>");
        builder.append("<td style=\"font-size:10px;white-space: nowrap;font-style:italic;\">" + DateUtils.formatTimeElapsedSinceMillisecond(System.currentTimeMillis() - notification.getCreateDate().getTime(), 1) + " ago</td>");
        builder.append("</tr>");
        return builder.toString();
    }

    private String getNotificationIconFromType(String type) {
        String icon = "e_icon-info";
        if (type.equalsIgnoreCase("gwasjob")) {
            icon = "e_icon-database";
        } else if (type.equalsIgnoreCase("permission")) {
            icon = "e_icon-share";
        }
        return icon;
    }

    private void updateCheckNotificationDate() {
        getUiHandlers().onOpenAccountInfo();
    }
}
