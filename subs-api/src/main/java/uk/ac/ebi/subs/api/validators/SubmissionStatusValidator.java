package uk.ac.ebi.subs.api.validators;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import uk.ac.ebi.subs.api.services.OperationControlService;
import uk.ac.ebi.subs.api.services.ValidationResultService;
import uk.ac.ebi.subs.data.status.StatusDescription;
import uk.ac.ebi.subs.repository.model.SubmissionStatus;
import uk.ac.ebi.subs.repository.repos.status.SubmissionStatusRepository;

import java.util.Map;

@Component
public class SubmissionStatusValidator implements Validator {

    @Autowired
    public SubmissionStatusValidator(
            SubmissionStatusRepository submissionStatusRepository,
            OperationControlService operationControlService,
            ValidationResultService validationResultService,
            Map<String, StatusDescription> submissionStatusDescriptionMap
    ) {
        this.submissionStatusRepository = submissionStatusRepository;
        this.submissionStatusDescriptionMap = submissionStatusDescriptionMap;
        this.validationResultService = validationResultService;
    }


    private Map<String, StatusDescription> submissionStatusDescriptionMap;
    private SubmissionStatusRepository submissionStatusRepository;
    private OperationControlService operationControlService;
    private ValidationResultService validationResultService;

    @Override
    public boolean supports(Class<?> clazz) {
        return SubmissionStatus.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        /* unchecked */
        SubmissionStatus submissionStatus = (SubmissionStatus) target;

        SubsApiErrors.rejectIfEmptyOrWhitespace(errors,"status");

        if (errors.hasErrors()) return;

        String targetStatusName = submissionStatus.getStatus();

        if (!submissionStatusDescriptionMap.containsKey(targetStatusName)) {
            SubsApiErrors.invalid.addError(errors,"status");
            return;
        }

        SubmissionStatus currentSubmissionStatus = submissionStatusRepository.findOne(submissionStatus.getId());
        StatusDescription currentStatusDescription = submissionStatusDescriptionMap.get(currentSubmissionStatus.getStatus());

        submissionStatus.setCreatedDate(currentSubmissionStatus.getCreatedDate());
        submissionStatus.setCreatedBy(currentSubmissionStatus.getCreatedBy());

        if (!currentStatusDescription.isUserTransitionPermitted(targetStatusName)) {
            SubsApiErrors.invalid.addError(errors,"status");
            return;
        }

        if (!validationResultService.isValidationFinishedAndPassed(submissionStatus.getId())) {
            SubsApiErrors.invalid.addError(errors,"status");
            return;
        }
    }
}
