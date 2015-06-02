package com.gmi.nordborglab.browser.server.tasks;

import com.gmi.nordborglab.browser.server.domain.util.CandidateGeneListEnrichment;
import com.gmi.nordborglab.browser.server.domain.util.StudyJob;
import com.gmi.nordborglab.browser.server.repository.CandidateGeneListEnrichmentRepository;
import com.gmi.nordborglab.browser.server.repository.StudyJobRepository;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by uemit.seren on 11/27/14.
 */

@Component()
@Transactional(readOnly = true)
public class CronJobs {

    @Resource
    private StudyJobRepository studyJobRepository;

    @Resource
    private SubmitAnalysisTask submitAnalysisTask;

    @Resource
    private CandidateGeneListEnrichmentRepository candidateGeneListEnrichmentRepository;

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(SubmitAnalysisTask.class);


    @Scheduled(fixedDelay = 60000)
    @Transactional(readOnly = false)
    public synchronized void submitGWASJobs() {
        try {
            List<StudyJob> studyJobs = studyJobRepository.findByStatusInAndTaskidIsNull("Waiting");
            for (StudyJob studyJob : studyJobs) {
                submitAnalysisTask.submitGWASJob(studyJob);
            }
        } catch (Exception e) {
            logger.error("Failed to submit GWAS analysis task",e);
        }
    }


    @Scheduled(fixedDelay = 30000)
    @Transactional(readOnly = false)
    public synchronized void checkGWASJobs() {
        try {
            List<StudyJob> studyJobs = studyJobRepository.findByStatusInAndTaskidIsNull("Pending", "Running");
            for (StudyJob studyJob : studyJobs) {
                if (studyJob.getHPC()) {
                    submitAnalysisTask.submitCheckHPCJobs(studyJob);
                }
            }
        } catch (Exception e) {
            logger.error("Failed to submit check GWAS job task",e);
        }
    }


    @Scheduled(fixedDelay = 60000)
    @Transactional(readOnly = false)
    public synchronized void submitCanidateGeneListEnrichment() {
        try {
            List<CandidateGeneListEnrichment> enrichments = candidateGeneListEnrichmentRepository.findByStatusInAndTaskidIsNull("Waiting");
            for (CandidateGeneListEnrichment enrichment : enrichments) {
                submitAnalysisTask.submitCanidateGeneListEnrichment(enrichment);
            }
        } catch (Exception e) {
            logger.error("Failed to submit candidate gene lsit enrichment analysis task",e);
        }
    }

    @Scheduled(cron = "* 0 0  * * *")
    @Transactional(readOnly = true)
    public synchronized void checkMetaAnalysis() {
        try {
            List<StudyJob> studyJobs = studyJobRepository.findByStatusInAndTaskidIsNull("Finished");
            for (StudyJob studyJob : studyJobs) {
                submitAnalysisTask.checkAndSubmitMetaAnalysis(studyJob);
            }
        } catch (Exception e) {
            logger.error("Failed to check meta-analysis to index top snps",e);
        }
    }

}
