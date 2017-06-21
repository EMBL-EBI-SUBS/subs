package uk.ac.ebi.subs.api.services;

import org.springframework.amqp.rabbit.core.RabbitMessagingTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.ac.ebi.subs.data.dto.AssayDto;
import uk.ac.ebi.subs.data.dto.SubmittableDtoMapper;
import uk.ac.ebi.subs.data.dto.SampleDto;
import uk.ac.ebi.subs.data.dto.StudyDto;
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
    private SubmittableDtoMapper submittableDtoMapper;

    @Autowired
    public SubmittableValidationDispatcherImpl(RabbitMessagingTemplate rabbitMessagingTemplate, SubmittableDtoMapper submittableDtoMapper) {
        this.rabbitMessagingTemplate = rabbitMessagingTemplate;
        this.submittableDtoMapper = submittableDtoMapper;
    }

    @Override
    public void validateCreate(Sample sample) {
        SubmittableValidationEnvelope<SampleDto> validationEnvelope = new SubmittableValidationEnvelope<>(sample.getSubmission().getId(), submittableDtoMapper.toDto(sample));

        rabbitMessagingTemplate.convertAndSend(
                Exchanges.SUBMISSIONS,
                RoutingKeys.SUBMITTABLE_SAMPLE_CREATED,
                validationEnvelope);

    }

    @Override
    public void validateCreate(Study study) {
        SubmittableValidationEnvelope<StudyDto> validationEnvelope = new SubmittableValidationEnvelope<>(study.getSubmission().getId(), submittableDtoMapper.toDto(study));

        rabbitMessagingTemplate.convertAndSend(
                Exchanges.SUBMISSIONS,
                RoutingKeys.SUBMITTABLE_STUDY_CREATED,
                validationEnvelope);

    }

    @Override
    public void validateCreate(Assay assay) {
        SubmittableValidationEnvelope<AssayDto> validationEnvelope = new SubmittableValidationEnvelope<>(assay.getSubmission().getId(), submittableDtoMapper.toDto(assay));

        rabbitMessagingTemplate.convertAndSend(
                Exchanges.SUBMISSIONS,
                RoutingKeys.SUBMITTABLE_ASSAY_CREATED,
                validationEnvelope);

    }

    @Override
    public void validateUpdate(Sample sample) {
        SubmittableValidationEnvelope<SampleDto> validationEnvelope = new SubmittableValidationEnvelope<>(sample.getSubmission().getId(), submittableDtoMapper.toDto(sample));

        rabbitMessagingTemplate.convertAndSend(
                Exchanges.SUBMISSIONS,
                RoutingKeys.SUBMITTABLE_SAMPLE_UPDATED,
                validationEnvelope);
    }

    @Override
    public void validateUpdate(Study study) {
        SubmittableValidationEnvelope<StudyDto> validationEnvelope = new SubmittableValidationEnvelope<>(study.getSubmission().getId(), submittableDtoMapper.toDto(study));

        rabbitMessagingTemplate.convertAndSend(
                Exchanges.SUBMISSIONS,
                RoutingKeys.SUBMITTABLE_STUDY_UPDATED,
                validationEnvelope);

    }

    @Override
    public void validateUpdate(Assay assay) {
        SubmittableValidationEnvelope<AssayDto> validationEnvelope = new SubmittableValidationEnvelope<>(assay.getSubmission().getId(), submittableDtoMapper.toDto(assay));

        rabbitMessagingTemplate.convertAndSend(
                Exchanges.SUBMISSIONS,
                RoutingKeys.SUBMITTABLE_ASSAY_UPDATED,
                validationEnvelope);

    }

}
