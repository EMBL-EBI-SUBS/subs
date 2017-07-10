package uk.ac.ebi.subs.xml;

import com.fasterxml.jackson.databind.SerializationFeature;
import org.eclipse.persistence.jaxb.MarshallerProperties;
import org.hamcrest.core.IsNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import uk.ac.ebi.subs.data.component.*;
import uk.ac.ebi.subs.data.status.ProcessingStatusEnum;
import uk.ac.ebi.subs.data.submittable.Assay;
import uk.ac.ebi.subs.data.submittable.ENAExperiment;
import uk.ac.ebi.subs.data.submittable.ENASubmittable;
import uk.ac.ebi.subs.data.submittable.Sample;
import uk.ac.ebi.subs.ena.validation.InvalidAttributeValue;
import uk.ac.ebi.subs.validator.data.SingleValidationResult;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;


@RunWith(SpringJUnit4ClassRunner.class)
public class ExperimentSerialisationTest extends SerialisationTest {
    public static final String ILLUMINA = "Illumina";
    public static final String LS454 = "Ls454";
    public static final String HELICOS = "HELICOS";
    public static final String ABI_SOLID = "ABI_SOLID";
    public static final String COMPLETE_GENOMICS = "COMPLETE_GENOMICS";
    public static final String BGISEQ = "BGISEQ";
    public static final String OXFORD_NANOPORE = "OXFORD_NANOPORE";
    public static final String PACBIO_SMRT = "PACBIO_SMRT";
    public static final String ION_TORRENT = "ION_TORRENT";
    public static final String CAPILLARY = "CAPILLARY";

    String EXPERIMENT_MARSHALLER = "uk/ac/ebi/subs/data/submittable/experiment_mapping.xml";
    String ASSAY_RESOURCE = "/uk/ac/ebi/subs/ena/submittable/assay_template.json";

    static String EXPERIMENT_ACCESSION_XPATH = "/EXPERIMENT/@accession";
    static String EXPERIMENT_ALIAS_XPATH = "/EXPERIMENT/@alias";
    static String EXPERIMENT_CENTER_NAME_XPATH ="/EXPERIMENT/@center_name";
    static String EXPERIMENT_TITLE_XPATH = "/EXPERIMENT/TITLE[1]/text()";
    static String EXPERIMENT_STUDY_REF_ACCESSION = "/EXPERIMENT[1]/STUDY_REF[1]/@accession";
    static String EXPERIMENT_STUDY_REF_NAME = "/EXPERIMENT[1]/STUDY_REF[1]/@ref_name";
    static String EXPERIMENT_SAMPLE_REF_ACCESSION = "/EXPERIMENT[1]/DESIGN[1]/SAMPLE_DESCRIPTOR[1]/@accession";
    static String EXPERIMENT_SAMPLE_REF_NAME = "/EXPERIMENT[1]/DESIGN[1]/SAMPLE_DESCRIPTOR[1]/@ref_name";
    static String ILLUMINA_INSTRUMENT_MODEL_XPATH = "/EXPERIMENT[1]/PLATFORM[1]/ILLUMINA[1]/INSTRUMENT_MODEL[1]/text()";
    static String LS454_INSTRUMENT_MODEL_XPATH = "/EXPERIMENT[1]/PLATFORM[1]/LS454[1]/INSTRUMENT_MODEL[1]/text()";
    static String HELICOS_INSTRUMENT_MODEL_XPATH = "/EXPERIMENT[1]/PLATFORM[1]/HELICOS[1]/INSTRUMENT_MODEL[1]/text()";
    static String ABI_SOLID_INSTRUMENT_MODEL_XPATH = "/EXPERIMENT[1]/PLATFORM[1]/ABI_SOLID[1]/INSTRUMENT_MODEL[1]/text()";
    static String COMPLETE_GENOMICS_INSTRUMENT_MODEL_XPATH = "/EXPERIMENT[1]/PLATFORM[1]/COMPLETE_GENOMICS[1]/INSTRUMENT_MODEL[1]/text()";
    static String BGISEQ_INSTRUMENT_MODEL_XPATH = "/EXPERIMENT[1]/PLATFORM[1]/BGISEQ[1]/INSTRUMENT_MODEL[1]/text()";
    static String OXFORD_NANOPORE_INSTRUMENT_MODEL_XPATH = "/EXPERIMENT[1]/PLATFORM[1]/OXFORD_NANOPORE[1]/INSTRUMENT_MODEL[1]/text()";
    static String PACBIO_SMRT_INSTRUMENT_MODEL_XPATH = "/EXPERIMENT[1]/PLATFORM[1]/PACBIO_SMRT[1]/INSTRUMENT_MODEL[1]/text()";
    static String ION_TORRENT_SMRT_INSTRUMENT_MODEL_XPATH = "/EXPERIMENT[1]/PLATFORM[1]/ION_TORRENT[1]/INSTRUMENT_MODEL[1]/text()";
    static String CAPILLARY_SMRT_INSTRUMENT_MODEL_XPATH = "/EXPERIMENT[1]/PLATFORM[1]/CAPILLARY[1]/INSTRUMENT_MODEL[1]/text()";
    static String SINGLE_LIBRARY_LAYOUT_XPATH = "/EXPERIMENT[1]/DESIGN[1]/LIBRARY_DESCRIPTOR[1]/LIBRARY_LAYOUT[1]/SINGLE[1]";
    static String PAIRED_LIBRARY_LAYOUT_XPATH = "/EXPERIMENT[1]/DESIGN[1]/LIBRARY_DESCRIPTOR[1]/LIBRARY_LAYOUT[1]/PAIRED[1]";
    static String PAIRED_NOMINAL_LENGTH_XPATH = "/EXPERIMENT[1]/DESIGN[1]/LIBRARY_DESCRIPTOR[1]/LIBRARY_LAYOUT[1]/PAIRED[1]/@NOMINAL_LENGTH";
    static String PAIRED_NOMINAL_SDEV_XPATH = "/EXPERIMENT[1]/DESIGN[1]/LIBRARY_DESCRIPTOR[1]/LIBRARY_LAYOUT[1]/PAIRED[1]/@NOMINAL_SDEV";

