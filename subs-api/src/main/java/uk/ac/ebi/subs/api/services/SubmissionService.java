package uk.ac.ebi.subs.api.services;

import uk.ac.ebi.subs.repository.model.Submission;

/**
 * Created by karoly on 12/07/2017.
 */
public interface SubmissionService {

    void setSubmissionToSubmitted(Submission submission);
}
