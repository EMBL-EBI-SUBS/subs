package uk.ac.ebi.subs.api.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import uk.ac.ebi.subs.validator.data.ValidationResult;
import uk.ac.ebi.subs.validator.repository.ValidationResultRepository;

import java.util.List;

/**
 * Created by karoly on 11/07/2017.
 */
@Service
public class ValidationResultServiceImpl implements ValidationResultService {

    private ValidationResultRepository validationResultRepository;

    @Autowired
    public ValidationResultServiceImpl(ValidationResultRepository validationResultRepository) {
        this.validationResultRepository = validationResultRepository;
    }

    @Override
    public List<ValidationResult> getValidationResultBySubmissionId(String submissionId) {
        Page<ValidationResult> results = validationResultRepository.findBySubmissionId(submissionId,
                new PageRequest(0, Integer.MAX_VALUE));

        return results.getContent();
    }
}
