package uk.ac.ebi.subs.api.processors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.rest.webmvc.support.RepositoryEntityLinks;
import org.springframework.hateoas.Identifiable;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import uk.ac.ebi.subs.repository.model.StoredSubmittable;

import java.util.Collection;
import java.util.List;

@Component
public class LinkHelper {

    private static final Logger logger = LoggerFactory.getLogger(LinkHelper.class);
    private List<Class<? extends StoredSubmittable>> submittablesClassList;

    static final String CREATE_REL_SUFFIX = ":create";
    static final String SEARCH_REL_SUFFIX = ":search";
    static final String UPDATE_REL_SUFFIX = ":update";
    static final String DELETE_REL_SUFFIX = ":delete";

    private RepositoryEntityLinks repositoryEntityLinks;

    public List<Class<? extends StoredSubmittable>> getSubmittablesClassList() {
        return submittablesClassList;
    }

    public LinkHelper(List<Class<? extends StoredSubmittable>> submittablesClassList, RepositoryEntityLinks repositoryEntityLinks, BasePathAwareLinks basePathAwareLinks) {
        this.submittablesClassList = submittablesClassList;
        this.repositoryEntityLinks = repositoryEntityLinks;

    }

    public void addSubmittablesSearchLinks(Collection<Link> links){
        for (Class type : submittablesClassList){
            this.addSearchLink(links,type);
        }
    }

    public void addSubmittablesCreateLinks(Collection<Link> links){
        for (Class type : submittablesClassList){
            this.addCreateLink(links,type);
        }
    }

    public void addSelfUpdateLink(Collection<Link> links, Identifiable<?> identifiable){
        Link singleResourceLink = repositoryEntityLinks.linkToSingleResource(identifiable).expand();

        Assert.notNull(singleResourceLink);

        Link updateLink = singleResourceLink.withRel( "self" + UPDATE_REL_SUFFIX );

        links.add(updateLink);

        Link deleteLink = singleResourceLink.withRel( "self" + DELETE_REL_SUFFIX );

        links.add(deleteLink);
    }



    public void addCreateLink(Collection<Link> links, Class type) {
        Link collectionLink = repositoryEntityLinks.linkToCollectionResource(type).expand();

        String relBase = collectionLink.getRel();

        links.add(collectionLink.withRel(relBase + CREATE_REL_SUFFIX));
    }

    public void addSearchLink(Collection<Link> links, Class type) {
                Link collectionLink = repositoryEntityLinks.linkToCollectionResource(type).expand();

        String relBase = collectionLink.getRel();


        Links searchLinks = repositoryEntityLinks.linksToSearchResources(type);

        if (searchLinks == null || searchLinks.isEmpty()) {
            logger.debug("No search links found for class {}", type);
        } else {
            logger.debug("Search links found for class {}: {} ", type, searchLinks);

            String href = collectionLink.getHref() + "/search";
            String rel = relBase + SEARCH_REL_SUFFIX;
            Link searchesLink = new Link(href, rel);

            links.add(searchesLink);

        }
    }



}
