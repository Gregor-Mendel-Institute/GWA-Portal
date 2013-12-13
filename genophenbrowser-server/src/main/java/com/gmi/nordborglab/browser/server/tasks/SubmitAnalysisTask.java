package com.gmi.nordborglab.browser.server.tasks;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gmi.nordborglab.browser.server.domain.cdv.Study;
import com.gmi.nordborglab.browser.server.domain.phenotype.TraitUom;
import com.gmi.nordborglab.browser.server.domain.util.CandidateGeneList;
import com.gmi.nordborglab.browser.server.domain.util.CandidateGeneListEnrichment;
import com.gmi.nordborglab.browser.server.domain.util.StudyJob;
import com.gmi.nordborglab.browser.server.domain.util.UserNotification;
import com.gmi.nordborglab.browser.server.errai.ClientComService;
import com.gmi.nordborglab.browser.server.repository.CandidateGeneListEnrichmentRepository;
import com.gmi.nordborglab.browser.server.repository.CandidateGeneListRepository;
import com.gmi.nordborglab.browser.server.repository.StudyJobRepository;
import com.gmi.nordborglab.browser.server.repository.UserNotificationRepository;
import com.gmi.nordborglab.browser.server.security.EsAclManager;
import com.gmi.nordborglab.browser.server.service.MetaAnalysisService;
import com.gmi.nordborglab.browser.server.service.impl.SearchServiceImpl;
import com.google.common.collect.Lists;
import org.elasticsearch.action.count.CountRequestBuilder;
import org.elasticsearch.action.count.CountResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 4/11/13
 * Time: 5:22 PM
 * To change this template use File | Settings | File Templates.
 */

@Component()
@Transactional(readOnly = true)
public class SubmitAnalysisTask {

    private static ObjectMapper om = new ObjectMapper();
    @Resource
    private StudyJobRepository studyJobRepository;

    @Resource
    private CandidateGeneListEnrichmentRepository candidateGeneListEnrichmentRepository;

    @Resource
    private CandidateGeneListRepository candidateGeneListRepository;

    @Resource
    private UserNotificationRepository userNotificationRepository;

    @Resource
    private MetaAnalysisService metaAnalysisService;

    @Resource
    private Client client;

    @Resource
    private AmqpTemplate amqpTemplate;

    @Resource
    private EsAclManager esAclManager;

    @Value("${AMQP.celery.exchange}")
    private String CELERY_EXCHANGE;

    @Value("${AMQP.celery.routingKey}")
    private String CELERY_ROUTING_KEY;

    private static final String GWAS_CHECK_TASK = "gmihpcworkflows.hpc_tasks.check_saga_job";
    private static final String GWAS_TASK = "gmihpcworkflows.hpc_tasks.start_saga";
    private static final String GWAS_TOP_SNPS_TASK = "gmihpcworkflows.hpc_tasks.index_top_study_snps";
    private static final String CANDIDATE_GENE_LIST_ENRICHMENT = "gmihpcworkflows.hpc_tasks.candidate_gene_enrichment";


    @Scheduled(fixedDelay = 60000)
    @Transactional(readOnly = false)
    public synchronized void submitGWASJobs() {
        try {
            List<StudyJob> studyJobs = studyJobRepository.findByStatusInAndTaskidIsNull("Waiting");
            for (StudyJob studyJob : studyJobs) {
                submitStudyJob(studyJob);
            }
        } catch (Exception e) {
            //TODO send an email.
            String test = "test";
        }
    }


    @Scheduled(fixedDelay = 30000)
    @Transactional(readOnly = false)
    public synchronized void checkGWASJobs() {
        try {
            List<StudyJob> studyJobs = studyJobRepository.findByStatusInAndTaskidIsNull("Pending", "Running");
            for (StudyJob studyJob : studyJobs) {
                submitStudyJob(studyJob);
            }
        } catch (Exception e) {
            //TODO send an email.
            String test = "test";
        }
    }

    @Scheduled(fixedDelay = 60000)
    @Transactional(readOnly = false)
    public synchronized void submitCanidateGeneListEnrichment() {
        try {
            List<CandidateGeneListEnrichment> enrichments = candidateGeneListEnrichmentRepository.findByStatusInAndTaskidIsNull("Waiting");
            for (CandidateGeneListEnrichment enrichment : enrichments) {
                submitCanidateGeneListEnrichment(enrichment);
            }
        } catch (Exception e) {
            //TODO send an email.
            String test = "test";
        }
    }

    private void submitCanidateGeneListEnrichment(CandidateGeneListEnrichment enrichment) {
        if (enrichment.getTaskid() != null || enrichment.getStatus() == null)
            return;
        CeleryTask task = null;
        task = getCeleryTaskForEnrichment(enrichment);
        if (task == null)
            return;
        submitCeleryTask(task);
        enrichment.setTaskid(task.getId());
        enrichment.setProgress(10);
        enrichment.setTask("Running analysis on worker");
        enrichment.setStatus("Running");
        candidateGeneListEnrichmentRepository.save(enrichment);
        indexCandidateGeneListEnrichment(enrichment);
    }

