package uk.ac.ebi.subs.repository.security;


import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@PostAuthorize("returnObject == null or hasAnyRole(@roleLookup.adminRole(),@teamNameExtractor.processingStatusTeam(returnObject))")
public @interface PostAuthorizeProcessingStatusTeamName {
}
