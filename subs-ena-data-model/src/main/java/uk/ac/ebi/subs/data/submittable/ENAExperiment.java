package uk.ac.ebi.subs.data.submittable;

import uk.ac.ebi.subs.data.component.*;
import uk.ac.ebi.subs.ena.annotation.*;

/**
 * Created by neilg on 28/03/2017.
 */
@ENAValidation(
        value = {
                @ENAFieldAttribute(attributeName = "platform_type", required = true),
                @ENAFieldAttribute(attributeName = "instrument_model", attributeFieldName = "platform_type", required = true),
                @ENAFieldAttribute(attributeName = "design_description", required = true),
                @ENAFieldAttribute(attributeName = "library_name", required = true),
                @ENAFieldAttribute(attributeName = "library_strategy", required = true),
                @ENAFieldAttribute(attributeName = "library_source", required = true),
                @ENAFieldAttribute(attributeName = "library_selection", required = true),
                @ENAFieldAttribute(attributeName = "library_layout", required = true),
                @ENAFieldAttribute(attributeName = "paired_nominal_length"),
                @ENAFieldAttribute(attributeName = "paired_nominal_sdev")
        },
        enaControlledValueAttributes = {
                @ENAControlledValueAttribute(
                        attributeName = "platform_type",
                        allowedValues = {"LS454","ILLUMINA","HELICOS","ABI_SOLID",
                                "COMPLETE_GENOMICS","BGISEQ","OXFORD_NANOPORE",
                                "PACBIO_SMRT","ION_TORRENT","CAPILLARY"}),
        }
)
public class    ENAExperiment extends AbstractENASubmittable<Assay> {
    public static final String DESIGN_DESCRIPTION = "design_description";
    public static final String LIBRARY_NAME = "library_name";
    public static final String LIBRARY_STRATEGY = "library_strategy";
    public static final String LIBRARY_SOURCE = "library_source";
    public static final String LIBRARY_SELECTION = "library_selection";

    public static final String PLATFORM_TYPE = "platform_type";
    public static final String INSTRUMENT_MODEL = "instrument_model";

    public static final String LIBRARY_LAYOUT = "library_layout";
    public static final String PAIRED_NOMINAL_LENGTH = "paired_nominal_length";
    public static final String PAIRED_NOMINAL_SDEV = "paired_nominal_sdev";

    public static final String SINGLE = "SINGLE";
    public static final String PAIRED = "PAIRED";

    @ENAField(fieldName = "LS454", attributeName = "instrument_model" ,values = {"454 GS 20", "454 GS FLX", "454 GS FLX+", "454 GS FLX Titanium", "454 GS Junior", "unspecified"})
    String ls454 ;

    @ENAField(fieldName = "ILLUMINA", attributeName = "instrument_model" ,values = {"Illumina Genome Analyzer", "Illumina Genome Analyzer II", "Illumina Genome Analyzer IIx",
            "Illumina HiSeq 2500", "Illumina HiSeq 2000", "Illumina HiSeq 1500", "Illumina HiSeq 1000", "Illumina MiSeq", "Illumina HiScanSQ",
            "HiSeq X Ten", "NextSeq 500", "HiSeq X Five", "Illumina HiSeq 3000", "Illumina HiSeq 4000", "NextSeq 550", "unspecified"})
    String illumina ;

    @ENAField(fieldName = "HELICOS", attributeName = "instrument_model" ,values = {"Helicos HeliScope", "unspecified"})
    String helicos;

    @ENAField(fieldName = "ABI_SOLID", attributeName = "instrument_model" ,values = {"AB SOLiD System 2.0", "AB SOLiD System 3.0", "AB SOLiD 3 Plus System",
            "AB SOLiD 4 System", "AB SOLiD 4hq System", "AB SOLiD PI System", "AB 5500 Genetic Analyzer", "AB 5500xl Genetic Analyzer",
            "AB 5500xl-W Genetic Analysis System", "unspecified"})
    String abiSolid ;

    @ENAField(fieldName = "COMPLETE_GENOMICS", attributeName = "instrument_model" ,values = {"Complete Genomics", "unspecified"})
    String completeGenomics;

    @ENAField(fieldName = "BGISEQ", attributeName = "instrument_model" ,values = {"BGISEQ-500"})
    String bgiseq;

    @ENAField(fieldName = "OXFORD_NANOPORE", attributeName = "instrument_model" ,values = {"MinION", "GridION", "unspecified"})
    String oxfordNanopore;

    @ENAField(fieldName = "PACBIO_SMRT", attributeName = "instrument_model" ,values = {"PacBio RS", "PacBio RS II", "Sequel", "unspecified"})
    String pacbioSMRT;

    @ENAField(fieldName = "ION_TORRENT", attributeName = "instrument_model" ,values = {"Ion Torrent PGM", "Ion Torrent Proton", "unspecified"})
    String ionTorrent;

