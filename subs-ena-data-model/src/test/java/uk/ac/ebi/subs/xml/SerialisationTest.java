package uk.ac.ebi.subs.xml;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlOptions;
import org.eclipse.persistence.jaxb.JAXBContextProperties;
import org.eclipse.persistence.jaxb.MarshallerProperties;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import uk.ac.ebi.subs.data.component.Team;
import uk.ac.ebi.subs.data.submittable.*;

import javax.xml.XMLConstants;
import javax.xml.bind.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.xpath.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static uk.ac.ebi.subs.xml.StudySerialisationTest.STUDY_ACCESSION_XPATH;

/**
 * Created by neilg on 28/03/2017.
 */
public abstract class SerialisationTest {
    static final Logger logger = LoggerFactory.getLogger(SerialisationTest.class);
    String ATTRIBUTE_MAPPING = "uk/ac/ebi/subs/data/component/attribute_mapping.xml";
    String SUBMITTABLE_PACKAGE = "uk.ac.ebi.subs.data.submittable";
    String COMPONENT_PACKAGE = "uk.ac.ebi.subs.data.component";

    static String ACCESSION_XPATH = "/%s/@accession";
    static String ALIAS_XPATH = "/%s/@alias";
    static String CENTER_NAME_XPATH ="/%s/@center_name";
    static String IDENTIFIERS_ACCESSION_XPATH = "/%s/IDENTIFIERS/PRIMARY_ID/text()";
    static String IDENTIFIERS_ALIAS_XPATH = "/%s/IDENTIFIERS/SUBMITTER_ID/text()";
    static String IDENTIFIERS_CENTER_NAME_XPATH ="/%s/IDENTIFIERS/SUBMITTER_ID/@namespace";

    DocumentBuilderFactory documentBuilderFactory = null;
    XPathFactory xPathFactory = null;
    XmlOptions xmlOptions = new XmlOptions();
    ArrayList<XmlError> validationErrors;
    ObjectMapper objectMapper = new ObjectMapper();
    Marshaller marshaller;
    Unmarshaller unmarshaller;

    public void setUp() throws IOException, JAXBException, URISyntaxException {
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        validationErrors = new ArrayList<>();
        xmlOptions.setErrorListener(validationErrors);
        documentBuilderFactory = DocumentBuilderFactory.newInstance();
        xPathFactory = XPathFactory.newInstance();
    }

    @Test
    public void testMarshalSubmittableAccession() throws Exception {
        Submittable submittable = createENASubmittable();
        submittable.setAccession(UUID.randomUUID().toString());
        String xpathQuery = String.format(ACCESSION_XPATH, getName());
        assertXMLSubmittable(submittable,xpathQuery,submittable.getAccession());
    }

    @Test
    public void testMarshalSubmittableAlias() throws Exception {
        Submittable submittable = createENASubmittable();
        submittable.setAlias(UUID.randomUUID().toString());
        String xpathQuery = String.format(ALIAS_XPATH, getName());
        assertXMLSubmittable(submittable,xpathQuery,submittable.getAlias());
    }

    @Test
    public void testMarshalCentreName() throws Exception {
        Submittable submittable = createENASubmittable();
        Team team = new Team();
        team.setName(UUID.randomUUID().toString());
        submittable.setTeam(team);
        String xpathQuery = String.format(CENTER_NAME_XPATH, getName());
        assertXMLSubmittable(submittable,xpathQuery,team.getName());
    }

    @Test
    public void testMarshalIdentifiersSubmittableAccession() throws Exception {
        Submittable submittable = createENASubmittable();
        submittable.setAccession(UUID.randomUUID().toString());
        String accessionXpathQuery = String.format(IDENTIFIERS_ACCESSION_XPATH, getName());
        assertXMLSubmittable(submittable,accessionXpathQuery,submittable.getAccession());
    }

