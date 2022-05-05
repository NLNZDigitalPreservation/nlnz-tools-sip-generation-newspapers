package nz.govt.natlib.tools.sip.generation.newspapers.processor

import groovy.util.logging.Log4j2
import nz.govt.natlib.tools.sip.IEEntityType
import nz.govt.natlib.tools.sip.Sip
import nz.govt.natlib.tools.sip.SipFileWrapperFactory
import nz.govt.natlib.tools.sip.generation.SipXmlGenerator
import nz.govt.natlib.tools.sip.generation.newspapers.parameters.ProcessingOption
import nz.govt.natlib.tools.sip.generation.newspapers.parameters.ProcessingRule
import nz.govt.natlib.tools.sip.generation.newspapers.parameters.ProcessingType
import nz.govt.natlib.tools.sip.generation.newspapers.NewspaperProcessingParameters
import nz.govt.natlib.tools.sip.generation.newspapers.NewspaperSpreadsheet
import nz.govt.natlib.tools.sip.generation.newspapers.NewspaperFile
import nz.govt.natlib.tools.sip.generation.newspapers.NewspaperType
import nz.govt.natlib.tools.sip.generation.newspapers.SipFactory
import nz.govt.natlib.tools.sip.generation.newspapers.processor.type.SipForFolderProcessor
import nz.govt.natlib.tools.sip.generation.newspapers.special.PageUnavailableWriter
import nz.govt.natlib.tools.sip.generation.newspapers.processor.type.ParentGroupingProcessor
import nz.govt.natlib.tools.sip.generation.newspapers.processor.type.ParentGroupingWithEditionProcessor
import nz.govt.natlib.tools.sip.generation.newspapers.processor.type.SupplementGroupingProcessor
import nz.govt.natlib.tools.sip.logging.JvmPerformanceLogger
import nz.govt.natlib.tools.sip.pdf.PdfValidator
import nz.govt.natlib.tools.sip.pdf.PdfValidatorFactory
import nz.govt.natlib.tools.sip.pdf.PdfValidatorType
import nz.govt.natlib.tools.sip.state.SipProcessingException
import nz.govt.natlib.tools.sip.state.SipProcessingExceptionReason
import nz.govt.natlib.tools.sip.state.SipProcessingExceptionReasonType
import nz.govt.natlib.tools.sip.state.SipProcessingState

import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDate
import org.apache.commons.io.FilenameUtils

/**
 * The main processing class. Takes a given set of processing parameters and
 * a set of files and processes them according to the parameters.
 */
@Log4j2
class NewspaperFilesProcessor {
    NewspaperProcessingParameters processingParameters
    List<Path> filesForProcessing
    NewspaperType newspaperType

    Map<NewspaperFile, NewspaperFile> processedNewspaperFiles

    static Sip getBlankSip() {
        Sip sip = new Sip(title: 'UNKNOWN_TITLE', ieEntityType: IEEntityType.UNKNOWN,
                objectIdentifierType: 'UNKNOWN_OBJECT_IDENTIFIER_TYPE',
                objectIdentifierValue: 'UNKNOWN_OBJECT_IDENTIFIER_VALUE', policyId: 'UNKNOWN_POLICY_ID',
                preservationType: 'UNKNOWN_PRESERVATION_TYPE', usageType: 'UNKNOWN_USAGE_TYPE',
                digitalOriginal: true, revisionNumber: 1,
                year: 2038, month: 12, dayOfMonth: 31)
        sip.updateFromDateFields()
        return sip
    }

    static void processCollectedFiles(NewspaperProcessingParameters processingParameters,
                                      List<Path> filesForProcessing,
                                      String newspaperType) {
        NewspaperFilesProcessor newspaperFilesProcessor = new NewspaperFilesProcessor(processingParameters,
                filesForProcessing, newspaperType)
        if (processingParameters.rules.contains(ProcessingRule.ForceSkip)) {
            log.info("Skipping processing sourceFolder=${processingParameters.sourceFolder.normalize()} as processing rules include=${ProcessingRule.ForceSkip.fieldValue}")
            processingParameters.skip = true
            processingParameters.sipProcessingState.sipAsXml = SipProcessingState.EMPTY_SIP_AS_XML
            return
        }
        newspaperFilesProcessor.process()
    }

