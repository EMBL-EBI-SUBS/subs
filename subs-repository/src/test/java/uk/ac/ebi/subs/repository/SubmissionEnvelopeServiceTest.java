package uk.ac.ebi.subs.repository;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.subs.data.component.SampleRef;
import uk.ac.ebi.subs.data.component.Team;
import uk.ac.ebi.subs.processing.SubmissionEnvelope;
import uk.ac.ebi.subs.repository.model.Sample;
import uk.ac.ebi.subs.repository.model.Submission;

import uk.ac.ebi.subs.repository.repos.submittables.SampleRepository;

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = TestRepoApplication.class)
public class SubmissionEnvelopeServiceTest {

    @Autowired
    SubmissionEnvelopeService submissionEnvelopeService;

    @Autowired
    SampleRepository sampleRepository;

    @Test
    public void testProcessSampleReferences_ByAccession() {
        // save Samples to the db, create a SubmissionEnvelope with a SampleRef to
        // the Samples and confirm that the Samples gets added to the SubmissionEnvelope
        SubmissionEnvelope testEnvelope = new SubmissionEnvelope();

        Submission sampleSubmission = generateSubmission();

        String testSampleAccession1 = "testAccession1";
        String testSampleAccession2 = "testAccession2";

        Sample testSample1 = new Sample();
        testSample1.setSubmission(sampleSubmission);
        testSample1.setAccession(testSampleAccession1);

        Sample testSample2 = new Sample();
        testSample2.setSubmission(sampleSubmission);
        testSample2.setAccession(testSampleAccession2);

        sampleRepository.insert(testSample1);
        sampleRepository.insert(testSample2);

        SampleRef sampleRef1 = new SampleRef();
        SampleRef sampleRef2 = new SampleRef();

        sampleRef1.setAccession(testSampleAccession1);
        sampleRef2.setAccession(testSampleAccession2);

        testEnvelope.getSupportingSamplesRequired().add(sampleRef1);
        testEnvelope.getSupportingSamplesRequired().add(sampleRef2);

        SubmissionEnvelope testEnvelopeProcessedReferences = submissionEnvelopeService.processSampleReferences(testEnvelope);

        assertThat(testEnvelopeProcessedReferences.getSamples(), hasSize(2)); // the 2 SampleRefs were processed and added to the envelope
        assertThat(testEnvelopeProcessedReferences.getSupportingSamplesRequired(), hasSize(0)); // the processed SampleRefs were plucked from the set of supporingSamplesRequired

        sampleRepository.delete(testSample1);
        sampleRepository.delete(testSample2);
    }

    @Test
    public void testProcessSampleReferences_ByTeamAliasPair() {
        SubmissionEnvelope testEnvelope = new SubmissionEnvelope();
        Submission sampleSubmission = generateSubmission();

        String testTeamName1 = "testTeamName1";
        String testTeamName2 = "testTeamName2";

        String testAliasName1 = "testAlias1";
        String testAliasName2 = "testAlias2";

        // create test Samples identified by team-alias pairs, along with corresponding SampleRefs
        Sample testSample1 = new Sample();
        testSample1.setTeam(Team.build(testTeamName1));
        testSample1.setAlias(testAliasName1);
        testSample1.setSubmission(sampleSubmission);

        Sample testSample2 = new Sample();
        testSample2.setTeam(Team.build(testTeamName2));
        testSample2.setAlias(testAliasName2);
        testSample2.setSubmission(sampleSubmission);

        sampleRepository.insert(testSample1);
        sampleRepository.insert(testSample2);

        SampleRef testSampleRef1 = new SampleRef();
        testSampleRef1.setTeam(testTeamName1);
        testSampleRef1.setAlias(testAliasName1);

        SampleRef testSampleRef2 = new SampleRef();
        testSampleRef2.setTeam(testTeamName2);
        testSampleRef2.setAlias(testAliasName2);

        testEnvelope.getSupportingSamplesRequired().add(testSampleRef1);
        testEnvelope.getSupportingSamplesRequired().add(testSampleRef2);

        SubmissionEnvelope testEnvelopeProcessedReferences = submissionEnvelopeService.processSampleReferences(testEnvelope);

        assertThat(testEnvelopeProcessedReferences.getSupportingSamplesRequired(), hasSize(0));
        assertThat(testEnvelopeProcessedReferences.getSamples(), hasSize(2));

        sampleRepository.delete(testSample1);
        sampleRepository.delete(testSample2);
    }

    @Test
    public void testProcessSampleReferences_PreservesReferencesNotInUSI() {
        SubmissionEnvelope testEnvelope = new SubmissionEnvelope();
        Submission sampleSubmission = generateSubmission();

        String testTeamName1 = "testTeamName1";
        String testTeamName2 = "testTeamName2";

        String testAliasName1 = "testAlias1";
        String testAliasName2 = "testAlias2";

        Sample testSample1 = new Sample();
        testSample1.setTeam(Team.build(testTeamName1));
        testSample1.setAlias(testAliasName1);
        testSample1.setSubmission(sampleSubmission);

        sampleRepository.insert(testSample1);

        SampleRef testSampleRef1 = new SampleRef();
        testSampleRef1.setTeam(testTeamName1);
        testSampleRef1.setAlias(testAliasName1);

        // reference to a Sample that's not in the USI db
        SampleRef testSampleRef2 = new SampleRef();
        testSampleRef2.setTeam(testTeamName2);
        testSampleRef2.setAlias(testAliasName2);

        testEnvelope.getSupportingSamplesRequired().add(testSampleRef1);
        testEnvelope.getSupportingSamplesRequired().add(testSampleRef2);

        SubmissionEnvelope testEnvelopeProcessedReferences = submissionEnvelopeService.processSampleReferences(testEnvelope);

        assertThat(testEnvelopeProcessedReferences.getSupportingSamplesRequired(), hasSize(1));
        assertThat(testEnvelopeProcessedReferences.getSamples(), hasSize(1));

        sampleRepository.delete(testSample1);
    }

    private Submission generateSubmission(){
        String randomString =  UUID.randomUUID().toString().substring(5,10);

        Submission sub = new Submission();

        sub.getSubmitter().setEmail("test" + randomString + "@example.ac.uk");
        sub.getTeam().setName("testTeam" + randomString);
        sub.setId(UUID.randomUUID().toString());

        return sub;
    }
}
