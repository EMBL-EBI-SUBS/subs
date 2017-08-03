package uk.ac.ebi.subs.repository;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.subs.TestRepoApplication;
import uk.ac.ebi.subs.data.component.Team;
import uk.ac.ebi.subs.data.status.SubmissionStatusEnum;
import uk.ac.ebi.subs.repository.model.ProcessingStatus;
import uk.ac.ebi.subs.repository.model.Sample;
import uk.ac.ebi.subs.repository.model.Submission;
import uk.ac.ebi.subs.repository.model.SubmissionStatus;
import uk.ac.ebi.subs.repository.repos.SubmissionRepository;
import uk.ac.ebi.subs.repository.repos.status.SubmissionStatusRepository;
import uk.ac.ebi.subs.repository.repos.submittables.SampleRepository;
import uk.ac.ebi.subs.repository.repos.submittables.SubmittableRepository;
import uk.ac.ebi.subs.repository.security.TeamNameExtractor;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = TestRepoApplication.class)
public class TeamNameExtractorTest {

    private TeamNameExtractor teamNameExtractor;

    @Autowired
    private List<SubmittableRepository<?>> submissionContentsRepositories;
    @Autowired
    private SampleRepository sampleRepository;
    @Autowired
    private SubmissionRepository submissionRepository;
    @Autowired
    private SubmissionStatusRepository submissionStatusRepository;


    @Test
    public void findCorrectTeamNameFromProcessingStatus() {
        String teamName = "testTeam";

        Team team = createTeam(teamName);

        Sample sample = createSample(team);

        sampleRepository.insert(sample);
        ProcessingStatus status = ProcessingStatus.createForSubmittable(sample);

        String teamNameFromStatus = teamNameExtractor.processingStatusTeam(status);

        assertThat(teamNameFromStatus, is(equalTo(teamName)));
    }

    @Test
    public void findCorrectTeamNameFromSubmissionStatus() {
        String teamName = "testTeam";

        Team team = createTeam(teamName);
        Submission submission = createSubmission(team);

        submissionStatusRepository.insert(submission.getSubmissionStatus());
        submissionRepository.insert(submission);


        String statusTeamName = teamNameExtractor.submissionStatusTeam(submission.getSubmissionStatus());

        assertThat(statusTeamName, is(equalTo(teamName)));
    }

    @Test
    public void findCorrectTeamNameFromSubmissionId() {
        String teamName = "testTeam";

        Team team = createTeam(teamName);
        Submission submission = createSubmission(team);

        submissionStatusRepository.insert(submission.getSubmissionStatus());
        submissionRepository.insert(submission);


        String submissionIdTeam = teamNameExtractor.submissionIdTeam(submission.getId());

        assertThat(submissionIdTeam, is(equalTo(teamName)));
    }



    private Team createTeam(String teamName) {
        Team team = new Team();
        team.setName(teamName);
        return team;
    }


    public Sample createSample(Team team) {
        Sample sample = new Sample();

        Submission submission = createSubmission(team);

        sample.setSubmission(submission);
        sample.setId(UUID.randomUUID().toString());
        sample.setTeam(team);

        return sample;
    }

    public Submission createSubmission(Team team) {
        Submission submission = new Submission();
        submission.setId(UUID.randomUUID().toString());

        submission.setTeam(team);

        submission.setSubmissionStatus(new SubmissionStatus(SubmissionStatusEnum.Draft));

        return submission;
    }

    @Before
    public void buildUp() {
        tearDown();
        teamNameExtractor = new TeamNameExtractor(submissionContentsRepositories,submissionRepository);
    }

    @After
    public void tearDown() {

        Stream.concat(
                Stream.of(submissionRepository,submissionStatusRepository),
                submissionContentsRepositories.stream()
        ).forEach(
                repo -> repo.deleteAll()
        );

    }


}
