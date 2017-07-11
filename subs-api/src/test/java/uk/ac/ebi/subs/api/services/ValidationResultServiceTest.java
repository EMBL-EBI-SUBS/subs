package uk.ac.ebi.subs.api.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.subs.validator.data.SingleValidationResult;
import uk.ac.ebi.subs.validator.data.ValidationAuthor;
import uk.ac.ebi.subs.validator.data.ValidationResult;
import uk.ac.ebi.subs.validator.repository.ValidationResultRepository;

import java.util.*;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by karoly on 11/07/2017.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@EnableMongoRepositories(basePackageClasses = ValidationResultRepository.class)
@EnableAutoConfiguration
@SpringBootTest(classes = ValidationResultServiceImpl.class)
public class ValidationResultServiceTest {

    @Autowired
    private ValidationResultRepository validationResultRepository;

    private ValidationResultService validationResultService;

    private static String VALID_SUBMISSION_ID = "1234";
    private static int SIZE_OF_ENTITIES_BY_VALID_SUBMISSION_ID = 5;
    private static String[] ENTITY_UUIDS = {"11", "22", "33", "44", "55", "66", "77", "88"};

    private PageRequest pageRequest = new PageRequest(0, 10);


    @Before
    public void setup() {
        validationResultRepository = mock(ValidationResultRepository.class);

        validationResultService = new ValidationResultServiceImpl(validationResultRepository);

        when(validationResultRepository.findBySubmissionId(eq(VALID_SUBMISSION_ID), any()))
                .thenReturn(generatePagedValidationResults(SIZE_OF_ENTITIES_BY_VALID_SUBMISSION_ID, VALID_SUBMISSION_ID)
        );
    }

    @Test
    public void whenSubmissionIdIsValidThanGotBackAListOfValidationResults() {
        assertThat(validationResultService.getValidationResultBySubmissionId(VALID_SUBMISSION_ID).size(),
                is(equalTo(SIZE_OF_ENTITIES_BY_VALID_SUBMISSION_ID)));
    }

    private Page<ValidationResult> generatePagedValidationResults(int numberOfValidationResults, String submissionId) {
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

            results.add(validationResult);
        }

        return new PageBuilder<ValidationResult>()
                .elements(results)
                .pageRequest(pageRequest)
                .totalElements(results.size())
                .build();
    }
}