    @Test
    public void testMarshalIdentifiersSubmittableAlias() throws Exception {
        Submittable submittable = createENASubmittable();
        submittable.setAlias(UUID.randomUUID().toString());
        String xpathQuery = String.format(IDENTIFIERS_ALIAS_XPATH, getName());
        assertXMLSubmittable(submittable,xpathQuery,submittable.getAlias());
    }

    @Test
    public void testMarshalIdentifiersCenterName() throws Exception {
        Submittable submittable = createENASubmittable();
        Team team = new Team();
        team.setName(UUID.randomUUID().toString());
        submittable.setTeam(team);
        String xpathQuery = String.format(IDENTIFIERS_CENTER_NAME_XPATH, getName());
        assertXMLSubmittable(submittable,xpathQuery,team.getName());
    }

    protected void assertXMLSubmittable (Submittable submittable, String xPathQuery, String actual) throws JAXBException, ParserConfigurationException, XPathExpressionException, TransformerException {
        final Document document = documentBuilderFactory.newDocumentBuilder().newDocument();
        marshaller.marshal(submittable,new DOMResult(document));
        String xmlAlias = executeXPathQueryNodeValue(document,xPathQuery);
        assertThat(xPathQuery, actual, equalTo(xmlAlias));
    }

    public Document marshal (Object object , Marshaller marshaller) throws ParserConfigurationException, JAXBException {
        final Document document = documentBuilderFactory.newDocumentBuilder().newDocument();
        marshaller.marshal(object,new DOMResult(document));
        return document;
    }

    Node executeXPathQuery (Document document, String xPathExpression) throws XPathExpressionException, TransformerException {
        final XPath xPath = xPathFactory.newXPath();
        StudySerialisationTest.logger.info(getDocumentString(document));
        final XPathExpression xpe = xPath.compile(xPathExpression);
        Node node = (Node) xpe.evaluate(document, XPathConstants.NODE);
        return node;
    }

    String executeXPathQueryNodeValue (Document document, String xPathExpression) throws XPathExpressionException, TransformerException {
        Node node = executeXPathQuery(document,xPathExpression);
        if (node != null) return node.getNodeValue();
        else return null;
    }

    String getDocumentString (Document document) throws TransformerException {
        final TransformerFactory transformerFactory = TransformerFactory.newInstance();
        DOMSource domSource = new DOMSource(document);
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.transform(domSource, result);
        return writer.toString();
    }

    public StreamSource createStreamSource (String resourceName) throws URISyntaxException {
        final URL resource = getClass().getClassLoader().getResource(resourceName);
        URI uri = resource.toURI();
        File file = new File(uri);
        StreamSource streamSource = new StreamSource(file);
        return streamSource;
    }

    public Marshaller createMarshaller (Class cl, String objectPackage, String objectMapperResource,
                                        String componentPackage, String componentResource) throws URISyntaxException, JAXBException {
        Map<String, Source> metadata = new HashMap<String, Source>();
        metadata.put(objectPackage, createStreamSource(objectMapperResource));
        metadata.put(componentPackage, createStreamSource(componentResource));

        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(JAXBContextProperties.OXM_METADATA_SOURCE, metadata);

        JAXBContext jaxbContext = JAXBContext.newInstance(new Class[] {cl}, properties);

        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.setProperty(MarshallerProperties.JSON_MARSHAL_EMPTY_COLLECTIONS, false);
        return marshaller;
    }

    public Unmarshaller createUnmarshaller (Class cl, String objectPackage, String objectMapperResource,
                                            String componentPackage, String componentResource) throws URISyntaxException, JAXBException {
        Map<String, Source> metadata = new HashMap<String, Source>();
        metadata.put(objectPackage, createStreamSource(objectMapperResource));
        metadata.put(componentPackage, createStreamSource(componentResource));

        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(JAXBContextProperties.OXM_METADATA_SOURCE, metadata);

        JAXBContext jaxbContext = JAXBContext.newInstance(new Class[] {cl}, properties);

        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.setProperty(MarshallerProperties.JSON_MARSHAL_EMPTY_COLLECTIONS, false);
        return unmarshaller;
    }

