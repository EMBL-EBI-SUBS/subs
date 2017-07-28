package uk.ac.ebi.subs.api.processors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceAssembler;
import uk.ac.ebi.subs.api.controllers.StatusDescriptionController;
import uk.ac.ebi.subs.data.status.StatusDescription;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@Configuration
public class StatusResourceAssemblerConfig {

    @Bean
    public ResourceAssembler<StatusDescription, Resource<StatusDescription>> submissionStatusResourceAssembler() {
        return entity -> {
            Resource<StatusDescription> res = new Resource<StatusDescription>(entity);

            res.add(
                    linkTo(
                            methodOn(StatusDescriptionController.class).submissionStatus(entity.getStatusName()
                            )
                    ).withSelfRel()
            );

            return res;
        };
    }

    @Bean
    public ResourceAssembler<StatusDescription, Resource<StatusDescription>> processingStatusResourceAssembler() {
        return entity -> {
            Resource<StatusDescription> res = new Resource<StatusDescription>(entity);

            res.add(
                    linkTo(
                            methodOn(StatusDescriptionController.class).processingStatus(entity.getStatusName())
                    ).withSelfRel()
            );

            return res;
        };
    }

    @Bean
    public ResourceAssembler<StatusDescription, Resource<StatusDescription>> releaseStatusResourceAssembler() {
        return entity -> {
            Resource<StatusDescription> res = new Resource<StatusDescription>(entity);

            res.add(
                    linkTo(
                            methodOn(StatusDescriptionController.class).releaseStatus(entity.getStatusName())
                    ).withSelfRel()
            );

            return res;
        };
    }


}
