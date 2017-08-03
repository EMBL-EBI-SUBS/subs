package uk.ac.ebi.subs.repository;

import uk.ac.ebi.subs.data.component.AbstractSubsRef;

import uk.ac.ebi.subs.data.submittable.Submittable;
import uk.ac.ebi.subs.repository.model.StoredSubmittable;

import java.util.Collection;
import java.util.Set;

/**
 * Created by rolando on 23/05/2017.
 */
public interface RefLookupService {

    <T extends  AbstractSubsRef> StoredSubmittable lookupRef(T ref);

    <T extends  AbstractSubsRef> Set<? extends StoredSubmittable> lookupRefs(Collection<T> refs);
}
