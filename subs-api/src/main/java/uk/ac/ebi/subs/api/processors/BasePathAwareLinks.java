package uk.ac.ebi.subs.api.processors;

import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.support.BaseUriLinkBuilder;
import org.springframework.hateoas.LinkBuilder;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.stereotype.Service;

import javax.servlet.ServletContext;
import java.net.URI;

@Service
/**
 * Workaround for https://github.com/spring-projects/spring-hateoas/issues/434
 *
 * Beware - does not work with tempated links
 */
public class BasePathAwareLinks {

    private final URI contextBaseURI;
    private final URI restBaseURI;

    public BasePathAwareLinks(ServletContext servletContext, RepositoryRestConfiguration config) {
        contextBaseURI = URI.create(servletContext.getContextPath());
        restBaseURI = config.getBasePath();
    }

    public LinkBuilder underBasePath(ControllerLinkBuilder linkBuilder) {
        return BaseUriLinkBuilder.create(contextBaseURI)
                .slash(restBaseURI)
                .slash(contextBaseURI.relativize(URI.create(linkBuilder.toUri().getPath())));
    }
}
