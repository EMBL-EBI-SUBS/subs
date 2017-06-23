package uk.ac.ebi.subs.api.processors;

import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.stereotype.Component;
import uk.ac.ebi.subs.api.controllers.StatusDescriptionController;
import uk.ac.ebi.subs.repository.model.ProcessingStatus;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@Component
public class ProcessingStatusResourceProcessor implements ResourceProcessor<Resource<ProcessingStatus>> {

    private BasePathAwareLinks basePathAwareLinks;

    public ProcessingStatusResourceProcessor(BasePathAwareLinks basePathAwareLinks) {
        this.basePathAwareLinks = basePathAwareLinks;
    }

    @Override
    public Resource<ProcessingStatus> process(Resource<ProcessingStatus> resource) {

        addStatusDescriptionRel(resource);

        redactIds(resource);

        return resource;
    }

    private void redactIds(Resource<ProcessingStatus> resource) {
        resource.getContent().setSubmissionId(null);
        resource.getContent().setSubmittableId(null);
    }

    private void addStatusDescriptionRel(Resource<ProcessingStatus> resource) {
        resource.add(
                basePathAwareLinks.underBasePath(
                        linkTo(
                                methodOn(StatusDescriptionController.class)
                                        .processingStatus(resource.getContent().getStatus()))
                ).withRel("statusDescription")
        );
    }
}
