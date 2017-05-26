package uk.ac.ebi.subs.agent.services;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.internal.util.collections.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestOperations;
import uk.ac.ebi.subs.BioSamplesDependentTest;
import uk.ac.ebi.subs.agent.converters.BsdAttributeToUsiAttribute;
import uk.ac.ebi.subs.agent.converters.BsdRelationshipToUsiRelationship;
import uk.ac.ebi.subs.agent.converters.BsdSampleToUsiSample;
import uk.ac.ebi.subs.data.Submission;
import uk.ac.ebi.subs.data.component.SampleRef;
import uk.ac.ebi.subs.data.submittable.Sample;
import uk.ac.ebi.subs.processing.SubmissionEnvelope;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {
        FetchService.class,
        RestOperations.class,
        BsdSampleToUsiSample.class,
        BsdAttributeToUsiAttribute.class,
        BsdRelationshipToUsiRelationship.class
})
@ConfigurationProperties(prefix = "test")
@EnableAutoConfiguration
@Category(BioSamplesDependentTest.class)
public class FetchServiceTest {

    @Autowired
    FetchService service;

    private String accession;

    private SubmissionEnvelope envelope;
    private Submission submission;
    private SampleRef sampleRef;

    @Before
    public void setUp() {
        sampleRef = new SampleRef();
        sampleRef.setAccession(accession);

        submission = new Submission();
        submission.setId(UUID.randomUUID().toString());

        envelope = new SubmissionEnvelope();
        envelope.setSubmission(submission);
        envelope.setSupportingSamplesRequired(Sets.newSet(sampleRef));
    }

    @Test
    public void successfulSupportingSamplesServiceTest() {
        List<Sample> sampleList = null;
        sampleList = service.findSamples(Arrays.asList(accession));

        System.out.println(sampleList.get(0));
        Assert.assertNotNull(sampleList);
    }

    @Test
    public void sampleNotFoundTest() {

        List<Sample> sampleList = null;
        try {
            sampleList = service.findSamples(Arrays.asList("SAM"));
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
        }

    }

    public String getAccession() {
        return accession;
    }

    public void setAccession(String accession) {
        this.accession = accession;
    }
}