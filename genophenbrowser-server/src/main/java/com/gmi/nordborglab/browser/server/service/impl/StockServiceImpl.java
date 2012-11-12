package com.gmi.nordborglab.browser.server.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gmi.nordborglab.browser.server.domain.germplasm.Stock;
import com.gmi.nordborglab.browser.server.repository.StockRepository;
import com.gmi.nordborglab.browser.server.service.StockService;
import com.google.visualization.datasource.base.TypeMismatchException;
import com.google.visualization.datasource.datatable.ColumnDescription;
import com.google.visualization.datasource.datatable.DataTable;
import com.google.visualization.datasource.datatable.value.ValueType;
import com.google.visualization.datasource.render.JsonRenderer;

@Service
@Transactional(readOnly=true)
public class StockServiceImpl implements StockService {

	@Resource
	private StockRepository repository;
	

	@Override
	public Stock findOne(Long stockId) {
		Stock stock = repository.findOne(stockId);
		String jsonText = "";
		try {
			DataTable pedigreeData = getPedigreeDataTable(stockId);
			CharSequence json = JsonRenderer.renderDataTable(pedigreeData, true, false, true);
			jsonText = json.toString();
		}
		catch (Exception e) {}
		stock.setPedigreeData(jsonText);
		return stock;
	}
	
	public DataTable getPedigreeDataTable(Long stockId) throws TypeMismatchException {
		List<Object[]> ancestors = repository.findAncestors(stockId);
		List<Object[]> descendents = repository.findDescendents(stockId);
		
		DataTable table = new DataTable();
		table.addColumn(new ColumnDescription("ID",ValueType.TEXT,"ID"));
		table.addColumn(new ColumnDescription("Parent",ValueType.TEXT,"Parent"));
		table.addColumn(new ColumnDescription("Tooltip",ValueType.TEXT,"Tooltip"));
		table.addColumn(new ColumnDescription("Type",ValueType.NUMBER,"Type"));
		table.addRowFromValues(stockId.toString(),"","Node",0);
		table.addRowFromValues(stockId.toString(),"","Node",1);
		for (Object[] ancestor:ancestors) {
			table.addRowFromValues(ancestor[2].toString(),ancestor[1].toString(),ancestor[3],0);
		}
		for (Object[] descendent:descendents) {
			table.addRowFromValues(descendent[1].toString(),descendent[2].toString(),descendent[3],1);
		}
		return table;
	}
	
	/*private TableRow getParentTableRows(List<Stock> parents) {
		TableRow row = new TableRow.
	}*/
	

}