    NewspaperFilesProcessor(NewspaperProcessingParameters processingParameters, List<Path> filesForProcessing, String type) {
        this.processingParameters = processingParameters
        this.filesForProcessing = filesForProcessing
        this.newspaperType = new NewspaperType(type)
    }

    void process() {
        log.info("STARTING process for processingParameters=${processingParameters}")
        JvmPerformanceLogger.logState("NewspaperFilesProcessor Current thread state at start",
                false, true, true, false, true, false, true)
        processedNewspaperFiles = [ : ]

        if (this.processingParameters.valid) {
            List<NewspaperFile> newspaperFilesForProcessing = filesForProcessing.collect { Path rawFile ->
                new NewspaperFile(rawFile, this.newspaperType)
            }
            List<NewspaperFile> validNamedFiles = extractValidNamedFiles(newspaperFilesForProcessing)

            List<NewspaperFile> sortedFilesForProcessing
            switch (processingParameters.type) {
                case ProcessingType.ParentGrouping:
                    sortedFilesForProcessing = ParentGroupingProcessor.selectAndSort(processingParameters, validNamedFiles, newspaperType)
                    break
                case ProcessingType.ParentGroupingWithEdition:
                    sortedFilesForProcessing = ParentGroupingWithEditionProcessor.selectAndSort(processingParameters, validNamedFiles, newspaperType)
                    break
                case ProcessingType.SupplementGrouping:
                    sortedFilesForProcessing = SupplementGroupingProcessor.selectAndSort(processingParameters, validNamedFiles)
                    break
                case ProcessingType.CreateSipForFolder:
                    sortedFilesForProcessing = SipForFolderProcessor.selectAndSort(processingParameters, validNamedFiles)
                    if (processingParameters.spreadsheetRow == NewspaperSpreadsheet.BLANK_ROW) {
                        String detailedReason = "No matching spreadsheet row for titleCode=${processingParameters.titleCode}, " +
                                "date=${processingParameters.date}, folder=${processingParameters.sourceFolder.normalize()}."
                        SipProcessingExceptionReason exceptionReason = new SipProcessingExceptionReason(
                                SipProcessingExceptionReasonType.NO_MATCHING_SIP_DEFINITION, null,
                                detailedReason)
                        SipProcessingException sipProcessingException = SipProcessingException.createWithReason(exceptionReason)
                        processingParameters.sipProcessingState.addException(sipProcessingException)
                        log.warn(detailedReason)
                    } else {
                        // TODO Should this be an error
                        log.warn("ProcessingType.CreateSipForFolder should have SipProcessingException reason.")
                    }
                    break
                default:
                    sortedFilesForProcessing = []
                    break
            }

            if (processingParameters.skip) {
                log.info("Skipping processing for processingParameters=${processingParameters}")
            } else {
                List<NewspaperFile> sipFiles = differentiateFiles(validNamedFiles, sortedFilesForProcessing)

                String sipAsXml = generateSipAsXml(sipFiles, processingParameters.date)
                processingParameters.sipProcessingState.sipAsXml = sipAsXml
            }
        }

        JvmPerformanceLogger.logState("NewspaperFilesProcessor Current thread state at end",
                false, true, true, false, true, false, true)
        log.info("ENDING process for processingParameters=${processingParameters}")
    }

