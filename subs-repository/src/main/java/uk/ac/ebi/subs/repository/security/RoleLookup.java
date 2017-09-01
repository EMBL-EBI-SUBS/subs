package uk.ac.ebi.subs.repository.security;

import org.springframework.stereotype.Component;

@Component
public class RoleLookup {
    private static final String ADMIN_USER_DOMAIN_NAME = "aap.admin";


    public String adminRole() {
        return ADMIN_USER_DOMAIN_NAME;
    }


}
