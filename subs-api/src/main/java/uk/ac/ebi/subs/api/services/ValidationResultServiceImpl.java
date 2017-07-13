package uk.ac.ebi.subs.api.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.ac.ebi.subs.repository.model.Submission;
import uk.ac.ebi.subs.repository.repos.SubmissionRepository;
import uk.ac.ebi.subs.validator.data.ValidationResult;
import uk.ac.ebi.subs.validator.data.ValidationStatus;
import uk.ac.ebi.subs.validator.repository.ValidationResultRepository;

import java.util.List;

/**
 * Created by karoly on 11/07/2017.
 */
@Service
public class ValidationResultServiceImpl implements ValidationResultService {

    private ValidationResultRepository validationResultRepository;
    private SubmissionRepository submissionRepository;

    @Autowired
    public ValidationResultServiceImpl(ValidationResultRepository validationResultRepository, SubmissionRepository submissionRepository) {
        this.validationResultRepository = validationResultRepository;
        this.submissionRepository = submissionRepository;
    }

    @Override
    public List<ValidationResult> getValidationResultBySubmissionId(String submissionId) {
        return validationResultRepository.findAllBySubmissionId(submissionId);
    }

    @Override
    public boolean isValidationFinished(String submissionStatusId) {
        Submission submission = submissionRepository.findBySubmissionStatusId(submissionStatusId);
        List<ValidationResult> validationResults = getValidationResultBySubmissionId(submission.getId());

        return validationResults.size() > 0 && isValidationPassed(validationResults);
    }

    private boolean isValidationPassed(List<ValidationResult> validationResults) {
        return validationResults.stream().filter(
                validationResult -> validationResult.getValidationStatus() != ValidationStatus.Pass)
                .findAny()
                .orElse(null) == null;
    }
}
