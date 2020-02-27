package com.gmi.nordborglab.browser.server.data.isatab;

import com.gmi.nordborglab.browser.server.domain.germplasm.Passport;
import com.gmi.nordborglab.browser.server.domain.observation.Experiment;
import com.gmi.nordborglab.browser.server.domain.phenotype.Trait;
import com.gmi.nordborglab.browser.server.repository.ExperimentRepository;
import com.gmi.nordborglab.browser.server.repository.TraitRepository;
import com.gmi.nordborglab.browser.server.rest.ExperimentUploadData;
import com.gmi.nordborglab.browser.server.rest.PhenotypeUploadData;
import com.gmi.nordborglab.browser.server.rest.SampleData;
import com.gmi.nordborglab.browser.server.testutils.BaseTest;
import com.gmi.nordborglab.browser.server.testutils.SecurityUtils;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Ordering;
import com.google.common.io.Files;
import com.google.common.io.LineProcessor;
import org.isatools.isacreator.model.Investigation;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Nullable;
import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipFile;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * Created by uemit.seren on 4/3/14.
 */
public class IsaTabExporterTest extends BaseTest {

    @Resource
    private IsaTabExporter isaTabExporter;

    @Resource
    private ExperimentRepository experimentRepository;

    @Resource
    private TraitRepository traitRepository;


    @Before
    public void setUp() {
        SecurityUtils.setAnonymousUser();
    }

    @After
    public void clearContext() {
        SecurityUtils.clearContext();
    }

    @Test
    public void testCreateInvestigation() {
        Experiment experiment = experimentRepository.findOne(1L);
        Investigation investigation = isaTabExporter.createInvestigation(experiment, "/tmp");
        assertThat(investigation, is(notNullValue()));
        checkInvestigation(investigation, experiment);
    }

    @Test
    public void testSave() throws IOException {
        Experiment experiment = experimentRepository.findOne(1L);
        String baseDirectory = isaTabExporter.save(experiment);
        assertThat(baseDirectory, is(notNullValue()));
        checkFileContents(baseDirectory, experiment.getObsUnits().size() + 1, 108);
    }

    @Test
    public void testSaveWithZip() throws IOException {
        Experiment experiment = experimentRepository.findOne(1L);
        String zipFile = isaTabExporter.save(experiment, true);
        assertThat(zipFile, is(notNullValue()));
        checkZip(zipFile, experiment);
    }



    @Test
    public void testImportFromZip() throws IOException {
        Experiment experiment = experimentRepository.findOne(1L);
        ExperimentUploadData data = isaTabExporter.getExperimentUploadDataFromArchive(isaTabExporter.save(experiment));
        assertNotNull(data);
        assertThat(data.getPhenotypes().size(), is(107));
        assertThat(data.getName().trim(), is(String.format("%s - %s", "1", experiment.getName())));
        assertThat(data.getDoi(), is("10.1038/nature08800"));
        assertThat(data.getOriginator().trim(), is("Admin"));
        assertThat(data.getDescription(), is(experiment.getDesign()));
        //assertThat(data.getCreated(), is(experiment.getCreated()));
        //assertThat(data.getPublished(), is(experiment.getPublished()));
        PhenotypeUploadData phenotype = data.getPhenotypes().get(0);
        assertThat(phenotype.getName(), is("LD"));
        assertThat(phenotype.getProtocol(), is("Number of days following stratification to opening of first flower. The experiment was stopped at 200 d, and accessions that had not flowered at that point were assigned a value of 200"));
        assertThat(phenotype.getValueCount(), is(167));
        assertThat(phenotype.getParseMask(), is(0));
        assertThat(phenotype.getUnitOfMeasure(), is("days"));
        assertThat(phenotype.getTraitOntology(), is("TO:0000344"));

        List<Trait> traits = traitRepository.findByTraitUomIdAndStatisticTypeId(1L, 2L);

        Collections.sort(traits,
                Ordering.natural().onResultOf(new Function<Trait, Long>() {
                    @Nullable
                    @Override
                    public Long apply(Trait input) {
                        return input.getObsUnit().getStock().getPassport().getId();
                    }
                })
        );
        List<SampleData> samples = FluentIterable.from(data.getSampleData()).filter(new Predicate<SampleData>() {
            @Override
            public boolean apply(@Nullable SampleData input) {
                return input.getValues().get(0) != null;
            }
        }).toSortedList(Ordering.natural().onResultOf(new Function<SampleData, Comparable>() {
            @Nullable
            @Override
            public Long apply(@Nullable SampleData input) {
                return input.getPassportId();
            }
        }));
        assertThat(traits.size(), is(samples.size()));
        for (int i = 0; i < traits.size(); i++) {
            Trait trait = traits.get(i);
            Passport passport = trait.getObsUnit().getStock().getPassport();
            SampleData sample = samples.get(i);
            assertThat(sample.getSourceId(), is(passport.getId().toString()));
            assertThat(sample.getPassportId(), is(passport.getId()));
            assertThat(sample.getAccessionName(), is(passport.getAccename()));
            assertThat(sample.getValues().get(0), is(trait.getValue()));
        }
    }

    private void checkZip(String zipFileName, Experiment experiment) throws IOException {
        File file = new File(zipFileName);
        assertThat(file.exists(), is(true));
        assertThat(file.isFile(), is(true));
        ZipFile zipFile = new ZipFile(file);
        assertThat(zipFile.size(), is(5));
    }


    private void checkInvestigation(Investigation investigation, Experiment experiment) {

    }

    private void checkFileContents(String baseDirectory, int sampleCount, int traitCount) {
        File directory = new File(baseDirectory);
        File investigationFile = new File(baseDirectory + File.separator + IsaTabExporterImpl.INVESTIGATION_FILE);
        File studyFile = new File(baseDirectory + File.separator + IsaTabExporterImpl.STUDY_FILE);
        File assayFile = new File(baseDirectory + File.separator + IsaTabExporterImpl.ASSAY_FILE);
        File traitDefFile = new File(baseDirectory + File.separator + IsaTabExporterImpl.TRAIT_DEF_FILE);
        File dervivedFile = new File(baseDirectory + File.separator + IsaTabExporterImpl.DERVIVED_DATA_FILE);

        assertThat(directory.exists(), is(true));
        assertThat(investigationFile.exists(), is(true));
        assertThat(studyFile.exists(), is(true));
        assertThat(assayFile.exists(), is(true));
        assertThat(traitDefFile.exists(), is(true));
        assertThat(dervivedFile.exists(), is(true));

        assertThat(getLineCountFromFile(studyFile), is(sampleCount));
        assertThat(getLineCountFromFile(assayFile), is(sampleCount));
        assertThat(getLineCountFromFile(traitDefFile), is(traitCount));
        assertThat(getLineCountFromFile(dervivedFile), is(sampleCount));
    }

    private int getLineCountFromFile(File file) {
        try {
            return Files.readLines(file, Charset.defaultCharset(), new LineProcessor<Integer>() {
                int count = 0;

                public Integer getResult() {
                    return count;
                }

                public boolean processLine(String line) {
                    count++;
                    return true;
                }
            });
        } catch (IOException e) {

        } finally {

        }
        return 0;
    }
}
