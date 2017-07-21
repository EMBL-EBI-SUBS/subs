package uk.ac.ebi.subs.repository.services;

import org.springframework.stereotype.Component;
import uk.ac.ebi.subs.repository.model.ProcessingStatus;
import uk.ac.ebi.subs.repository.model.StoredSubmittable;
import uk.ac.ebi.subs.repository.repos.status.ProcessingStatusRepository;
import uk.ac.ebi.subs.validator.data.ValidationResult;
import uk.ac.ebi.subs.validator.repository.ValidationResultRepository;

import java.util.UUID;

@Component
public class SubmittableHelperService {

    public SubmittableHelperService(ProcessingStatusRepository processingStatusRepository, ValidationResultRepository validationResultRepository) {
        this.processingStatusRepository = processingStatusRepository;
        this.validationResultRepository = validationResultRepository;
    }

    private ProcessingStatusRepository processingStatusRepository;
    private ValidationResultRepository validationResultRepository;

    public void setupNewSubmittable(StoredSubmittable submittable){
        submittable.setId(UUID.randomUUID().toString());

        createProcessingStatus(submittable);
        createValidationResult(submittable);

        setTeamFromSubmission(submittable);
    }

    private void createValidationResult(StoredSubmittable submittable) {
        ValidationResult validationResult = new ValidationResult();
        validationResult.setEntityUuid(submittable.getId());
        validationResult.setUuid(UUID.randomUUID().toString());

        if (submittable.getSubmission() != null)
            validationResult.setSubmissionId(submittable.getSubmission().getId());
        validationResultRepository.save(validationResult);

        submittable.setValidationResult(validationResult);
    }

    private void createProcessingStatus(StoredSubmittable submittable) {
        ProcessingStatus processingStatus = ProcessingStatus.createForSubmittable(submittable);
        processingStatus.setId(UUID.randomUUID().toString());
        processingStatusRepository.insert(processingStatus);
    }

    public void setTeamFromSubmission(StoredSubmittable submittable) {
        if (submittable.getSubmission() != null) {
            submittable.setTeam(submittable.getSubmission().getTeam());
        }
    }
}