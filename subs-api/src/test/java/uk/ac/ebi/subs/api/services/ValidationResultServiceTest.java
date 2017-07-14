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
import uk.ac.ebi.subs.validator.data.SingleValidationResult;
import uk.ac.ebi.subs.validator.data.ValidationAuthor;
import uk.ac.ebi.subs.validator.data.ValidationResult;
import uk.ac.ebi.subs.validator.data.ValidationStatus;
import uk.ac.ebi.subs.validator.repository.ValidationResultRepository;

import java.util.*;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Created by karoly on 11/07/2017.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@EnableMongoRepositories(basePackageClasses = { ValidationResultRepository.class, SubmissionRepository.class })
@EnableAutoConfiguration
@SpringBootTest(classes = ValidationResultService.class)
public class ValidationResultServiceTest {

    @Autowired
    private ValidationResultRepository validationResultRepository;

    @Autowired
    private SubmissionRepository submissionRepository;

    private ValidationResultService validationResultService;

    private static String SUCCESSFUL_SUBMISSION_ID = "1111";
    private static String SUBMISSION_WITH_UNFINISHED_VALIDATION_ID = "2222";
    private static String SUBMISSION_WITH_NOT_PASSED_VALIDATION_ID = "3333";
    private static String SUCCESSFUL_SUBMISSION_STATUS_ID = "5678";
    private static String NOT_PASSED_SUBMISSION_STATUS_ID = "8765";
    private static int SIZE_OF_ENTITIES_BY_VALID_SUBMISSION_ID = 5;
    private static String[] ENTITY_UUIDS = {"11", "22", "33", "44", "55", "66", "77", "88"};

    @Before
    public void setup() {
        validationResultRepository = spy(ValidationResultRepository.class);
        submissionRepository = mock(SubmissionRepository.class);

        validationResultService = new ValidationResultServiceImpl(validationResultRepository, submissionRepository);

        when(validationResultRepository.findAllBySubmissionId(eq(SUCCESSFUL_SUBMISSION_ID)))
                .thenReturn(generateValidationResults(SIZE_OF_ENTITIES_BY_VALID_SUBMISSION_ID, SUCCESSFUL_SUBMISSION_ID)
        );
    }

    @Test
    public void whenSubmissionIdIsValidThanGotBackAListOfValidationResults() {
        assertThat(validationResultService.getValidationResultBySubmissionId(SUCCESSFUL_SUBMISSION_ID).size(),
                is(equalTo(SIZE_OF_ENTITIES_BY_VALID_SUBMISSION_ID)));
    }

    @Test
    public void whenValidationNotFinishedShouldReturnFalse() {
        Submission submission_with_not_finished_validation = createSubmission(SUBMISSION_WITH_UNFINISHED_VALIDATION_ID, SUCCESSFUL_SUBMISSION_STATUS_ID);

        when(submissionRepository.findBySubmissionStatusId(SUCCESSFUL_SUBMISSION_STATUS_ID))
                .thenReturn(submission_with_not_finished_validation);

        assertThat(validationResultService.isValidationFinishedAndPassed(submission_with_not_finished_validation.getSubmissionStatus().getId()),
                    is(equalTo(false)));
    }

    @Test
    public void whenValidationFinishedButNotAllPassedShouldReturnFalse() {
        Submission submission_with_not_passed_validation = createSubmission(SUBMISSION_WITH_NOT_PASSED_VALIDATION_ID, NOT_PASSED_SUBMISSION_STATUS_ID);

        List<ValidationResult> results = generateValidationResults(1, SUBMISSION_WITH_NOT_PASSED_VALIDATION_ID);
        addSingleValidationResult(results.get(0), ValidationAuthor.Taxonomy, ValidationStatus.Pass);
        addSingleValidationResult(results.get(0), ValidationAuthor.Biosamples, ValidationStatus.Error);
        results.get(0).setValidationStatus(ValidationStatus.Complete);

        when(submissionRepository.findBySubmissionStatusId(NOT_PASSED_SUBMISSION_STATUS_ID))
                .thenReturn(submission_with_not_passed_validation);
        when(validationResultRepository.findAllBySubmissionId(eq(SUBMISSION_WITH_NOT_PASSED_VALIDATION_ID)))
                .thenReturn(results);

        assertThat(validationResultService.isValidationFinishedAndPassed(submission_with_not_passed_validation.getSubmissionStatus().getId()),
                is(equalTo(false)));
    }

    @Test
    public void whenValidationFinishedAndAllPassedShouldReturnTrue() {
        Submission submission_with_successful_validation = createSubmission(SUCCESSFUL_SUBMISSION_ID, SUCCESSFUL_SUBMISSION_STATUS_ID);

        List<ValidationResult> results = generateValidationResults(1, SUCCESSFUL_SUBMISSION_ID);
        addSingleValidationResult(results.get(0), ValidationAuthor.Taxonomy, ValidationStatus.Pass);
        addSingleValidationResult(results.get(0), ValidationAuthor.Biosamples, ValidationStatus.Pass);
        results.get(0).setValidationStatus(ValidationStatus.Complete);

        when(submissionRepository.findBySubmissionStatusId(SUCCESSFUL_SUBMISSION_STATUS_ID))
                .thenReturn(submission_with_successful_validation);
        when(validationResultRepository.findAllBySubmissionId(eq(SUCCESSFUL_SUBMISSION_ID)))
                .thenReturn(results);

        assertThat(validationResultService.isValidationFinishedAndPassed(submission_with_successful_validation.getSubmissionStatus().getId()),
                is(equalTo(true)));

    }

    private List<ValidationResult> generateValidationResults(int numberOfValidationResults, String submissionId) {
        Map<ValidationAuthor, List<SingleValidationResult>> validationAuthorListMap = new HashMap<>();
        validationAuthorListMap.put(ValidationAuthor.Taxonomy, new ArrayList<>());
        validationAuthorListMap.put(ValidationAuthor.Biosamples, new ArrayList<>());

        List<ValidationResult> results = new ArrayList<>();
        for (int i = 0; i < numberOfValidationResults; i++) {
            ValidationResult validationResult = new ValidationResult();
            validationResult.setUuid(UUID.randomUUID().toString());
            validationResult.setExpectedResults(validationAuthorListMap);
            validationResult.setVersion(1);
            validationResult.setSubmissionId(submissionId);
            validationResult.setEntityUuid(ENTITY_UUIDS[i]);
            validationResult.setValidationStatus(ValidationStatus.Pending);

            results.add(validationResult);
        }

        return results;
    }

    private void addSingleValidationResult(ValidationResult validationResult, ValidationAuthor validationAuthor,
                                           ValidationStatus validationStatus) {
        SingleValidationResult singleValidationResult = new SingleValidationResult(validationAuthor, validationResult.getEntityUuid());
        singleValidationResult.setValidationStatus(validationStatus);

        validationResult.getExpectedResults().put(validationAuthor, Collections.singletonList(singleValidationResult));
    }

    private Submission createSubmission(String submissionId, String submissionStatusId) {
        Submission submission = new Submission();
        submission.setId(submissionId);
        submission.setSubmitter(Submitter.build("submitter@email.com"));
        submission.setVersion(1L);
        submission.setTeam(Team.build("team name"));

        SubmissionStatus submissionStatus = new SubmissionStatus();
        submissionStatus.setStatus(SubmissionStatusEnum.Draft);
        submissionStatus.setId(submissionStatusId);

        submission.setSubmissionStatus(submissionStatus);

        return submission;
    }
}
