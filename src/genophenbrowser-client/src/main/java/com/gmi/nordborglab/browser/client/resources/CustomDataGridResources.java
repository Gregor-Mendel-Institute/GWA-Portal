package com.gmi.nordborglab.browser.client.resources;

import com.google.gwt.user.cellview.client.DataGrid;

public interface CustomDataGridResources  extends DataGrid.Resources {

        String DEFAULT_GSS = "com/google/gwt/user/cellview/client/DataGrid.gss";
	
	interface CustomDataGridStyle extends DataGrid.Style {
		
		@Override
                String dataGridHeader();
		
		@Override
                String dataGridCell();
		
		@Override
                String dataGridOddRow();
		
		@Override
                String dataGridHoveredRow();
		
		@Override
                String dataGridKeyboardSelectedRow();
		
		@Override
                String dataGridSelectedRow();
	}

	@Override
        @Source({DEFAULT_GSS, "customDataGridStyle.gss"})
        CustomDataGridStyle dataGridStyle();
}
