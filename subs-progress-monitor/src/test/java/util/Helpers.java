package util;


import uk.ac.ebi.subs.data.component.Team;
import uk.ac.ebi.subs.repository.model.Sample;
import uk.ac.ebi.subs.repository.model.Submission;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Helpers {

    public static List<Sample> generateTestSamples() {
        List<Sample> samples = new ArrayList<>();
        Sample sample2 = new Sample();
        sample2.setAccession("S2");
        sample2.setDescription("Test sample 2.");
        sample2.setId(UUID.randomUUID().toString());
        samples.add(sample2);

        Sample sample3 = new Sample();
        sample3.setAccession("S3");
        sample3.setDescription("Test sample 3.");
        sample3.setId(UUID.randomUUID().toString());
        samples.add(sample3);

        return samples;
    }

    public static Submission generateTestSubmission() {
        Submission sub = new Submission();
        sub.setId(UUID.randomUUID().toString());
        Team d = new Team();
        d.setName("subs-test");
        sub.setTeam(d);
        return sub;
    }
}
