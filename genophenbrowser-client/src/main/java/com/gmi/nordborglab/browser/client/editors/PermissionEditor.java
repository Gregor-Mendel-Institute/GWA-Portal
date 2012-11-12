package com.gmi.nordborglab.browser.client.editors;

import java.util.List;

import com.gmi.nordborglab.browser.shared.proxy.AccessControlEntryProxy;
import com.gmi.nordborglab.browser.shared.proxy.CustomAclProxy;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.adapters.HasDataEditor;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class PermissionEditor extends Composite implements Editor<CustomAclProxy>{

	private static PermissionEditorUiBinder uiBinder = GWT
			.create(PermissionEditorUiBinder.class);

	interface PermissionEditorUiBinder extends
			UiBinder<Widget, PermissionEditor> {
	}
	@UiField(provided=true) DataGrid<AccessControlEntryProxy> dataGrid;
	
	@Path("entries")
	HasDataEditor<AccessControlEntryProxy> permissionEditor; 
	
	@Path("isEntriesInheriting")
	@UiField CheckBox isEntriesInheriting;
	
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

	public PermissionEditor() {
		createDataGrid();
		initWidget(uiBinder.createAndBindUi(this));
		permissionEditor = HasDataEditor.of(dataGrid);
	}
	
	private void createDataGrid() {
		Column<AccessControlEntryProxy,Boolean> checkBoxColumn = null;
		dataGrid = new DataGrid<AccessControlEntryProxy>();
		dataGrid.addColumn(new Column<AccessControlEntryProxy, String>(new TextCell()) {
			@Override
			public String getValue(AccessControlEntryProxy object) {
				String name = object.getPrincipal().getName();
				return name;
			}
		},"User/Role");
		
		checkBoxColumn = new Column<AccessControlEntryProxy,Boolean>(new CheckboxCell(false, false)) {

			@Override
			public Boolean getValue(AccessControlEntryProxy object) {
				return ((object.getMask() & AccessControlEntryProxy.READ) == AccessControlEntryProxy.READ);
			}
			
		};
		checkBoxColumn.setFieldUpdater(new PermissionFieldUpdater(AccessControlEntryProxy.READ));
		dataGrid.addColumn(checkBoxColumn,"READ");
		
		checkBoxColumn = new Column<AccessControlEntryProxy,Boolean>(new CheckboxCell(false, false)) {

			@Override
			public Boolean getValue(AccessControlEntryProxy object) {
				return ((object.getMask() & AccessControlEntryProxy.WRITE) == AccessControlEntryProxy.WRITE);
			}
			
		};
		checkBoxColumn.setFieldUpdater(new PermissionFieldUpdater(AccessControlEntryProxy.WRITE));
		dataGrid.addColumn(checkBoxColumn,"WRITE");
		
		checkBoxColumn = new Column<AccessControlEntryProxy,Boolean>(new CheckboxCell(false, false)) {

			@Override
			public Boolean getValue(AccessControlEntryProxy object) {
				return ((object.getMask() & AccessControlEntryProxy.DELETE) == AccessControlEntryProxy.DELETE);
			}
			
		};
		checkBoxColumn.setFieldUpdater(new PermissionFieldUpdater(AccessControlEntryProxy.DELETE));
		dataGrid.addColumn(checkBoxColumn,"DELETE");
		
		checkBoxColumn = new Column<AccessControlEntryProxy,Boolean>(new CheckboxCell(false, false)) {

			@Override
			public Boolean getValue(AccessControlEntryProxy object) {
				return ((object.getMask() & AccessControlEntryProxy.CREATE) == AccessControlEntryProxy.CREATE);
			}
			
		};
		checkBoxColumn.setFieldUpdater(new PermissionFieldUpdater(AccessControlEntryProxy.CREATE));
		dataGrid.addColumn(checkBoxColumn,"CREATE");
		
		checkBoxColumn = new Column<AccessControlEntryProxy,Boolean>(new CheckboxCell(false, false)) {

			@Override
			public Boolean getValue(AccessControlEntryProxy object) {
				return ((object.getMask() & AccessControlEntryProxy.ADMINISTRATION) == AccessControlEntryProxy.ADMINISTRATION);
			}
			
		};
		checkBoxColumn.setFieldUpdater(new PermissionFieldUpdater(AccessControlEntryProxy.ADMINISTRATION));
		dataGrid.addColumn(checkBoxColumn,"ADMINISTRATION");
	}

	public void addPermission(AccessControlEntryProxy permission) {
		permissionEditor.getList().add(permission);
	}
	
	public List<AccessControlEntryProxy> getPermissionList() {
		return permissionEditor.getList();
	}

}
