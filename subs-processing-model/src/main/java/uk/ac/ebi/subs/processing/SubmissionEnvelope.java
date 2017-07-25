package uk.ac.ebi.subs.processing;


import lombok.EqualsAndHashCode;
import lombok.ToString;
import uk.ac.ebi.subs.data.Submission;
import uk.ac.ebi.subs.data.component.SampleRef;
import uk.ac.ebi.subs.data.submittable.*;

import java.util.*;
import java.util.stream.Stream;


@ToString
@EqualsAndHashCode
public class SubmissionEnvelope {

    private Submission submission;

    private Set<SampleRef> supportingSamplesRequired = new HashSet<>();
    private List<Sample> supportingSamples = new ArrayList<>();

    private List<Analysis> analyses = new ArrayList<>();
    private List<Assay> assays = new ArrayList<>();
    private List<AssayData> assayData = new ArrayList<>();
    private List<EgaDac> egaDacs = new ArrayList<>();
    private List<EgaDacPolicy> egaDacPolicies = new ArrayList<>();
    private List<EgaDataset> egaDatasets = new ArrayList<>();
    private List<Project> projects = new ArrayList<>();
    private List<Sample> samples = new ArrayList<>();
    private List<SampleGroup> sampleGroups = new ArrayList<>();
    private List<Study> studies = new ArrayList<>();
    private List<Protocol> protocols = new ArrayList<>();

    public SubmissionEnvelope() {
    }

    ;

    public SubmissionEnvelope(Submission submission) {
        this.submission = submission;
    }

    public Submission getSubmission() {
        return submission;
    }

    public void setSubmission(Submission submission) {
        this.submission = submission;
    }

    public Set<SampleRef> getSupportingSamplesRequired() {
        return supportingSamplesRequired;
    }

    public void setSupportingSamplesRequired(Set<SampleRef> supportingSamplesRequired) {
        this.supportingSamplesRequired = supportingSamplesRequired;
    }

    public List<Sample> getSupportingSamples() {
        return supportingSamples;
    }

    public void setSupportingSamples(List<Sample> supportingSamples) {
        this.supportingSamples = supportingSamples;
    }

    public List<Analysis> getAnalyses() {
        return analyses;
    }

    public void setAnalyses(List<Analysis> analyses) {
        this.analyses = analyses;
    }

    public List<Assay> getAssays() {
        return assays;
    }

    public void setAssays(List<Assay> assays) {
        this.assays = assays;
    }

    public List<AssayData> getAssayData() {
        return assayData;
    }

    public void setAssayData(List<AssayData> assayData) {
        this.assayData = assayData;
    }

    public List<EgaDac> getEgaDacs() {
        return egaDacs;
    }

    public void setEgaDacs(List<EgaDac> egaDacs) {
        this.egaDacs = egaDacs;
    }

    public List<EgaDacPolicy> getEgaDacPolicies() {
        return egaDacPolicies;
    }

    public void setEgaDacPolicies(List<EgaDacPolicy> egaDacPolicies) {
        this.egaDacPolicies = egaDacPolicies;
    }

    public List<EgaDataset> getEgaDatasets() {
        return egaDatasets;
    }

    public void setEgaDatasets(List<EgaDataset> egaDatasets) {
        this.egaDatasets = egaDatasets;
    }

    public List<Project> getProjects() {
        return projects;
    }

    public void setProjects(List<Project> projects) {
        this.projects = projects;
    }

    public List<Sample> getSamples() {
        return samples;
    }

    public void setSamples(List<Sample> samples) {
        this.samples = samples;
    }

    public List<SampleGroup> getSampleGroups() {
        return sampleGroups;
    }

    public void setSampleGroups(List<SampleGroup> sampleGroups) {
        this.sampleGroups = sampleGroups;
    }

    public List<Study> getStudies() {
        return studies;
    }

    public void setStudies(List<Study> studies) {
        this.studies = studies;
    }

    public List<Protocol> getProtocols() {
        return protocols;
    }

    public void setProtocols(List<Protocol> protocols) {
        this.protocols = protocols;
    }

    /**
     * Get a list of all the lists of objects implementing Submittable within the submission.
     *
     * @return a list of all the lists of objects implementing Submittable within the submission.
     */
    private List<List<Submittable>> allSubmittablesLists() {
        List lists = Arrays.asList(analyses, assays, assayData, egaDacs, egaDacPolicies, egaDatasets, projects, samples, sampleGroups, studies);
        return (List<List<Submittable>>) lists;
    }

    public List<Submittable> allSubmissionItems() {
        List<Submittable> submittables = new ArrayList<>();

        this.allSubmittablesLists().forEach(submittables::addAll);

        return Collections.unmodifiableList(submittables);
    }

    public Stream<Submittable> allSubmissionItemsStream() {
        return allSubmittablesLists().stream().flatMap(l -> l.stream());
    }


}
