package uk.ac.ebi.subs.api.controllers;

import org.springframework.data.rest.webmvc.BasePathAwareController;
import org.springframework.data.rest.webmvc.support.RepositoryEntityLinks;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceAssembler;
import org.springframework.hateoas.Resources;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.ac.ebi.subs.api.services.ValidationResultService;
import uk.ac.ebi.subs.data.status.StatusDescription;
import uk.ac.ebi.subs.repository.model.Submission;
import uk.ac.ebi.subs.repository.repos.SubmissionRepository;

import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

/**
 * Created by karoly on 13/07/2017.
 */
@RestController
@BasePathAwareController
@RequestMapping("/submissions/{submissionId}")
public class SubmissionStatusController {

    private Map<String, StatusDescription> submissionStatusDescriptionMap;
    private SubmissionRepository submissionRepository;
    private ValidationResultService validationResultService;
    private ResourceAssembler<StatusDescription, Resource<StatusDescription>> submissionStatusResourceAssembler;
    private RepositoryEntityLinks repositoryEntityLinks;

    public SubmissionStatusController(Map<String, StatusDescription> submissionStatusDescriptionMap, SubmissionRepository submissionRepository, ValidationResultService validationResultService, ResourceAssembler<StatusDescription, Resource<StatusDescription>> submissionStatusResourceAssembler, RepositoryEntityLinks repositoryEntityLinks) {
        this.submissionStatusDescriptionMap = submissionStatusDescriptionMap;
        this.submissionRepository = submissionRepository;
        this.validationResultService = validationResultService;
        this.submissionStatusResourceAssembler = submissionStatusResourceAssembler;
        this.repositoryEntityLinks = repositoryEntityLinks;
    }

    @RequestMapping("/availableSubmissionStatuses")
    public Resources<Resource<StatusDescription>> availableSubmissionStatuses(@PathVariable String submissionId) {
        Submission currentSubmission = submissionRepository.findOne(submissionId);

        Collection<String> statusNames;

        if (validationResultService.isValidationFinishedAndPassed(currentSubmission.getSubmissionStatus().getId())) {
            StatusDescription statusDescription = submissionStatusDescriptionMap.get(currentSubmission.getSubmissionStatus().getStatus());

            statusNames = statusDescription.getUserTransitions();
        } else {
            statusNames = Collections.emptySet();
        }

        List<Resource<StatusDescription>> statusResources = statusNames
                .stream()
                .map(statusName -> submissionStatusDescriptionMap.get(statusName))
                .filter(Objects::nonNull)
                .map(statusDescription -> submissionStatusResourceAssembler.toResource(statusDescription))
                .collect(Collectors.toList());

        Resources<Resource<StatusDescription>> resources = new Resources<>(statusResources);

        resources.add(
            linkTo(
                methodOn(this.getClass())
                    .availableSubmissionStatuses(submissionId)
            ).withSelfRel()
        );

        resources.add(
            repositoryEntityLinks.linkToSingleResource(Submission.class,submissionId)
        );


        return resources;
    }
}
