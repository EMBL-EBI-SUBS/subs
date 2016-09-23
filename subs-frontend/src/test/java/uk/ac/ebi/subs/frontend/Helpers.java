package uk.ac.ebi.subs.frontend;

import uk.ac.ebi.subs.data.component.Domain;
import uk.ac.ebi.subs.data.submittable.Sample;
import uk.ac.ebi.subs.data.submittable.Submission;

import java.util.ArrayList;
import java.util.List;

public class Helpers {

    public static List<Sample> generateTestSamples() {
        List<Sample> samples = new ArrayList<>();
        Sample sample1 = new Sample();
        sample1.setDescription("Donor 1");
        sample1.setTaxon("Homo sapiens");
        sample1.setTaxonId(9606L);
        samples.add(sample1);

        Sample sample2 = new Sample();
        sample2.setDescription("Donor 2");
        sample2.setTaxon("Homo sapiens");
        sample2.setTaxonId(9606L);
        samples.add(sample2);

        return samples;
    }

    public static Submission generateTestSubmission() {
        Submission sub = new Submission();
        Domain d = new Domain();
        d.setName("my-domain");
        sub.setDomain(d);
        sub.setSamples(generateTestSamples());
        return sub;
    }
}
