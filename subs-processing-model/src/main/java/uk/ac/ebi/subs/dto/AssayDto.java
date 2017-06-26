package uk.ac.ebi.subs.dto;

import uk.ac.ebi.subs.data.component.ProtocolUse;
import uk.ac.ebi.subs.data.component.SampleUse;
import uk.ac.ebi.subs.data.component.StudyRef;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is responsible for transfering {@link uk.ac.ebi.subs.data.submittable.Assay} data over messaging.
 *
 * Created by rolando on 14/06/2017.
 * Modified by karoly on 26/06/2017.
 */
public class AssayDto extends BaseSubmittableDto {
    private StudyRef studyRef = new StudyRef();
    private List<SampleUse> sampleUses = new ArrayList<>();
    private List<ProtocolUse> protocolUses = new ArrayList<>();

    public AssayDto() {

    }
    public StudyRef getStudyRef() {
        return studyRef;
    }

    public void setStudyRef(StudyRef studyRef) {
        this.studyRef = studyRef;
    }

    public List<SampleUse> getSampleUses() {
        return sampleUses;
    }

    public void setSampleUses(List<SampleUse> sampleUses) {
        this.sampleUses = sampleUses;
    }

    public List<ProtocolUse> getProtocolUses() {
        return protocolUses;
    }

    public void setProtocolUses(List<ProtocolUse> protocolUses) {
        this.protocolUses = protocolUses;
    }
}
