package nz.govt.natlib.tools.sip.generation.newspapers.scenarios

import groovy.util.logging.Log4j2
import nz.govt.natlib.tools.sip.extraction.SipXmlExtractor
import nz.govt.natlib.tools.sip.generation.newspapers.FairfaxProcessingParameters
import nz.govt.natlib.tools.sip.generation.newspapers.TestHelper
import nz.govt.natlib.tools.sip.generation.newspapers.parameters.ProcessingRule
import nz.govt.natlib.tools.sip.generation.newspapers.parameters.ProcessingType
import nz.govt.natlib.tools.sip.generation.newspapers.processor.FairfaxFilesProcessor
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
class HasSupplementTest {
    // TODO Make this processing simpler
    // - given a starting folder
    // - and a set of selection criteria
    // - create SIPs for the given files
    static String ID_COLUMN_NAME = "MMSID"

    static final String RESOURCES_FOLDER = "ingestion-files-tests/scenario-has-supplement"
    static final String IMPORT_PARAMETERS_FILENAME = "test-publication-types.json"
    static final String PUBLICATION_TYPE = "alliedPress"

    TestHelper.TestMethodState testMethodState

    @Before
    void setup() {
        testMethodState = new TestHelper.TestMethodState(ID_COLUMN_NAME, RESOURCES_FOLDER, IMPORT_PARAMETERS_FILENAME, PUBLICATION_TYPE)
    }

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
        String dateString = "26Oct2021"
        LocalDate processingDate = LocalDate.parse(dateString, DateTimeFormatter.ofPattern(testMethodState.publicationType.DATE_TIME_PATTERN))

        Path sourceFolder = Path.of(testMethodState.localPath)
        List<FairfaxProcessingParameters> parametersList = FairfaxProcessingParameters.build("Newspaper",
                [ProcessingType.ParentGrouping ], sourceFolder, processingDate, testMethodState.newspaperSpreadsheet,
                [ProcessingRule.MissingSequenceIgnored, ProcessingRule.UseFileNameForMetsLabel])

        assertThat("Only a single FairfaxProcessingParameters is returned, size=${parametersList.size()}",
                parametersList.size(), is(1))

        FairfaxProcessingParameters processingParameters = parametersList.first()

        processingParameters.sipProcessingState = testMethodState.sipProcessingState
        FairfaxFilesProcessor.processCollectedFiles(processingParameters, filesForProcessing, PUBLICATION_TYPE)
        String sipAsXml = processingParameters.sipProcessingState.sipAsXml

        log.info("${System.lineSeparator()}FairfaxProcessingParameters and SipProcessingState:")
        log.info(processingParameters.detailedDisplay(0, true))
        log.info(System.lineSeparator())

        int expectedNumberOfFilesProcessed = 2
        int expectedNumberOfSipFiles = 2
        int expectedNumberOfThumbnailPageFiles = 2
        int expectedNumberOfValidFiles = 2
        int expectedNumberOfInvalidFiles = 0
        int expectedNumberOfIgnoredFiles = 0
        int expectedNumberOfUnrecognizedFiles = 0
        TestHelper.assertSipProcessingStateFileNumbers(expectedNumberOfFilesProcessed, expectedNumberOfSipFiles,
                expectedNumberOfThumbnailPageFiles, expectedNumberOfValidFiles, expectedNumberOfInvalidFiles,
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

        TestHelper.assertExpectedSipFileValues(sipForValidation, 1, "Newspaper-26Oct2021-Tue.pdf", "Newspaper-26Oct2021-Tue.pdf",
                636L, "MD5", "7273a4d61a8dab92be4393e2923ad2d2", "Newspaper-26Oct2021-Tue", "application/pdf")

        TestHelper.assertExpectedSipFileValues(sipForValidation, 2, "Supplement-26Oct2021-Tue.pdf", "Supplement-26Oct2021-Tue.pdf",
                636L, "MD5", "7273a4d61a8dab92be4393e2923ad2d2", "Supplement-26Oct2021-Tue", "application/pdf")

    }

}
