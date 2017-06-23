package uk.ac.ebi.subs.repository.repos;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import uk.ac.ebi.subs.data.component.Team;

/**
 * Created by davidr on 23/06/2017.
 */
public interface SubmissionRepositoryCustom {

    Page<Team> distinctTeams(Pageable pageable);

}
