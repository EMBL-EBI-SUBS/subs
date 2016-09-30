package uk.ac.ebi.subs.submissiongeneration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.any23.encoding.TikaEncodingDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.ena.taxonomy.client.TaxonomyClient;
import uk.ac.ebi.subs.data.submittable.Submission;
import uk.ac.ebi.subs.submissiongeneration.ArrayExpressModel.ArrayExpressFilesResponse;

import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.stream.Stream;

@Service
public class ArrayExpressSubmissionGenerationService implements SubmissionGenerationService {

    private static final Logger logger = LoggerFactory.getLogger(ArrayExpressSubmissionGenerationService.class);


    @Override
    public void writeSubmissions(Path targetDir) {
        try {
            streamTsv().forEach(p -> {
                try {
                    processAccDate(p.getFirst(), p.getSecond(), targetDir);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (uk.ac.ebi.arrayexpress2.magetab.exception.ParseException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void writeSubmissionsFromRange(Date start, Date end, Path targetDir) {
        logger.debug("writing submissions to {} for date range {} to {}", targetDir, start, end);

        try {
            streamTsv()
                    .filter(stringDatePair -> {
                        Date d = stringDatePair.getSecond();

                        boolean dateBeforeEnd = (d.before(end) || d.equals(end));
                        boolean dateAfterStart = (d.after(start) || d.equals(start));

                        return dateBeforeEnd && dateAfterStart;
                    }).forEach(p -> {
                try {
                    processAccDate(p.getFirst(), p.getSecond(), targetDir);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (uk.ac.ebi.arrayexpress2.magetab.exception.ParseException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }


    }


    @Value("${arrayExpressTsvUrl:https://www.ebi.ac.uk/arrayexpress/ArrayExpress-Experiments.txt?keywords=&organism=&exptype%5B%5D=&exptype%5B%5D=&array=&directsub=on}")
    URL arrayExpressTsvUrl;

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    SimpleDateFormat weekInYearSdf = new SimpleDateFormat("ww");
    SimpleDateFormat yearSdf = new SimpleDateFormat("yyyy");

    RestTemplate restTemplate = new RestTemplate();

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    AeMageTabConverter aeMageTabConverter;


    public void processAccDate(String accession, Date releaseDate, Path targetDir) throws IOException, uk.ac.ebi.arrayexpress2.magetab.exception.ParseException, ParseException {
        logger.info("Converting {} of {} into {}",accession, releaseDate, targetDir);

        String url = "http://www.ebi.ac.uk/arrayexpress/json/v2/files/" + accession;
        ArrayExpressFilesResponse response = restTemplate.getForObject(url, ArrayExpressFilesResponse.class);

        Submission submission = aeMageTabConverter.mageTabToSubmission(response.idfUrl(accession));

        if (submission == null){
            logger.info("Skipping submission {}",accession); //TODO parser can't handle multi-SDRF magetab
            return;
        }

        writeSubmission(submission, accession, targetDir, releaseDate);
    }

    public java.io.File downloadTsv() throws IOException {
        java.io.File tmpFile = java.io.File.createTempFile("ae-experiments", "tsv");
        tmpFile.deleteOnExit();

        ReadableByteChannel rbc = Channels.newChannel(arrayExpressTsvUrl.openStream());
        FileOutputStream fos = new FileOutputStream(tmpFile);
        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        fos.close();

        return tmpFile;
    }

    public Stream<Pair<String, Date>> streamTsv() throws IOException {
        java.io.File tsvFile = downloadTsv();
        return streamTsvLines(tsvFile)
                .filter(l -> !l.startsWith("Accession"))
                .map(line -> {
                    String[] row = line.split("\t");
                    return row;
                })
                .filter(row -> row.length >= 6)
                .map(row -> {
                    String[] accDate = {row[0], row[5]};
                    return accDate;
                })
                .filter(accDate -> accDate[0].matches("E-\\w+-\\d+"))
                .map(accDate -> {
                    Date date = null;
                    try {
                        date = sdf.parse(accDate[1]);
                    } catch (java.text.ParseException e) {
                        e.printStackTrace();
                    }
                    Pair<String, Date> pair = Pair.of(accDate[0], date);
                    return pair;
                });
    }

    public Stream<String> streamTsvLines(java.io.File tsvFile) throws IOException {
        Path p = tsvFile.toPath();
        return java.nio.file.Files.lines(p, guessCharset(tsvFile));
    }

    public Charset guessCharset(java.io.File file) throws IOException {
        InputStream is = new BufferedInputStream(new FileInputStream(file));
        Charset cs = Charset.forName(new TikaEncodingDetector().guessEncoding(is));
        is.close();
        logger.debug("Charset guessed {}", cs);
        return cs;
    }

    public void writeSubmission(Submission submission, String accession, Path rootTargetDir, Date releaseDate) throws IOException {

        String year = yearSdf.format(releaseDate);
        String week = weekInYearSdf.format(releaseDate);

        rootTargetDir.toFile();

        String dirName = String.join(File.separator, rootTargetDir.toString(), year, week);

        Files.createDirectories(Paths.get(dirName));

        String fileName = dirName + File.separator + accession + "." + releaseDate.getTime() + ".json";
        File outputFile = new File(fileName);

        System.out.println("Writing " + outputFile.getAbsolutePath().toString());
        objectMapper.writeValue(outputFile, submission);
        logger.debug("Output target: {}", outputFile);

    }


}
