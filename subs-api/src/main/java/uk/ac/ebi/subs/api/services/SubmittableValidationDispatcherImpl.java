package uk.ac.ebi.subs.api.services;

import org.springframework.amqp.rabbit.core.RabbitMessagingTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.ac.ebi.subs.repository.model.Assay;
import uk.ac.ebi.subs.repository.model.Sample;
import uk.ac.ebi.subs.repository.model.Study;
import uk.ac.ebi.subs.validator.data.SubmittableValidationEnvelope;
import uk.ac.ebi.subs.validator.messaging.Exchanges;
import uk.ac.ebi.subs.validator.messaging.RoutingKeys;


/**
 * Created by rolando on 09/06/2017.
 */
@Component
public class SubmittableValidationDispatcherImpl implements SubmittableValidationDispatcher {

    private RabbitMessagingTemplate rabbitMessagingTemplate;

    @Autowired
    public SubmittableValidationDispatcherImpl(RabbitMessagingTemplate rabbitMessagingTemplate) {
        this.rabbitMessagingTemplate = rabbitMessagingTemplate;
    }

    @Override
    public void validateCreate(Sample sample) {
        SubmittableValidationEnvelope<Sample> validationEnvelope = new SubmittableValidationEnvelope<>(sample.getSubmission().getId(), sample);

        rabbitMessagingTemplate.convertAndSend(
                Exchanges.SUBMISSIONS,
                RoutingKeys.SUBMITTABLE_SAMPLE_CREATED,
                validationEnvelope);

    }

    @Override
    public void validateCreate(Study study) {
        SubmittableValidationEnvelope<Study> validationEnvelope = new SubmittableValidationEnvelope<>(study.getSubmission().getId(), study);

        rabbitMessagingTemplate.convertAndSend(
                Exchanges.SUBMISSIONS,
                RoutingKeys.SUBMITTABLE_STUDY_CREATED,
                validationEnvelope);

    }

    @Override
    public void validateCreate(Assay assay) {
        SubmittableValidationEnvelope<Assay> validationEnvelope = new SubmittableValidationEnvelope<>(assay.getSubmission().getId(), assay);

        rabbitMessagingTemplate.convertAndSend(
                Exchanges.SUBMISSIONS,
                RoutingKeys.SUBMITTABLE_ASSAY_CREATED,
                validationEnvelope);

    }

    @Override
    public void validateUpdate(Sample sample) {
        SubmittableValidationEnvelope<Sample> validationEnvelope = new SubmittableValidationEnvelope<>(sample.getSubmission().getId(), sample);

        rabbitMessagingTemplate.convertAndSend(
                Exchanges.SUBMISSIONS,
                RoutingKeys.SUBMITTABLE_SAMPLE_UPDATED,
                validationEnvelope);
    }

    @Override
    public void validateUpdate(Study study) {
        SubmittableValidationEnvelope<Study> validationEnvelope = new SubmittableValidationEnvelope<>(study.getSubmission().getId(), study);

        rabbitMessagingTemplate.convertAndSend(
                Exchanges.SUBMISSIONS,
                RoutingKeys.SUBMITTABLE_STUDY_UPDATED,
                validationEnvelope);

    }

    @Override
    public void validateUpdate(Assay assay) {
        SubmittableValidationEnvelope<Assay> validationEnvelope = new SubmittableValidationEnvelope<>(assay.getSubmission().getId(), assay);

        rabbitMessagingTemplate.convertAndSend(
                Exchanges.SUBMISSIONS,
                RoutingKeys.SUBMITTABLE_ASSAY_UPDATED,
                validationEnvelope);

    }

}
