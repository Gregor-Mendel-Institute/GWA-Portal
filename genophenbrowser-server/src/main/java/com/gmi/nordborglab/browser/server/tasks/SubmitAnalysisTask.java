package com.gmi.nordborglab.browser.server.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gmi.nordborglab.browser.server.data.GWASData;
import com.gmi.nordborglab.browser.server.domain.cdv.Study;
import com.gmi.nordborglab.browser.server.domain.phenotype.TraitUom;
import com.gmi.nordborglab.browser.server.domain.util.CandidateGeneListEnrichment;
import com.gmi.nordborglab.browser.server.domain.util.StudyJob;
import com.gmi.nordborglab.browser.server.domain.util.UserNotification;
import com.gmi.nordborglab.browser.server.errai.ClientComService;
import com.gmi.nordborglab.browser.server.es.EsIndexer;
import com.gmi.nordborglab.browser.server.repository.CandidateGeneListEnrichmentRepository;
import com.gmi.nordborglab.browser.server.repository.CandidateGeneListRepository;
import com.gmi.nordborglab.browser.server.repository.StudyJobRepository;
import com.gmi.nordborglab.browser.server.repository.UserNotificationRepository;
import com.gmi.nordborglab.browser.server.security.EsAclManager;
import com.gmi.nordborglab.browser.server.service.GWASDataService;
import com.gmi.nordborglab.browser.server.service.MetaAnalysisService;
import com.google.common.collect.Lists;
import ncsa.hdf.hdf5lib.exceptions.HDF5FileNotFoundException;
import org.elasticsearch.action.count.CountRequestBuilder;
import org.elasticsearch.action.count.CountResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilders;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.IOException;
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

