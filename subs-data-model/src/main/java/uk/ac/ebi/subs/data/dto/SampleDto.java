package uk.ac.ebi.subs.data.dto;

import uk.ac.ebi.subs.data.component.SampleRelationship;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rolando on 14/06/2017.
 */
public class SampleDto extends BaseSubmittableDto {
    private List<SampleRelationship> sampleRelationships = new ArrayList<SampleRelationship>();
    private Long taxonId;
    private String taxon;

    public SampleDto() {
    }

    public List<SampleRelationship> getSampleRelationships() {
        return sampleRelationships;
    }

    public void setSampleRelationships(List<SampleRelationship> sampleRelationships) {
        this.sampleRelationships = sampleRelationships;
    }

    public Long getTaxonId() {
        return taxonId;
    }

    public void setTaxonId(Long taxonId) {
        this.taxonId = taxonId;
    }

    public String getTaxon() {
        return taxon;
    }

    public void setTaxon(String taxon) {
        this.taxon = taxon;
    }
}
