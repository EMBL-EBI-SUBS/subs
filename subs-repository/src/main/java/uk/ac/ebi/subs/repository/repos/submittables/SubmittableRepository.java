package uk.ac.ebi.subs.repository.repos.submittables;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import uk.ac.ebi.subs.repository.model.StoredSubmittable;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

@NoRepositoryBean
@RepositoryRestResource
public interface SubmittableRepository<T extends StoredSubmittable> extends MongoRepository<T, String>, SubmittableRepositoryCustom<T> {

    // exported as GET /things/:id
    @Override
    @RestResource(exported = true)
    public T findOne(String id);

    // exported as GET /things
    @Override
    @RestResource(exported = false)
    public Page<T> findAll(Pageable pageable);

    // controls PUT /things and PATCH /things/:id
    @Override
    @RestResource(exported = true)
    public <S extends T> S save(S s);

    // controls POST /things
    @Override
    @RestResource(exported = true)
    public <S extends T> S insert(S s);

    // exported as DELETE /things/:id
    @Override
    @RestResource(exported = true)
    public void delete(T t);


    @RestResource(exported = false)
    List<T> findBySubmissionId(String submissionId);

    @RestResource(exported = true, path = "by-submission", rel = "by-submission")
    Page<T> findBySubmissionId(@Param("submissionId") String submissionId, Pageable pageable);


    @RestResource(exported = true, path = "by-team", rel = "by-team")
    @Query("'team.name': ?0")
        //THIS IS A DUMMY QUERY, real implementation comes from Implementation of SubmittableRepositoryCustom
    Page<T> submittablesInTeam(@Param("teamName") String teamName, Pageable pageable);

    @RestResource(exported = true, path = "current-version", rel = "current-version")
    T findFirstByTeamNameAndAliasOrderByCreatedDateDesc(@Param("teamName") String teamName, @Param("alias") String alias);

    @RestResource(exported = true, path = "history", rel = "history")
    Page<T> findByTeamNameAndAliasOrderByCreatedDateDesc(
            @Param("teamName") String teamName, @Param("alias") String alias,
            Pageable pageable);

    @RestResource(exported = false)
    Stream<T> streamByTeamNameAndAliasOrderByCreatedDateDesc(
            @Param("teamName") String teamName, @Param("alias") String alias);

    @RestResource(exported = false)
    Stream<T> streamBySubmissionId(
            @Param("submissionId") String submissionId);

    @RestResource(exported = true, path="by-accession", rel = "by-accession")
    T findFirstByAccessionOrderByCreatedDateDesc(@Param("accession") String accession);

    @RestResource(exported = false)
    List<T> findBySubmissionIdAndAlias(String submissionId, String alias);


    @RestResource(exported = false)
    void deleteBySubmissionId(String submissionId);

}
