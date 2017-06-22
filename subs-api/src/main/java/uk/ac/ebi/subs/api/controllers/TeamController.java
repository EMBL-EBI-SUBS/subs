package uk.ac.ebi.subs.api.controllers;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.rest.webmvc.BasePathAwareController;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.Resource;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import uk.ac.ebi.subs.data.component.Team;
import uk.ac.ebi.subs.repository.repos.SubmissionRepository;
import uk.ac.ebi.subs.repository.model.Submission;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@RestController
public class TeamController {

    public TeamController(SubmissionRepository submissionRepository, PagedResourcesAssembler<Submission> pagedResourcesAssembler) {
        this.submissionRepository = submissionRepository;
        this.pagedResourcesAssembler = pagedResourcesAssembler;
    }

    private SubmissionRepository submissionRepository;
    private PagedResourcesAssembler<Submission> pagedResourcesAssembler;

    /**
     * including /api in this path is a workaround for this bug
     * https://github.com/spring-projects/spring-hateoas/issues/434
     *
     * The workaround given in the ticket does not handle templated paths
     * TODO remove /api and re-add @BasePathRestController once fixed in Spring Data REST
     */
     @RequestMapping("/api/teams/{teamName}")
    public Resource<Team> getTeam(@PathVariable String teamName) {
        //TODO this is a stub, we should make sure that the Teams are real and that the user is authorised
        Team d = new Team();
        d.setName(teamName);

        Page<Submission> subsPage = submissionRepository.findByTeamName(teamName, new PageRequest(0, 1));

        if (subsPage.getTotalElements() == 0) {
            throw new ResourceNotFoundException();
            //TODO temporary check until we have real team support
        }

        Resource<Team> resource = new Resource<>(d);

        resource.add(
                linkTo(
                        methodOn(this.getClass()).getTeam(
                                d.getName()
                        )
                ).withSelfRel()
        );

        return resource;
    }

}
