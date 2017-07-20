package uk.ac.ebi.subs.api.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.rest.core.annotation.HandleAfterDelete;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;
import uk.ac.ebi.subs.api.services.SubmissionEventService;
import uk.ac.ebi.subs.data.status.SubmissionStatusEnum;
import uk.ac.ebi.subs.repository.model.Submission;
import uk.ac.ebi.subs.repository.model.SubmissionStatus;
import uk.ac.ebi.subs.repository.repos.SubmissionRepository;
import uk.ac.ebi.subs.repository.repos.status.SubmissionStatusRepository;

import java.util.Date;
import java.util.UUID;

/**
 * Repo event handler for submissions in the api
 * <p>
 * * locks down changes to non-draft submissions, based on the OperationControlService
 * * send submissions off to rabbit after storing a submission with the 'Submitted' status
 */
@Component
@RepositoryEventHandler(Submission.class)
public class SubmissionEventHandler {


    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public SubmissionEventHandler(
            SubmissionRepository submissionRepository,
            SubmissionEventService submissionEventService,
            SubmissionStatusRepository submissionStatusRepository
    ) {
        this.submissionEventService = submissionEventService;
        this.submissionStatusRepository = submissionStatusRepository;
        this.submissionRepository = submissionRepository;
    }

    public void setSubmissionRepository(SubmissionRepository submissionRepository) {
        this.submissionRepository = submissionRepository;
    }

    public void setSubmissionEventService(SubmissionEventService submissionEventService) {
        this.submissionEventService = submissionEventService;
    }

    public void setSubmissionStatusRepository(SubmissionStatusRepository submissionStatusRepository) {
        this.submissionStatusRepository = submissionStatusRepository;
    }

    private SubmissionRepository submissionRepository;
    private SubmissionEventService submissionEventService;
    private SubmissionStatusRepository submissionStatusRepository;

    /**
     * Give submission an ID and draft status on creation
     *
     * @param submission
     */
    @HandleBeforeCreate
    public void handleBeforeCreate(Submission submission) {
        submission.setId(UUID.randomUUID().toString());
        submission.setCreatedDate(new Date());

        SubmissionStatus submissionStatus = new SubmissionStatus(SubmissionStatusEnum.Draft);
        submissionStatus.setId(UUID.randomUUID().toString());
        submissionStatusRepository.insert(submissionStatus);

        submission.setSubmissionStatus(submissionStatus);

        submissionEventService.submissionCreated(submission);
    }

    /**
     * make sure the submission is ready for storing
     * * give it an ID if it has not got one
     * * check it can be modified if there it already exists
     *
     * @param submission
     */
    @HandleBeforeSave
    public void handleBeforeSave(Submission submission) {

        Submission storedSubmission = submissionRepository.findOne(submission.getId());
        submission.setSubmissionStatus(storedSubmission.getSubmissionStatus());

        submissionEventService.submissionUpdated(submission);
    }

    @HandleAfterDelete
    public void handleBeforeDelete(Submission submission) {
        submissionEventService.submissionDeleted(submission);
    }


}
