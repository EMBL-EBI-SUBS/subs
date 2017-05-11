package uk.ac.ebi.subs.repository;

import uk.ac.ebi.subs.processing.SubmissionEnvelope;

public interface SubmissionEnvelopeService {

    SubmissionEnvelope fetchOne(String submissionId);

    SubmissionEnvelope processSampleReferences(SubmissionEnvelope submissionEnvelope);
}
