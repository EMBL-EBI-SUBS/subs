package uk.ac.ebi.subs.api.config;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import uk.ac.ebi.subs.api.services.SubmittableValidationDispatcher;

/**
 * Created by rolando on 20/06/2017.
 */

@Profile("SubmittableValidationDispatcherTest")
@Configuration
public class SubmittableValidationDispatcherTestConfig {
    @Bean
    public SubmittableValidationDispatcher submittableValidationDispatcher() {
        return Mockito.spy(SubmittableValidationDispatcher.class);
    }
}
