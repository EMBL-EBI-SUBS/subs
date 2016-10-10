package uk.ac.ebi.subs.enaagent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitMessagingTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.stereotype.Service;
import uk.ac.ebi.subs.data.Submission;
import uk.ac.ebi.subs.data.SubmissionEnvelope;
import uk.ac.ebi.subs.data.component.Archive;
import uk.ac.ebi.subs.data.submittable.*;
import uk.ac.ebi.subs.enarepo.EnaAssayDataRepository;
import uk.ac.ebi.subs.enarepo.EnaAssayRepository;
import uk.ac.ebi.subs.enarepo.EnaStudyRepository;
import uk.ac.ebi.subs.messaging.Exchanges;
import uk.ac.ebi.subs.messaging.Queues;
import uk.ac.ebi.subs.messaging.Topics;

import java.util.*;


@Service
public class EnaAgentSubmissionsProcessor {

    private static final Logger logger = LoggerFactory.getLogger(EnaAgentSubmissionsProcessor.class);

    String processedStatusValue = "processed";

    @Autowired
    EnaStudyRepository enaStudyRepository;

    @Autowired
    EnaAssayRepository enaAssayRepository;

    @Autowired
    EnaAssayDataRepository enaAssayDataRepository;

    RabbitMessagingTemplate rabbitMessagingTemplate;

    @Autowired
    public EnaAgentSubmissionsProcessor(RabbitMessagingTemplate rabbitMessagingTemplate, MessageConverter messageConverter) {
        this.rabbitMessagingTemplate = rabbitMessagingTemplate;
        this.rabbitMessagingTemplate.setMessageConverter(messageConverter);
    }


    @RabbitListener(queues = {Queues.ENA_AGENT})
    public void handleSubmission(SubmissionEnvelope submissionEnvelope) {

        logger.info("received submission {}, most recent handler was ",
                submissionEnvelope.getSubmission().getId(),
                submissionEnvelope.mostRecentHandler());

        processSubmission(submissionEnvelope);

        submissionEnvelope.addHandler(this.getClass());

        logger.info("processed submission {}",submissionEnvelope.getSubmission().getId());

        rabbitMessagingTemplate.convertAndSend(Exchanges.SUBMISSIONS,Topics.EVENT_SUBMISSION_PROCESSED, submissionEnvelope);

        logger.info("sent submission {}",submissionEnvelope.getSubmission().getId());
    }

    public void processSubmission(SubmissionEnvelope submissionEnvelope) {

        submissionEnvelope.getSubmission().getStudies().stream()
                .filter(s -> s.getArchive() == Archive.Ena)
                .forEach(s -> processStudy(s, submissionEnvelope));


        submissionEnvelope.getSubmission().getAssays().stream()
                .filter(a -> a.getArchive() == Archive.Ena)
                .forEach(a -> processAssay(a, submissionEnvelope));



        submissionEnvelope.getSubmission().getAssayData().stream()
                .filter(ad -> ad.getArchive() == Archive.Ena)
                .forEach(ad -> processAssayData(ad, submissionEnvelope));
    }


    private void processStudy(Study study, SubmissionEnvelope submissionEnvelope) {

        if (!study.isAccessioned()) {
            study.setAccession("ENA-STU-" + UUID.randomUUID());
        }

        enaStudyRepository.save(study);
        study.setStatus(processedStatusValue);
    }


    private void processAssay(Assay assay, SubmissionEnvelope submissionEnvelope) {
        Submission submission = submissionEnvelope.getSubmission();

        assay.getSampleRef().fillIn(submission.getSamples());

        if (assay.getSampleRef().getReferencedObject() == null){
            assay.getSampleRef().fillIn(submissionEnvelope.getSupportingSamples());
        }

        assay.getStudyRef().fillIn(submission.getStudies());



        if (!assay.isAccessioned()) {
            assay.setAccession("ENA-EXP-" + UUID.randomUUID());
        }

        enaAssayRepository.save(assay);
        assay.setStatus(processedStatusValue);
    }

    private void processAssayData(AssayData assayData, SubmissionEnvelope submissionEnvelope) {
        assayData.getAssayRef().fillIn(submissionEnvelope.getSubmission().getAssays());

        if (!assayData.isAccessioned()) {
            assayData.setAccession("ENA-RUN-" + UUID.randomUUID());
        }

        enaAssayDataRepository.save(assayData);

        assayData.setStatus(processedStatusValue);
    }
}