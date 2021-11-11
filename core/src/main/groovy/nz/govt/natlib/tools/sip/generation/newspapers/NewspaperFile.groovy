package nz.govt.natlib.tools.sip.generation.newspapers

import groovy.transform.Canonical
import groovy.transform.EqualsAndHashCode
import groovy.transform.Sortable
import groovy.transform.ToString
import groovy.util.logging.Log4j2
import nz.govt.natlib.tools.sip.Sip
import nz.govt.natlib.tools.sip.SipFileWrapperFactory
import nz.govt.natlib.tools.sip.generation.newspapers.parameters.ProcessingOption
import nz.govt.natlib.tools.sip.generation.newspapers.parameters.ProcessingRule
import nz.govt.natlib.tools.sip.pdf.PdfDimensionFinder
import nz.govt.natlib.tools.sip.utils.PathUtils
import org.apache.commons.collections4.CollectionUtils

import java.awt.Point
import java.awt.geom.Point2D
import java.nio.file.Path
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.regex.Matcher

/**
 * Encapsulates the characteristics of Fairfax file. Includes methods for operating on lists of such files.
 */
@Canonical
@Sortable(includes = ['titleCode', 'sectionCode', 'dateYear', 'dateMonthOfYear', 'dateDayOfMonth', 'sequenceLetter',
        'sequenceNumber', 'qualifier' ])
@ToString(includeNames = true, includePackage = false, includes = [ 'filename', 'file' ])
@EqualsAndHashCode(includes = [ 'titleCode', 'sectionCode', 'date', 'sequenceLetter', 'sequenceNumber' ])
@Log4j2
class NewspaperFile {
    // Note that the titleCode appears to be, in some cases 4 characters long (eg. JAZZTAB), but for most cases it is 3.
    // The populate() method attempts to correct any issues with the titleCode/sectionCode grouping.
    // Note that the pdf extension can be upper or lower case (and we handle the mixed case as well

    // Normal Fairfax Processing
//    static final String PDF_FILE_WITH_TITLE_SECTION_DATE_SEQUENCE_GROUPING_PATTERN = "(?<titleCode>[a-zA-Z0-9]{3,4})" +
//            "(?<sectionCode>[a-zA-Z0-9]{2,3})-(?<date>\\d{8})-(?<sequenceLetter>[A-Za-z]{0,2})" +
//            "(?<sequenceNumber>\\d{1,4})(?<qualifier>.*?)\\.[pP]{1}[dD]{1}[fF]{1}"
//    static final String PDF_FILE_WITH_TITLE_SECTION_DATE_SEQUENCE_PATTERN = '\\w{5,7}-\\d{8}-\\w{1,4}.*?\\.[pP]{1}[dD]{1}[fF]{1}'
//    static final String PDF_FILE_WITH_TITLE_SECTION_DATE_PATTERN = '\\w{5,7}-\\d{8}-.*?\\.[pP]{1}[dD]{1}[fF]{1}'
//    static final DateTimeFormatter LOCAL_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd")


    // Wairarapa Times Processing
//    static final String PDF_FILE_WITH_TITLE_SECTION_DATE_SEQUENCE_GROUPING_PATTERN = "(?<titleCode>[a-zA-Z0-9]{3,4})" +
//            "(?<sectionCode>)(?<date>\\d{2}\\w{3}\\d{2})(?<sequenceLetter>[A-Za-z]{0,2})" +
//            "(?<sequenceNumber>\\d{1,4})(?<qualifier>.*?)\\.[pP]{1}[dD]{1}[fF]{1}"
//    static final String PDF_FILE_WITH_TITLE_SECTION_DATE_SEQUENCE_PATTERN = '\\w{4,7}\\d{2}\\w{3}\\d{2}\\w{1,4}.*?\\.[pP]{1}[dD]{1}[fF]{1}'
//    static final String PDF_FILE_WITH_TITLE_SECTION_DATE_PATTERN = '\\w{4,7}\\d{2}\\w{3}\\d{2}.*?\\.[pP]{1}[dD]{1}[fF]{1}'
//    static final DateTimeFormatter LOCAL_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("ddMMMyy")

