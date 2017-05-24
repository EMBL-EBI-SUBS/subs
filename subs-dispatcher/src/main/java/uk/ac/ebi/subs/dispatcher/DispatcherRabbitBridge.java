package uk.ac.ebi.subs.dispatcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitMessagingTemplate;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.stereotype.Service;
import uk.ac.ebi.subs.data.component.Archive;
import uk.ac.ebi.subs.data.component.SampleRef;
import uk.ac.ebi.subs.data.component.SampleUse;
import uk.ac.ebi.subs.messaging.Exchanges;
import uk.ac.ebi.subs.messaging.Queues;
import uk.ac.ebi.subs.messaging.Topics;
import uk.ac.ebi.subs.processing.SubmissionEnvelope;
import uk.ac.ebi.subs.repository.RefLookupService;
import uk.ac.ebi.subs.repository.model.Submission;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Dispatcher looks at the state of a submission and works out which archives need to handle it next.
 * This can be for the purposes of getting supporting information, or for archiving
 */
@Service
public class DispatcherRabbitBridge {

    private static final Logger logger = LoggerFactory.getLogger(DispatcherRabbitBridge.class);

    RabbitMessagingTemplate rabbitMessagingTemplate;
    private DispatcherService dispatcherService;
    private RefLookupService refLookupService;



    public DispatcherRabbitBridge(
            RabbitMessagingTemplate rabbitMessagingTemplate,
            MessageConverter messageConverter,
            DispatcherService dispatcherService

    ) {
        this.rabbitMessagingTemplate = rabbitMessagingTemplate;
        this.rabbitMessagingTemplate.setMessageConverter(messageConverter);
        this.dispatcherService = dispatcherService;
    }

    /**
     * Determine what supporting information is required from the archvies
     * @param submission
     */
    @RabbitListener(queues = Queues.SUBMISSION_SUBMITTED_CHECK_SUPPORTING_INFO)
    public void checkSupportingInfoRequirement(Submission submission) {
        logger.info("checkSupportingInfoRequirement {}", submission);


        Map<Archive,SubmissionEnvelope> submissionEnvelopesForArchives = dispatcherService.determineSupportingInformationRequired(submission);

        if (!submissionEnvelopesForArchives.containsKey(Archive.BioSamples)){
            return;
        }

        //TODO only handles BioSamples
        SubmissionEnvelope submissionEnvelope = submissionEnvelopesForArchives.get(Archive.BioSamples);

        if (submissionEnvelope.getSupportingSamplesRequired().isEmpty()){
            return;
        }

        rabbitMessagingTemplate.convertAndSend(
                Exchanges.SUBMISSIONS,
                Topics.EVENT_SUBMISSION_NEEDS_SAMPLES,
                submissionEnvelope
        );

    }

    /**
     * For a submission, assess which archives can be sent information for archiving. Send them the information
     * as a message
     *
     * @param submission
     */
    @RabbitListener(queues = Queues.SUBMISSION_DISPATCHER)
    public void dispatchToArchives(Submission submission) {

        logger.info("dispatchToArchives {}", submission);


        Map<Archive,SubmissionEnvelope> readyToDispatch = dispatcherService
                .assessDispatchReadiness(submission);

        Map<Archive,String> archiveTopic = new HashMap<>();
        archiveTopic.put(Archive.BioSamples,Topics.SAMPLES_PROCESSING);
        archiveTopic.put(Archive.Ena,Topics.ENA_PROCESSING);
        archiveTopic.put(Archive.ArrayExpress,Topics.AE_PROCESSING);


        for (Map.Entry<Archive,SubmissionEnvelope> entry : readyToDispatch.entrySet()){

            Archive archive = entry.getKey();
            SubmissionEnvelope submissionEnvelopeToTransmit = entry.getValue();

            if(!archiveTopic.containsKey(archive)){
                throw new IllegalStateException("Dispatcher does not have topic mapping for archive "+archive+". Processing submission "+submission.getId());
            }

            String targetTopic = archiveTopic.get(archive);

            dispatcherService.insertReferencedSamples(submissionEnvelopeToTransmit);

            rabbitMessagingTemplate.convertAndSend(Exchanges.SUBMISSIONS, targetTopic, submissionEnvelopeToTransmit);
            logger.info("sent submission {} to {}", submission.getId(), targetTopic);

            dispatcherService.updateSubmittablesStatusToSubmitted(archive,submissionEnvelopeToTransmit);
        }
    }
}