    public static final String ILLUMINA_GENOME_ANALYZER_INSTRUMENT_MODEL = "Illumina Genome Analyzer";
    public static final String LS454_454_GS_20_INSTRUMENT_MODEL = "454 GS 20";
    public static final String HELICOS_HELISCOPE_INSTRUMENT_MODEL = "Helicos HeliScope";
    public static final String ABI_SOLID_SYSTEM_2_INSTRUMENT_MODEL = "AB SOLiD System 2.0";
    public static final String COMPLETE_GENOMICS_INSTRUMENT_MODEL = "Complete Genomics";
    public static final String BGI_SEQ_INSTRUMENT_MODEL = "BGISEQ-500";
    public static final String OXFORD_NANOPORE_INSTRUMENT_MODEL = "MinION";
    public static final String PACBIO_SMRT_INSTRUMENT_MODEL = "PacBio RS";
    public static final String ION_TORRENT_INSTRUMENT_MODEL = "Ion Torrent PGM";
    public static final String CAPILLARY_INSTRUMENT_MODEL = "AB 3730xL Genetic Analyzer";

    String EXPERIMENT_XSD = "https://raw.githubusercontent.com/enasequence/schema/master/src/main/resources/uk/ac/ebi/ena/sra/schema/SRA.study.xsd";

    @Override
    @Before
    public void setUp() throws IOException, JAXBException, URISyntaxException {
        super.setUp();
        marshaller = createMarshaller(ENAExperiment.class,SUBMITTABLE_PACKAGE,EXPERIMENT_MARSHALLER,COMPONENT_PACKAGE, ATTRIBUTE_MAPPING);
        unmarshaller = createUnmarshaller(ENAExperiment.class,SUBMITTABLE_PACKAGE,EXPERIMENT_MARSHALLER,COMPONENT_PACKAGE, ATTRIBUTE_MAPPING);
        marshaller.setProperty(MarshallerProperties.JSON_MARSHAL_EMPTY_COLLECTIONS, false);
    }

    @Override
    protected String getName() {
        return "EXPERIMENT";
    }

    @Test
    public void testMarshalExperimentAccession() throws Exception {
        Assay assay = createAssay();
        assay.setAccession(UUID.randomUUID().toString());
        ENAExperiment enaExperiment = new ENAExperiment(assay);
        final Document document = documentBuilderFactory.newDocumentBuilder().newDocument();
        marshaller.marshal(enaExperiment,new DOMResult(document));
        String accession = executeXPathQueryNodeValue(document,EXPERIMENT_ACCESSION_XPATH);
        assertThat("experiment accession serialised to XML", enaExperiment.getAccession(), equalTo(accession));
    }

