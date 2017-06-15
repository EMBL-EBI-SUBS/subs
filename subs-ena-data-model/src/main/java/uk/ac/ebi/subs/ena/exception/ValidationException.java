package uk.ac.ebi.subs.ena.exception;

import uk.ac.ebi.subs.validator.data.ValidationResult;

/**
 * Created by neilg on 14/06/2017.
 *
 */
public class ValidationException extends RuntimeException {
    ValidationResult validationResult;

    public ValidationException(ValidationResult validationResult) {
        this.validationResult = validationResult;
    }

    public ValidationException(String message, ValidationResult validationResult) {
        super(message);
        this.validationResult = validationResult;
    }

    public ValidationException(String message, Throwable cause, ValidationResult validationResult) {
        super(message, cause);
        this.validationResult = validationResult;
    }

    public ValidationException(Throwable cause, ValidationResult validationResult) {
        super(cause);
        this.validationResult = validationResult;
    }

    public ValidationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, ValidationResult validationResult) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.validationResult = validationResult;
    }

    public ValidationResult getValidationResult() {
        return validationResult;
    }

}
