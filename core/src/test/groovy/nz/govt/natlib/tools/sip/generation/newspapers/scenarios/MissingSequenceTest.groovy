package nz.govt.natlib.tools.sip.generation.newspapers.scenarios

import groovy.util.logging.Log4j2
import nz.govt.natlib.tools.sip.IEEntityType
import nz.govt.natlib.tools.sip.extraction.SipXmlExtractor
import nz.govt.natlib.tools.sip.generation.newspapers.NewspaperProcessingParameters
import nz.govt.natlib.tools.sip.generation.newspapers.TestHelper
import nz.govt.natlib.tools.sip.generation.newspapers.TestHelper.TestMethodState
import nz.govt.natlib.tools.sip.generation.newspapers.parameters.ProcessingType
import nz.govt.natlib.tools.sip.generation.newspapers.processor.NewspaperFilesProcessor
import nz.govt.natlib.tools.sip.state.SipProcessingException
import nz.govt.natlib.tools.sip.state.SipProcessingExceptionReason
import nz.govt.natlib.tools.sip.state.SipProcessingExceptionReasonType
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

import java.nio.file.Path
import java.time.LocalDate
import java.time.format.DateTimeFormatter

import static org.hamcrest.core.Is.is
import static org.junit.Assert.assertThat
import static org.junit.Assert.assertTrue

/**
 * Tests the {@code missing-sequence} scenario.
 *
 * Note that this test is complicated by the files either being part of a directory structure or in a resource file (jar),
 * so the {@link TestHelper} class is used to handle both scenarios. In real-life processing the files would be on the
 * filesystem and not in a resource. We explicitly use only filesystem files in
 * {@link #correctlyAssembleSipFromFilesOnFilesystem} (as an example to script writers), but this unit test is
 * ignored for builds.
 */
@RunWith(MockitoJUnitRunner.class)
@Log4j2
class MissingSequenceTest {
    // TODO Make this processing simpler
    // - given a starting folder
    // - and a set of selection criteria
    // - create SIPs for the given files
    static String ID_COLUMN_NAME = "MMSID"

    static final String RESOURCES_FOLDER = "ingestion-files-tests/scenario-missing-sequence"
    static final String IMPORT_PARAMETERS_FILENAME = "test-newspaper-types.json"
    static final String NEWSPAPER_TYPE = "WMMA"

    TestMethodState testMethodState

    @Before
    void setup() {
        testMethodState = new TestMethodState(ID_COLUMN_NAME, RESOURCES_FOLDER, IMPORT_PARAMETERS_FILENAME, NEWSPAPER_TYPE)
    }

    /**
     * Note to developers: Ensure that this is exactly the same test as {@link #correctlyAssembleSipFromFiles()}, except
     * that this test only reads from the file system, not a resource file.
     *
     * This test should use the local filesystem when running from within an IDE.
     *
     * This test then becomes a starting point for scripts that create and process SIPs.
     */
    @Test
    // TODO Ignore this test before making a code commit
    @Ignore
    void correctlyAssembleSipFromFilesOnFilesystem() {
        boolean forLocalFilesystem = true
        TestHelper.initializeTestMethod(testMethodState, "MissingSequenceTest-", forLocalFilesystem)

        // TODO A more complicated pattern -- date and other masks?
        boolean isRegexNotGlob = true
        boolean matchFilenameOnly = true
        boolean sortFiles = true
        List<Path> filesForProcessing = TestHelper.getFilesForProcessingFromFileSystem(isRegexNotGlob, matchFilenameOnly,
                sortFiles, testMethodState.localPath, ".*?\\.[pP]{1}[dD]{1}[fF]{1}")

        processFiles(filesForProcessing)
    }

    @Test
    void correctlyAssembleSipFromFiles() {
        boolean forLocalFilesystem = false
        TestHelper.initializeTestMethod(testMethodState, "MissingSequenceTest-", forLocalFilesystem)

        // TODO A more complicated pattern -- date and other masks?
        boolean isRegexNotGlob = true
        boolean matchFilenameOnly = true
        boolean sortFiles = true
        List<Path> filesForProcessing = TestHelper.getFilesForProcessingFromResource(isRegexNotGlob, matchFilenameOnly,
                sortFiles, testMethodState.resourcePath, testMethodState.localPath, ".*?\\.[pP]{1}[dD]{1}[fF]{1}")

        processFiles(filesForProcessing)
    }

