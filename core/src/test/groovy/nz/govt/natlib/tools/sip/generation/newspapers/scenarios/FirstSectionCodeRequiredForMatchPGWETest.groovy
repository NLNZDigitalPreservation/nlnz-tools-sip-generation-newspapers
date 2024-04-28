package nz.govt.natlib.tools.sip.generation.newspapers.scenarios

import groovy.util.logging.Log4j2
import nz.govt.natlib.tools.sip.IEEntityType
import nz.govt.natlib.tools.sip.extraction.SipXmlExtractor
import nz.govt.natlib.tools.sip.generation.newspapers.parameters.ProcessingType
import nz.govt.natlib.tools.sip.generation.newspapers.NewspaperProcessingParameters
import nz.govt.natlib.tools.sip.generation.newspapers.TestHelper
import nz.govt.natlib.tools.sip.generation.newspapers.TestHelper.TestMethodState
import nz.govt.natlib.tools.sip.generation.newspapers.processor.NewspaperFilesProcessor
import nz.govt.natlib.tools.sip.state.SipProcessingState
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

import java.nio.file.Path
import java.time.LocalDate
import java.time.format.DateTimeFormatter

import static org.hamcrest.core.Is.is
import static org.junit.Assert.*

/**
 * Tests the {@code first-section-code-match-parent-grouping-with-edition} scenario.
 *
 * Note that this test is complicated by the files either being part of a directory structure or in a resource file (jar),
 * so the {@link TestHelper} class is used to handle both scenarios. In real-life processing the files would be on the
 * filesystem and not in a resource. We explicitly use only filesystem files in
 * {@link #correctlyAssembleSipFromFilesOnFilesystem} (as an example to script writers), but this unit test is
 * ignored for builds.
 */
@RunWith(MockitoJUnitRunner.class)
@Log4j2
class FirstSectionCodeRequiredForMatchPGWETest {
    // TODO Make this processing simpler
    // - given a starting folder
    // - and a set of selection criteria
    // - create SIPs for the given files
    static String ID_COLUMN_NAME = "MMSID"

    static final String RESOURCES_FOLDER = "ingestion-files-tests/scenario-first-section-code-match-parent-grouping-with-edition"
    static final String IMPORT_PARAMETERS_FILENAME = "test-newspaper-types.json"
    static final String NEWSPAPER_TYPE = "stuff"

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
        TestHelper.initializeTestMethod(testMethodState, "FirstSectionCodeRequiredForMatchPGWETest-", forLocalFilesystem)

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
        TestHelper.initializeTestMethod(testMethodState, "FirstSectionCodeRequiredForMatchPGWETest-", forLocalFilesystem)

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
        List<NewspaperProcessingParameters> parametersList = NewspaperProcessingParameters.build("TST",
                [ProcessingType.ParentGrouping, ProcessingType.ParentGroupingWithEdition ], sourceFolder,
                processingDate, testMethodState.newspaperSpreadsheet, testMethodState.newspaperType)

        assertThat("Two FairfaxProcessingParameters are returned, size=${parametersList.size()}",
                parametersList.size(), is(2))

        SipProcessingState originalSipProcessingState = testMethodState.sipProcessingState