    static final Point UNDIMENSIONED = new Point(-1, -1)

//    String PDF_FILE_WITH_TITLE_SECTION_DATE_SEQUENCE_GROUPING_PATTERN
//    String PDF_FILE_WITH_TITLE_SECTION_DATE_SEQUENCE_PATTERN
//    String PDF_FILE_WITH_TITLE_SECTION_DATE_PATTERN
    DateTimeFormatter LOCAL_DATE_TIME_FORMATTER

    Path file
    static PublicationType publicationType
    // This is for when the file gets replaced, such as when a zero-length pdf is replaced by another file.
    Path originalFile
    String filename
    String titleCode
    String sectionCode
    Integer dateYear
    Integer dateMonthOfYear
    Integer dateDayOfMonth
    LocalDate date
    String sequenceLetter
    String sequenceNumberString
    Integer sequenceNumber
    String qualifier
    boolean validForProcessing
    boolean validPdf
    Point dimensionsInPoints = UNDIMENSIONED
    boolean zeroLengthFile = false

    static List<NewspaperFile> differences(List<NewspaperFile> list1, List<NewspaperFile> list2) {
        List<NewspaperFile> list1MinusList2 = CollectionUtils.subtract(list1, list2)
        List<NewspaperFile> list2MinusList1 = CollectionUtils.subtract(list2, list1)
        List<NewspaperFile> differences = [ ]
        differences.addAll(list1MinusList2)
        differences.addAll(list2MinusList1)

        return differences
    }

    static List<String> allSectionCodes(List<NewspaperFile> files) {
        List<String> sectionCodes = [ ]
        files.each { NewspaperFile newspaperFile ->
            if (newspaperFile.sectionCode != null && !newspaperFile.sectionCode.isEmpty()) {
                if (!sectionCodes.contains(newspaperFile.sectionCode)) {
                    sectionCodes.add(newspaperFile.sectionCode)
                }
            }
        }
        return sectionCodes
    }

    // The assumption here is that the list of files is only different in sectionCode, sequenceLetter, sequenceNumber
    // and qualifier.
    // Except in the case of a substitution, where the original sectionCode and the substitute sectionCode are treated
    // as if they are the same.
    static List<NewspaperFile> sortWithSameTitleCodeAndDate(List<NewspaperFile> files,
                                                            NewspaperProcessingParameters processingParameters) {
        // FIRST: Order by sectionCode as per processingParameters
        Map<String, List<NewspaperFile>> filesBySection = [: ]
        processingParameters.sectionCodes.each { String sectionCode ->
            List<NewspaperFile> sectionFiles = [ ]
            filesBySection.put(sectionCode, sectionFiles)
            files.each { NewspaperFile newspaperFile ->
                if (sectionCode == newspaperFile.sectionCode ||
                        processingParameters.matchesCurrentSection(sectionCode, newspaperFile.sectionCode)) {
                    sectionFiles.add(newspaperFile)
                }
            }
        }
        // NEXT: Sort each sectionCode by numberAndAlpha
        boolean alphaBeforeNumeric = processingParameters.options.contains(ProcessingOption.AlphaBeforeNumericSequencing)
        processingParameters.sectionCodes.each { String sectionCode ->
            List<NewspaperFile> sectionFiles = filesBySection.get(sectionCode)
            sectionFiles = sortNumericAndAlpha(sectionFiles, alphaBeforeNumeric)
            filesBySection.put(sectionCode, sectionFiles)
        }
        List<NewspaperFile> sorted = [ ]
        processingParameters.sectionCodes.each { String sectionCode ->
            sorted.addAll(filesBySection.get(sectionCode))
        }
        if (sorted.size() != files.size()) {
            log.warn("Not all sorted files exist in final list, differences=${differences(sorted, files)}")
        }
        return sorted
    }

