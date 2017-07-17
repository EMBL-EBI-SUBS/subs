package uk.ac.ebi.subs.api.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.ac.ebi.subs.repository.model.Submission;
import uk.ac.ebi.subs.repository.repos.SubmissionRepository;
import uk.ac.ebi.subs.validator.data.SingleValidationResult;
import uk.ac.ebi.subs.validator.data.ValidationAuthor;
import uk.ac.ebi.subs.validator.data.ValidationResult;
import uk.ac.ebi.subs.validator.data.ValidationStatus;
import uk.ac.ebi.subs.validator.repository.ValidationResultRepository;

import java.util.List;
import java.util.Map;
import java.util.Set;

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
    public boolean isValidationFinishedAndPassed(String submissionStatusId) {
        Submission submission = submissionRepository.findBySubmissionStatusId(submissionStatusId);
        List<ValidationResult> validationResults = getValidationResultBySubmissionId(submission.getId());

        return validationResults.size() > 0 && isValidationCompleted(validationResults) && isValidationPassed(validationResults);
    }

    private boolean isValidationCompleted(List<ValidationResult> validationResults) {
        return validationResults.stream().filter(
                validationResult -> validationResult.getValidationStatus() != ValidationStatus.Complete)
                .findAny()
                .orElse(null) == null;
    }

    private boolean isValidationPassed(List<ValidationResult> validationResults) {
        for (ValidationResult validationResult : validationResults) {
            Map<ValidationAuthor, List<SingleValidationResult>> expectedResults = validationResult.getExpectedResults();
            Set<ValidationAuthor> authors = expectedResults.keySet();
            for ( ValidationAuthor validationAuthor : authors) {
                List<SingleValidationResult> singleValidationResults = expectedResults.get(validationAuthor);
                if (singleValidationResults.size() == 0) {
                    return false;
                }
                boolean isPassed = singleValidationResults.stream()
                        .filter(singleValidationResult -> singleValidationResult.getValidationStatus().equals(ValidationStatus.Error))
                        .findFirst()
                        .orElse(null) == null;
                if (!isPassed) {
                    return false;
                }
            }
        }

        return true;
    }
}
