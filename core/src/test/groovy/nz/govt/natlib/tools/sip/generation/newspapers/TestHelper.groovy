package nz.govt.natlib.tools.sip.generation.newspapers

import groovy.util.logging.Log4j2
import groovy.xml.slurpersupport.GPathResult
import nz.govt.natlib.tools.sip.IEEntityType
import nz.govt.natlib.tools.sip.extraction.SipXmlExtractor
import nz.govt.natlib.tools.sip.utils.FilesFinder
import nz.govt.natlib.tools.sip.generation.parameters.Spreadsheet
import nz.govt.natlib.tools.sip.processing.ProcessOutputInterceptor
import nz.govt.natlib.tools.sip.state.SipProcessingException
import nz.govt.natlib.tools.sip.state.SipProcessingExceptionReason
import nz.govt.natlib.tools.sip.state.SipProcessingExceptionReasonType
import nz.govt.natlib.tools.sip.state.SipProcessingState
import org.apache.commons.io.FilenameUtils

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import static org.hamcrest.core.Is.is
import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertThat
import static org.junit.Assert.assertTrue

/**
 * Useful methods for use across different unit tests.
 *
 */
@Log4j2
class TestHelper {
    static final String RESOURCES_FOLDER = "nz/govt/natlib/tools/sip/generation/newspapers"


    static class TestMethodState {
        String idColumnName
        String resourcesFolder
        String importParametersFilename
        String importParametersNewspaperType
        SipProcessingState sipProcessingState
        ProcessOutputInterceptor processOutputInterceptor
        String localPath
        String resourcePath
        NewspaperType newspaperType
        NewspaperSpreadsheet newspaperSpreadsheet

        TestMethodState(String idColumnName, String resourcesFolder, String importParametersFilename, String newspaperType) {
            this.idColumnName = idColumnName
            this.resourcesFolder = resourcesFolder
            this.importParametersFilename = importParametersFilename
            this.importParametersNewspaperType = newspaperType
        }
    }

    static void initializeTestMethod(TestMethodState testMethodState, String filePrefix, boolean forLocalFilesystem) {
        testMethodState.sipProcessingState = new SipProcessingState()
        testMethodState.processOutputInterceptor = ProcessOutputInterceptor.forTempFile(filePrefix,
                ".txt", false)
        testMethodState.sipProcessingState.processingOutputPath = testMethodState.processOutputInterceptor.path
        testMethodState.processOutputInterceptor.start()

        if (forLocalFilesystem) {
            testMethodState.localPath = "src/test/resources/${testMethodState.resourcesFolder}"

            String unconvertedPath = "${testMethodState.localPath}/${testMethodState.importParametersFilename}"
            String convertedPath = FilenameUtils.separatorsToSystem(unconvertedPath)
            File spreadsheetFile = new File(convertedPath)

            Spreadsheet spreadsheet = Spreadsheet.fromJson(testMethodState.idColumnName, spreadsheetFile.text, true, true)
            testMethodState.newspaperSpreadsheet = new NewspaperSpreadsheet(spreadsheet)
        } else {
            testMethodState.resourcePath = "${testMethodState.resourcesFolder}"
            testMethodState.localPath = "src/test/resources/${testMethodState.resourcesFolder}"
            testMethodState.newspaperType = new NewspaperType(testMethodState.importParametersNewspaperType,
                    "${testMethodState.localPath}/${testMethodState.importParametersFilename}")
            testMethodState.newspaperSpreadsheet = loadSpreadsheet(testMethodState.resourcePath, testMethodState.localPath,
                    testMethodState.newspaperType.PATH_TO_SPREADSHEET, testMethodState.idColumnName)
        }
    }