    @ENAField(fieldName = "CAPILLARY", attributeName = "instrument_model" ,values = {"AB 3730xL Genetic Analyzer", "AB 3730 Genetic Analyzer", "AB 3500xL Genetic Analyzer",
            "AB 3500 Genetic Analyzer", "AB 3130xL Genetic Analyzer", "AB 3130 Genetic Analyzer", "AB 3130 Genetic Analyzer", "AB 310 Genetic Analyzer",
            "unspecified"})
    String capillary;

    @ENAField(fieldName = "design_description")
    String designDescription;

    @ENAField(fieldName = "library_name")
    String libraryName;

    @ENAField(fieldName = "library_strategy")
    String libraryStrategy;

    @ENAField(fieldName = "library_source")
    String librarySource;

    @ENAField(fieldName = "library_selection")
    String librarySelection;

    @ENAField(fieldName = "library_layout")
    String libraryLayout;

    @ENAField(fieldName = "paired_nominal_length")
    String nominalLength = null;

    @ENAField(fieldName = "paired_nominal_sdev")
    String nominalSdev = null;

    String singleLibraryLayout;
    PairedLibraryLayout pairedLibraryLayout;

    public ENAExperiment(Assay assay) throws IllegalAccessException {
        super(assay);
    }

    public ENAExperiment() throws IllegalAccessException {
        super();
    }

    /*
    public Map<String,String[]> getPlatformInstrumentMap () {
        Map<String,String[]> platformInstrumentMap = new HashMap<>();
        final Field[] declaredFields = this.getClass().getDeclaredFields();

        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(ENAPlatform.class)) {
                final ENAPlatform annotation = field.getAnnotation(ENAPlatform.class);
                platformInstrumentMap.put(annotation.name(),annotation.instrumentModels());
            }
        }
    }
    */

    @Override
    public void serialiseAttributes() throws IllegalAccessException {
        super.serialiseAttributes();
        serialiseLibraryLayout();
    }

    public void deSerialiseAttributes () throws IllegalAccessException {
        deserialiseLibraryLayout();
        super.deSerialiseAttributes();
        final ENAFieldAttribute instrumentModel = getInstrumentModel();
    }

    public void serialiseLibraryLayout() throws IllegalAccessException {
        if (libraryLayout == null)
            libraryLayout = SINGLE;
        else if (libraryLayout.equals(PAIRED)) {
            this.pairedLibraryLayout = new PairedLibraryLayout(nominalLength, nominalSdev);
            this.singleLibraryLayout = null;
        } else if (libraryLayout.equals(SINGLE)) {
            this.singleLibraryLayout = "";
            this.pairedLibraryLayout = null;
        }
    }

    public void deserialiseLibraryLayout() throws IllegalAccessException {
        if (pairedLibraryLayout != null) {
            nominalLength = pairedLibraryLayout.getNominalLength();
            nominalSdev = pairedLibraryLayout.getNominalSdev();
            libraryLayout = PAIRED;
        } else {
            libraryLayout = SINGLE;
        }
    }

    public SampleRef getSampleRef () {
        Assay assay = getBaseObject();
        if (assay.getSampleUses().isEmpty())
            return null;
        else
            return assay.getSampleUses().get(0).getSampleRef();
    }

    public void setSampleRef (SampleRef sampleRef) {
        SampleUse sampleUse = new SampleUse(sampleRef);
        getBaseObject().getSampleUses().add(sampleUse);
    }

    @Override
    public Submittable createNewSubmittable() {
        return new Assay();
    }

    public static class Single {}

    public static class PairedLibraryLayout {
        String nominalLength = null;
        String nominalSdev = null;

        public PairedLibraryLayout(String nominalLength, String nominalSdev) {
            this.nominalLength = nominalLength;
            this.nominalSdev = nominalSdev;
        }

        public PairedLibraryLayout () {}

        public String getNominalLength() {
            return nominalLength;
        }

        public String getNominalSdev() {
            return nominalSdev;
        }
    }


    public StudyRef getStudyRef() {
        return getBaseObject().getStudyRef();
    }

    public void setStudyRef(StudyRef studyRef) {
        getBaseObject().setStudyRef(studyRef);
    }

    public void setIllumina(String illumina) {
        this.illumina = illumina;
    }

    public void setDesignDescription(String designDescription) {
        this.designDescription = designDescription;
    }

    public void setLibraryName(String libraryName) {
        this.libraryName = libraryName;
    }

    public void setLibraryStrategy(String libraryStrategy) {
        this.libraryStrategy = libraryStrategy;
    }

    public String getLibrarySource() {
        return librarySource;
    }

    public void setLibrarySource(String librarySource) {
        this.librarySource = librarySource;
    }

    public void setLibrarySelection(String librarySelection) {
        this.librarySelection = librarySelection;
    }

    public void setLibraryLayout(String libraryLayout) {
        this.libraryLayout = libraryLayout;
    }

    public ENAFieldAttribute getInstrumentModel () {
        final ENAValidation enaValidation = getEnaValidation();
        for (ENAFieldAttribute enaFieldAttribute : enaValidation.value()) {
            if (enaFieldAttribute.attributeName().equals("instrument_model")) {
                return enaFieldAttribute;
            }
        }
        return null;
    }

}
