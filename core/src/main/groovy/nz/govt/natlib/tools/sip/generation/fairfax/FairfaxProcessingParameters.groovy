package nz.govt.natlib.tools.sip.generation.fairfax

import groovy.transform.AutoClone
import groovy.transform.Canonical
import groovy.transform.ToString
import groovy.util.logging.Log4j2
import nz.govt.natlib.tools.sip.generation.fairfax.parameters.ProcessingOption
import nz.govt.natlib.tools.sip.generation.fairfax.parameters.ProcessingRule
import nz.govt.natlib.tools.sip.generation.fairfax.parameters.ProcessingType
import nz.govt.natlib.tools.sip.state.SipProcessingException
import nz.govt.natlib.tools.sip.state.SipProcessingExceptionReason
import nz.govt.natlib.tools.sip.state.SipProcessingExceptionReasonType
import nz.govt.natlib.tools.sip.state.SipProcessingState
import org.apache.commons.lang3.StringUtils

import java.nio.file.Path
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Encapsulates all the parameters needed to process a set of files.
 */
@Canonical
@ToString(includeNames=true, includePackage=false, excludes=[ 'spreadsheetRow', 'sipProcessingState' ])
@AutoClone(excludes = [ 'currentEdition' ])
@Log4j2
class FairfaxProcessingParameters {
    static DateTimeFormatter READABLE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    static final List<String> IGNORE_EDITIONS_FOR_DC_COVERAGE = [ 'TAB', 'NEL', 'MEX' ]

    boolean valid = true
    boolean skip = false
    String titleCode
    Path sourceFolder
    ProcessingType type
    List<ProcessingRule> rules = [ ]
    List<ProcessingOption> options = [ ]
    LocalDate date
    Map<String, String> spreadsheetRow = [ : ]
    List<String> sectionCodes = [ ]
    List<String> editionDiscriminators = [ ]
    boolean isMagazine = false
    String currentEdition
    SipProcessingState sipProcessingState = new SipProcessingState()
    Path thumbnailPageFile
    String thumbnailPageFileFinalName