    /**
     * Returns the contents of the file from the given filename and resources folder.
     * Make an attempt to open the file as a resource.
     * If that fails, try to open the file with the path resourcesFolder/filename. This should be relative
     * to the current working directory if the the resourcesFolder is a relative path.
     *
     * @param filename
     * @param resourcesFolder
     * @return
     */
    static String getTextFromResourceOrFile(String filename, String resourcesFolder = RESOURCES_FOLDER) {
        String resourcePath = "${resourcesFolder}/${filename}"
        String localPath = "src/test/resources/${resourcePath}"

        String text
        InputStream inputStream = TestHelper.class.getResourceAsStream(filename)
        if (inputStream == null) {
            File inputFile = new File(localPath)
            if (!inputFile.exists()) {
                inputFile = new File(new File(""), localPath)
            }
            text = inputFile.text
        } else {
            text = inputStream.text
        }
        return text
    }

    /**
     * Returns the file from the given filename and resources folder.
     * Make an attempt to open the file as a resource.
     * If that fails, try to open the file with the path resourcesFolder/filename. This should be relative
     * to the current working directory if the resourcesFolder is a relative path.
     *
     * @param filename
     * @param resourcesFolder
     * @return
     */
    static File getFileFromResourceOrFile(String filename, String resourcesFolder = RESOURCES_FOLDER) {
        String resourcePath = "${resourcesFolder}/${filename}"
        String localPath = "src/test/resources/${resourcePath}"

        URL resourceURL = TestHelper.class.getResource(filename)
        File resourceFile
        if (resourceURL != null) {
            resourceFile = new File(resourceURL.getFile())
        }
        if (resourceFile != null && (resourceFile.isFile() || resourceFile.isDirectory())) {
            return resourceFile
        } else {
            File returnFile = new File(localPath)
            return returnFile
        }
    }

    /**
     * When loading files from a resource path, we assume that there aren't that many files (tens rather than thousands)
     * so we use use the traditional java.io approach to listing files.
     *
     * @param folderResourcePath
     * @return
     */
    static List<Path> getResourceFiles(String folderResourcePath, boolean isRegexNotGlob, boolean matchFilenameOnly,
                                        String pattern) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader()
        URL url = loader.getResource(folderResourcePath)
        String path = url.getPath()

