package uk.ac.ebi.subs.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This class is responsible for the configuration of the USI internal queues.
 */
@Configuration
public class UsiInternalQueueConfig {

    /**
     * Instantiate a {@code Queue} for validate submissions.
     *
     * @return an instance of a {@code Queue} for validate submissions.
     */
    @Bean
    Queue validatorQueue() {
        return new Queue(Queues.SUBMISSION_VALIDATOR, true);
    }

    /**
     * Create a {@code Binding} between the submission exchange and validation queue using the routing key of created submissions.
     *
     * @param validatorQueue {@code Queue} for validating submissions before submitting them
     * @param submissionExchange {@code TopicExchange} for submissions
     * @return a {@code Binding} between the submission exchange and validation queue using the routing key of created submissions.
     */
    @Bean
    Binding validationForCreatedSubmissionBinding(Queue validatorQueue, TopicExchange submissionExchange) {
        return BindingBuilder.bind(validatorQueue).to(submissionExchange)
                .with(Queues.SUBMISSION_VALIDATOR_SUBMISSION_CREATED_ROUTING_KEY);
    }

    /**
     * Create a {@code Binding} between the submission exchange and validation queue using the routing key of updated submissions.
     *
     * @param validatorQueue {@code Queue} for validating submissions before submitting them
     * @param submissionExchange {@code TopicExchange} for submissions
     * @return a {@code Binding} between the submission exchange and validation queue using the routing key of updated submissions.
     */
    @Bean
    Binding validationForUpdatedSubmissionBinding(Queue validatorQueue, TopicExchange submissionExchange) {
        return BindingBuilder.bind(validatorQueue).to(submissionExchange)
                .with(Queues.SUBMISSION_VALIDATOR_SUBMISSION_UPDATED_ROUTING_KEY);
    }

    /**
     * Queue for certificate envelopes to update submission state
     *
     * @return
     */
    @Bean
    Queue monitorQueue() {
        return new Queue(Queues.SUBMISSION_MONITOR, true);
    }

    @Bean
    Binding monitorBinding(Queue monitorQueue, TopicExchange submissionExchange) {
        return BindingBuilder.bind(monitorQueue).to(submissionExchange).with(Queues.SUBMISSION_MONITOR_ROUTING_KEY);
    }

    /**
     * Queue for submission envelopes that have had supporting information added
     *
     * @return
     */
    @Bean
    Queue suppInfoProvidedQueue() {
        return new Queue(Queues.SUBMISSION_SUPPORTING_INFO_PROVIDED, true);
    }

    @Bean
    Binding suppInfoProvidedBinding(Queue suppInfoProvidedQueue, TopicExchange submissionExchange) {
        return BindingBuilder.bind(suppInfoProvidedQueue).to(submissionExchange).with(Queues.SUBMISSION_SUPPORTING_INFO_PROVIDED_ROUTING_KEY);
    }

}
