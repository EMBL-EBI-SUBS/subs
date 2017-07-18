package uk.ac.ebi.subs.api.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleAfterCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;
import uk.ac.ebi.subs.api.services.SubmittableValidationDispatcher;
import uk.ac.ebi.subs.api.services.ValidationResultService;
import uk.ac.ebi.subs.repository.model.*;
import uk.ac.ebi.subs.repository.repos.status.ProcessingStatusRepository;
import uk.ac.ebi.subs.validator.data.ValidationResult;
import uk.ac.ebi.subs.validator.repository.ValidationResultRepository;

import java.util.HashMap;
import java.util.UUID;

@Component
@RepositoryEventHandler
public class CoreSubmittableEventHelper {

    public CoreSubmittableEventHelper(
            ProcessingStatusRepository processingStatusRepository,
            SubmittableValidationDispatcher submittableValidationDispatcher,
            ValidationResultRepository validationResultRepository) {
        this.processingStatusRepository = processingStatusRepository;
        this.submittableValidationDispatcher = submittableValidationDispatcher;
        this.validationResultRepository = validationResultRepository;
    }

    private ProcessingStatusRepository processingStatusRepository;
    private SubmittableValidationDispatcher submittableValidationDispatcher;
    private ValidationResultRepository validationResultRepository;


    /**
     * Give submittables an ID and draft status on creation
     *
     * @param submittable
     */
    @HandleBeforeCreate
    public void addDependentObjectsToSubmittable(StoredSubmittable submittable) {
        submittable.setId(UUID.randomUUID().toString());

        ValidationResult validationResult = new ValidationResult();
        validationResult.setEntityUuid(submittable.getId());
        validationResult.setUuid(UUID.randomUUID().toString());

        if (submittable.getSubmission() != null)
            validationResult.setSubmissionId(submittable.getSubmission().getId());
        validationResultRepository.save(validationResult);

        submittable.setValidationResult(validationResult);

        ProcessingStatus processingStatus = ProcessingStatus.createForSubmittable(submittable);
        processingStatus.setId(UUID.randomUUID().toString());
        processingStatusRepository.insert(processingStatus);

        setTeamFromSubmission(submittable);
    }

    // Validation of created submittables
    @HandleAfterCreate
    public void validateOnCreate(Sample sample) { submittableValidationDispatcher.validateCreate(sample); }

    @HandleAfterCreate
    public void validateOnCreate(Study study) { submittableValidationDispatcher.validateCreate(study); }

    @HandleAfterCreate
    public void validateOnCreate(Assay assay) {
        submittableValidationDispatcher.validateCreate(assay);
    }

    // Validation of updated submittables

    @HandleBeforeSave
    public void validateOnSave(Sample sample) { submittableValidationDispatcher.validateUpdate(sample); }

    @HandleBeforeSave
    public void validateOnSave(Study study) { submittableValidationDispatcher.validateUpdate(study); }

    @HandleBeforeSave
    public void validateOnSave(Assay assay) {
        submittableValidationDispatcher.validateUpdate(assay);
    }


    private void setTeamFromSubmission(StoredSubmittable submittable) {
        if (submittable.getSubmission() != null) {
            submittable.setTeam(submittable.getSubmission().getTeam());
        }
    }

    @HandleBeforeSave
    public void beforeSave(StoredSubmittable storedSubmittable) {
        setTeamFromSubmission(storedSubmittable);
    }
}
