package uk.ac.ebi.subs.api.services;

import uk.ac.ebi.subs.validator.data.ValidationResult;
import uk.ac.ebi.subs.repository.model.Submission;

import java.util.List;

/**
 * This class is responsible for the {@link ValidationResult} related services.
 * Created by karoly on 11/07/2017.
 */
public interface ValidationResultService {

    /**
     * Returns a list of {@link ValidationResult}s by the given submission ID.
     * @param submissionId the identifier of the submission to get the list of {@link ValidationResult}s
     * @return a list of {@link ValidationResult}s by the given submission ID.
     */
    List<ValidationResult> getValidationResultBySubmissionId(String submissionId);

    /**
     * Returns true if all the validation finished for the given {@link Submission}, otherwise false.
     * @param submissionStatusId identifier of the submission status. It will be used to get the submission by this id.
     * @return true if all the validation finished for the given {@link Submission}, otherwise false.
     */
    boolean isValidationFinished(String submissionStatusId);
}
