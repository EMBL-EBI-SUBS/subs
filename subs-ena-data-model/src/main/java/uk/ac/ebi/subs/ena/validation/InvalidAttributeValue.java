package uk.ac.ebi.subs.ena.validation;

import org.apache.commons.lang.StringUtils;
import uk.ac.ebi.subs.data.submittable.ENASubmittable;

/**
 * Created by neilg on 14/06/2017.
 */
public class InvalidAttributeValue extends AbstractENAValidationResult {
    static final String INVALID_VALUE_ERROR_MESSAGE = "Invalid value for attribute %s value must be one of %s.";
    String attributeValue;
    String [] allowedValues;

    public InvalidAttributeValue(ENASubmittable enaSubmittable, String attributeValue,String [] allowedValues) {
        super(enaSubmittable, String.format(INVALID_VALUE_ERROR_MESSAGE,allowedValues, StringUtils.join(allowedValues)));
        this.attributeValue = attributeValue;
        this.allowedValues = allowedValues;

    }
}
