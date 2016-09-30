package uk.ac.ebi.subs.submissiongeneration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.ac.ebi.arrayexpress2.magetab.datamodel.IDF;
import uk.ac.ebi.arrayexpress2.magetab.datamodel.MAGETABInvestigation;
import uk.ac.ebi.arrayexpress2.magetab.datamodel.SDRF;
import uk.ac.ebi.arrayexpress2.magetab.datamodel.graph.Node;
import uk.ac.ebi.arrayexpress2.magetab.datamodel.sdrf.node.*;
import uk.ac.ebi.arrayexpress2.magetab.datamodel.sdrf.node.attribute.ArrayDesignAttribute;
import uk.ac.ebi.arrayexpress2.magetab.datamodel.sdrf.node.attribute.CharacteristicsAttribute;
import uk.ac.ebi.arrayexpress2.magetab.datamodel.sdrf.node.attribute.FactorValueAttribute;
import uk.ac.ebi.arrayexpress2.magetab.exception.ParseException;
import uk.ac.ebi.arrayexpress2.magetab.parser.MAGETABParser;
import uk.ac.ebi.ena.taxonomy.client.TaxonomyClient;
import uk.ac.ebi.ena.taxonomy.client.model.Taxon;
import uk.ac.ebi.subs.data.component.*;
import uk.ac.ebi.subs.data.submittable.*;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class AeMageTabConverter {

    private static final Logger logger = LoggerFactory.getLogger(ArrayExpressSubmissionGenerationService.class);

    @Autowired
    TaxonomyClient taxonomyClient;

    Set<String> protocolTypesTriggeringAssayData = new HashSet<>(
            Arrays.asList(
                    "nucleic acid sequencing protocol",
                    "array scanning and feature extraction protocol",
                    "normalization data transformation protocol")
    );

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    SimpleDateFormat alternateSdf = new SimpleDateFormat("dd/MM/yyyy");

    public Submission mageTabToSubmission(URL mageTabUrl) throws ParseException {
        MAGETABParser parser = new MAGETABParser();

        MAGETABInvestigation investigation = parser.parse(mageTabUrl);

        if (investigation.IDF.sdrfFile.size() > 1){
            return null; //TODO parser doesn't seem to work properly where there is more than one
        }

        Submission submission = mageTabToSubmission(investigation);

        return submission;
    }

    public Submission mageTabToSubmission(MAGETABInvestigation investigation) {
        return createSubmission(investigation);
    }

    Submission createSubmission(MAGETABInvestigation investigation) {
        Submission submission = new Submission();

        Study study = createStudy(investigation.IDF);

        Map<String, String> protocolTypes = new HashMap<>();
        for (Protocol protocol : study.getProtocols()) {
            protocolTypes.put(protocol.getName(), protocol.getType());
        }

        Optional<Contact> firstContact = study.getContacts().stream().filter(c -> c.getEmail() != null).findFirst();
        String domainName;
        String email;
        if (firstContact.isPresent() && firstContact.get().getEmail() != null) {
            domainName = firstContact.get().getEmail();
            email = firstContact.get().getEmail();
        }
        else {
            domainName = investigation.getAccession();
            email = domainName+"@noemail.ac.uk";
        }

        submission.getDomain().setName(domainName);
        submission.getSubmitter().setEmail(email);
        study.setDomain(submission.getDomain());

        submission.getStudies().add(study);
        convertSdrf(investigation.SDRF, submission, (StudyRef)study.asRef(), protocolTypes);

        return submission;
    }

    void convertSdrf(SDRF sdrf, Submission submission, StudyRef studyRef, Map<String, String> protocolTypes) {

        Collection<SourceNode> rootNodes = (Collection<SourceNode>) sdrf.getRootNodes();


        for (SourceNode node : rootNodes) {
            Sample sample = sampleFromNode(node, studyRef);
            submission.getSamples().add(sample);

            for (Node childNode : node.getChildNodes()) {
                Assay assay = new Assay();
                assay.setDomain(new Domain());
                assay.getDomain().setName(studyRef.getDomain());
                assay.setAlias(studyRef.getAlias() + '~' + node.getNodeName());
                assay.setStudyRef(studyRef);
                assay.setSampleRef((SampleRef) sample.asRef());
                assay.setArchive(Archive.ArrayExpress);

                submission.getAssays().add(assay);

                Collection<AssayData> assayData = new ArrayList<>();
                traverseAssayNodes(assay, childNode, assayData, 0, protocolTypes);

                submission.getAssayData().addAll(assayData);
            }
        }
    }

    void handleProtocolRef(ProtocolApplicationNode node,Attributes attributes){
        buildSingleAttribute("protocol ref", node.protocol, attributes);
        if (node.performer != null){
            buildSingleAttribute("performer", node.performer.getNodeName(),attributes);
        }

    }

    void traverseAssayNodes(Assay assay, Node node, Collection<AssayData> assayData, int depth, Map<String, String> protocolTypes) {

        logger.debug("Alias {}",assay.getAlias());
        logger.debug("Depth {}",depth);
        logger.debug("Node {}",node);


        boolean childNodesProcessed = false;

        switch (node.getNodeType()) {
            case "protocolref":
                ProtocolApplicationNode protocolApplicationNode = (ProtocolApplicationNode) node;
                String type = protocolTypes.get(protocolApplicationNode.protocol);

                if (protocolTypesTriggeringAssayData.contains(type)) {
                    childNodesProcessed = true;
                    AssayData ad = assayData(assay, assayData);
                    traverseAssayDataNodes(ad, node, depth);
                } else {
                    handleProtocolRef(protocolApplicationNode,assay);
                    buildSingleAttribute("protocol ref", protocolApplicationNode.protocol, assay);
                }
                break;
            case "scanname":
                ScanNode scanNode = (ScanNode)node;
                childNodesProcessed = true;
                AssayData scanAd = assayData(assay,assayData);
                traverseAssayDataNodes(scanAd,node,depth);
                break;
            case "extractname":
                ExtractNode extractNode = (ExtractNode) node;
                buildSingleAttribute("extract name", extractNode.getNodeName(), assay);

                for (Map.Entry<String,List<String>> entry : extractNode.comments.entrySet()){
                    for (String value : entry.getValue()) {
                        buildSingleAttribute(entry.getKey(),value,assay);
                    }
                }

                break;
            case "labeledextractname":
                LabeledExtractNode labeledExtractNode = (LabeledExtractNode) node;
                buildSingleAttribute("labeled extract name", labeledExtractNode.getNodeName(), assay);
                buildSingleAttribute("label", labeledExtractNode.label.getAttributeValue(), assay);
                break;
            case "assayname":
                AssayNode assayNode = (AssayNode) node;
                buildSingleAttribute("assay name", assayNode.getNodeName(), assay);

                for (ArrayDesignAttribute arrayDesignAttribute : assayNode.arrayDesigns) {
                    buildSingleAttribute("array design", arrayDesignAttribute.getNodeName(), assay);
                }

                for (FactorValueAttribute factorValue : assayNode.factorValues) {
                    buildSingleAttribute("factor value[" + factorValue.type + "]", factorValue.getNodeName(), assay);
                }
                break;
            default:
                logger.debug("no assay handler for " + node.getNodeType());

        }


        if (!childNodesProcessed) {
            for (Node childNode : node.getChildNodes()) {
                traverseAssayNodes(assay, childNode, assayData, depth + 1, protocolTypes);
            }
        }


    }

    void traverseAssayDataNodes(AssayData assayData, Node node, int depth) {
        logger.debug("Alias {}",assayData.getAlias());
        logger.debug("Depth {}",depth);
        logger.debug("Node {}",node);

        switch (node.getNodeType()) {
            case "protocolref":
                ProtocolApplicationNode protocolApplicationNode = (ProtocolApplicationNode) node;
                handleProtocolRef(protocolApplicationNode,assayData);
                break;
            case "derivedarraydatafile":
                DerivedArrayDataNode derivedArrayDataNode = (DerivedArrayDataNode) node;
                uk.ac.ebi.subs.data.component.File derivedFile = new uk.ac.ebi.subs.data.component.File();
                derivedFile.setName(derivedArrayDataNode.getNodeName());
                derivedFile.setType("derived array data");
                assayData.getFiles().add(derivedFile);
                break;
            case "arraydatafile":
                ArrayDataNode arrayDataNode = (ArrayDataNode) node;
                uk.ac.ebi.subs.data.component.File arrayFile = new uk.ac.ebi.subs.data.component.File();
                arrayFile.setName(arrayDataNode.getNodeName());
                arrayFile.setType("array data");
                assayData.getFiles().add(arrayFile);
                break;
            case "scanname":
                ScanNode scanNode = (ScanNode)node;
                uk.ac.ebi.subs.data.component.File scanFile = new uk.ac.ebi.subs.data.component.File();
                for (Map.Entry<String,List<String>> commentsEntry : scanNode.comments.entrySet()){
                    for (String commentValue : commentsEntry.getValue()){
                        if (commentsEntry.getKey().equals("SUBMITTED_FILE_NAME")){
                            scanFile.setName(commentValue);
                        }

                        buildSingleAttribute(commentsEntry.getKey(),commentValue,assayData);
                    }
                }

                assayData.getFiles().add(scanFile);
            default:
                logger.debug("no assay data handler for " + node.getNodeType());
        }

        for (Node childNode : node.getChildNodes()) {
            traverseAssayDataNodes(assayData, childNode,depth + 1);
        }
    }

    AssayData assayData(Assay assay, Collection<AssayData> assayData) {
        AssayData ad = new AssayData();

        ad.setDomain(assay.getDomain());
        ad.setArchive(Archive.ArrayExpress);
        ad.setAssayRef((AssayRef) assay.asRef());
        ad.setAlias(assay.getAlias() + '~' + assayData.size() + 1);

        assayData.add(ad);
        return ad;
    }


    Sample sampleFromNode(SourceNode sourceNode, AbstractSubsRef<Study> studyRef) {
        Sample sample = new Sample();
        sample.setDomain(new Domain());
        sample.getDomain().setName(studyRef.getDomain());
        sample.setArchive(Archive.Usi);

        String nodeName = sourceNode.getNodeName();
        sample.setTitle(nodeName);
        sample.setAlias(studyRef.getAlias() + '~' + nodeName);

        for (CharacteristicsAttribute characteristic : sourceNode.characteristics) {

            if (characteristic.getAttributeType().equals("characteristics[organism]")) {
                sample.setTaxon(characteristic.getAttributeValue());

                List<Taxon>  taxa = taxonomyClient.getTaxonByScientificName(characteristic.getAttributeValue());
                for (Taxon taxon : taxa){
                    sample.setTaxonId(taxon.getTaxId());
                }

            } else {
                buildSingleAttribute(characteristic, sample);
            }
        }

        if (sourceNode.materialType != null) {
            Attribute a = new Attribute();

            a.setName("material type");
            a.setValue(sourceNode.materialType.getAttributeValue());

            if (sourceNode.materialType.termAccessionNumber != null) {
                Term t = new Term();
                t.setTermID(sourceNode.materialType.termAccessionNumber);
                t.setSourceName(sourceNode.materialType.termSourceREF);
                a.setTerm(t);
            }

            sample.getAttributes().add(a);
        }

        return sample;
    }

    void buildSingleAttribute(CharacteristicsAttribute characteristic, Attributes attributes) {
        Attribute a = new Attribute();

        a.setName(characteristic.type);
        a.setValue(characteristic.getAttributeValue());

        if (characteristic.unit != null) {
            a.setUnits(characteristic.unit.getAttributeValue());
        }

        if (characteristic.termAccessionNumber != null) {
            Term t = new Term();
            t.setTermID(characteristic.termAccessionNumber);
            t.setSourceName(characteristic.termSourceREF);
            a.setTerm(t);
        }

        attributes.getAttributes().add(a);
    }

    void buildAttributes(String attributeName, List<String> values, List<String> termIds, List<String> termRefs, Attributes submittable) {
        for (int i = 0; i < values.size(); i++) {
            Attribute a = new Attribute();
            a.setName(attributeName);
            a.setValue(values.get(i));

            Term t = new Term();

            t.setSourceName(stringPresent(termRefs, i));
            t.setTermID(stringPresent(termIds, i));


            if (t.getSourceName() != null || t.getTermID() != null) {
                a.setTerm(t);
            }

            submittable.getAttributes().add(a);
        }
    }


    void buildContacts(IDF idf, Contacts contacts) {
        for (int i = 0; i < idf.personEmail.size(); i++) {
            Contact contact = new Contact();

            contact.setLastName(stringPresent(idf.personLastName, i));
            contact.setFirstName(stringPresent(idf.personFirstName, i));
            contact.setMiddleInitials(stringPresent(idf.personMidInitials, i));
            contact.setEmail(stringPresent(idf.personEmail, i));
            contact.setPhone(stringPresent(idf.personPhone, i));
            contact.setAddress(stringPresent(idf.personAddress, i));
            contact.setAffiliation(stringPresent(idf.personAffiliation, i));

            String roles = stringPresent(idf.personRoles, i);
            if (roles != null) {
                contact.getRoles().addAll(Arrays.asList(roles.split(";")));
            }

            contacts.getContacts().add(contact);
        }
    }

    void buildSingleAttribute(String name, String value, Attributes attributes) {
        if (value == null) {
            return;
        }
        Attribute a = new Attribute();
        a.setName(name);
        a.setValue(value);
        attributes.getAttributes().add(a);
    }

    void buildProtocols(IDF idf, Protocols protocols) {

        for (int i = 0; i < idf.protocolName.size(); i++) {
            Protocol p = new Protocol();

            p.setName(stringPresent(idf.protocolName, i));
            p.setType(stringPresent(idf.protocolType, i));
            p.setDescription(stringPresent(idf.protocolDescription, i));

            buildSingleAttribute("hardware", stringPresent(idf.protocolHardware, i), p);
            buildSingleAttribute("software", stringPresent(idf.protocolSoftware, i), p);
            buildSingleAttribute("parameters", stringPresent(idf.protocolParameters, i), p);
            buildSingleAttribute("contact", stringPresent(idf.protocolContact, i), p);

            protocols.getProtocols().add(p);
        }
    }

    void buildPublications(IDF idf, Publications ations) {
        for (int i = 0; i < idf.publicationTitle.size(); i++) {
            Publication p = new Publication();

            p.setPubmedId(stringPresent(idf.pubMedId, i));
            p.setDoi(stringPresent(idf.publicationDOI, i));

            ations.getPublications().add(p);
        }
    }

    String stringPresent(List<String> list, int index) {
        if (index < list.size() && list.get(index) != null && !list.get(index).isEmpty()) {
            return list.get(index);
        } else {
            return null;
        }
    }


    Study createStudy(IDF idf) {
        Study study = new Study();

        study.setArchive(Archive.ArrayExpress);
        study.setAlias(idf.accession);

        study.setTitle(idf.investigationTitle);
        study.setDescription(idf.experimentDescription);

        Date publicReleaseDate;
        try {
            publicReleaseDate = sdf.parse(idf.publicReleaseDate);
        } catch (java.text.ParseException e) {
            try {
                publicReleaseDate = alternateSdf.parse(idf.publicReleaseDate);
            }
            catch(java.text.ParseException e2) {
                throw new RuntimeException("Could not parse date "+idf.publicReleaseDate+" with patterns yyyy-MM-dd or dd/MM/yyyy",e2);
            }

        }
        study.setReleaseDate(publicReleaseDate);

        buildPublications(idf, study);

        buildProtocols(idf, study);

        buildContacts(idf, study);

        buildAttributes("experimental design", idf.experimentalDesign, idf.experimentalDesignTermAccession, idf.experimentalDesignTermSourceREF, study);

        buildAttributes("experimental factor", idf.experimentalFactorName, idf.experimentalFactorTermAccession, idf.experimentalFactorTermSourceREF, study);

        buildAttributes("normalization type", idf.normalizationType, idf.normalizationTermAccession, idf.normalizationTermSourceREF, study);

        buildAttributes("quality control type", idf.qualityControlType, idf.qualityControlTermAccession, idf.qualityControlTermSourceREF, study);

        buildAttributes("replicate type", idf.replicateType, idf.replicateTermAccession, idf.replicateTermSourceREF, study);

        buildSingleAttribute("date of experiment", idf.dateOfExperiment, study);


        return study;
    }


}
