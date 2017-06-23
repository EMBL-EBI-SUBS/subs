package uk.ac.ebi.subs.repository.repos;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import uk.ac.ebi.subs.data.component.Team;
import uk.ac.ebi.subs.repository.model.Submission;
import uk.ac.ebi.subs.repository.projections.SubmissionWithStatus;

@RepositoryRestResource(excerptProjection = SubmissionWithStatus.class)
public interface SubmissionRepository extends MongoRepository<Submission, String> {

    // exported as GET /things/:id
    @Override
    @RestResource(exported = true)
    public Submission findOne(String id);

    // exported as GET /things
    @Override
    @RestResource(exported = false)
    public Page<Submission> findAll(Pageable pageable);

    // Prevents POST /things and PATCH /things/:id
    @Override
    @RestResource(exported = true)
    public <S extends Submission> S save(S s);

    // exported as DELETE /things/:id
    @Override
    @RestResource(exported = true)
    public void delete(Submission t);

    @RestResource(exported = true,rel="by-team", path="by-team")
    Page<Submission> findByTeamName(@Param(value = "teamName") String teamName, Pageable pageable);

    @RestResource(exported = false)
    Submission findBySubmissionStatusId(String submissionStatusId);


}
