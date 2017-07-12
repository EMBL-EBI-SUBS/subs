package uk.ac.ebi.subs.ena.validation;

import uk.ac.ebi.subs.data.submittable.ENASubmittable;
import uk.ac.ebi.subs.validator.data.SingleValidationResult;
import uk.ac.ebi.subs.validator.data.ValidationAuthor;
import uk.ac.ebi.subs.validator.data.ValidationStatus;

/**
 * Created by neilg on 14/06/2017.
 */
public class AbstractENAValidationResult extends SingleValidationResult {
    ENASubmittable enaSubmittable;

    public AbstractENAValidationResult(ENASubmittable enaSubmittable, String message) {
        super(ValidationAuthor.Ena, enaSubmittable.getId().toString());
        this.enaSubmittable = enaSubmittable;
        setMessage(message);
        this.setValidationStatus(ValidationStatus.Error);
    }

    public ENASubmittable getEnaSubmittable() {
        return enaSubmittable;
    }

}