    List<NewspaperFile> differentiateFiles(List<NewspaperFile> validNamedFiles, List<NewspaperFile> sortedFilesForProcessing) {
        processingParameters.sipProcessingState.ignoredFiles =
                NewspaperFile.differences(validNamedFiles, sortedFilesForProcessing).collect { NewspaperFile ffxFile ->
                    ffxFile.file
                }

        if (processingParameters.sipProcessingState.ignoredFiles.size() > 0 &&
                processingParameters.rules.contains(ProcessingRule.AllSectionsInSipRequired)) {
            // Strip the ignored of any editionDiscriminator files
            List<NewspaperFile> withoutEditionFiles = processingParameters.sipProcessingState.ignoredFiles.findAll {
                Path file ->
                    NewspaperFile newspaperFile = new NewspaperFile(file, this.newspaperType)
                    !processingParameters.editionDiscriminators.contains(newspaperFile.sectionCode)
            }
            if (!withoutEditionFiles.isEmpty()) {
                String detailedReason = "${ProcessingRule.AllSectionsInSipRequired.fieldValue} but these files are not processed=${withoutEditionFiles}".toString()
                if (processingParameters.rules.contains(ProcessingRule.AllSectionsInSipRequired)) {
                    SipProcessingExceptionReason exceptionReason = new SipProcessingExceptionReason(
                            SipProcessingExceptionReasonType.ALL_FILES_CANNOT_BE_PROCESSED, null,
                            detailedReason)
                    SipProcessingException sipProcessingException = SipProcessingException.createWithReason(exceptionReason)
                    processingParameters.sipProcessingState.addException(sipProcessingException)
                    log.warn(detailedReason)
                } else {
                    log.info(detailedReason)
                }
            }
        }
        sortedFilesForProcessing.each { NewspaperFile fileForProcessing ->
            processFile(fileForProcessing)
        }
        // TODO We are converting back and forth between NewspaperFile and File for different processing stages to
        // ensure that the sip-generation-core classes don't get polluted with Newspaper-specific functionality.
        // At some point we need to look at finding a better way. Perhaps there's an interface that might expose
        // a wrapper so that it can be processed through implementation-specific processing.
        // For the moment we do the conversion. This is something to consider when refactoring/redesigning this
        // application.
        List<NewspaperFile> sipFiles = processingParameters.sipProcessingState.sipFiles.collect { Path file ->
            new NewspaperFile(file, this.newspaperType)
        }
        checkForMissingSequenceFiles(sipFiles)
        checkForManualProcessing()

        return sipFiles
    }

    List<NewspaperFile> extractValidNamedFiles(List<NewspaperFile> originalList) {
        List<NewspaperFile> cleanedList = [ ]
        originalList.each { NewspaperFile newspaperFile ->
            if (newspaperFile.isValidName()) {
                cleanedList.add(newspaperFile)
            } else {
                SipProcessingExceptionReason exceptionReason = new SipProcessingExceptionReason(
                        SipProcessingExceptionReasonType.INVALID_PAGE_FILENAME, null,
                        newspaperFile.file.normalize().toString())
                SipProcessingException sipProcessingException = SipProcessingException.createWithReason(exceptionReason)
                processingParameters.sipProcessingState.addException(sipProcessingException)
                log.warn(sipProcessingException.toString())
                processingParameters.sipProcessingState.unrecognizedFiles.add(newspaperFile.file)
            }
        }
        return cleanedList
    }

    void processFile(NewspaperFile newspaperFile) {
        log.info("Processing newspaperFile=${newspaperFile}")
        // We generally include all files whether they are valid or invalid. We don't include duplicate files.
        SipProcessingState sipProcessingState = processingParameters.sipProcessingState

        boolean includeFileInSip = true
        if (processedNewspaperFiles.containsKey(newspaperFile)) {
            // We have a duplicate file -- possibly a different qualifier
            // We use the newspaper file as a key, but we'll get the duplicate back
            NewspaperFile firstVersion = processedNewspaperFiles.get(newspaperFile)
            SipProcessingExceptionReason exceptionReason = new SipProcessingExceptionReason(
                    SipProcessingExceptionReasonType.DUPLICATE_FILE, null,
                    firstVersion.file.normalize().toString(), newspaperFile.file.normalize().toString())
            sipProcessingState.addException(SipProcessingException.createWithReason(exceptionReason))
            includeFileInSip = false
        } else {
            processedNewspaperFiles.put(newspaperFile, newspaperFile)
            if (Files.size(newspaperFile.file) == 0) {
                SipProcessingExceptionReason exceptionReason = new SipProcessingExceptionReason(
                        SipProcessingExceptionReasonType.FILE_OF_LENGTH_ZERO, null,
                        newspaperFile.file.normalize().toString())
                newspaperFile.zeroLengthFile = true
                if (processingParameters.rules.contains(ProcessingRule.ZeroLengthPdfReplacedWithPageUnavailablePdf)) {
                    Path replacementFile = PageUnavailableWriter.writeToToTemporaryDirectory(newspaperFile.file.fileName.toString())
                    newspaperFile.originalFile = newspaperFile.file
                    newspaperFile.file = replacementFile
                }
                processingParameters.sipProcessingState.addException(SipProcessingException.createWithReason(exceptionReason))
            } else {
                // We use the Jhove validator as it is the same type used by Rosetta.
                // There is also a PDF/A validator using the PdfValidatorType.PDF_BOX_VALIDATOR
                PdfValidator pdfValidator = PdfValidatorFactory.getValidator(PdfValidatorType.JHOVE_VALIDATOR)
                SipProcessingException sipProcessingException = pdfValidator.validatePdf(newspaperFile.file)
                if (sipProcessingException != null) {
                    processingParameters.sipProcessingState.addException(sipProcessingException)
                } else {
                    newspaperFile.validPdf = true
                    newspaperFile.validForProcessing = true
                }
            }
        }
        if (newspaperFile.validPdf && newspaperFile.validForProcessing) {
            processingParameters.sipProcessingState.validFiles.add(newspaperFile.file)
        } else {
            processingParameters.sipProcessingState.invalidFiles.add(newspaperFile.originalFileOrFile)
        }
        if (includeFileInSip) {
            sipProcessingState.sipFiles.add(newspaperFile.file)
        }
    }

