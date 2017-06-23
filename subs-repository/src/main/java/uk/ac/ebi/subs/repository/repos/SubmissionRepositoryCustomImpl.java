package uk.ac.ebi.subs.repository.repos;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.stereotype.Service;
import uk.ac.ebi.subs.data.component.Team;
import uk.ac.ebi.subs.repository.model.Submission;

import java.util.List;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

/**
 * Created by davidr on 23/06/2017.
 */
@Service
public class SubmissionRepositoryCustomImpl implements SubmissionRepositoryCustom {

    private MongoTemplate mongoTemplate;

    public SubmissionRepositoryCustomImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Page<Team> distinctTeams(Pageable pageable) {
        List<Team> teams = mongoTemplate.aggregate(
                Aggregation.newAggregation(
                        group("team"),
                        sort(Sort.Direction.ASC, "_id.name"),
                        skip((long) pageable.getOffset()),
                        limit((long) pageable.getPageSize()),
                        replaceRoot("_id")
                ),
                Submission.class, Team.class
        ).getMappedResults();

        long totalCount = getTotalCount();

        return new PageImpl<Team>(teams, pageable, totalCount);
    }

    private long getTotalCount() {
        AggregationResults aggregationResults = mongoTemplate.aggregate(
                Aggregation.newAggregation(
                        group("team"),
                        count().as("count")),
                Submission.class, Team.class
        );

        Object results = aggregationResults.getRawResults().get("result");

        if (results != null && results instanceof BasicDBList) {
            BasicDBList resultsList = (BasicDBList) results;

            if (resultsList.isEmpty()) {
                return 0;
            }
            BasicDBObject countResult = (BasicDBObject) resultsList.get(0);

            if (countResult.containsField("count")) {
                return countResult.getLong("count");
            }

            return 0;


        }

        return -1;
    }


}
