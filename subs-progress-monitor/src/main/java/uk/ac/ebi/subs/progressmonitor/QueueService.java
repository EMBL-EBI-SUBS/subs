package uk.ac.ebi.subs.progressmonitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitMessagingTemplate;
import org.springframework.stereotype.Component;
import uk.ac.ebi.subs.repository.model.ProcessingStatus;
import uk.ac.ebi.subs.repository.model.StoredSubmittable;
import uk.ac.ebi.subs.repository.model.Submission;

import uk.ac.ebi.subs.messaging.Exchanges;
import uk.ac.ebi.subs.messaging.Queues;
import uk.ac.ebi.subs.messaging.Topics;
import uk.ac.ebi.subs.processing.ProcessingCertificate;
import uk.ac.ebi.subs.processing.ProcessingCertificateEnvelope;
import uk.ac.ebi.subs.processing.SubmissionEnvelope;
import uk.ac.ebi.subs.repository.SubmissionEnvelopeService;
import uk.ac.ebi.subs.repository.repos.SubmissionRepository;
import uk.ac.ebi.subs.repository.model.SubmissionStatus;
import uk.ac.ebi.subs.repository.processing.SupportingSample;
import uk.ac.ebi.subs.repository.processing.SupportingSampleRepository;
import uk.ac.ebi.subs.repository.repos.status.ProcessingStatusRepository;
import uk.ac.ebi.subs.repository.repos.status.SubmissionStatusRepository;
import uk.ac.ebi.subs.repository.repos.submittables.SubmittablesBulkOperations;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class QueueService {
    private static final Logger logger = LoggerFactory.getLogger(QueueService.class);


    public QueueService(List<Class<? extends StoredSubmittable>> submittablesClassList, SubmissionRepository submissionRepository,
                        SupportingSampleRepository supportingSampleRepository, SubmissionEnvelopeService submissionEnvelopeService,
                        ProcessingStatusRepository processingStatusRepository, SubmittablesBulkOperations submittablesBulkOperations,
                        SubmissionStatusRepository submissionStatusRepository, RabbitMessagingTemplate rabbitMessagingTemplate) {
        this.submittablesClassList = submittablesClassList;
        this.submissionRepository = submissionRepository;
        this.supportingSampleRepository = supportingSampleRepository;
        this.submissionEnvelopeService = submissionEnvelopeService;
        this.processingStatusRepository = processingStatusRepository;
        this.submittablesBulkOperations = submittablesBulkOperations;
        this.submissionStatusRepository = submissionStatusRepository;
        this.rabbitMessagingTemplate = rabbitMessagingTemplate;
    }

    private List<Class<? extends StoredSubmittable>> submittablesClassList;
    private SubmissionRepository submissionRepository;
    private SupportingSampleRepository supportingSampleRepository;
    private SubmissionEnvelopeService submissionEnvelopeService;
    private ProcessingStatusRepository processingStatusRepository;
    private SubmittablesBulkOperations submittablesBulkOperations;
    private SubmissionStatusRepository submissionStatusRepository;
    private RabbitMessagingTemplate rabbitMessagingTemplate;

    @RabbitListener(queues = Queues.SUBMISSION_MONITOR_STATUS_UPDATE)
    public void submissionStatusUpdated(ProcessingCertificate processingCertificate) {
        if (processingCertificate.getSubmittableId() == null) return;

        Submission submission = submissionRepository.findOne(processingCertificate.getSubmittableId());

        if (submission == null) return;

        SubmissionStatus submissionStatus = submission.getSubmissionStatus();
        submissionStatus.setStatus(processingCertificate.getProcessingStatus().name()); //TODO rewrite this to use submission status

        submissionStatusRepository.save(submissionStatus);
    }

    @RabbitListener(queues = Queues.SUBMISSION_SUPPORTING_INFO_PROVIDED)
    public void handleSupportingInfo(SubmissionEnvelope submissionEnvelope) {

        final String submissionId = submissionEnvelope.getSubmission().getId();


        List<SupportingSample> supportingSamples = submissionEnvelope.getSupportingSamples().stream()
                .map(s -> new SupportingSample(submissionId, s))
                .collect(Collectors.toList());

        //store supporting info,
        logger.info(
                "storing supporting sample info for submission {}, {} samples",
                submissionEnvelope.getSubmission().getId(),
                supportingSamples.size()
        );

        supportingSampleRepository.save(supportingSamples);

        //send submission to the dispatcher

        sendSubmissionUpdated(submissionId);
    }


    @RabbitListener(queues = Queues.SUBMISSION_MONITOR)
    public void checkForProcessedSubmissions(ProcessingCertificateEnvelope processingCertificateEnvelope) {

        logger.info("received agent results for submission {} with {} certificates ",
                processingCertificateEnvelope.getSubmissionId(), processingCertificateEnvelope.getProcessingCertificates().size());


        for (ProcessingCertificate cert : processingCertificateEnvelope.getProcessingCertificates()){
            ProcessingStatus processingStatus = processingStatusRepository.findBySubmittableId(cert.getSubmittableId());

            if (cert.getAccession() != null){
                processingStatus.setAccession(cert.getAccession());
            }

            processingStatus.setArchive(cert.getArchive().name());
            processingStatus.setMessage(cert.getMessage());

            processingStatus.setStatus(cert.getProcessingStatus());

            processingStatus.setLastModifiedBy(cert.getArchive().name());
            processingStatus.setLastModifiedDate(new Date());

            processingStatusRepository.save(processingStatus);
        }

        for (Class submittableClass : submittablesClassList) {
            submittablesBulkOperations.applyProcessingCertificates(processingCertificateEnvelope, submittableClass);
        }

        sendSubmissionUpdated(processingCertificateEnvelope.getSubmissionId());
    }

    /**
     * Submission or it's supporting information has been updated
     * <p>
     * Recreate the submission envelope from storage and send it as a message
     *
     * @param submissionId
     */
    private void sendSubmissionUpdated(String submissionId) {

        rabbitMessagingTemplate.convertAndSend(
                Exchanges.SUBMISSIONS,
                Topics.EVENT_SUBMISSION_PROCESSING_UPDATED,
                submissionRepository.findOne(submissionId)
        );

        logger.info("submission {} update message sent", submissionId);
    }

}
