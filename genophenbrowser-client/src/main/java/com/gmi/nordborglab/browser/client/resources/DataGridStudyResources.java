package com.gmi.nordborglab.browser.client.resources;

import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.DataGrid.Style;

public interface DataGridStudyResources extends DataGrid.Resources {
	
	
	interface DataGridStudyStyle extends DataGrid.Style {
		
		@Override
		public String dataGridCell();
		
		@Override
		public String dataGridFirstColumnHeader();
		
		@Override
		public String dataGridFirstColumnFooter();
	}
	
	@Override
	@Source({Style.DEFAULT_CSS,"studyGridStyle.css"})
	public DataGridStudyStyle dataGridStyle();
}