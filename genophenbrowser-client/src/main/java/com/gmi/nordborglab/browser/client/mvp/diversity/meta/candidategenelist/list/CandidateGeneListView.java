package com.gmi.nordborglab.browser.client.mvp.diversity.meta.candidategenelist.list;


import com.gmi.nordborglab.browser.client.editors.CandidateGeneListEditEditor;
import com.gmi.nordborglab.browser.client.mvp.widgets.facets.FacetSearchPresenterWidget;
import com.gmi.nordborglab.browser.client.place.NameTokens;
import com.gmi.nordborglab.browser.client.resources.CustomDataGridResources;
import com.gmi.nordborglab.browser.client.ui.CustomPager;
import com.gmi.nordborglab.browser.client.ui.cells.AccessColumn;
import com.gmi.nordborglab.browser.client.ui.cells.AvatarNameCell;
import com.gmi.nordborglab.browser.shared.proxy.AppUserProxy;
import com.gmi.nordborglab.browser.shared.proxy.CandidateGeneListProxy;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.NumberCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.IdentityColumn;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.HasData;
import com.google.inject.Inject;
import com.google.web.bindery.requestfactory.gwt.client.RequestFactoryEditorDriver;
import com.google.web.bindery.requestfactory.gwt.ui.client.EntityProxyKeyProvider;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.ModalBody;
import org.gwtbootstrap3.client.ui.ModalFooter;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.ModalBackdrop;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 20.09.13
 * Time: 15:40
 * To change this template use File | Settings | File Templates.
 */
public class CandidateGeneListView extends ViewWithUiHandlers<CanidateGeneListUiHandlers> implements CandidateGeneListPresenter.MyView {

    interface Binder extends UiBinder<Widget, CandidateGeneListView> {
    }

    public interface CandidateGeneListEditDriver extends RequestFactoryEditorDriver<CandidateGeneListProxy, CandidateGeneListEditEditor> {
    }

    private CandidateGeneListEditEditor candidateGeneListEditor = new CandidateGeneListEditEditor();

    private final Widget widget;

    @UiField
    CustomPager pager;

    @UiField(provided = true)
    DataGrid<CandidateGeneListProxy> dataGrid;
    @UiField
    Button newCandidateGeneListBtn;
    @UiField
    SimplePanel facetContainer;


    private final PlaceManager placeManager;
    private final CandidateGeneListEditDriver candidateGeneListEditDriver;
    private final AvatarNameCell avatarNameCell;
    private Modal editPopup = new Modal();

    public static class TitleCell extends AbstractCell<CandidateGeneListProxy> {

        interface Template extends SafeHtmlTemplates {

            @SafeHtmlTemplates.Template("<div style=\"font-size:110%;\"><a href=\"{0}\">{1}</a></div><div style=\"font-size:90%;color:#777;\">{2}</div>")
            SafeHtml cell(SafeUri link, SafeHtml name, SafeHtml subTitle);

        }

        private static Template templates = GWT.create(Template.class);

        private final PlaceManager placeManager;
        private PlaceRequest.Builder placeRequest;

        public TitleCell(PlaceRequest.Builder placeRequest, PlaceManager placeManager) {
            super();
            this.placeManager = placeManager;
            this.placeRequest = placeRequest;
        }