    static List<NewspaperFile> postMissingSequenceFiles(List<NewspaperFile> files,
                                                        NewspaperProcessingParameters processingParameters) {
        List<NewspaperFile> sorted = null;
        // Sort list in ascending order if it doesn't contain a section code
        if (files[0].getSectionCode() == null || files[0].getSectionCode().isEmpty()) sorted = files.sort()
        else sorted = sortWithSameTitleCodeAndDate(files, processingParameters)

        NewspaperFile previousFile = null
        List<NewspaperFile> postMissingFiles = [ ]
        sorted.each { NewspaperFile testFile ->
            if (previousFile != null) {
                if (!testFile.canComeDirectlyAfter(previousFile, processingParameters.editionDiscriminators)) {
                    if (testFile.isAHundredsSequenceStart() &&
                            processingParameters.rules.contains(ProcessingRule.NumericStartsInHundredsNotConsideredSequenceSkips)) {
                        // We don't consider this a skip in the sequence.
                        // Note that there's a small edge case where there are hundreds of pages, such as:
                        // 397, 398, 400, 401, ... -> this would be considered okay, even though there is a page missing.
                    } else {
                        postMissingFiles.add(testFile)
                    }
                }
                previousFile = testFile
            } else {
                previousFile = testFile
                if (testFile.sequenceNumber != 1) {
                    postMissingFiles.add(testFile)
                }
            }
        }
        return postMissingFiles
    }

    static NewspaperFile substituteFor(String sourceSectionCode, String replacementSectionCode, NewspaperFile newspaperFile,
                                       List<NewspaperFile> possibleFiles) {
        if (newspaperFile.sectionCode == sourceSectionCode) {
            NewspaperFile replacementFile = possibleFiles.find { NewspaperFile candidateFile ->
                if (candidateFile.sectionCode == replacementSectionCode) {
                    candidateFile.canSubstituteFor(newspaperFile)
                } else {
                    false
                }
            }
            return replacementFile == null ? newspaperFile : replacementFile
        } else if (newspaperFile.sectionCode == replacementSectionCode) {
            // We add it if the substitution does not exist (in other words, the substitute doesn't map to the source
            NewspaperFile replacementFile = possibleFiles.find { NewspaperFile candidateFile ->
                if (candidateFile.sectionCode == sourceSectionCode) {
                    candidateFile.canSubstituteFor(newspaperFile)
                } else {
                    false
                }
            }
            return replacementFile == null ? newspaperFile : null
        } else {
            return newspaperFile
        }
    }

    static List<NewspaperFile> substituteAllFor(String sourceSectionCode, String replacementSectionCode,
                                                List<String> allSectionCodes, List<NewspaperFile> possibleFiles) {
        List<NewspaperFile> substituted = []
        List<String> otherSectionCodes = allSectionCodes.findAll { String sectionCode ->
            sectionCode != sourceSectionCode && sectionCode != replacementSectionCode
        }
        possibleFiles.each { NewspaperFile newspaperFile ->
            if (!otherSectionCodes.contains(newspaperFile.sectionCode)) {
                NewspaperFile replacementFile = substituteFor(sourceSectionCode, replacementSectionCode, newspaperFile,
                        possibleFiles)
                if (replacementFile != null) {
                    substituted.add(replacementFile)
                }
            }
        }
        return substituted
    }

    static boolean hasSubstitutions(String replacementSectionCode, List<NewspaperFile> possibleFiles) {
        return possibleFiles.any { NewspaperFile newspaperFile ->
            newspaperFile.sectionCode == replacementSectionCode
        }
    }

    static List<NewspaperFile> filterAllFor(List<String> sectionCodes, List<NewspaperFile> possibleFiles) {
        List<NewspaperFile> filtered = possibleFiles.findAll { NewspaperFile newspaperFile ->
            sectionCodes.contains(newspaperFile.sectionCode)
        }
        if (possibleFiles.size() != filtered.size()) {
            log.warn("Not all filtered files exist in final list, differences=${differences(possibleFiles, filtered)}")
        }
        return filtered
    }

