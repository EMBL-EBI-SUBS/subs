package uk.ac.ebi.subs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import uk.ac.ebi.subs.stresstest.StressTestService;

import java.nio.file.Path;
import java.nio.file.Paths;
@SpringBootApplication

public class StressTesterApplication implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(StressTesterApplication.class);

    @Autowired
    StressTestService stressTestService;

    @Value("${searchDir:/Users/davidr/ArrayExpressSubs/2017}")
    String searchDir;

    @Override
    public void run(String... args) {

        Path searchDirPath = Paths.get(searchDir);

        logger.info("Searching for json under: "+searchDirPath.toAbsolutePath());

        this.stressTestService.submitJsonInDir(searchDirPath);
    }

    public static void main(String[] args) {
        SpringApplication.run(StressTesterApplication.class, args);
    }
}
