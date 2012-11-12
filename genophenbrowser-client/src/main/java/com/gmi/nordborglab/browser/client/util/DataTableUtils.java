package com.gmi.nordborglab.browser.client.util;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.visualization.client.DataTable;

public class DataTableUtils {
	
	public static CustomDataTable createDataTableFromString(String json) {
		CustomDataTable dataTable = null;
		try {
			JavaScriptObject jsData = JsonUtils.safeEval(json);
			dataTable = (CustomDataTable) DataTable.create(jsData);
		}
		catch (Exception e) {}
		return dataTable;
	}

}
