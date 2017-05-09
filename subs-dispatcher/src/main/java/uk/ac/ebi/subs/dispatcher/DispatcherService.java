package uk.ac.ebi.subs.dispatcher;

import uk.ac.ebi.subs.data.Submission;
import uk.ac.ebi.subs.data.component.Archive;
import uk.ac.ebi.subs.processing.SubmissionEnvelope;


import java.util.Map;

/**
 * Created by davidr on 27/03/2017.
 */
public interface DispatcherService {

    Map<Archive, SubmissionEnvelope> assessDispatchReadiness(Submission submission);

    Map<Archive, SubmissionEnvelope> determineSupportingInformationRequired(Submission submission);

    void updateSubmittablesStatusToSubmitted(Archive archive, SubmissionEnvelope submissionEnvelope);

}
