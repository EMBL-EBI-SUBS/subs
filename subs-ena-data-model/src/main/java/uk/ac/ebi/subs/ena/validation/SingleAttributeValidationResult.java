package uk.ac.ebi.subs.ena.validation;

import uk.ac.ebi.subs.data.submittable.ENASubmittable;
import uk.ac.ebi.subs.validator.data.SingleValidationResult;
import uk.ac.ebi.subs.validator.data.ValidationAuthor;

/**
 * Created by neilg on 14/06/2017.
 */
public class SingleAttributeValidationResult extends AbstractENAValidationResult {
    static final String ERROR_MESSAGE = "Multiple values found for attribute %s.";
    String attributeName;

    public SingleAttributeValidationResult(ENASubmittable enaSubmittable, String attributeName) {
        super(enaSubmittable, String.format(ERROR_MESSAGE,attributeName));
        this.attributeName = attributeName;
    }

    public String getAttributeName() {
        return attributeName;
    }
}
