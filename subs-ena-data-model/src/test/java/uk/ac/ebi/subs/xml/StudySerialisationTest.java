package uk.ac.ebi.subs.xml;

import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.w3c.dom.Document;
import uk.ac.ebi.subs.data.component.Attribute;
import uk.ac.ebi.subs.data.component.Team;
import uk.ac.ebi.subs.data.submittable.ENAStudy;
import uk.ac.ebi.subs.data.submittable.ENASubmittable;
import uk.ac.ebi.subs.data.submittable.Study;
import uk.ac.ebi.subs.ena.validation.InvalidAttributeValue;
import uk.ac.ebi.subs.validator.data.SingleValidationResult;

import javax.xml.bind.JAXBException;
import javax.xml.transform.dom.DOMResult;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
public class StudySerialisationTest extends SerialisationTest {

    String STUDY_RESOURCE = "/uk/ac/ebi/subs/ena/submittable/study_template.json";
    String STUDY_MARSHALLER = "uk/ac/ebi/subs/data/submittable/study_mapping.xml";


    static String STUDY_ACCESSION_XPATH = "/STUDY/@accession";
    static String STUDY_ALIAS_XPATH = "/STUDY/@alias";
    static String STUDY_CENTER_NAME_XPATH ="/STUDY/@center_name";
    static String STUDY_TITLE_XPATH = "/STUDY/DESCRIPTOR[1]/STUDY_TITLE[1]/text()";
    static String STUDY_DESCRIPTION_XPATH = "/STUDY/DESCRIPTOR[1]/STUDY_DESCRIPTION[1]/text()";
    static String STUDY_ABSTRACT_XPATH = "/STUDY/DESCRIPTOR[1]/STUDY_ABSTRACT[1]/text()";
    static String STUDY_TYPE_XPATH = "/STUDY/DESCRIPTOR[1]/STUDY_TYPE[1]/@existing_study_type";
    static String STUDY_ATTRIBUTE = "/STUDY/STUDY_ATTRIBUTES[1]/STUDY_ATTRIBUTE";

    String STUDY_XSD = "https://raw.githubusercontent.com/enasequence/schema/master/src/main/resources/uk/ac/ebi/ena/sra/schema/SRA.study.xsd";

    @Test
    public void testMarshalStudyXML() throws Exception {
        Study study = getStudyFromResource();
        ENAStudy enaStudy = new ENAStudy(study);
        final Document document = documentBuilderFactory.newDocumentBuilder().newDocument();
        DOMResult domResult = new DOMResult(document);
        marshaller.marshal(enaStudy,domResult);
        final String documentString = getDocumentString(document);
        logger.info(documentString);
        assertNotNull(study);
    }

    protected ENASubmittable createENASubmittable() throws IllegalAccessException {
        return new ENAStudy();
    }

    @Test
    public void testMarshalStudyAccession() throws Exception {
        Study study = new Study();
        study.setAccession(UUID.randomUUID().toString());
        ENAStudy enaStudy = new ENAStudy(study);
        final Document document = documentBuilderFactory.newDocumentBuilder().newDocument();
        marshaller.marshal(enaStudy,new DOMResult(document));
        String xmlStudyAccession = executeXPathQueryNodeValue(document,STUDY_ACCESSION_XPATH);
        assertThat("study accession serialised to XML", enaStudy.getAccession(), equalTo(xmlStudyAccession));
    }

    @Test
    public void testMarshalStudyAlias() throws Exception {
        Study study = new Study();
        study.setAlias(UUID.randomUUID().toString());
        ENAStudy enaStudy = new ENAStudy(study);
        final Document document = documentBuilderFactory.newDocumentBuilder().newDocument();
        marshaller.marshal(enaStudy,new DOMResult(document));
        String str = executeXPathQueryNodeValue(document,STUDY_ALIAS_XPATH);
        assertThat("study alias serialised to XML", enaStudy.getAlias(), equalTo(str));
    }

    @Test
    public void testMarshalCenterName() throws Exception {
        Study study = new Study();
        Team team = new Team();
        team.setName(UUID.randomUUID().toString());
        study.setTeam(team);
        ENAStudy enaStudy = new ENAStudy(study);
        final Document document = documentBuilderFactory.newDocumentBuilder().newDocument();
        marshaller.marshal(enaStudy,new DOMResult(document));
        String str = executeXPathQueryNodeValue(document,STUDY_CENTER_NAME_XPATH);
        assertThat("study center_name serialised to XML", team.getName(), equalTo(str));
    }

