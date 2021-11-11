package nz.govt.natlib.tools.sip.generation.newspapers.scenarios

import groovy.util.logging.Log4j2
import nz.govt.natlib.tools.sip.IEEntityType
import nz.govt.natlib.tools.sip.extraction.SipXmlExtractor
import nz.govt.natlib.tools.sip.generation.newspapers.NewspaperProcessingParameters
import nz.govt.natlib.tools.sip.generation.newspapers.TestHelper
import nz.govt.natlib.tools.sip.generation.newspapers.TestHelper.TestMethodState
import nz.govt.natlib.tools.sip.generation.newspapers.parameters.ProcessingOption
import nz.govt.natlib.tools.sip.generation.newspapers.parameters.ProcessingRule
import nz.govt.natlib.tools.sip.generation.newspapers.parameters.ProcessingType
import nz.govt.natlib.tools.sip.generation.newspapers.processor.NewspaperFilesProcessor
import nz.govt.natlib.tools.sip.state.SipProcessingExceptionReasonType
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDate
import java.time.format.DateTimeFormatter

import static org.hamcrest.core.Is.is
import static org.junit.Assert.*

/**
 * Tests the {@code missing-sequence-with-double-wide-pages} scenario.
 *
 * Note that this test is complicated by the files either being part of a directory structure or in a resource file (jar),
 * so the {@link TestHelper} class is used to handle both scenarios. In real-life processing the files would be on the
 * filesystem and not in a resource. We explicitly use only filesystem files in
 * {@link #correctlyAssembleSipFromFilesOnFilesystem} (as an example to script writers), but this unit test is
 * ignored for builds.
 */
@RunWith(MockitoJUnitRunner.class)
@Log4j2
class MissingSequenceDoubleWidePagesTest {
    // TODO Make this processing simpler
    // - given a starting folder
    // - and a set of selection criteria
    // - create SIPs for the given files
    static String ID_COLUMN_NAME = "MMSID"

    static final String RESOURCES_FOLDER = "ingestion-files-tests/scenario-missing-sequence-with-double-wide-pages"
    static final String IMPORT_PARAMETERS_FILENAME = "test-publication-types.json"
    static final String PUBLICATION_TYPE = "WMMA"

    TestMethodState testMethodState

    @Before
    void setup() {
        testMethodState = new TestMethodState(ID_COLUMN_NAME, RESOURCES_FOLDER, IMPORT_PARAMETERS_FILENAME, PUBLICATION_TYPE)
    }

    /**
     * Note to developers: Ensure that this is exactly the same test as {@link #correctlyAssembleSipFromFilesNoRuleOverrides()}, except
     * that this test only reads from the file system, not a resource file.
     *
     * This test should use the local filesystem when running from within an IDE.
     *
     * This test then becomes a starting point for scripts that create and process SIPs.
     */
    @Test
    // TODO Ignore this test before making a code commit
    @Ignore
    void correctlyAssembleSipFromFilesOnFilesystemNoRuleOverrides() {
        correctlyAssembleSipFromFilesOnFilesystem([ ])
    }

    void correctlyAssembleSipFromFilesOnFilesystem(List<ProcessingRule> overrideRules) {
        boolean forLocalFilesystem = true
        TestHelper.initializeTestMethod(testMethodState, "MissingSequenceDoubleWidePagesTest-", forLocalFilesystem)

        // TODO A more complicated pattern -- date and other masks?
        boolean isRegexNotGlob = true
        boolean matchFilenameOnly = true
        boolean sortFiles = true
        List<Path> filesForProcessing = TestHelper.getFilesForProcessingFromFileSystem(isRegexNotGlob, matchFilenameOnly,
                sortFiles, testMethodState.localPath, ".*?\\.[pP]{1}[dD]{1}[fF]{1}")

        processFiles(filesForProcessing, overrideRules)
    }

    @Test
    void correctlyAssembleSipFromFilesNoRuleOverrides() {
        correctlyAssembleSipFromFiles([ ])
    }

