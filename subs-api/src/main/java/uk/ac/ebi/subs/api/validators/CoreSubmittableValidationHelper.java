package uk.ac.ebi.subs.api.validators;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import uk.ac.ebi.subs.api.services.OperationControlService;
import uk.ac.ebi.subs.data.component.Archive;
import uk.ac.ebi.subs.data.status.StatusDescription;
import uk.ac.ebi.subs.data.submittable.Submittable;
import uk.ac.ebi.subs.repository.model.StoredSubmittable;
import uk.ac.ebi.subs.repository.repos.SubmissionRepository;
import uk.ac.ebi.subs.repository.repos.submittables.SubmittableRepository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Base validator for submitted items
 * <p>
 * Ensures that we have a submission ID and that it relates to a real submission
 * <p>
 * Note that we must supply a default message. Not having a message causes the client to get a 500 (server error)
 * status code instead of a 400 (bad request)
 */
@Component
public class CoreSubmittableValidationHelper {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private SubmissionRepository submissionRepository;
    private List<StatusDescription> processingStatuses;
    private List<StatusDescription> releaseStatuses;
    private OperationControlService operationControlService;

    @Autowired
    public CoreSubmittableValidationHelper(
            SubmissionRepository submissionRepository,
            List<StatusDescription> processingStatuses,
            List<StatusDescription> releaseStatuses,
            OperationControlService operationControlService) {
        this.submissionRepository = submissionRepository;
        this.processingStatuses = processingStatuses;
        this.releaseStatuses = releaseStatuses;
        this.operationControlService = operationControlService;
    }

    public void validate(StoredSubmittable target, SubmittableRepository repository, Errors errors) {
        StoredSubmittable storedVersion = null;

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "submission", "required", "submission is required");

        if (errors.hasErrors()){
            return;
        }

        if (target.getId() != null) {
            storedVersion = (StoredSubmittable) repository.findOne(target.getId());
        }

        this.validateAlias(target,repository,errors);

        this.validate(target, storedVersion, errors);
    }

    public void validateAlias(StoredSubmittable target, SubmittableRepository repository, Errors errors) {

        validateOnlyUseOfAliasInSubmission(target, repository, errors);

        validateArchiveIsLockedToAlias(target, repository, errors);
    }

    public void validateArchiveIsLockedToAlias(StoredSubmittable target, SubmittableRepository repository, Errors errors) {
        if (target.getAlias() == null ){
            return;
        }

        List<Archive> archives;

        try (Stream<? extends StoredSubmittable> itemsWithAliasStream = repository.streamByTeamNameAndAliasOrderByCreatedDateDesc(
                target.getSubmission().getTeam().getName(),
                target.getAlias()
        )){
            archives = itemsWithAliasStream
                    .map(Submittable::getArchive)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());
        }

        if (archives.size() > 1){
            throw new IllegalStateException("Multiple archives found in history of item "+target);
        }

        if (archives.size() == 1 && !target.getArchive().equals( archives.get(0) )){
            SubsApiErrors.invalid.addError(errors,"archive");
        }
    }

    public void validateOnlyUseOfAliasInSubmission(StoredSubmittable target, SubmittableRepository repository, Errors errors) {
        if (target.getAlias() == null || target.getSubmission() == null) {
            return;
        }

        List<? extends StoredSubmittable> itemsInSubmissionWithSameAlias = repository.findBySubmissionIdAndAlias
                (target.getSubmission().getId(),
                        target.getAlias()
                );

        Optional<? extends StoredSubmittable> itemWithSameAliasDifferentId = itemsInSubmissionWithSameAlias.stream()
                .filter(item -> !item.getId().equals(target.getId()))
                .findAny();

        if (itemWithSameAliasDifferentId.isPresent()) {
            SubsApiErrors.already_exists.addError(errors, "alias");
        }

    }


    public void validate(StoredSubmittable target, StoredSubmittable storedVersion, Errors errors) {
        logger.info("validate {}", target);
        StoredSubmittable submittable = (StoredSubmittable) target;


        if (submittable.getSubmission() != null && !operationControlService.isUpdateable(submittable.getSubmission())) {
            SubsApiErrors.resource_locked.addError(errors);
        }

        if (errors.hasErrors()) return;

        if (storedVersion != null && !operationControlService.isUpdateable(storedVersion)) {
            SubsApiErrors.resource_locked.addError(errors);
        }

        if (storedVersion != null) {
            validateAgainstStoredVersion(errors, submittable, storedVersion);

        }
    }

    private void validateAgainstStoredVersion(Errors errors, StoredSubmittable submittable, StoredSubmittable storedVersion) {

        ValidationHelper.thingCannotChange(
                (submittable.getSubmission() == null) ? null : submittable.getSubmission().getId(),
                (storedVersion.getSubmission() == null) ? null : storedVersion.getSubmission().getId(),
                "submission",
                errors
        );

        /*Yes, this is stupid
         * Spring Data Auditing is set for this object, but it doesn't maintain the createdDate on save
         */

        submittable.setCreatedDate(storedVersion.getCreatedDate());
        submittable.setCreatedBy(storedVersion.getCreatedBy());
    }
}