    @Scheduled(cron = "* 0 0  * * *")
    @Transactional(readOnly = true)
    public synchronized void checkMetaAnalysis() {
        try {
            List<StudyJob> studyJobs = studyJobRepository.findByStatusInAndTaskidIsNull("Finished");
            for (StudyJob studyJob : studyJobs) {
                checkAndSubmitMetaAnalysis(studyJob);
            }
        } catch (Exception e) {
            //TODO send an email.
            String test = "test";
        }
    }

    private void checkAndSubmitMetaAnalysis(StudyJob job) {
        if (!job.getStatus().equalsIgnoreCase("Finished"))
            return;
        CountRequestBuilder request = client.prepareCount(esAclManager.getIndex())
                .setTypes("meta_analysis_snps").setQuery(QueryBuilders.termQuery("studyid", job.getStudy().getId()));
        CountResponse response = request.execute().actionGet();
        if (response.getCount() == 0) {
            submitAnalysisForTopSNPs(job.getStudy());
        }
    }


    private void submitStudyJob(StudyJob studyJob) {

        if (studyJob.getTaskid() != null || studyJob.getStatus() == null)
            return;
        CeleryTask task = null;
        if (studyJob.getStatus().equalsIgnoreCase("Waiting")) {
            task = getCeleryTaskForJob(studyJob);
        } else if (studyJob.getStatus().equalsIgnoreCase("Pending") || studyJob.getStatus().equalsIgnoreCase("Running")) {
            task = getCeleryTaskForCheckJob(studyJob);
        }
        if (task == null)
            return;
        submitCeleryTask(task);
        studyJob.setTaskid(task.getId());
        studyJobRepository.save(studyJob);
    }

    private void submitCeleryTask(CeleryTask task) {
        MessageProperties messageProperties = new MessageProperties();
        if (task == null)
            return;
        String payload = "";
        try {
            payload = om.writeValueAsString(task);
        } catch (JsonProcessingException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            throw new RuntimeException("Could not serialize task");
        }
        messageProperties.setContentEncoding("utf-8");
        messageProperties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        amqpTemplate.send(CELERY_EXCHANGE, CELERY_ROUTING_KEY, new Message(payload.getBytes(), messageProperties));
    }


    private CeleryTask getCeleryTaskForCheckJob(StudyJob studyJob) {
        String taskId = UUID.randomUUID().toString();
        List<Object> args = Lists.newArrayList();
        try {
            Map<String, Object> payload = om.readValue(studyJob.getPayload().getBytes(), Map.class);
            args.add(studyJob.getStudy().getId());
            args.add(studyJob.getId());
            args.add(payload.get("saga_job_id"));
            args.add(payload.get("sge_job_id"));
            args.add(payload.get("newHPC"));
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
            //TODO email
        }

        return new CeleryTask(taskId, GWAS_CHECK_TASK, args);
    }

    private CeleryTask getCeleryTaskForJob(StudyJob studyJob) {
        String taskId = UUID.randomUUID().toString();
        List<Object> args = Lists.newArrayList();
        args.add(studyJob.getStudy().getId());
        args.add(studyJob.getId());
        args.add(true);
        return new CeleryTask(taskId, GWAS_TASK, args);
    }

    private CeleryTask getCeleryTaskForEnrichment(CandidateGeneListEnrichment enrichment) {
        String taskId = UUID.randomUUID().toString();
        List<Object> args = Lists.newArrayList();
        args.add(enrichment.getId());
        args.add(enrichment.getCandidateGeneList().getId());
        args.add(enrichment.getStudy().getId());
        args.add(enrichment.getStudy().getAlleleAssay().getId());
        args.add(enrichment.getWindowsize());
        args.add(enrichment.getPermutationCount());
        args.add(enrichment.getTopSNPCount());
        return new CeleryTask(taskId, CANDIDATE_GENE_LIST_ENRICHMENT, args);
    }

