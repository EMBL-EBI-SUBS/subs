package uk.ac.ebi.subs.api.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.BasePathAwareController;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceAssembler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.ac.ebi.subs.data.status.StatusDescription;

import java.util.List;
import java.util.Optional;

/**
 * including /api in this path is a workaround for this bug
 * https://github.com/spring-projects/spring-hateoas/issues/434
 *
 * The workaround given in the ticket does not handle templated paths
 * TODO remove /api and re-add @BasePathRestController once fixed in Spring Data REST
 */
@RestController
@RequestMapping("/api/statusDescriptions")
public class StatusDescriptionController {


    public StatusDescriptionController(List<StatusDescription> releaseStatuses, List<StatusDescription> processingStatuses, List<StatusDescription> submissionStatuses, PagedResourcesAssembler pagedResourcesAssembler, ResourceAssembler<StatusDescription, Resource<StatusDescription>> processingStatusResourceAssembler, ResourceAssembler<StatusDescription, Resource<StatusDescription>> releaseStatusResourceAssembler, ResourceAssembler<StatusDescription, Resource<StatusDescription>> submissionStatusResourceAssembler) {
        this.releaseStatuses = releaseStatuses;
        this.processingStatuses = processingStatuses;
        this.submissionStatuses = submissionStatuses;
        this.pagedResourcesAssembler = pagedResourcesAssembler;
        this.processingStatusResourceAssembler = processingStatusResourceAssembler;
        this.releaseStatusResourceAssembler = releaseStatusResourceAssembler;
        this.submissionStatusResourceAssembler = submissionStatusResourceAssembler;
    }

    private List<StatusDescription> releaseStatuses;
    private List<StatusDescription> processingStatuses;
    private List<StatusDescription> submissionStatuses;
    private PagedResourcesAssembler pagedResourcesAssembler;
    private ResourceAssembler<StatusDescription, Resource<StatusDescription>> processingStatusResourceAssembler;
    private ResourceAssembler<StatusDescription, Resource<StatusDescription>> releaseStatusResourceAssembler;
    private ResourceAssembler<StatusDescription, Resource<StatusDescription>> submissionStatusResourceAssembler;

    @RequestMapping("/processingStatuses")
    public PagedResources<Resource<StatusDescription>> allProcessingStatus(Pageable pageable) {
        Page<StatusDescription> page = new PageImpl<StatusDescription>(processingStatuses, pageable, processingStatuses.size());

        return pagedResourcesAssembler.toResource(page, processingStatusResourceAssembler);
    }

    @RequestMapping("/processingStatuses/{status}")
    public Resource<StatusDescription> processingStatus(@PathVariable String status) {
        Optional<StatusDescription> optionalStatus = processingStatuses.stream().filter(s -> s.getStatusName().equals(status))
                .findFirst();

        if (optionalStatus.isPresent()) {
            return processingStatusResourceAssembler.toResource(optionalStatus.get());
        } else {
            throw new ResourceNotFoundException();
        }
    }

    @RequestMapping("/releaseStatuses")
    public PagedResources<Resource<StatusDescription>> allReleaseStatus(Pageable pageable) {
        Page<StatusDescription> page = new PageImpl<StatusDescription>(releaseStatuses, pageable, releaseStatuses.size());

        return pagedResourcesAssembler.toResource(page, releaseStatusResourceAssembler);
    }

    @RequestMapping("/releaseStatuses/{status}")
    public Resource<StatusDescription> releaseStatus(@PathVariable String status) {
        Optional<StatusDescription> optionalStatus = releaseStatuses.stream().filter(s -> s.getStatusName().equals(status))
                .findFirst();

        if (optionalStatus.isPresent()) {
            return releaseStatusResourceAssembler.toResource(optionalStatus.get());
        } else {
            throw new ResourceNotFoundException();
        }
    }

    @RequestMapping("/submissionStatuses")
    public PagedResources<Resource<StatusDescription>> allSubmissionStatus(Pageable pageable) {
        Page<StatusDescription> page = new PageImpl<StatusDescription>(submissionStatuses, pageable, submissionStatuses.size());

        return pagedResourcesAssembler.toResource(page, submissionStatusResourceAssembler);
    }

    @RequestMapping("submissionStatuses/{status}")
    public Resource<StatusDescription> submissionStatus(@PathVariable String status) {
        Optional<StatusDescription> optionalStatus = submissionStatuses.stream().filter(s -> s.getStatusName().equals(status))
                .findFirst();

        if (optionalStatus.isPresent()) {
            return submissionStatusResourceAssembler.toResource(optionalStatus.get());
        } else {
            throw new ResourceNotFoundException();
        }
    }
}
