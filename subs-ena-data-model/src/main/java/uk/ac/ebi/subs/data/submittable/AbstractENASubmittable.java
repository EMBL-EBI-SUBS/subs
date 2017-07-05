package uk.ac.ebi.subs.data.submittable;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import uk.ac.ebi.subs.data.component.Archive;
import uk.ac.ebi.subs.data.component.Attribute;
import uk.ac.ebi.subs.data.component.Team;
import uk.ac.ebi.subs.ena.annotation.ENAAttribute;
import uk.ac.ebi.subs.ena.annotation.ENAValidation;
import uk.ac.ebi.subs.ena.exception.AttributeException;
import uk.ac.ebi.subs.ena.validation.AttributeRequiredValidationResult;
import uk.ac.ebi.subs.ena.validation.InvalidAttributeValue;
import uk.ac.ebi.subs.ena.validation.SingleAttributeValidationResult;
import uk.ac.ebi.subs.validator.data.SingleValidationResult;
import uk.ac.ebi.subs.validator.data.ValidationAuthor;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Stream;

/**
 * Created by neilg on 03/03/2017.
 */
public abstract class AbstractENASubmittable<T extends BaseSubmittable> implements BaseSubmittableFactory<T>  {
    private static final String MULTIPLE_VALUES_ERROR_MESSAGE = "Multiple values found for attribute %s.";
    static final String ATTRIBUTE_VALUE_REQUIRED_ERROR_MESSAGE = "Value for attribute %s is required.";
    private static final String INVALID_VALUE_ERROR_MESSAGE = "Invalid value for attribute %s value must be one of %s.";

    private Submittable baseSubmittable;
    private List<SingleValidationResult> validationResultList = new ArrayList<>();

    public AbstractENASubmittable(Submittable baseSubmittable) throws IllegalAccessException {
        setBaseSubmittable(baseSubmittable);
    }

    public AbstractENASubmittable() {
        this.baseSubmittable = createNewSubmittable();
    }

    public void setBaseSubmittable(Submittable baseSubmittable) throws IllegalAccessException {
        this.baseSubmittable = baseSubmittable;
        serialiseAttributes();
    }


    Optional<Attribute> getExistingAttribute(String attributeName, boolean allowMultiple) {
        List<Attribute> attributeList = getAttributes();
        if (attributeList == null)
            attributeList = new ArrayList<>();
        if (!allowMultiple && attributeList.stream().filter(attribute -> attribute.getName().equalsIgnoreCase(attributeName)).count() > 1)
            throw new IllegalArgumentException(String.format(MULTIPLE_VALUES_ERROR_MESSAGE,attributeName));
        return attributeList.stream().filter(attribute -> attribute.getName().equalsIgnoreCase(attributeName)).findFirst();
    }

    Stream<Attribute> getExistingAttribute(String attributeName) {
        return getAttributes().stream().filter(attribute -> attribute.getName().equalsIgnoreCase(attributeName));
    }

    private int getAttributeCount(String attributeName) {
        final List<Attribute> attributeList = getAttributes();
        if (attributeList == null) {
            return 0;
        } else {
            return (int)attributeList.stream().filter(attribute -> attribute.getName().equalsIgnoreCase(attributeName)).count();
        }
    }

    void deleteAttribute(Attribute attribute) {
        getAttributes().remove(attribute);
    }

    public void serialiseAttributes () throws IllegalAccessException {
        if (this.getClass().isAnnotationPresent(ENAValidation.class)) {
            if (getId() == null )
                setId(UUID.randomUUID().toString());
            serialiseFields(this.getClass(), this);
        }
    }