    @Test
    public void testMarshalExperimentAlias() throws Exception {
        Assay assay = createAssay();
        assay.setAlias(UUID.randomUUID().toString());
        ENAExperiment enaExperiment = new ENAExperiment(assay);
        final Document document = documentBuilderFactory.newDocumentBuilder().newDocument();
        marshaller.marshal(enaExperiment,new DOMResult(document));
        String xmlStudyAlias = executeXPathQueryNodeValue(document,EXPERIMENT_ALIAS_XPATH);
        assertThat("experiment alias serialised to XML", enaExperiment.getAlias(), equalTo(xmlStudyAlias));
    }

    @Test
    public void testMarshalExperimentCenterName() throws Exception {
        Assay assay = createAssay();
        Team team = new Team();
        team.setName(UUID.randomUUID().toString());
        assay.setTeam(team);
        ENAExperiment enaExperiment = new ENAExperiment(assay);
        final Document document = documentBuilderFactory.newDocumentBuilder().newDocument();
        marshaller.marshal(enaExperiment,new DOMResult(document));
        String str = executeXPathQueryNodeValue(document,EXPERIMENT_CENTER_NAME_XPATH);
        assertThat("experiment center_name to XML", team.getName(), equalTo(str));
    }

    @Test
    public void testMarshalExperimentTitle() throws Exception {
        Assay assay = createAssay();
        assay.setTitle(UUID.randomUUID().toString());
        ENAExperiment enaExperiment = new ENAExperiment(assay);
        final Document document = documentBuilderFactory.newDocumentBuilder().newDocument();
        marshaller.marshal(enaExperiment,new DOMResult(document));
        String str = executeXPathQueryNodeValue(document,EXPERIMENT_TITLE_XPATH);
        assertThat("experiment title to XML", assay.getTitle(), equalTo(str));
    }

    @Test
    public void testMarshalExperimentStudyRef() throws Exception {
        Assay assay = createAssay();
        StudyRef studyRef = new StudyRef();
        studyRef.setAccession(UUID.randomUUID().toString());
        assay.setStudyRef(studyRef);
        ENAExperiment enaExperiment = new ENAExperiment(assay);
        final Document document = documentBuilderFactory.newDocumentBuilder().newDocument();
        marshaller.marshal(enaExperiment,new DOMResult(document));
        String studyAccession = executeXPathQueryNodeValue(document, EXPERIMENT_STUDY_REF_ACCESSION);
        assertThat("experiment alias serialised to XML", studyRef.getAccession(), equalTo(studyAccession));
    }

    @Test
    public void testMarshalExperimentSingleLibraryLayout() throws Exception {
        Assay assay = createAssay();
        Attribute libraryLayoutAttribute = new Attribute();
        libraryLayoutAttribute.setName(ENAExperiment.LIBRARY_LAYOUT);
        libraryLayoutAttribute.setValue(ENAExperiment.SINGLE);
        assay.getAttributes().add(libraryLayoutAttribute);
        ENAExperiment enaExperiment = new ENAExperiment(assay);
        final Document document = documentBuilderFactory.newDocumentBuilder().newDocument();
        marshaller.marshal(enaExperiment,new DOMResult(document));
        Node node = executeXPathQuery(document,SINGLE_LIBRARY_LAYOUT_XPATH);
        assertThat("experiment single library layout",node,notNullValue());
    }

    @Test
    public void testMarshalExperimentPairedLibraryLayout() throws Exception {
        Assay assay = createAssay();
        Attribute libraryLayoutAttribute = new Attribute();
        libraryLayoutAttribute.setName(ENAExperiment.LIBRARY_LAYOUT);
        libraryLayoutAttribute.setValue(ENAExperiment.PAIRED);
        assay.getAttributes().add(libraryLayoutAttribute);
        ENAExperiment enaExperiment = new ENAExperiment(assay);
        final Document document = documentBuilderFactory.newDocumentBuilder().newDocument();
        marshaller.marshal(enaExperiment,new DOMResult(document));
        Node node = executeXPathQuery(document,PAIRED_LIBRARY_LAYOUT_XPATH);
        assertThat("experiment paired library layout",node,notNullValue());
    }

    @Test
    public void testMarshalExperimentSampleRef() throws Exception {
        Assay assay = createAssay();
        SampleRef sampleRef = new SampleRef();
        sampleRef.setAccession(UUID.randomUUID().toString());
        SampleUse sampleUse = new SampleUse(sampleRef);
        assay.getSampleUses().add(sampleUse);
        ENAExperiment enaExperiment = new ENAExperiment(assay);
        StringWriter stringWriter = new StringWriter();
        objectMapper.writeValue(stringWriter,assay);
        String assayString = stringWriter.toString();
        logger.info(assayString);
        final Document document = documentBuilderFactory.newDocumentBuilder().newDocument();
        marshaller.marshal(enaExperiment,new DOMResult(document));
        String sampleAccession = executeXPathQueryNodeValue(document, EXPERIMENT_SAMPLE_REF_ACCESSION);
        assertThat("experiment alias serialised to XML", sampleRef.getAccession(), equalTo(sampleAccession));
    }

