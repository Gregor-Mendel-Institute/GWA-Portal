package com.gmi.nordborglab.browser.client.resources;

import com.google.gwt.user.cellview.client.DataGrid;

public interface DataGridStudyResources extends DataGrid.Resources {
	
	
	interface DataGridStudyStyle extends DataGrid.Style {
		
		@Override
                String dataGridCell();
		
		@Override
                String dataGridFirstColumnHeader();
		
		@Override
                String dataGridFirstColumnFooter();
	}
	
	@Override
        @Source({CustomDataGridResources.DEFAULT_GSS, "studyGridStyle.gss"})
        DataGridStudyStyle dataGridStyle();
}