    static List<FairfaxProcessingParameters> build(String titleCode, List<ProcessingType> processingTypes, Path sourceFolder,
                                                   LocalDate processingDate, FairfaxSpreadsheet spreadsheet,
                                                   List<ProcessingRule> overrideRules = [],
                                                   List<ProcessingOption> overrideOptions = []) {
        List<FairfaxProcessingParameters> parametersList = [ ]
//        PublicationType publicationType = new PublicationType(publication)
//        String fileFindPattern = publicationType.getPDF_FILE_WITH_TITLE_SECTION_DATE_SEQUENCE_PATTERN()

        processingTypes.sort().each { ProcessingType processingType ->
            List<Map<String, String>> matchingRows = matchingRowsFor(titleCode, processingType, sourceFolder, processingDate, spreadsheet)
            if (processingType == ProcessingType.ParentGroupingWithEdition) {
                matchingRows.each { Map<String, String> singleRow ->
                    FairfaxProcessingParameters candidate = buildForRows(titleCode, processingType, sourceFolder,
                            processingDate, [ singleRow ])
                    candidate.rules = ProcessingRule.mergeOverrides(candidate.rules, overrideRules)
                    candidate.options = ProcessingOption.mergeOverrides(candidate.options, overrideOptions)
                    parametersList.add(candidate)
                }
            } else if (processingType == ProcessingType.SupplementGrouping) {
                matchingRows.each { Map<String, String> singleRow ->
                    FairfaxProcessingParameters candidate = buildForRows(titleCode, processingType, sourceFolder,
                            processingDate, [ singleRow ])
                    candidate.rules = ProcessingRule.mergeOverrides(candidate.rules, overrideRules)
                    candidate.options = ProcessingOption.mergeOverrides(candidate.options, overrideOptions)
                    parametersList.add(candidate)
                }
            } else {
                FairfaxProcessingParameters candidate = buildForRows(titleCode, processingType, sourceFolder, processingDate, matchingRows)
                candidate.rules = ProcessingRule.mergeOverrides(candidate.rules, overrideRules)
                candidate.options = ProcessingOption.mergeOverrides(candidate.options, overrideOptions)
                if (candidate.valid) {
                    if (processingType == ProcessingType.CreateSipForFolder) {
                        if (parametersList.isEmpty()) {
                            parametersList.add(candidate)
                        } else {
                            log.debug("Ignoring processingType=${processingType}, valid candidate=${candidate}, as other processing types have matched.")
                        }
                    } else {
                        parametersList.add(candidate)
                    }
                } else {
                    // TODO This seems like a filter chain
                    if (parametersList.isEmpty()) {
                        if (processingType == ProcessingType.ParentGroupingWithEdition && processingTypes.size() > 1) {
                            // ParentGroupingWithEdition is always first in the list, so if there are others, they may match
                            log.debug("Ignoring processingType=${processingType}, invalid candidate=${candidate}, as other processing types may match.")
                        } else if (processingType == ProcessingType.ParentGrouping) {
                            if (candidate.sipProcessingState.exceptions.first().reasons.first().reasonType ==
                                    SipProcessingExceptionReasonType.MULTIPLE_DEFINITIONS) {
                                // If there are too many definitions then show that as an exception -- our definitions need fixing.
                                parametersList.add(candidate)
                            } else if (processingTypes.contains(ProcessingType.SupplementGrouping) ||
                                    processingTypes.contains(ProcessingType.CreateSipForFolder)) {
                                log.debug("Ignoring processingType=${processingType}, invalid candidate=${candidate}, as other processing types may match.")
                            } else {
                                parametersList.add(candidate)
                            }
                        } else if (processingType == ProcessingType.SupplementGrouping &&
                                processingTypes.contains(ProcessingType.CreateSipForFolder)) {
                            log.debug("Ignoring processingType=${processingType}, invalid candidate=${candidate}, as other processing types may match.")
                        } else {
                            parametersList.add(candidate)
                        }
                    } else {
                        log.debug("Ignoring processingType=${processingType}, invalid candidate=${candidate}, as other processing types have matched.")
                    }
                }
            }
        }
        parametersList.each { FairfaxProcessingParameters parameters ->
            parameters.applyOverrides(overrideRules, overrideOptions)
        }
        List<FairfaxProcessingParameters> editionExpandedList = [ ]
        parametersList.each { FairfaxProcessingParameters processingParameters ->
            boolean hasMultipleEditions = processingParameters.editionDiscriminators.size() > 0
            if (hasMultipleEditions) {
                processingParameters.editionDiscriminators.each { String editionDiscriminator ->
                    boolean hasMatchingEdition = true
                    if (processingParameters.rules.contains(ProcessingRule.IgnoreEditionsWithoutMatchingFiles)) {
                        List<FairfaxFile> allFairfaxFiles = FairfaxFile.fromSourceFolder(sourceFolder)
                        hasMatchingEdition = allFairfaxFiles.any { FairfaxFile fairfaxFile ->
                            editionDiscriminator == fairfaxFile.sectionCode
                        }
                    }
                    if (hasMatchingEdition) {
                        FairfaxProcessingParameters clonedParameters = (FairfaxProcessingParameters) processingParameters.clone()
                        clonedParameters.currentEdition = editionDiscriminator
                        editionExpandedList.add(clonedParameters)
                    }
                }
            } else {
                editionExpandedList.add(processingParameters)
            }
        }
        return editionExpandedList
    }

