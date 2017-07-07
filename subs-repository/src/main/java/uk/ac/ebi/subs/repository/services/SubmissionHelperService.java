package uk.ac.ebi.subs.repository.services;

import org.springframework.stereotype.Component;
import uk.ac.ebi.subs.data.component.Submitter;
import uk.ac.ebi.subs.data.component.Team;
import uk.ac.ebi.subs.data.status.SubmissionStatusEnum;
import uk.ac.ebi.subs.repository.model.Submission;
import uk.ac.ebi.subs.repository.model.SubmissionStatus;
import uk.ac.ebi.subs.repository.repos.SubmissionRepository;
import uk.ac.ebi.subs.repository.repos.status.SubmissionStatusRepository;

import java.util.Date;
import java.util.UUID;


@Component
public class SubmissionHelperService {

    private SubmissionStatusRepository submissionStatusRepository;
    private SubmissionRepository submissionRepository;

    public Submission createSubmission(Team team, Submitter submitter){
        Submission submission = new Submission();
        setupNewSubmission(submission);
        submissionRepository.insert(submission);
        return submission;
    }

    public void setupNewSubmission(Submission submission){
        submission.setId(UUID.randomUUID().toString());
        submission.setCreatedDate(new Date());

        SubmissionStatus submissionStatus = new SubmissionStatus(SubmissionStatusEnum.Draft);
        submissionStatus.setId(UUID.randomUUID().toString());
        submissionStatusRepository.insert(submissionStatus);

        submission.setSubmissionStatus(submissionStatus);
    }
}
