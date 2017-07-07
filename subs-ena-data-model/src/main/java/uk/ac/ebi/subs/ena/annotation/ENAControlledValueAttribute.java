package uk.ac.ebi.subs.ena.annotation;

import java.lang.annotation.*;

/**
 * Created by neilg on 06/07/2017.
 */

@Retention(RetentionPolicy.RUNTIME)
public @interface ENAControlledValueAttribute {
    static String NO_FIELD = "No_Field";

    /**
     * Must match up to an fieldName is an ENAFieldAttribute
     * @return
     */
    String attributeName() ;

    /**
     * Field name to copy the value, if not defined will not be copied over
     * @return
     */
    String fieldName() default NO_FIELD;
    String [] allowedValues() default {};

}
