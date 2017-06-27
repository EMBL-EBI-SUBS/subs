package uk.ac.ebi.subs.dispatcher;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StopWatch;
import uk.ac.ebi.subs.DispatcherApplication;
import uk.ac.ebi.subs.data.component.Archive;
import uk.ac.ebi.subs.data.component.Team;
import uk.ac.ebi.subs.data.status.ProcessingStatusEnum;
import uk.ac.ebi.subs.processing.SubmissionEnvelope;
import uk.ac.ebi.subs.repository.model.ProcessingStatus;
import uk.ac.ebi.subs.repository.model.Sample;
import uk.ac.ebi.subs.repository.model.Submission;
import uk.ac.ebi.subs.repository.repos.SubmissionRepository;
import uk.ac.ebi.subs.repository.repos.status.ProcessingStatusRepository;
import uk.ac.ebi.subs.repository.repos.submittables.SampleRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by davidr on 27/06/2017.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = DispatcherApplication.class)
public class UpdateSubmittablesStatusToSubmittedTest {

    @Autowired
    ProcessingStatusRepository processingStatusRepository;
    @Autowired
    SampleRepository sampleRepository;
    @Autowired
    SubmissionRepository submissionRepository;

    @Autowired
    DispatcherService dispatcherService;

    Submission submission;
    List<Sample> samples = new ArrayList<>();
    SubmissionEnvelope submissionEnvelope;

    @Test
    public void updateStatus(){
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("status change test");
        dispatcherService.updateSubmittablesStatusToSubmitted(Archive.BioSamples,submissionEnvelope);
        stopWatch.stop();
        System.out.println(stopWatch.prettyPrint());

        for (Sample sample : samples){
            ProcessingStatus processingStatus = processingStatusRepository.findOne(sample.getProcessingStatus().getId());

            ProcessingStatusEnum expectedStatus = ProcessingStatusEnum.Draft;

            if (sample.getTitle() != null && sample.getTitle().equals("hold")){
                    expectedStatus = ProcessingStatusEnum.Rejected;
                }
                else if (sample.getArchive().equals(Archive.BioSamples)){
                    expectedStatus = ProcessingStatusEnum.Dispatched;
                }
                else {
                    expectedStatus = ProcessingStatusEnum.Draft;

            }

            assertThat(processingStatus.getStatus(),equalTo(expectedStatus.name()));


        }


    }


    @Before
    public void buildUp(){
        tearDown();

        Team team = Team.build("test");

        submission = new Submission();
        submission.setId(uuid());
        submission.setTeam(team);
        submissionRepository.insert(submission);

        for (int i = 0; i < 1000; i++){
            Sample s = new Sample();
            s.setAlias("testSample"+i);
            s.setId(uuid());
            s.setTeam(team);
            s.setSubmission(submission);

            //set half to one archive, half to the other
            Archive archive  = (i % 2 == 0) ? Archive.BioSamples : Archive.Pride;

            s.setArchive(archive);

            ProcessingStatus processingStatus = ProcessingStatus.createForSubmittable(s);;
            processingStatus.setId(uuid());

            if (i % 3 == 0) {
                s.setTitle("hold");
                processingStatus.setStatus(ProcessingStatusEnum.Rejected);
            }

            processingStatusRepository.insert(processingStatus);
            sampleRepository.insert(s);

            samples.add(s);
        }

        submissionEnvelope = new SubmissionEnvelope();
        submissionEnvelope.setSubmission(submission);
        submissionEnvelope.getSamples().addAll(samples);
    }

    private String uuid(){
        return UUID.randomUUID().toString();
    }


    @After
    public void tearDown(){
        Stream.of(processingStatusRepository,sampleRepository,sampleRepository).forEach(repo -> repo.deleteAll());
    }

}