    String generateSipAsXml(List<NewspaperFile> sortedNewspaperFiles, LocalDate sipDate) {
        String sipAsXml = ""
        if (sortedNewspaperFiles.isEmpty()) {
            String detailedReason = "Unable to process processingParameters=${processingParameters}: No matching files."
            SipProcessingExceptionReason exceptionReason = new SipProcessingExceptionReason(
                    SipProcessingExceptionReasonType.NO_MATCHING_SIP_DEFINITION, null,
                    detailedReason)
            SipProcessingException sipProcessingException = SipProcessingException.createWithReason(exceptionReason)
            processingParameters.sipProcessingState.addException(sipProcessingException)
            log.warn(detailedReason)
        } else {
            String titleKey = processingParameters.type == ProcessingType.SupplementGrouping ?
                    SipFactory.TITLE_METS_KEY :
                    SipFactory.TITLE_PARENT_KEY
            Sip sip = SipFactory.fromMap(processingParameters.spreadsheetRow, [ ], false, false, titleKey)

            sip.year = sipDate.year
            sip.month = sipDate.monthValue
            sip.dayOfMonth = sipDate.dayOfMonth
            sip.updateFromDateFields()

            if (processingParameters.includeCurrentEditionForDcCoverage) {
                sip.dcCoverage = "${sipDate.dayOfMonth} [${processingParameters.currentEdition}]"
            }
            processingParameters.sipProcessingState.ieEntityType = sip.ieEntityType
            processingParameters.sipProcessingState.identifier = formatSipProcessingStateIdentifier()

            List<Path> filesForSip = sortedNewspaperFiles.collect() { NewspaperFile newspaperFile ->
                newspaperFile.file
            }
            Sip testSip = sip.clone()
            sipAsXml = generateSipAsXml(testSip, filesForSip)
            processingParameters.sipProcessingState.totalFilesProcessed = filesForSip.size()
            processingParameters.sipProcessingState.setComplete(true)
            log.debug("\n* * *   S I P   * * *")
            log.debug(sipAsXml)
            log.debug("\n* * *   E N D   O F   S I P   * * *")
        }

        return sipAsXml
    }

    String generateSipAsXml(Sip sip, List<Path> files) {
        List<Sip.FileWrapper> fileWrappers = files.collect() { Path file ->
            SipFileWrapperFactory.generate(file, true, true)
        }
        int sequenceNumber = 1
        fileWrappers.each { Sip.FileWrapper fileWrapper ->
            String label
            if (processingParameters.rules.contains(ProcessingRule.UseFileNameForMetsLabel)) {
                label = FilenameUtils.removeExtension(fileWrapper.fileOriginalName)
            } else {
                label = String.format("%04d", sequenceNumber)
                sequenceNumber += 1
            }
            fileWrapper.label = label
        }
        sip.fileWrappers = fileWrappers
        SipXmlGenerator sipXmlGenerator = new SipXmlGenerator(sip)

        return sipXmlGenerator.getSipAsXml()
    }

