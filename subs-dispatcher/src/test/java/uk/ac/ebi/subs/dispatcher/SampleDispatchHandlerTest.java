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
import uk.ac.ebi.subs.data.status.ProcessingStatusEnum;
import uk.ac.ebi.subs.data.submittable.BaseSubmittable;
import uk.ac.ebi.subs.processing.SubmissionEnvelope;
import uk.ac.ebi.subs.repository.model.*;
import uk.ac.ebi.subs.repository.repos.status.ProcessingStatusRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * Created by davidr on 07/07/2017.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = DispatcherApplication.class)
public class SampleDispatchHandlerTest {


    @Autowired
    private DispatchTestSubmissionSetup dispatchTestSubmissionSetup;

    @Autowired
    private ProcessingStatusRepository processingStatusRepository;

    @Autowired
    private DispatcherService dispatcherService;


    private Submission submission;
    private static final int SAMPLE_AND_ASSAY_COUNT = 10;


    @Before
    public void buildUp(){
        dispatchTestSubmissionSetup.clearRepos();

        dispatchTestSubmissionSetup.createSubmission();

        submission = dispatchTestSubmissionSetup.createSubmission();
        Study study = dispatchTestSubmissionSetup.createStudy("test-study",submission);

        List<Sample> samples = IntStream
                .rangeClosed(1,SAMPLE_AND_ASSAY_COUNT)
                .mapToObj(i -> Integer.valueOf(i))
                .map(i -> dispatchTestSubmissionSetup.createSample(i.toString(),submission))
                .collect(Collectors.toList());

        List<Assay> assays = IntStream
                .rangeClosed(1,SAMPLE_AND_ASSAY_COUNT)
                .mapToObj(i -> Integer.valueOf(i))
                .map(i -> dispatchTestSubmissionSetup.createAssay(i.toString(),submission,samples.get(i - 1),study))
                .collect(Collectors.toList());

        ProcessingStatusEnum statusEnum  = ProcessingStatusEnum.Submitted;

        Stream.concat(
                samples.stream(),
                assays.stream()
        ).forEach(
                s -> changeSubmittableStatus(statusEnum, s)
        );

    }

    private void changeSubmittableStatus(ProcessingStatusEnum statusEnum, StoredSubmittable s) {
        ProcessingStatus ps = s.getProcessingStatus();
        ps.setStatus(statusEnum);
        processingStatusRepository.save(ps);
    }

    @After
    public void tearDown(){
        dispatchTestSubmissionSetup.clearRepos();
    }

    @Test
    public void testSampleReadiness(){
        StopWatch stopWatch = new StopWatch();

        stopWatch.start("sample dispatch test");

        Map<Archive,SubmissionEnvelope> dispatcherOutput = dispatcherService.assessDispatchReadiness(submission);

        stopWatch.stop();
        System.out.println(stopWatch.prettyPrint());


        assertThat(dispatcherOutput.keySet(),hasSize(1));
        assertThat(dispatcherOutput,hasKey(Archive.BioSamples));


        SubmissionEnvelope submissionEnvelope = dispatcherOutput.get(Archive.BioSamples);

        assertThat(
            submissionEnvelope.getSamples(),hasSize(SAMPLE_AND_ASSAY_COUNT)
        );


    }
}
