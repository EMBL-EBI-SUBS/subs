package uk.ac.ebi.subs.api;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.subs.ApiApplication;

import java.io.IOException;
import java.util.*;

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

    @Override
    public Map<String, String> createGetHeaders() throws UnirestException {
        final Map<String, String> getHeaders = super.createGetHeaders();
        String jwtToken = getJWTToken(aapURL,aapUsername,appPassword);
        getHeaders.put("Authorization", "Bearer " + jwtToken);
        return getHeaders;
    }

    @Override
    public Map<String, String> createPostHeaders() throws UnirestException {
        final Map<String, String> postHeaders = super.createPostHeaders();
        String jwtToken = getJWTToken(aapURL,aapUsername,appPassword);
        postHeaders.put("Authorization", "Bearer " + jwtToken);
        return postHeaders;
    }

    static String getJWTToken (String authURL, String username, String password) throws UnirestException {
        final HttpResponse<String> stringHttpResponse = Unirest.get(authURL).basicAuth(username, password).asString();
        return stringHttpResponse.getBody();
    }

    @Override
    public void checkRootRels() throws IOException, UnirestException {
        final Map<String, String> standardGetHeader = ApiIntegrationTestHelper.createStandardGetHeader();
        testHelper = new ApiIntegrationTestHelper(objectMapper, rootUri,
                Arrays.asList(submissionRepository, sampleRepository, submissionStatusRepository),standardGetHeader,ApiIntegrationTestHelper.createStandardGetHeader());
        super.checkRootRels();
    }
}
