package uk.ac.ebi.subs.repository.security;

import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Component;
import uk.ac.ebi.subs.data.component.Team;
import uk.ac.ebi.subs.repository.model.ProcessingStatus;
import uk.ac.ebi.subs.repository.model.StoredSubmittable;
import uk.ac.ebi.subs.repository.model.Submission;
import uk.ac.ebi.subs.repository.model.SubmissionStatus;
import uk.ac.ebi.subs.repository.repos.SubmissionRepository;
import uk.ac.ebi.subs.repository.repos.submittables.SubmittableRepository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
public class TeamNameExtractor {

    public TeamNameExtractor(List<SubmittableRepository<?>> submissionContentsRepositories, SubmissionRepository submissionRepository) {
        this.submissionContentsRepositories = submissionContentsRepositories;
        this.submissionRepository = submissionRepository;
    }

    private List<SubmittableRepository<?>> submissionContentsRepositories;
    private SubmissionRepository submissionRepository;

    public static final String ADMIN_USER_DOMAIN_NAME = "usiAdmin";


    public String adminRole() {
        return ADMIN_USER_DOMAIN_NAME;
    }


    public String processingStatusTeam(ProcessingStatus processingStatus) {
        Optional<Team> optionalTeam = submissionContentsRepositories //TODO can we be more targetted here?
                .stream()
                .map(repo -> (StoredSubmittable) repo.findOne(processingStatus.getSubmittableId()))
                .filter(Objects::nonNull)
                .map(storedSubmittable -> storedSubmittable.getTeam())
                .filter(Objects::nonNull)
                .findFirst();

        if (!optionalTeam.isPresent()) {
            throw new IllegalStateException("Cannot find owning team for processing status during authorization " + processingStatus.toString());
        }
        return optionalTeam.get().getName();
    }

    public String submissionStatusTeam(SubmissionStatus submissionStatus) {
        Submission submission = submissionRepository.findBySubmissionStatusId(submissionStatus.getId());

        if (submission == null) throw new ResourceNotFoundException();

        return submission.getTeam().getName();
    }

    public String submissionIdTeam(String submissionId) {
        Submission submission = submissionRepository.findOne(submissionId);

        if (submission == null) throw new ResourceNotFoundException();

        return submission.getTeam().getName();
    }
}
