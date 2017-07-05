package uk.ac.ebi.subs.data.submittable;

import uk.ac.ebi.subs.ena.annotation.ENAAttribute;
import uk.ac.ebi.subs.ena.annotation.ENAValidation;

@ENAValidation(requiredAttributes = {"existing_study_type", "study_abstract"})
public class ENAStudy extends AbstractENASubmittable<Study> {

    public static final String EXISTING_STUDY_TYPE = "existing_study_type";
    @ENAAttribute(name = EXISTING_STUDY_TYPE,
            required = true,
            allowedValues = {
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
                    "Other"
            }
    )
    String studyType;
    public static final String STUDY_ABSTRACT = "study_abstract";
    @ENAAttribute(name = STUDY_ABSTRACT, required = true)
    String studyAbstract;

    @ENAAttribute(name = "test_field", required = false, allowedValues = "ILLUMINA")
    String testField;


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
