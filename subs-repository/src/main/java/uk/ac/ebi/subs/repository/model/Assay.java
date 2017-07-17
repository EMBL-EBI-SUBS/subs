package uk.ac.ebi.subs.repository.model;

import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import uk.ac.ebi.subs.data.component.AbstractSubsRef;

import java.util.Date;
import java.util.stream.Stream;

@CompoundIndexes({
        @CompoundIndex(background = true, name = "team_alias", def = "{ 'team.name': 1, 'alias': 1 }"),
        @CompoundIndex(background = true, name = "accession", def = "{ 'accession': 1}"),
        @CompoundIndex(background = true, name = "submission", def = "{ 'submission': 1}")
})
@Document
public class Assay extends uk.ac.ebi.subs.data.submittable.Assay implements StoredSubmittable {

    @Override
    public Stream<AbstractSubsRef> refs() {
        return Stream.concat(
            Stream.of( (AbstractSubsRef)this.getStudyRef()),
            this.getSampleUses().stream().map(su -> (AbstractSubsRef)su.getSampleRef())
        );
    }

    @DBRef
    private ProcessingStatus processingStatus;
    @Version
    private Long version;
    @CreatedDate
    private Date createdDate;
    @LastModifiedDate
    private Date lastModifiedDate;
    @CreatedBy
    private String createdBy;
    @LastModifiedBy
    private String lastModifiedBy;
    @DBRef
    private Submission submission;

    @Override
    public ProcessingStatus getProcessingStatus() {
        return processingStatus;
    }

    @Override
    public void setProcessingStatus(ProcessingStatus processingStatus) {
        this.processingStatus = processingStatus;
    }

    public Submission getSubmission() {
        return submission;
    }

    public void setSubmission(Submission submission) {
        this.submission = submission;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Date getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(Date lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }
}
