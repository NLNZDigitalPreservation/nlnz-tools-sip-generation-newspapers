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
 * Tests the {@code parent-grouping-with-edition-multiple-same-day} scenario.
 *
 * Note that this test is complicated by the files either being part of a directory structure or in a resource file (jar),
 * so the {@link TestHelper} class is used to handle both scenarios. In real-life processing the files would be on the
 * filesystem and not in a resource. We explicitly use only filesystem files in
 * {@link #correctlyAssembleSipFromFilesOnFilesystem} (as an example to script writers), but this unit test is
 * ignored for builds.
 */
@RunWith(MockitoJUnitRunner.class)
@Log4j2
class ParentGroupingWithEdition {
    // TODO Make this processing simpler
    // - given a starting folder
    // - and a set of selection criteria
    // - create SIPs for the given files
    static String ID_COLUMN_NAME = "MMSID"

    static final String RESOURCES_FOLDER = "ingestion-files-tests/scenario-parent-grouping-with-edition"
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
        TestHelper.initializeTestMethod(testMethodState, "ParentGroupingWithEditionTest-", forLocalFilesystem)

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
        TestHelper.initializeTestMethod(testMethodState, "ParentGroupingWithEditionTest-", forLocalFilesystem)

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
                [ProcessingType.ParentGroupingWithEdition, ProcessingType.ParentGrouping, ProcessingType.CreateSipForFolder ],
                sourceFolder, processingDate, testMethodState.newspaperSpreadsheet, testMethodState.newspaperType)

        assertThat("2 NewspaperProcessingParameters are returned, size=${parametersList.size()}, list=${parametersList}",
                parametersList.size(), is(2))

        SipProcessingState originalSipProcessingState = testMethodState.sipProcessingState

        parametersList.each { NewspaperProcessingParameters currentProcessingParameters ->

            String discriminatorCode = currentProcessingParameters.editionDiscriminators.first()
            switch (discriminatorCode) {
                case "NT":
                    assertThat("Processing type is ParentGroupingWithEdition", currentProcessingParameters.type,
                            is(ProcessingType.ParentGroupingWithEdition))
                    assertThat("editionDiscriminator matches=NT", discriminatorCode, is("NT"))
                    assertThat ( "Multiple edition codes: 'NT', 'ZN'", currentProcessingParameters.editionCodes,
                            is ( [ 'NT', 'ZN' ] ) )
                    break
                case "ST":
                    assertThat("Processing type is ParentGroupingWithEdition", currentProcessingParameters.type,
                            is(ProcessingType.ParentGroupingWithEdition))
                    assertThat("editionDiscriminator matches=ST", discriminatorCode, is("ST"))
                    assertThat ( "Multiple edition codes: 'ST', 'ZN'", currentProcessingParameters.editionCodes,
                            is ( [ 'ST', 'ZN' ] ) )
                    break
                default:
                    assertFalse("Unrecognized discriminatorCode=${discriminatorCode}", true)
                    break
            }
            assertTrue ( "Numeric before alpha sequencing",
                    currentProcessingParameters.options.contains (ProcessingOption.NumericBeforeAlphaSequencing))

            testMethodState.sipProcessingState = originalSipProcessingState.clone()
            currentProcessingParameters.sipProcessingState = testMethodState.sipProcessingState
            NewspaperFilesProcessor.processCollectedFiles(currentProcessingParameters, filesForProcessing, NEWSPAPER_TYPE,
                    testMethodState.newspaperType)
            String sipAsXml = currentProcessingParameters.sipProcessingState.sipAsXml

//            assertTrue("Processing rules includes EditionDiscriminatorsUsingSmartSubstitute",
//                    currentProcessingParameters.rules.contains(ProcessingRule.EditionDiscriminatorsUsingSmartSubstitute))
//
//            // With rule EditionDiscriminatorsUsingSmartSubstitute, the processing parameters will change depending on
//            // the discriminatorCode. This takes place in the ParentGroupingWithEditionProcessor
//            switch (discriminatorCode) {
//                case "NT":
//                    assertThat("currentEdition matches=NT", currentProcessingParameters.currentEdition, is("NT"))
//                    assertThat("editionDiscriminators matches=[ NT ]", currentProcessingParameters.editionDiscriminators,
//                            is([ "NT" ]))
//                    assertThat ( "Multiple section codes: 'PB1', 'BOO', 'ZOO', 'AAT'", currentProcessingParameters.sectionCodes,
//                            is ( [ 'PB1', 'BOO', 'ZOO', 'AAT' ] ) )
//                    break
//                case "ST":
//                    assertThat("currentEdition matches=ST", currentProcessingParameters.currentEdition, is("ST"))
//                    assertThat("editionDiscriminators matches=[ ST ZN ]", currentProcessingParameters.editionDiscriminators,
//                            is([ "ST", "ZN" ]))
//                    assertThat ( "Multiple section codes: 'PB1', 'BOO', 'ZOO'", currentProcessingParameters.sectionCodes,
//                            is ( [ 'PB1', 'BOO', 'ZOO' ] ) )
//                    break
//                default:
//                    assertFalse("Unrecognized discriminatorCode=${discriminatorCode}", true)
//                    break
//            }

            log.info("${System.lineSeparator()}NewspaperProcessingParameters and SipProcessingState:")
            log.info(currentProcessingParameters.detailedDisplay(0, true))
            log.info(System.lineSeparator())

            switch (discriminatorCode) {
                case "NT" :
                    expectedSizingNT()
                    break
                case "ST" :
                    expectedSizingST()
                    break
                default:
                    assertFalse("Unrecognized discriminatorCode=${discriminatorCode}", true)
                    break
            }

            log.info("STARTING SIP validation")
            switch (discriminatorCode) {
                case "NT" :
                    sipConstructedCorrectlyNTEdition(sipAsXml)
                    break
                case "ST" :
                    sipConstructedCorrectlySTEdition(sipAsXml)
                    break
                case "PP" :
                    assertThat("Empty string for SIP=${sipAsXml}", sipAsXml, is(""))
                    break
                default:
                    assertFalse("Unrecognized discriminatorCode=${discriminatorCode}", true)
                    break
            }

            log.info("ENDING SIP validation")
            log.info("Process output path=${testMethodState.processOutputInterceptor.path}")
            Path processingStateFilePath = testMethodState.sipProcessingState.toTempFile()
            log.info("sipProcessingState file path=${processingStateFilePath}")
            testMethodState.processOutputInterceptor.stopAndClose()
            // In a normal processing script, the processed files, the processing output and the sipProcessingState file
            // would be moved/copied to a processing completed directory based on the processing state.
        }
    }

    void expectedSizingNT() {
        int expectedNumberOfFilesProcessed = 7
        int expectedNumberOfSipFiles = 7
        int expectedNumberOfValidFiles = 7
        int expectedNumberOfInvalidFiles = 0
        int expectedNumberOfIgnoredFiles = 3
        int expectedNumberOfUnrecognizedFiles = 0
        TestHelper.assertSipProcessingStateFileNumbers(expectedNumberOfFilesProcessed, expectedNumberOfSipFiles,
                expectedNumberOfValidFiles, expectedNumberOfInvalidFiles,
                expectedNumberOfIgnoredFiles, expectedNumberOfUnrecognizedFiles, testMethodState.sipProcessingState)
    }

    void expectedSizingST() {
        int expectedNumberOfFilesProcessed = 7
        int expectedNumberOfSipFiles = 7
        int expectedNumberOfValidFiles = 7
        int expectedNumberOfInvalidFiles = 0
        int expectedNumberOfIgnoredFiles = 3
        int expectedNumberOfUnrecognizedFiles = 0
        TestHelper.assertSipProcessingStateFileNumbers(expectedNumberOfFilesProcessed, expectedNumberOfSipFiles,
                expectedNumberOfValidFiles, expectedNumberOfInvalidFiles,
                expectedNumberOfIgnoredFiles, expectedNumberOfUnrecognizedFiles, testMethodState.sipProcessingState)
    }

    void sipConstructedCorrectlyNTEdition(String sipXml) {
        SipXmlExtractor sipForValidation = new SipXmlExtractor(sipXml)

        assertTrue("SipXmlExtractor has content", sipForValidation.xml.length() > 0)

        assertTrue("SipProcessingState is complete", testMethodState.sipProcessingState.isComplete())
        assertTrue("SipProcessingState is successful", testMethodState.sipProcessingState.isSuccessful())

        TestHelper.assertExpectedSipMetadataValues(sipForValidation, "Test Publication One", "2018", "11", "23 [NT]",
                IEEntityType.NewspaperIE, "ALMAMMS", "test-mms-id-one", "200",
                "PRESERVATION_MASTER", "VIEW", true, 1)

        TestHelper.assertExpectedSipFileValues(sipForValidation, 1, "TST-ED1-ZN-20181123-001.pdf", "TST-ED1-ZN-20181123-001.pdf",
                636L, "MD5", "7273a4d61a8dab92be4393e2923ad2d2", "0001", "application/pdf")

        TestHelper.assertExpectedSipFileValues(sipForValidation, 2, "TST-ED1-ZN-20181123-002.pdf", "TST-ED1-ZN-20181123-002.pdf",
                636L, "MD5", "7273a4d61a8dab92be4393e2923ad2d2", "0002", "application/pdf")

        TestHelper.assertExpectedSipFileValues(sipForValidation, 3, "TST-ED1-ZN-20181123-003.pdf", "TST-ED1-ZN-20181123-003.pdf",
                636L, "MD5", "7273a4d61a8dab92be4393e2923ad2d2", "0003", "application/pdf")

        TestHelper.assertExpectedSipFileValues(sipForValidation, 4, "TST-ED1-NT-20181123-004.pdf", "TST-ED1-NT-20181123-004.pdf",
                636L, "MD5", "7273a4d61a8dab92be4393e2923ad2d2", "0004", "application/pdf")

        TestHelper.assertExpectedSipFileValues(sipForValidation, 5, "TST-ED1-ZN-20181123-005.pdf", "TST-ED1-ZN-20181123-005.pdf",
                636L, "MD5", "7273a4d61a8dab92be4393e2923ad2d2", "0005", "application/pdf")

        TestHelper.assertExpectedSipFileValues(sipForValidation, 6, "TST-ED1-NT-20181123-006.pdf", "TST-ED1-NT-20181123-006.pdf",
                636L, "MD5", "7273a4d61a8dab92be4393e2923ad2d2", "0006", "application/pdf")

        TestHelper.assertExpectedSipFileValues(sipForValidation, 7, "TST-ED1-NT-20181123-007.pdf", "TST-ED1-NT-20181123-007.pdf",
                636L, "MD5", "7273a4d61a8dab92be4393e2923ad2d2", "0007", "application/pdf")

    }

    void sipConstructedCorrectlySTEdition(String sipXml) {
        SipXmlExtractor sipForValidation = new SipXmlExtractor(sipXml)

        assertTrue("SipXmlExtractor has content", sipForValidation.xml.length() > 0)

        assertTrue("SipProcessingState is complete", testMethodState.sipProcessingState.isComplete())
        assertTrue("SipProcessingState is successful", testMethodState.sipProcessingState.isSuccessful())

        TestHelper.assertExpectedSipMetadataValues(sipForValidation, "Test Publication Two", "2018", "11", "23 [ST]",
                IEEntityType.NewspaperIE, "ALMAMMS", "test-mms-id-two", "200",
                "PRESERVATION_MASTER", "VIEW", true, 1)

        TestHelper.assertExpectedSipFileValues(sipForValidation, 1, "TST-ED1-ZN-20181123-001.pdf", "TST-ED1-ZN-20181123-001.pdf",
                636L, "MD5", "7273a4d61a8dab92be4393e2923ad2d2", "0001", "application/pdf")

        TestHelper.assertExpectedSipFileValues(sipForValidation, 2, "TST-ED1-ZN-20181123-002.pdf", "TST-ED1-ZN-20181123-002.pdf",
                636L, "MD5", "7273a4d61a8dab92be4393e2923ad2d2", "0002", "application/pdf")

        TestHelper.assertExpectedSipFileValues(sipForValidation, 3, "TST-ED1-ZN-20181123-003.pdf", "TST-ED1-ZN-20181123-003.pdf",
                636L, "MD5", "7273a4d61a8dab92be4393e2923ad2d2", "0003", "application/pdf")

        TestHelper.assertExpectedSipFileValues(sipForValidation, 4, "TST-ED1-ST-20181123-004.pdf", "TST-ED1-ST-20181123-004.pdf",
                636L, "MD5", "7273a4d61a8dab92be4393e2923ad2d2", "0004", "application/pdf")

        TestHelper.assertExpectedSipFileValues(sipForValidation, 5, "TST-ED1-ZN-20181123-005.pdf", "TST-ED1-ZN-20181123-005.pdf",
                636L, "MD5", "7273a4d61a8dab92be4393e2923ad2d2", "0005", "application/pdf")

        TestHelper.assertExpectedSipFileValues(sipForValidation, 6, "TST-ED1-ST-20181123-006.pdf", "TST-ED1-ST-20181123-006.pdf",
                636L, "MD5", "7273a4d61a8dab92be4393e2923ad2d2", "0006", "application/pdf")

        TestHelper.assertExpectedSipFileValues(sipForValidation, 7, "TST-ED1-ST-20181123-007.pdf", "TST-ED1-ST-20181123-007.pdf",
                636L, "MD5", "7273a4d61a8dab92be4393e2923ad2d2", "0007", "application/pdf")    }

}
