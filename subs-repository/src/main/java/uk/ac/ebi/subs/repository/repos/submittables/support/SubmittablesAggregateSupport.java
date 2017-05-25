package uk.ac.ebi.subs.repository.repos.submittables.support;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import uk.ac.ebi.subs.repository.model.StoredSubmittable;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;
import static org.springframework.data.mongodb.core.query.Criteria.where;

public class SubmittablesAggregateSupport<T extends StoredSubmittable> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private MongoTemplate mongoTemplate;
    private Class<T> clazz;

    public SubmittablesAggregateSupport(MongoTemplate mongoTemplate, Class<T> clazz) {
        this.mongoTemplate = mongoTemplate;
        this.clazz = clazz;
    }

    public Page<T> itemsByTeam(String teamName, Pageable pageable) {
        List<T> resultsList = getLimitedItemListByTeam(teamName, pageable);
        long totalItemsCount = getTotalItemCountByTeam(teamName);
        return new PageImpl<T>(resultsList, pageable, totalItemsCount);
    }

    private long getTotalItemCountByTeam(String teamName) {
        AggregationResults aggregationResults = mongoTemplate.aggregate(
                Aggregation.newAggregation(
                        teamMatchOperation(teamName),
                        groupByAlias(),
                        group().count().as("count")
                ),
                clazz, clazz
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

    private List<T> getLimitedItemListByTeam(String teamName, Pageable pageable) {
        final List<T> resultsList = new ArrayList<>();

        AggregationResults aggregationResults = mongoTemplate.aggregate(Aggregation.newAggregation(
                teamMatchOperation(teamName),
                sortAliasCreatedDate(),
                groupByAliasWithFirstItem(),
                skip((long) pageable.getOffset()),
                limit((long) pageable.getPageSize()),
                replaceRoot("first")
        ), clazz, clazz);

        return aggregationResults.getMappedResults();
    }

    private GroupOperation groupByAliasWithFirstItem() {
        return group("alias").first("$$ROOT").as("first");
    }

    private GroupOperation groupByAlias() {
        return group("alias");
    }

    private SortOperation sortAliasCreatedDate() {
        return sort(Sort.Direction.DESC, "alias").and(Sort.Direction.DESC, "createdDate");
    }

    private MatchOperation teamMatchOperation(String teamName) {
        return match(where("team.name").is(teamName));
    }

}