    static List<NewspaperFile> filterSubstituteAndSort(List<NewspaperFile> allPossibleFiles,
                                                       NewspaperProcessingParameters processingParameters) {
        List<NewspaperFile> filteredSubstitutedAndSorted
        if (processingParameters.currentEdition != null && !processingParameters.editionDiscriminators.isEmpty()) {
            // First we filter so we only have the files we want to process
            List<NewspaperFile> filtered = filterAllFor(processingParameters.validSectionCodes(), allPossibleFiles)
            // Then we do the substitutions
            // Substitutions happen if the FIRST editionDiscriminator has a substitution with the same date/sequenceLetter/sequenceNumber
            String firstDiscriminatorCode = processingParameters.editionDiscriminators.first()

            boolean hasSubstitutions = hasSubstitutions(processingParameters.currentEdition, filtered)
            if (hasSubstitutions) {
                List<NewspaperFile> substituted = substituteAllFor(firstDiscriminatorCode,
                        processingParameters.currentEdition, processingParameters.editionDiscriminators, filtered)
                // Then we sort so the ordering is correct
                // Sort list in ascending order if it doesn't contain a section code
                if (substituted[0].getSectionCode() == null || substituted[0].getSectionCode().isEmpty()) filteredSubstitutedAndSorted = substituted.sort()
                else filteredSubstitutedAndSorted = sortWithSameTitleCodeAndDate(substituted, processingParameters)
            } else {
                // If there are no substitutions (including the first for itself) then there is nothing to process
                filteredSubstitutedAndSorted = [ ]
            }
        } else {
            // Sort list in ascending order if it doesn't contain a section code
            if (allPossibleFiles[0].getSectionCode() || allPossibleFiles[0].getSectionCode().isEmpty()) filteredSubstitutedAndSorted = allPossibleFiles.sort()
            else filteredSubstitutedAndSorted =  sortWithSameTitleCodeAndDate(allPossibleFiles, processingParameters)
        }
        return filteredSubstitutedAndSorted
    }

    // The assumption is that all these files share the same: title_code, section_code and date
    static List<NewspaperFile> sortNumericAndAlpha(List<NewspaperFile> files, boolean alphaBeforeNumeric = false) {
        List<NewspaperFile> sorted = files.sort() { NewspaperFile file1, NewspaperFile file2 ->
            // TODO Not taking into account sectionCode (or date, for that matter)
            if (alphaBeforeNumeric) {
                if (file1.sequenceLetter.isEmpty()) {
                    if (file2.sequenceLetter.isEmpty()) {
                        // file1 and file2 are numeric
                        file1.sequenceNumber <=> file2.sequenceNumber
                    } else {
                        // file1 is numeric-only, file2 is alpha-numeric
                        +1
                    }
                } else {
                    if (file2.sequenceLetter.isEmpty()) {
                        // file1 is alpha-numeric, file2 is numeric
                        -1
                    } else {
                        // file1 and file2 are alpha-numeric
                        file1.sequenceLetter <=> file2.sequenceLetter ?: file1.sequenceNumber <=> file2.sequenceNumber
                    }
                }
            } else {
                if (file1.sequenceLetter.isEmpty()) {
                    if (file2.sequenceLetter.isEmpty()) {
                        // file1 and file2 are numeric
                        file1.sequenceNumber <=> file2.sequenceNumber
                    } else {
                        // file1 is numeric-only, file2 is alpha-numeric
                        -1
                    }
                } else {
                    if (file2.sequenceLetter.isEmpty()) {
                        // file1 is alpha-numeric, file2 is numeric
                        +1
                    } else {
                        // file1 and file2 are alpha-numeric
                        file1.sequenceLetter <=> file2.sequenceLetter ?: file1.sequenceNumber <=> file2.sequenceNumber
                    }
                }
            }
        }
        return sorted
    }

