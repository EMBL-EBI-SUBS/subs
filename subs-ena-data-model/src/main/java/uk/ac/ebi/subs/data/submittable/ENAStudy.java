package uk.ac.ebi.subs.data.submittable;

import uk.ac.ebi.subs.ena.annotation.*;

@ENAValidation(
        value = {
                @ENAFieldAttribute(attributeName = "existing_study_type", required = true),
                @ENAFieldAttribute(attributeName = "study_abstract", required = true)
        })
public class ENAStudy extends AbstractENASubmittable<Study> {

    @ENAField(fieldName = "existing_study_type", values = {
            "Whole Genome Sequencing",
            "Metagenomics",
            "Transcriptome Analysis",
            "Resequencing",
            "Epigenetics",
            "Synthetic Genomics",
            "Forensic or Paleo-genomics",
            "Gene Regulation Study",
            "Cancer Genomics",
            "Population Genomics",
            "RNASeq",
            "Exome Sequencing",
            "Pooled Clone Sequencing",
            "Other"})
    String studyType;

    @ENAField(fieldName = "study_abstract")
    String studyAbstract;

    public ENAStudy(Study study) throws IllegalAccessException {
        super(study);
        //serialiseAttributes();
    }

    public ENAStudy() throws IllegalAccessException {
        super(new Study());
    }


    public String getStudyType() {
        return studyType;
    }

    public void setStudyType(String studyType) {
        this.studyType = studyType;
    }

    public String getStudyAbstract() {
        return studyAbstract;
    }

    public void setStudyAbstract(String studyAbstract) {
        this.studyAbstract = studyAbstract;
    }

    @Override
    public Submittable createNewSubmittable() {
        return new Study();
    }
}
