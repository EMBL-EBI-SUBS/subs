package uk.ac.ebi.subs.api.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;
import uk.ac.ebi.subs.repository.model.ProcessingStatus;
import uk.ac.ebi.subs.repository.model.StoredSubmittable;
import uk.ac.ebi.subs.repository.repos.status.ProcessingStatusRepository;
import uk.ac.ebi.subs.repository.services.SubmittableHelperService;

import java.util.UUID;

@Component
@RepositoryEventHandler
public class CoreSubmittableEventHelper {


    public CoreSubmittableEventHelper(ProcessingStatusRepository processingStatusRepository) {
        this.processingStatusRepository = processingStatusRepository;
    }

    private ProcessingStatusRepository processingStatusRepository;
    private SubmittableHelperService submittableHelperService;

    /**
     * Give submittables an ID and draft status on creation
     *
     * @param submittable
     */
    @HandleBeforeCreate
    public void beforeCreate(StoredSubmittable submittable) {
        submittableHelperService.setupNewSubmittable(submittable);
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
