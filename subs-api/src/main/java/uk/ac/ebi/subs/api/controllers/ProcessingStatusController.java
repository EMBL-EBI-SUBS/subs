package uk.ac.ebi.subs.api.controllers;

import org.springframework.data.rest.webmvc.BasePathAwareController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.ac.ebi.subs.repository.repos.status.ProcessingStatusRepository;

import java.util.Map;


@RestController
@BasePathAwareController
@RequestMapping("/submissions/{submissionId}")
public class ProcessingStatusController {

    private ProcessingStatusRepository processingStatusRepository;

    public ProcessingStatusController(ProcessingStatusRepository processingStatusRepository) {
        this.processingStatusRepository = processingStatusRepository;
    }

    @RequestMapping("/processingStatusSummaryCounts")
    public Map<String,Integer> summariseProcessingStatusForSubmission(@PathVariable String submissionId){

        return processingStatusRepository.summariseSubmissionStatus(submissionId);
    }

    @RequestMapping("/processingStatusSummaryTypeCounts")
    public Map<String,Map<String,Integer>> summariseTypeProcessingStatusForSubmission(@PathVariable String submissionId){

        return processingStatusRepository.summariseSubmissionStatusAndType(submissionId);

    }
}
