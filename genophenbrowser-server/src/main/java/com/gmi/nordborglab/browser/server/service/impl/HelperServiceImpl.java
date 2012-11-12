package com.gmi.nordborglab.browser.server.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gmi.nordborglab.browser.server.domain.AppData;
import com.gmi.nordborglab.browser.server.domain.BreadcrumbItem;
import com.gmi.nordborglab.browser.server.domain.cdv.Study;
import com.gmi.nordborglab.browser.server.domain.cdv.StudyProtocol;
import com.gmi.nordborglab.browser.server.domain.genotype.AlleleAssay;
import com.gmi.nordborglab.browser.server.domain.germplasm.Passport;
import com.gmi.nordborglab.browser.server.domain.germplasm.Sampstat;
import com.gmi.nordborglab.browser.server.domain.germplasm.Stock;
import com.gmi.nordborglab.browser.server.domain.germplasm.Taxonomy;
import com.gmi.nordborglab.browser.server.domain.observation.Experiment;
import com.gmi.nordborglab.browser.server.domain.phenotype.StatisticType;
import com.gmi.nordborglab.browser.server.domain.phenotype.TraitUom;
import com.gmi.nordborglab.browser.server.domain.phenotype.UnitOfMeasure;
import com.gmi.nordborglab.browser.server.repository.AlleleAssayRepository;
import com.gmi.nordborglab.browser.server.repository.ExperimentRepository;
import com.gmi.nordborglab.browser.server.repository.PassportRepository;
import com.gmi.nordborglab.browser.server.repository.SampstatRepository;
import com.gmi.nordborglab.browser.server.repository.StatisticTypeRepository;
import com.gmi.nordborglab.browser.server.repository.StockRepository;
import com.gmi.nordborglab.browser.server.repository.StudyProtocolRepository;
import com.gmi.nordborglab.browser.server.repository.StudyRepository;
import com.gmi.nordborglab.browser.server.repository.TaxonomyRepository;
import com.gmi.nordborglab.browser.server.repository.TraitUomRepository;
import com.gmi.nordborglab.browser.server.repository.UnitOfMeasureRepository;
import com.gmi.nordborglab.browser.server.service.HelperService;
import com.gmi.nordborglab.browser.shared.service.HelperFactory;
import com.google.common.collect.Iterables;
import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;

@Service
@Transactional(readOnly = true)
public class HelperServiceImpl implements HelperService {
	
	private static HelperFactory helperFactory = AutoBeanFactorySource.create(HelperFactory.class);
	
	@Resource
	private ExperimentRepository experimentRepository;
	
	@Resource
	private StockRepository stockRepository;
	
	@Resource
	private TaxonomyRepository taxonomRepository;
	
	@Resource
	private PassportRepository passportRepository;
	
	@Resource
	private SampstatRepository sampstatRepository;
	
	@Resource
	private AlleleAssayRepository alleleAssayRepository;
	
	@Resource
	private StudyProtocolRepository studyProtocolRepository;
	
	@Resource
	private UnitOfMeasureRepository unitOfMeasureRepository;
	
	@Resource
	private StatisticTypeRepository statisticTypeRepository;
	
	@Resource
	private TraitUomRepository traitUomRepository;
	
	@Resource
	private StudyRepository studyRepository;
	

