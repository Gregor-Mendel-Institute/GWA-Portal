package com.gmi.nordborglab.browser.client.resources;

import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.DataGrid.Style;

public interface CustomDataGridResources  extends DataGrid.Resources {
	
	
	interface CustomDataGridStyle extends DataGrid.Style {
		
		@Override
		public String dataGridHeader();
		
		@Override
		public String dataGridCell();
		
		@Override
		public String dataGridOddRow();
		
		@Override
		public String dataGridHoveredRow();
		
		@Override
		public String dataGridKeyboardSelectedRow();
		
		@Override
		public String dataGridSelectedRow();
		
	
	}
	
	
	
	@Override
	@Source({Style.DEFAULT_CSS,"customDataGridStyle.css"})
	public CustomDataGridStyle dataGridStyle();
}
