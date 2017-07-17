package uk.ac.ebi.subs.api.controllers;

import org.springframework.data.rest.webmvc.BasePathAwareController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.ac.ebi.subs.data.status.StatusDescription;
import uk.ac.ebi.subs.repository.model.Submission;
import uk.ac.ebi.subs.repository.model.SubmissionStatus;
import uk.ac.ebi.subs.repository.repos.SubmissionRepository;
import uk.ac.ebi.subs.repository.repos.status.SubmissionStatusRepository;

import java.util.Map;
import java.util.Set;

/**
 * Created by karoly on 13/07/2017.
 */
@RestController
@BasePathAwareController
@RequestMapping("/submissions/{submissionId}")
public class SubmissionStatusController {

    private Map<String, StatusDescription> submissionStatusDescriptionMap;
    private SubmissionRepository submissionRepository;

    public SubmissionStatusController(Map<String, StatusDescription> submissionStatusDescriptionMap, SubmissionRepository submissionRepository) {
        this.submissionStatusDescriptionMap = submissionStatusDescriptionMap;
        this.submissionRepository = submissionRepository;
    }

    @RequestMapping("/availableSubmissionStatuses")
    public Set<String> availableSubmissionStatuses(@PathVariable String submissionId) {
        Submission currentSubmission = submissionRepository.findOne(submissionId);

        StatusDescription statusDescription = submissionStatusDescriptionMap.get(currentSubmission.getSubmissionStatus().getStatus());

        return statusDescription.getUserTransitions();
    }
}
