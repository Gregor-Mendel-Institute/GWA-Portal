package com.gmi.nordborglab.browser.server.tasks;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gmi.nordborglab.browser.server.domain.util.StudyJob;
import com.gmi.nordborglab.browser.server.domain.util.UserNotification;
import com.gmi.nordborglab.browser.server.errai.ClientComService;
import com.gmi.nordborglab.browser.server.repository.StudyJobRepository;
import com.gmi.nordborglab.browser.server.repository.UserNotificationRepository;
import com.gmi.nordborglab.browser.server.service.UserService;
import com.google.common.collect.Lists;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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
    private UserNotificationRepository userNotificationRepository;

    @Resource
    private AmqpTemplate amqpTemplate;

    @Value("${AMQP.celery.exchange}")
    private String CELERY_EXCHANGE;

    @Value("${AMQP.celery.routingKey}")
    private String CELERY_ROUTING_KEY;

    private static final String GWAS_CHECK_TASK = "taverna_gwas.workflow.check_saga_job";

    private static final String GWAS_TASK ="taverna_gwas.workflow.start_saga";


    @Scheduled(fixedDelay = 50000)
    @Transactional(readOnly = false)
    public synchronized void submitGWASJobs() {
        try {
            List<StudyJob> studyJobs = studyJobRepository.findByStatusInAndTaskidIsNull("Waiting");
            for (StudyJob studyJob:studyJobs) {
                submitCeleryTask(studyJob);
            }
        }
        catch (Exception e) {
            //TODO send an email.
             String test="test";
        }
    }


    @Scheduled(fixedDelay = 10000)
    @Transactional(readOnly = false)
    public synchronized void checkGWASJobs() {
        try {
            List<StudyJob> studyJobs = studyJobRepository.findByStatusInAndTaskidIsNull("Queued", "Running");
            for (StudyJob studyJob:studyJobs) {
                submitCeleryTask(studyJob);
            }
        }
        catch (Exception e) {
            //TODO send an email.
            String test="test";
        }
    }


    private void submitCeleryTask(StudyJob studyJob) {
        MessageProperties messageProperties = new MessageProperties();
        if (studyJob.getTaskid() != null || studyJob.getStatus() == null)
            return;
        CeleryTask task = null;
        if (studyJob.getStatus().equalsIgnoreCase("Waiting")) {
            task = getCeleryTaskForJob(studyJob);
        }
        else if (studyJob.getStatus().equalsIgnoreCase("Queued") || studyJob.getStatus().equalsIgnoreCase("Running")) {
            task = getCeleryTaskForCheckJob(studyJob);
        }
        if (task == null)
            return;
        String payload = "";
        try {
            payload = om.writeValueAsString(task);
        }
        catch (JsonProcessingException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            throw new RuntimeException("Could not serialize task");
        }
        messageProperties.setContentEncoding("utf-8");
        messageProperties.setContentType("application/json");
        amqpTemplate.send(CELERY_EXCHANGE,CELERY_ROUTING_KEY,new Message(payload.getBytes(),messageProperties));
        studyJob.setTaskid(task.getId());
        studyJobRepository.save(studyJob);
    }

    private CeleryTask getCeleryTaskForCheckJob(StudyJob studyJob) {
        String taskId =  UUID.randomUUID().toString();
        List<Object> args = Lists.newArrayList();
        try {
            Map<String,Object> payload = om.readValue(studyJob.getPayload().getBytes(),Map.class);
            args.add(studyJob.getStudy().getId());
            args.add(studyJob.getId());
            args.add(payload.get("saga_job_id"));
            args.add(payload.get("sge_job_id"));
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
            //TODO email
        }

        return new CeleryTask(taskId,GWAS_CHECK_TASK,args);
    }

     private CeleryTask getCeleryTaskForJob(StudyJob studyJob) {
         String taskId =  UUID.randomUUID().toString();
         List<Object> args = Lists.newArrayList();
         args.add(studyJob.getStudy().getId());
         args.add(studyJob.getId());
         return new CeleryTask(taskId,GWAS_TASK,args);
     }

    @Transactional(readOnly = false)
    public void onUpdateJobId(byte[] message) {
        try {
            Map<String,Object> payload = om.readValue(message,Map.class);
            Long studyJobId = Long.parseLong(payload.get("studyjobid").toString());
            StudyJob studyJob = studyJobRepository.findOne(studyJobId);
            studyJob.setPayload(new String(message));
            studyJob.setStatus("Queued");
            studyJob.setTask("Queued on the HPC");
            studyJob.setProgress(10);
            studyJob.setTaskid(null);
            studyJobRepository.save(studyJob);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    @Transactional(readOnly = false)
    public void onUpdateJobStatus(byte [] message) {
        try {
            Map<String,Object> payload = om.readValue(message,Map.class);
            Long studyJobId = Long.parseLong(payload.get("studyjobid").toString());
            StudyJob studyJob = studyJobRepository.findOne(studyJobId);
            String status = (String)payload.get("status");
            studyJob.setTaskid(null);
            if (!studyJob.getStatus().equalsIgnoreCase(status)) {
                studyJob.setModificationDate(new Date());
                if ("Done".equalsIgnoreCase(status)) {
                    studyJob.setPayload(null);
                    studyJob.setStatus("Finished");
                    studyJob.setTask("Finished on HPC cluster");
                    studyJob.setProgress(100);
                }
                else if ("Pending".equalsIgnoreCase(status)) {
                    studyJob.setStatus("Queued");
                    studyJob.setTask("Queued on the HPC");
                    studyJob.setProgress(10);
                }
                else if ("Running".equalsIgnoreCase(status)) {
                    studyJob.setStatus("Running");
                    studyJob.setTask("Running on the HPC");
                    studyJob.setProgress(30);
                }
                else if ("Failed".equalsIgnoreCase(status)) {
                    studyJob.setStatus("Error");
                    studyJob.setTask("HPC job failed");
                }
                if (studyJob.getAppUser() != null) {
                    UserNotification notification = new UserNotification();
                    notification.setAppUser(studyJob.getAppUser());
                    notification.setType("gwasjob");
                    notification.setText("State of GWAS-Job ("+studyJob.getStudy().getName()+") on HPC cluster changed to " + status);
                    userNotificationRepository.save(notification);
                    ClientComService.pushUserNotification(studyJob.getAppUser().getUsername(),studyJob.getAppUser().getEmail(),"gwasjob",studyJob.getStudy().getId());
                }
            }
            studyJobRepository.save(studyJob);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }
}
