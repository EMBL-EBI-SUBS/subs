package uk.ac.ebi.subs.ena.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that is used to tag a member of a class that extends ENASubmittable .
 * Used to copy values from the submittables attributes to the member value
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ENAFieldAtrribute {
    /*
    Maps to a field in a class
     */
    String fieldName();
    /*
    The
     */
    String fieldAttributeName();

    String [] allowedValues() default {};
}