    @Test
    public void testMarshalStudyTitle() throws Exception {
        Study study = new Study();
        study.setTitle(UUID.randomUUID().toString());
        ENAStudy enaStudy = new ENAStudy(study);
        final Document document = documentBuilderFactory.newDocumentBuilder().newDocument();
        marshaller.marshal(enaStudy,new DOMResult(document));
        String str = executeXPathQueryNodeValue(document,STUDY_TITLE_XPATH);
        assertThat("study title serialised to XML", enaStudy.getTitle(), equalTo(str));
    }

    @Test
    public void testMarshalStudyDescription() throws Exception {
        Study study = new Study();
        study.setDescription(UUID.randomUUID().toString());
        ENAStudy enaStudy = new ENAStudy(study);
        final Document document = documentBuilderFactory.newDocumentBuilder().newDocument();
        marshaller.marshal(enaStudy,new DOMResult(document));
        String str = executeXPathQueryNodeValue(document,STUDY_DESCRIPTION_XPATH);
        assertThat("study description serialised to XML", enaStudy.getDescription(), equalTo(str));
    }

    @Test
    public void testMarshalStudyAbstract() throws Exception {
        Study study = new Study();
        Attribute attribute = new Attribute();
        attribute.setName("study_abstract");
        attribute.setValue(UUID.randomUUID().toString());
        study.getAttributes().add(attribute);
        ENAStudy enaStudy = new ENAStudy(study);
        final Document document = documentBuilderFactory.newDocumentBuilder().newDocument();
        marshaller.marshal(enaStudy,new DOMResult(document));
        String str = executeXPathQueryNodeValue(document,STUDY_ABSTRACT_XPATH);
        assertThat("study abstract attribute serialised to XML", attribute.getValue(), equalTo(str));
    }

    @Test
    public void testMarshalStudyType() throws Exception {
        Study study = new Study();
        Attribute attribute = new Attribute();
        attribute.setName("existing_study_type");
        attribute.setValue("Whole Genome Sequencing");
        study.getAttributes().add(attribute);
        ENAStudy enaStudy = new ENAStudy(study);
        final Document document = documentBuilderFactory.newDocumentBuilder().newDocument();
        marshaller.marshal(enaStudy,new DOMResult(document));
        String str = executeXPathQueryNodeValue(document,STUDY_TYPE_XPATH);
        assertThat("study type attribute serialised to XML", attribute.getValue(), equalTo(str));
    }

    @Test
    public void testMarshalInvalidStudyType() throws Exception {
        Study study = new Study();
        Attribute attribute = new Attribute();
        attribute.setName("existing_study_type");
        String incorrectStudyType = UUID.randomUUID().toString();
        attribute.setValue(incorrectStudyType);
        study.getAttributes().add(attribute);
        ENAStudy enaStudy = new ENAStudy(study);
        final Document document = documentBuilderFactory.newDocumentBuilder().newDocument();
        marshaller.marshal(enaStudy,new DOMResult(document));
        final List<SingleValidationResult> validationResultList = enaStudy.getValidationResultList();

        assertThat("Study is invalid",enaStudy.isValid(),equalTo(false));
    }

    @Before
    public void setUp() throws IOException, JAXBException, URISyntaxException {
        super.setUp();
        marshaller = createMarshaller(ENAStudy.class,SUBMITTABLE_PACKAGE,STUDY_MARSHALLER,COMPONENT_PACKAGE, ATTRIBUTE_MAPPING);
        unmarshaller = createUnmarshaller(ENAStudy.class,SUBMITTABLE_PACKAGE,STUDY_MARSHALLER,COMPONENT_PACKAGE, ATTRIBUTE_MAPPING);
    }

    @Override
    protected String getName() {
        return "STUDY";
    }

    public Study getStudyFromResource () throws IOException {
        return getStudyFromResource(STUDY_RESOURCE);
    }

    public Study getStudyFromResource (String studyResource) throws IOException {
        final InputStream inputStream = getClass().getResourceAsStream(studyResource);
        final Study study = objectMapper.readValue(inputStream, Study.class);

        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        final UUID uuid = UUID.randomUUID();
        study.setId(uuid.toString());
        return study;

    }

    @Test
    public void testMarshalUnmarshallStudy () throws Exception {
        serialiseDeserialiseTest(STUDY_RESOURCE,ENAStudy.class,Study.class);
    }


}
