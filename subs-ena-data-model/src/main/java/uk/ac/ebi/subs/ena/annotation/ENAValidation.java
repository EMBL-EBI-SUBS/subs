package uk.ac.ebi.subs.ena.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that is used to tag a class that extends ENASubmittable .
 * The fields marked with the ENAAttribute annotation will be serialised from the submittables attributes
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ENAValidation {
    ENAFieldAttribute [] value();
    ENAControlledValueAttribute [] enaControlledValueAttributes() default {};
}
