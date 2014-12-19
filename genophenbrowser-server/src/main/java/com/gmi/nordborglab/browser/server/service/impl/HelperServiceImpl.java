package com.gmi.nordborglab.browser.server.service.impl;

import com.gmi.nordborglab.browser.server.data.isatab.IsaTabExporter;
import com.gmi.nordborglab.browser.server.domain.AppData;
import com.gmi.nordborglab.browser.server.domain.BreadcrumbItem;
import com.gmi.nordborglab.browser.server.domain.acl.AppUser;
import com.gmi.nordborglab.browser.server.domain.cdv.Study;
import com.gmi.nordborglab.browser.server.domain.cdv.StudyProtocol;
import com.gmi.nordborglab.browser.server.domain.cdv.Transformation;
import com.gmi.nordborglab.browser.server.domain.genotype.AlleleAssay;
import com.gmi.nordborglab.browser.server.domain.germplasm.Passport;
import com.gmi.nordborglab.browser.server.domain.germplasm.Sampstat;
import com.gmi.nordborglab.browser.server.domain.germplasm.Stock;
import com.gmi.nordborglab.browser.server.domain.germplasm.Taxonomy;
import com.gmi.nordborglab.browser.server.domain.observation.Experiment;
import com.gmi.nordborglab.browser.server.domain.phenotype.StatisticType;
import com.gmi.nordborglab.browser.server.domain.phenotype.Trait;
import com.gmi.nordborglab.browser.server.domain.phenotype.TraitUom;
import com.gmi.nordborglab.browser.server.domain.phenotype.TransformationData;
import com.gmi.nordborglab.browser.server.domain.phenotype.UnitOfMeasure;
import com.gmi.nordborglab.browser.server.domain.stats.AppStat;
import com.gmi.nordborglab.browser.server.domain.stats.DateStatHistogram;
import com.gmi.nordborglab.browser.server.domain.stats.DateStatHistogramFacet;
import com.gmi.nordborglab.browser.server.domain.util.CandidateGeneList;
import com.gmi.nordborglab.browser.server.domain.util.GWASRuntimeInfo;
import com.gmi.nordborglab.browser.server.domain.util.NewsItem;
import com.gmi.nordborglab.browser.server.domain.util.Publication;
import com.gmi.nordborglab.browser.server.domain.util.UserNotification;
import com.gmi.nordborglab.browser.server.math.Transformations;
import com.gmi.nordborglab.browser.server.repository.AlleleAssayRepository;
import com.gmi.nordborglab.browser.server.repository.CandidateGeneListRepository;
import com.gmi.nordborglab.browser.server.repository.ExperimentRepository;
import com.gmi.nordborglab.browser.server.repository.GWASRuntimeInfoRepository;
import com.gmi.nordborglab.browser.server.repository.NewsRepository;
import com.gmi.nordborglab.browser.server.repository.PassportRepository;
import com.gmi.nordborglab.browser.server.repository.PublicationRepository;
import com.gmi.nordborglab.browser.server.repository.SampstatRepository;
import com.gmi.nordborglab.browser.server.repository.StatisticTypeRepository;
import com.gmi.nordborglab.browser.server.repository.StockRepository;
import com.gmi.nordborglab.browser.server.repository.StudyProtocolRepository;
import com.gmi.nordborglab.browser.server.repository.StudyRepository;
import com.gmi.nordborglab.browser.server.repository.TaxonomyRepository;
import com.gmi.nordborglab.browser.server.repository.TraitUomRepository;
import com.gmi.nordborglab.browser.server.repository.TransformationRepository;
import com.gmi.nordborglab.browser.server.repository.UnitOfMeasureRepository;
import com.gmi.nordborglab.browser.server.repository.UserNotificationRepository;
import com.gmi.nordborglab.browser.server.repository.UserRepository;
import com.gmi.nordborglab.browser.server.rest.ExperimentUploadData;
import com.gmi.nordborglab.browser.server.rest.PhenotypeUploadData;
import com.gmi.nordborglab.browser.server.rest.PhenotypeUploadValue;
import com.gmi.nordborglab.browser.server.security.EsAclManager;
import com.gmi.nordborglab.browser.server.security.SecurityUtil;
import com.gmi.nordborglab.browser.server.service.HelperService;
import com.gmi.nordborglab.browser.shared.proxy.AppStatProxy;
import com.gmi.nordborglab.browser.shared.proxy.DateStatHistogramFacetProxy;
import com.gmi.nordborglab.browser.shared.proxy.DateStatHistogramProxy;
import com.gmi.nordborglab.browser.shared.proxy.TransformationDataProxy;
import com.gmi.nordborglab.browser.shared.service.HelperFactory;
import com.gmi.nordborglab.jpaontology.repository.TermRepository;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.elasticsearch.action.search.MultiSearchRequestBuilder;
import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.facet.FacetBuilder;
import org.elasticsearch.search.facet.FacetBuilders;
import org.elasticsearch.search.facet.Facets;
import org.elasticsearch.search.facet.datehistogram.DateHistogramFacet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.supercsv.cellprocessor.CellProcessorAdaptor;
import org.supercsv.cellprocessor.ParseDouble;
import org.supercsv.cellprocessor.ParseInt;
import org.supercsv.cellprocessor.ParseLong;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.cellprocessor.ift.StringCellProcessor;
import org.supercsv.exception.SuperCsvCellProcessorException;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.ICsvListReader;
import org.supercsv.prefs.CsvPreference;
import org.supercsv.util.CsvContext;

