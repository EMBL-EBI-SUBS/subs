package uk.ac.ebi.subs.dispatcher;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import uk.ac.ebi.subs.data.component.*;
import uk.ac.ebi.subs.repository.model.Assay;
import uk.ac.ebi.subs.repository.model.Sample;
import uk.ac.ebi.subs.repository.model.Study;
import uk.ac.ebi.subs.repository.model.Submission;
import uk.ac.ebi.subs.repository.repos.SubmissionRepository;
import uk.ac.ebi.subs.repository.repos.status.ProcessingStatusRepository;
import uk.ac.ebi.subs.repository.repos.submittables.AssayRepository;
import uk.ac.ebi.subs.repository.repos.submittables.SampleRepository;
import uk.ac.ebi.subs.repository.repos.submittables.StudyRepository;
import uk.ac.ebi.subs.repository.services.SubmissionHelperService;
import uk.ac.ebi.subs.repository.services.SubmittableHelperService;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Stream;

/**
 * Created by davidr on 07/07/2017.
 */
@Component
public class DispatchTestSubmissionSetup {

    private SubmissionRepository submissionRepository;
    private SubmissionHelperService submissionHelperService;
    private SubmittableHelperService submittableHelperService;
    private SampleRepository sampleRepository;
    private StudyRepository studyRepository;
    private AssayRepository assayRepository;
    private ProcessingStatusRepository processingStatusRepository;

    private Team team = Team.build("tester1");
    private Submitter submitter =  Submitter.build("alice@test.ac.uk");

    public DispatchTestSubmissionSetup(SubmissionRepository submissionRepository, SubmissionHelperService submissionHelperService, SubmittableHelperService submittableHelperService, SampleRepository sampleRepository, StudyRepository studyRepository, AssayRepository assayRepository, ProcessingStatusRepository processingStatusRepository) {
        this.submissionRepository = submissionRepository;
        this.submissionHelperService = submissionHelperService;
        this.submittableHelperService = submittableHelperService;
        this.sampleRepository = sampleRepository;
        this.studyRepository = studyRepository;
        this.assayRepository = assayRepository;
        this.processingStatusRepository = processingStatusRepository;
    }

    public void clearRepos(){
        Stream.of(
               sampleRepository,studyRepository,assayRepository,submissionRepository, processingStatusRepository
        ).forEach(
                repo -> repo.deleteAll()
        );
    }

    public Submission createSubmission(){
        return submissionHelperService.createSubmission(team,submitter);
    }

    public Sample createSample(String alias, Submission submission){
        Sample s = new Sample();
        s.setAlias(alias);
        s.setSubmission(submission);
        submittableHelperService.setupNewSubmittable(s);
        sampleRepository.insert(s);
        return s;
    }

    public Study createStudy(String alias, Submission submission){
        Study s = new Study();
        s.setAlias(alias);
        s.setSubmission(submission);
        submittableHelperService.setupNewSubmittable(s);
        studyRepository.insert(s);
        return s;
    }

    public Assay createAssay(String alias, Submission submission, Sample sample, Study study){
        Assay a = new Assay();
        a.setAlias(alias);
        a.setSubmission(submission);
        submittableHelperService.setupNewSubmittable(a);

        a.setStudyRef((StudyRef) study.asRef());
        a.getSampleUses().add(new SampleUse((SampleRef) sample.asRef()));

        assayRepository.insert(a);
        return a;
    }

}
