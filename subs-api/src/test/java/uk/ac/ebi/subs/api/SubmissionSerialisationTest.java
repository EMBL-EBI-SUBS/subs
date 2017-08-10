package uk.ac.ebi.subs.api;

import com.mashape.unirest.http.Unirest;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.ObjectContent;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.subs.ApiApplication;
import uk.ac.ebi.subs.data.Submission;
import uk.ac.ebi.subs.processing.SubmissionEnvelope;
import uk.ac.ebi.subs.repository.model.Sample;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ApiApplication.class)
@JsonTest
public class SubmissionSerialisationTest {

    @Autowired
    private JacksonTester<Submission> json;

    String exampleJson;
    SubmissionEnvelope submissionEnvelope;

    @Before
    public void setUp() throws IOException {
        submissionEnvelope = new SubmissionEnvelope(new Submission());
        submissionEnvelope.getSubmission().getTeam().setName("exampleTeam");
        submissionEnvelope.getSubmission().getSubmitter().setEmail("test@example.ac.uk");

        exampleJson = "{\"submitter\":{\"email\":\"test@example.ac.uk\"},\"team\":{\"name\":\"exampleTeam\"}}";
    }

    @AfterClass
    public static void shutdown() throws IOException {
        Unirest.shutdown();
    }

    @Test
    public void testSerialize() throws Exception {
        assertThat(this.json.write(submissionEnvelope.getSubmission())).isEqualToJson(exampleJson);
    }

    @Test
    public void testDeserialize() throws Exception {

        ObjectContent<Submission> deserializedSub = this.json.parse(exampleJson);
        Submission actualSubmission = deserializedSub.getObject();

        assertThat(actualSubmission.getTeam())
                .isEqualTo(submissionEnvelope.getSubmission().getTeam());
        assertThat(actualSubmission.getSubmitter())
                .isEqualTo(submissionEnvelope.getSubmission().getSubmitter());
        assertThat(actualSubmission.getSubmissionDate())
                .isEqualTo(submissionEnvelope.getSubmission().getSubmissionDate());
    }
}
