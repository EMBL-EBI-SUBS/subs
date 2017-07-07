package uk.ac.ebi.subs.repository.services;

import org.springframework.stereotype.Component;
import uk.ac.ebi.subs.repository.model.ProcessingStatus;
import uk.ac.ebi.subs.repository.model.StoredSubmittable;
import uk.ac.ebi.subs.repository.repos.status.ProcessingStatusRepository;

import java.util.UUID;

@Component
public class SubmittableHelperService {

    public SubmittableHelperService(ProcessingStatusRepository processingStatusRepository) {
        this.processingStatusRepository = processingStatusRepository;
    }

    private ProcessingStatusRepository processingStatusRepository;

    public void setupNewSubmittable(StoredSubmittable submittable){
        submittable.setId(UUID.randomUUID().toString());

        ProcessingStatus processingStatus = ProcessingStatus.createForSubmittable(submittable);
        processingStatus.setId(UUID.randomUUID().toString());
        processingStatusRepository.insert(processingStatus);

        setTeamFromSubmission(submittable);
    }

    private void setTeamFromSubmission(StoredSubmittable submittable) {
        if (submittable.getSubmission() != null) {
            submittable.setTeam(submittable.getSubmission().getTeam());
        }
    }
}