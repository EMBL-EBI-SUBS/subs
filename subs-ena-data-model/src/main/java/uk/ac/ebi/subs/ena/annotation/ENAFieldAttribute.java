package uk.ac.ebi.subs.ena.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
public @interface ENAFieldAttribute {

    static String NO_FIELD = "No_Field";

    /**
     * The attribute name to retrieve
     * @return
     */
    String attributeName();
    boolean required() default false;

    /**
     * If set to the default then the value is not copied into the field
     * @return
     */
    String fieldName() default NO_FIELD;

    /**
     * If this is set it will use another attributes value as the field name.
     * The attribute must have been previously validated
     * @return
     */
    String attributeFieldName() default NO_FIELD;

}
