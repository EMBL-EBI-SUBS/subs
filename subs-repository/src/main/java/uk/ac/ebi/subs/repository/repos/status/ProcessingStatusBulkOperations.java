package uk.ac.ebi.subs.repository.repos.status;

import com.mongodb.BulkWriteResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;
import uk.ac.ebi.subs.data.Submission;
import uk.ac.ebi.subs.data.status.ProcessingStatusEnum;
import uk.ac.ebi.subs.data.submittable.Submittable;
import uk.ac.ebi.subs.repository.model.ProcessingStatus;

import java.util.Collection;
import java.util.stream.Stream;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;
import static org.springframework.data.mongodb.core.query.Update.update;

/**
 * Created by davidr on 26/06/2017.
 */
@Component
public class ProcessingStatusBulkOperations {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private MongoTemplate mongoTemplate;

    public ProcessingStatusBulkOperations(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public void updateProcessingStatus(Collection<String> statusesToApplyTo, Stream<Submittable> submittables, Submission submission, ProcessingStatusEnum processingStatusEnum) {

        BulkOperations ops = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, ProcessingStatus.class);

        Update update = update("status", processingStatusEnum.name());

        submittables
                .map(submittable -> query(
                        where("submissionId").is(submission.getId())
                                .and("submittableId").is(submittable.getId())
                                .and("status").in(statusesToApplyTo)
                ))
                .forEach(query ->
                        ops.updateOne(query, update)
                );

        BulkWriteResult writeResult = ops.execute();
        logger.info("Setting processing status to {} for certs for {} items in submission {}",
                processingStatusEnum,
                writeResult.getModifiedCount(),
                submission.getId()
        );
    }
}
