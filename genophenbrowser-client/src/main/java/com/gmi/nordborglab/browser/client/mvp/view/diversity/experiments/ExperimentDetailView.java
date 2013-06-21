package com.gmi.nordborglab.browser.client.mvp.view.diversity.experiments;

import com.github.gwtbootstrap.client.ui.Form;
import com.github.gwtbootstrap.client.ui.Modal;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.constants.BackdropType;
import com.gmi.nordborglab.browser.client.editors.ExperimentDisplayEditor;
import com.gmi.nordborglab.browser.client.editors.ExperimentEditEditor;
import com.gmi.nordborglab.browser.client.mvp.handlers.ExperimentDetailUiHandlers;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.experiments.ExperimentDetailPresenter;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.experiments.ExperimentDetailPresenter.State;
import com.gmi.nordborglab.browser.client.resources.CustomDataGridResources;
import com.gmi.nordborglab.browser.client.resources.MainResources;
import com.gmi.nordborglab.browser.client.ui.PhaseAnimation;
import com.gmi.nordborglab.browser.client.ui.cells.EntypoIconActionCell;
import com.gmi.nordborglab.browser.client.ui.cells.HyperlinkCell;
import com.gmi.nordborglab.browser.client.ui.cells.HyperlinkPlaceManagerColumn;
import com.gmi.nordborglab.browser.shared.proxy.AccessControlEntryProxy;
import com.gmi.nordborglab.browser.shared.proxy.ExperimentProxy;
import com.gmi.nordborglab.browser.shared.proxy.PublicationProxy;
import com.google.gwt.cell.client.ActionCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.IdentityColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ProvidesKey;
import com.google.inject.Inject;
import com.google.web.bindery.requestfactory.gwt.client.RequestFactoryEditorDriver;
import com.google.web.bindery.requestfactory.gwt.ui.client.EntityProxyKeyProvider;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

