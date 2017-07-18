package uk.ac.ebi.subs.api;


import uk.ac.ebi.subs.data.client.*;
import uk.ac.ebi.subs.data.client.Study;
import uk.ac.ebi.subs.data.component.*;
import uk.ac.ebi.subs.data.status.ProcessingStatusEnum;
import uk.ac.ebi.subs.data.status.SubmissionStatusEnum;
import uk.ac.ebi.subs.repository.model.*;
import uk.ac.ebi.subs.repository.model.Assay;
import uk.ac.ebi.subs.repository.model.Sample;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class Helpers {

    public static Submission generateSubmission() {
        Submission s = new Submission();

        s.setTeam(generateTestTeam());
        s.setSubmitter(generateTestSubmitter());

        return s;
    }

    private static Submitter generateTestSubmitter() {
        Submitter u = new Submitter();
        u.setEmail("test@test.org");
        return u;
    }

    public static List<Sample> generateTestSamples() {
        return generateTestSamples(2);
    }

    public static List<uk.ac.ebi.subs.data.client.Sample> generateTestClientSamples(int numberOfSamplesRequired) {
        List<uk.ac.ebi.subs.data.client.Sample> samples = new ArrayList<>(numberOfSamplesRequired);

        for (int i = 1; i <= numberOfSamplesRequired; i++) {
            uk.ac.ebi.subs.data.client.Sample s = new uk.ac.ebi.subs.data.client.Sample();
            samples.add(s);

            s.setAlias("D" + i);
            s.setTitle("Donor " + i);
            s.setDescription("Human sample donor");
            s.setTaxon("Homo sapiens");
            s.setTaxonId(9606L);
            s.setArchive(Archive.BioSamples);
        }

        return samples;
    }

    public static List<uk.ac.ebi.subs.data.client.Study> generateTestClientStudies(int numberOfStudiesRequired) {
        List<uk.ac.ebi.subs.data.client.Study> studies= new ArrayList<>(numberOfStudiesRequired);

        for (int i = 1; i <= numberOfStudiesRequired; i++) {
            uk.ac.ebi.subs.data.client.Study s = new uk.ac.ebi.subs.data.client.Study();
            studies.add(s);

            Attribute studyType = new Attribute();
            studyType.setName("study_type");
            studyType.setValue("Whole Genome Sequencing");

            Term studyFactorTerm = new Term();
            studyFactorTerm.setUrl("http://www.ebi.ac.uk/efo/EFO_0003744");

            studyType.getTerms().add(studyFactorTerm);

            s.setAlias("Study" + i);
            s.setTitle("My Sequencing Study " + i);
            s.setDescription("We sequenced some humans to discover variants linked with a disease");

            s.setArchive(Archive.Ena);

            Attribute studyAbstract = new Attribute();
            studyAbstract.setName("study_abstract");
            studyAbstract.setValue(s.getDescription());

            s.getAttributes().add(studyType);
            s.getAttributes().add(studyAbstract);

            LocalDate releaseDate = LocalDate.parse("2020-12-25");

            s.setReleaseDate(java.sql.Date.valueOf(releaseDate));
        }

        return studies;
    }

    private static Attribute attribute(String name, String value){
        Attribute attribute = new Attribute();
        attribute.setName(name);
        attribute.setValue(value);
        return attribute;
    }

    public static List<uk.ac.ebi.subs.data.client.Assay> generateTestClientAssays(int numberOfAssaysRequired) {
        List<uk.ac.ebi.subs.data.client.Assay> assays = new ArrayList<>(numberOfAssaysRequired);

        Study study = generateTestClientStudies(1).get(0);
        StudyRef studyRef = new StudyRef();
        studyRef.setAlias(study.getAlias());

        List<uk.ac.ebi.subs.data.client.Sample> samples = generateTestClientSamples(numberOfAssaysRequired);


        for (int i = 1; i <= numberOfAssaysRequired; i++) {
            uk.ac.ebi.subs.data.client.Assay a = new uk.ac.ebi.subs.data.client.Assay();
            assays.add(a);

            a.setId(createId());

            a.setAlias("A" + i);
            a.setTitle("Assay " + i);
            a.setDescription("Human sequencing experiment");

            a.setStudyRef(studyRef);

            a.setArchive(Archive.Ena);

            SampleRef sampleRef = new SampleRef();
            sampleRef.setAlias(samples.get(i-1).getAlias());
            SampleUse sampleUse = new SampleUse();
            sampleUse.setSampleRef( sampleRef);
            a.getSampleUses().add(sampleUse);

            a.getAttributes().add(attribute("library_strategy","WGS"));
            a.getAttributes().add(attribute("library_source","GENOMIC"));
            a.getAttributes().add(attribute("library_selection","RANDOM"));
            a.getAttributes().add(attribute("library_layout","SINGLE"));

            a.getAttributes().add(attribute("platform_type","ILLUMINA"));
            a.getAttributes().add(attribute("instrument_model","Illumina HiSeq 2000"));
        }

        return assays;
    }


    public static List<Sample> generateTestSamples(int numberOfSamplesRequired) {
        List<Sample> samples = new ArrayList<>(numberOfSamplesRequired);

        for (int i = 1; i <= numberOfSamplesRequired; i++) {
            Sample s = new Sample();
            samples.add(s);

            s.setId(createId());

            s.setAlias("D" + i);
            s.setTitle("Donor " + i);
            s.setDescription("Human sample donor");
            s.setTaxon("Homo sapiens");
            s.setTaxonId(9606L);

            s.setProcessingStatus(new ProcessingStatus(ProcessingStatusEnum.Draft));
        }

        return samples;
    }


    public static Team generateTestTeam() {
        Team d = new Team();
        d.setName("my-team");
        return d;
    }


    public static Submission generateTestSubmission() {
        Submission sub = new Submission();
        Team d = new Team();
        sub.setId(createId());

        sub.setTeam(generateTestTeam());

        sub.setSubmissionStatus(new SubmissionStatus(SubmissionStatusEnum.Draft));

        return sub;
    }

    private static String createId() {
        return UUID.randomUUID().toString();
    }
}
