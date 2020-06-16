package com.gmi.nordborglab.browser.server.tasks;

import com.gmi.nordborglab.browser.server.domain.util.CandidateGeneListEnrichment;
import com.gmi.nordborglab.browser.server.domain.util.StudyJob;
import com.gmi.nordborglab.browser.server.repository.CandidateGeneListEnrichmentRepository;
import com.gmi.nordborglab.browser.server.repository.StudyJobRepository;
import com.gmi.nordborglab.browser.server.testutils.BaseTest;
import com.gmi.nordborglab.browser.server.testutils.SecurityUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Resource;

/**
 * Created by uemit.seren on 11/27/14.
 */
public class SubmitAnalysisTaskTest extends BaseTest {

    @Resource
    private SubmitAnalysisTask task;

    @Resource
    private StudyJobRepository studyJobRepository;

    @Resource
    private CandidateGeneListEnrichmentRepository candidateGeneListEnrichmentRepository;

    @Before
    public void setUp() {
        SecurityUtils.setAnonymousUser();
    }

    @After
    public void clearContext() {
        SecurityUtils.clearContext();
    }

    @Test
    public void testSubmitGWASJob() {
        StudyJob studyJob = studyJobRepository.findOne(1515L);
        task.submitGWASJob(studyJob);
    }

    @Test
    public void testSubmitEnrichmentAnalysis() {
        CandidateGeneListEnrichment enrichment = candidateGeneListEnrichmentRepository.findOne(1L);
        task.submitCanidateGeneListEnrichment(enrichment);
    }

}
