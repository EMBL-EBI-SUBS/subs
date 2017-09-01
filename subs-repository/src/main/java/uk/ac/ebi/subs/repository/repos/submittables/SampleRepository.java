package uk.ac.ebi.subs.repository.repos.submittables;

import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.security.access.method.P;

import uk.ac.ebi.subs.repository.model.Sample;
import uk.ac.ebi.subs.repository.projections.SubmittableWithStatus;
import uk.ac.ebi.subs.repository.security.PreAuthorizeSubmissionTeamName;
import uk.ac.ebi.subs.repository.security.PreAuthorizeSubmittableTeamName;

@RepositoryRestResource(excerptProjection = SubmittableWithStatus.class)
public interface SampleRepository extends SubmittableRepository<Sample> {

    @RestResource(exported = false)
    Sample findByAccession(String accession);

}