    static FairfaxProcessingParameters buildForRows(String titleCode, ProcessingType processingType,
                                                    Path sourceFolder, LocalDate processingDate,
                                                    List<Map<String, String>> matchingRows) {
        if (matchingRows.size() > 1) {
            String message = "Multiple spreadsheet rows for processingType=${processingType.fieldValue} and titleCode=${titleCode}. Unable to generate parameters".toString()
            SipProcessingExceptionReason exceptionReason = new SipProcessingExceptionReason(
                    SipProcessingExceptionReasonType.MULTIPLE_DEFINITIONS, null, message)
            SipProcessingState replacementSipProcessingState = new SipProcessingState()
            replacementSipProcessingState.exceptions = [ SipProcessingException.createWithReason(exceptionReason) ]
            log.warn(message)
            return new FairfaxProcessingParameters(valid: false, titleCode: titleCode, type: processingType,
                    rules: processingType.defaultRules, options: processingType.defaultOptions,
                    sourceFolder: sourceFolder, date: processingDate, sipProcessingState: replacementSipProcessingState)
        } else if (matchingRows.size() == 0) {
            if (ProcessingType.CreateSipForFolder == processingType) {
                Map<String, String> blankRow = [ : ]
                return new FairfaxProcessingParameters(titleCode: titleCode, type: processingType,
                        rules: processingType.defaultRules, options: processingType.defaultOptions,
                        sourceFolder: sourceFolder, date: processingDate, spreadsheetRow: blankRow)
            } else {
                String message = "Unable to create processing parameters: No matching row for titleCode=${titleCode}".toString()
                SipProcessingExceptionReason exceptionReason = new SipProcessingExceptionReason(
                        SipProcessingExceptionReasonType.INVALID_PARAMETERS, null, message)
                SipProcessingState replacementSipProcessingState = new SipProcessingState()
                replacementSipProcessingState.exceptions = [ SipProcessingException.createWithReason(exceptionReason) ]
                log.warn(message)
                return new FairfaxProcessingParameters(valid: false, titleCode: titleCode, type: processingType,
                        rules: processingType.defaultRules, options: processingType.defaultOptions,
                        sourceFolder: sourceFolder, date: processingDate,
                        sipProcessingState: replacementSipProcessingState)
            }
        } else if (processingType == null) {
            String message = "ProcessingType must be set."
            SipProcessingExceptionReason exceptionReason = new SipProcessingExceptionReason(
                    SipProcessingExceptionReasonType.INVALID_PARAMETERS, null, message)
            SipProcessingState replacementSipProcessingState = new SipProcessingState()
            replacementSipProcessingState.exceptions = [ SipProcessingException.createWithReason(exceptionReason) ]
            log.warn(message)
            return new FairfaxProcessingParameters(valid: false, titleCode: titleCode, type: processingType,
                    rules: processingType.defaultRules, options: processingType.defaultOptions,
                    sourceFolder: sourceFolder, date: processingDate, sipProcessingState: replacementSipProcessingState)
        } else {
            // matchingRows.size() == 1
            Map<String, String> matchingRow = matchingRows.first()
            String rules = matchingRow.get(FairfaxSpreadsheet.PROCESSING_RULES_KEY)
            String options = matchingRow.get(FairfaxSpreadsheet.PROCESSING_OPTIONS_KEY)
            return new FairfaxProcessingParameters(titleCode: titleCode, type: processingType,
                    rules: ProcessingRule.extract(rules, ",", processingType.defaultRules),
                    options: ProcessingOption.extract(options, ",", processingType.defaultOptions),
                    sourceFolder: sourceFolder, date: processingDate, spreadsheetRow: matchingRow,
                    sectionCodes: extractSeparatedValues(matchingRow, FairfaxSpreadsheet.SECTION_CODE_KEY),
                    editionDiscriminators: extractSeparatedValues(matchingRow, FairfaxSpreadsheet.EDITION_DISCRIMINATOR_KEY),
                    isMagazine: FairfaxSpreadsheet.extractBooleanValue(matchingRow, FairfaxSpreadsheet.IS_MAGAZINE_KEY))
        }
    }

    static List<Map<String, String>> matchingRowsFor(String titleCode, ProcessingType processingType, Path sourceFolder,
                                                     LocalDate processingDate, FairfaxSpreadsheet spreadsheet) {
        List<Map<String, String>> matchingRows = spreadsheet.matchingProcessingTypeParameterMaps(
                processingType.fieldValue, titleCode)
        if (!matchingRows.isEmpty() && processingType == ProcessingType.ParentGroupingWithEdition) {
            // Step 1: Find all the files, get the different section_codes
//            List<FairfaxFile> allFairfaxFiles = FairfaxFile.fromSourceFolder(sourceFolder, fileFindPattern)
            List<FairfaxFile> allFairfaxFiles = FairfaxFile.fromSourceFolder(sourceFolder)
            List<String> uniqueSectionCodes = FairfaxFile.uniqueSectionCodes(allFairfaxFiles)

            // Step 2: Based on the section_code pick the right spreadsheet row
            List<Map<String, String>> editionMatchingRows = matchingRows.findAll { Map<String, String> row ->
                List<String> editionDiscriminators = extractSeparatedValues(row, FairfaxSpreadsheet.EDITION_DISCRIMINATOR_KEY)
                editionDiscriminators.any { String discriminator ->
                    uniqueSectionCodes.contains(discriminator)
                }
            }
            matchingRows = editionMatchingRows
        }
        return matchingRows
    }

    static List<String> extractSeparatedValues(Map<String, String> spreadsheetRow, String columnKey,
                                               String regex = "\\+|,|-") {
        List<String> extractedValues = splitColumnValue(spreadsheetRow.get(columnKey), regex)

        return extractedValues
    }

    static List<String> splitColumnValue(String columnValue, String regex = "\\+|,|-") {
        List<String> extractedValues = columnValue.split(regex).collect { String value ->
            value.strip()
        }

        return extractedValues.findAll { String value ->
            !value.isBlank()
        }
    }

    void applyOverrides(List<ProcessingRule> overrideRules, List<ProcessingOption> overrideOptions) {
        overrideProcessingRules(overrideRules)
        overrideProcessingOptions(overrideOptions)
    }

    void overrideProcessingRules(List<ProcessingRule> overrides) {
        rules = ProcessingRule.mergeOverrides(rules, overrides)
    }

    void overrideProcessingOptions(List<ProcessingOption> overrides) {
        options = ProcessingOption.mergeOverrides(options, overrides)
    }

    String getTitleParent() {
        String titleParent = spreadsheetRow.get(FairfaxSpreadsheet.TITLE_PARENT_KEY)
        if (titleParent == null || titleParent.strip().isEmpty()) {
            titleParent = "NO-TITLE-GIVEN"
        }
        return titleParent
    }

