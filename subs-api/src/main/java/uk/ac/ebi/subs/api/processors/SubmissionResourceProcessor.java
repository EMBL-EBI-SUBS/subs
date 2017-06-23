package uk.ac.ebi.subs.api.processors;

import org.springframework.data.rest.webmvc.support.RepositoryEntityLinks;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import uk.ac.ebi.subs.api.controllers.ProcessingStatusController;
import uk.ac.ebi.subs.api.controllers.TeamController;
import uk.ac.ebi.subs.api.services.OperationControlService;
import uk.ac.ebi.subs.repository.model.ProcessingStatus;
import uk.ac.ebi.subs.repository.model.StoredSubmittable;
import uk.ac.ebi.subs.repository.model.Submission;
import uk.ac.ebi.subs.repository.repos.status.SubmissionStatusRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;


@Component
public class SubmissionResourceProcessor implements ResourceProcessor<Resource<Submission>> {


    public SubmissionResourceProcessor(
            SubmissionStatusRepository submissionStatusRepository,
            RepositoryEntityLinks repositoryEntityLinks,
            List<Class<? extends StoredSubmittable>> submittablesClassList,
            OperationControlService operationControlService,
            LinkHelper linkHelper,
            BasePathAwareLinks basePathAwareLinks
    ) {
        this.submissionStatusRepository = submissionStatusRepository;
        this.repositoryEntityLinks = repositoryEntityLinks;
        this.submittablesClassList = submittablesClassList;
        this.operationControlService = operationControlService;
        this.linkHelper = linkHelper;
        this.basePathAwareLinks = basePathAwareLinks;
    }

    private BasePathAwareLinks basePathAwareLinks;
    private SubmissionStatusRepository submissionStatusRepository;
    private RepositoryEntityLinks repositoryEntityLinks;
    private List<Class<? extends StoredSubmittable>> submittablesClassList;
    private OperationControlService operationControlService;
    private LinkHelper linkHelper;

    @Override
    public Resource<Submission> process(Resource<Submission> resource) {

        addTeamRel(resource);
        addContentsRels(resource);

        ifUpdateableAddLinks(resource);

        addStatusSummaryReport(resource);
        addTypeStatusSummaryReport(resource);

        addReceiptLink(resource);

        return resource;
    }

    private void addReceiptLink(Resource<Submission> resource) {
        Link searchLink = repositoryEntityLinks.linkToSearchResource(ProcessingStatus.class, "by-submission");
        Assert.notNull(searchLink);

        Map<String, String> params = new HashMap<>();
        params.put("submissionId", resource.getContent().getId());

        Link link = searchLink.expand(params).withRel("processingStatuses");

        resource.add(link);
    }

    private void addStatusSummaryReport(Resource<Submission> resource) {
        Link statusSummary =
                basePathAwareLinks.underBasePath(
                        linkTo(
                                methodOn(ProcessingStatusController.class)
                                        .summariseProcessingStatusForSubmission(resource.getContent().getId())
                        )
                ).withRel("processingStatusSummary");


        resource.add(statusSummary);
    }

    private void addTypeStatusSummaryReport(Resource<Submission> resource) {
        Link typeStatusSummary = basePathAwareLinks.underBasePath(
                linkTo(
                        methodOn(ProcessingStatusController.class)
                                .summariseTypeProcessingStatusForSubmission(resource.getContent().getId())
                )
        ).withRel("typeProcessingStatusSummary");


        resource.add(typeStatusSummary);
    }

    private void ifUpdateableAddLinks(Resource<Submission> submissionResource) {
        if (operationControlService.isUpdateable(submissionResource.getContent())) {

            linkHelper.addSubmittablesCreateLinks(submissionResource.getLinks());

            linkHelper.addSelfUpdateLink(submissionResource.getLinks(), submissionResource.getContent());
        }
    }

    private void addContentsRels(Resource<Submission> resource) {
        Map<String, String> expansionParams = new HashMap<>();
        expansionParams.put("submissionId", resource.getContent().getId());

        for (Class<? extends StoredSubmittable> submittableClass : submittablesClassList) {
            Link contentsLink = repositoryEntityLinks.linkToSearchResource(submittableClass, "by-submission");
            Link collectionLink = repositoryEntityLinks.linkToCollectionResource(submittableClass);

            Assert.notNull(contentsLink);
            Assert.notNull(collectionLink);


            resource.add(
                    contentsLink.expand(expansionParams).withRel(collectionLink.getRel())
            );

        }

    }

    private void addTeamRel(Resource<Submission> resource) {
        if (resource.getContent().getTeam() != null && resource.getContent().getTeam().getName() != null) {
            resource.add(
                    basePathAwareLinks.underBasePath(
                            linkTo(
                                    methodOn(TeamController.class)
                                            .getTeam(resource.getContent().getTeam().getName())
                            )
                    ).withRel("team")
            );
        }
    }


}
