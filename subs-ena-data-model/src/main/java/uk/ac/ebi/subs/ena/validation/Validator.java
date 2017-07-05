package uk.ac.ebi.subs.ena.validation;

import uk.ac.ebi.subs.validator.data.SingleValidationResult;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Created by neilg on 04/07/2017.
 */
public interface Validator  {
    SingleValidationResult validate (String value);
}
