package uk.ac.ebi.subs.progressmonitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitMessagingTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import uk.ac.ebi.subs.data.Submission;
import uk.ac.ebi.subs.data.SubmissionEnvelope;
import uk.ac.ebi.subs.data.submittable.*;
import uk.ac.ebi.subs.messaging.Queues;
import uk.ac.ebi.subs.repository.SubmissionService;

import java.util.ArrayList;
import java.util.List;

@Component
public class QueueService {


    private static final Logger logger = LoggerFactory.getLogger(QueueService.class);

    private RabbitMessagingTemplate rabbitMessagingTemplate;
    private SubmissionService submissionService;

    private List<Submission> submissions;

    @Autowired
    public QueueService(RabbitMessagingTemplate rabbitMessagingTemplate, SubmissionService submissionService) {
        this.rabbitMessagingTemplate = rabbitMessagingTemplate;
        this.submissionService = submissionService;
    }

    @RabbitListener(queues = Queues.SUBMISSION_MONITOR)
    public void checkForProcessedSubmissions(SubmissionEnvelope submissionEnvelope) {
        Submission queueSubmission = submissionEnvelope.getSubmission();

        logger.info("received submission {}",queueSubmission.getId());

        Submission mongoSubmission = submissionService.fetchSubmission(queueSubmission.getId());

        if(checkForUpdates(queueSubmission, mongoSubmission)) {
            //FIXME - Is this store/save doing an upsert? Check and fix if required.
            submissionService.storeSubmission(mongoSubmission);
            logger.info("updated submission {}",queueSubmission.getId());
        }
        else {
            logger.info("no changes for submission {}",queueSubmission.getId());
        }
    }

    private boolean checkForUpdates(Submission queueSubmission, Submission mongoSubmission) {
        boolean updates = false;

        List<Analysis> analyses = queueSubmission.getAnalyses();
        if(!analyses.isEmpty() && !StringUtils.isEmpty(analyses.get(0).getAccession())) {
            mongoSubmission.setAnalyses(analyses);
            updates = true;
        }

        List<Assay> assays = queueSubmission.getAssays();
        if(!assays.isEmpty() && !StringUtils.isEmpty(assays.get(0).getAccession())) {
            mongoSubmission.setAssays(assays);
            updates = true;
        }

        List<AssayData> assayData = new ArrayList<>();
        if(!assayData.isEmpty() && !StringUtils.isEmpty(assayData.get(0).getAccession())) {
            mongoSubmission.setAssayData(assayData);
            updates = true;
        }

        List<EgaDac> egaDacs = new ArrayList<>();
        if(!egaDacs.isEmpty() && !StringUtils.isEmpty(egaDacs.get(0).getAccession())) {
            mongoSubmission.setEgaDacs(egaDacs);
            updates = true;
        }

        List<EgaDacPolicy> egaDacPolicies = new ArrayList<>();
        if(!egaDacPolicies.isEmpty() && !StringUtils.isEmpty(egaDacPolicies.get(0).getAccession())) {
            mongoSubmission.setEgaDacPolicies(egaDacPolicies);
            updates = true;
        }

        List<EgaDataset> egaDatasets = new ArrayList<>();
        if(!egaDatasets.isEmpty() && !StringUtils.isEmpty(egaDatasets.get(0).getAccession())) {
            mongoSubmission.setEgaDatasets(egaDatasets);
            updates = true;
        }

        List<Project> projects = new ArrayList<>();
        if(!projects.isEmpty() && !StringUtils.isEmpty(projects.get(0).getAccession())) {
            mongoSubmission.setProjects(projects);
            updates = true;
        }

        List<Sample> samples = queueSubmission.getSamples();
        if(!samples.isEmpty() && !StringUtils.isEmpty(samples.get(0).getAccession())){
            mongoSubmission.setSamples(samples);
            updates = true;
        }

        List<Study> studies = new ArrayList<>();
        if(!studies.isEmpty() && !StringUtils.isEmpty(studies.get(0).getAccession())){
            mongoSubmission.setStudies(studies);
            updates = true;
        }

        return updates;
    }
}
