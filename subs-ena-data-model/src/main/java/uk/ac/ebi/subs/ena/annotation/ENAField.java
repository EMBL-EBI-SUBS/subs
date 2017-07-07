package uk.ac.ebi.subs.ena.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by neilg on 06/07/2017.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.FIELD})
public @interface ENAField {
    String TYPE_FIELD_NAME = "Type_Field_Name";
    String [] values() default {};
    String fieldName() default TYPE_FIELD_NAME;
    boolean copyValues() default true;
}