    static List<NewspaperFile> fromSourceFolder(Path sourceFolder) {
        String pattern = publicationType.PDF_FILE_WITH_TITLE_SECTION_DATE_SEQUENCE_PATTERN
        boolean isRegexNotGlob = true
        boolean matchFilenameOnly = true
        boolean sortFiles = true

        log.info("Processing for pattern=${pattern}, sourceFolder=${sourceFolder.normalize()}")

        // Note that we only want the current directory and we don't want info messages
        List<Path> allFiles = PathUtils.findFiles(sourceFolder.normalize().toString(),
                isRegexNotGlob, matchFilenameOnly, sortFiles, pattern, null, false, true)
        List<NewspaperFile> onlyNewspaperFiles = [ ]
        allFiles.each { Path file ->
            NewspaperFile newspaperFile = new NewspaperFile(file, publicationType)
            // TODO We have no checks here for NewspaperFile validity -- the pattern supposedly selects only validly named ones.
            onlyNewspaperFiles.add(newspaperFile)
        }
        return onlyNewspaperFiles
    }

    static List<String> uniqueSectionCodes(List<NewspaperFile> newspaperFiles) {
        Set<String> uniqueCodes = [ ]
        newspaperFiles.each { NewspaperFile file ->
            uniqueCodes.add(file.sectionCode)
        }
        return uniqueCodes.toList()
    }

    static List<String> asFilenames(List<NewspaperFile> files) {
        return files.collect { NewspaperFile newspaperFile ->
            newspaperFile.file.fileName.toString()
        }
    }

    NewspaperFile(Path file, PublicationType publicationType) {
        this.file = file
        this.publicationType = publicationType
        populate()
    }

    Sip.FileWrapper generateFileWrapper() {
        return SipFileWrapperFactory.generate(this.file)
    }

    private populate() {
        this.filename = file.fileName.toString()
        this.LOCAL_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(publicationType.DATE_TIME_PATTERN)
        // TODO Maybe the pattern comes from a resource or properties file?
        Matcher matcher = filename =~ /${publicationType.PDF_FILE_WITH_TITLE_SECTION_DATE_SEQUENCE_GROUPING_PATTERN}/
        if (matcher.matches()) {
            this.titleCode = matcher.group('titleCode')
            this.sectionCode = matcher.group('sectionCode')
            // In some situations the titleCode will take too many characters
//            if ((this.titleCode.length() == 4) && (this.sectionCode.length() == 2)) {
//                this.sectionCode = "${this.titleCode.substring(3, 4)}${this.sectionCode}"
//                this.titleCode = this.titleCode.substring(0, 3)
//             }
            String dateString = matcher.group('date')
            // Will not parse if the month is all in caps
            dateString = dateString.substring(0,3) + dateString.substring(3, 5).toLowerCase() + dateString.substring(5)
            this.date = LocalDate.parse(dateString, LOCAL_DATE_TIME_FORMATTER)
            this.dateYear = date.year
            this.dateMonthOfYear = date.monthValue
            this.dateDayOfMonth = date.dayOfMonth
            this.sequenceLetter = matcher.group('sequenceLetter')
            this.sequenceNumberString = matcher.group('sequenceNumber')
            this.sequenceNumber = sequenceNumberString.length() > 0 ? Integer.parseInt(sequenceNumberString) : 0
            this.qualifier = matcher.group('qualifier')
        }
    }

    boolean isValidName() {
        return this.file != null && this.titleCode != null && this.sectionCode != null && this.dateYear != null &&
                this.dateMonthOfYear != null && this.dateDayOfMonth != null && this.sequenceNumber != null
    }

    boolean matches(NewspaperFile newspaperFile) {
        return this.matches(newspaperFile.titleCode, newspaperFile.sectionCode, newspaperFile.dateYear, newspaperFile.dateMonthOfYear,
            newspaperFile.dateDayOfMonth)
    }

    boolean matches(String comparisonTitleCode, String comparisonSectionCode, Integer comparisonYear,
                    Integer comparisonMonthOfYear, Integer comparisonDayOfMonth) {
        if (isValidName()) {
            if (this.titleCode == comparisonTitleCode && this.sectionCode == comparisonSectionCode) {
                return (this.dateYear == comparisonYear && this.dateMonthOfYear == comparisonMonthOfYear &&
                        this.dateDayOfMonth == comparisonDayOfMonth)
            } else {
                return false
            }
        } else {
            return false
        }
    }

