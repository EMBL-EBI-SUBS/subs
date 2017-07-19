package uk.ac.ebi.subs.agent.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import uk.ac.ebi.biosamples.client.BioSamplesClient;
import uk.ac.ebi.subs.agent.converters.BsdSampleToUsiSample;
import uk.ac.ebi.subs.agent.converters.UsiSampleToBsdSample;
import uk.ac.ebi.subs.data.submittable.Sample;

import java.util.ArrayList;
import java.util.List;

@Service
@ConfigurationProperties(prefix = "biosamples")
public class SubmissionService {
    private static final Logger logger = LoggerFactory.getLogger(SubmissionService.class);

    @Autowired
    BioSamplesClient client;

    @Autowired
    UsiSampleToBsdSample toBsdSample;
    @Autowired
    BsdSampleToUsiSample toUsiSample;

    public List<Sample> submit(List<Sample> sampleList) {
        ArrayList<Sample> submittedSamples = new ArrayList<>();

        sampleList.forEach(usiSample -> {
            uk.ac.ebi.biosamples.model.Sample bsdSample = toBsdSample.convert(usiSample);
            Sample submitted = submit(bsdSample);
            submittedSamples.add(submitted);
        });
        return submittedSamples;
    }

    private Sample submit(uk.ac.ebi.biosamples.model.Sample bsdSample) {
        logger.debug("Submitting sample.");

        try {
            return toUsiSample.convert(client.persist(bsdSample));
        } catch (HttpClientErrorException e) {
            throw new RuntimeException("Submission failed with error:", e);
        } catch (ResourceAccessException e) {
            throw new RuntimeException("Submission failed with error:", e);
        }

    }
}
