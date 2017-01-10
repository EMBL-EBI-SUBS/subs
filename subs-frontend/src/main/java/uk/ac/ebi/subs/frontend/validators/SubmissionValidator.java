package uk.ac.ebi.subs.frontend.validators;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import uk.ac.ebi.subs.data.Submission;
import uk.ac.ebi.subs.data.status.Status;
import uk.ac.ebi.subs.repository.SubmissionRepository;

import java.util.List;
import java.util.Optional;

@Component
public class SubmissionValidator implements Validator {

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private DomainValidator domainValidator;

    @Autowired
    private SubmitterValidator submitterValidator;

    @Autowired
    private List<Status> submissionStatuses;

    @Override
    public void validate(Object target, Errors errors) {

        Submission submission = (Submission) target;

        try {
            errors.pushNestedPath("domain");
            ValidationUtils.invokeValidator(this.domainValidator, submission.getDomain(), errors);
        } finally {
            errors.popNestedPath();
        }

        try {
            errors.pushNestedPath("submitter");
            ValidationUtils.invokeValidator(this.submitterValidator, submission.getSubmitter(), errors);
        } finally {
            errors.popNestedPath();
        }

        if (submission.getId() != null) {
            Submission storedVersion = submissionRepository.findOne(submission.getId());

            if (storedVersion != null) {
                validateAgainstStoredVersion(submission, storedVersion, errors);
            }
        }


    }

    private void validateAgainstStoredVersion(Submission target, Submission storedVersion, Errors errors) {

        submitterCannotChange(target, storedVersion, errors);

        domainCannotChange(target, storedVersion, errors);

        statusChangeMustBePermitted(target, storedVersion, errors);

        createdDateCannotChange(target, storedVersion, errors);

        submittedDateCannotChange(target, storedVersion, errors);
    }

    private void statusChangeMustBePermitted(Submission target, Submission storedVersion, Errors errors) {
        ValidationHelper.validateStatusChange(
                target.getStatus(),
                storedVersion.getStatus(),
                submissionStatuses,
                "status",
                errors
        );
    }

    private void submitterCannotChange(Submission target, Submission storedVersion, Errors errors) {
        ValidationHelper.thingCannotChange(
                target.getSubmitter(),
                storedVersion.getSubmitter(),
                "submitter",
                errors
        );
    }

    private void domainCannotChange(Submission target, Submission storedVersion, Errors errors) {
        ValidationHelper.thingCannotChange(
                target.getDomain(),
                storedVersion.getDomain(),
                "domain",
                errors
        );
    }

    private void createdDateCannotChange(Submission target, Submission storedVersion, Errors errors) {
        ValidationHelper.thingCannotChange(
                target.getCreatedDate(),
                storedVersion.getCreatedDate(),
                "createdDate",
                errors
        );
    }

    private void submittedDateCannotChange(Submission target, Submission storedVersion, Errors errors) {
        ValidationHelper.thingCannotChange(
                target.getSubmissionDate(),
                storedVersion.getSubmissionDate(),
                "submissionDate",
                errors
        );
    }






    @Override
    public boolean supports(Class<?> clazz) {
        return Submission.class.isAssignableFrom(clazz);
    }
}