    void correctlyAssembleSipFromFiles(List<ProcessingRule> overrideRules) {
        boolean forLocalFilesystem = false
        TestHelper.initializeTestMethod(testMethodState, "MissingSequenceDoubleWidePagesTest-", forLocalFilesystem)

        // TODO A more complicated pattern -- date and other masks?
        boolean isRegexNotGlob = true
        boolean matchFilenameOnly = true
        boolean sortFiles = true
        List<Path> filesForProcessing = TestHelper.getFilesForProcessingFromResource(isRegexNotGlob, matchFilenameOnly,
                sortFiles, testMethodState.resourcePath, testMethodState.localPath, ".*?\\.[pP]{1}[dD]{1}[fF]{1}")

        processFiles(filesForProcessing, overrideRules)
    }

    void processFiles(List<Path> filesForProcessing, List<ProcessingRule> overrideRules) {
        String dateString = "23Nov18"
        LocalDate processingDate = LocalDate.parse(dateString, DateTimeFormatter.ofPattern(testMethodState.publicationType.DATE_TIME_PATTERN))

        Path sourceFolder = Path.of(testMethodState.localPath)
        List<NewspaperProcessingParameters> parametersList = NewspaperProcessingParameters.build("TSTP",
                [ ProcessingType.ParentGrouping ], sourceFolder, processingDate, testMethodState.newspaperSpreadsheet,
                [ ], [ ProcessingOption.AlwaysGenerateThumbnailPage ])

        assertThat("Only a single NewspaperProcessingParameters is returned, size=${parametersList.size()}",
                parametersList.size(), is(1))

        NewspaperProcessingParameters processingParameters = parametersList.first()

        assertTrue("Processing parameters always generates thumbnail page setting=${processingParameters.options}",
                processingParameters.options.contains(ProcessingOption.GenerateProcessedPdfThumbnailsPage))

        processingParameters.sipProcessingState = testMethodState.sipProcessingState
        NewspaperFilesProcessor.processCollectedFiles(processingParameters, filesForProcessing, PUBLICATION_TYPE)
        String sipAsXml = processingParameters.sipProcessingState.sipAsXml

        log.info("${System.lineSeparator()}NewspaperProcessingParameters and SipProcessingState:")
        log.info(processingParameters.detailedDisplay(0, true))
        log.info(System.lineSeparator())

        int expectedNumberOfFilesProcessed = 10
        int expectedNumberOfSipFiles = 10
        int expectedNumberOfThumbnailPageFiles = 10
        int expectedNumberOfValidFiles = 10
        int expectedNumberOfInvalidFiles = 0
        int expectedNumberOfIgnoredFiles = 0
        int expectedNumberOfUnrecognizedFiles = 0
        TestHelper.assertSipProcessingStateFileNumbers(expectedNumberOfFilesProcessed, expectedNumberOfSipFiles,
                expectedNumberOfThumbnailPageFiles, expectedNumberOfValidFiles, expectedNumberOfInvalidFiles,
                expectedNumberOfIgnoredFiles, expectedNumberOfUnrecognizedFiles, testMethodState.sipProcessingState)

        if (processingParameters.options.contains(ProcessingOption.GenerateProcessedPdfThumbnailsPage) &&
                processingParameters.options.contains(ProcessingOption.AlwaysGenerateThumbnailPage)) {
            assertTrue("Thumbnail page exists, file=${processingParameters.thumbnailPageFile.normalize()}",
                    Files.exists(processingParameters.thumbnailPageFile))
            // We delete the file because we don't want it sticking around after the test
            // Comment out the following line if you want to view the file
            Files.deleteIfExists(processingParameters.thumbnailPageFile)
        } else {
            assertNull("Thumbnail page DOES NOT exist, file=${processingParameters.thumbnailPageFile}",
                    processingParameters.thumbnailPageFile)
        }

        log.info("STARTING SIP validation")
        sipConstructedCorrectly(sipAsXml, processingParameters.rules.contains(ProcessingRule.Manual))
        log.info("ENDING SIP validation")
        log.info("Process output path=${testMethodState.processOutputInterceptor.path}")
        Path processingStateFilePath = testMethodState.sipProcessingState.toTempFile()
        log.info("sipProcessingState file path=${processingStateFilePath}")
        testMethodState.processOutputInterceptor.stopAndClose()
        // In a normal processing script, the processed files, the processing output and the sipProcessingState file
        // would be moved/copied to a processing completed directory based on the processing state.
    }

