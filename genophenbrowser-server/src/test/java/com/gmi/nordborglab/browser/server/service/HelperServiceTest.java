package com.gmi.nordborglab.browser.server.service;

import com.gmi.nordborglab.browser.server.domain.AppData;
import com.gmi.nordborglab.browser.server.domain.BreadcrumbItem;
import com.gmi.nordborglab.browser.server.domain.cdv.Study;
import com.gmi.nordborglab.browser.server.domain.cdv.Transformation;
import com.gmi.nordborglab.browser.server.domain.germplasm.Passport;
import com.gmi.nordborglab.browser.server.domain.germplasm.Stock;
import com.gmi.nordborglab.browser.server.domain.germplasm.Taxonomy;
import com.gmi.nordborglab.browser.server.domain.observation.Experiment;
import com.gmi.nordborglab.browser.server.domain.phenotype.Trait;
import com.gmi.nordborglab.browser.server.domain.phenotype.TraitUom;
import com.gmi.nordborglab.browser.server.exceptions.CommandLineException;
import com.gmi.nordborglab.browser.server.math.Transformations;
import com.gmi.nordborglab.browser.server.repository.ExperimentRepository;
import com.gmi.nordborglab.browser.server.repository.PassportRepository;
import com.gmi.nordborglab.browser.server.repository.StockRepository;
import com.gmi.nordborglab.browser.server.repository.StudyRepository;
import com.gmi.nordborglab.browser.server.repository.TaxonomyRepository;
import com.gmi.nordborglab.browser.server.repository.TraitUomRepository;
import com.gmi.nordborglab.browser.server.rest.ExperimentUploadData;
import com.gmi.nordborglab.browser.server.rest.PhenotypeUploadData;
import com.gmi.nordborglab.browser.server.rest.SampleData;
import com.gmi.nordborglab.browser.server.testutils.BaseTest;
import com.gmi.nordborglab.browser.server.testutils.SecurityUtils;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class HelperServiceTest extends BaseTest {

    @Resource
    private HelperService service;

    @Resource
    private ExperimentRepository experimentRepository;

    @Resource
    private StockRepository stockRepository;

    @Resource
    private TaxonomyRepository taxonomyRepository;

    @Resource
    private PassportRepository passportRepository;

    @Resource
    private TraitUomRepository traitUomRepository;

    @Resource
    private StudyRepository studyRepository;


    @Before
    public void setUp() {

    }

    @After
    public void clearContext() {
        SecurityUtils.clearContext();
    }


    @Test
    public void testExperimentBreadcrumbs() {
        Experiment experiment = experimentRepository.findOne(1L);
        List<BreadcrumbItem> breadcrumbs = service.getBreadcrumbs(1L, "experiment");


        assertNotNull(breadcrumbs);
        assertEquals(1, breadcrumbs.size());
        BreadcrumbItem experimentItem = breadcrumbs.get(0);

        assertEquals(experiment.getId(), experimentItem.getId());
        assertEquals(experiment.getName(), experimentItem.getText());
        assertEquals("experiment", experimentItem.getType());
    }

    @Test
    public void testPhenotypeBreadCrumbs() {
        Experiment experiment = experimentRepository.findOne(1L);
        TraitUom trait = traitUomRepository.findOne(1L);
        List<BreadcrumbItem> breadcrumbs = service.getBreadcrumbs(1L, "phenotype");


        assertNotNull(breadcrumbs);
        assertEquals(2, breadcrumbs.size());
        BreadcrumbItem experimentItem = breadcrumbs.get(0);

        assertEquals(experiment.getId(), experimentItem.getId());
        assertEquals(experiment.getName(), experimentItem.getText());
        assertEquals("experiment", experimentItem.getType());

        BreadcrumbItem phenotypeItem = breadcrumbs.get(1);
        assertEquals(trait.getId(), phenotypeItem.getId());
        assertEquals(trait.getLocalTraitName(), phenotypeItem.getText());
        assertEquals("phenotype", phenotypeItem.getType());
    }

    @Test
    public void testStudyBreadcrumbs() {
        Experiment experiment = experimentRepository.findOne(1L);
        TraitUom trait = traitUomRepository.findOne(1L);
        Study study = studyRepository.findOne(1L);
        List<BreadcrumbItem> breadcrumbs = service.getBreadcrumbs(1L, "study");


        assertNotNull(breadcrumbs);
        assertEquals(3, breadcrumbs.size());
        BreadcrumbItem experimentItem = breadcrumbs.get(0);

        assertEquals(experiment.getId(), experimentItem.getId());
        assertEquals(experiment.getName(), experimentItem.getText());
        assertEquals("experiment", experimentItem.getType());

        BreadcrumbItem phenotypeItem = breadcrumbs.get(1);
        assertEquals(trait.getId(), phenotypeItem.getId());
        assertEquals(trait.getLocalTraitName(), phenotypeItem.getText());
        assertEquals("phenotype", phenotypeItem.getType());

        BreadcrumbItem studyItem = breadcrumbs.get(2);
        assertEquals(study.getId(), studyItem.getId());
        assertEquals(study.getName(), studyItem.getText());
        assertEquals("study", studyItem.getType());
    }

    @Test
    public void testStudyWizardBreadcrumbs() {
        Experiment experiment = experimentRepository.findOne(1L);
        TraitUom trait = traitUomRepository.findOne(1L);
        List<BreadcrumbItem> breadcrumbs = service.getBreadcrumbs(1L, "studywizard");


        assertNotNull(breadcrumbs);
        assertEquals(3, breadcrumbs.size());
        BreadcrumbItem experimentItem = breadcrumbs.get(0);

        assertEquals(experiment.getId(), experimentItem.getId());
        assertEquals(experiment.getName(), experimentItem.getText());
        assertEquals("experiment", experimentItem.getType());

        BreadcrumbItem phenotypeItem = breadcrumbs.get(1);
        assertEquals(trait.getId(), phenotypeItem.getId());
        assertEquals(trait.getLocalTraitName(), phenotypeItem.getText());
        assertEquals("phenotype", phenotypeItem.getType());

        BreadcrumbItem studyItem = breadcrumbs.get(2);
        assertEquals(trait.getId(), studyItem.getId());
        assertEquals("New Study", studyItem.getText());
        assertEquals("studywizard", studyItem.getType());
    }


    @Test
    public void testTaxonomyBreadcrumbs() {
        Taxonomy taxonomy = taxonomyRepository.findOne(1L);
        List<BreadcrumbItem> breadcrumbs = service.getBreadcrumbs(1L, "taxonomy");


        assertNotNull(breadcrumbs);
        assertEquals(1, breadcrumbs.size());
        BreadcrumbItem breadcrumbItem = breadcrumbs.get(0);

        assertEquals(taxonomy.getId(), breadcrumbItem.getId());
        assertEquals(taxonomy.getGenus() + " " + taxonomy.getSpecies(), breadcrumbItem.getText());
        assertEquals("taxonomy", breadcrumbItem.getType());
    }

    @Test
    public void testPassportsBreadcrumbs() {
        Taxonomy taxonomy = taxonomyRepository.findOne(1L);
        List<BreadcrumbItem> breadcrumbs = service.getBreadcrumbs(1L, "passports");
        assertNotNull(breadcrumbs);
        assertEquals(1, breadcrumbs.size());
        BreadcrumbItem breadcrumbItem = breadcrumbs.get(0);

        assertEquals(taxonomy.getId(), breadcrumbItem.getId());
        assertEquals(taxonomy.getGenus() + " " + taxonomy.getSpecies(), breadcrumbItem.getText());
        assertEquals("passports", breadcrumbItem.getType());
    }

    @Test
    public void testStockBreadcrumbs() {
        Stock stock = stockRepository.findOne(1L);
        Passport passport = stock.getPassport();
        Taxonomy taxonomy = passport.getTaxonomy();
        List<BreadcrumbItem> breadcrumbs = service.getBreadcrumbs(1L, "stock");
        assertNotNull(breadcrumbs);
        assertEquals(3, breadcrumbs.size());
        BreadcrumbItem breadcrumbItem = breadcrumbs.get(0);
        assertEquals(taxonomy.getId(), breadcrumbItem.getId());
        assertEquals(taxonomy.getGenus() + " " + taxonomy.getSpecies(), breadcrumbItem.getText());
        assertEquals("taxonomy", breadcrumbItem.getType());

        breadcrumbItem = breadcrumbs.get(1);
        assertEquals(passport.getId(), breadcrumbItem.getId());
        assertEquals(passport.getAccename(), breadcrumbItem.getText());
        assertEquals("passport", breadcrumbItem.getType());

        breadcrumbItem = breadcrumbs.get(2);
        assertEquals(stock.getId(), breadcrumbItem.getId());
        assertEquals(stock.getId().toString(), breadcrumbItem.getText());
        assertEquals("stock", breadcrumbItem.getType());
    }


    @Test
    public void testGetAppData() {
        SecurityUtils.setAnonymousUser();
        AppData data = service.getAppData();
        assertNotNull("could not retrive AppData", data);
        assertNotNull("Could not retrieve StatisticTypeList", data.getStatisticTypeList());
        assertTrue("no elements found for StatisticTypeList", data.getStatisticTypeList().size() > 0);

        assertNotNull("Could not retrieve StudyProtocolList", data.getStudyProtocolList());
        assertTrue("no elements found for StudyProtocolList", data.getStudyProtocolList().size() > 0);

        assertNotNull("Could not retrieve AlleleAssayList", data.getAlleleAssayList());
        assertTrue("no elements found for StatisticTypeList", data.getAlleleAssayList().size() > 0);

        assertNotNull("Could not retrieve UnitOfMeasureList", data.getUnitOfMeasureList());
        assertTrue("no elements found for UnitOfMeasureList", data.getUnitOfMeasureList().size() > 0);

        assertNotNull("Could not retrieve Sampstats", data.getSampStatList());
        assertTrue("no elements found for Sampstats", data.getSampStatList().size() > 0);

        assertNotNull("Could not retrieve Transformations", data.getTransformationList());
        assertTrue("no elements found for Transformations", data.getTransformationList().size() > 0);

        assertThat(data.getGWASRuntimeInfoList(), is(notNullValue()));
        assertThat(data.getGWASRuntimeInfoList().size(), is(not(0)));
    }


    @Test
    public void getExperimentUploadDataFromCsvWrongHeader() throws IOException {
        List<String[]> csvData = getPhenotypeWithWrongHeader();
        ExperimentUploadData data = service.getExperimentUploadDataFromCsv(getByteFromArray(csvData));
        assertThat(data, is(notNullValue()));
        assertThat(data.getErrorMessage(), is(notNullValue()));
    }


    @Test
    public void getExperimentUploadDataFromCsvParseError() throws IOException {
        List<String[]> csvData = getPhenotypeWithParseError();
        ExperimentUploadData data = service.getExperimentUploadDataFromCsv(getByteFromArray(csvData));
        assertThat(data, is(notNullValue()));
        assertThat(data.getSampleData().size(), is(4));
        assertThat(data.getPhenotypes().size(), is(1));

        assertThat(data.getSampleData().get(0).getSourceId(), nullValue());
        assertThat(data.getSampleData().get(0).getPassportId(), nullValue());
        assertThat(data.getSampleData().get(0).isParseError(), is(true));
        assertThat(data.getSampleData().get(0).hasError(), is(true));
        assertThat(data.getSampleData().get(0).getParseMask(), is(0));

        assertThat(data.getSampleData().get(1).getSourceId(), is("asdasd"));
        assertThat(data.getSampleData().get(1).getPassportId(), nullValue());
        assertThat(data.getSampleData().get(1).isParseError(), is(true));
        assertThat(data.getSampleData().get(1).hasError(), is(true));
        assertThat(data.getSampleData().get(1).getParseMask(), is(0));

        assertThat(data.getSampleData().get(2).getSourceId(), is("40000000"));
        assertThat(data.getSampleData().get(2).getPassportId(), nullValue());
        assertThat(data.getSampleData().get(2).isParseError(), is(false));
        assertThat(data.getSampleData().get(2).isIdKnown(), is(false));
        assertThat(data.getSampleData().get(2).hasError(), is(true));
        assertThat(data.getSampleData().get(2).getParseMask(), is(0));

        assertThat(data.getSampleData().get(3).getSourceId(), is("1"));
        assertThat(data.getSampleData().get(3).getPassportId(), is(1L));
        assertThat(data.getSampleData().get(3).isParseError(), is(false));
        assertThat(data.getSampleData().get(3).isIdKnown(), is(true));
        assertThat(data.getSampleData().get(3).hasError(), is(true));
        assertThat(data.getSampleData().get(3).getParseMask(), is(1 << 1));

        // check phenotype parse error
        PhenotypeUploadData phenotype = data.getPhenotypes().get(0);
        assertThat(phenotype.getValueCount(), is(4));
        assertThat(phenotype.getParseMask(), is(1 << 4));

        for (int i = 0; i < data.getSampleData().size(); i++) {
            SampleData sample = data.getSampleData().get(i);
            String csvValue = csvData.get((i + 1))[1];
            if ((sample.getParseMask() & (1 << 1)) == 0) {
                csvValue = String.valueOf(Double.parseDouble(csvValue));
            }
            assertThat(sample.getValues().get(0), is(csvValue));
        }
    }

    @Test
    public void getExperimentUploadDataFromCsv() throws IOException {
        List<String[]> csvData = getPhenotypeOkData();
        ExperimentUploadData data = service.getExperimentUploadDataFromCsv(getByteFromArray(csvData));
        assertThat(data, is(notNullValue()));
        assertThat(data.getErrorMessage(), is(nullValue()));
        assertThat(data.getSampleData().size(), is(4));
        assertThat(data.getPhenotypes().size(), is(3));
        // check samples
        int i = 1;
        for (SampleData sample : data.getSampleData()) {
            assertThat(sample.getSourceId(), is(csvData.get(i)[0]));
            assertThat(sample.getPassportId(), is(notNullValue()));
            assertThat(sample.isIdKnown(), is(true));
            assertThat(sample.isParseError(), is(false));
            assertThat(sample.hasError(), is(false));
            assertThat(sample.getValues().size(), is(csvData.get(i).length - 1));
            assertThat(sample.getParseMask(), is(0));
            if (!sample.hasIdError()) {
                assertThat(sample.getCountry(), notNullValue());
                assertThat(sample.getCountryShort(), notNullValue());
                assertThat(sample.getLatitude(), notNullValue());
                assertThat(sample.getLongitude(), notNullValue());
            }
            for (int j = 0; j < sample.getValues().size(); j++) {
                String value = sample.getValues().get(j);
                String csvValue = csvData.get(i)[j + 1];
                if (csvValue.isEmpty()) {
                    assertThat(value, nullValue());
                } else {
                    if (!csvValue.isEmpty() && !csvValue.equalsIgnoreCase("NA") && !csvValue.equalsIgnoreCase("N/A")) {
                        csvValue = String.valueOf(Double.parseDouble(csvValue));
                    }
                    assertThat(value, is(csvValue));
                }
            }
            i = i + 1;
        }
        i = 1;
        int[] phenotypeCount = new int[]{3, 4, 3};
        for (PhenotypeUploadData phen : data.getPhenotypes()) {
            assertThat(phen.getName(), is(csvData.get(0)[i]));
            assertThat(phen.getParseMask(), is(0));
            assertThat(phen.getValueCount(), is(phenotypeCount[i - 1]));
            i = i + 1;
        }
    }


    @Test
    public void testApplyTransformation() {
        Study study = getTestStudyWithTraits();
        service.applyTransformation(study);
        Transformations.LogTransformFunc logTransFunc = new Transformations.LogTransformFunc(1.0, 7.5);
        for (Trait trait : study.getTraits()) {
            assertThat(logTransFunc.apply(Double.valueOf(trait.getId())), is(Double.valueOf(trait.getValue())));
        }
    }


    @Test
    public void testGetPseudoHeritabilityFromTraitUom() throws CommandLineException {
        SecurityUtils.setAnonymousUser();
        TraitUom traitUom = traitUomRepository.findOne(5L);
        Transformation transformation = new Transformation();
        transformation.setName("log");
        Double pseudo = service.getPseudoHeritability(1L, traitUom, transformation);
        assertThat(pseudo, is(0.8788464607344941));

    }

    @Test
    public void testGetPseudoHeritabilityFromStudy() throws CommandLineException {
        SecurityUtils.setAnonymousUser();
        Study study = studyRepository.findOne(160L);
        Double pseudo = service.getPseudoHeritability(study);
        assertThat(pseudo, is(0.8788464607344941));
    }


    private Study getTestStudyWithTraits() {
        Study study = new Study();
        Transformation transformation = new Transformation();
        transformation.setName("log");
        study.setTransformation(transformation);
        Set<Trait> traits = new HashSet<>();
        for (int i = 1; i < 10; i++) {
            Trait trait = new Trait();
            trait.setId(Long.valueOf(i));
            trait.setValue(String.valueOf(i));
            traits.add(trait);
        }
        study.setTraits(traits);
        return study;
    }


    private List<String[]> getPhenotypeOkData() {
        List<String[]> data = Lists.newArrayList();
        data.add(new String[]{"accessionid", "Phen1", "Phen2", "Phen3",});
        data.add(new String[]{"1", "1.5", "1", "1"});
        data.add(new String[]{"1", "", "1", "1"});
        data.add(new String[]{"6008", "1", "1", ""});
        data.add(new String[]{"6008", "1", "1", "1"});
        return data;
    }

    private List<String[]> getPhenotypeWithWrongHeader() {
        List<String[]> data = Lists.newArrayList();
        data.add(new String[]{"", "Phen1", "", "Phen3",});
        return data;
    }

    private byte[] getByteFromArray(List<String[]> array) {
        StringBuilder b = new StringBuilder();
        for (String[] row : array) {
            b.append(Joiner.on(",").join(row));
            b.append("\n");
        }
        return b.toString().getBytes();
    }

    private List<String[]> getPhenotypeWithParseError() {
        List<String[]> data = Lists.newArrayList();
        data.add(new String[]{"accessionid", "Phen1"});
        data.add(new String[]{"", "1"});
        data.add(new String[]{"asdasd", "1"});
        data.add(new String[]{"40000000", "1"});
        data.add(new String[]{"1", "asd"});
        return data;
    }






}
