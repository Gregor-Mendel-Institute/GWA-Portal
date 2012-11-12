package com.gmi.nordborglab.browser.server.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;

import com.gmi.nordborglab.browser.server.data.GWASData;
import com.gmi.nordborglab.browser.server.data.GWASDataForClient;
import com.gmi.nordborglab.browser.server.service.GWASDataService;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.visualization.datasource.Capabilities;
import com.google.visualization.datasource.DataTableGenerator;
import com.google.visualization.datasource.base.DataSourceException;
import com.google.visualization.datasource.datatable.ColumnDescription;
import com.google.visualization.datasource.datatable.DataTable;
import com.google.visualization.datasource.datatable.value.ValueType;
import com.google.visualization.datasource.query.Query;
import com.google.visualization.datasource.render.JsonRenderer;

@Component
public class GWASDataTableGenerator implements DataTableGenerator {

	@Resource
	GWASDataService gwasDataService;
	
	@Override
	public DataTable generateDataTable(Query query, HttpServletRequest request)
			throws DataSourceException {
		Long studyId = Long.parseLong(request.getParameter("studyId"));
		String chr = request.getParameter("chr");
		Map<String,GWASData> map = gwasDataService.getGWASData(studyId);
		GWASData data = map.get(chr);
		DataTable table = convertGWASDataToDataTable(data);
		return table;
	}

	@Override
	public Capabilities getCapabilities() {
		return Capabilities.NONE;
	}

	public GWASDataForClient getGWASDataForClient(Long studyId) throws DataSourceException {
		GWASDataForClient gwasData =null;
		Map<String,GWASData> map = gwasDataService.getGWASData(studyId);
		List<String> dataTables = new ArrayList<String>();
		List<String> chromosomes = new ArrayList<String>();
		///TODO not hardcoded
		List<Integer> chrLenghts = ImmutableList.of(30429953, 19701870, 23467451, 18578708, 26992130);
		for (Map.Entry<String, GWASData> entry : map.entrySet())
        {
            String chr = entry.getKey();
        	chromosomes.add(chr);
            GWASData data = entry.getValue();
            DataTable dataTable =  convertGWASDataToDataTable(data);   
            CharSequence json = JsonRenderer.renderDataTable(dataTable, true, false, true);
            dataTables.add(json.toString());
        }
		gwasData = new GWASDataForClient(10,6, chromosomes, chrLenghts, dataTables);
		return gwasData;
	}
	
	public static DataTable convertGWASDataToDataTable(GWASData data) throws DataSourceException  {
		DataTable table = new DataTable();
		table.addColumn(new ColumnDescription("pos",ValueType.NUMBER,"Position"));
		table.addColumn(new ColumnDescription("pValue",ValueType.NUMBER,"pValue"));
		
		for (int i =0;i<data.getPositions().length;i++) {
			table.addRowFromValues(data.getPositions()[i],data.getPvalues()[i]);
		}
		return table;
	}

}
