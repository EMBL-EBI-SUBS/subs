package uk.ac.ebi.subs.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.subs.ApiApplication;
import uk.ac.ebi.subs.data.Submission;
import uk.ac.ebi.subs.data.client.Sample;
import uk.ac.ebi.subs.repository.model.SubmissionStatus;
import uk.ac.ebi.subs.repository.repos.SubmissionRepository;
import uk.ac.ebi.subs.repository.repos.status.SubmissionStatusRepository;
import uk.ac.ebi.subs.repository.repos.submittables.SampleRepository;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ApiApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("aap")
public class AAPIntegrationTest extends ApiIntegrationTest {

    @Value("${aap.url}")
    private String aapURL;

    @Value("${aap.username}")
    private String aapUsername;

    @Value("${aap.password}")
    private String appPassword;


    @Before
    public void buildUp() throws URISyntaxException, UnirestException {
        super.buildUp();
        final Map<String, String> jwtGetHeaders = ApiIntegrationTestHelper.createJWTGetHeaders(aapURL, aapUsername, appPassword);
        final Map<String, String> jwtPostHeaders = ApiIntegrationTestHelper.createJWTPostHeaders(aapURL, aapUsername, appPassword);
        testHelper = new ApiIntegrationTestHelper(objectMapper, rootUri,
                Arrays.asList(submissionRepository, sampleRepository, submissionStatusRepository),jwtGetHeaders,jwtPostHeaders);
    }


}