    boolean matchesWithSequence(NewspaperFile newspaperFile) {
        if (matches(newspaperFile)) {
            return (this.sequenceLetter == newspaperFile.sequenceLetter) &&
                    (this.sequenceNumber == newspaperFile.sequenceNumber)
        } else {
            return false
        }
    }

    boolean canComeDirectlyAfter(NewspaperFile newspaperFile, List<String> editionDiscriminators = [ ]) {
        // this file's sequence number must be greater (or a letter starting at 1)
        int sequenceDifference = this.sequenceNumber - newspaperFile.sequenceNumber
        if (this.sequenceLetter == newspaperFile.sequenceLetter) {
            boolean sameSectionCode = this.sectionCode == newspaperFile.sectionCode ||
                    (editionDiscriminators.contains(this.sectionCode) &&
                            editionDiscriminators.contains(newspaperFile.sectionCode))
            if (sameSectionCode) {
                return sequenceDifference == 1
            } else {
                return this.sequenceNumber == 1
            }
        } else {
            return this.sequenceNumber == 1
        }
    }

    // Substitutions can happen if the file has the same date, sequence letter and sequence number
    boolean canSubstituteFor(NewspaperFile newspaperFile) {
        return this.date == newspaperFile.date && this.sequenceLetter == newspaperFile.sequenceLetter &&
                this.sequenceNumber == newspaperFile.sequenceNumber
    }

    boolean isDimensioned() {
        return dimensionsInPoints.x > 0 && dimensionsInPoints.y > 0
    }

    void updateDimensions(boolean whenNotDimensioned = true) {
        if ((whenNotDimensioned && !isDimensioned()) || !whenNotDimensioned) {
            this.dimensionsInPoints = PdfDimensionFinder.getDimensions(this.file, 0)
        }
    }

    boolean isSameHeightDoubleWidth(NewspaperFile otherFile) {
        updateDimensions(true)
        otherFile.updateDimensions(true)

        Point2D.Double ratio = PdfDimensionFinder.getDimensionalRatio(this.dimensionsInPoints, otherFile.dimensionsInPoints)
        boolean isSameHeightDoubleWidth = PdfDimensionFinder.isSameHeightDoubleWidth(ratio, 0.1)
        if (!isSameHeightDoubleWidth) {
            log.info("Not same height/double width dimensions1=${this.dimensionsInPoints}, dimensions2=${otherFile.dimensionsInPoints} file1=${this.file.normalize()}, file2=${otherFile.file.normalize()}")
        }
        return isSameHeightDoubleWidth
    }

    boolean isSameHeightHalfWidth(NewspaperFile otherFile) {
        updateDimensions(true)
        otherFile.updateDimensions(true)

        Point2D.Double ratio = PdfDimensionFinder.getDimensionalRatio(this.dimensionsInPoints, otherFile.dimensionsInPoints)
        boolean isSameHeightHalfWidth = PdfDimensionFinder.isSameHeightHalfWidth(ratio, 0.1)
        if (!isSameHeightHalfWidth) {
            log.info("Not same height/half width dimensions1=${this.dimensionsInPoints}, dimensions2=${otherFile.dimensionsInPoints} file1=${this.file.normalize()}, file2=${otherFile.file.normalize()}")
        }
        return isSameHeightHalfWidth
    }

    Path getOriginalFileOrFile() {
        return originalFile == null ? file : originalFile
    }

    boolean isAHundredsSequenceStart() {
        // TODO the case that we've seen is STL/SOT (Southland Times) with 401, 402 sequences
        // so we have set this at 400 so that cases like 98, 100, 101 will catch missing files.
        if (sequenceNumber < 400) {
            return false
        }
        int hundredRemainder = sequenceNumber % 100

        return hundredRemainder == 0 || hundredRemainder == 1
    }
}