package uk.ac.ebi.subs.ena.validation;

import uk.ac.ebi.ena.sra.xml.RECEIPTDocument;
import uk.ac.ebi.subs.data.submittable.ENASubmittable;
import uk.ac.ebi.subs.validator.data.ValidationStatus;

/**
 * Created by neilg on 14/06/2017.
 */
public class AttributeRequiredValidationResult extends AbstractENAValidationResult {
    private static String MESSAGE = "Value for attribute %s is required.";
    String attributeName;

    public AttributeRequiredValidationResult(ENASubmittable enaSubmittable, String attributeName) {
        super(enaSubmittable, String.format(MESSAGE,attributeName));
        this.attributeName = attributeName;
        this.setValidationStatus(ValidationStatus.Error);
    }
}
