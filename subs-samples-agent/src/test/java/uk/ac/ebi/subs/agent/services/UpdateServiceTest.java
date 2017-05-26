package uk.ac.ebi.subs.agent.services;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
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
import uk.ac.ebi.subs.agent.converters.UsiAttributeToBsdAttribute;
import uk.ac.ebi.subs.agent.converters.UsiRelationshipToBsdRelationship;
import uk.ac.ebi.subs.agent.converters.UsiSampleToBsdSample;
import uk.ac.ebi.subs.agent.utils.TestUtils;
import uk.ac.ebi.subs.data.submittable.Sample;

import java.util.Arrays;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {
        UpdateService.class,
        UsiSampleToBsdSample.class,
        UsiAttributeToBsdAttribute.class,
        UsiRelationshipToBsdRelationship.class,
        BsdSampleToUsiSample.class,
        BsdAttributeToUsiAttribute.class,
        BsdRelationshipToUsiRelationship.class,
        TestUtils.class,
        RestOperations.class
})
@ConfigurationProperties(prefix = "test")
@EnableAutoConfiguration
@Category(BioSamplesDependentTest.class)
public class UpdateServiceTest {

    @Autowired
    UpdateService updateService;

    @Autowired
    TestUtils utils;

    private Sample sample;

    @Before
    public void setUp() {
        sample = utils.generateUsiSampleForUpdate();
    }

    @Test
    @Category(BioSamplesDependentTest.class)
    public void update() {
        List<Sample> updated = updateService.update(Arrays.asList(sample));
        Assert.assertEquals(updated.get(0).getAccession(), sample.getAccession());
    }

}
