package uk.ac.ebi.subs.data.submittable;

import uk.ac.ebi.subs.validator.data.SingleValidationResult;

import java.util.List;

/**
 * Created by neilg on 05/03/2017.
 */
public interface BaseSubmittableFactory<T extends Submittable> extends ENASubmittable<T> {
    void setBaseSubmittable(Submittable submittable) throws IllegalAccessException;
    List<SingleValidationResult> getValidationResultList();
    boolean isValid ();
    Submittable createNewSubmittable();

    static ENASubmittable create (Class<? extends BaseSubmittableFactory> clasz,
                                  Submittable submittable) throws IllegalArgumentException, IllegalAccessException, InstantiationException {
        BaseSubmittableFactory baseSubmittableFactory = clasz.newInstance();
        baseSubmittableFactory.setBaseSubmittable(submittable);
        return baseSubmittableFactory;
    }

    static ENASubmittable create (Class<? extends BaseSubmittableFactory> clasz) throws IllegalArgumentException, IllegalAccessException, InstantiationException {
        BaseSubmittableFactory baseSubmittableFactory = clasz.newInstance();
        return baseSubmittableFactory;
    }





}