    @Test
    public void testExperimentIlluminaPlatform() throws Exception {
        testPlatform(ILLUMINA_INSTRUMENT_MODEL_XPATH,ILLUMINA, ILLUMINA_GENOME_ANALYZER_INSTRUMENT_MODEL);
    }

    @Test
    public void testExperimentLS454Platform() throws Exception {
        testPlatform(LS454_INSTRUMENT_MODEL_XPATH,LS454, LS454_454_GS_20_INSTRUMENT_MODEL);
    }

    @Test
    public void testExperimentHelicosPlatform() throws Exception {
        testPlatform(HELICOS_INSTRUMENT_MODEL_XPATH,HELICOS, HELICOS_HELISCOPE_INSTRUMENT_MODEL);
    }

    //@Test
    public void testExperimentHelicosInvalidPlatform() throws Exception {
        testInvalidPlatform(HELICOS_INSTRUMENT_MODEL_XPATH,HELICOS);
    }

    @Test
    public void testExperimentABISolidPlatform() throws Exception {
        testPlatform(ABI_SOLID_INSTRUMENT_MODEL_XPATH,ABI_SOLID, ABI_SOLID_SYSTEM_2_INSTRUMENT_MODEL);
    }

    @Test
    public void testExperimentCompleteGenomicsPlatform() throws Exception {
        testPlatform(COMPLETE_GENOMICS_INSTRUMENT_MODEL_XPATH,COMPLETE_GENOMICS, COMPLETE_GENOMICS_INSTRUMENT_MODEL);
    }

    @Test
    public void testExperimentBGISEQPlatform() throws Exception {
        testPlatform(BGISEQ_INSTRUMENT_MODEL_XPATH, BGISEQ, BGI_SEQ_INSTRUMENT_MODEL);
    }

    @Test
    public void testExperimentOxfordNanoporePlatform() throws Exception {
        testPlatform(OXFORD_NANOPORE_INSTRUMENT_MODEL_XPATH,OXFORD_NANOPORE, OXFORD_NANOPORE_INSTRUMENT_MODEL);
    }

    @Test
    public void testExperimentPacbioPlatform() throws Exception {
        testPlatform(PACBIO_SMRT_INSTRUMENT_MODEL_XPATH,PACBIO_SMRT, PACBIO_SMRT_INSTRUMENT_MODEL);
    }

    @Test
    public void testExperimentIonTorrentPlatform() throws Exception {
        testPlatform(ION_TORRENT_SMRT_INSTRUMENT_MODEL_XPATH,ION_TORRENT, ION_TORRENT_INSTRUMENT_MODEL);
    }

    @Test
    public void testExperimentCapillaryPlatform() throws Exception {
        testPlatform(CAPILLARY_SMRT_INSTRUMENT_MODEL_XPATH,CAPILLARY, CAPILLARY_INSTRUMENT_MODEL);
    }

    public void testPlatform(String platformXpathQuery, String plaformType, String instrumentModel) throws Exception {
        Assay assay = createAssay(plaformType,instrumentModel);
        ENAExperiment enaExperiment = new ENAExperiment(assay);
        final Document document = documentBuilderFactory.newDocumentBuilder().newDocument();
        marshaller.marshal(enaExperiment,new DOMResult(document));
        String returnedInstrumentModel = executeXPathQueryNodeValue(document,platformXpathQuery);
        assertThat("experiment alias serialised to XML", instrumentModel, equalTo(returnedInstrumentModel));
    }

    public void testInvalidPlatform(String platformXpathQuery, String plaformType) throws Exception {
        String instrumentModel = UUID.randomUUID().toString();
        Assay assay = createAssay(plaformType,instrumentModel);
        ENAExperiment enaExperiment = new ENAExperiment(assay);
        final Document document = documentBuilderFactory.newDocumentBuilder().newDocument();
        marshaller.marshal(enaExperiment,new DOMResult(document));
        String returnedInstrumentModel = executeXPathQueryNodeValue(document,platformXpathQuery);
        assertThat("experiment alias serialised to XML", instrumentModel, equalTo(returnedInstrumentModel));
    }

