package uk.ac.ebi.subs.dta;

import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.subs.data.component.*;
import uk.ac.ebi.subs.data.submittable.Assay;
import uk.ac.ebi.subs.data.submittable.Sample;
import uk.ac.ebi.subs.data.submittable.Study;
import uk.ac.ebi.subs.data.submittable.Submittable;
import uk.ac.ebi.subs.dto.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by karoly on 26/06/2017.
 */
public class SubmittableDtoMapperTest {

    private SubmittableDtoMapper mapper;

    private static String ALIAS = "Alias";
    private static String TITLE = "Title";
    private static String DESCRIPTION = "Description";
    private static Archive ARCHIVE = Archive.Ena;
    private static Long TAXON_ID = 9606l;
    private static String TAXON = "homo sapiens";

    @Before
    public void setup() {
        mapper = new SubmittableDtoMapper();
    }

    @Test
    public void whenPassingASampleToTheMapperThenShouldGetASampleDTO() {
        String id = UUID.randomUUID().toString();

        Sample sample = createSample(id);
        SampleDto expectedSampleDto = createSampleDto(id);

        SampleDto actualSampleDto = mapper.toDto(sample);

        assertThat(actualSampleDto).isEqualToComparingFieldByField(expectedSampleDto);
    }

    @Test
    public void whenPassingAStudyToTheMapperThenShouldGetAStudyDTO() {
        String id = UUID.randomUUID().toString();

        Study study = createStudy(id);
        StudyDto expectedStudyDto = createStudyDTO(id);

        StudyDto actualStudyDto = mapper.toDto(study);

        assertThat(actualStudyDto).isEqualToComparingFieldByField(expectedStudyDto);
    }

    @Test
    public void whenPassingAnAssayToTheMapperThenShouldGetAnAssayDTO() {
        String id = UUID.randomUUID().toString();

        Assay assay = createAssay(id);
        AssayDto expectedAssayDto = createAssayDTO(id);

        AssayDto actualAssayDto = mapper.toDto(assay);

        assertThat(actualAssayDto).isEqualToComparingFieldByField(expectedAssayDto);
    }

    private Sample createSample(String id) {
        Sample sample = new Sample();
        sample = (Sample) setupSubmittable(sample, id);
        sample.setTaxonId(TAXON_ID);
        sample.setTaxon(TAXON);
        return sample;
    }

    private SampleDto createSampleDto(String id) {
        SampleDto sampleDto = new SampleDto();
        sampleDto = (SampleDto) setupSubmittableDTO(sampleDto, id);
        sampleDto.setTaxonId(TAXON_ID);
        sampleDto.setTaxon(TAXON);
        return sampleDto;
    }

    private Study createStudy(String id) {
        Study study = new Study();
        study = (Study) setupSubmittable(study, id);

        study.setPublications(createTestPublications());

        study.setContacts(createTestContacts());

        return study;
    }

    private StudyDto createStudyDTO(String id) {
        StudyDto studyDto = new StudyDto();
        studyDto = (StudyDto) setupSubmittableDTO(studyDto, id);

        studyDto.setPublications(createTestPublications());

        studyDto.setContacts(createTestContacts());

        return studyDto;
    }

    private Assay createAssay(String id) {
        Assay assay = new Assay();
        assay = (Assay) setupSubmittable(assay, id);

        assay.setStudyRef( createStudyRef());

        assay.setSampleUses(createSampleUses());

        return assay;
    }

    private AssayDto createAssayDTO(String id) {
        AssayDto assayDto = new AssayDto();
        assayDto = (AssayDto) setupSubmittableDTO(assayDto, id);

        assayDto.setStudyRef( createStudyRef());

        assayDto.setSampleUses(createSampleUses());

        return assayDto;
    }

    private StudyRef createStudyRef() {
        StudyRef studyRef = new StudyRef();
        setupBaseSubRef(studyRef);

        return studyRef;
    }

    private void setupBaseSubRef(AbstractSubsRef subRef) {
        subRef.setAccession("test accession");
        subRef.setArchive(ARCHIVE.name());
        subRef.setTeam(getTeam().getName());
        subRef.setAlias(ALIAS);
    }

    private SampleRef createSampleRef() {
        SampleRef sampleRef = new SampleRef();
        setupBaseSubRef(sampleRef);

        return sampleRef;
    }

    private List<SampleUse> createSampleUses() {
        List<SampleUse> sampleUses = new ArrayList<>();
        SampleUse sampleUse = new SampleUse( createSampleRef());
        sampleUses.add(sampleUse);

        return sampleUses;
    }

    private List<Contact> createTestContacts() {
        List<Contact> contacts = new ArrayList<>();
        Contact contact = new Contact();
        contact.setFirstName("firstname");
        contact.setLastName("lastname");
        contact.setAddress("test address");
        contacts.add(contact);
        return contacts;
    }

    private List<Publication> createTestPublications() {
        List<Publication> publications = new ArrayList<>();
        Publication publication = new Publication();
        publication.setDoi("Test doi");
        publication.setPubmedId("Test Pub med id");
        publications.add(publication);
        return publications;
    }

    private Submittable setupSubmittable(Submittable submittable, String id) {
        submittable.setId(id);
        submittable.setAlias(ALIAS);
        submittable.setTitle(TITLE);
        submittable.setDescription(DESCRIPTION);
        submittable.setArchive(ARCHIVE);
        submittable.setTeam(getTeam());

        return submittable;
    }

    private BaseSubmittableDto setupSubmittableDTO(BaseSubmittableDto baseSubmittableDto, String id) {
        baseSubmittableDto.setId(id);
        baseSubmittableDto.setAlias(ALIAS);
        baseSubmittableDto.setTitle(TITLE);
        baseSubmittableDto.setDescription(DESCRIPTION);
        baseSubmittableDto.setArchive(ARCHIVE);
        baseSubmittableDto.setTeam(getTeam());

        return baseSubmittableDto;
    }

    private Team getTeam() {
        Team team = new Team();
        team.setName("TeamName");

        return team;
    }
}
