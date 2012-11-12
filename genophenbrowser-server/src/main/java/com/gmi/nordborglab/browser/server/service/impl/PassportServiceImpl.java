package com.gmi.nordborglab.browser.server.service.impl;

import static com.gmi.nordborglab.browser.server.domain.specifications.PassportPredicates.accNameContains;
import static com.gmi.nordborglab.browser.server.domain.specifications.PassportPredicates.accNumberContains;
import static com.gmi.nordborglab.browser.server.domain.specifications.PassportPredicates.alleleAssayIdsEqual;
import static com.gmi.nordborglab.browser.server.domain.specifications.PassportPredicates.collectorContains;
import static com.gmi.nordborglab.browser.server.domain.specifications.PassportPredicates.countriesIn;
import static com.gmi.nordborglab.browser.server.domain.specifications.PassportPredicates.passportIdEqual;
import static com.gmi.nordborglab.browser.server.domain.specifications.PassportPredicates.sampStatIdEqual;
import static com.gmi.nordborglab.browser.server.domain.specifications.PassportPredicates.sourceContains;
import static com.gmi.nordborglab.browser.server.domain.specifications.PassportPredicates.taxonomyIdEqual;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gmi.nordborglab.browser.server.domain.PassportSearchCriteria;
import com.gmi.nordborglab.browser.server.domain.PassportStats;
import com.gmi.nordborglab.browser.server.domain.germplasm.Passport;
import com.gmi.nordborglab.browser.server.domain.germplasm.Stock;
import com.gmi.nordborglab.browser.server.domain.pages.PassportPage;
import com.gmi.nordborglab.browser.server.domain.phenotype.StatisticType;
import com.gmi.nordborglab.browser.server.domain.phenotype.Trait;
import com.gmi.nordborglab.browser.server.domain.phenotype.TraitUom;
import com.gmi.nordborglab.browser.server.domain.phenotype.UnitOfMeasure;
import com.gmi.nordborglab.browser.server.repository.PassportRepository;
import com.gmi.nordborglab.browser.server.repository.StockRepository;
import com.gmi.nordborglab.browser.server.service.PassportService;
import com.google.visualization.datasource.base.DataSourceException;
import com.google.visualization.datasource.datatable.ColumnDescription;
import com.google.visualization.datasource.datatable.DataTable;
import com.google.visualization.datasource.datatable.value.ValueType;
import com.google.visualization.datasource.render.JsonRenderer;
import com.mysema.query.BooleanBuilder;

@Service
@Transactional(readOnly = true)
public class PassportServiceImpl implements PassportService {
	
	@Resource
	protected PassportRepository passportRepository;
	
	@Resource
	protected StockRepository stockRepository;

	@Override
	public PassportPage findAll(Long taxonomyId, PassportSearchCriteria filter, int start, int size) {
		Sort sort = new Sort(new Sort.Order(Sort.Direction.ASC, "id"));
		if (start > 0)
			start = start/size;
		PassportPage page = null;
		PageRequest pageRequest = new PageRequest(start, size,sort);
		BooleanBuilder predicate = new BooleanBuilder(taxonomyIdEqual(taxonomyId));
		if (filter != null) {
			predicate.and(passportIdEqual(filter.getPassportId()));
			predicate.and(collectorContains(filter.getCollector()));
			predicate.and(sampStatIdEqual(filter.getSampStatId()));
			predicate.and(accNumberContains(filter.getAccNumber()));
			predicate.and(accNameContains(filter.getAccName()));
			predicate.and(sourceContains(filter.getSource()));
			if (filter.getAlleleAssayIds() != null && filter.getAlleleAssayIds().size() > 0 ) {
				predicate.and(alleleAssayIdsEqual(filter.getAlleleAssayIds()));
			}
			if (filter.getCountries() != null) {
				predicate.andAnyOf(countriesIn(filter.getCountries()));
			}
		}
		Page<Passport> passportPage = passportRepository.findAll(predicate,pageRequest);
		
		page = new PassportPage(passportPage.getContent(), pageRequest, passportPage.getTotalElements());
		return page;
	}

	@Override
	public Passport findOne(Long passportId) {
		return passportRepository.findOne(passportId);
	}

	@Override
	public PassportStats findStats(Long passportId) {
		PassportStats stats = new PassportStats();
		List<Object[]> numberOfPasportsPerTraitOntology = passportRepository.countPassportsPerTraitOntology(passportId);
		List<Object[]> numberOfPasportsPerEnvironmentOntology = passportRepository.countPassportsPerEnvironmentOntology(passportId);
		List<Object[]> numberOfPassportsPerStatisticType = passportRepository.countPassportsPerStatisticType(passportId);
		List<Object[]> numberOfPassportsPerUnitOfMeasure = passportRepository.countPassportsPerUnitOfMeasure(passportId);
		
		try {
			DataTable table = new DataTable();
			table.addColumn(new ColumnDescription("label",ValueType.TEXT,"Label"));
			table.addColumn(new ColumnDescription("count",ValueType.NUMBER,"# Count"));
			table.addColumn(new ColumnDescription("type",ValueType.NUMBER,"Type"));
			
			for (Object[] item: numberOfPasportsPerTraitOntology) {
				String traitOntology = (String)item[0];
				table.addRowFromValues(traitOntology != null ? traitOntology : "N/A",item[1],0);
			}
			for (Object[] item: numberOfPasportsPerEnvironmentOntology) {
				String envOntology = (String)item[0];
				table.addRowFromValues(envOntology != null ? envOntology: "N/A",item[1],1);
			}
			
			for (Object[] item: numberOfPassportsPerStatisticType) {
				StatisticType statisticType = (StatisticType)item[0];
				table.addRowFromValues(statisticType != null ? statisticType.getStatType() : "N/A",item[1],2);
			}
			for (Object[] item: numberOfPassportsPerUnitOfMeasure) {
				UnitOfMeasure unitOfMeasure = (UnitOfMeasure)item[0];
				table.addRowFromValues(unitOfMeasure != null ? unitOfMeasure.getUnitType() : "N/A",item[1],3);
			}
			CharSequence json = JsonRenderer.renderDataTable(table, true, false, true);
			stats.setData(json.toString());
			
		}
		catch (DataSourceException e) {
			throw new RuntimeException(e.getLocalizedMessage());
		}
		return stats;
	}

	@Override
	public List<Stock> findAllStocks(Long passportId) {
		Sort sort = new Sort("id");
		return stockRepository.findAllByPassportId(passportId,sort);
	}
}
