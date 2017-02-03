package uk.ac.ebi.subs.frontend.handlers;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;
import uk.ac.ebi.subs.repository.model.Project;

/**
 * Repo event handler for projects nested in a submission
 */
@Component
@RepositoryEventHandler(Project.class)
public class ProjectEventHandler {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private CoreSubmittableEventHelper coreSubmittableEventHelper;

    @HandleBeforeCreate
    public void handleBeforeCreate(Project src) {
        logger.info("event before create {}", src);
        coreSubmittableEventHelper.beforeCreate(src);
    }
}