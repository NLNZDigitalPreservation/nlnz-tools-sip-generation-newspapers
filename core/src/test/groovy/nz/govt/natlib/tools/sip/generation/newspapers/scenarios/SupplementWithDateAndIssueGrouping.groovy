package nz.govt.natlib.tools.sip.generation.newspapers.scenarios

import groovy.util.logging.Log4j2
import nz.govt.natlib.tools.sip.IEEntityType
import nz.govt.natlib.tools.sip.extraction.SipXmlExtractor
import nz.govt.natlib.tools.sip.generation.newspapers.NewspaperProcessingParameters
import nz.govt.natlib.tools.sip.generation.newspapers.TestHelper
import nz.govt.natlib.tools.sip.generation.newspapers.TestHelper.TestMethodState
import nz.govt.natlib.tools.sip.generation.newspapers.parameters.ProcessingType
import nz.govt.natlib.tools.sip.generation.newspapers.processor.NewspaperFilesProcessor
import org.apache.commons.configuration2.PropertiesConfiguration
import org.apache.commons.configuration2.PropertiesConfigurationLayout
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

import java.nio.file.Path
import java.time.LocalDate
import java.time.format.DateTimeFormatter

import static org.hamcrest.core.Is.is
import static org.junit.Assert.assertThat
import static org.junit.Assert.assertTrue

@RunWith(MockitoJUnitRunner.class)
@Log4j2
class SupplementWithDateAndIssueGrouping {
    // TODO Make this processing simpler
    // - given a starting folder
    // - and a set of selection criteria
    // - create SIPs for the given files
    static String ID_COLUMN_NAME = "MMSID"

    static final String RESOURCES_FOLDER = "ingestion-files-tests/scenario-supplement-with-date-and-issue-grouping"
    static final String IMPORT_PARAMETERS_FILENAME = "test-newspaper-types.json"
    static final String PREVIOUS_SUPPLEMENTS_ISSUES_PATH = "src/test/resources/ingestion-files-tests/scenario-supplement-with-date-and-issue-grouping/supplements-previous-issues.properties"
    static final String NEWSPAPER_TYPE = "stuff"

    TestMethodState testMethodState

    @Before
    void setup() {
        testMethodState = new TestMethodState(ID_COLUMN_NAME, RESOURCES_FOLDER, IMPORT_PARAMETERS_FILENAME, NEWSPAPER_TYPE)

        PropertiesConfiguration supplementsConfig = new PropertiesConfiguration()
        PropertiesConfigurationLayout layout = new PropertiesConfigurationLayout()
        supplementsConfig.setLayout(layout)
        layout.load(supplementsConfig, new FileReader(PREVIOUS_SUPPLEMENTS_ISSUES_PATH))
        supplementsConfig.setProperty("FPS_PREVIOUS_ISSUE", "10")
        supplementsConfig.setProperty("FPS_PREVIOUS_DATE", "2018-01-01")
        FileWriter fileWriter = new FileWriter(PREVIOUS_SUPPLEMENTS_ISSUES_PATH, false)
        layout.save(supplementsConfig, fileWriter)
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
    void correctlyAssembleSipFromFiles() {
        boolean forLocalFilesystem = false
        TestHelper.initializeTestMethod(testMethodState, "CreateSipForFolderTest-", forLocalFilesystem)

        // TODO A more complicated pattern -- date and other masks?
        boolean isRegexNotGlob = true
        boolean matchFilenameOnly = true
        boolean sortFiles = true
        List<Path> filesForProcessing = TestHelper.getFilesForProcessingFromResource(isRegexNotGlob, matchFilenameOnly,
                sortFiles, testMethodState.resourcePath, testMethodState.localPath, ".*?\\.[pP]{1}[dD]{1}[fF]{1}")

        processFiles(filesForProcessing)
    }

    void processFiles(List<Path> filesForProcessing) {
        String dateString = "20181123"
        LocalDate processingDate = LocalDate.parse(dateString, DateTimeFormatter.ofPattern(testMethodState.newspaperType.DATE_TIME_PATTERN))

        Path sourceFolder = Path.of(testMethodState.localPath)
        List<NewspaperProcessingParameters> parametersList = NewspaperProcessingParameters.build("FPS",
                [ ProcessingType.SupplementWithDateAndIssue ], sourceFolder, processingDate, testMethodState.newspaperSpreadsheet,
                testMethodState.newspaperType, [], [], PREVIOUS_SUPPLEMENTS_ISSUES_PATH)

        assertThat("Only a single NewspaperProcessingParameters is returned, size=${parametersList.size()}",
                parametersList.size(), is(1))

        NewspaperProcessingParameters processingParameters = parametersList.first()

        processingParameters.sipProcessingState = testMethodState.sipProcessingState
        NewspaperFilesProcessor.processCollectedFiles(processingParameters, filesForProcessing, NEWSPAPER_TYPE,
                testMethodState.newspaperType)
        String sipAsXml = processingParameters.sipProcessingState.sipAsXml

        log.info("${System.lineSeparator()}NewspaperProcessingParameters and SipProcessingState:")
        log.info(processingParameters.detailedDisplay(0, true))
        log.info(System.lineSeparator())

        int expectedNumberOfFilesProcessed = 4
        int expectedNumberOfSipFiles = 4
        int expectedNumberOfValidFiles = 4
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

        TestHelper.assertExpectedSipMetadataValues(sipForValidation, "The Forever Project", "2018", "11", "23",
                IEEntityType.NewspaperIE, "ALMAMMS", "test-mms-id-one",
                "200", "PRESERVATION_MASTER", "VIEW", true, 1, "11")

        TestHelper.assertExpectedSipFileValues(sipForValidation, 1, "FPSED1-20181123-001.pdf", "FPSED1-20181123-001.pdf",
                636L, "MD5", "7273a4d61a8dab92be4393e2923ad2d2", "0001", "application/pdf")

        TestHelper.assertExpectedSipFileValues(sipForValidation, 2, "FPSED1-20181123-002.pdf", "FPSED1-20181123-002.pdf",
                636L, "MD5", "7273a4d61a8dab92be4393e2923ad2d2", "0002", "application/pdf")

        TestHelper.assertExpectedSipFileValues(sipForValidation, 3, "FPSED1-20181123-003.pdf", "FPSED1-20181123-003.pdf",
                636L, "MD5", "7273a4d61a8dab92be4393e2923ad2d2", "0003", "application/pdf")

        TestHelper.assertExpectedSipFileValues(sipForValidation, 4, "FPSED1-20181123-004.pdf", "FPSED1-20181123-004.pdf",
                636L, "MD5", "7273a4d61a8dab92be4393e2923ad2d2", "0004", "application/pdf")
    }
}
