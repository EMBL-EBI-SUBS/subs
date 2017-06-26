package uk.ac.ebi.subs.dto;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import uk.ac.ebi.subs.data.submittable.Assay;
import uk.ac.ebi.subs.data.submittable.Sample;
import uk.ac.ebi.subs.data.submittable.Study;

/**
 * This class is responsible for mapping each entities to their data transfer object pair.
 *
 * Created by rolando on 14/06/2017.
 * Modified by karoly on 26/06/2017.
 */
@Component
public class SubmittableDtoMapper {
    private ObjectMapper mapper;

    public SubmittableDtoMapper(){
        mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    // Submitable to DTO

    public SampleDto toDto(Sample sample) {
        return mapper.convertValue(sample, SampleDto.class);
    }

    public StudyDto toDto(Study study) { return mapper.convertValue(study, StudyDto.class); }

    public AssayDto toDto(Assay assay) { return mapper.convertValue(assay, AssayDto.class); }

    // DTO to submittable

    public Sample fromDto(SampleDto sampleDto) { return mapper.convertValue(sampleDto, Sample.class); }

    public Study fromDto(StudyDto studyDto) { return mapper.convertValue(studyDto, Study.class); }

    public Assay fromDto(AssayDto assayDto) { return mapper.convertValue(assayDto, Assay.class); }

}
