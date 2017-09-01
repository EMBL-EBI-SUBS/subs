package uk.ac.ebi.subs.api;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import uk.ac.ebi.subs.ApiApplication;
import uk.ac.ebi.subs.api.validators.CoreSubmittableValidationHelper;


import uk.ac.ebi.subs.api.validators.SubsApiErrors;
import uk.ac.ebi.subs.data.component.Submitter;
import uk.ac.ebi.subs.data.component.Team;
import uk.ac.ebi.subs.repository.model.Sample;
import uk.ac.ebi.subs.repository.model.Submission;
import uk.ac.ebi.subs.repository.repos.SubmissionRepository;
import uk.ac.ebi.subs.repository.repos.submittables.SampleRepository;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ApiApplication.class)
@WithMockUser(username="usi_user",roles={CoreValidatorTest.TEST_TEAM_1})
public class CoreValidatorTest {


    public static final String TEST_TEAM_1 = "testTeam1";
    @Autowired
    SubmissionRepository submissionRepository;
    @Autowired
    SampleRepository sampleRepository;
    @Autowired
    CoreSubmittableValidationHelper coreSubmittableValidationHelper;

    Errors errors;
    Sample sampleUnderValidation;
    Team team;
    Submission submission;
    Submitter submitter;

    @Test
    public void itemMustHaveConsistentArchive(){

    }

    @Test
    public void newUseOfAliasInSubmissionIsGood(){
        coreSubmittableValidationHelper.validateOnlyUseOfAliasInSubmission(sampleUnderValidation,sampleRepository,errors);

        assertThat(errors.getErrorCount(), is(equalTo(0)));

    }

    @Test
    public void updateUseOfAliasInSubmissionIsGood(){
        sampleRepository.insert(sampleUnderValidation);

        coreSubmittableValidationHelper.validateOnlyUseOfAliasInSubmission(sampleUnderValidation,sampleRepository,errors);

        assertThat(errors.getErrorCount(), is(equalTo(0)));

    }

    @Test
    public void twoCopiesOfAliasInSubmissionIsBad(){
        Sample sampleWithSameAlias = new Sample();
        BeanUtils.copyProperties(sampleUnderValidation,sampleWithSameAlias);
        sampleWithSameAlias.setId("iwasherefirst");

        sampleRepository.insert(sampleWithSameAlias);

        coreSubmittableValidationHelper.validateOnlyUseOfAliasInSubmission(sampleUnderValidation,sampleRepository,errors);

        assertThat(errors.getErrorCount(), is(equalTo(1)));


        assertThat(errors.getFieldError("alias"),notNullValue());

        FieldError error=errors.getFieldError("alias");

        assertThat(error.getDefaultMessage(),is(equalTo(SubsApiErrors.already_exists.name())));

    }


    @Before
    public void setUp(){
        tearDown();

        team = Team.build(TEST_TEAM_1);
        submitter = Submitter.build("bob@ebi.ac.uk");

        submission = new Submission();
        submission.setId("sub1");
        submission.setTeam(team);
        submission.setSubmitter(submitter);

        submissionRepository.insert(submission);


        sampleUnderValidation = new Sample();
        sampleUnderValidation.setAlias("testSample1");
        sampleUnderValidation.setTeam(team);
        sampleUnderValidation.setId("ts1");
        sampleUnderValidation.setSubmission(submission);

        errors = new BeanPropertyBindingResult(sampleUnderValidation, "sample");
    }

    @After
    public void tearDown(){

     submissionRepository.deleteAll();
     sampleRepository.deleteAll();
    }



}
