package uk.ac.ebi.subs.repository.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.ac.ebi.subs.data.component.*;
import uk.ac.ebi.subs.repository.model.*;
import uk.ac.ebi.subs.repository.repos.submittables.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class RepoConfig {

    @Autowired
    private AnalysisRepository analysisRepository;
    @Autowired
    private AssayDataRepository assayDataRepository;
    @Autowired
    private AssayRepository assayRepository;
    @Autowired
    private EgaDacPolicyRepository egaDacPolicyRepository;
    @Autowired
    private EgaDacRepository egaDacRepository;
    @Autowired
    private EgaDatasetRepository egaDatasetRepository;
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private ProtocolRepository protocolRepository;
    @Autowired
    private SampleGroupRepository sampleGroupRepository;
    @Autowired
    private SampleRepository sampleRepository;
    @Autowired
    private StudyRepository studyRepository;


    @Bean
    public List<SubmittableRepository<?>> submissionContentsRepositories() {
        return Arrays.asList(
                analysisRepository,
                assayDataRepository,
                assayRepository,
                egaDacPolicyRepository,
                egaDacRepository,
                egaDatasetRepository,
                projectRepository,
                protocolRepository,
                sampleGroupRepository,
                sampleRepository,
                studyRepository
        );
    }

    @Bean
    public Map<Class<? extends AbstractSubsRef>, SubmittableRepository<? extends StoredSubmittable>> referenceRepositoryMap() {
        Map<Class<? extends AbstractSubsRef>, SubmittableRepository<? extends StoredSubmittable>> referenceRepositoryMap = new HashMap<>();

        referenceRepositoryMap.put(AssayDataRef.class, assayDataRepository);
        referenceRepositoryMap.put(AnalysisRef.class, analysisRepository);
        referenceRepositoryMap.put(AssayRef.class, assayRepository);
        referenceRepositoryMap.put(EgaDacPolicyRef.class, egaDacPolicyRepository);
        referenceRepositoryMap.put(EgaDacRef.class, egaDacRepository);
        referenceRepositoryMap.put(EgaDatasetRef.class, egaDatasetRepository);
        referenceRepositoryMap.put(ProjectRef.class, projectRepository);
        referenceRepositoryMap.put(ProtocolRef.class, protocolRepository);
        referenceRepositoryMap.put(SampleGroupRef.class, sampleGroupRepository);
        referenceRepositoryMap.put(SampleRef.class, sampleRepository);
        referenceRepositoryMap.put(StudyRef.class, studyRepository);

        return referenceRepositoryMap;
    }

    @Bean
    public Map<Class<? extends StoredSubmittable>, SubmittableRepository<? extends StoredSubmittable>> submittableRepositoryMap() {
        Map<Class<? extends StoredSubmittable>, SubmittableRepository<? extends StoredSubmittable>> map = new HashMap<>();

        map.put(Analysis.class, analysisRepository);
        map.put(AssayData.class, assayDataRepository);
        map.put(Assay.class, assayRepository);
        map.put(EgaDacPolicy.class, egaDacPolicyRepository);
        map.put(EgaDac.class, egaDacRepository);
        map.put(EgaDataset.class, egaDatasetRepository);
        map.put(Project.class, projectRepository);
        map.put(Protocol.class, protocolRepository);
        map.put(SampleGroup.class, sampleGroupRepository);
        map.put(Sample.class, sampleRepository);
        map.put(Study.class, studyRepository);

        return map;
    }
}
