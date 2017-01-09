package uk.ac.ebi.subs.frontend.validators;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import uk.ac.ebi.subs.data.submittable.EgaDac;
import uk.ac.ebi.subs.repository.submittable.EgaDacRepository;

@Component
public class EgaDacValidator implements Validator {

    @Autowired
    private CoreSubmittableValidationHelper coreSubmittableValidationHelper;
    @Autowired
    private EgaDacRepository repository;


    @Override
    public boolean supports(Class<?> clazz) {
        return EgaDac.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object object, Errors errors) {
        EgaDac target = (EgaDac) object;
        coreSubmittableValidationHelper.validate(target, repository, errors);
    }
}