	@Override
	public List<BreadcrumbItem> getBreadcrumbs(Long id, String object) {
		List<BreadcrumbItem> breadcrumbs = new ArrayList<BreadcrumbItem>();
		if (object.equals("experiment") || object.equals("phenotypes")) {
			Experiment exp = experimentRepository.findOne(id);
			breadcrumbs.add(new BreadcrumbItem(exp.getId(),exp.getName(),"experiment"));
		}
		else if (object.equals("phenotype")) {
			Experiment exp = experimentRepository.findByPhenotypeId(id);
			TraitUom traitUom = traitUomRepository.findOne(id);
		    breadcrumbs.add(new BreadcrumbItem(exp.getId(),exp.getName(),"experiment"));
		    breadcrumbs.add(new BreadcrumbItem(traitUom.getId(),traitUom.getLocalTraitName(),"phenotype"));
		}
		else if (object.equals("study")) {
			Study study = studyRepository.findOne(id);
			TraitUom trait = Iterables.get(study.getTraits(),0).getTraitUom();
			Experiment exp = Iterables.get(study.getTraits(),0).getObsUnit().getExperiment();
			breadcrumbs.add(new BreadcrumbItem(exp.getId(),exp.getName(),"experiment"));
		    breadcrumbs.add(new BreadcrumbItem(trait.getId(),trait.getLocalTraitName(),"phenotype"));
		    breadcrumbs.add(new BreadcrumbItem(study.getId(),study.getName(),"study"));
		}
		else if (object.equals("studywizard")) {
			Experiment exp = experimentRepository.findByPhenotypeId(id);
			TraitUom trait = traitUomRepository.findOne(id);
			breadcrumbs.add(new BreadcrumbItem(exp.getId(),exp.getName(),"experiment"));
		    breadcrumbs.add(new BreadcrumbItem(trait.getId(),trait.getLocalTraitName(),"phenotype"));
		    breadcrumbs.add(new BreadcrumbItem(trait.getId(),"New Study","studywizard"));
		}
		else if (object.equals("taxonomy") ) {
			Taxonomy tax = taxonomRepository.findOne(id);
			breadcrumbs.add(new BreadcrumbItem(tax.getId(),tax.getGenus()+" "+tax.getSpecies(),"taxonomy"));
		}
		else if (object.equals("passports")) {
			Taxonomy tax = taxonomRepository.findOne(id);
			breadcrumbs.add(new BreadcrumbItem(tax.getId(),tax.getGenus()+" "+tax.getSpecies(),"passports"));
		}
		else if (object.equals("passport")) {
			Passport passport = passportRepository.findOne(id);
			Taxonomy taxonomy = passport.getTaxonomy();
			breadcrumbs.add(new BreadcrumbItem(taxonomy.getId(),taxonomy.getGenus()+" "+taxonomy.getSpecies(),"taxonomy"));
			breadcrumbs.add(new BreadcrumbItem(passport.getId(),passport.getAccename(),"passport"));
		}
		else if (object.equals("stock")) {
			Stock stock = stockRepository.findOne(id);
			Passport passport  = stock.getPassport();
			Taxonomy taxonomy = passport.getTaxonomy();
			breadcrumbs.add(new BreadcrumbItem(taxonomy.getId(),taxonomy.getGenus()+" "+taxonomy.getSpecies(),"taxonomy"));
			breadcrumbs.add(new BreadcrumbItem(passport.getId(),passport.getAccename(),"passport"));
			breadcrumbs.add(new BreadcrumbItem(stock.getId(),stock.getId().toString(),"stock"));
		}
		return breadcrumbs;
	}


	/*@Override
	public String getAppData() {
		List<UnitOfMeasure> unitOfMeasureValues = unitOfMeasureRepository.findAll();
		List<StatisticType> statisticTypeValues = statisticTypeRepository.findAll();
		String json;
		AutoBean<AppDataProxy> bean = helperFactory.appData();
		List<UnitOfMeasureProxy> unitOfMeasures = new ArrayList<UnitOfMeasureProxy>();
		List<StatisticTypeProxy> statisticTypes = new ArrayList<StatisticTypeProxy>();
		UnitOfMeasureProxy unitOfMeasureProxy = helperFactory.unitOfMeasure().as();
		unitOfMeasureProxy.setUnitType("");
		unitOfMeasures.add(unitOfMeasureProxy);
		for (UnitOfMeasure unitOfMeasure:unitOfMeasureValues) {
			unitOfMeasureProxy = helperFactory.unitOfMeasure().as();
			unitOfMeasureProxy.setId(unitOfMeasure.getId());
			unitOfMeasureProxy.setUnitType(unitOfMeasure.getUnitType());
			unitOfMeasures.add(unitOfMeasureProxy);
		}
		//bean.setUnitOfMeasureList(unitOfMeasures);
		for (StatisticType statisticType:statisticTypeValues) {
			StatisticTypeProxy statisticTypeProxy = helperFactory.statisticType().as();
			statisticTypeProxy.setId(statisticType.getId());
			statisticTypeProxy.setStatType(statisticType.getStatType());
			statisticTypes.add(statisticTypeProxy);
		}
		//bean.setStatisticTypeList(statisticTypes);
		//String key = ser.serialize(someProxy);
		// Create the flattened representation
		//String payload = store.encode();
		json = AutoBeanCodex.encode(bean).getPayload();
		return json;
		
	}*/




	@Override
	public AppData getAppData() {
		List<UnitOfMeasure> unitOfMeasureValues = unitOfMeasureRepository.findAll();
		List<StatisticType> statisticTypeValues = statisticTypeRepository.findAll();
		List<AlleleAssay> alleleAssayValues = alleleAssayRepository.findAll();
		List<StudyProtocol> studyProtocolValues = studyProtocolRepository.findAll();
		List<Sampstat> sampStatValues = sampstatRepository.findAll();
		AppData appData = new AppData();
		appData.setStatisticTypeList(statisticTypeValues);
		appData.setUnitOfMeasureList(unitOfMeasureValues);
		appData.setAlleleAssayList(alleleAssayValues);
		appData.setStudyProtocolList(studyProtocolValues);
		appData.setSampStatList(sampStatValues);
		return appData;
	}

}
