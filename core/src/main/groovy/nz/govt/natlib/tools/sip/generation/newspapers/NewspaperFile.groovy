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

import java.awt.Point
import java.awt.geom.Point2D
import java.nio.file.Path
import java.time.LocalDate
import java.time.Year
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.SignStyle
import java.time.temporal.ChronoField
import java.util.regex.Matcher

/**
 * Encapsulates the characteristics of Newspaper file. Includes methods for operating on lists of such files.
 */
@Canonical
@Sortable(includes = ['titleCode', 'sectionCode', 'dateYear', 'dateMonthOfYear', 'dateDayOfMonth', 'sequenceLetter',
        'sequenceNumber', 'qualifier' ])
@ToString(includeNames = true, includePackage = false, includes = [ 'filename', 'file' ])
@EqualsAndHashCode(includes = [ 'titleCode', 'sectionCode', 'date', 'sequenceLetter', 'sequenceNumber' ])
@Log4j2
class NewspaperFile {

    static final Point UNDIMENSIONED = new Point(-1, -1)

    DateTimeFormatter LOCAL_DATE_TIME_FORMATTER

    Path file
    static NewspaperType newspaperType
    // This is for when the file gets replaced, such as when a zero-length pdf is replaced by another file.
    Path originalFile
    String filename
    String titleCode
    String sectionCode
    String editionCode
    Integer dateYear
    Integer dateMonthOfYear
    Integer dateDayOfMonth
    LocalDate date
    Integer issueNumber
    Integer issueYear
    Integer issue
    String sequenceLetter
    String sequenceNumberString
    Integer sequenceNumber
    String qualifier
    String revision
    boolean validForProcessing
    boolean validPdf
    Point dimensionsInPoints = UNDIMENSIONED
    boolean zeroLengthFile = false

