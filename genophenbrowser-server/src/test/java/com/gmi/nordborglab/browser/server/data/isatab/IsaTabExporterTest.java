package com.gmi.nordborglab.browser.server.data.isatab;

import com.gmi.nordborglab.browser.server.domain.observation.Experiment;
import com.gmi.nordborglab.browser.server.repository.ExperimentRepository;
import com.gmi.nordborglab.browser.server.rest.ExperimentUploadData;
import com.gmi.nordborglab.browser.server.rest.PhenotypeUploadData;
import com.gmi.nordborglab.browser.server.rest.PhenotypeUploadValue;
import com.gmi.nordborglab.browser.server.testutils.BaseTest;
import com.gmi.nordborglab.browser.server.testutils.SecurityUtils;
import com.google.common.io.Files;
import com.google.common.io.LineProcessor;
import com.sun.jdori.common.sco.Date;
import org.isatools.isacreator.model.Investigation;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Resource;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Enumeration;
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

    private final String parentDir = "/tmp/1401200536112-0";

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
    public void testImportFromZip() {

    }

    @Test
    public void testImportFromDir() {
        ExperimentUploadData data = isaTabExporter.getExperimentUploadDataFromArchive(parentDir);
        assertNotNull(data);
        assertThat(data.getPhenotypes().size(), is(107));
        assertThat(data.getName(), is(String.format("%s - %s", "1", "Atwell et. al, Nature 2010")));
        assertThat(data.getDoi(), is("10.1038/nature08800"));
        assertThat(data.getOriginator(), is("Susanna Atwell"));
        assertThat(data.getDescription(), is("GWAS of 107 phenotypes in Arabidopsis thaliana inbred lines using ~250k SNPs in 199 accessions"));
        try {
            assertThat(data.getCreated(), is(IsaTabExporter.dateFormatter.parseObject("27/06/2013")));
            assertThat(data.getPublished(), is(IsaTabExporter.dateFormatter.parseObject("03/06/2010")));
        } catch (ParseException e) {
            Assert.fail(e.getMessage());
        }
        PhenotypeUploadData phenotype = data.getPhenotypes().get(0);
        assertThat(phenotype.getName(), is("At1"));
        assertThat(phenotype.getProtocol(), is("Four days after inoculation, leaves were scored by eye for disease symptom using a scale from 0 (no visible symptom) to 10 (leaves collapse and turn yellow), with an increment of 1."));
        assertThat(phenotype.getPhenotypeUploadValues().size(), is(175));
        assertThat(phenotype.getValueHeader().size(), is(1));
        assertThat(phenotype.getValueHeader().get(0), is("MEASURE"));
        PhenotypeUploadValue value = phenotype.getPhenotypeUploadValues().get(0);
        assertThat(value.getSourceId(), is("4932"));
        assertThat(value.getPassportId(), is(4932L));
        assertThat(value.getAccessionName(), is("UKSW06-334"));
        assertThat(value.getValues().size(), is(1));
        assertThat(value.getValues().get(0), is("0.167"));
        assertThat(phenotype.getTraitOntology().getAcc(), is("TO:0000315"));
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
        File investigationFile = new File(baseDirectory + File.separator + IsaTabExporter.INVESTIGATION_FILE);
        File studyFile = new File(baseDirectory + File.separator + IsaTabExporter.STUDY_FILE);
        File assayFile = new File(baseDirectory + File.separator + IsaTabExporter.ASSAY_FILE);
        File traitDefFile = new File(baseDirectory + File.separator + IsaTabExporter.TRAIT_DEF_FILE);
        File dervivedFile = new File(baseDirectory + File.separator + IsaTabExporter.DERVIVED_DATA_FILE);

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
