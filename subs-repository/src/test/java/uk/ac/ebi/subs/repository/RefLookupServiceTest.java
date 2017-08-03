package uk.ac.ebi.subs.repository;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.subs.TestRepoApplication;
import uk.ac.ebi.subs.data.component.SampleRef;
import uk.ac.ebi.subs.data.component.Team;
import uk.ac.ebi.subs.repository.model.Sample;
import uk.ac.ebi.subs.repository.repos.submittables.SampleRepository;


import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * Created by rolando on 24/05/2017.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = TestRepoApplication.class)
public class RefLookupServiceTest {
    @Autowired
    private RefLookupService refLookupService;

    @Autowired
    private SampleRepository sampleRepository;

    private SampleRef testSampleRefAccessioned = new SampleRef();
    private SampleRef testSampleRefNotAccessioned = new SampleRef();

    private Sample testSampleAccessioned = new Sample();
    private Sample testSampleNotAccessioned = new Sample();

    private List<SampleRef> testSampleRefs;
    private Set<Sample> testSamples;

    @Before
    public void setUp() {
        // Just testing Samples and SampleRefs
        String testSampleAccession = "test" + randomChars();
        String testSampleTeamName = "testTeam" + randomChars();
        String testSampleAlias = "testAlias" + randomChars();

        // Set up test Samples and save
        testSampleAccessioned.setAccession(testSampleAccession);
        testSampleNotAccessioned.setAlias(testSampleAlias);
        testSampleNotAccessioned.setTeam(Team.build(testSampleTeamName));

        sampleRepository.insert(testSampleAccessioned);
        Sample t = sampleRepository.findByAccession(testSampleAccessioned.getAccession());

        sampleRepository.insert(testSampleNotAccessioned);

        // set up references to saved Samples
        testSampleRefAccessioned.setAccession(testSampleAccession);

        testSampleRefNotAccessioned.setAlias(testSampleAlias);
        testSampleRefNotAccessioned.setTeam(testSampleTeamName);

        // set up the Sample/SampleRef collections
        testSamples = new HashSet<>((Arrays.asList(testSampleAccessioned, testSampleNotAccessioned)));
        testSampleRefs = Arrays.asList(testSampleRefAccessioned, testSampleRefNotAccessioned);
    }

    @Test
    public void testFindByRef_RefHasAccession() {
        assertThat(refLookupService.lookupRef(testSampleRefAccessioned), equalTo(testSampleAccessioned));
    }

    @Test
    public void testFindByRef_RefHasNoAccession() {
        assertThat(refLookupService.lookupRef(testSampleRefNotAccessioned), equalTo(testSampleNotAccessioned));
    }

    @Test
    public void testFindByRefs() {
        assertTrue(refLookupService.lookupRefs(testSampleRefs).equals(testSamples));
    }

    @After
    public void cleanUp() {
        sampleRepository.delete(testSampleAccessioned);
        sampleRepository.delete(testSampleNotAccessioned);
    }

    private String randomChars() {
        return UUID.randomUUID().toString().substring(0, 9);
    }
}