        @Override
        public void render(Context context, CandidateGeneListProxy value, SafeHtmlBuilder sb) {
            if (value == null)
                return;
            placeRequest.with("id", value.getId().toString());
            SafeUri link = UriUtils.fromTrustedString("#" + placeManager.buildHistoryToken(placeRequest.build()));
            SafeHtml name = SafeHtmlUtils.fromString(value.getName());
            SafeHtmlBuilder builder = new SafeHtmlBuilder();
            builder
                    .append(SafeHtmlUtils.fromSafeConstant("created on "))
                    .append(SafeHtmlUtils.fromString(DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_SHORT).format(value.getCreated())))
                    .append(SafeHtmlUtils.fromSafeConstant(" by "))
                    .append(SafeHtmlUtils.fromString(value.getOwnerUser().getFirstname() + " " + value.getOwnerUser().getLastname()));
            sb.append(templates.cell(link, name, builder.toSafeHtml()));
        }
    }

    @Inject
    public CandidateGeneListView(Binder binder, final CustomDataGridResources dataGridResources,
                                 final PlaceManager placeManager,
                                 final CandidateGeneListEditDriver candidateGeneListEditDriver,
                                 final AvatarNameCell avatarNameCell) {
        this.placeManager = placeManager;
        this.avatarNameCell = avatarNameCell;
        dataGrid = new DataGrid<>(50, dataGridResources, new EntityProxyKeyProvider<CandidateGeneListProxy>());
        initCellTable();
        widget = binder.createAndBindUi(this);
        bindSlot(FacetSearchPresenterWidget.SLOT_CONTENT, facetContainer);
        this.candidateGeneListEditDriver = candidateGeneListEditDriver;
        this.candidateGeneListEditDriver.initialize(candidateGeneListEditor);
        pager.setDisplay(dataGrid);
        editPopup.setDataBackdrop(ModalBackdrop.STATIC);
        editPopup.setClosable(true);
        editPopup.setFade(true);
        editPopup.setTitle("Create Candidate Gene list");
        Button cancelEditBtn = new Button("Cancel", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                getUiHandlers().onCancel();
            }
        });
        cancelEditBtn.setType(ButtonType.DEFAULT);
        Button saveEditBtn = new Button("Save", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                getUiHandlers().onSave();
            }
        });
        saveEditBtn.setType(ButtonType.PRIMARY);
        ModalFooter footer = new ModalFooter();
        footer.add(cancelEditBtn);
        footer.add(saveEditBtn);
        ModalBody modalBody = new ModalBody();
        modalBody.add(candidateGeneListEditor);
        editPopup.add(modalBody);
        editPopup.add(footer);
    }

    private void initCellTable() {
        final PlaceRequest.Builder request = new PlaceRequest.Builder().nameToken(NameTokens.candidateGeneListDetail);
        dataGrid.addColumn(new IdentityColumn<CandidateGeneListProxy>(new TitleCell(request, placeManager)) {

        }, "Name");

        dataGrid.addColumn(new Column<CandidateGeneListProxy, String>(new TextCell()) {
            @Override
            public String getValue(CandidateGeneListProxy object) {
                String retval = object.getDescription();
                return retval;
            }
        }, "Description");
        dataGrid.addColumn(new Column<CandidateGeneListProxy, Number>(new NumberCell()) {
            @Override
            public Number getValue(CandidateGeneListProxy object) {
                return object.getGeneCount();
            }
        }, "# Genes");
        dataGrid.addColumn(new Column<CandidateGeneListProxy, AppUserProxy>(avatarNameCell) {
            @Override
            public AppUserProxy getValue(CandidateGeneListProxy object) {
                return object.getOwnerUser();
            }
        }, "Owner");
        dataGrid.addColumn(new AccessColumn(), "Access");
        dataGrid.setColumnWidth(3, 250, Style.Unit.PX);
        dataGrid.setColumnWidth(4, 150, Style.Unit.PX);
    }



    @Override
    public Widget asWidget() {
        return widget;
    }

    @Override
    public HasData<CandidateGeneListProxy> getDisplay() {
        return dataGrid;
    }

    @Override
    public CandidateGeneListEditDriver getCandidateGeneListEditDriver() {
        return candidateGeneListEditDriver;
    }

    @UiHandler("newCandidateGeneListBtn")
    public void onCreateCandidateGeneList(ClickEvent e) {
        getUiHandlers().onCreate();
    }

    @Override
    public void showEditPopup(boolean show) {
        if (show)
            editPopup.show();
        else
            editPopup.hide();
    }

    @Override
    public void showCreateBtn(boolean show) {
        newCandidateGeneListBtn.setVisible(show);
    }

}