    private void serialiseFields(Class<?> aClass, Object obj) throws IllegalAccessException {
        final Field[] fields = aClass.getDeclaredFields();

        for (Field field : fields ) {
            for (ENAAttribute annotation : field.getAnnotationsByType(ENAAttribute.class)) {
                annotation.annotationType();
                int attributeCount = getAttributeCount(annotation.name());
                if (attributeCount == 1) {
                    final Optional<Attribute> existingAttribute = getExistingAttribute(annotation.name(),false);
                    if (annotation.allowedValues().length > 0) {
                        final String value = existingAttribute.get().getValue().toUpperCase();
                        if ( !Arrays.stream(annotation.allowedValues()).anyMatch(value::equalsIgnoreCase) && annotation.required()) {
                            validationResultList.add(new InvalidAttributeValue(this,value,annotation));
                        }
                    }
                    field.set(obj,existingAttribute.get().getValue());
                    deleteAttribute(existingAttribute.get());
                } else if (attributeCount > 1) {
                    validationResultList.add(new SingleAttributeValidationResult(this,annotation.name()));
                } else if (attributeCount == 0 && annotation.required()) {
                    validationResultList.add(new AttributeRequiredValidationResult(this,annotation.name()));
                }
            }
            if (field.getType().isMemberClass()) {
                serialiseFields(field.getType(),field.get(obj));
            }

        }
    }

    public void deSerialiseAttributes () throws IllegalAccessException {
        deSerialiseFields(this.getClass(), this);
    }

    private void deSerialiseFields (Class<?> aClass, Object obj) throws IllegalAccessException {
        final Field[] fields = aClass.getDeclaredFields();
        for (Field field : fields ) {
            if (field.isAnnotationPresent(ENAAttribute.class)) {
                final ENAAttribute annotation = field.getAnnotation(ENAAttribute.class);
                final Object o = field.get(obj);
                if ( o != null ) {
                    Attribute attribute = new Attribute();
                    attribute.setName(annotation.name());
                    attribute.setValue(o.toString());
                    getAttributes().add(attribute);
                }
            } else if (field.getType().isMemberClass()) {
                deSerialiseFields(field.getType(),field.get(obj));
            }

        }
    }


    @Override
    public String getId() {
        return baseSubmittable.getId();
    }

    @Override
    public void setId(String id) {
        baseSubmittable.setId(id);
    }

    @Override
    public String getAccession() {
        return baseSubmittable.getAccession();
    }

    @Override
    public void setAccession(String accession) {
        baseSubmittable.setAccession(accession);
    }

    @Override
    public String getAlias() {
        return baseSubmittable.getAlias();
    }

    @Override
    public void setAlias(String alias) {
        baseSubmittable.setAlias(alias);
    }

    @Override
    public Team getTeam() {
        return baseSubmittable.getTeam();
    }

    @Override
    public void setTeam(Team team) {
        baseSubmittable.setTeam(team);
    }

    @Override
    public String getTitle() {
        return baseSubmittable.getTitle();
    }

    @Override
    public void setTitle(String title) {
        baseSubmittable.setTitle(title);
    }

    @Override
    public String getDescription() {
        return baseSubmittable.getDescription();
    }

    @Override
    public void setDescription(String description) {
        baseSubmittable.setDescription(description);
    }

    /**
     * Return null for a empty list to prevent moxy from creating an empty attributes element as the schema doesn't allow this
     */
    @Override
    public List<Attribute> getAttributes() {
        return baseSubmittable.getAttributes();
    }

    @Override
    public void setAttributes(List<Attribute> attributes) {
        baseSubmittable.setAttributes(attributes);
    }

    @Override
    public Archive getArchive() {
        return baseSubmittable.getArchive();
    }

    @Override
    public void setArchive(Archive archive) {
        baseSubmittable.setArchive(archive);
    }

    @Override
    public boolean isAccessioned() {
        return baseSubmittable.isAccessioned();
    }

    public String getTeamName() {
        Team team = getTeam();
        if (team != null)
            return team.getName();
        else return null;
    }

    //@Override
    public void setTeamName(String teamName) {
        Team team = new Team();
        team.setName(teamName);
        setTeam(team);
    }

    @Override
    public T getBaseObject() {
        return (T)baseSubmittable;
    }

    @Override
    public List<Attribute> getAttributesXML() {
        if (baseSubmittable.getAttributes().isEmpty()) {
            return null;
        } else {
            return baseSubmittable.getAttributes();
        }
    }

    @Override
    public void setAttributesXML(List<Attribute> attributeList) {
        baseSubmittable.setAttributes(attributeList);
    }

    public List<SingleValidationResult> getValidationResultList() {
        return validationResultList;
    }

    @Override
    public boolean isValid() {
        return validationResultList.isEmpty();
    }
}
