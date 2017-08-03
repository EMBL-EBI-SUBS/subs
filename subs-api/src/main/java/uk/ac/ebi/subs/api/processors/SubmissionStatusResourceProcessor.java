package uk.ac.ebi.subs.api.processors;

import org.springframework.data.rest.webmvc.support.RepositoryEntityLinks;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import uk.ac.ebi.subs.api.controllers.StatusDescriptionController;
import uk.ac.ebi.subs.api.controllers.SubmissionStatusController;
import uk.ac.ebi.subs.api.services.ValidationResultService;
import uk.ac.ebi.subs.data.status.SubmissionStatusEnum;
import uk.ac.ebi.subs.repository.model.Submission;
import uk.ac.ebi.subs.repository.model.SubmissionStatus;
import uk.ac.ebi.subs.repository.repos.SubmissionRepository;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@Component
public class SubmissionStatusResourceProcessor implements ResourceProcessor<Resource<SubmissionStatus>> {

    private ValidationResultService validationResultService;
    private RepositoryEntityLinks repositoryEntityLinks;
    private SubmissionRepository submissionRepository;

    public SubmissionStatusResourceProcessor(ValidationResultService validationResultService,
                                             RepositoryEntityLinks repositoryEntityLinks, SubmissionRepository submissionRepository) {
        this.validationResultService = validationResultService;
        this.repositoryEntityLinks = repositoryEntityLinks;
        this.submissionRepository = submissionRepository;
    }

    @Override
    public Resource<SubmissionStatus> process(Resource<SubmissionStatus> resource) {

        addStatusDescriptionRel(resource);

        addStatusUpdateRel(resource);

        addAvailableStatuses(resource);

        return resource;
    }

    private void addStatusDescriptionRel(Resource<SubmissionStatus> resource) {
        resource.add(
                linkTo(
                        methodOn(StatusDescriptionController.class)
                                .submissionStatus(resource.getContent().getStatus())
                ).withRel("statusDescription")
        );
    }

    private void addStatusUpdateRel(Resource<SubmissionStatus> submissionStatusResource) {
        SubmissionStatus submissionStatus = submissionStatusResource.getContent();

        if (submissionStatus.getStatus().equals(SubmissionStatusEnum.Draft.name()) && validationResultService.isValidationFinishedAndPassed(submissionStatus.getId())) {
            Link submissionStatusResourceLink = repositoryEntityLinks.linkToSingleResource(submissionStatus).expand();

            Assert.notNull(submissionStatusResourceLink);

            Link updateLink = submissionStatusResourceLink.withRel("self" + LinkHelper.UPDATE_REL_SUFFIX);
            submissionStatusResource.add(updateLink);
        }
    }

    private void addAvailableStatuses(Resource<SubmissionStatus> submissionStatusResource) {
        SubmissionStatus submissionStatus = submissionStatusResource.getContent();
        Submission submission = submissionRepository.findBySubmissionStatusId(submissionStatus.getId());

        if (validationResultService.isValidationFinishedAndPassed(submissionStatus.getId())) {
            submissionStatusResource.add(
                    linkTo(
                            methodOn(SubmissionStatusController.class)
                                    .availableSubmissionStatuses(submission.getId())
                    ).withRel("availableStatuses")
            );
        }
    }
}
