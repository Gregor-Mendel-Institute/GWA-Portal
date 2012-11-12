package com.gmi.nordborglab.browser.client.resources;

import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.DataGrid.Style;

public interface DataGridResources extends DataGrid.Resources {
	
	
	interface CustomDataGridStyle extends DataGrid.Style {
		
		@Override
		public String dataGridCell();
		
		@Override
		public String dataGridFirstColumnHeader();
		
		@Override
		public String dataGridFirstColumnFooter();
	}
	
	@Override
	@Source({Style.DEFAULT_CSS,"dataGridStyle.css"})
	public CustomDataGridStyle dataGridStyle();
}