    static List<NewspaperFile> differences(List<NewspaperFile> list1, List<NewspaperFile> list2) {
//        List<NewspaperFile> list1MinusList2 = CollectionUtils.subtract(list1, list2)
        List<NewspaperFile> list1MinusList2 = new ArrayList<NewspaperFile>(list1)
        list1MinusList2.removeIf {nf -> list2.stream().anyMatch {nf2 -> nf.filename == nf2.filename }}

//        List<NewspaperFile> list2MinusList1 = CollectionUtils.subtract(list2, list1)
        List<NewspaperFile> list2MinusList1 = new ArrayList<NewspaperFile>(list2)
        list2MinusList1.removeIf {nf -> list1.stream().anyMatch {nf2 -> nf.filename == nf2.filename }}

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

    static boolean currentSectionCodeFile(String key, NewspaperFile file, NewspaperProcessingParameters parameters) {
        if (key != file.sectionCode && !parameters.matchesCurrentSection(key, file.sectionCode)) {
            return false
        }
        // Do not process sequence letter file as part of the section code files
        if (!parameters.sequenceLetters.isEmpty() && parameters.sequenceLetters.contains(file.sequenceLetter)) {
            return false
        }
        return true
    }

    // The assumption here is that the list of files is only different in sectionCode, sequenceLetter, sequenceNumber
    // and qualifier.
    // Except in the case of a substitution, where the original sectionCode and the substitute sectionCode are treated
    // as if they are the same.
    static List<NewspaperFile> sortWithSameTitleCodeAndDate(List<NewspaperFile> files,
                                                            NewspaperProcessingParameters processingParameters) {

//        if (!processingParameters.ignoreSequence.isEmpty()) {
//            List<NewspaperFile> ignoredRemoved = []
//            files.each {NewspaperFile newspaperFile ->
//                boolean ignoreFile = false
//                processingParameters.ignoreSequence.each { String ignoreLetter ->
//                    if (newspaperFile.sequenceLetter == ignoreLetter) {
//                        ignoreFile = true
//                    }
//                }
//                if (!ignoreFile) {
//                    ignoredRemoved.push(newspaperFile)
//                }
//            }
//            files = ignoredRemoved
//        }

        // FIRST: Order by sectionCode as per processingParameters
        Map<String, List<NewspaperFile>> filesBySection = [:]
        processingParameters.sectionCodes.each { String sectionCode ->
            List<NewspaperFile> sectionFiles = [ ]
            filesBySection.put(sectionCode, sectionFiles)
            files.each { NewspaperFile newspaperFile ->
                if (currentSectionCodeFile(sectionCode, newspaperFile, processingParameters)) {
                    sectionFiles.add(newspaperFile)
                }
            }
        }

        if (!processingParameters.supplementTitleCodes.isEmpty()) {
            if (filesBySection.isEmpty()) {
                List<NewspaperFile> tileCodeFiles = []
                filesBySection.put(processingParameters.titleCode, tileCodeFiles)
            }
            processingParameters.supplementTitleCodes.each { String stc ->
                List<NewspaperFile> supplementFiles = []
                filesBySection.put(stc, supplementFiles)
                files.each { NewspaperFile newspaperFile ->
                    if (stc == newspaperFile.titleCode) {
                        supplementFiles.add(newspaperFile)
                    }
                    if (filesBySection.containsKey(processingParameters.titleCode) &&
                            newspaperFile.titleCode == processingParameters.titleCode &&
                    !filesBySection.get(processingParameters.titleCode).contains(newspaperFile)) {
                        filesBySection.get(processingParameters.titleCode).add(newspaperFile)
                    }
                }
            }
        }

        // Create section for sequence letter
        if (!processingParameters.sequenceLetters.isEmpty()) {
            processingParameters.sequenceLetters.each { String sequenceLetter ->
                List<NewspaperFile> sequenceLetterFiles = []
                filesBySection.put(sequenceLetter, sequenceLetterFiles)
                files.each { NewspaperFile newspaperFile ->
                    if (sequenceLetter == newspaperFile.sequenceLetter) {
                        sequenceLetterFiles.add(newspaperFile)
                    }
                }
            }
        }

        // NEXT: Sort each sectionCode by numberAndAlpha
        boolean alphaBeforeNumeric = processingParameters.options.contains(ProcessingOption.AlphaBeforeNumericSequencing)
        filesBySection.keySet().each  { String sectionCode ->
            List<NewspaperFile> sectionFiles = filesBySection.get(sectionCode)
            sectionFiles = sortNumericAndAlpha(sectionFiles, alphaBeforeNumeric)
            filesBySection.put(sectionCode, sectionFiles)
        }
        List<NewspaperFile> sorted = [ ]
        filesBySection.keySet().each { String sectionCode ->
            sorted.addAll(filesBySection.get(sectionCode))
        }
        if (sorted.size() != files.size()) {
            log.warn("Not all sorted files exist in final list, differences=${differences(sorted, files)}")
        }
        return sorted
    }

    static List<NewspaperFile> postMissingSequenceFiles(List<NewspaperFile> files,
                                                        NewspaperProcessingParameters processingParameters,
                                                        NewspaperType newspaperType) {
        List<NewspaperFile> sorted = null
        // Sort list in ascending order if it doesn't contain a section code
        if (files[0] && (files[0].getSectionCode() == null || files[0].getSectionCode().isEmpty()) &&
        processingParameters.supplementTitleCodes.isEmpty()) {
            if (newspaperType.CASE_SENSITIVE) {
                sorted = files.sort()
            } else {
                sorted = files.sort {it.filename.toLowerCase()}
            }
        }

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
                    }  else if ((newspaperType.SUPPLEMENTS != null && newspaperType.SUPPLEMENTS[testFile.titleCode]) ||
                            (newspaperType.PARENT_SUPPLEMENTS != null && newspaperType.PARENT_SUPPLEMENTS[testFile.titleCode] ||
                            !processingParameters.supplementTitleCodes.isEmpty())
                    ) {
                        // This not a skip in sequence, these files have a different a title code to their
                        // parent_publication
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

    static List<NewspaperFile> substituteAllFor(String sourceSectionCode, String replacementSectionCode, String titleCode,
                                                List<String> allSectionCodes, List<NewspaperFile> possibleFiles) {
        List<NewspaperFile> substituted = []
        List<String> otherSectionCodes = allSectionCodes.findAll { String sectionCode ->
            sectionCode != sourceSectionCode && sectionCode != replacementSectionCode
        }
        possibleFiles.each { NewspaperFile newspaperFile ->
            if ((newspaperType.SUPPLEMENTS != null && newspaperType.SUPPLEMENTS[newspaperFile.titleCode]) ||
                (newspaperType.PARENT_SUPPLEMENTS != null &&
                            newspaperType.PARENT_SUPPLEMENTS[newspaperFile.titleCode] &&
                            newspaperFile.titleCode != titleCode)
                && newspaperFile.sectionCode == replacementSectionCode
            ) {
                substituted.add(newspaperFile)
            } else if (!otherSectionCodes.contains(newspaperFile.sectionCode)) {
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
            sectionCodes.contains(newspaperFile.editionCode)
        }
        if (possibleFiles.size() != filtered.size()) {
            log.warn("Not all filtered files exist in final list, differences=${differences(possibleFiles, filtered)}")
        }
        return filtered
    }

    static List<NewspaperFile> replaceUpdatedEditionPages(List<NewspaperFile> allPossibleFiles, List<String> editionCodes) {
        def updates = [:]
        allPossibleFiles.forEach {newspaperFile ->
            if (editionCodes.contains(newspaperFile.editionCode)) {
                if (!updates[newspaperFile.sequenceNumber]) {
                    updates[newspaperFile.sequenceNumber] = newspaperFile.editionCode
                } else if (newspaperFile.editionCode > (updates[newspaperFile.sequenceNumber] as String)) {
                    updates[newspaperFile.sequenceNumber] = newspaperFile.editionCode
                }
            }
        }

        // Add all pages to a new list, if there are revisions add only the latest revision to the list
        List<NewspaperFile> filesWithUpdates = [ ]
        if (updates.size() > 0) {
            allPossibleFiles.forEach { newspaperFile ->
                if (!updates[newspaperFile.sequenceNumber]) {
                    filesWithUpdates.push(newspaperFile)
                } else if (updates[newspaperFile.sequenceNumber] == newspaperFile.editionCode) {
                    filesWithUpdates.push(newspaperFile)
                }
            }
        } else {
            filesWithUpdates = allPossibleFiles
        }

        return filesWithUpdates
    }

    static List<NewspaperFile> replaceRevisions(List<NewspaperFile> allPossibleFiles, NewspaperType newspaperType) {
        // Find all pages with a revision
        def revisions = [:]
        allPossibleFiles.forEach {newspaperFile ->
            if (newspaperFile.revision.length() > 0) {
                if (!revisions[newspaperFile.sequenceNumber]) {
                    revisions[newspaperFile.sequenceNumber] = newspaperFile.revision
                } else if (getRevisionNumber(newspaperFile.revision, newspaperType) >
                        getRevisionNumber(revisions[newspaperFile.sequenceNumber] as String, newspaperType)) {
                    revisions[newspaperFile.sequenceNumber] = newspaperFile.revision
                }
            }
        }

        // Add all pages to a new list, if there are revisions add only the latest revision to the list
        List<NewspaperFile> filesWithRevisions = [ ]
        if (revisions.size() > 0) {
            allPossibleFiles.forEach { newspaperFile ->
                if (!revisions[newspaperFile.sequenceNumber]) {
                    filesWithRevisions.push(newspaperFile)
                } else if (revisions[newspaperFile.sequenceNumber] == newspaperFile.revision) {
                    filesWithRevisions.push(newspaperFile)
                }
            }
        } else {
            filesWithRevisions = allPossibleFiles
        }

        return filesWithRevisions
    }

    static Integer getRevisionNumber(String revisionString, NewspaperType newspaperType) {
        return revisionString.replace(newspaperType.REVISIONS, "").toInteger()
    }

    static List<NewspaperFile> removeIgnored(List<NewspaperFile> allPossibleFiles, NewspaperType newspaperType) {
        // Check that the there is more than one occurrence of the page before removing it - it's possible an ignore
        // string could be in the qualifier unrelatedly
        def occurrences = [:]
        allPossibleFiles.forEach {newspaperFile ->
            if (!occurrences[newspaperFile.sequenceNumber]) {
                occurrences[newspaperFile.sequenceNumber] = 1
            } else {
                occurrences[newspaperFile.sequenceNumber] += 1
            }
        }

        List<NewspaperFile> filesWithIgnoredRemoved = [ ]

        allPossibleFiles.forEach {newspaperFile ->
            if (occurrences[newspaperFile.sequenceNumber] > 1) {
                boolean containsIgnore = false
                newspaperType.IGNORE.forEach { String ignore ->
                    if (newspaperFile.qualifier.toUpperCase().contains(ignore)) {
                        containsIgnore = true
                    }
                }
                if (!containsIgnore) {
                    filesWithIgnoredRemoved.push(newspaperFile)
                }
            } else {
                filesWithIgnoredRemoved.push(newspaperFile)
            }
        }

        return filesWithIgnoredRemoved
    }

    static List<NewspaperFile> filterSubstituteAndSort(List<NewspaperFile> allPossibleFiles,
                                                       NewspaperProcessingParameters processingParameters,
                                                       NewspaperType newspaperType) {
        List<NewspaperFile> filteredSubstitutedAndSorted

        if (!processingParameters.ignoreSequence.isEmpty()) {
            List<NewspaperFile> ignoredRemoved = []
            allPossibleFiles.each {NewspaperFile newspaperFile ->
                boolean ignoreFile = false
                processingParameters.ignoreSequence.each { String ignoreLetter ->
                    if (newspaperFile.sequenceLetter == ignoreLetter) {
                        ignoreFile = true
                    }
                }
                if (!ignoreFile) {
                    ignoredRemoved.push(newspaperFile)
                }
            }
            allPossibleFiles = ignoredRemoved
        }

        if (processingParameters.currentEdition != null && !processingParameters.editionDiscriminators.isEmpty()) {
            // First we filter so we only have the files we want to process
            List<NewspaperFile> filtered = filterAllFor(processingParameters.validSectionCodes(), allPossibleFiles)
            // Then we do the substitutions
            // Substitutions happen if the FIRST editionDiscriminator has a substitution with the same date/sequenceLetter/sequenceNumber
            String firstDiscriminatorCode = processingParameters.editionDiscriminators.first()

            boolean hasSubstitutions = hasSubstitutions(processingParameters.currentEdition, filtered)
            if (hasSubstitutions) {
                List<NewspaperFile> substituted = substituteAllFor(firstDiscriminatorCode,
                        processingParameters.currentEdition, processingParameters.titleCode,
                        processingParameters.editionDiscriminators, filtered)
                // Then we sort so the ordering is correct
                // Sort list in ascending order if it doesn't contain a section code
                if (substituted[0].getSectionCode() == null || substituted[0].getSectionCode().isEmpty()) filteredSubstitutedAndSorted = substituted.sort()
                else filteredSubstitutedAndSorted = sortWithSameTitleCodeAndDate(substituted, processingParameters)
            } else {
                // If there are no substitutions (including the first for itself) then there is nothing to process
//                filteredSubstitutedAndSorted = [ ]
                if (filtered[0].getSectionCode() == null || filtered[0].getSectionCode().isEmpty()) filteredSubstitutedAndSorted = filtered.sort()
                else filteredSubstitutedAndSorted = sortWithSameTitleCodeAndDate(filtered, processingParameters)
            }
        } else if (!processingParameters.editionCodes.isEmpty() &&
                (processingParameters.editionDiscriminators == null || processingParameters.editionDiscriminators.isEmpty())) {
            // There are potential pages from a newer addition which should be swapped in for the older pages
            List<NewspaperFile> filtered = replaceUpdatedEditionPages(allPossibleFiles, processingParameters.editionCodes)
            if (filtered[0].getSectionCode() || filtered[0].getSectionCode().isEmpty()) filteredSubstitutedAndSorted = filtered.sort()
            else filteredSubstitutedAndSorted = sortWithSameTitleCodeAndDate(filtered, processingParameters)
        } else if (newspaperType.REVISIONS != null) {
            List<NewspaperFile> filtered = replaceRevisions(allPossibleFiles, newspaperType)
            if (filtered[0].getSectionCode() || filtered[0].getSectionCode().isEmpty()) filteredSubstitutedAndSorted = filtered.sort()
            else filteredSubstitutedAndSorted = sortWithSameTitleCodeAndDate(filtered, processingParameters)
        } else {
            // Sort list in ascending order if it doesn't contain a section code
            if (allPossibleFiles[0].getSectionCode() == null || allPossibleFiles[0].getSectionCode().isEmpty() &&
                    processingParameters.supplementTitleCodes.isEmpty()) filteredSubstitutedAndSorted = allPossibleFiles.sort()
            else filteredSubstitutedAndSorted = sortWithSameTitleCodeAndDate(allPossibleFiles, processingParameters)
        }

        if (newspaperType.IGNORE != null) {
            filteredSubstitutedAndSorted = removeIgnored(filteredSubstitutedAndSorted, newspaperType)
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

    static List<NewspaperFile> fromSourceFolder(Path sourceFolder, NewspaperType newspaperType) {
        String pattern = newspaperType.PDF_FILE_WITH_TITLE_SECTION_DATE_SEQUENCE_PATTERN
        boolean isRegexNotGlob = true
        boolean matchFilenameOnly = true
        boolean sortFiles = true

        log.info("Processing for pattern=${pattern}, sourceFolder=${sourceFolder.normalize()}")

        // Note that we only want the current directory and we don't want info messages
        List<Path> allFiles = PathUtils.findFiles(sourceFolder.normalize().toString(),
                isRegexNotGlob, matchFilenameOnly, sortFiles, pattern, null, false, true)
        List<NewspaperFile> onlyNewspaperFiles = [ ]
        allFiles.each { Path file ->
            NewspaperFile newspaperFile = new NewspaperFile(file, newspaperType)
            // TODO We have no checks here for NewspaperFile validity -- the pattern supposedly selects only validly named ones.
            onlyNewspaperFiles.add(newspaperFile)
        }
        return onlyNewspaperFiles
    }

    static List<String> uniqueSectionCodes(List<NewspaperFile> newspaperFiles) {
        Set<String> uniqueCodes = [ ]
        newspaperFiles.each { NewspaperFile file ->
            uniqueCodes.add(file.editionCode)
        }
        return uniqueCodes.toList()
    }

    static List<String> asFilenames(List<NewspaperFile> files) {
        return files.collect { NewspaperFile newspaperFile ->
            newspaperFile.file.fileName.toString()
        }
    }

    NewspaperFile(Path file, NewspaperType newspaperType) {
        this.file = file
        this.newspaperType = newspaperType
        populate()
    }

    Sip.FileWrapper generateFileWrapper() {
        return SipFileWrapperFactory.generate(this.file)
    }

    private populate() {
        this.filename = file.fileName.toString()
        this.LOCAL_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(newspaperType.DATE_TIME_PATTERN)

        Matcher matcher = filename =~ /${newspaperType.PDF_FILE_WITH_TITLE_SECTION_DATE_SEQUENCE_GROUPING_PATTERN}/
        if (matcher.matches()) {
            this.titleCode = matcher.group('titleCode')
            this.sectionCode = matcher.group('sectionCode')
            this.editionCode = matcher.group("editionCode")
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

            String issueString = matcher.group('issue')
            if (issueString.length() > 0) {
                this.issue = Integer.parseInt(issueString)
                this.issueNumber = Integer.parseInt(issueString.substring(0,2))
                Year year = Year.parse(issueString.substring(2,4), "yy")
                this.issueYear = year.getValue()
            }
            this.sequenceLetter = matcher.group('sequenceLetter')
            this.sequenceNumberString = matcher.group('sequenceNumber')
            this.sequenceNumber = sequenceNumberString.length() > 0 ? Integer.parseInt(sequenceNumberString) : 0
            this.qualifier = matcher.group('qualifier')
            this.revision = matcher.group('revision')
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
        return this.titleCode == newspaperFile.titleCode && this.date == newspaperFile.date &&
                this.sequenceLetter == newspaperFile.sequenceLetter &&
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
