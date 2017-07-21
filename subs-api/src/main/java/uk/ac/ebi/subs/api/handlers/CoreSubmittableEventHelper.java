package uk.ac.ebi.subs.api.handlers;

import org.springframework.data.rest.core.annotation.*;
import org.springframework.stereotype.Component;
import uk.ac.ebi.subs.api.services.SubmittableValidationDispatcher;
import uk.ac.ebi.subs.repository.model.*;
import uk.ac.ebi.subs.repository.repos.status.ProcessingStatusRepository;
import uk.ac.ebi.subs.repository.services.SubmittableHelperService;
import uk.ac.ebi.subs.validator.data.ValidationResult;
import uk.ac.ebi.subs.validator.repository.ValidationResultRepository;

import java.util.UUID;

@Component
@RepositoryEventHandler
public class CoreSubmittableEventHelper {


    public CoreSubmittableEventHelper(
                ProcessingStatusRepository processingStatusRepository,
                SubmittableValidationDispatcher submittableValidationDispatcher,
                ValidationResultRepository validationResultRepository) {
            this.processingStatusRepository = processingStatusRepository;
            this.submittableHelperService = submittableHelperService;
            this.submittableValidationDispatcher = submittableValidationDispatcher;
            this.validationResultRepository = validationResultRepository;
        }

        private ProcessingStatusRepository processingStatusRepository;
        private SubmittableHelperService submittableHelperService;
        private SubmittableValidationDispatcher submittableValidationDispatcher;
        private ValidationResultRepository validationResultRepository;


        /**
         * Give submittables an ID and draft status on creation
         *
         * @param submittable
         */
        @HandleBeforeCreate
        public void addDependentObjectsToSubmittable (StoredSubmittable submittable){
            submittableHelperService.setupNewSubmittable(submittable);
        }

        // Validation of created submittables
        @HandleAfterCreate
        public void validateOnCreate (Sample sample){
            submittableValidationDispatcher.validateCreate(sample);
        }

        @HandleAfterCreate
        public void validateOnCreate (Study study){
            submittableValidationDispatcher.validateCreate(study);
        }

        @HandleAfterCreate
        public void validateOnCreate (Assay assay){
            submittableValidationDispatcher.validateCreate(assay);
        }

        // Validation of updated submittables

        @HandleAfterSave
        public void validateOnSave (Sample sample){
            submittableValidationDispatcher.validateUpdate(sample);
        }

        @HandleAfterSave
        public void validateOnSave (Study study){
            submittableValidationDispatcher.validateUpdate(study);
        }

        @HandleAfterSave
        public void validateOnSave (Assay assay){
            submittableValidationDispatcher.validateUpdate(assay);
        }


        private void setTeamFromSubmission (StoredSubmittable submittable){
            if (submittable.getSubmission() != null) {
                submittable.setTeam(submittable.getSubmission().getTeam());
            }
        }

        @HandleBeforeSave
        public void beforeSave (StoredSubmittable storedSubmittable){
            submittableHelperService.setTeamFromSubmission(storedSubmittable);
        }
    }