        parametersList.each { NewspaperProcessingParameters processingParameters ->

            String mmsid = processingParameters.spreadsheetRow.get("MMSID")
            switch (mmsid) {
                case "test-mms-id-one":
                    assertThat("Processing type is parent_grouping", processingParameters.type,
                            is(ProcessingType.ParentGrouping))
                    assertThat("Section codes are [ 'PB1', 'ZOO' ]", processingParameters.sectionCodes, is(["PB1", "ZOO"]))
                    break
                case "test-mms-id-zoo":
                    assertThat("Processing type is parent_grouping_with_edition", processingParameters.type,
                            is(ProcessingType.ParentGroupingWithEdition))
                    assertThat("Section codes are [ 'ZOO' ]", processingParameters.sectionCodes, is(["ZOO"]))
                    break
                default:
                    assertTrue("The mmsid=${mmsid} should have been matched", false)
                    break
            }

            testMethodState.sipProcessingState = originalSipProcessingState.clone()
            processingParameters.sipProcessingState = testMethodState.sipProcessingState
            NewspaperFilesProcessor.processCollectedFiles(processingParameters, filesForProcessing, NEWSPAPER_TYPE,
                    testMethodState.newspaperType)
            String sipAsXml = processingParameters.sipProcessingState.sipAsXml

            switch (mmsid) {
                case "test-mms-id-one":
                    assertTrue("processingParameters is skip", processingParameters.skip)
                    break
                case "test-mms-id-zoo":
                    assertFalse("processingParameters is NOT skip", processingParameters.skip)
                    break
                default:
                    assertTrue("The mmsid=${mmsid} should have been matched", false)
                    break
            }

            if (!processingParameters.skip) {
                // We expect that the ZOO parameters will produce these results
                log.info("${System.lineSeparator()}FairfaxProcessingParameters and SipProcessingState:")
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

//                if (processingParameters.options.contains(ProcessingOption.GenerateProcessedPdfThumbnailsPage) &&
//                        processingParameters.options.contains(ProcessingOption.AlwaysGenerateThumbnailPage)) {
//                    assertTrue("Thumbnail page exists, file=${processingParameters.thumbnailPageFile.normalize()}",
//                            Files.exists(processingParameters.thumbnailPageFile))
//                    // We delete the file because we don't want it sticking around after the test
//                    // Comment out the following line if you want to view the file
//                    Files.deleteIfExists(processingParameters.thumbnailPageFile)
//                } else {
//                    assertNull("Thumbnail page DOES NOT exist, file=${processingParameters.thumbnailPageFile}",
//                            processingParameters.thumbnailPageFile)
//                }

                log.info("STARTING SIP validation")
                sipConstructedCorrectly(sipAsXml)
                log.info("ENDING SIP validation")
                log.info("Process output path=${testMethodState.processOutputInterceptor.path}")
                Path processingStateFilePath = testMethodState.sipProcessingState.toTempFile()
                log.info("sipProcessingState file path=${processingStateFilePath}")
                testMethodState.processOutputInterceptor.stopAndClose()
                // In a normal processing script, the processed files, the processing output and the sipProcessingState file
                // would be moved/copied to a processing completed directory based on the processing state.
            }
        }
    }

    void sipConstructedCorrectly(String sipXml) {
        SipXmlExtractor sipForValidation = new SipXmlExtractor(sipXml)

        assertTrue("SipXmlExtractor has content", sipForValidation.xml.length() > 0)

        assertTrue("SipProcessingState is complete", testMethodState.sipProcessingState.isComplete())
        assertTrue("SipProcessingState is successful", testMethodState.sipProcessingState.isSuccessful())

        TestHelper.assertExpectedSipMetadataValues(sipForValidation, "Test Publication Zoo", "2018", "11", "23 [ZN]",
                IEEntityType.NewspaperIE, "ALMAMMS", "test-mms-id-zoo", "200",
                "PRESERVATION_MASTER", "VIEW", true, 1)

        TestHelper.assertExpectedSipFileValues(sipForValidation, 1, "TST-ZOO-ZN-20181123-001.pdf", "TST-ZOO-ZN-20181123-001.pdf",
                636L, "MD5", "7273a4d61a8dab92be4393e2923ad2d2", "0001", "application/pdf")

        TestHelper.assertExpectedSipFileValues(sipForValidation, 2, "TST-ZOO-ZN-20181123-002.pdf", "TST-ZOO-ZN-20181123-002.pdf",
                636L, "MD5", "7273a4d61a8dab92be4393e2923ad2d2", "0002", "application/pdf")

        TestHelper.assertExpectedSipFileValues(sipForValidation, 3, "TST-ZOO-ZN-20181123-003.pdf", "TST-ZOO-ZN-20181123-003.pdf",
                636L, "MD5", "7273a4d61a8dab92be4393e2923ad2d2", "0003", "application/pdf")

        TestHelper.assertExpectedSipFileValues(sipForValidation, 4, "TST-ZOO-ZN-20181123-004.pdf", "TST-ZOO-ZN-20181123-004.pdf",
                636L, "MD5", "7273a4d61a8dab92be4393e2923ad2d2", "0004", "application/pdf")
    }
}
