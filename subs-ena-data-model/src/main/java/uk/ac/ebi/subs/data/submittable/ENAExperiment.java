package uk.ac.ebi.subs.data.submittable;

import org.apache.commons.lang.StringUtils;
import uk.ac.ebi.subs.data.component.*;
import uk.ac.ebi.subs.ena.annotation.ENAAttribute;
import uk.ac.ebi.subs.ena.annotation.ENAPlatform;
import uk.ac.ebi.subs.ena.annotation.ENAValidation;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Created by neilg on 28/03/2017.
 */
@ENAValidation
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


    @ENAAttribute(name= "platform_type", allowedValues = "LS454" )
    @ENAAttribute(name = "instrument_model", allowedValues = {"454 GS 20", "454 GS FLX", "454 GS FLX", "454 GS FLX Titanium", "454 GS Junior", "unspecified"})
    @ENAPlatform(name = "LS454", instrumentModels = {"454 GS 20", "454 GS FLX", "454 GS FLX", "454 GS FLX Titanium", "454 GS Junior", "unspecified"})
    String ls454 ;

    @ENAPlatform(name = "ILLUMINA", instrumentModels = {"Illumina Genome Analyzer", "Illumina Genome Analyzer II", "Illumina Genome Analyzer IIx",
            "Illumina HiSeq 2500", "Illumina HiSeq 2000", "Illumina HiSeq 1500", "Illumina HiSeq 1000", "Illumina MiSeq", "Illumina HiScanSQ",
            "HiSeq X Ten", "NextSeq 500", "HiSeq X Five", "Illumina HiSeq 3000", "Illumina HiSeq 4000", "NextSeq 550", "unspecified"})
    String illumina ;

    @ENAPlatform(name = "HELICOS", instrumentModels = {"Helicos HeliScope", "unspecified"})
    String helicos;

    @ENAPlatform(name = "ABI_SOLID", instrumentModels = {"AB SOLiD System 2.0", "AB SOLiD System 3.0", "AB SOLiD 3 Plus System",
            "AB SOLiD 4 System", "AB SOLiD 4hq System", "AB SOLiD PI System", "AB 5500 Genetic Analyzer", "AB 5500xl Genetic Analyzer",
            "AB 5500xl-W Genetic Analysis System", "unspecified"})
    String abiSolid ;

    @ENAPlatform(name = "COMPLETE_GENOMICS", instrumentModels = {"Complete Genomics", "unspecified"})
    String completeGenomics;

    @ENAPlatform(name = "BGISEQ", instrumentModels = {"BGISEQ-500"})
    String bgiseq;

    @ENAPlatform(name = "OXFORD_NANOPORE", instrumentModels = {"MinION", "GridION", "unspecified"})
    String oxfordNanopore;

    @ENAPlatform(name = "PACBIO_SMRT", instrumentModels = {"PacBio RS", "PacBio RS II", "Sequel", "unspecified"})
    String pacbioSMRT;

    @ENAPlatform(name = "ION_TORRENT", instrumentModels = {"Ion Torrent PGM", "Ion Torrent Proton", "unspecified"})
    String ionTorrent;

    @ENAPlatform(name = "CAPILLARY", instrumentModels = {"AB 3730xL Genetic Analyzer", "AB 3730 Genetic Analyzer", "AB 3500xL Genetic Analyzer",
            "AB 3500 Genetic Analyzer", "AB 3130xL Genetic Analyzer", "AB 3130 Genetic Analyzer", "AB 3130 Genetic Analyzer", "AB 310 Genetic Analyzer",
            "unspecified"})
    String capillary;

    @ENAAttribute(name = DESIGN_DESCRIPTION)
    String designDescription;

    @ENAAttribute(name = LIBRARY_NAME)
    String libraryName;

    @ENAAttribute(name = LIBRARY_STRATEGY)
    String libraryStrategy;

    @ENAAttribute(name = LIBRARY_SOURCE)
    String librarySource;

    @ENAAttribute(name = LIBRARY_SELECTION)
    String librarySelection;

    @ENAAttribute(name = LIBRARY_LAYOUT, allowedValues = {SINGLE,PAIRED})
    String libraryLayout;

    @ENAAttribute(name = PAIRED_NOMINAL_LENGTH)
    String nominalLength = null;
    @ENAAttribute(name = PAIRED_NOMINAL_SDEV)
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
        //serialisePlatformTypeInstrumentModel();
        serialiseLibraryLayout();
    }

    public void deSerialiseAttributes () throws IllegalAccessException {
        deserialiseLibraryLayout();
        deserialisePlatformTypeInstrumentModel();
        super.deSerialiseAttributes();
    }



    private void serialisePlatformTypeInstrumentModel() throws IllegalAccessException {
        final Optional<Attribute> platformTypeAttribute = getExistingAttribute(PLATFORM_TYPE,false);
        final Optional<Attribute> instrumentModelAttribute = getExistingAttribute(INSTRUMENT_MODEL,false);
        if (!platformTypeAttribute.isPresent())
            throw new IllegalAccessException("Attribute " + PLATFORM_TYPE + " not defined");
        if (!instrumentModelAttribute.isPresent())
            throw new IllegalAccessException("Attribute " + INSTRUMENT_MODEL + " not defined");

        final Field[] declaredFields = this.getClass().getDeclaredFields();
        Field platformField = null;
        List<String> allowedPlatforms = new ArrayList<>();

        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(ENAPlatform.class)) {
                final ENAPlatform annotation = field.getAnnotation(ENAPlatform.class);
                allowedPlatforms.add(annotation.name());
                if (annotation.name().equalsIgnoreCase(platformTypeAttribute.get().getValue())) platformField = field;
            }
        }

        if (platformField == null)
            throw new IllegalArgumentException(String.format("%s is not a valid platform.  Platform must be one of %s",
                    platformTypeAttribute.get().getValue(), StringUtils.join(allowedPlatforms, ",")));

        List<String> instrumentModel = Arrays.asList(platformField.getAnnotation(ENAPlatform.class).instrumentModels());

        if (!instrumentModel.contains(instrumentModelAttribute.get().getValue()))
            throw new IllegalArgumentException(String.format("%s is not a valid instrument model for platform %s.  Instrument models must be one of %s",
                instrumentModelAttribute.get().getValue(), platformTypeAttribute.get().getValue(),StringUtils.join(instrumentModel, ",")));

        platformField.set(this,instrumentModelAttribute.get().getValue());
        deleteAttribute(platformTypeAttribute.get());
        deleteAttribute(instrumentModelAttribute.get());
    }

    public void deserialisePlatformTypeInstrumentModel () throws IllegalAccessException {

        final Field[] declaredFields = this.getClass().getDeclaredFields();
        Field platformField = null;

        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(ENAPlatform.class)) {
                if (field.get(this) != null) {
                    if (platformField != null) throw new IllegalArgumentException("Multiple platforms found in class");
                    platformField = field;
                }
            }
        }

        Attribute platformTypeAttribute = new Attribute();
        platformTypeAttribute.setName(PLATFORM_TYPE);
        platformTypeAttribute.setValue(platformField.getAnnotation(ENAPlatform.class).name());
        getAttributes().add(platformTypeAttribute);
        Attribute instrumentModelAttribute = new Attribute();
        instrumentModelAttribute.setName(INSTRUMENT_MODEL);
        String pt = (String) platformField.get(this);
        instrumentModelAttribute.setValue(pt);
        getAttributes().add(instrumentModelAttribute);
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
}
