package com.gmi.nordborglab.browser.client.editors;

import java.util.List;

import com.github.gwtbootstrap.client.ui.FluidContainer;
import com.gmi.nordborglab.browser.client.resources.PermissionDataGridResources;
import com.gmi.nordborglab.browser.client.ui.cells.EntypoIconActionCell;
import com.gmi.nordborglab.browser.client.ui.cells.PermissionSelectionCell;
import com.gmi.nordborglab.browser.shared.proxy.AccessControlEntryProxy;
import com.gmi.nordborglab.browser.shared.proxy.CustomAclProxy;
import com.google.common.collect.ImmutableList;
import com.google.gwt.cell.client.*;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.adapters.HasDataEditor;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.IdentityColumn;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class PermissionEditor extends Composite implements Editor<CustomAclProxy>{

	private static PermissionEditorUiBinder uiBinder = GWT.create(PermissionEditorUiBinder.class);
    interface PermissionEditorUiBinder extends UiBinder<Widget, PermissionEditor> {}

    private static class PermissionDeleteIconCell extends EntypoIconActionCell<AccessControlEntryProxy> {


        public PermissionDeleteIconCell(ActionCell.Delegate<AccessControlEntryProxy> delegate) {
            super("&#10060;", delegate);
        }

        @Override
        public void render(Context context, AccessControlEntryProxy value, SafeHtmlBuilder sb) {
            if (!value.getPrincipal().getIsUser() || value.getPrincipal().getIsOwner())
                return;
            super.render(context, value, sb);    //To change body of overridden methods use File | Settings | File Templates.
        }

    }

    @UiField(provided=true)
    CellTable<AccessControlEntryProxy> dataGrid;
    @Path("entries")
	HasDataEditor<AccessControlEntryProxy> permissionEditor;

    @Path("isEntriesInheriting")
	@UiField CheckBox isEntriesInheriting;
    private final PermissionSelectionCell permissionSelectionCell;

    private ActionCell.Delegate<AccessControlEntryProxy> deleteDelegate;

    private FieldUpdater<AccessControlEntryProxy,AccessControlEntryProxy> fieldUpdater;
    static class PermissionFieldUpdater implements FieldUpdater<AccessControlEntryProxy,Boolean> {

		protected final int permission;

		public PermissionFieldUpdater(int permission) {
			super();
			this.permission = permission;
		}

		@Override
		public void update(int index, AccessControlEntryProxy object,
				Boolean value) {
			int mask = object.getMask();
			if (value) {
				mask |=permission;
			}
			else {
				mask &=~permission;
			}
			object.setMask(mask);
		}

    }

    @Inject
	public PermissionEditor(PermissionDataGridResources dataGridResources, final PermissionSelectionCell permissionSelectionCell) {
        this.permissionSelectionCell = permissionSelectionCell;
        dataGrid = new CellTable<AccessControlEntryProxy>(50,dataGridResources);
        createDataGrid();
		initWidget(uiBinder.createAndBindUi(this));
		permissionEditor = HasDataEditor.of(dataGrid);
	}
	private void createDataGrid() {
		Column<AccessControlEntryProxy,Boolean> checkBoxColumn = null;

        dataGrid.addColumn(new Column<AccessControlEntryProxy, String>(new ImageCell()) {
            @Override
            public String getValue(AccessControlEntryProxy object) {
                return "test";
            }
        });
		dataGrid.addColumn(new Column<AccessControlEntryProxy, String>(new TextCell()) {
			@Override
			public String getValue(AccessControlEntryProxy object) {
				String name = object.getPrincipal().getName();
				return name;
			}
		});
        dataGrid.setColumnWidth(0, 30, Style.Unit.PX);
        IdentityColumn<AccessControlEntryProxy> permissionColumn = new IdentityColumn<AccessControlEntryProxy>(permissionSelectionCell);
        dataGrid.addColumn(permissionColumn);
        dataGrid.addColumn(new IdentityColumn<AccessControlEntryProxy>(new PermissionDeleteIconCell(new ActionCell.Delegate<AccessControlEntryProxy>() {
            @Override
            public void execute(AccessControlEntryProxy object) {
                if (deleteDelegate != null)
                    deleteDelegate.execute(object);
            }
        })));
        dataGrid.setColumnWidth(2,100, Style.Unit.PX);
        permissionColumn.setFieldUpdater(new FieldUpdater<AccessControlEntryProxy, AccessControlEntryProxy>() {
            @Override
            public void update(int index, AccessControlEntryProxy object, AccessControlEntryProxy value) {
                if (fieldUpdater != null)
                    fieldUpdater.update(index,object,value);
            }
        });
        dataGrid.setColumnWidth(3,20, Style.Unit.PX);
    }

	public void addPermission(AccessControlEntryProxy permission) {
		permissionEditor.getList().add(permission);
	}



	public List<AccessControlEntryProxy> getPermissionList() {
		return permissionEditor.getList();
	}

    public void setDeleteDelegate(ActionCell.Delegate<AccessControlEntryProxy> delegate) {
        this.deleteDelegate = delegate;
    }

    public void setFieldUpdater(FieldUpdater<AccessControlEntryProxy,AccessControlEntryProxy> fieldUpdater) {
        this.fieldUpdater = fieldUpdater;
    }
}
