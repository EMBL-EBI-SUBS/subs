package uk.ac.ebi.subs.repository.repos;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import uk.ac.ebi.subs.data.component.Team;
import org.springframework.security.access.method.P;
import org.springframework.security.access.prepost.PreAuthorize;
import uk.ac.ebi.subs.repository.model.Submission;
import uk.ac.ebi.subs.repository.projections.SubmissionWithStatus;
import uk.ac.ebi.subs.repository.security.PostAuthorizeReturnObjectHasTeamName;
import uk.ac.ebi.subs.repository.security.PreAuthorizeParamTeamName;
import uk.ac.ebi.subs.repository.security.PreAuthorizeSubmissionTeamName;

@RepositoryRestResource(excerptProjection = SubmissionWithStatus.class)
public interface SubmissionRepository extends MongoRepository<Submission, String> {

    // exported as GET /things/:id
    @Override
    @RestResource(exported = true)
    @PostAuthorizeReturnObjectHasTeamName
    public Submission findOne(String id);

    // exported as GET /things
    @Override
    @RestResource(exported = false) //could export this and expose rels to admin users
    public Page<Submission> findAll(Pageable pageable);

    // Prevents PUT /things and PATCH /things/:id
    @Override
    @RestResource(exported = true)
    @PreAuthorizeSubmissionTeamName
    public <S extends Submission> S save(@P("entity") S entity);

    // Prevents POST /things and PATCH /things/:id
    @Override
    @RestResource(exported = true)
    @PreAuthorizeSubmissionTeamName
    public <S extends Submission> S insert(@P("entity") S entity);

    // exported as DELETE /things/:id
    @Override
    @RestResource(exported = true)
    @PreAuthorizeSubmissionTeamName
    public void delete(@P("entity") Submission entity);

    @RestResource(exported = true, rel = "by-team", path = "by-team")
    @PreAuthorizeParamTeamName
    Page<Submission> findByTeamName(@Param(value = "teamName") String teamName, Pageable pageable);

    @RestResource(exported = false)
    Submission findBySubmissionStatusId(String submissionStatusId);


}