public class ExperimentDetailView extends ViewWithUiHandlers<ExperimentDetailUiHandlers> implements
        ExperimentDetailPresenter.MyView {

    private final Widget widget;

    public interface Binder extends UiBinder<Widget, ExperimentDetailView> {
    }

    public interface ExperimentEditDriver extends RequestFactoryEditorDriver<ExperimentProxy, ExperimentEditEditor> {
    }

    public interface ExperimentDisplayDriver extends RequestFactoryEditorDriver<ExperimentProxy, ExperimentDisplayEditor> {
    }

    @UiField
    ExperimentEditEditor experimentEditEditor;
    @UiField
    ExperimentDisplayEditor experimentDisplayEditor;
    @UiField
    ToggleButton edit;
    @UiField
    ToggleButton save;
    @UiField
    Anchor cancel;
    @UiField
    Anchor delete;
    @UiField
    ToggleButton share;
    @UiField(provided = true)
    ResponsiveDataGrid publicationDataGrid;
    @UiField
    TextBox doiTb;
    @UiField
    Form addDOIForm;
    private final ExperimentEditDriver experimentEditDriver;
    private final ExperimentDisplayDriver experimentDisplayDriver;
    private State state = State.DISPLAYING;
    private final MainResources resources;
    private Modal permissionPopUp = new Modal(true);
    private boolean layoutScheduled = false;
    private final Scheduler.ScheduledCommand layoutCmd = new Scheduler.ScheduledCommand() {
        public void execute() {
            layoutScheduled = false;
            forceLayout();
        }
    };


    public static class ResponsiveDataGrid extends DataGrid<PublicationProxy> {

        private final Column<PublicationProxy, String> citationColumn;
        private final Column<PublicationProxy, String> authorColumn;
        private final Column<PublicationProxy, String> titleColumn;
        private final Column<PublicationProxy, String> yearColumn;
        private final Column<PublicationProxy, String> journalColumn;
        private final Column<PublicationProxy, HyperlinkPlaceManagerColumn.HyperlinkParam> doiColumn;
        private final IdentityColumn<PublicationProxy> actionColumn;
        private Boolean isCompact;
        private boolean showAction = true;


        public ResponsiveDataGrid(int pageSize, Resources resources, ActionCell.Delegate<PublicationProxy> actionDelegate) {
            super(pageSize, resources, new EntityProxyKeyProvider<PublicationProxy>());
            citationColumn = new Column<PublicationProxy, String>(new TextCell()) {
                @Override
                public String getValue(PublicationProxy object) {
                    StringBuilder builder = new StringBuilder();
                    builder.append(object.getFirstAuthor() + " (" + object.getPubDate().getYear() + "). ");
                    builder.append(object.getTitle() + ". ");
                    builder.append(object.getJournal() + ", ");
                    builder.append(object.getVolume());
                    builder.append("(" + object.getIssue() + "), ");
                    builder.append(object.getPage() + ". ");
                    builder.append("doi:" + object.getDOI());
                    return builder.toString();
                }
            };
            authorColumn = new Column<PublicationProxy, String>(new TextCell()) {
                @Override
                public String getValue(PublicationProxy object) {
                    return object.getFirstAuthor();
                }
            };
            titleColumn = new Column<PublicationProxy, String>(new TextCell()) {
                @Override
                public String getValue(PublicationProxy object) {
                    return object.getTitle();
                }
            };
            yearColumn = new Column<PublicationProxy, String>(new TextCell()) {
                @Override
                public String getValue(PublicationProxy object) {
                    return String.valueOf(object.getPubDate().getYear());
                }
            };
            journalColumn = new Column<PublicationProxy, String>(new TextCell()) {
                @Override
                public String getValue(PublicationProxy object) {
                    return object.getJournal();
                }
            };
            doiColumn = new Column<PublicationProxy, HyperlinkPlaceManagerColumn.HyperlinkParam>(new HyperlinkCell()) {
                @Override
                public HyperlinkPlaceManagerColumn.HyperlinkParam getValue(PublicationProxy object) {
                    return new HyperlinkPlaceManagerColumn.HyperlinkParam(object.getDOI(), object.getURL());
                }
            };
            actionColumn = new IdentityColumn<PublicationProxy>(new EntypoIconActionCell<PublicationProxy>("&#59177;", actionDelegate)) {

                @Override
                public PublicationProxy getValue(PublicationProxy object) {
                    return object;
                }
            };
            //initColumns();
            // updateColumns();
        }


        private void initColumns() {
            addColumn(citationColumn, "Citation");
            addColumn(authorColumn, "Author");
            addColumn(titleColumn, "Title");
            addColumn(yearColumn, "Year");
            addColumn(journalColumn, "Journal");
            addColumn(doiColumn, "DOI");
            addColumn(actionColumn);

        }

        private void updateColumns() {
            int columnCount = getColumnCount();
            for (int i = columnCount - 1; i >= 0; i--) {
                removeColumn(i);
            }
            removeUnusedColGroups();
            if (isCompact) {
                addColumn(citationColumn, "Citation");
                clearColumnWidth(0);
                if (showAction) {
                    addColumn(actionColumn);
                    setColumnWidth(1, "40px");
                }

            } else {
                addColumn(authorColumn, "Author");
                addColumn(titleColumn, "Title");
                addColumn(yearColumn, "Year");
                addColumn(journalColumn, "Journal");
                addColumn(doiColumn, "DOI");
                setColumnWidth(0, "100px");
                clearColumnWidth(1);
                setColumnWidth(2, "60px");
                setColumnWidth(3, "80px");
                clearColumnWidth(4);
                if (showAction) {
                    addColumn(actionColumn);
                    setColumnWidth(5, "40px");
                }
            }
        }

        @Override
        protected int getRealColumnCount() {
            return getColumnCount();
        }

        private void removeUnusedColGroups() {
            int columnCount = getColumnCount();
            NodeList<Element> colGroups = getElement().getElementsByTagName("colgroup");

            for (int i = 0; i < colGroups.getLength(); i++) {
                Element colGroupEle = colGroups.getItem(i);
                NodeList<Element> colList = colGroupEle.getElementsByTagName("col");

                for (int j = colList.getLength() - 1; j >= 0; j--) {
                    colGroupEle.removeChild(colList.getItem(j));
                }
            }
        }

        public void setShowAction(boolean showAction) {
            if (showAction != this.showAction) {
                this.showAction = showAction;
                updateColumns();
            }
        }


        @Override
        public void onResize() {
            super.onResize();
            if (!isAttached()) {
                return;
            }
            boolean isNewCompact = (getOffsetWidth() < 800);
            if (isCompact == null || isNewCompact != isCompact) {
                isCompact = isNewCompact;
                updateColumns();
            }
        }


    }

    @Inject
    public ExperimentDetailView(final Binder binder,
                                final ExperimentEditDriver experimentEditDriver,
                                final ExperimentDisplayDriver experimentDisplayDriver,
                                final MainResources resources,
                                final CustomDataGridResources customDataGridResources) {
        this.resources = resources;
        publicationDataGrid = new ResponsiveDataGrid(50, customDataGridResources, new ActionCell.Delegate<PublicationProxy>() {
            @Override
            public void execute(PublicationProxy object) {
                getUiHandlers().onDeletePublication(object);
            }
        });
        widget = binder.createAndBindUi(this);
        this.experimentEditDriver = experimentEditDriver;
        this.experimentDisplayDriver = experimentDisplayDriver;
        this.experimentDisplayDriver.initialize(experimentDisplayEditor);
        this.experimentEditDriver.initialize(experimentEditEditor);
        permissionPopUp.setBackdrop(BackdropType.STATIC);
        permissionPopUp.setTitle("Permissions");
        permissionPopUp.setMaxHeigth("700px");
        permissionPopUp.setCloseVisible(false);
        permissionPopUp.setKeyboard(false);
        //initDataGrid();
    }

    private void initDataGrid() {

        HTML emptyWidget = new HTML("<h1 style=\"line-height:220px;color: #ccc;\">No Publications added</h1>");
        publicationDataGrid.setEmptyTableWidget(emptyWidget);
        publicationDataGrid.addColumn(new Column<PublicationProxy, String>(new TextCell()) {
            @Override
            public String getValue(PublicationProxy object) {
                return object.getFirstAuthor();
            }
        }, "Author");

        publicationDataGrid.addColumn(new Column<PublicationProxy, String>(new TextCell()) {
            @Override
            public String getValue(PublicationProxy object) {
                return object.getTitle();
            }
        }, "Title");

        publicationDataGrid.addColumn(new Column<PublicationProxy, String>(new TextCell()) {
            @Override
            public String getValue(PublicationProxy object) {
                return String.valueOf(object.getPubDate().getYear());
            }
        }, "Year");
        publicationDataGrid.addColumn(new Column<PublicationProxy, String>(new TextCell()) {
            @Override
            public String getValue(PublicationProxy object) {
                return object.getJournal();
            }
        }, "Journal");
        publicationDataGrid.addColumn(new Column<PublicationProxy, HyperlinkPlaceManagerColumn.HyperlinkParam>(new HyperlinkCell(true)) {
            @Override
            public HyperlinkPlaceManagerColumn.HyperlinkParam getValue(PublicationProxy object) {
                return new HyperlinkPlaceManagerColumn.HyperlinkParam(object.getDOI(), object.getURL());
            }
        }, "Doi");
        publicationDataGrid.addColumn(new IdentityColumn<PublicationProxy>(new EntypoIconActionCell<PublicationProxy>("&#59177;", new ActionCell.Delegate<PublicationProxy>() {
            @Override
            public void execute(PublicationProxy object) {
                getUiHandlers().onDeletePublication(object);
            }
        })) {
            @Override
            public PublicationProxy getValue(PublicationProxy object) {
                return object;
            }
        }, "");
        publicationDataGrid.setColumnWidth(0, "100px");
        publicationDataGrid.setColumnWidth(2, "60px");
        publicationDataGrid.setColumnWidth(3, "80px");
        publicationDataGrid.setColumnWidth(5, "40px");
    }

    @Override
    public Widget asWidget() {
        return widget;
    }

    @Override
    public ExperimentEditDriver getExperimentEditDriver() {
        return experimentEditDriver;
    }

    @Override
    public void setInSlot(Object slot, Widget content) {
        if (slot == ExperimentDetailPresenter.TYPE_SetPermissionContent) {
            permissionPopUp.add(content);
        } else {
            super.setInSlot(slot, content);
        }
    }


    @Override
    public void setState(State state, int permission) {
        this.state = state;
        experimentDisplayEditor.setVisible(state == State.DISPLAYING);
        experimentEditEditor.setVisible((state == State.EDITING || state == State.SAVING) && (permission & AccessControlEntryProxy.EDIT) == AccessControlEntryProxy.EDIT);
        edit.setVisible(state == State.DISPLAYING &&
                (permission & AccessControlEntryProxy.EDIT) == AccessControlEntryProxy.EDIT);
        save.setVisible(state == State.EDITING && (permission & AccessControlEntryProxy.EDIT) == AccessControlEntryProxy.EDIT);
        cancel.setVisible(state == State.EDITING && (permission & AccessControlEntryProxy.EDIT) == AccessControlEntryProxy.EDIT);
        delete.setVisible(state == State.EDITING && (permission & AccessControlEntryProxy.EDIT) == AccessControlEntryProxy.EDIT);
        share.setVisible((permission & AccessControlEntryProxy.ADMINISTRATION) == AccessControlEntryProxy.ADMINISTRATION);
        addDOIForm.setVisible(state == State.DISPLAYING && (permission & AccessControlEntryProxy.EDIT) == AccessControlEntryProxy.EDIT);
        publicationDataGrid.setShowAction((state == State.DISPLAYING && (permission & AccessControlEntryProxy.EDIT) == AccessControlEntryProxy.EDIT));
    }

    @Override
    public State getState() {
        return state;
    }

    @Override
    public void showPermissionPanel(boolean show) {
        if (show)
            permissionPopUp.show();
        else
            permissionPopUp.hide();
    }

    @Override
    public HasData<PublicationProxy> getPublicationDisplay() {
        return publicationDataGrid;
    }

    @Override
    public ExperimentDisplayDriver getExperimentDisplayDriver() {
        return experimentDisplayDriver;
    }

    @UiHandler("edit")
    public void onEdit(ClickEvent e) {
        if (state == State.DISPLAYING) {
            getUiHandlers().onEdit();
        }
    }

    @UiHandler("delete")
    public void onDelete(ClickEvent e) {
        if (state == State.EDITING) {
            if (Window.confirm("Do you really want to delete the Experiment?"))
                getUiHandlers().onDelete();
        }
    }

    @UiHandler("save")
    public void onSave(ClickEvent e) {
        if (state == State.EDITING) {
            getUiHandlers().onSave();
        }
    }

    @UiHandler("cancel")
    public void onCancel(ClickEvent e) {
        if (state == State.EDITING) {
            getUiHandlers().onCancel();
        }
    }

    @UiHandler("share")
    public void onClickShareBtn(ClickEvent e) {
        getUiHandlers().onShare();
    }

    @UiHandler("addPublication")
    public void onClickAddPublication(ClickEvent e) {
        if (doiTb.getText().equals(""))
            return;
        getUiHandlers().queryDOI(doiTb.getText());
    }

    private void forceLayout() {
        publicationDataGrid.onResize();
    }

    @Override
    public void scheduledLayout() {
        if (!layoutScheduled) {
            layoutScheduled = true;
            Scheduler.get().scheduleDeferred(layoutCmd);
        }
    }

    @Override
    public void phaseInPublication(PublicationProxy publication, ProvidesKey<PublicationProxy> providesKey) {
        (new PhaseAnimation.DataGridPhaseAnimation<PublicationProxy>(publicationDataGrid, publication, providesKey)).run();
    }

    @Override
    public HasText getDOIText() {
        return doiTb;
    }
}
