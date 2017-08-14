package uk.ac.ebi.subs.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.Unirest;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.subs.ApiApplication;
import uk.ac.ebi.subs.api.services.SubmittableValidationDispatcher;
import uk.ac.ebi.subs.repository.model.Sample;
import uk.ac.ebi.subs.repository.model.Study;
import uk.ac.ebi.subs.repository.repos.SubmissionRepository;
import uk.ac.ebi.subs.repository.repos.status.SubmissionStatusRepository;
import uk.ac.ebi.subs.repository.repos.submittables.SampleRepository;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Map;

import static org.mockito.Mockito.*;

/**
 * Created by rolando on 12/06/2017.
 */
@RunWith(SpringRunner.class)
@ActiveProfiles({"SubmittableValidationDispatcherTest","basic_auth"})
@SpringBootTest(classes = ApiApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SubmittableHandlerTest {
    @LocalServerPort
    private int port;
    private String rootUri;

    private ApiIntegrationTestHelper testHelper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    SubmissionStatusRepository submissionStatusRepository;

    @Autowired
    private SampleRepository sampleRepository;

    @Autowired
    private SubmittableValidationDispatcher submittableValidationDispatcher;

    @Before
    public void buildUp() throws URISyntaxException {
        rootUri = "http://localhost:" + port + "/api";
        final Map<String, String> standardGetContentHeader = ApiIntegrationTestHelper.createStandardGetHeader();
        standardGetContentHeader.putAll(ApiIntegrationTestHelper.createBasicAuthheaders(TestWebSecurityConfig.USI_USER,TestWebSecurityConfig.USI_PASSWORD));
        final Map<String, String> standardPostContentHeader = ApiIntegrationTestHelper.createStandardGetHeader();
        standardPostContentHeader.putAll(ApiIntegrationTestHelper.createBasicAuthheaders(TestWebSecurityConfig.USI_USER,TestWebSecurityConfig.USI_PASSWORD));
        testHelper = new ApiIntegrationTestHelper(objectMapper, rootUri,
                Arrays.asList(submissionRepository, sampleRepository, submissionStatusRepository),standardGetContentHeader,standardPostContentHeader);
    }

    @After
    public void tearDown() throws IOException {
        submissionRepository.deleteAll();
        sampleRepository.deleteAll();
        submissionStatusRepository.deleteAll();
    }

    @AfterClass
    public static void shutdown() throws IOException {
        //Unirest.shutdown();
    }

    @Test
    public void testValidationMessageSamplesOnSubmit() throws Exception {
        testHelper.submissionWithSamples(testHelper.rootRels());

        // assert that created Samples caused a validation request event
        verify(submittableValidationDispatcher, atLeast(1)).validateCreate(any(Sample.class));
    }

    @Test
    public void testValidationMessageStudiesOnSubmit() throws Exception {
        testHelper.submissionWithStudies(testHelper.rootRels());

        verify(submittableValidationDispatcher, atLeast(1)).validateCreate(any(Study.class));
    }
}