    void processFiles(List<Path> filesForProcessing) {
        String dateString = "23Nov18"
        LocalDate processingDate = LocalDate.parse(dateString, DateTimeFormatter.ofPattern(testMethodState.newspaperType.DATE_TIME_PATTERN))

        Path sourceFolder = Path.of(testMethodState.localPath)
        List<NewspaperProcessingParameters> parametersList = NewspaperProcessingParameters.build("TSTP",
                [ ProcessingType.ParentGrouping ], sourceFolder, processingDate, testMethodState.newspaperSpreadsheet)

        assertThat("Only a single NewspaperProcessingParameters is returned, size=${parametersList.size()}",
                parametersList.size(), is(1))

        NewspaperProcessingParameters processingParameters = parametersList.first()
        processingParameters.sipProcessingState = testMethodState.sipProcessingState
        NewspaperFilesProcessor.processCollectedFiles(processingParameters, filesForProcessing, NEWSPAPER_TYPE)
        String sipAsXml = processingParameters.sipProcessingState.sipAsXml

        log.info("${System.lineSeparator()}NewspaperProcessingParameters and SipProcessingState:")
        log.info(processingParameters.detailedDisplay(0, true))
        log.info(System.lineSeparator())

        int expectedNumberOfFilesProcessed = 7
        int expectedNumberOfSipFiles = 7
        int expectedNumberOfValidFiles = 7
        int expectedNumberOfInvalidFiles = 0
        int expectedNumberOfIgnoredFiles = 0
        int expectedNumberOfUnrecognizedFiles = 0
        TestHelper.assertSipProcessingStateFileNumbers(expectedNumberOfFilesProcessed, expectedNumberOfSipFiles,
                expectedNumberOfValidFiles, expectedNumberOfInvalidFiles,
                expectedNumberOfIgnoredFiles, expectedNumberOfUnrecognizedFiles, testMethodState.sipProcessingState)

        log.info("SIP validation")
        sipConstructedCorrectly(sipAsXml)
        log.info("ENDING SIP validation")
        log.info("Process output path=${testMethodState.processOutputInterceptor.path}")
        Path processingStateFilePath = testMethodState.sipProcessingState.toTempFile()
        log.info("sipProcessingState file path=${processingStateFilePath}")
        testMethodState.processOutputInterceptor.stopAndClose()
        // In a normal processing script, the processed files, the processing output and the sipProcessingState file
        // would be moved/copied to a processing completed directory based on the processing state.
    }

    void sipConstructedCorrectly(String sipXml) {
        SipXmlExtractor sipForValidation = new SipXmlExtractor(sipXml)

        assertTrue("SipXmlExtractor has content", sipForValidation.xml.length() > 0)

        assertTrue("SipProcessingState is complete", testMethodState.sipProcessingState.isComplete())
        TestHelper.assertExpectedExceptionReason(testMethodState.sipProcessingState, SipProcessingExceptionReasonType.MISSING_SEQUENCE_FILES)
        SipProcessingException firstException = testMethodState.sipProcessingState.exceptions.first()
        SipProcessingExceptionReason firstExceptionReason = firstException.reasons.first()
        String expectedReason = "One or more skipped sequences precedes these files=[TSTP23Nov18004.pdf, TSTP23Nov18A02.pdf, TSTP23Nov18B03.pdf]"
        assertThat("SipProcessingState firstExceptionReason is ${expectedReason}", firstExceptionReason.toString(), is(expectedReason))

        TestHelper.assertExpectedSipMetadataValues(sipForValidation, "Test Publication One", "2018", "11", "23",
                IEEntityType.NewspaperIE, "ALMAMMS", "test-mms-id-one", "200",
                "PRESERVATION_MASTER", "VIEW", true, 1)

        TestHelper.assertExpectedSipFileValues(sipForValidation, 1, "TSTP23Nov18001.pdf", "TSTP23Nov18001.pdf",
                636L, "MD5", "7273a4d61a8dab92be4393e2923ad2d2", "0001", "application/pdf")

        TestHelper.assertExpectedSipFileValues(sipForValidation, 2, "TSTP23Nov18002.pdf", "TSTP23Nov18002.pdf",
                636L, "MD5", "7273a4d61a8dab92be4393e2923ad2d2", "0002", "application/pdf")

        TestHelper.assertExpectedSipFileValues(sipForValidation, 3, "TSTP23Nov18004.pdf", "TSTP23Nov18004.pdf",
                636L, "MD5", "7273a4d61a8dab92be4393e2923ad2d2", "0003", "application/pdf")

        TestHelper.assertExpectedSipFileValues(sipForValidation, 4, "TSTP23Nov18A02.pdf", "TSTP23Nov18A02.pdf",
                636L, "MD5", "7273a4d61a8dab92be4393e2923ad2d2", "0004", "application/pdf")

        TestHelper.assertExpectedSipFileValues(sipForValidation, 5, "TSTP23Nov18B01.pdf", "TSTP23Nov18B01.pdf",
                636L, "MD5", "7273a4d61a8dab92be4393e2923ad2d2", "0005", "application/pdf")

        TestHelper.assertExpectedSipFileValues(sipForValidation, 6, "TSTP23Nov18B03.pdf", "TSTP23Nov18B03.pdf",
                636L, "MD5", "7273a4d61a8dab92be4393e2923ad2d2", "0006", "application/pdf")

        TestHelper.assertExpectedSipFileValues(sipForValidation, 7, "TSTP23Nov18C01.pdf", "TSTP23Nov18C01.pdf",
                636L, "MD5", "7273a4d61a8dab92be4393e2923ad2d2", "0007", "application/pdf")
    }

}
