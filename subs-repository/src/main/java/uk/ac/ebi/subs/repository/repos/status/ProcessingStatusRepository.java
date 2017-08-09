package uk.ac.ebi.subs.repository.repos.status;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.security.access.method.P;
import uk.ac.ebi.subs.repository.model.ProcessingStatus;
import uk.ac.ebi.subs.repository.projections.ProcessingStatusWithAlias;
import uk.ac.ebi.subs.repository.security.PostAuthorizeProcessingStatusTeamName;
import uk.ac.ebi.subs.repository.security.PreAuthorizeProcessingStatusTeamName;
import uk.ac.ebi.subs.repository.security.PreAuthorizeSubmissionIdTeamName;

import java.util.List;


@RepositoryRestResource(excerptProjection = ProcessingStatusWithAlias.class)
public interface ProcessingStatusRepository extends MongoRepository<ProcessingStatus, String>, ProcessingStatusRepositoryCustom {

    // exported as GET /things/:id
    @Override
    @RestResource(exported = true)
    @PostAuthorizeProcessingStatusTeamName
    public ProcessingStatus findOne(String id);

    // exported as GET /things
    @Override
    @RestResource(exported = false)
    Page<ProcessingStatus> findAll(Pageable pageable);

    // Prevents POST /things and PATCH /things/:id
    @Override
    @RestResource(exported = true)
    @PreAuthorizeProcessingStatusTeamName
    <S extends ProcessingStatus> S save(@P("processingStatus") S processingStatus);

    // exported as DELETE /things/:id
    @Override
    @RestResource(exported = false)
    void delete(ProcessingStatus t);

    @RestResource(exported = false)
    List<ProcessingStatus> findBySubmissionId(String submissionId);

    @RestResource(exported = true)
    @PostAuthorizeProcessingStatusTeamName
    ProcessingStatus findBySubmittableId(@Param("submittableId") String submittableId);




    @RestResource(exported = false)
    void deleteBySubmissionId(String submissionId);

    @PreAuthorizeSubmissionIdTeamName
    @RestResource(exported = true, rel = "by-submission")
    Page<ProcessingStatus> findBySubmissionId(@P("submissionId") @Param("submissionId") String submissionId, Pageable pageable);

    @RestResource(exported = true, rel = "by-submission-and-type")
    Page<ProcessingStatus> findBySubmissionIdAndSubmittableType(@Param("submissionId") String submissionId, @Param("type") String type, Pageable pageable);
}
