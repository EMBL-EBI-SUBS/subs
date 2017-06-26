package uk.ac.ebi.subs.dispatcher;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.ac.ebi.subs.data.Submission;
import uk.ac.ebi.subs.data.component.Archive;
import uk.ac.ebi.subs.data.component.SampleRef;
import uk.ac.ebi.subs.data.component.SampleUse;
import uk.ac.ebi.subs.data.status.ProcessingStatusEnum;
import uk.ac.ebi.subs.data.status.SubmissionStatusEnum;
import uk.ac.ebi.subs.data.submittable.Assay;
import uk.ac.ebi.subs.data.submittable.Sample;
import uk.ac.ebi.subs.data.submittable.Submittable;
import uk.ac.ebi.subs.processing.SubmissionEnvelope;
import uk.ac.ebi.subs.repository.RefLookupService;
import uk.ac.ebi.subs.repository.SubmissionEnvelopeService;
import uk.ac.ebi.subs.repository.model.ProcessingStatus;
import uk.ac.ebi.subs.repository.model.StoredSubmittable;
import uk.ac.ebi.subs.repository.repos.SubmissionRepository;
import uk.ac.ebi.subs.repository.repos.status.ProcessingStatusBulkOperations;
import uk.ac.ebi.subs.repository.repos.status.ProcessingStatusRepository;
import uk.ac.ebi.subs.repository.repos.status.SubmissionStatusRepository;
import uk.ac.ebi.subs.repository.repos.submittables.SubmittableRepository;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class DispatcherServiceImpl implements DispatcherService {


    private static final Logger logger = LoggerFactory.getLogger(DispatcherServiceImpl.class);

    @Override
    public Map<Archive, SubmissionEnvelope> assessDispatchReadiness(Submission submission) {

        SubmissionEnvelope submissionEnvelope = submissionEnvelopeService.fetchOne(submission.getId());

        /*
         * TODO this does not use the referenced sample information in supportingSamples
         */

        /*
        * this is a deliberately simple implementation for prototyping
        * we will need to redo this as we flesh out the system
        * */


        /*
         * for now, dispatch envelopes to one archive at a time
         */

        Map<Archive, Boolean> archiveProcessingRequired = new HashMap<>();
        Arrays.asList(Archive.values()).forEach(a -> archiveProcessingRequired.put(a, false));


        submissionContentsRepositories
                .stream()
                .flatMap(repo -> repo.streamBySubmissionId(submission.getId()))
                .filter(item ->
                        processingStatusesToAllow.contains(item.getProcessingStatus().getStatus()))
                .forEach(item -> {
                    archiveProcessingRequired.put(item.getArchive(), true);
                });


        Archive targetArchive = null;

        if (archiveProcessingRequired.get(Archive.BioSamples)) {
            targetArchive = Archive.BioSamples;
        } else if (archiveProcessingRequired.get(Archive.Ena)) {
            targetArchive = Archive.Ena;
        } else if (archiveProcessingRequired.get(Archive.ArrayExpress)) {
            targetArchive = Archive.ArrayExpress;
        }

        Map<Archive, SubmissionEnvelope> readyToDispatch = new HashMap<>();


        if (targetArchive == null) {
            logger.info("no work to do on submission {}", submission.getId());
        } else {
            readyToDispatch.put(targetArchive, submissionEnvelope);
        }

        return readyToDispatch;
    }

    @Override
    public Map<Archive, SubmissionEnvelope> determineSupportingInformationRequired(Submission submission) {
        SubmissionEnvelope submissionEnvelope = submissionEnvelopeService.fetchOne(submission.getId());

        determineSupportingInformationRequired(submissionEnvelope);

        if (submissionEnvelope.getSupportingSamplesRequired().isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Archive, SubmissionEnvelope> maps = new HashMap<>();

        maps.put(Archive.BioSamples, submissionEnvelope);

        return maps;
    }

    @Override
    public void updateSubmittablesStatusToSubmitted(Archive archive, SubmissionEnvelope submissionEnvelope) {
        String submissionId = submissionEnvelope.getSubmission().getId();

        Stream<Submittable> submittables = submissionEnvelope
                .allSubmissionItemsStream()
                .filter(item -> archive.equals(item.getArchive()));

        processingStatusBulkOperations.updateProcessingStatus(
                processingStatusesToAllow,
                submittables,
                submissionEnvelope.getSubmission(),
                ProcessingStatusEnum.Dispatched
        );
    }

    // only inserting Assays' SampleRefs for now
    @Override
    public void insertReferencedSamples(SubmissionEnvelope submissionEnvelope) {
        Set<SampleRef> assaySampleRefs = submissionEnvelope.getAssays()
                .stream()
                .flatMap(assay -> assay.getSampleUses().stream())
                .map(SampleUse::getSampleRef)
                .collect(Collectors.toSet());

        submissionEnvelope.getSupportingSamples().addAll((Set<Sample>) refLookupService.lookupRefs(assaySampleRefs));
    }

    public void determineSupportingInformationRequired(SubmissionEnvelope submissionEnvelope) {
        List<Sample> samples = submissionEnvelope.getSamples();
        List<Assay> assays = submissionEnvelope.getAssays();
        Set<SampleRef> suppportingSamplesRequired = submissionEnvelope.getSupportingSamplesRequired();
        List<Sample> supportingSamples = submissionEnvelope.getSupportingSamples();

        for (Assay assay : assays) {
            for (SampleUse sampleUse : assay.getSampleUses()) {
                SampleRef sampleRef = sampleUse.getSampleRef();

                if (suppportingSamplesRequired.contains(sampleRef)) {
                    //skip the searching steps if the sample ref is already in the sample required set
                    continue;
                }

                //is the sample in the submission
                Sample s = sampleRef.findMatch(samples);

                if (s == null) {
                    //is the sample already in the supporting information
                    s = sampleRef.findMatch(supportingSamples);
                }

                if (s == null) {
                    // is the sample already in the USI db
                    s = (Sample) refLookupService.lookupRef(sampleRef);
                }

                if (s == null) {
                    // sample referenced is not in the supporting information, nor in the submission, nor in the USI db so need to fetch it
                    suppportingSamplesRequired.add(sampleRef);
                }

            }
        }

    }

    private List<Class<? extends StoredSubmittable>> submittablesClassList;
    private SubmissionEnvelopeService submissionEnvelopeService;
    private RefLookupService refLookupService;
    private SubmissionRepository submissionRepository;
    private SubmissionStatusRepository submissionStatusRepository;
    private ProcessingStatusRepository processingStatusRepository;
    private ProcessingStatusBulkOperations processingStatusBulkOperations;
    private List<SubmittableRepository<?>> submissionContentsRepositories;
    private Set<String> processingStatusesToAllow;

    public DispatcherServiceImpl(
            SubmissionEnvelopeService submissionEnvelopeService,
            RefLookupService refLookupService,
            SubmissionRepository submissionRepository,
            SubmissionStatusRepository submissionStatusRepository,
            ProcessingStatusRepository processingStatusRepository,
            List<Class<? extends StoredSubmittable>> submittablesClassList,
            List<SubmittableRepository<?>> submissionContentsRepositories,
            ProcessingStatusBulkOperations processingStatusBulkOperations

    ) {
        this.submissionEnvelopeService = submissionEnvelopeService;
        this.refLookupService = refLookupService;
        this.submissionRepository = submissionRepository;
        this.submissionStatusRepository = submissionStatusRepository;

        this.submittablesClassList = submittablesClassList;
        this.processingStatusRepository = processingStatusRepository;
        this.submissionContentsRepositories = submissionContentsRepositories;
        this.processingStatusBulkOperations = processingStatusBulkOperations;

        processingStatusesToAllow = new HashSet<>();
        processingStatusesToAllow.add(ProcessingStatusEnum.Draft.name());
        processingStatusesToAllow.add(ProcessingStatusEnum.Submitted.name());
    }

}