    String getTitleMets() {
        String titleMets = spreadsheetRow.get(FairfaxSpreadsheet.TITLE_METS_KEY)
        if (titleMets == null || titleMets.strip().isEmpty()) {
            titleMets = "NO-TITLE-METS-GIVEN"
        }
        return titleMets
    }

    String getMmsid() {
        String mmsId = spreadsheetRow.get(FairfaxSpreadsheet.MMSID_COLUMN_NAME)
        if (mmsId == null || mmsId.strip().isEmpty()) {
            mmsId = "NO-MMSID-GIVEN"
        }
        return mmsId
    }

    String getSectionCodesString() {
        String sectionCodesString = spreadsheetRow.get(FairfaxSpreadsheet.SECTION_CODE_KEY)
        if (sectionCodesString == null || sectionCodesString.strip().isEmpty()) {
            sectionCodesString = ""
        }
        return sectionCodesString
    }

    boolean hasCurrentEdition() {
        return (currentEdition != null && !currentEdition.isEmpty())
    }

    boolean matchesCurrentSection(String matchSectionCode, String fileSectionCode) {
        if (this.hasCurrentEdition()) {
            return this.editionDiscriminators.first() == matchSectionCode &&
                    (editionDiscriminators.first() == fileSectionCode ||
                            this.currentEdition == fileSectionCode)
        } else {
            return false
        }
    }

    List<String> validSectionCodes() {
        if (!hasCurrentEdition()) {
            return (List<String>) this.sectionCodes.clone()
        }
        List<String> validSectionCodes = [ ]
        if (this.currentEdition != this.editionDiscriminators.first()) {
            validSectionCodes.add(this.editionDiscriminators.first())
        }
        validSectionCodes.add(this.currentEdition)
        this.sectionCodes.each { String sectionCode ->
            if (sectionCode != currentEdition && sectionCode != editionDiscriminators.first()) {
                validSectionCodes.add(sectionCode)
            }
        }
        return validSectionCodes
    }

    String processingDifferentiator() {
        String baseDifferentiator = "${date.format(READABLE_DATE_FORMAT)}_${titleCode}_${type.fieldValue}"
        if (currentEdition == null) {
            return baseDifferentiator
        } else {
            return "${baseDifferentiator}_${currentEdition}"
        }
    }

    boolean isIncludeCurrentEditionForDcCoverage() {
        return hasCurrentEdition() && !IGNORE_EDITIONS_FOR_DC_COVERAGE.contains(currentEdition)
    }

    String detailedDisplay(int offset = 0, boolean includeSipProcessingState = false) {

        String initialOffset = StringUtils.repeat(' ', offset)
        StringBuilder stringBuilder = new StringBuilder(initialOffset)
        stringBuilder.append("${this.getClass().getName()}:")
        stringBuilder.append(System.lineSeparator())
        stringBuilder.append("${initialOffset}    type=${type.fieldValue}")
        stringBuilder.append(System.lineSeparator())
        stringBuilder.append("${initialOffset}    sourceFolder=${sourceFolderPath()}")
        stringBuilder.append(System.lineSeparator())
        stringBuilder.append("${initialOffset}    date=${date.format(READABLE_DATE_FORMAT)}")
        stringBuilder.append(System.lineSeparator())
        stringBuilder.append("${initialOffset}    rules=${rules}")
        stringBuilder.append(System.lineSeparator())
        stringBuilder.append("${initialOffset}    options=${options}")
        stringBuilder.append(System.lineSeparator())
        stringBuilder.append("${initialOffset}    valid=${valid}")
        stringBuilder.append(System.lineSeparator())
        stringBuilder.append("${initialOffset}    titleCode=${titleCode}")
        stringBuilder.append(System.lineSeparator())
        stringBuilder.append("${initialOffset}    sectionCodes=${sectionCodes}")
        stringBuilder.append(System.lineSeparator())
        stringBuilder.append("${initialOffset}    editionDiscriminators=${editionDiscriminators}")
        stringBuilder.append(System.lineSeparator())
        stringBuilder.append("${initialOffset}    currentEdition=${currentEdition}")
        stringBuilder.append(System.lineSeparator())
        stringBuilder.append("${initialOffset}    isMagazine=${isMagazine}")
        stringBuilder.append(System.lineSeparator())
        stringBuilder.append("${initialOffset}    spreadsheetRow=${spreadsheetRow}")
        stringBuilder.append(System.lineSeparator())
        if (includeSipProcessingState) {
            stringBuilder.append(this.sipProcessingState.toString(offset))
        }

        return stringBuilder.toString()
    }

    String sourceFolderPath() {
        return sourceFolder == null ? "null" : sourceFolder.normalize().toString()
    }
}
