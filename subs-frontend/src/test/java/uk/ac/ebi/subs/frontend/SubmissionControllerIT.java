package uk.ac.ebi.subs.frontend;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.client.Traverson;
import org.springframework.hateoas.hal.Jackson2HalModule;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.subs.FrontendApplication;
import uk.ac.ebi.subs.data.submittable.Submission;
import uk.ac.ebi.subs.messaging.Channels;
import uk.ac.ebi.subs.repository.SubmissionRepository;
import static org.springframework.hateoas.client.Hop.rel;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = FrontendApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SubmissionControllerIT {

    @LocalServerPort
    private int port;

    private URL submit;

    private TestRestTemplate template;
    private List<Submission> submissionsReceived;

    @RabbitListener(queues = Channels.SUBMISSION_SUBMITTED)
    public void handleSampleCreation(Submission submission) {
        System.out.println("Received a newly created submission: accession = " + submission.getId());
        this.submissionsReceived.add(submission);
    }


    @Autowired
    SubmissionRepository submissionRepository;

    @Autowired RestTemplate restTemplate;

    private Submission sub;

    @Before
    public void setUp() throws Exception {
        this.submit = new URL("http://localhost:" + this.port + "/api/submit/");

        template = new TestRestTemplate(restTemplate);

        UUID uuid = UUID.randomUUID();

        sub = new Submission();
        sub.getDomain().setName("integrationTestExampleDomain."+uuid.toString());
        sub.getSubmitter().setEmail("test@example.ac.uk");

        this.submissionsReceived = new ArrayList<>();
    }

    @After
    public void tearDown() {
        Pageable pageable = new PageRequest(0,100);

        for (Submission repSub : submissionRepository.findByDomainName(sub.getDomain().getName(),pageable)) {
            if (sub.getDomain().getName().equals(repSub.getDomain().getName())) {
                submissionRepository.delete(repSub);
            }
        }
    }



    @Test
    public void doSubmit() throws URISyntaxException {
        template.put(submit.toString(), sub);

        Page<Submission> subs = submissionRepository.findByDomainName(sub.getDomain().getName(),new PageRequest(0,100));
        assertThat(subs.getTotalElements(), equalTo(1L));

        assertThat(submissionsReceived.size(),equalTo(1));
    }
}
