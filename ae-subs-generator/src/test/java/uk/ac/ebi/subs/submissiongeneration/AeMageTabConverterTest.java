package uk.ac.ebi.subs.submissiongeneration;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.subs.data.submittable.Sample;
import uk.ac.ebi.subs.data.Submission;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = SubGenTestApp.class)
public class AeMageTabConverterTest {

    @Autowired
    AeMageTabConverter aeMageTabConverter;

    @Test
    public void testEMTAB4222() throws IOException, ParseException, uk.ac.ebi.arrayexpress2.magetab.exception.ParseException {

        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("E-MTAB-4222.idf.txt").getFile());

        Submission sub = aeMageTabConverter.mageTabToSubmission(file.toURI().toURL());

        assertThat("Studies expected", sub.getStudies().size(), equalTo(1));
        assertThat("Samples expected", sub.getSamples().size(), equalTo(72));
        assertThat("Assays expected", sub.getAssays().size(), equalTo(72));
        assertThat("AssayData expected", sub.getAssayData().size(), equalTo(72));

        for (Sample s : sub.getSamples()){
            assertThat("Taxon name",s.getTaxon(),equalTo("Triticum aestivum"));
            assertThat("TaxonId",s.getTaxonId(),equalTo(4565L));
        }

    }

    @Test
    /**
     * Submission with multiple sdrf files breaks the parser, so lets just skip it
     */
    public void testEMTAB1443() throws IOException, ParseException, uk.ac.ebi.arrayexpress2.magetab.exception.ParseException {

        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("E-MTAB-1443.idf.txt").getFile());

        Submission sub = aeMageTabConverter.mageTabToSubmission(file.toURI().toURL());

        assertThat("Null submission expected", sub, nullValue());

    }
}