    @Transactional(readOnly = false)
    public void onUpdateJobId(byte[] message) {
        try {
            Map<String, Object> payload = om.readValue(message, Map.class);
            Long studyJobId = Long.parseLong(payload.get("studyjobid").toString());
            StudyJob studyJob = studyJobRepository.findOne(studyJobId);
            if (studyJob == null) {
                return;
            }
            studyJob.setPayload(new String(message));
            studyJob.setStatus("Pending");
            studyJob.setTask("Queued on the HPC");
            studyJob.setProgress(10);
            studyJob.setTaskid(null);
            studyJobRepository.save(studyJob);
            if (studyJob.getAppUser() != null) {
                UserNotification notification = getUserNotificationFromStudyJob(studyJob);
                userNotificationRepository.save(notification);
                ClientComService.pushUserNotification(studyJob.getAppUser().getId().toString(), studyJob.getAppUser().getEmail(), "gwasjob", studyJob.getStudy().getId());
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new AmqpRejectAndDontRequeueException(e.getMessage());
        }
    }

    @Transactional(readOnly = false)
    public void onUpdateJobStatus(byte[] message) {
        try {
            Map<String, Object> payload = om.readValue(message, Map.class);
            Long studyJobId = Long.parseLong(payload.get("studyjobid").toString());
            StudyJob studyJob = studyJobRepository.findOne(studyJobId);
            if (studyJob == null)
                return;
            String status = (String) payload.get("status");
            studyJob.setTaskid(null);
            if (!studyJob.getStatus().equalsIgnoreCase(status)) {
                studyJob.setModificationDate(new Date());
                if ("Done".equalsIgnoreCase(status)) {
                    studyJob.setPayload(null);
                    studyJob.setStatus("Finished");
                    studyJob.setTask("Finished on HPC cluster");
                    studyJob.setProgress(100);
                    submitAnalysisForTopSNPs(studyJob.getStudy());
                } else if ("Pending".equalsIgnoreCase(status)) {
                    studyJob.setStatus("Pending");
                    studyJob.setTask("Queued on the HPC");
                    studyJob.setProgress(10);
                } else if ("Running".equalsIgnoreCase(status)) {
                    studyJob.setStatus("Running");
                    studyJob.setTask("Running on the HPC");
                    studyJob.setProgress(30);
                } else if ("Failed".equalsIgnoreCase(status)) {
                    studyJob.setStatus("Error");
                    studyJob.setTask("HPC job failed");
                }


                if (studyJob.getAppUser() != null) {
                    UserNotification notification = getUserNotificationFromStudyJob(studyJob);
                    userNotificationRepository.save(notification);
                    ClientComService.pushUserNotification(studyJob.getAppUser().getId().toString(), studyJob.getAppUser().getEmail(), "gwasjob", studyJob.getStudy().getId());
                }
            }
            studyJobRepository.save(studyJob);
        } catch (Exception e) {
            e.printStackTrace();
            throw new AmqpRejectAndDontRequeueException(e.getMessage());
        }
    }

    public void submitAnalysisForTopSNPs(Study study) {
        TraitUom traitUom = study.getPhenotype();
        List<Object> args = Lists.newArrayList();
        args.add(study.getId());
        args.add(traitUom.getExperiment().getId());
        CeleryTask task = new CeleryTask(UUID.randomUUID().toString(), GWAS_TOP_SNPS_TASK, args);
        submitCeleryTask(task);
    }

    private static String getBadgeFromStatus(String status) {
        String badge = "";
        if ("Pending".equalsIgnoreCase(status)) {
            badge = "warning";
        } else if ("Failed".equalsIgnoreCase(status) || "Error".equalsIgnoreCase(status)) {
            badge = "important";
        } else if ("Running".equalsIgnoreCase(status)) {
            badge = "info";
        } else if ("Done".equalsIgnoreCase(status) || "Finished".equalsIgnoreCase(status)) {
            badge = "success";
        }
        if (!badge.isEmpty())
            badge = "badge-" + badge;
        return badge;
    }

    private static UserNotification getUserNotificationFromStudyJob(StudyJob studyJob) {
        UserNotification notification = new UserNotification();
        notification.setAppUser(studyJob.getAppUser());
        notification.setType("gwasjob");
        String badge = getBadgeFromStatus(studyJob.getStatus());
        String notificationText = "State of <a href=\"#!analysis/%s/overview\">GWAS-Job (%s)</a> on HPC cluster changed to <span class=\"badge %s\">%s</span>";
        notification.setText(String.format(notificationText, studyJob.getStudy().getId(), studyJob.getStudy().getName(), badge, studyJob.getStatus()));
        return notification;
    }

    @Transactional(readOnly = false)
    public void onUpdateCandidateGeneListEnrichment(byte[] message) {
        try {
            Map<String, Object> payload = om.readValue(message, Map.class);
            Long candidateGeneListEnrichmentId = Long.parseLong(payload.get("id").toString());
            Double pValue = Double.parseDouble(payload.get("pvalue").toString());
            CandidateGeneListEnrichment enrichment = candidateGeneListEnrichmentRepository.findOne(candidateGeneListEnrichmentId);
            if (enrichment == null)
                return;
            String status = (String) payload.get("status");
            enrichment.setTaskid(null);
            if (!enrichment.getStatus().equalsIgnoreCase(status)) {
                enrichment.setModified(new Date());
                if ("Done".equalsIgnoreCase(status)) {
                    enrichment.setPayload(null);
                    enrichment.setStatus("Finished");
                    enrichment.setTask("Finished on worker");
                    enrichment.setProgress(100);
                    enrichment.setPvalue(pValue);
                } else if ("Failed".equalsIgnoreCase(status)) {
                    enrichment.setStatus("Error");
                    enrichment.setTask("Enrichment analysis failed");
                }
            }
            candidateGeneListEnrichmentRepository.save(enrichment);
            indexCandidateGeneListEnrichment(enrichment);
        } catch (Exception e) {
            e.printStackTrace();
            throw new AmqpRejectAndDontRequeueException(e.getMessage());
        }
    }

    private void indexCandidateGeneListEnrichment(CandidateGeneListEnrichment enrichment) {
        metaAnalysisService.indexCandidateGeneListEnrichment(enrichment);
    }
}
