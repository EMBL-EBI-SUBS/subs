package uk.ac.ebi.subs.data.component;


import uk.ac.ebi.subs.data.submittable.Sample;

public class SampleRelationship {
    private String relationshipNature; // e.g. Child of
    private String relationshipTargetAccession; // e.g. SAMEA12345
    private String relationshipTargetUUID; // e.g. SAMEA12345

    public SampleRelationship(String relationshipNature, String relationshipTargetUUID) {
        this.relationshipNature = relationshipNature;
        this.relationshipTargetUUID = relationshipTargetUUID;
    }
    
    public SampleRelationship(String relationshipNature, String relationshipTargetAccession) {
        this.relationshipNature = relationshipNature;
        this.relationshipTargetAccession = relationshipTargetAccession;
    }
    
    public String getRelationshipNature() {
        return relationshipNature;
    }
    
    public String getRelationshipTargetAccession() {
        return relationshipTargetAccession;
    }
    
    public String getRelationshipTargeUUID() {
        return relationshipTargeUUID;
    }
}
