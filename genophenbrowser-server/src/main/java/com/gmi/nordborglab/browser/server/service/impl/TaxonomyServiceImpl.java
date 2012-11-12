package com.gmi.nordborglab.browser.server.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gmi.nordborglab.browser.server.domain.TaxonomyStats;
import com.gmi.nordborglab.browser.server.domain.genotype.AlleleAssay;
import com.gmi.nordborglab.browser.server.domain.germplasm.Sampstat;
import com.gmi.nordborglab.browser.server.domain.germplasm.Taxonomy;
import com.gmi.nordborglab.browser.server.repository.TaxonomyRepository;
import com.gmi.nordborglab.browser.server.service.TaxonomyService;
import com.google.visualization.datasource.base.DataSourceException;
import com.google.visualization.datasource.datatable.ColumnDescription;
import com.google.visualization.datasource.datatable.DataTable;
import com.google.visualization.datasource.datatable.value.ValueType;
import com.google.visualization.datasource.render.JsonRenderer;



@Service
@Transactional(readOnly=true)
public class TaxonomyServiceImpl implements TaxonomyService {
	
	@Resource
	protected TaxonomyRepository taxonomyRepository;

	@Override
	public List<Taxonomy> findAll() {
		List<Taxonomy> taxonomies = taxonomyRepository.findAll();
		for (Taxonomy taxonomy: taxonomies) {
			List<AlleleAssay> alleleAssays = taxonomyRepository.findAlleleAssaysForTaxonomy(taxonomy.getId());
			taxonomy.setAlleleAssays(alleleAssays);		
		}
		return taxonomies;
	}

	@Override
	public Taxonomy findOne(Long id) {
		return taxonomyRepository.findOne(id);
	}

	@Override
	public Taxonomy save(Taxonomy taxonomy) {
		Taxonomy savedTaxonomy = taxonomyRepository.save(taxonomy);
		return savedTaxonomy;
	}

	@Override
	public TaxonomyStats findStats(Long id)  {
		TaxonomyStats stats = new TaxonomyStats();
		List<Object[]> numberOfPassportsPerAlleleAssay = taxonomyRepository.countPassportsPerAlleleAssay(id);
		List<Object[]> numberOfPassportsPerSampStat = taxonomyRepository.countPassportsPerSampStat(id);
		List<Object[]> numberOfPassportsPerCountry = taxonomyRepository.countPassportsPerCountry(id);
		List<Object[]> numberOfStocksPerGeneration = taxonomyRepository.countStocksPerGeneration(id);
		
		try {
			DataTable table = new DataTable();
			table.addColumn(new ColumnDescription("alleleAssay",ValueType.TEXT,"Genotype"));
			table.addColumn(new ColumnDescription("count",ValueType.NUMBER,"# Count"));
			
			for (Object[] item: numberOfPassportsPerAlleleAssay) {
				table.addRowFromValues(((AlleleAssay)item[0]).getName(),item[1]);
			}
			CharSequence json = JsonRenderer.renderDataTable(table, true, false, true);
			stats.setAlleleAssayData(json.toString());
			
			table = new DataTable();
			table.addColumn(new ColumnDescription("sampstat",ValueType.TEXT,"Type"));
			table.addColumn(new ColumnDescription("count",ValueType.NUMBER,"# Count"));
			
			for (Object[] item: numberOfPassportsPerSampStat) {
				table.addRowFromValues(((Sampstat)item[0]).getGermplasmType(),item[1]);
			}
			json = JsonRenderer.renderDataTable(table, true, false, true);
			stats.setSampStatData(json.toString());
			
			table = new DataTable();
			table.addColumn(new ColumnDescription("country",ValueType.TEXT,"Country"));
			table.addColumn(new ColumnDescription("count",ValueType.NUMBER,"# Count"));
			
			for (Object[] item: numberOfPassportsPerCountry) {
				table.addRowFromValues(item);
			}
			json = JsonRenderer.renderDataTable(table, true, false, true);
			stats.setGeoChartData(json.toString());
			
			table = new DataTable();
			table.addColumn(new ColumnDescription("generation",ValueType.TEXT,"Generation"));
			table.addColumn(new ColumnDescription("count",ValueType.NUMBER,"# Count"));
			
			for (Object[] item: numberOfStocksPerGeneration) {
				table.addRowFromValues(item);
			}
			json = JsonRenderer.renderDataTable(table, true, false, true);
			stats.setStockGenerationData(json.toString());
			
		}
		catch (DataSourceException e) {
			throw new RuntimeException(e.getLocalizedMessage());
		}
		
		
		
		
		return stats;
	}

}