@Component
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
    private GWASDataService gwasDataService;

    @Resource
    private Client client;

    @Resource
    private AmqpTemplate amqpTemplate;

    @Resource
    protected EsIndexer esIndexer;

    @Resource
    private EsAclManager esAclManager;

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(SubmitAnalysisTask.class);


    private static final String EXCHANGE_GWAS = "gwas";
    private static final String EXCHANGE_ENRICHMENT = "enrichment";
    private static final String EXCHANGE_TOPSNPS = "topsnps";


    private final String ROUTING_WORKER_SLOW = "gwas.portal.worker.slow";
    private final String ROUTING_HPC = "gwas.portal.hpc";
    private final String ROUTING_WORKER_FAST = "gwas.portal.worker.fast";
    private final String ROUTING_HPC_CHECK_JOBS = ROUTING_HPC + ".check_jobs";


    @Value("${AMQP.SLOW_GWAS_THRESHOLD}")
    private Double SLOW_GWAS_THRESHOLD;

    @Value("${AMQP.celery.task.gwas.hpc.check}")
    private String GWAS_CHECK_TASK;

    @Value("${AMQP.celery.task.gwas.hpc}")
    private String GWAS_HPC_TASK;

    @Value("${AMQP.celery.task.topsnps}")
    private String GWAS_TOP_SNPS_TASK;

    @Value("${AMQP.celery.task.gwas.worker}")
    private String GWAS_TASK;

    @Value("${AMQP.celery.task.enrichment}")
    private String CANDIDATE_GENE_LIST_ENRICHMENT;


    @Transactional(readOnly = false)
    @RabbitListener(queues = "gwas.portal.worker.status")
    public void onUpdateGWASJob(Message message, @Payload CeleryResult result, @Headers Map<String, Object> headers) {
        try {

            if (!new String(message.getMessageProperties().getCorrelationId()).equals(result.getTask_id()))
                throw new Exception("Task id does not match correlation_id");

            StudyJob studyJob = studyJobRepository.findByTaskid(result.getTask_id());
            if (studyJob == null)
                throw new Exception("StudyJob not found");
            String status = result.getStatus();
            Map<String, Object> payload = result.getResult();
            studyJob.setModificationDate(new Date());
            if (payload != null && payload.containsKey("progress")) {
                studyJob.setProgress((int) Math.round(Double.valueOf(payload.get("progress").toString())));
            }
            if (payload != null && payload.containsKey("task")) {
                studyJob.setTask((String) payload.get("task"));
            }
            if (!status.equalsIgnoreCase(studyJob.getStatus())) {
                studyJob.setStatus(status);
                switch (status.toUpperCase()) {
                    case "SUCCESS":
                        studyJob.setStatus("Finished");
                        studyJob.setTask("Finished on Worker");
                        studyJob.setProgress(100);
                        studyJob.setTaskid(null);
                        try {
                            indexTopSNPs(studyJob.getStudy());
                        } catch (HDF5FileNotFoundException e) {
                            logger.warn("HDF5 file to index not found", e);
                        }
                        break;
                    case "PROGRESS":
                        studyJob.setTask("Running on the Worker");
                        break;
                    case "FAILURE":
                        studyJob.setStatus("ERROR");
                        studyJob.setTask("GWAS failed on Worker");
                        // log error and send email
                        logger.error("GWAS job failed. Error: " + result.getTraceback());
                }

                if (studyJob.getAppUser() != null) {
                    UserNotification notification = getUserNotificationFromStudyJob(studyJob);
                    userNotificationRepository.save(notification);
                    ClientComService.pushUserNotification(studyJob.getAppUser().getId().toString(), studyJob.getAppUser().getEmail(), "gwasjob", studyJob.getStudy().getId());
                }
            }
            studyJobRepository.save(studyJob);
        } catch (Exception e) {
            logger.error("Failed to update the GWAS job status", e);
            throw new AmqpRejectAndDontRequeueException(e.getMessage());
        }
    }


    @Transactional(readOnly = false)
    @RabbitListener(queues = "enrichment.status")
    public void onUpdateCandidateGeneListEnrichment(Message message, @Payload CeleryResult result, @Headers Map<String, Object> headers) {
        try {
            if (!new String(message.getMessageProperties().getCorrelationId()).equals(result.getTask_id()))
                throw new Exception("Task id does not match correlation_id");
            CandidateGeneListEnrichment enrichment = candidateGeneListEnrichmentRepository.findByTaskid(result.getTask_id());
            if (enrichment == null)
                throw new Exception("Enrichment not found");
            Map<String, Object> payload = result.getResult();
            String status = result.getStatus();
            if (!enrichment.getStatus().equalsIgnoreCase(status)) {
                enrichment.setModified(new Date());
                enrichment.setStatus(status);
                switch (status.toUpperCase()) {
                    case "PROGRESS":
                        if (payload != null && payload.containsKey("progress")) {
                            enrichment.setProgress((int) Math.round(Double.valueOf(payload.get("progress").toString())));
                        }
                        if (payload != null && payload.containsKey("task")) {
                            enrichment.setTask((String) payload.get("task"));
                        }
                        break;
                    case "SUCCESS":
                        Double pValue = Double.parseDouble(payload.get("pvalue").toString());
                        enrichment.setPayload(null);
                        enrichment.setStatus("Finished");
                        enrichment.setTask("Finished on worker");
                        enrichment.setProgress(100);
                        enrichment.setPvalue(pValue);
                        break;
                    case "FAILED":
                        enrichment.setStatus("Error");
                        enrichment.setTask("Enrichment analysis failed");
                        logger.error("Enrichment job failed. Error: " + result.getTraceback());
                        break;
                }
            }
            candidateGeneListEnrichmentRepository.save(enrichment);
            indexCandidateGeneListEnrichment(enrichment);
        } catch (Exception e) {
            logger.error("Failed to update the Candidate Gene List enrichment status", e);
            throw new AmqpRejectAndDontRequeueException(e.getMessage());
        }
    }


    @Transactional(readOnly = false)
    public void submitGWASJob(StudyJob studyJob) {
        if (studyJob == null || studyJob.getTaskid() != null || studyJob.getStatus() == null)
            return;
        CeleryTask task = null;
        if (studyJob.getStatus().equalsIgnoreCase("Waiting")) {
            if (studyJob.getHPC()) {
                task = getCeleryTaskForHPCJob(studyJob);
            } else {
                task = getCeleryTaskForGWAS(studyJob);
            }
        }
        if (task == null)
            return;
        String replyTo = studyJob.getHPC() ? ROUTING_HPC + ".status" : "gwas.portal.worker.status";
        String routingKey = studyJob.getHPC() ? ROUTING_HPC : getRoutingKeyFromStudyJob(studyJob);
        submitCeleryTask(task, EXCHANGE_GWAS, routingKey, replyTo);
        studyJob.setTaskid(task.getId());
        studyJobRepository.save(studyJob);
    }


    private String getRoutingKeyFromStudyJob(StudyJob studyJob) {
        String routingKey = ROUTING_WORKER_FAST;
        if (studyJob.getStudy().getAlleleAssay().getId() != 1 && studyJob.getStudy().getPhenotype().getTraits().size() > SLOW_GWAS_THRESHOLD) {
            routingKey = ROUTING_WORKER_SLOW;
        }
        return routingKey;
    }


    @Transactional(readOnly = false)
    public void submitCanidateGeneListEnrichment(CandidateGeneListEnrichment enrichment) {
        if (enrichment.getTaskid() != null || enrichment.getStatus() == null)
            return;
        // only run if the job is finished
        if (enrichment.getStudy().getJob() != null && !enrichment.getStudy().getJob().getStatus().equalsIgnoreCase("Finished"))
            return;
        CeleryTask task = null;
        task = getCeleryTaskForEnrichment(enrichment);
        if (task == null)
            return;
        submitCeleryTask(task, EXCHANGE_ENRICHMENT, EXCHANGE_ENRICHMENT, "enrichment.status");
        enrichment.setTaskid(task.getId());
        enrichment.setProgress(1);
        enrichment.setTask("Waiting for worker");
        enrichment.setStatus("Waiting");
        candidateGeneListEnrichmentRepository.save(enrichment);
        indexCandidateGeneListEnrichment(enrichment);
    }


    @Transactional(readOnly = false)
    public void checkAndSubmitMetaAnalysis(StudyJob job) {
        if (!job.getStatus().equalsIgnoreCase("Finished"))
            return;
        CountRequestBuilder request = client.prepareCount(esAclManager.getIndex())
                .setTypes("meta_analysis_snps").setQuery(QueryBuilders.termQuery("studyid", job.getStudy().getId()));
        CountResponse response = request.execute().actionGet();
        if (response.getCount() == 0) {
            //submitAnalysisForTopSNPs(job.getStudy());
            indexTopSNPs(job.getStudy());
        }
    }

    private CeleryTask getCeleryTaskForGWAS(StudyJob studyJob) {
        String taskId = UUID.randomUUID().toString();
        List<Object> args = Lists.newArrayList();
        args.add(studyJob.getStudy().getId());
        return new CeleryTask(taskId, GWAS_TASK, args);
    }

    @Transactional(readOnly = false)
    public void submitCheckHPCJobs(StudyJob studyJob) {
        if (studyJob.getTaskid() != null || studyJob.getStatus() == null || !studyJob.getHPC())
            return;
        CeleryTask task = null;
        if (studyJob.getStatus().equalsIgnoreCase("Pending") || studyJob.getStatus().equalsIgnoreCase("Running")) {
            task = getCeleryTaskForCheckJob(studyJob);
        }
        if (task == null)
            return;
        submitCeleryTask(task, EXCHANGE_GWAS, ROUTING_HPC_CHECK_JOBS, ROUTING_HPC_CHECK_JOBS + ".status");
        studyJob.setTaskid(task.getId());
        studyJobRepository.save(studyJob);
    }


    private void submitCeleryTask(final CeleryTask task, String exchange, String routingKey, final String repylyTo) {
        amqpTemplate.convertAndSend(exchange, routingKey, task, new MessagePostProcessor() {
            @Override
            public Message postProcessMessage(Message message) throws AmqpException {
                if (repylyTo != null) {
                    message.getMessageProperties().setReplyTo(repylyTo);
                }
                message.getMessageProperties().setCorrelationId(task.getId().getBytes());
                return message;
            }

            ;
        });
    }


    private CeleryTask getCeleryTaskForCheckJob(StudyJob studyJob) {
        String taskId = UUID.randomUUID().toString();
        List<Object> args = Lists.newArrayList();
        try {
            Map<String, Object> payload = om.readValue(studyJob.getPayload().getBytes(), Map.class);
            args.add(studyJob.getStudy().getId());
            args.add(payload.get("saga_job_id"));
            args.add(payload.get("sge_job_id"));
        } catch (Exception e) {
            logger.error("Failed to retrieve Celery task message for Check job", e);
            throw new RuntimeException(e.getMessage());
            //TODO email
        }

        return new CeleryTask(taskId, GWAS_CHECK_TASK, args);
    }

    private CeleryTask getCeleryTaskForHPCJob(StudyJob studyJob) {
        String taskId = UUID.randomUUID().toString();
        List<Object> args = Lists.newArrayList();
        args.add(studyJob.getStudy().getId());
        return new CeleryTask(taskId, GWAS_HPC_TASK, args);
    }

    private CeleryTask getCeleryTaskForEnrichment(CandidateGeneListEnrichment enrichment) {
        String taskId = UUID.randomUUID().toString();
        List<Object> args = Lists.newArrayList();
        args.add(enrichment.getCandidateGeneList().getId());
        args.add(enrichment.getStudy().getId());
        args.add(enrichment.getStudy().getAlleleAssay().getId());
        args.add(enrichment.getWindowsize());
        args.add(enrichment.getPermutationCount());
        args.add(enrichment.getTopSNPCount());
        return new CeleryTask(taskId, CANDIDATE_GENE_LIST_ENRICHMENT, args);
    }

    @Transactional(readOnly = false)
    @RabbitListener(queues = ROUTING_HPC + ".status")
    public void onUpdateHPCJobId(Message message, @Payload CeleryResult result, @Headers Map<String, Object> headers) {
        try {
            if (!new String(message.getMessageProperties().getCorrelationId()).equals(result.getTask_id()))
                throw new Exception("Task id does not match correlation_id");
            StudyJob studyJob = studyJobRepository.findByTaskid(result.getTask_id());
            if (studyJob == null)
                throw new Exception("StudyJob not found");
            Map<String, Object> payload = result.getResult();
            String status = result.getStatus();
            if (status.equalsIgnoreCase("SUCCESS")) {
                ObjectMapper mapper = new ObjectMapper();
                studyJob.setPayload(mapper.writeValueAsString(payload));
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
            } else {
                logger.error("Can't handle status: " + result.getTraceback());
                studyJob.setStatus("Error");
                studyJob.setTask("HPC job failed");
                studyJobRepository.save(studyJob);
                if (studyJob.getAppUser() != null) {
                    UserNotification notification = getUserNotificationFromStudyJob(studyJob);
                    userNotificationRepository.save(notification);
                    ClientComService.pushUserNotification(studyJob.getAppUser().getId().toString(), studyJob.getAppUser().getEmail(), "gwasjob", studyJob.getStudy().getId());
                }
            }
        } catch (Exception e) {
            logger.error("Failed to update the HPC job id", e);
            throw new AmqpRejectAndDontRequeueException(e.getMessage());
        }
    }

    @Transactional(readOnly = false)
    @RabbitListener(queues = "gwas.portal.hpc.progress")
    public void onUpdateHPCJobProgress(Message message, @Payload CeleryResult result, @Headers Map<String, Object> headers) {
        try {

        } catch (Exception e) {
            logger.error("Failed to update the HPC Job task progress", e);
            throw new AmqpRejectAndDontRequeueException(e.getMessage());
        }
    }


    @Transactional(readOnly = false)
    @RabbitListener(queues = "gwas.portal.hpc.check_jobs.status")
    public void onUpdateHPCJobStatus(Message message, @Payload CeleryResult result, @Headers Map<String, Object> headers) {
        try {
            if (!new String(message.getMessageProperties().getCorrelationId()).equals(result.getTask_id()))
                throw new Exception("Task id does not match correlation_id");
            StudyJob studyJob = studyJobRepository.findByTaskid(result.getTask_id());
            if (studyJob == null)
                return;
            Map<String, Object> payload = result.getResult();
            String status;
            if (!result.getStatus().equalsIgnoreCase("SUCCESS")) {
                logger.error("Check HPC Job failed: "+result.getTraceback());
                status = "Failed";
            }
            else {
                status = (String) payload.get("status");
            }
            studyJob.setTaskid(null);
            if (!studyJob.getStatus().equalsIgnoreCase(status)) {
                studyJob.setModificationDate(new Date());
                if ("Done".equalsIgnoreCase(status)) {
                    studyJob.setPayload(null);
                    studyJob.setStatus("Finished");
                    studyJob.setTask("Finished on HPC cluster");
                    studyJob.setProgress(100);
                    try {
                        indexTopSNPs(studyJob.getStudy());
                    } catch (HDF5FileNotFoundException e) {
                        logger.warn("HDF5 file to index not found", e);
                    }
                } else if ("Pending".equalsIgnoreCase(status)) {
                    studyJob.setStatus("Pending");
                    studyJob.setTask("Queued on the HPC");
                    studyJob.setProgress(10);
                } else if ("Running".equalsIgnoreCase(status)) {
                    studyJob.setStatus("Running");
                    studyJob.setTask("Running on the HPC");
                    studyJob.setProgress(30);
                } else if ("Failed".equalsIgnoreCase(status)) {
                    logger.error("HPC job failed", studyJob);
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
            logger.error("Failed to update the HPC job status", e);
            throw new AmqpRejectAndDontRequeueException(e.getMessage());
        }
    }

    public void submitAnalysisForTopSNPs(Study study) {
        TraitUom traitUom = study.getPhenotype();
        List<Object> args = Lists.newArrayList();
        args.add(study.getId());
        args.add(traitUom.getExperiment().getId());
        CeleryTask task = new CeleryTask(UUID.randomUUID().toString(), GWAS_TOP_SNPS_TASK, args);
        //submitCeleryTask(task);
    }

    private void indexTopSNPs(Study study) {
        GWASData gwasData;
        try {
            gwasData = gwasDataService.getGWASDataByStudyIdForIndexer(study.getId());
            esIndexer.indexMetaAnalysisSnps(gwasData,study.getId(),study.getPhenotype().getExperiment().getId().toString(),false);
        } catch (HDF5FileNotFoundException e) {
            logger.warn("HDF5 File not found. Skipping");
        }
        catch (IOException ex) {
            logger.error("Failed to index top snps", ex);
        }
    }

    private static String getBadgeFromStatus(String status) {
        String label = "";
        if ("Pending".equalsIgnoreCase(status)) {
            label = "warning";
        } else if ("Failed".equalsIgnoreCase(status) || "Error".equalsIgnoreCase(status)) {
            label = "danger";
        } else if ("Running".equalsIgnoreCase(status) || "Progress".equalsIgnoreCase(status)) {
            label = "info";
        } else if ("Done".equalsIgnoreCase(status) || "Finished".equalsIgnoreCase(status)) {
            label = "success";
        }
        if (!label.isEmpty())
            label = "label-" + label;
        return label;
    }

    private static UserNotification getUserNotificationFromStudyJob(StudyJob studyJob) {
        UserNotification notification = new UserNotification();
        notification.setAppUser(studyJob.getAppUser());
        notification.setType("gwasjob");
        String badge = getBadgeFromStatus(studyJob.getStatus());
        String notificationText = "State of <a href=\"/#/analysis/%s/overview\">GWAS-Job (%s)</a> on HPC cluster changed to <span class=\"label %s\">%s</span>";
        notification.setText(String.format(notificationText, studyJob.getStudy().getId(), studyJob.getStudy().getName(), badge, studyJob.getStatus()));
        return notification;
    }


    private void indexCandidateGeneListEnrichment(CandidateGeneListEnrichment enrichment) {
        metaAnalysisService.indexCandidateGeneListEnrichment(enrichment);
    }
}
