package uk.ac.ebi.subs.api.processors;

import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.stereotype.Component;
import uk.ac.ebi.subs.api.controllers.StatusDescriptionController;
import uk.ac.ebi.subs.repository.model.SubmissionStatus;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@Component
public class SubmissionStatusResourceProcessor implements ResourceProcessor<Resource<SubmissionStatus>> {

    private BasePathAwareLinks basePathAwareLinks;

    public SubmissionStatusResourceProcessor(BasePathAwareLinks basePathAwareLinks) {
        this.basePathAwareLinks = basePathAwareLinks;
    }

    @Override
    public Resource<SubmissionStatus> process(Resource<SubmissionStatus> resource) {

        addStatusDescriptionRel(resource);

        return resource;
    }

    private void addStatusDescriptionRel(Resource<SubmissionStatus> resource) {
        resource.add(
                basePathAwareLinks.underBasePath(
                        linkTo(
                                methodOn(StatusDescriptionController.class)
                                        .submissionStatus(resource.getContent().getStatus()))
                ).withRel("statusDescription")
        );
    }
}