    String formatSipProcessingStateIdentifier() {

        String titleWithUnderscores
        if (processingParameters.type == ProcessingType.SupplementGrouping) {
            // supplement_grouping needs to add a few more things for uniqueness
            // as it's possible for multiple supplements to have the same title code
            String sectionCodesString = processingParameters.sectionCodesString.
                    replace("+", "-").replace(",", "-")
            String titleAndId = processingParameters.titleMets.strip().replace(' ', '_') +
                    "_" + processingParameters.mmsid
            titleWithUnderscores = sectionCodesString.isEmpty() ? titleAndId : "${sectionCodesString}_${titleAndId}"
        } else {
            String title = processingParameters.getTitleParent()
            titleWithUnderscores = title.trim().replace(' ', '_')
        }
        if (processingParameters.currentEdition == null) {
            return "_${titleWithUnderscores}"
        } else {
            return "${processingParameters.currentEdition}__${titleWithUnderscores}"
        }
    }

    void checkForMissingSequenceFiles(List<NewspaperFile> checkList) {
        if (processingParameters.rules.contains(ProcessingRule.MissingSequenceError) &&
            processingParameters.rules.contains(ProcessingRule.IsMultiPdfFiles)) {
            List<NewspaperFile> postMissingSequenceFiles = NewspaperFile.postMissingSequenceFiles(checkList,
                    processingParameters)
            if (postMissingSequenceFiles.size() > 0) {
                boolean hasMissingFiles = true
                if (processingParameters.rules.contains(ProcessingRule.MissingSequenceDoubleWideIgnored)) {
                    hasMissingFiles = notAllMissingFilesAreDoubleWides(checkList, postMissingSequenceFiles)
                }
                if (hasMissingFiles) {
                    List<String> filenamesOnly = NewspaperFile.asFilenames(postMissingSequenceFiles)
                    String listOfFiles = "${filenamesOnly}".toString()
                    SipProcessingExceptionReason exceptionReason = new SipProcessingExceptionReason(
                            SipProcessingExceptionReasonType.MISSING_SEQUENCE_FILES, null,
                            listOfFiles)
                    SipProcessingException sipProcessingException = SipProcessingException.createWithReason(exceptionReason)
                    processingParameters.sipProcessingState.addException(sipProcessingException)
                    log.warn(exceptionReason.toString())
                }
            }
        }
    }

    boolean notAllMissingFilesAreDoubleWides(List<NewspaperFile> checkList, List<NewspaperFile> postMissingSequenceFiles) {
        NewspaperFile genuineMissingFile = postMissingSequenceFiles.find { NewspaperFile missingFile ->
            !isDoubleWideInSequence(checkList, missingFile)
        }
        return genuineMissingFile != null
    }

    boolean isDoubleWideInSequence(List<NewspaperFile> checkList, NewspaperFile postMissingFile) {
        int postMissingFileIndex = checkList.indexOf(postMissingFile)
        int previousIndex = postMissingFileIndex - 1
        if (previousIndex < 0) {
            // There is no previous file
            return false
        }
        NewspaperFile previousFile = checkList.get(previousIndex)

        // Either the previous file was a double wide or this file is a double wide.
        return previousFile.isSameHeightDoubleWidth(postMissingFile) ||
                previousFile.isSameHeightHalfWidth(postMissingFile)
    }

    void checkForManualProcessing() {
        if (processingParameters.rules.contains(ProcessingRule.Manual)) {
            String reason = "Processing will be redirected to for-review."
            SipProcessingExceptionReason exceptionReason = new SipProcessingExceptionReason(
                    SipProcessingExceptionReasonType.MANUAL_PROCESSING_REQUIRED, null,
                    reason)
            SipProcessingException sipProcessingException = SipProcessingException.createWithReason(exceptionReason)
            processingParameters.sipProcessingState.addException(sipProcessingException)
            log.warn(exceptionReason.toString())
        }
    }
}
