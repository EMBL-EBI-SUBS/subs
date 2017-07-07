package uk.ac.ebi.subs.ena.validation;

import org.apache.commons.lang.StringUtils;
import uk.ac.ebi.subs.data.submittable.ENASubmittable;

/**
 * Created by neilg on 14/06/2017.
 */
public class InvalidAttributeValue extends AbstractENAValidationResult {
    static final String INVALID_VALUE_ERROR_MESSAGE = "Invalid value %s for attribute %s value must be one of %s.";

    String attributeValue;
    String[] allowedValues;

    public InvalidAttributeValue(ENASubmittable enaSubmittable, String invalidValue, String fieldName, String [] allowedValues) {
        super(
                enaSubmittable,
                String.format(INVALID_VALUE_ERROR_MESSAGE,
                        invalidValue,
                        fieldName,
                        StringUtils.join(allowedValues, ",")
                )
        );
        this.attributeValue = attributeValue;
        this.allowedValues = allowedValues;
    }

    public String getAttributeValue() {
        return attributeValue;
    }
}
