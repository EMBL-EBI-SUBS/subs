package uk.ac.ebi.subs.stresstest;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.client.Traverson;

import java.net.URI;

public class ApiLinkDiscovery {


    public Link discoverNamedLink(URI uri, String... linkNames){
        Traverson traverson = new Traverson(uri, MediaTypes.HAL_JSON);

        Traverson.TraversalBuilder builder = traverson.follow(linkNames);

        return builder.asLink();
    }
}