    @Test
    public void testInvalidInvalidPlatform() throws Exception {
        Assay assay = createAssay("New Platform","N/A");
        ENAExperiment enaExperiment = new ENAExperiment(assay);
        InvalidAttributeValue invalidAttributeValue = null;
        for (SingleValidationResult singleValidationResult : enaExperiment.getValidationResultList()) {
            if (singleValidationResult instanceof InvalidAttributeValue) {
                invalidAttributeValue = (InvalidAttributeValue) singleValidationResult;
                break;
            }
        }
        assertThat(invalidAttributeValue, is(IsNull.notNullValue()));

        final Document document = documentBuilderFactory.newDocumentBuilder().newDocument();
        marshaller.marshal(enaExperiment,new DOMResult(document));
    }

    //@Test(expected = IllegalArgumentException.class)
    public void testInvalidInstrument() throws Exception {
        Assay assay = createAssay(ILLUMINA,"N/A");
        ENAExperiment enaExperiment = new ENAExperiment(assay);
        final Document document = documentBuilderFactory.newDocumentBuilder().newDocument();
        marshaller.marshal(enaExperiment,new DOMResult(document));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidMultiplePlatforms() throws Exception {
        Assay assay = createAssay(ILLUMINA,ILLUMINA_GENOME_ANALYZER_INSTRUMENT_MODEL);
        Attribute platformTypeAttribute = new Attribute();
        platformTypeAttribute.setName(ENAExperiment.PLATFORM_TYPE);
        platformTypeAttribute.setValue(COMPLETE_GENOMICS);
        assay.getAttributes().add(platformTypeAttribute);
        ENAExperiment enaExperiment = new ENAExperiment(assay);
        final Document document = documentBuilderFactory.newDocumentBuilder().newDocument();
        marshaller.marshal(enaExperiment,new DOMResult(document));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidMultipleInstruments() throws Exception {
        Assay assay = createAssay(ILLUMINA,ILLUMINA_GENOME_ANALYZER_INSTRUMENT_MODEL);
        Attribute platformTypeAttribute = new Attribute();
        platformTypeAttribute.setName(ENAExperiment.INSTRUMENT_MODEL);
        platformTypeAttribute.setValue(COMPLETE_GENOMICS_INSTRUMENT_MODEL);
        assay.getAttributes().add(platformTypeAttribute);
        ENAExperiment enaExperiment = new ENAExperiment(assay);
        final Document document = documentBuilderFactory.newDocumentBuilder().newDocument();
        marshaller.marshal(enaExperiment,new DOMResult(document));
    }

    @Test
    public void testExperimentSerialisation () throws IOException, IllegalAccessException, JAXBException, ParserConfigurationException, TransformerException {
        final Assay assayFromResource = getAssayFromResource(ASSAY_RESOURCE);
        ENAExperiment enaExperiment = new ENAExperiment(assayFromResource);
        final Document document = documentBuilderFactory.newDocumentBuilder().newDocument();
        marshaller.marshal(enaExperiment,new DOMResult(document));
        final String documentString = getDocumentString(document);
        logger.info(documentString);
        assertNotNull(enaExperiment);
    }

    public Assay getAssayFromResource (String assayResource) throws IOException {
        final InputStream inputStream = getClass().getResourceAsStream(assayResource);
        final Assay assay = objectMapper.readValue(inputStream, Assay.class);

        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        final UUID uuid = UUID.randomUUID();
        assay.setId(uuid.toString());
        return assay;

    }

    static Assay createAssay () {
        return createAssay(ILLUMINA, ILLUMINA_GENOME_ANALYZER_INSTRUMENT_MODEL);
    }

    static Assay createAssay (String plaformType, String instrumentModel) {
        Assay assay = new Assay();
        Attribute platformTypeAttribute = new Attribute();
        platformTypeAttribute.setName(ENAExperiment.PLATFORM_TYPE);
        platformTypeAttribute.setValue(plaformType);
        assay.getAttributes().add(platformTypeAttribute);
        Attribute instrumentModelAttribute = new Attribute();
        instrumentModelAttribute.setName(ENAExperiment.INSTRUMENT_MODEL);
        instrumentModelAttribute.setValue(instrumentModel);
        assay.getAttributes().add(instrumentModelAttribute);
        return assay;
    }

    @Override
    protected ENASubmittable createENASubmittable() throws IllegalAccessException {
        return new ENAExperiment();
    }

    //@Test
    public void testMarshalUnmarshallExperiment () throws Exception {
        serialiseDeserialiseTest(ASSAY_RESOURCE,ENAExperiment.class,Assay.class);
    }



}