import javax.annotation.Nullable;
import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class HelperServiceImpl implements HelperService {

    private static HelperFactory helperFactory = AutoBeanFactorySource.create(HelperFactory.class);

    private static class ParsePhenotypeValue extends CellProcessorAdaptor {


        public static enum PHENOTYPE_VALUE_TYPES {
            MEASURE, MEAN, STD, VARIANCE, MODE, MEDIAN, COUNT;

        }

        private ParsePhenotypeValue() {
            super();
        }

        private ParsePhenotypeValue(CellProcessor next) {
            super(next);
        }

        @Override
        public Object execute(Object value, CsvContext context) {
            validateInputNotNull(value, context);
            for (PHENOTYPE_VALUE_TYPES type : PHENOTYPE_VALUE_TYPES.values()) {
                if (type.name().equalsIgnoreCase(value.toString())) {
                    return next.execute(value, context);
                }
            }
            throw new SuperCsvCellProcessorException(String.format("Could not parse '%s' as a phenotype value type", value), context, this);
        }

    }

    private static class SupressException extends CellProcessorAdaptor {

        private SuperCsvCellProcessorException suppressedException;
        private Object value;

        public SupressException(CellProcessor next) {
            super(next);
        }

        public Object execute(Object value, CsvContext context) {
            try {
                // attempt to execute the next processor
                this.value = value;
                return next.execute(value, context);

            } catch (SuperCsvCellProcessorException e) {
                // save the exception
                if (value == null) {
                    this.value = "MISSING";
                }
                suppressedException = e;
            } finally {

            }
            // and suppress it (null is written as "")
            return value;
        }


        public SuperCsvCellProcessorException getSuppressedException() {
            return suppressedException;
        }

        public Object getValue() {
            return value;
        }

        public void reset() {
            suppressedException = null;
            value = null;
        }
    }

    @Resource
    private IsaTabExporter isaTabExporter;

    @Resource
    private CandidateGeneListRepository candidateGeneListRepository;

    @Resource
    private UserRepository userRepository;

    @Resource
    private ExperimentRepository experimentRepository;

    @Resource
    private UserNotificationRepository userNotificationRepository;

    @Resource
    private TermRepository termRepository;

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
    private TransformationRepository transformationRepository;

    @Resource
    private TraitUomRepository traitUomRepository;

    @Resource
    private StudyRepository studyRepository;

    @Resource
    private PublicationRepository publicationRepository;

    @Resource
    private NewsRepository newsRepository;

    @Resource
    private GWASRuntimeInfoRepository gwasRuntimeInfoRepository;

    @Resource
    private Client client;

    @Resource
    protected EsAclManager esAclManager;

    private final Map<ParsePhenotypeValue.PHENOTYPE_VALUE_TYPES, CellProcessor> phenotype2CellProcessors;


    public HelperServiceImpl() {
        phenotype2CellProcessors = ImmutableMap.<ParsePhenotypeValue.PHENOTYPE_VALUE_TYPES, CellProcessor>builder()
                .put(ParsePhenotypeValue.PHENOTYPE_VALUE_TYPES.MEAN, new ParseDouble())
                .put(ParsePhenotypeValue.PHENOTYPE_VALUE_TYPES.MEASURE, new ParseDouble())
                .put(ParsePhenotypeValue.PHENOTYPE_VALUE_TYPES.STD, new ParseDouble())
                .put(ParsePhenotypeValue.PHENOTYPE_VALUE_TYPES.MODE, new ParseInt())
                .put(ParsePhenotypeValue.PHENOTYPE_VALUE_TYPES.COUNT, new ParseInt())
                .put(ParsePhenotypeValue.PHENOTYPE_VALUE_TYPES.VARIANCE, new ParseDouble())
                .put(ParsePhenotypeValue.PHENOTYPE_VALUE_TYPES.MEDIAN, new ParseDouble()).build();
    }

    @Override
    public List<BreadcrumbItem> getBreadcrumbs(Long id, String object) {
        List<BreadcrumbItem> breadcrumbs = new ArrayList<BreadcrumbItem>();
        if (object.equals("experiment") || object.equals("phenotypes")) {
            Experiment exp = experimentRepository.findOne(id);
            breadcrumbs.add(new BreadcrumbItem(exp.getId(), exp.getName(), "experiment"));
        } else if (object.equals("phenotype")) {
            Experiment exp = experimentRepository.findByPhenotypeId(id);
            TraitUom traitUom = traitUomRepository.findOne(id);
            breadcrumbs.add(new BreadcrumbItem(exp.getId(), exp.getName(), "experiment"));
            breadcrumbs.add(new BreadcrumbItem(traitUom.getId(), traitUom.getLocalTraitName(), "phenotype"));
        } else if (object.equals("study")) {
            Study study = studyRepository.findOne(id);
            TraitUom trait = Iterables.get(study.getTraits(), 0).getTraitUom();
            Experiment exp = Iterables.get(study.getTraits(), 0).getObsUnit().getExperiment();
            breadcrumbs.add(new BreadcrumbItem(exp.getId(), exp.getName(), "experiment"));
            breadcrumbs.add(new BreadcrumbItem(trait.getId(), trait.getLocalTraitName(), "phenotype"));
            breadcrumbs.add(new BreadcrumbItem(study.getId(), study.getName(), "study"));
        } else if (object.equals("studywizard")) {
            Experiment exp = experimentRepository.findByPhenotypeId(id);
            TraitUom trait = traitUomRepository.findOne(id);
            breadcrumbs.add(new BreadcrumbItem(exp.getId(), exp.getName(), "experiment"));
            breadcrumbs.add(new BreadcrumbItem(trait.getId(), trait.getLocalTraitName(), "phenotype"));
            breadcrumbs.add(new BreadcrumbItem(trait.getId(), "New Study", "studywizard"));
        } else if (object.equals("taxonomy")) {
            Taxonomy tax = taxonomRepository.findOne(id);
            breadcrumbs.add(new BreadcrumbItem(tax.getId(), tax.getGenus() + " " + tax.getSpecies(), "taxonomy"));
        } else if (object.equals("passports")) {
            Taxonomy tax = taxonomRepository.findOne(id);
            breadcrumbs.add(new BreadcrumbItem(tax.getId(), tax.getGenus() + " " + tax.getSpecies(), "passports"));
        } else if (object.equals("passport")) {
            Passport passport = passportRepository.findOne(id);
            Taxonomy taxonomy = passport.getTaxonomy();
            breadcrumbs.add(new BreadcrumbItem(taxonomy.getId(), taxonomy.getGenus() + " " + taxonomy.getSpecies(), "taxonomy"));
            breadcrumbs.add(new BreadcrumbItem(passport.getId(), passport.getAccename(), "passport"));
        } else if (object.equals("stock")) {
            Stock stock = stockRepository.findOne(id);
            Passport passport = stock.getPassport();
            Taxonomy taxonomy = passport.getTaxonomy();
            breadcrumbs.add(new BreadcrumbItem(taxonomy.getId(), taxonomy.getGenus() + " " + taxonomy.getSpecies(), "taxonomy"));
            breadcrumbs.add(new BreadcrumbItem(passport.getId(), passport.getAccename(), "passport"));
            breadcrumbs.add(new BreadcrumbItem(stock.getId(), stock.getId().toString(), "stock"));
        } else if (object.equalsIgnoreCase("publication")) {
            Publication publication = publicationRepository.findOne(id);
            breadcrumbs.add(new BreadcrumbItem(publication.getId(), publication.getFirstAuthor() + " - " + publication.getTitle(), "publication"));
        } else if (object.equalsIgnoreCase("candidategenelist")) {
            CandidateGeneList candidateGeneList = candidateGeneListRepository.findOne(id);
            breadcrumbs.add(new BreadcrumbItem(candidateGeneList.getId(), candidateGeneList.getName(), "candidategenelist"));
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
        List<Transformation> transformationValues = transformationRepository.findAll();
        List<UserNotification> userNotifications = getUserNotifications(10);
        List<GWASRuntimeInfo> gwasRuntimeInfos = gwasRuntimeInfoRepository.findAll();
        Page<NewsItem> page = newsRepository.findAll(new PageRequest(0, 10, Sort.Direction.DESC, "createDate"));

        AppData appData = new AppData();
        appData.setStatisticTypeList(statisticTypeValues);
        appData.setUnitOfMeasureList(unitOfMeasureValues);
        appData.setAlleleAssayList(alleleAssayValues);
        appData.setStudyProtocolList(studyProtocolValues);
        appData.setSampStatList(sampStatValues);
        appData.setTransformationList(transformationValues);
        appData.setUserNotificationList(userNotifications);
        appData.setStats(getAppStats());
        appData.setGWASRuntimeInfoList(gwasRuntimeInfos);
        appData.setNews(page.getContent());
        return appData;
    }


    @Override
    public PhenotypeUploadData getPhenotypeUploadData(byte[] csvData) throws IOException {
        PhenotypeUploadData data = new PhenotypeUploadData();
        ICsvListReader metaInformationReader = null;
        ICsvListReader valueHeaderReader = null;
        ICsvListReader valueReader = null;
        boolean hasMetaInfo = false;
        try {
            metaInformationReader = new CsvListReader(new InputStreamReader(new ByteArrayInputStream(csvData)), CsvPreference.STANDARD_PREFERENCE);
            String[] valueHeader = null;
            final String[] metaHeader = metaInformationReader.getHeader(true);
            hasMetaInfo = (metaHeader.length > 0 && metaHeader[0].equals("#HEADER"));
            if (hasMetaInfo) {
                final Map<String, String> metaInfo = getMetaInformationFromHeader(metaHeader);
                updatePhenotypeUploadDataWithMetaInformation(data, metaInfo);
                valueHeader = metaInformationReader.getHeader(false);
            } else {
                valueHeader = metaHeader;
            }


            final int columnCount = valueHeader.length;
            List<String> headers = Arrays.asList(valueHeader).subList(1, valueHeader.length);
            data.setValueHeader(headers);

            CellProcessor[] valueHeaderCellProccessors = createValueHeaderCellProcessors(columnCount);

            valueHeaderReader = new CsvListReader(new InputStreamReader(new ByteArrayInputStream(csvData)), CsvPreference.STANDARD_PREFERENCE);
            if (hasMetaInfo)
                valueHeaderReader.getHeader(true);
            valueHeaderReader.read(valueHeaderCellProccessors);

            valueReader = new CsvListReader(new InputStreamReader(new ByteArrayInputStream(csvData)), CsvPreference.STANDARD_PREFERENCE);
            if (hasMetaInfo)
                valueReader.getHeader(true);
            valueReader.getHeader(false);
            CellProcessor[] valueCellProcessors = createValueCellProcessors(headers);
            List<Object> phenotypeValues = null;
            while ((phenotypeValues = valueReader.read(valueCellProcessors)) != null) {
                PhenotypeUploadValue value = parseAndCheckPhenotypeValue(phenotypeValues, getParseErrorFromProcessor(valueCellProcessors));
                if (value != null)
                    data.addPhenotypeValue(value);
            }
            data.sortByErrors();

        } catch (SuperCsvCellProcessorException e) {
            data.setErrorMessage(String.format("Error parsing header. '%s'", e.getMessage()));
        } catch (Exception e) {
            data.setErrorMessage("General error reading csv file");
        } finally {
            if (metaInformationReader != null)
                metaInformationReader.close();
        }
        return data;
    }


    private boolean getParseErrorFromProcessor(CellProcessor[] processors) {
        boolean hasError = false;
        for (CellProcessor processor : processors) {
            if (processor instanceof SupressException) {
                SupressException cell = (SupressException) processor;
                if (cell.getSuppressedException() != null)
                    hasError = true;
                cell.reset();
            }
        }
        return hasError;
    }

    @Override
    public List<String> getGenesFromCanddiateGeneListUpload(byte[] inputStream) throws IOException {
        List<String> geneIds = Lists.newArrayList();
        ICsvListReader csvReader = null;
        try {
            csvReader = new CsvListReader(new InputStreamReader(new ByteArrayInputStream(inputStream)), CsvPreference.STANDARD_PREFERENCE);
            CellProcessor[] cellProcessors = {new NotNull()};
            List<String> phenotypeValues = null;
            while ((phenotypeValues = csvReader.read()) != null) {
                geneIds.add(phenotypeValues.get(0).toUpperCase());
            }

        } catch (SuperCsvCellProcessorException e) {
            geneIds = null;

        } catch (Exception e) {
            geneIds = null;
        } finally {
            if (csvReader != null)
                csvReader.close();
        }
        return geneIds;
    }

    @Override
    public List<TransformationData> calculateTransformations(List<Double> values) {
        List<TransformationData> transformations = Lists.newArrayList();
        transformations.add(new TransformationData(TransformationDataProxy.TYPE.LOG, Transformations.logTransform(values)));
        transformations.add(new TransformationData(TransformationDataProxy.TYPE.SQRT, Transformations.sqrtTransform(values)));
        transformations.add(new TransformationData(TransformationDataProxy.TYPE.BOXCOX, Transformations.boxCoxTransform(values)));
        return transformations;
    }

    @Override
    public List<UserNotification> getUserNotifications(Integer limit) {
        AppUser appUser = null;
        try {
            appUser = userRepository.findOne(Long.parseLong(SecurityUtil.getUsername()));
        } catch (Exception e) {

        }

        if (appUser == null)
            return null;
        List<UserNotification> notifications = userNotificationRepository.findByAppUserIdOrAppUserIsNullOrderByIdDesc(Long.parseLong(SecurityUtil.getUsername()));
        return filterUserNotifications(notifications, appUser.getNotificationCheckDate(), limit);
    }

    private static List<UserNotification> filterUserNotifications(List<UserNotification> notifications, Date modificationCheckDate, Integer limit) {
        List<UserNotification> recentNotifications = Lists.newArrayList();
        for (int i = 0; i < notifications.size(); i++) {
            UserNotification notification = notifications.get(i);
            if (!notification.isRead(modificationCheckDate) || limit == null || (limit != null && limit > recentNotifications.size())) {
                recentNotifications.add(notification);
            }
        }
        return recentNotifications;
    }

    public PhenotypeUploadValue parseAndUpdateAccession(PhenotypeUploadValue value) {
        boolean isIdKnown = false;
        try {
            Long id = Long.parseLong(value.getSourceId());
            Long passportId = null;
            Long stockId = null;
            String accessionName = null;
            if (passportRepository.exists(id)) {
                Passport passport = passportRepository.findOne(id);
                passportId = id;
                isIdKnown = true;
                accessionName = passport.getAccename();

            } else if (stockRepository.exists(id)) {
                Stock stock = stockRepository.findOne(id);
                stockId = id;
                isIdKnown = true;
                passportId = stock.getPassport().getId();
                accessionName = stock.getPassport().getAccename();
            }
            value.setAccessionName(accessionName);
            value.setStockId(stockId);
            value.setPassportId(passportId);
            value.setIdKnown(isIdKnown);
        } catch (Exception e) {
            value.setParseError(true);
        }

        return value;
    }


    private PhenotypeUploadValue parseAndCheckPhenotypeValue(List<Object> phenotypeValues, boolean valueError) {
        PhenotypeUploadValue parsedValue = new PhenotypeUploadValue();
        try {
            parsedValue.setParseError(valueError);
            Long sourceId = (Long) phenotypeValues.get(0);
            List<String> values = Lists.newArrayList();
            for (Object obj : phenotypeValues) {
                values.add(String.valueOf(obj));
            }
            parsedValue.setValues(values.subList(1, values.size()));
            parsedValue.setSourceId(sourceId.toString());
            parsedValue = parseAndUpdateAccession(parsedValue);
        } catch (Exception e) {
            parsedValue.setParseError(true);
        }
        return parsedValue;
    }

    private void updatePhenotypeUploadDataWithMetaInformation(PhenotypeUploadData data, Map<String, String> metaInfo) {
        if (metaInfo.containsKey("name"))
            data.setName(metaInfo.get("name"));
        //TODO retrieve unitofMeasureFromText
        if (metaInfo.containsKey("unitofmeasure"))
            data.setUnitOfMeasure(metaInfo.get("unitofmeasure"));
        if (metaInfo.containsKey("protocol"))
            data.setProtocol(metaInfo.get("protocol"));
        if (metaInfo.containsKey("traitontology"))
            data.setTraitOntology(termRepository.findByAcc(metaInfo.get("traitontology")));
        if (metaInfo.containsKey("environmentontology"))
            data.setEnvironmentOntology(termRepository.findByAcc(metaInfo.get("environmentontology")));
    }

    private static Map<String, String> getMetaInformationFromHeader(String[] header) {
        Map<String, String> metaInfo = new HashMap<String, String>();
        for (int i = 1; i < header.length; i++) {
            String[] metaSplit = header[i].split("=");
            metaInfo.put(metaSplit[0].toLowerCase(), metaSplit[1]);
        }
        return metaInfo;
    }

    private static CellProcessor[] createValueHeaderCellProcessors(int valueColumns) {
        StringCellProcessor valueHeaderCell = new NotNull(new ParsePhenotypeValue());
        CellProcessor[] processors = new CellProcessor[valueColumns];
        processors[0] = null;
        for (int i = 1; i < valueColumns; i++) {
            processors[i] = valueHeaderCell;
        }
        return processors;
    }

    private CellProcessor[] createValueCellProcessors(List<String> header) {
        CellProcessor processor = null;
        CellProcessor[] processors = new CellProcessor[header.size() + 1];
        processors[0] = new NotNull(new ParseLong());
        for (int i = 1; i < processors.length; i++) {
            try {
                processor = phenotype2CellProcessors.get(ParsePhenotypeValue.PHENOTYPE_VALUE_TYPES.valueOf(header.get(i - 1)));
            } catch (Exception e) {

            }
            if (processor == null) {
                processor = new ParseDouble();
            }
            SupressException valueCell = new SupressException(new NotNull(processor));
            processors[i] = valueCell;
        }
        //TODO properly implement parse issues on the backend.
        return processors;

    }

    private UnitOfMeasure getUnitOfMeasureFromText(String text) {
        List<UnitOfMeasure> unitOfmeasures = unitOfMeasureRepository.findAll();
        for (UnitOfMeasure unitOfMeasure : unitOfmeasures) {
            if (unitOfMeasure.getUnitType().equalsIgnoreCase(text))
                return unitOfMeasure;
        }
        return null;
    }

    @Override
    public List<AppStat> getAppStats() {
        List<AppStat> stats = Lists.newArrayList();
        MultiSearchRequestBuilder requestBuilder = client.prepareMultiSearch();
        // user count
        requestBuilder.add(getStatsSearchBuilder().setTypes("user"));

        FilterBuilder filter = esAclManager.getAclFilter(Lists.newArrayList("read"));
        //  get studies
        requestBuilder.add(getStatsSearchBuilder().setTypes("experiment").setQuery(QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(), filter)));
        requestBuilder.add(getStatsSearchBuilder().setTypes("phenotype").setQuery(QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(), filter)));
        requestBuilder.add(getStatsSearchBuilder().setTypes("study").setQuery(QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(), filter)));
        requestBuilder.add(getStatsSearchBuilder().setTypes("publication"));

        MultiSearchResponse multiResponse = requestBuilder.execute().actionGet();

        stats.add(new AppStat(AppStatProxy.STAT.USER, multiResponse.getResponses()[0].getResponse().getHits().getTotalHits()));
        stats.add(new AppStat(AppStatProxy.STAT.STUDY, multiResponse.getResponses()[1].getResponse().getHits().getTotalHits()));
        stats.add(new AppStat(AppStatProxy.STAT.PHENOTYPE, multiResponse.getResponses()[2].getResponse().getHits().getTotalHits()));
        stats.add(new AppStat(AppStatProxy.STAT.ANALYSIS, multiResponse.getResponses()[3].getResponse().getHits().getTotalHits()));
        stats.add(new AppStat(AppStatProxy.STAT.PUBLICATION, multiResponse.getResponses()[4].getResponse().getHits().getTotalHits()));
        stats.add(new AppStat(AppStatProxy.STAT.ONTOLOGY, 2));
        return stats;
    }

    private SearchRequestBuilder getStatsSearchBuilder() {
        return client.prepareSearch(esAclManager.getIndex()).setSearchType(SearchType.COUNT);
    }

    @Override
    public List<DateStatHistogramFacet> findRecentTraitHistogram(DateStatHistogramProxy.INTERVAL interval) {
        List<DateStatHistogramFacet> histogram = Lists.newArrayList();
        FacetBuilder facet = FacetBuilders.dateHistogramFacet("recent").field("published").interval(interval.name().toLowerCase());
        FilterBuilder filter = esAclManager.getAclFilter(Lists.newArrayList("read"));

        MultiSearchRequestBuilder requestBuilder = client.prepareMultiSearch();

        // experiments
        requestBuilder.add(
                client.prepareSearch(esAclManager.getIndex()).setTypes("experiment")
                        .addFacet(facet)
                        .setQuery(QueryBuilders.constantScoreQuery(filter))
                        .setSize(0)
        );
        //phenotypes
        requestBuilder.add(
                client.prepareSearch(esAclManager.getIndex()).setTypes("phenotype")
                        .addFacet(facet)
                        .setQuery(QueryBuilders.constantScoreQuery(filter))
                        .setSize(0)
        );

        //study
        requestBuilder.add(
                client.prepareSearch(esAclManager.getIndex()).setTypes("study")
                        .addFacet(facet)
                        .setQuery(QueryBuilders.constantScoreQuery(filter))
                        .setSize(0)
        );

        MultiSearchResponse multiResponse = requestBuilder.execute().actionGet();
        histogram.add(new DateStatHistogramFacet(getHistogram(multiResponse.getResponses()[0].getResponse().getFacets(), interval), DateStatHistogramFacetProxy.TYPE.study));
        histogram.add(new DateStatHistogramFacet(getHistogram(multiResponse.getResponses()[1].getResponse().getFacets(), interval), DateStatHistogramFacetProxy.TYPE.phenotype));
        histogram.add(new DateStatHistogramFacet(getHistogram(multiResponse.getResponses()[2].getResponse().getFacets(), interval), DateStatHistogramFacetProxy.TYPE.analysis));
        return histogram;
    }

    @Override
    public Study applyTransformation(Study study) {
        DescriptiveStatistics stats = Transformations.getDescriptiveStatistics(Collections2.transform(study.getTraits(), new Function<Trait, Double>() {

            @Nullable
            @Override
            public Double apply(@Nullable Trait trait) {
                Double value = null;
                try {
                    value = Double.parseDouble(trait.getValue());
                } catch (Exception e) {

                }
                return value;
            }
        }));
        Transformations.TransformFunc transFormFunc = Transformations.getTransformFunc(study.getTransformation().getName(), stats.getMin(), stats.getVariance());
        if (transFormFunc != null) {
            for (Trait trait : study.getTraits()) {
                try {
                    Double value = Double.parseDouble(trait.getValue());
                    trait.setValue(transFormFunc.apply(value).toString());
                } catch (Exception e) {

                }
            }
        }
        return study;
    }

    @Override
    public ExperimentUploadData getExperimentUploadData(byte[] isaTabData) {
        return isaTabExporter.getExperimentUploadDataFromArchive(isaTabData);
    }


    private List<DateStatHistogram> getHistogram(Facets facets, DateStatHistogramProxy.INTERVAL interval) {
        DateHistogramFacet searchFacet = (DateHistogramFacet) facets.facetsAsMap().get("recent");
        List<DateStatHistogram> dates = Lists.newArrayList();
        for (DateHistogramFacet.Entry termEntry : searchFacet) {
            dates.add(new DateStatHistogram(new Date(termEntry.getTime()), termEntry.getCount(), interval));
        }
        return dates;
    }

}
