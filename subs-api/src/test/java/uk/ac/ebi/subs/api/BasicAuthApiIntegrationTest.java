package uk.ac.ebi.subs.api;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.http.utils.Base64Coder;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.subs.ApiApplication;

import java.util.Map;

/**
 * Created by neilg on 27/07/2017.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ApiApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("basic_auth")
public class BasicAuthApiIntegrationTest extends ApiIntegrationTest {

    public static String DEFAULT_USER = "usi_user";
    public static String DEFAULT_PASSWORD = "password";

    @Override
    public Map<String, String> createGetHeaders() throws UnirestException {
        final Map<String, String> getHeaders = super.createGetHeaders();
        getHeaders.putAll(ApiIntegrationTestHelper.createBasicAuthheaders());
        return getHeaders;
    }

    @Override
    public Map<String, String> createPostHeaders() throws UnirestException {
        final Map<String, String> postHeaders = super.createPostHeaders();
        postHeaders.putAll(ApiIntegrationTestHelper.createBasicAuthheaders());
        return postHeaders;
    }

}
