package uk.ac.ebi.subs.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.subs.ApiApplication;
import uk.ac.ebi.subs.RabbitMQDependentTest;
import uk.ac.ebi.subs.repository.model.Submission;
import uk.ac.ebi.subs.repository.repos.SubmissionRepository;
import uk.ac.ebi.subs.repository.repos.status.SubmissionStatusRepository;
import uk.ac.ebi.subs.repository.repos.submittables.SampleRepository;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ApiApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Category(RabbitMQDependentTest.class)
public class ApiIntegrationTestRabbitDependent {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

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


    @Before
    public void buildUp() throws URISyntaxException {

        rootUri = "http://localhost:" + port + "/api";
        submissionRepository.deleteAll();
        sampleRepository.deleteAll();
        submissionStatusRepository.deleteAll();

        testHelper = new ApiIntegrationTestHelper(objectMapper,rootUri);
    }

    @After
    public void tearDown() throws IOException {
        Unirest.shutdown();
        submissionRepository.deleteAll();
        sampleRepository.deleteAll();
        submissionStatusRepository.deleteAll();
    }

    @Test
    @Category(RabbitMQDependentTest.class)
    //Requires dispatcher to delete the contents
    public void postThenDeleteSubmission() throws UnirestException, IOException {
        Map<String, String> rootRels = testHelper.rootRels();

        String submissionLocation = testHelper.submissionWithSamples(rootRels);
        HttpResponse<JsonNode> deleteResponse = Unirest.delete(submissionLocation)
                .headers(testHelper.standardPostHeaders())
                .asJson();

        assertThat(deleteResponse.getStatus(), equalTo(HttpStatus.NO_CONTENT.value()));

        List<Submission> submissions = submissionRepository.findAll();
        assertThat(submissions, empty());

    }

    /**
     * create a submission with some samples and submit it
     *
     * @throws IOException
     * @throws UnirestException
     */
    @Test
    @Category(RabbitMQDependentTest.class)
    public void simpleSubmissionWorkflow() throws IOException, UnirestException {
        Map<String, String> rootRels = testHelper.rootRels();

        String submissionLocation = testHelper.submissionWithSamples(rootRels);

        HttpResponse<JsonNode> submissionGetResponse = Unirest
                .get(submissionLocation)
                .headers(testHelper.standardGetHeaders())
                .asJson();

        assertThat(submissionGetResponse.getStatus(), is(equalTo(HttpStatus.OK.value())));
        JSONObject payload = submissionGetResponse.getBody().getObject();

        Map<String,String> rels = testHelper.relsFromPayload(payload);

        assertThat(rels.get("submissionStatus"),notNullValue());
        String submissionStatusLocation = rels.get("submissionStatus");

        HttpResponse<JsonNode> submissionStatusGetResponse = Unirest
                .get(submissionStatusLocation)
                .headers(testHelper.standardGetHeaders())
                .asJson();

        assertThat(submissionStatusGetResponse.getStatus(), is(equalTo(HttpStatus.OK.value())));
        JSONObject statusPayload = submissionStatusGetResponse.getBody().getObject();

        rels = testHelper.relsFromPayload(statusPayload);

        assertThat(rels.get("self"),notNullValue());
        submissionStatusLocation = rels.get("self");


        //update the submission
        //create a new submission
        HttpResponse<JsonNode> submissionPatchResponse = Unirest.patch(submissionStatusLocation)
                .headers(testHelper.standardPostHeaders())
                .body("{\"status\": \"Submitted\"}")
                .asJson();


        assertThat(submissionPatchResponse.getStatus(), is(equalTo(HttpStatus.OK.value())));
    }
}
