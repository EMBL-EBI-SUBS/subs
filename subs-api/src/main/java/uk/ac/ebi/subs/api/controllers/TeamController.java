package uk.ac.ebi.subs.api.controllers;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.BasePathAwareController;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.security.access.method.P;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import uk.ac.ebi.subs.data.component.Team;
import uk.ac.ebi.subs.repository.repos.SubmissionRepository;
import uk.ac.ebi.subs.repository.model.Submission;
import uk.ac.ebi.subs.repository.repos.SubmissionRepositoryCustom;
import uk.ac.ebi.subs.repository.security.PreAuthorizeParamTeamName;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@RestController
@BasePathAwareController
public class TeamController {

    public TeamController(SubmissionRepository submissionRepository,
                          SubmissionRepositoryCustom submissionRepositoryCustom,
                          PagedResourcesAssembler<Submission> pagedResourcesAssembler,
                          PagedResourcesAssembler<Team> teamPagedResourcesAssembler) {
        this.submissionRepository = submissionRepository;
        this.pagedResourcesAssembler = pagedResourcesAssembler;
        this.teamPagedResourcesAssembler = teamPagedResourcesAssembler;
        this.submissionRepositoryCustom = submissionRepositoryCustom;
    }

    private SubmissionRepository submissionRepository;
    private PagedResourcesAssembler<Submission> pagedResourcesAssembler;
    private PagedResourcesAssembler<Team> teamPagedResourcesAssembler;
    private SubmissionRepositoryCustom submissionRepositoryCustom;

    @RequestMapping("/teams")
    public PagedResources<Resource<Team>> getTeams(Pageable pageable){
        Page<Team> teams = submissionRepositoryCustom.distinctTeams(pageable);
        return teamPagedResourcesAssembler.toResource(teams);
    }


    @RequestMapping("/teams/{teamName}")
    @PreAuthorizeParamTeamName
    public Resource<Team> getTeam(@PathVariable @P("teamName") String teamName) {
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
