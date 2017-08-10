package uk.ac.ebi.subs.repository.repos.status;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.security.access.method.P;
import uk.ac.ebi.subs.repository.model.SubmissionStatus;
import uk.ac.ebi.subs.repository.security.PostAuthorizeSubmissionStatusTeamName;
import uk.ac.ebi.subs.repository.security.PreAuthorizeSubmissionStatusTeamName;


@RepositoryRestResource
public interface SubmissionStatusRepository extends MongoRepository<SubmissionStatus, String> {

    // exported as GET /things/:id
    @Override
    @RestResource(exported = true)
    @PostAuthorizeSubmissionStatusTeamName
    public SubmissionStatus findOne(String id);

    // exported as GET /things
    @Override
    @RestResource(exported = false)
    Page<SubmissionStatus> findAll(Pageable pageable);

    // Prevents POST /things and PATCH /things/:id
    @Override
    @RestResource(exported = true)
    @PreAuthorizeSubmissionStatusTeamName
    <S extends SubmissionStatus> S save(@P("submissionStatus")S submissionStatus);

    // exported as DELETE /things/:id
    @Override
    @RestResource(exported = false)
    void delete(SubmissionStatus t);


}
