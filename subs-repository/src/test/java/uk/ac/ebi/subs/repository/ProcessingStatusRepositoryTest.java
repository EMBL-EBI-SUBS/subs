package uk.ac.ebi.subs.repository;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.subs.TestRepoApplication;
import uk.ac.ebi.subs.repository.model.ProcessingStatus;
import uk.ac.ebi.subs.repository.repos.status.ProcessingStatusRepository;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = TestRepoApplication.class)
public class ProcessingStatusRepositoryTest {

    @Autowired
    private ProcessingStatusRepository processingStatusRepository;

    private final Logger logger = LoggerFactory.getLogger(ProcessingStatusRepositoryTest.class);


    @Before
    public void buildUp() {
        processingStatusRepository.deleteAll();

    }

    @Test
    public void testAggregationWithNoData() {
        Map<String,Integer> statusCounts = processingStatusRepository.summariseSubmissionStatus("foo");
        assertThat(statusCounts,notNullValue());
        assertThat(statusCounts,is(equalTo(new HashMap<>())));
    }

    @Test
    public void testAggregation() {
        Map<String,Integer> expected = new HashMap<>();
        expected.put("bad",1);
        expected.put("ok",2);

        for (Map.Entry<String,Integer> entry : expected.entrySet()){
            for (int i = 0; i < entry.getValue() ; i++){
                processingStatusRepository.insert(status(entry.getKey(),"foo","sample"));
            }
        }


        Map<String,Integer> statusCounts = processingStatusRepository.summariseSubmissionStatus("foo");
        assertThat(statusCounts,notNullValue());

        assertThat(statusCounts,is(equalTo(expected)));

    }

    @Test
    public void testTypeStatusAggregation() {
        Map<String,Map<String,Integer>> expected = new HashMap<>();
        expected.put("sample",new HashMap<>());
        expected.put("study",new HashMap<>());
        expected.get("sample").put("good",100);
        expected.get("sample").put("bad",2);
        expected.get("study").put("tolerable",42);

        for (Map.Entry<String,Map<String,Integer>> typeEntry : expected.entrySet() ) {
            String type = typeEntry.getKey();

            for (Map.Entry<String, Integer> statusCountEntry : typeEntry.getValue().entrySet()) {
                for (int i = 0; i < statusCountEntry.getValue(); i++) {
                    ProcessingStatus status = status(statusCountEntry.getKey(), "foo", type);
                    processingStatusRepository.insert(status);
                }
            }
        }


        Map<String,Map<String,Integer>> statusCounts = processingStatusRepository.summariseSubmissionStatusAndType("foo");
        assertThat(statusCounts,notNullValue());

        assertThat(statusCounts,is(equalTo(expected)));

    }


    private ProcessingStatus status(String status, String subId, String type) {
        ProcessingStatus ps = new ProcessingStatus();

        ps.setStatus(status);
        ps.setSubmissionId(subId);
        ps.setSubmittableType(type);

        return ps;
    }

}
