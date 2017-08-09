package uk.ac.ebi.subs.api.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfiguration;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import uk.ac.ebi.tsc.aap.client.security.StatelessAuthenticationEntryPoint;
import uk.ac.ebi.tsc.aap.client.security.StatelessAuthenticationFilter;
import uk.ac.ebi.tsc.aap.client.security.TokenAuthenticationService;

/**
 * Created by neilg on 24/05/2017.
 */
@Configuration
@EnableConfigurationProperties
@ConditionalOnWebApplication
@EnableWebSecurity
@ComponentScan("uk.ac.ebi.tsc.aap.client.security")
@ConditionalOnProperty(prefix = "aap", name = "enabled", matchIfMissing = true)
@Order(SecurityProperties.BASIC_AUTH_ORDER - 15)
public class SubsAAPWebSecurityConfig extends WebSecurityConfigurerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubsAAPWebSecurityConfig.class);


    @Autowired
    private StatelessAuthenticationEntryPoint unauthorizedHandler;

    @Autowired
    private TokenAuthenticationService tokenAuthenticationService;

    private StatelessAuthenticationFilter statelessAuthenticationFilterBean() throws Exception {
        LOGGER.info("this.tokenAuthenticationService: " + this.tokenAuthenticationService);
        return new StatelessAuthenticationFilter(this.tokenAuthenticationService);
    }

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        LOGGER.info("[StatelessAuthenticationEntryPoint]- " + unauthorizedHandler);

        httpSecurity
                // we don't need CSRF because our token is invulnerable
                .csrf().disable()
                .exceptionHandling().authenticationEntryPoint(unauthorizedHandler).and()
                // don't create session
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                .authorizeRequests().antMatchers("/api").permitAll()
                .antMatchers("/").permitAll()
                .antMatchers("/browser/**/*").permitAll()
                .antMatchers("/docs/**/*").permitAll()
                .anyRequest().authenticated();

        httpSecurity.addFilterBefore(statelessAuthenticationFilterBean(),
                UsernamePasswordAuthenticationFilter.class);
        // disable page caching
        httpSecurity.headers().cacheControl();
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService());
    }

}
