package com.gmi.nordborglab.browser.client.mvp.diversity.experiment.detail;

import com.gmi.nordborglab.browser.client.ui.cells.EntypoIconActionCell;
import com.gmi.nordborglab.browser.client.ui.cells.HyperlinkCell;
import com.gmi.nordborglab.browser.client.ui.cells.HyperlinkPlaceManagerColumn;
import com.gmi.nordborglab.browser.shared.proxy.PublicationProxy;
import com.google.gwt.cell.client.ActionCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.IdentityColumn;
import com.google.web.bindery.requestfactory.gwt.ui.client.EntityProxyKeyProvider;

/**
 * Created by uemit.seren on 2/24/15.
 */
public class PublicationResponsiveDataGrid extends DataGrid<PublicationProxy> {

    private final Column<PublicationProxy, String> citationColumn;
    private final Column<PublicationProxy, String> authorColumn;
    private final Column<PublicationProxy, String> titleColumn;
    private final Column<PublicationProxy, String> yearColumn;
    private final Column<PublicationProxy, String> journalColumn;
    private final Column<PublicationProxy, HyperlinkPlaceManagerColumn.HyperlinkParam> doiColumn;
    private final IdentityColumn<PublicationProxy> actionColumn;
    private Boolean isCompact;
    private boolean showAction = true;


    public PublicationResponsiveDataGrid(int pageSize, Resources resources, ActionCell.Delegate<PublicationProxy> actionDelegate) {
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
        actionColumn = new IdentityColumn<PublicationProxy>(new EntypoIconActionCell<PublicationProxy>("e_icon-trash", actionDelegate)) {

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
