package nz.govt.natlib.tools.sip.generation.newspapers

import groovy.transform.AutoClone
import groovy.transform.Canonical
import groovy.transform.ToString
import groovy.util.logging.Log4j2
import nz.govt.natlib.tools.sip.generation.newspapers.parameters.ProcessingOption
import nz.govt.natlib.tools.sip.generation.newspapers.parameters.ProcessingRule
import nz.govt.natlib.tools.sip.generation.newspapers.parameters.ProcessingType
import nz.govt.natlib.tools.sip.generation.newspapers.special.ExtractValues
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
class NewspaperProcessingParameters {
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
    List<String> supplementTitleCodes = [ ]
    List<String> supplementSequenceCodes = [ ]
    List<String> sectionCodes = [ ]
    List<String> editionCodes = [ ]
    List<String> sequenceLetters = [ ]
    List<String> ignoreSequence = [ ]
    List<String> editionDiscriminators = [ ]
    boolean isMagazine = false
    String currentEdition
    String supplementPreviousIssuesFile = null
    SipProcessingState sipProcessingState = new SipProcessingState()

    static List<NewspaperProcessingParameters> build(String titleCode, List<ProcessingType> processingTypes, Path sourceFolder,
                                                     LocalDate processingDate, NewspaperSpreadsheet spreadsheet, NewspaperType newspaperType,
                                                     List<ProcessingRule> overrideRules = [], List<ProcessingOption> overrideOptions = [],
                                                     String supplementPreviousIssuesFile = null) {
        List<NewspaperProcessingParameters> parametersList = [ ]

        processingTypes.sort().each { ProcessingType processingType ->
            List<Map<String, String>> matchingRows = matchingRowsFor(titleCode, processingType, sourceFolder, processingDate, spreadsheet, newspaperType)
            if (processingType == ProcessingType.ParentGroupingWithEdition) {
                matchingRows.each { Map<String, String> singleRow ->
                    NewspaperProcessingParameters candidate = buildForRows(titleCode, processingType, sourceFolder,
                            processingDate, [ singleRow ])
                    candidate.rules = ProcessingRule.mergeOverrides(candidate.rules, overrideRules)
                    candidate.options = ProcessingOption.mergeOverrides(candidate.options, overrideOptions)
                    parametersList.add(candidate)
                }
            } else if (processingType == ProcessingType.SupplementGrouping) {
                matchingRows.each { Map<String, String> singleRow ->
                    NewspaperProcessingParameters candidate = buildForRows(titleCode, processingType, sourceFolder,
                            processingDate, [ singleRow ])
                    candidate.rules = ProcessingRule.mergeOverrides(candidate.rules, overrideRules)
                    candidate.options = ProcessingOption.mergeOverrides(candidate.options, overrideOptions)
                    parametersList.add(candidate)
                }
            } else if (processingType == ProcessingType.SupplementWithDateAndIssue) {
                matchingRows.each { Map<String, String> singleRow ->
                    NewspaperProcessingParameters candidate = buildForRows(titleCode, processingType, sourceFolder,
                            processingDate, [ singleRow ])
                    candidate.rules = ProcessingRule.mergeOverrides(candidate.rules, overrideRules)
                    candidate.options = ProcessingOption.mergeOverrides(candidate.options, overrideOptions)
                    candidate.supplementPreviousIssuesFile = supplementPreviousIssuesFile
                    parametersList.add(candidate)
                }
            } else {
                NewspaperProcessingParameters candidate = buildForRows(titleCode, processingType, sourceFolder, processingDate, matchingRows)
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
                                    processingTypes.contains(ProcessingType.CreateSipForFolder) ||
                                    processingTypes.contains(ProcessingType.SupplementWithDateAndIssue)) {
                                log.debug("Ignoring processingType=${processingType}, invalid candidate=${candidate}, as other processing types may match.")
                            } else {
                                parametersList.add(candidate)
                            }
                        } else if ((processingType == ProcessingType.SupplementGrouping ||
                                processingType == ProcessingType.SupplementWithDateAndIssue) &&
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
        parametersList.each { NewspaperProcessingParameters parameters ->
            parameters.applyOverrides(overrideRules, overrideOptions)
        }
        List<NewspaperProcessingParameters> editionExpandedList = [ ]
        parametersList.each { NewspaperProcessingParameters processingParameters ->
            boolean hasMultipleEditions = processingParameters.editionDiscriminators.size() > 0
            if (hasMultipleEditions) {
                processingParameters.editionDiscriminators.each { String editionDiscriminator ->
                    boolean hasMatchingEdition = true
                    if (processingParameters.rules.contains(ProcessingRule.IgnoreEditionsWithoutMatchingFiles)) {
                        List<NewspaperFile> allNewspaperFiles = NewspaperFile.fromSourceFolder(sourceFolder, newspaperType)
                        hasMatchingEdition = allNewspaperFiles.any { NewspaperFile newspaperFile ->
                            editionDiscriminator == newspaperFile.editionCode
                        }
                    }
                    if (hasMatchingEdition) {
                        NewspaperProcessingParameters clonedParameters = (NewspaperProcessingParameters) processingParameters.clone()
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

    static NewspaperProcessingParameters buildForRows(String titleCode, ProcessingType processingType,
                                                      Path sourceFolder, LocalDate processingDate,
                                                      List<Map<String, String>> matchingRows) {
        if (matchingRows.size() > 1) {
            String message = "Multiple spreadsheet rows for processingType=${processingType.fieldValue} and titleCode=${titleCode}. Unable to generate parameters".toString()
            SipProcessingExceptionReason exceptionReason = new SipProcessingExceptionReason(
                    SipProcessingExceptionReasonType.MULTIPLE_DEFINITIONS, null, message)
            SipProcessingState replacementSipProcessingState = new SipProcessingState()
            replacementSipProcessingState.exceptions = [ SipProcessingException.createWithReason(exceptionReason) ]
            log.warn(message)
            return new NewspaperProcessingParameters(valid: false, titleCode: titleCode, type: processingType,
                    rules: processingType.defaultRules, options: processingType.defaultOptions,
                    sourceFolder: sourceFolder, date: processingDate, sipProcessingState: replacementSipProcessingState)
        } else if (matchingRows.size() == 0) {
            if (ProcessingType.CreateSipForFolder == processingType) {
                Map<String, String> blankRow = [ : ]
                return new NewspaperProcessingParameters(titleCode: titleCode, type: processingType,
                        rules: processingType.defaultRules, options: processingType.defaultOptions,
                        sourceFolder: sourceFolder, date: processingDate, spreadsheetRow: blankRow)
            } else {
                String message = "Unable to create processing parameters: No matching row for titleCode=${titleCode}".toString()
                SipProcessingExceptionReason exceptionReason = new SipProcessingExceptionReason(
                        SipProcessingExceptionReasonType.INVALID_PARAMETERS, null, message)
                SipProcessingState replacementSipProcessingState = new SipProcessingState()
                replacementSipProcessingState.exceptions = [ SipProcessingException.createWithReason(exceptionReason) ]
                log.warn(message)
                return new NewspaperProcessingParameters(valid: false, titleCode: titleCode, type: processingType,
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
            return new NewspaperProcessingParameters(valid: false, titleCode: titleCode, type: processingType,
                    rules: processingType.defaultRules, options: processingType.defaultOptions,
                    sourceFolder: sourceFolder, date: processingDate, sipProcessingState: replacementSipProcessingState)
        } else {
            // matchingRows.size() == 1
            Map<String, String> matchingRow = matchingRows.first()
            String rules = matchingRow.get(NewspaperSpreadsheet.PROCESSING_RULES_KEY)
            String options = matchingRow.get(NewspaperSpreadsheet.PROCESSING_OPTIONS_KEY)
            return new NewspaperProcessingParameters(titleCode: titleCode, type: processingType,
                    rules: ProcessingRule.extract(rules, ",", processingType.defaultRules),
                    options: ProcessingOption.extract(options, ",", processingType.defaultOptions),
                    sourceFolder: sourceFolder, date: processingDate, spreadsheetRow: matchingRow,
                    supplementTitleCodes: ExtractValues.extractSeparatedValues(matchingRow, NewspaperSpreadsheet.SUPPLEMENT_TITLE_CODE_KEY),
                    supplementSequenceCodes: ExtractValues.extractSeparatedValues(matchingRow, NewspaperSpreadsheet.SUPPLEMENT_WITH_SEQUENCE_KEY),
                    sectionCodes: ExtractValues.extractSeparatedValues(matchingRow, NewspaperSpreadsheet.SECTION_CODE_KEY),
                    editionCodes: ExtractValues.extractSeparatedValues(matchingRow, NewspaperSpreadsheet.EDITION_CODE_KEY),
                    sequenceLetters: ExtractValues.extractSeparatedValues(matchingRow, NewspaperSpreadsheet.SEQUENCE_LETTER_KEY),
                    ignoreSequence: ExtractValues.extractSeparatedValues(matchingRow, NewspaperSpreadsheet.IGNORE_SEQUENCE_KEY),
                    editionDiscriminators: ExtractValues.extractSeparatedValues(matchingRow, NewspaperSpreadsheet.EDITION_DISCRIMINATOR_KEY),
                    isMagazine: NewspaperSpreadsheet.extractBooleanValue(matchingRow, NewspaperSpreadsheet.IS_MAGAZINE_KEY))
        }
    }

    static List<Map<String, String>> matchingRowsFor(String titleCode, ProcessingType processingType, Path sourceFolder,
                                                     LocalDate processingDate, NewspaperSpreadsheet spreadsheet, NewspaperType newspaperType) {
        List<Map<String, String>> matchingRows = spreadsheet.matchingProcessingTypeParameterMaps(
                processingType.fieldValue, titleCode)
        if (!matchingRows.isEmpty() && processingType == ProcessingType.ParentGroupingWithEdition) {
            // Step 1: Find all the files, get the different section_codes
//            List<NewspaperFile> allNewspaperFiles = NewspaperFile.fromSourceFolder(sourceFolder, fileFindPattern)
            List<NewspaperFile> allNewspaperFiles = NewspaperFile.fromSourceFolder(sourceFolder, newspaperType)
            List<String> uniqueSectionCodes = NewspaperFile.uniqueSectionCodes(allNewspaperFiles)

            // Step 2: Based on the section_code pick the right spreadsheet row
            List<Map<String, String>> editionMatchingRows = matchingRows.findAll { Map<String, String> row ->
                List<String> editionDiscriminators = ExtractValues.extractSeparatedValues(row, NewspaperSpreadsheet.EDITION_DISCRIMINATOR_KEY)
                editionDiscriminators.any { String discriminator ->
                    uniqueSectionCodes.contains(discriminator)
                }
            }
            matchingRows = editionMatchingRows
        }
        return matchingRows
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
        String titleParent = spreadsheetRow.get(NewspaperSpreadsheet.TITLE_PARENT_KEY)
        if (titleParent == null || titleParent.strip().isEmpty()) {
            titleParent = "NO-TITLE-GIVEN"
        }
        return titleParent
    }

    String getTitleMets() {
        String titleMets = spreadsheetRow.get(NewspaperSpreadsheet.TITLE_METS_KEY)
        if (titleMets == null || titleMets.strip().isEmpty()) {
            titleMets = "NO-TITLE-METS-GIVEN"
        }
        return titleMets
    }

    String getMmsid() {
        String mmsId = spreadsheetRow.get(NewspaperSpreadsheet.MMSID_COLUMN_NAME)
        if (mmsId == null || mmsId.strip().isEmpty()) {
            mmsId = "NO-MMSID-GIVEN"
        }
        return mmsId
    }

    String getSectionCodesString() {
        String sectionCodesString = spreadsheetRow.get(NewspaperSpreadsheet.SECTION_CODE_KEY)
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
            return (List<String>) this.editionCodes.clone()
        }
        List<String> validSectionCodes = [ ]
        if (this.currentEdition != this.editionDiscriminators.first()) {
            validSectionCodes.add(this.editionDiscriminators.first())
        }
        validSectionCodes.add(this.currentEdition)
        this.editionCodes.each { String sectionCode ->
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
        stringBuilder.append("${initialOffset}    editionCodes=${editionCodes}")
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