    public Validator getValidator(String url) throws MalformedURLException, SAXException {
        SchemaFactory schemaFactory = SchemaFactory
                .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        URL schemaURL = new URL(url);
        Schema schema = schemaFactory.newSchema(schemaURL);
        Validator validator = schema.newValidator();
        return validator;
    }

    protected abstract ENASubmittable createENASubmittable () throws IllegalAccessException;

    protected abstract String getName ();

    protected void serialiseDeserialiseTest (String studyResource,
                                             Class<? extends BaseSubmittableFactory> baseSubmittableFactoryClass,
                                             Class<? extends BaseSubmittable> baseSubmittableClass) throws Exception {
        final BaseSubmittable baseSubmittableFromResource = getBaseSubmittableFromResource(studyResource, baseSubmittableClass);
        final BaseSubmittable baseSubmittableForCompare = getBaseSubmittableFromResource(studyResource, baseSubmittableClass);
        if (baseSubmittableClass.equals(Study.class)) {
            ((Study)baseSubmittableForCompare).setReleaseDate(null);
        }
        baseSubmittableForCompare.setArchive(null);
        Collections.sort(baseSubmittableForCompare.getAttributes());

        final ENASubmittable enaSubmittable = BaseSubmittableFactory.create(baseSubmittableFactoryClass, baseSubmittableFromResource);
        final Document document = documentBuilderFactory.newDocumentBuilder().newDocument();
        marshaller.marshal(enaSubmittable,new DOMResult(document));
        logger.info(getDocumentString(document));
        DOMSource domSource = new DOMSource(document);
        final JAXBElement<? extends BaseSubmittableFactory> baseSubmittable = unmarshaller.unmarshal(domSource, baseSubmittableFactoryClass);
        final BaseSubmittableFactory baseSubmittableValue = baseSubmittable.getValue();
        baseSubmittableValue.deSerialiseAttributes();
        final Submittable baseObject = baseSubmittableValue.getBaseObject();
        Collections.sort(baseObject.getAttributes());
        baseObject.setId(baseSubmittableForCompare.getId());
        assertThat("serialised and deserialised submittable", baseSubmittableForCompare, equalTo(baseObject));
    }

    protected void serialiseDeserialiseTest (Submittable submittable,
                                             Class<? extends BaseSubmittableFactory> baseSubmittableFactoryClass) throws Exception {
        StringWriter stringWriter = new StringWriter();
        final Class<? extends Submittable> submittableClass = submittable.getClass();
        objectMapper.writeValue(stringWriter,submittable);
        // clone the object
        final Submittable clonedSubmittable = objectMapper.readValue(stringWriter.toString(), submittableClass);
        clonedSubmittable.setArchive(null);
        clonedSubmittable.setId(null);
        Collections.sort(clonedSubmittable.getAttributes());

        final ENASubmittable enaSubmittable = BaseSubmittableFactory.create(baseSubmittableFactoryClass, submittable);
        final Document document = documentBuilderFactory.newDocumentBuilder().newDocument();
        marshaller.marshal(enaSubmittable,new DOMResult(document));
        String documentString = getDocumentString(document);
        logger.info(documentString);
        DOMSource domSource = new DOMSource(document);
        final JAXBElement<? extends BaseSubmittableFactory> baseSubmittable = unmarshaller.unmarshal(domSource, baseSubmittableFactoryClass);
        final BaseSubmittableFactory baseSubmittableValue = baseSubmittable.getValue();
        baseSubmittableValue.deSerialiseAttributes();
        final Submittable baseObject = baseSubmittableValue.getBaseObject();
        Collections.sort(baseObject.getAttributes());
        assertThat("serialised and deserialised submittable", clonedSubmittable, equalTo(baseObject));
    }

    public BaseSubmittable getBaseSubmittableFromResource (String resource, Class<? extends BaseSubmittable> cl) throws IOException {
        final InputStream inputStream = getClass().getResourceAsStream(resource);
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        final BaseSubmittable baseSubmittable = objectMapper.readValue(inputStream, cl);
        return baseSubmittable;
    }
}
