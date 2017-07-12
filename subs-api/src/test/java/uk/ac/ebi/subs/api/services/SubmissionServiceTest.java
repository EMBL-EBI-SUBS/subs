package uk.ac.ebi.subs.api.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.subs.data.component.Submitter;
import uk.ac.ebi.subs.data.component.Team;
import uk.ac.ebi.subs.data.status.SubmissionStatusEnum;
import uk.ac.ebi.subs.repository.model.Submission;
import uk.ac.ebi.subs.repository.model.SubmissionStatus;
import uk.ac.ebi.subs.repository.repos.SubmissionRepository;
import uk.ac.ebi.subs.repository.repos.status.SubmissionStatusRepository;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Created by karoly on 12/07/2017.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@EnableMongoRepositories(basePackageClasses = {SubmissionRepository.class, SubmissionStatusRepository.class})
@EnableAutoConfiguration
@SpringBootTest(classes = SubmissionService.class)
public class SubmissionServiceTest {

    @Autowired
    private SubmissionRepository submissionRepository;
    @Autowired
    private SubmissionStatusRepository submissionStatusRepository;

    private SubmissionService submissionService;

    private Submission submission;

    @Before
    public void setup() {
        generateSubmission();

        submissionService = new SubmissionServiceImpl(submissionRepository, submissionStatusRepository);
    }

    @Test
    public void whenSubmissionIdIsValidThanGotBackAListOfValidationResults() {
        submissionService.setSubmissionToSubmitted(submission);

        Submission savedSubmission = submissionRepository.findOne(submission.getId());
        SubmissionStatus savedSubmissionStatus =
                submissionStatusRepository.findOne(submission.getSubmissionStatus().getId());

        assertThat(savedSubmission.getSubmissionStatus().getStatus(), is(equalTo(SubmissionStatusEnum.Submitted.name())));
        assertThat(savedSubmissionStatus.getStatus(), is(equalTo(SubmissionStatusEnum.Submitted.name())));
    }

    private void generateSubmission() {
        submission = new Submission();

        submission.setSubmissionStatus(new SubmissionStatus());
        submission.getSubmissionStatus().setStatus(SubmissionStatusEnum.Submitted);

        submission.setVersion(1L);
        submission.setTeam(Team.build("test team"));
        submission.setSubmitter(Submitter.build("submitter@email.com"));

        submissionStatusRepository.save(submission.getSubmissionStatus());
        submissionRepository.save(submission);
    }
}
