package uk.ac.ebi.subs.api.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.ac.ebi.subs.data.status.SubmissionStatusEnum;
import uk.ac.ebi.subs.repository.model.Submission;
import uk.ac.ebi.subs.repository.model.SubmissionStatus;
import uk.ac.ebi.subs.repository.repos.SubmissionRepository;
import uk.ac.ebi.subs.repository.repos.status.SubmissionStatusRepository;

/**
 * Created by karoly on 12/07/2017.
 */
@Service
public class SubmissionServiceImpl implements SubmissionService {

    private SubmissionRepository submissionRepository;
    private SubmissionStatusRepository submissionStatusRepository;

    @Autowired
    public SubmissionServiceImpl(SubmissionRepository submissionRepository, SubmissionStatusRepository submissionStatusRepository) {
        this.submissionRepository = submissionRepository;
        this.submissionStatusRepository = submissionStatusRepository;
    }

    @Override
    public void setSubmissionToSubmitted(Submission submission) {
        submission.setSubmissionStatus(new SubmissionStatus());
        submission.getSubmissionStatus().setStatus(SubmissionStatusEnum.Submitted);

        submissionStatusRepository.save(submission.getSubmissionStatus());
        submissionRepository.save(submission);
    }
}
