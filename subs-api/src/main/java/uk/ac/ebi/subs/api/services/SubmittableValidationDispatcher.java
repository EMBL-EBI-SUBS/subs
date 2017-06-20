package uk.ac.ebi.subs.api.services;

import uk.ac.ebi.subs.repository.model.Assay;
import uk.ac.ebi.subs.repository.model.Sample;
import uk.ac.ebi.subs.repository.model.Study;

/**
 * Created by rolando on 09/06/2017.
 */
public interface SubmittableValidationDispatcher {
     void validateCreate(Sample sample);
     void validateCreate(Study study);
     void validateCreate(Assay assay);

     void validateUpdate(Sample sample);
     void validateUpdate(Study study);
     void validateUpdate(Assay assay);

}
