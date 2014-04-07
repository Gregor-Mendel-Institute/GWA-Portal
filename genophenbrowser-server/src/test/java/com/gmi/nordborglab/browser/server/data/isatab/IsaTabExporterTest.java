package com.gmi.nordborglab.browser.server.data.isatab;

import com.gmi.nordborglab.browser.server.domain.observation.Experiment;
import com.gmi.nordborglab.browser.server.repository.ExperimentRepository;
import com.gmi.nordborglab.browser.server.testutils.BaseTest;
import com.gmi.nordborglab.browser.server.testutils.SecurityUtils;
import com.google.common.io.Files;
import com.google.common.io.LineProcessor;
import org.isatools.isacreator.model.Investigation;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Resource;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
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