    void sipConstructedCorrectly(String sipXml, boolean expectManualError) {
        SipXmlExtractor sipForValidation = new SipXmlExtractor(sipXml)

        assertTrue("SipXmlExtractor has content", sipForValidation.xml.length() > 0)

        assertTrue("SipProcessingState is complete", testMethodState.sipProcessingState.isComplete())

        if (expectManualError) {
            TestHelper.assertExpectedExceptionReason(testMethodState.sipProcessingState, SipProcessingExceptionReasonType.MANUAL_PROCESSING_REQUIRED)
        } else {
            assertTrue("SipProcessingState is successful", testMethodState.sipProcessingState.isSuccessful())
        }

        TestHelper.assertExpectedSipMetadataValues(sipForValidation, "Test Publication One", "2018", "11", "23",
                IEEntityType.NewspaperIE, "ALMAMMS", "test-mms-id-one", "200",
                "PRESERVATION_MASTER", "VIEW", true, 1)

        TestHelper.assertExpectedSipFileValues(sipForValidation, 1, "TSTP23Nov18001.pdf", "TSTP23Nov18001.pdf",
                12539L, "MD5", "6803efd241fed95f0614ad1e70380148", "0001", "application/pdf")

        TestHelper.assertExpectedSipFileValues(sipForValidation, 2, "TSTP23Nov18002.pdf", "TSTP23Nov18002.pdf",
                12770L, "MD5", "d455554b29d9ec67e0841834619efdfc", "0002", "application/pdf")

        TestHelper.assertExpectedSipFileValues(sipForValidation, 3, "TSTP23Nov18004.pdf", "TSTP23Nov18004.pdf",
                12539L, "MD5", "6803efd241fed95f0614ad1e70380148", "0003", "application/pdf")

        TestHelper.assertExpectedSipFileValues(sipForValidation, 4, "TSTP23Nov18005.pdf", "TSTP23Nov18005.pdf",
                12539L, "MD5", "6803efd241fed95f0614ad1e70380148", "0004", "application/pdf")

        TestHelper.assertExpectedSipFileValues(sipForValidation, 5, "TSTP23Nov18007.pdf", "TSTP23Nov18007.pdf",
                12770L, "MD5", "d455554b29d9ec67e0841834619efdfc", "0005", "application/pdf")

        TestHelper.assertExpectedSipFileValues(sipForValidation, 6, "TSTP23Nov18008.pdf", "TSTP23Nov18008.pdf",
                12539L, "MD5", "6803efd241fed95f0614ad1e70380148", "0006", "application/pdf")

        TestHelper.assertExpectedSipFileValues(sipForValidation, 7, "TSTP23Nov18009.pdf", "TSTP23Nov18009.pdf",
                12539L, "MD5", "6803efd241fed95f0614ad1e70380148", "0007", "application/pdf")

        TestHelper.assertExpectedSipFileValues(sipForValidation, 8, "TSTP23Nov18010.pdf", "TSTP23Nov18010.pdf",
                12539L, "MD5", "6803efd241fed95f0614ad1e70380148", "0008", "application/pdf")

        TestHelper.assertExpectedSipFileValues(sipForValidation, 9, "TSTP23Nov18012.pdf", "TSTP23Nov18012.pdf",
                12923L, "MD5", "d58fb5c894632654d9bcb638485bac91", "0009", "application/pdf")

        TestHelper.assertExpectedSipFileValues(sipForValidation, 10, "TSTP23Nov18013.pdf", "TSTP23Nov18013.pdf",
                12539L, "MD5", "6803efd241fed95f0614ad1e70380148", "0010", "application/pdf")
    }
}