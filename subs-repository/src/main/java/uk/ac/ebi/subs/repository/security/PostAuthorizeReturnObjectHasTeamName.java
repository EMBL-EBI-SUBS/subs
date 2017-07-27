package uk.ac.ebi.subs.repository.security;

import org.springframework.security.access.prepost.PostAuthorize;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@PostAuthorize("returnObject == null or hasAnyRole(@roleLookup.adminRole(),returnObject.team.name)")
public @interface PostAuthorizeReturnObjectHasTeamName {}
