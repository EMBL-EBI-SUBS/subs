package uk.ac.ebi.subs.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitMessagingTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import uk.ac.ebi.subs.data.Submission;
import uk.ac.ebi.subs.data.client.Sample;
import uk.ac.ebi.subs.repository.model.SubmissionStatus;
import uk.ac.ebi.subs.repository.repos.SubmissionRepository;
import uk.ac.ebi.subs.repository.repos.status.SubmissionStatusRepository;
import uk.ac.ebi.subs.repository.repos.submittables.SampleRepository;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public abstract class ApiIntegrationTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @LocalServerPort
    private int port;
    protected String rootUri;

    protected ApiIntegrationTestHelper testHelper;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected SubmissionRepository submissionRepository;

    @Autowired
    protected SubmissionStatusRepository submissionStatusRepository;

    @Autowired
    protected SampleRepository sampleRepository;

    @MockBean
    private RabbitMessagingTemplate rabbitMessagingTemplate;

    @Before
    public void buildUp() throws URISyntaxException, UnirestException {
        rootUri = "http://localhost:" + port + "/api";
        testHelper = new ApiIntegrationTestHelper(objectMapper, rootUri,
                Arrays.asList(submissionRepository, sampleRepository, submissionStatusRepository),createGetHeaders(),createPostHeaders());
    }

    @After
    public void tearDown() throws IOException {
        submissionRepository.deleteAll();
        sampleRepository.deleteAll();
        submissionStatusRepository.deleteAll();
    }

    @AfterClass
    public static void shutdown() throws IOException {
        Unirest.shutdown();
    }

    @Test
    public void checkRootRels() throws UnirestException, IOException {
        Map<String, String> rootRels = testHelper.rootRels();

        assertThat(rootRels.keySet(), hasItems("submissions:create", "samples:create"));
    }

    @Test
    public void postSubmission() throws UnirestException, IOException {
        Map<String, String> rootRels = testHelper.rootRels();

        Submission submission = Helpers.generateSubmission();
        HttpResponse<JsonNode> submissionResponse = testHelper.postSubmission(rootRels, submission);

        List<SubmissionStatus> submissionStatuses = submissionStatusRepository.findAll();
        assertThat(submissionStatuses, notNullValue());
        assertThat(submissionStatuses, hasSize(1));
        SubmissionStatus submissionStatus = submissionStatuses.get(0);
        assertThat(submissionStatus.getStatus(), notNullValue());
        assertThat(submissionStatus.getStatus(), equalTo("Draft"));
    }

    @Test
    public void submissionWithSamples() throws IOException, UnirestException {
        Map<String, String> rootRels = testHelper.rootRels();

        String submissionLocation = testHelper.submissionWithSamples(rootRels);
    }

    @Test
    /**
     * POSTing a sample without a submission should throw an error
     */
    public void submittablesMustHaveSubmissionEmbedded()throws IOException, UnirestException{
        Map<String, String> rootRels = testHelper.rootRels();

        List<Sample> testSamples = Helpers.generateTestClientSamples(1);
        Sample sample = testSamples.get(0);

        assertThat(rootRels.get("samples:create"), notNullValue());

        HttpResponse<JsonNode> sampleResponse = Unirest.post(rootRels.get("samples:create"))
                .headers(testHelper.getPostHeaders())
                .body(sample)
                .asJson();

        assertThat(sampleResponse.getStatus(), is(equalTo(HttpStatus.BAD_REQUEST.value())));
    }

    /**
     * POSTing two samples with the same alias in one submission should throw an error
     */
    @Test
    public void reuseAliasInSubmissionGivesError() throws IOException, UnirestException{
        Map<String, String> rootRels = testHelper.rootRels();

        Submission submission = Helpers.generateSubmission();
        List<Sample> testSamples = Helpers.generateTestClientSamples(1);
        Sample sample = testSamples.get(0);


        HttpResponse<JsonNode> submissionResponse = testHelper.postSubmission(rootRels, submission);

        String submissionLocation = submissionResponse.getHeaders().getFirst("Location");
        Map<String, String> submissionRels = testHelper.relsFromPayload(submissionResponse.getBody().getObject());

        assertThat(submissionRels.get("samples:create"), notNullValue());

        sample.setSubmission(submissionLocation);

        HttpResponse<JsonNode> sampleFirstResponse = Unirest.post(rootRels.get("samples:create"))
                .headers(testHelper.getPostHeaders())
                .body(sample)
                .asJson();

        assertThat(sampleFirstResponse.getStatus(), is(equalTo(HttpStatus.CREATED.value())));

        HttpResponse<JsonNode> sampleSecondResponse = Unirest.post(rootRels.get("samples:create"))
                .headers(testHelper.getPostHeaders())
                .body(sample)
                .asJson();

        assertThat(sampleSecondResponse.getStatus(), is(equalTo(HttpStatus.BAD_REQUEST.value())));

        JSONArray errors = sampleSecondResponse.getBody().getObject().getJSONArray("errors");

        assertThat(errors,notNullValue());
        assertThat(errors.length(),is(equalTo(1)));

        Map<String,String> expectedError = new HashMap<>();
        expectedError.put("property","alias");
        expectedError.put("message","already_exists");
        expectedError.put("entity","Sample");
        expectedError.put("invalidValue",sample.getAlias());

        Map<String,Object> errorAsMap = new HashMap<>();
        JSONObject error = errors.getJSONObject(0);
        error.keySet().stream().forEach(key -> errorAsMap.put((String)key,error.get((String)key)));

        assertThat(errorAsMap,is(equalTo(expectedError)));
    }

    /**
     * POSTing two samples with different aliases in one submission, and changing one so they have the same
     * alias should throw an error
     */
    @Test
    public void sneakyReuseAliasInSubmissionGivesError() throws IOException, UnirestException{
        Map<String, String> rootRels = testHelper.rootRels();

        Submission submission = Helpers.generateSubmission();
        List<Sample> testSamples = Helpers.generateTestClientSamples(2);
        Map<Sample,String> testSampleLocations = new HashMap<>();

        HttpResponse<JsonNode> submissionResponse = testHelper.postSubmission(rootRels, submission);

        String submissionLocation = submissionResponse.getHeaders().getFirst("Location");
        Map<String, String> submissionRels = testHelper.relsFromPayload(submissionResponse.getBody().getObject());

        assertThat(submissionRels.get("samples:create"), notNullValue());

        for (Sample sample : testSamples) {

            sample.setSubmission(submissionLocation);

            HttpResponse<JsonNode> samplePostResponse = Unirest.post(rootRels.get("samples:create"))
                    .headers(testHelper.getPostHeaders())
                    .body(sample)
                    .asJson();

            assertThat(samplePostResponse.getStatus(), is(equalTo(HttpStatus.CREATED.value())));

            testSampleLocations.put(sample, samplePostResponse.getHeaders().getFirst("Location") );
        }

        Sample firstSample = testSamples.remove(0);

        for (Sample sample : testSamples){
            String sampleLocation = testSampleLocations.get(sample);

            sample.setAlias( firstSample.getAlias() );


            HttpResponse<JsonNode> samplePutResponse = Unirest.put(sampleLocation)
                    .headers(testHelper.getPostHeaders())
                    .body(sample)
                    .asJson();

            assertThat(samplePutResponse.getStatus(), is(equalTo(HttpStatus.BAD_REQUEST.value())));

            JSONArray errors = samplePutResponse.getBody().getObject().getJSONArray("errors");

            assertThat(errors,notNullValue());
            assertThat(errors.length(),is(equalTo(1)));

            Map<String,String> expectedError = new HashMap<>();
            expectedError.put("property","alias");
            expectedError.put("message","already_exists");
            expectedError.put("entity","Sample");
            expectedError.put("invalidValue",firstSample.getAlias());

            Map<String,Object> errorAsMap = new HashMap<>();
            JSONObject error = errors.getJSONObject(0);
            error.keySet().stream().forEach(key -> errorAsMap.put((String)key,error.get((String)key)));

            assertThat(errorAsMap,is(equalTo(expectedError)));

        }
    }

    /**
     * Make multiple submissions with the same contents. Use the sample history endpoint to check that you can
     * get the right number of entries back
     *
     * @throws IOException
     * @throws UnirestException
     */
    @Test
    public void sampleVersions() throws IOException, UnirestException {
        Map<String, String> rootRels = testHelper.rootRels();


        int numberOfSubmissions = 5;

        Submission submission = Helpers.generateSubmission();
        List<Sample> testSamples = Helpers.generateTestClientSamples(2);

        for (int i = 0; i < numberOfSubmissions; i++) {
            HttpResponse<JsonNode> submissionResponse = testHelper.postSubmission(rootRels, submission);

            String submissionLocation = submissionResponse.getHeaders().getFirst("Location");
            Map<String, String> submissionRels = testHelper.relsFromPayload(submissionResponse.getBody().getObject());

            assertThat(submissionRels.get("samples:create"), notNullValue());

            //add samples to the submission
            for (Sample sample : testSamples) {

                sample.setSubmission(submissionLocation);

                HttpResponse<JsonNode> sampleResponse = Unirest.post(rootRels.get("samples:create"))
                        .headers(testHelper.getPostHeaders())
                        .body(sample)
                        .asJson();

                assertThat(sampleResponse.getStatus(), is(equalTo(HttpStatus.CREATED.value())));
            }
        }

        String teamName = submission.getTeam().getName();
        String teamUrl = this.rootUri + "/teams/" + teamName;
        HttpResponse<JsonNode> teamQueryResponse = Unirest.get(teamUrl).headers(testHelper.getGetHeaders()).asJson();

        assertThat(teamQueryResponse.getStatus(), is(equalTo(HttpStatus.OK.value())));

        JSONObject teamPayload = teamQueryResponse.getBody().getObject();
        Map<String, String> teamRels = testHelper.relsFromPayload(teamPayload);

        String teamSamplesUrl = teamRels.get("samples");

        assertThat(teamSamplesUrl,notNullValue());

        HttpResponse<JsonNode> teamSamplesQueryResponse = Unirest.get(teamSamplesUrl).headers(testHelper.getGetHeaders()).asJson();
        assertThat(teamSamplesQueryResponse.getStatus(), is(equalTo(HttpStatus.OK.value())));
        JSONObject teamSamplesPayload = teamSamplesQueryResponse.getBody().getObject();
        JSONArray teamSamples = teamSamplesPayload.getJSONObject("_embedded").getJSONArray("samples");

        assertThat(teamSamples.length(),is(equalTo(testSamples.size())));

        for (int i = 0; i < teamSamples.length(); i++){
            JSONObject teamSample = teamSamples.getJSONObject(i);

            Map<String,String> sampleRels = testHelper.relsFromPayload(teamSample);
            String selfUrl = sampleRels.get("self");

            HttpResponse<JsonNode> sampleResponse = Unirest.get(selfUrl).headers(testHelper.getGetHeaders()).asJson();
            assertThat(sampleResponse.getStatus(), is(equalTo(HttpStatus.OK.value())));
            JSONObject samplePayload = sampleResponse.getBody().getObject();
            sampleRels = testHelper.relsFromPayload(samplePayload);

            String historyUrl = sampleRels.get("history");

            assertThat(historyUrl,notNullValue());

            HttpResponse<JsonNode> historyResponse = Unirest.get(historyUrl).headers(testHelper.getGetHeaders()).asJson();
            assertThat(historyResponse.getStatus(),is(equalTo(HttpStatus.OK.value())));
            JSONObject historyPayload = historyResponse.getBody().getObject();
            assertThat(historyPayload.has("_embedded"),is(true));
            JSONObject embedded = historyPayload.getJSONObject("_embedded");
            assertThat(embedded.has("samples"),is(true));
            JSONArray sampleHistory = embedded.getJSONArray("samples");
            assertThat(sampleHistory.length(),is(equalTo(numberOfSubmissions)));
        }
    }

    @Test
    public void testPut() throws IOException, UnirestException {
        Map<String, String> rootRels = testHelper.rootRels();

        Submission submission = Helpers.generateSubmission();
        HttpResponse<JsonNode> submissionResponse = testHelper.postSubmission(rootRels, submission);

        String submissionLocation = submissionResponse.getHeaders().getFirst("Location");
        Map<String, String> submissionRels = testHelper.relsFromPayload(submissionResponse.getBody().getObject());

        assertThat(submissionRels.get("samples"), notNullValue());

        Sample sample = Helpers.generateTestClientSamples(1).get(0);
        //add samples to the submission

        sample.setSubmission(submissionLocation);

        HttpResponse<JsonNode> sampleResponse = Unirest.post(rootRels.get("samples:create"))
                .headers(testHelper.getPostHeaders())
                .body(sample)
                .asJson();

        assertThat(sampleResponse.getStatus(), is(equalTo(HttpStatus.CREATED.value())));
        assertThat(sampleResponse.getHeaders().getFirst("Location"), notNullValue());

        String sampleLocation = sampleResponse.getHeaders().getFirst("Location");

        sample.setAlias("bob"); //modify the sample
        sample.setSubmission(submissionLocation);

        HttpResponse<JsonNode> samplePutResponse = Unirest.put(sampleLocation)
                .headers(testHelper.getPostHeaders())
                .body(sample)
                .asJson();

        logger.info("samplePutResponse: {}", samplePutResponse.getBody());
        assertThat(samplePutResponse.getStatus(), is(equalTo(HttpStatus.OK.value())));
    }

    public Map<String,String> createGetHeaders () throws UnirestException {
        return ApiIntegrationTestHelper.createStandardGetHeader();
    }

    public Map<String,String> createPostHeaders () throws UnirestException {
        return ApiIntegrationTestHelper.createStandardPostHeader();
    }
}