        List<Path> files = new File(path).listFiles().collect { File file -> file.toPath() }
        log.info("All files:")
        files.each { Path file ->
            log.info("folderResourcePath=${folderResourcePath} found file=${file.normalize().toString()}")
        }
        if (!isRegexNotGlob) {
            throw new RuntimeException("Globbing not supported for finding resource files, use a regex pattern instead")
        }
        List<Path> filteredFiles
        if (matchFilenameOnly) {
            filteredFiles = files.findAll { Path file ->
                file.fileName.toString() ==~ /${pattern}/
            }
        } else {
            filteredFiles = files.findAll { Path file ->
                file.normalize().toString() ==~ /${pattern}/
            }
        }
        return filteredFiles
    }

    static List<Path> getFilesForProcessingFromFileSystem(boolean isRegexNotGlob, boolean matchFilenameOnly, boolean sortFiles,
                                                   String localPath, String pattern) {
        List<Path> filesForProcessing = [ ]
        Path filesPath = Paths.get(localPath)
        if (!Files.exists(filesPath) || !Files.isDirectory(filesPath)) {
            log.warn("Path '${filesPath}' does not exist is not a directory. Returning empty file list.")
        } else {
            filesForProcessing = FilesFinder.getMatchingFiles(filesPath, isRegexNotGlob, matchFilenameOnly,
                    sortFiles, pattern)
        }

        log.info("Collected ${filesForProcessing.size()} files for processing")
        filesForProcessing.each { Path file ->
            log.info("File for processing=${file.normalize()}")
        }

        return filesForProcessing
    }

    static List<Path> getFilesForProcessingFromResource(boolean isRegexNotGlob, boolean matchFilenameOnly, boolean sortFiles,
                                                        String resourcePath, String localPath, String pattern) {
        List<Path> filesForProcessing = findFiles(resourcePath, localPath, isRegexNotGlob, matchFilenameOnly,
                sortFiles, pattern)

        log.info("Collected ${filesForProcessing.size()} files for processing")
        filesForProcessing.each { Path file ->
            log.info("File for processing=${file.normalize()}")
        }

        return filesForProcessing
    }

    static List<Path> getMatchingFiles(Collection<Path> files, String pattern) {
        return files.findAll { Path file ->
            file.normalize().toString() ==~ /${pattern}/
        }
    }

    static NewspaperSpreadsheet loadSpreadsheet(String resourcePath, String localPath, String importParametersFilename, String idColumnName) {
        Spreadsheet spreadsheet
        InputStream defaultSpreadsheetInputStream = NewspaperSpreadsheet.getResourceAsStream(resourcePath)
        if (defaultSpreadsheetInputStream == null) {
            File spreadsheetFile = new File("${localPath}/${importParametersFilename}")
            spreadsheet = Spreadsheet.fromJson(idColumnName, spreadsheetFile.text, true, true)
        } else {
            spreadsheet = Spreadsheet.fromJson(idColumnName, defaultSpreadsheetInputStream.text, true, true)
        }
        NewspaperSpreadsheet newspaperSpreadsheet = new NewspaperSpreadsheet(spreadsheet)

        return newspaperSpreadsheet
    }

    // TODO Could handle more than one pattern (see https://www.javacodegeeks.com/2012/11/java-7-file-filtering-using-nio-2-part-2.html)
    static List<Path> findFiles(String resourcePath, String localPath, boolean isRegexNotGlob, boolean matchFilenameOnly,
                                boolean sortFiles, String pattern) {
        List<Path> filesList = [ ]
        // We check if we're using a resource stream to load the files, otherwise we are loading from the file system
        InputStream doWeChooseAResourceStream = TestHelper.getResourceAsStream(resourcePath)
        if (doWeChooseAResourceStream == null) {
            Path filesPath = Paths.get(localPath)
            if (!Files.exists(filesPath) || !Files.isDirectory(filesPath)) {
                log.warn("Path '${filesPath}' does not exist is not a directory. Returning empty file list.")
                return filesList
            }

            filesList = FilesFinder.getMatchingFiles(filesPath, isRegexNotGlob, matchFilenameOnly, sortFiles, pattern)
            return filesList
        } else {
            List<Path> files = getResourceFiles(resourcePath, isRegexNotGlob, matchFilenameOnly, pattern)
            filesList = getMatchingFiles(files, pattern)

            return filesList
        }
    }

    static void assertSipProcessingStateFileNumbers(int expectedTotalFilesProcessed, int expectedSipFiles,
                                                    int expectedValidFiles,
                                                    int expectedInvalidFiles, int expectedIgnoredFiles,
                                                    int expectedUnrecognizedFiles, SipProcessingState sipProcessingState) {
        assertThat("expectedTotalFilesProcessed=${expectedTotalFilesProcessed}",
                sipProcessingState.totalFilesProcessed, is(expectedTotalFilesProcessed))
        assertThat("expectedSipFiles=${expectedSipFiles}",
                sipProcessingState.sipFiles.size(), is(expectedSipFiles))
        assertThat("expectedValidFiles=${expectedValidFiles}",
                sipProcessingState.validFiles.size(), is(expectedValidFiles))
        assertThat("expectedInvalidFiles=${expectedInvalidFiles}",
                sipProcessingState.invalidFiles.size(), is(expectedInvalidFiles))
        assertThat("expectedIgnoredFiles=${expectedIgnoredFiles}",
                sipProcessingState.ignoredFiles.size(), is(expectedIgnoredFiles))
        assertThat("expectedUnrecognizedFiles=${expectedUnrecognizedFiles}",
                sipProcessingState.unrecognizedFiles.size(), is(expectedUnrecognizedFiles))
    }

    static void assertExpectedSipMetadataValues(SipXmlExtractor sipForValidation, String title, String dcDate,
                                                String dcTermsAvailable, String dcCoverage, IEEntityType ieEntityType,
                                                String objectIdentifierType, String objectcIdentifierValue,
                                                String policyId, String preservationType, String usageType,
                                                boolean isDigitalOriginal, int revisionNumber, String dcTermsIssued = null) {
        assertThat("title", sipForValidation.extractTitle(), is(title))
        assertThat("dcDate", sipForValidation.extractDcDate(), is(dcDate))
        assertThat("dcTermsAvailable", sipForValidation.extractDcTermsAvailable(), is(dcTermsAvailable))
        if (dcCoverage != null) {
            assertThat("dcCoverage", sipForValidation.extractDcCoverage(), is(dcCoverage))
        }
        assertThat("ieEntityType", sipForValidation.extractIEEntityType(), is(ieEntityType))
        assertThat("objectIdentifierType", sipForValidation.extractObjectIdentifierType(), is(objectIdentifierType))
        assertThat("objectIdentifierValue", sipForValidation.extractObjectIdentifierValue(), is(objectcIdentifierValue))
        assertThat("policyId", sipForValidation.extractPolicyId(), is(policyId))
        assertThat("preservationType", sipForValidation.extractPreservationType(), is(preservationType))
        assertThat("usageType", sipForValidation.extractUsageType(), is(usageType))
        assertThat("digitalOriginal", sipForValidation.extractDigitalOriginal(), is(isDigitalOriginal))
        assertThat("revisionNumber", sipForValidation.extractRevisionNumber(), is(revisionNumber))
        if (dcTermsIssued != null) {
            assertThat("dcTermsIssued", sipForValidation.extractIssueNumber(), is(dcTermsIssued))
        }
    }

    static void assertExpectedSipFileValues(SipXmlExtractor sipForValidation, int idIndex, String originalName,
                                            String originalPath, long sizeBytes, String fixityType, String fixityValue,
                                            String fileLabel, String mimeType) {
        GPathResult fileGPath = sipForValidation.extractFileIdRecord(idIndex)
        // NOTE Any unit test errors in this section (such as:
        // java.lang.NoSuchMethodError: org.hamcrest.Matcher.describeMismatch(Ljava/lang/Object;Lorg/hamcrest/Description;)V
        // could indicate that a null value is coming into the test, which could mean that the value is not in the SIP's
        // XML.
        assertThat("fileWrapper${idIndex}.fileOriginalName", sipForValidation.extractFileOriginalName(fileGPath), is(originalName))
        assertThat("fileWrapper${idIndex}.fileOriginalPath", sipForValidation.extractFileOriginalPath(fileGPath), is(originalPath))
        assertThat("fileWrapper${idIndex}.fileSizeBytes", sipForValidation.extractFileSizeBytes(fileGPath), is(sizeBytes))
        assertThat("fileWrapper${idIndex}.fixityType", sipForValidation.extractFileFixityType(fileGPath), is(fixityType))
        assertThat("fileWrapper${idIndex}.fixityValue", sipForValidation.extractFileFixityValue(fileGPath), is(fixityValue))
        assertThat("fileWrapper${idIndex}.label", sipForValidation.extractFileLabel(fileGPath), is(fileLabel))
        assertThat("fileWrapper${idIndex}.mimeType", sipForValidation.extractFileMimeType(fileGPath), is(mimeType))
        // This is dependent on the filesystem, so we can't really test this
        //assertThat("fileWrapper${idIndex}.modificationDate", sipForValidation.extractFileModificationDate(fileGPath), is(LocalDateTime.of(
        //        LocalDate.of(2015, 7, 29),
        //        LocalTime.of(0, 0, 0, 0))))
        //assertThat("fileWrapper${idIndex}.creationDate", sipForValidation.extractFileCreationDate(fileGPath), is(LocalDateTime.of(
        //        LocalDate.of(2015, 7, 29),
        //        LocalTime.of(0, 0, 0, 0))))

    }

    static void assertExpectedExceptionReason(SipProcessingState sipProcessingState, SipProcessingExceptionReasonType type) {
        assertFalse("SipProcessingState is NOT successful", sipProcessingState.isSuccessful())
        assertTrue("SipProcessingState has exceptions", sipProcessingState.exceptions.size() > 0)
        SipProcessingException firstException = sipProcessingState.exceptions.first()
        assertTrue("SipProcessingException has reasons", firstException.reasons.size() > 0)
        SipProcessingExceptionReason firstExceptionReason = firstException.reasons.first()
        assertThat("SipProcessingState firstExceptionReason type is ${type}", firstExceptionReason.reasonType, is(type))
    }
}
