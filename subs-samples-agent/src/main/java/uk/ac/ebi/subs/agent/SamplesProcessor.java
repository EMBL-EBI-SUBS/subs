package uk.ac.ebi.subs.agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitMessagingTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.stereotype.Component;
import uk.ac.ebi.subs.agent.services.FetchService;
import uk.ac.ebi.subs.agent.services.SubmissionService;
import uk.ac.ebi.subs.agent.services.UpdateService;
import uk.ac.ebi.subs.data.Submission;
import uk.ac.ebi.subs.data.component.Attribute;
import uk.ac.ebi.subs.data.submittable.Sample;
import uk.ac.ebi.subs.messaging.Exchanges;
import uk.ac.ebi.subs.messaging.Topics;
import uk.ac.ebi.subs.processing.ProcessingCertificate;
import uk.ac.ebi.subs.processing.SubmissionEnvelope;
import uk.ac.ebi.subs.processing.UpdatedSamplesEnvelope;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class SamplesProcessor {
    private static final Logger logger = LoggerFactory.getLogger(SamplesProcessor.class);

    private RabbitMessagingTemplate rabbitMessagingTemplate;

    @Autowired
    SubmissionService submissionService;
    @Autowired
    UpdateService updateService;
    @Autowired
    FetchService fetchService;

    @Autowired
    CertificatesGenerator certificatesGenerator;

    @Autowired
    public SamplesProcessor(RabbitMessagingTemplate rabbitMessagingTemplate, MessageConverter messageConverter) {
        this.rabbitMessagingTemplate = rabbitMessagingTemplate;
        this.rabbitMessagingTemplate.setMessageConverter(messageConverter);
    }

    protected List<ProcessingCertificate> processSamples(SubmissionEnvelope envelope) {
        Submission submission = envelope.getSubmission();

        logger.debug("Processing {} samples from {} submission", envelope.getSamples().size(), submission.getId());

        List<ProcessingCertificate> certificates = new ArrayList<>();

        // Set updateDate
        for (Sample sample : envelope.getSamples()) {
            Attribute attribute = new Attribute();
            attribute.setName("update");
            attribute.setValue(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
            sample.getAttributes().add(attribute);
        }

        // Update
        List<Sample> samplesToUpdate = envelope.getSamples().stream()
                .filter(s -> (s.isAccessioned()))
                .collect(Collectors.toList());

        List<Sample> updatedSamples = updateService.update(samplesToUpdate);
        announceSampleUpdate(submission.getId(), updatedSamples);

        certificates.addAll(certificatesGenerator.generateCertificates(updatedSamples));

        // Submission
        List<Sample> samplesToSubmit = envelope.getSamples().stream()
                .filter(s -> !s.isAccessioned())
                .collect(Collectors.toList());

        List<Sample> submittedSamples = submissionService.submit(samplesToSubmit);

        certificates.addAll(certificatesGenerator.generateCertificates(submittedSamples));

        return certificates;
    }

    protected List<Sample> findSamples(SubmissionEnvelope envelope) {
        logger.debug("Finding {} samples from {} submission", envelope.getSupportingSamplesRequired().size(), envelope.getSubmission().getId());

        List<String> accessions = new ArrayList<>();

        envelope.getSupportingSamplesRequired().forEach(sampleRef -> accessions.add(sampleRef.getAccession()));

        return fetchService.findSamples(accessions);
    }

    private void announceSampleUpdate(String submissionId, List<Sample> updatedSamples) {
        if (!updatedSamples.isEmpty()) {
            UpdatedSamplesEnvelope updatedSamplesEnvelope = new UpdatedSamplesEnvelope();
            updatedSamplesEnvelope.setSubmissionId(submissionId);
            updatedSamplesEnvelope.setUpdatedSamples(updatedSamples);

            logger.debug("Submission {} with {} samples updates", submissionId, updatedSamples.size());

            rabbitMessagingTemplate.convertAndSend(Exchanges.SUBMISSIONS, Topics.EVENT_SAMPLES_UPDATED, updatedSamplesEnvelope);

        }
    }

}
