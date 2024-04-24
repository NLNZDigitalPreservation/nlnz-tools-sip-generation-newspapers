package nz.govt.natlib.tools.sip.generation.newspapers.processor

import groovy.util.logging.Log4j2
import nz.govt.natlib.tools.sip.generation.newspapers.NewspaperFile
import nz.govt.natlib.tools.sip.generation.newspapers.NewspaperFileTitleEditionKey
import nz.govt.natlib.tools.sip.generation.newspapers.NewspaperSpreadsheet
import nz.govt.natlib.tools.sip.generation.newspapers.NewspaperType
import nz.govt.natlib.tools.sip.generation.newspapers.processor.support.TitleCodeByDateSummary
import nz.govt.natlib.tools.sip.pdf.PdfInformationExtractor
import nz.govt.natlib.tools.sip.utils.PathUtils

import java.nio.file.Path
import java.time.LocalDate
import java.time.Period

@Log4j2
class ReportsProcessor {
    ProcessorConfiguration processorConfiguration
    Set<String> recognizedTitleCodes = []
    Set<String> unrecognizedTitleCodes = []
    NewspaperType newspaperType

    ReportsProcessor(ProcessorConfiguration processorConfiguration) {
        this.processorConfiguration = processorConfiguration
    }

    void listFiles() {
        log.info("STARTING listFiles")
        this.newspaperType = new NewspaperType(processorConfiguration.newspaperType)

        // Clear the set of recognized and unrecognized names before processing begins
        recognizedTitleCodes = []
        unrecognizedTitleCodes = []
        Set<NewspaperFileTitleEditionKey> recognizedTitleCodeSectionCodes = []
        Set<NewspaperFileTitleEditionKey> unrecognizedTitleCodeSectionCodes = []
        Set<Path> invalidFiles = []

        log.info("sourceFolder=${processorConfiguration.sourceFolder}")

        NewspaperSpreadsheet newspaperSpreadsheet = NewspaperSpreadsheet.defaultInstance()
        Set<String> allNameKeys = newspaperSpreadsheet.allTitleCodeKeys
        Map<String, String> allSupplementKeys = newspaperSpreadsheet.allSupplementTitleCodes
        Set<NewspaperFileTitleEditionKey> allNameEditionKeys = newspaperSpreadsheet.allTitleCodeSectionCodeKeys

        boolean isRegexNotGlob = true
        boolean matchFilenameOnly = true
        boolean sortFiles = true
        String pattern = ".*?\\.[pP]{1}[dD]{1}[fF]{1}"
        List<Path> foundFiles = PathUtils.findFiles(processorConfiguration.sourceFolder.normalize().toString(),
                isRegexNotGlob, matchFilenameOnly, sortFiles, pattern, processorConfiguration.timekeeper)
        List<NewspaperFile> newspaperFiles = foundFiles.collect { Path file ->
            new NewspaperFile(file)
        }

        NewspaperFile previousFile
        newspaperFiles.each { NewspaperFile newspaperFile ->
            if (newspaperFile.isValidName()) {
                if (allNameKeys.contains(newspaperFile.titleCode.toUpperCase())) {
                    if (!recognizedTitleCodes.contains(newspaperFile.titleCode)) {
                        recognizedTitleCodes.add(newspaperFile.titleCode)
                        log.info("listFiles adding recognizedTitleCode=${newspaperFile.titleCode}")
                    }
                } else if (allSupplementKeys!= null && allSupplementKeys.size() > 0 &&
                        allSupplementKeys[newspaperFile.titleCode]) {
                    String parentTitleCode = allSupplementKeys.get(newspaperFile.titleCode)

                    if (!recognizedTitleCodes.contains(parentTitleCode)) {
                        recognizedTitleCodes.add(parentTitleCode)
                        log.info("listFiles adding recognizedTitleCode=${parentTitleCode} for pulbication ${newspaperFile.titleCode}")
                    }
                } else {
                    if (!unrecognizedTitleCodes.contains(newspaperFile.titleCode)) {
                        unrecognizedTitleCodes.add(newspaperFile.titleCode)
                        log.info("listFiles adding unrecognizedTitleCode=${newspaperFile.titleCode}")
                    }
                }
                NewspaperFileTitleEditionKey newspaperFileTitleEditionKey = new NewspaperFileTitleEditionKey(
                        titleCode: newspaperFile.titleCode, sectionCode: newspaperFile.sectionCode)
                if (allNameEditionKeys.contains(newspaperFileTitleEditionKey)) {
                    if (!recognizedTitleCodeSectionCodes.contains(newspaperFileTitleEditionKey)) {
                        recognizedTitleCodeSectionCodes.add(newspaperFileTitleEditionKey)
                        log.info("listFiles adding recognizedTitleCodeSectionCodes=${newspaperFileTitleEditionKey}")
                    }
                } else {
                    if (!unrecognizedTitleCodeSectionCodes.contains(newspaperFileTitleEditionKey)) {
                        unrecognizedTitleCodeSectionCodes.add(newspaperFileTitleEditionKey)
                        log.info("listFiles adding unrecognizedTitleCodeSectionCodes=${newspaperFileTitleEditionKey}")
                    }
                }
            } else {
                invalidFiles.add(newspaperFile.file)
            }

            if (previousFile != null) {
                if (previousFile.titleCode != newspaperFile.titleCode) {
                    println("* * * CHANGE OF PREFIX * * *")
                } else if (previousFile.sectionCode != newspaperFile.sectionCode) {
                    println("* * * CHANGE OF SECTION * * *")
                } else if (previousFile.dateYear != newspaperFile.dateYear &&
                        previousFile.dateMonthOfYear != newspaperFile.dateMonthOfYear &&
                        previousFile.dateDayOfMonth != newspaperFile.dateDayOfMonth) {
                    println("* * * CHANGE OF DATE * * *")
                }
            }
            println(newspaperFile)

            previousFile = newspaperFile
        }

        log.info("* * * *")
        log.info("Recognized tileCodes:")
        recognizedTitleCodes.each { String recognizedName ->
            log.info("    ${recognizedName}")
        }
        log.info("* * * *")
        log.info("Recognized titleCodes and sectionCodes:")
        recognizedTitleCodeSectionCodes.each { NewspaperFileTitleEditionKey newspaperFileNameEditionKey ->
            log.info("    ${newspaperFileNameEditionKey}")
        }
        log.info("* * * *")
        log.info("UNRECOGNIZED titleCodes:")
        unrecognizedTitleCodes.each { String recognizedName ->
            log.info("    ${recognizedName}")
        }
        log.info("* * * *")
        log.info("UNRECOGNIZED titleCodes and sectionCodes:")
        unrecognizedTitleCodeSectionCodes.each { NewspaperFileTitleEditionKey newspaperFileNameEditionKey ->
            log.info("    ${newspaperFileNameEditionKey}")
        }
        log.info("* * * *")
        log.info("INVALID files:")
        invalidFiles.each { Path file ->
            log.info("    ${file.normalize().toString()}")
        }
        log.info("* * * *")

        log.info("ENDING listFiles")
        processorConfiguration.timekeeper.logElapsed()
    }

    void statisticalAudit() {
        log.info("STARTING statisticalAudit")
        StringBuilder summaryTextBuilder = new StringBuilder()

        // Clear the set of recognized and unrecognized names before processing begins
        recognizedTitleCodes = []
        unrecognizedTitleCodes = []
        Set<NewspaperFileTitleEditionKey> recognizedTitleCodeSectionCodes = []
        Set<NewspaperFileTitleEditionKey> unrecognizedTitleCodeSectionCodes = []
        Set<Path> invalidFiles = []
        List<Tuple2<LocalDate, Integer>> totalsByDateList = [ ]

        log.info("sourceFolder=${processorConfiguration.sourceFolder}")

        NewspaperSpreadsheet newspaperSpreadsheet = NewspaperSpreadsheet.defaultInstance()
        Set<String> allNameKeys = newspaperSpreadsheet.allTitleCodeKeys
        Set<NewspaperFileTitleEditionKey> allNameEditionKeys = newspaperSpreadsheet.allTitleCodeSectionCodeKeys

        boolean isRegexNotGlob = true
        boolean matchFilenameOnly = true
        boolean sortFiles = true
        String pattern = ".*?\\.[pP]{1}[dD]{1}[fF]{1}"
        List<Path> foundFiles = PathUtils.findFiles(processorConfiguration.sourceFolder.normalize().toString(),
                isRegexNotGlob, matchFilenameOnly, sortFiles, pattern, processorConfiguration.timekeeper)
        Map<LocalDate, Map<String, TitleCodeByDateSummary>> dateToTitleCodeMap = [ : ]
        foundFiles.each { Path file ->
            NewspaperFile newspaperFile = new NewspaperFile(file)
            if (newspaperFile.isValidName()) {
                if (allNameKeys.contains(newspaperFile.titleCode.toUpperCase())) {
                    if (!recognizedTitleCodes.contains(newspaperFile.titleCode)) {
                        recognizedTitleCodes.add(newspaperFile.titleCode)
                        log.info("listFiles adding recognizedTitleCode=${newspaperFile.titleCode}")
                    }
                } else {
                    if (!unrecognizedTitleCodes.contains(newspaperFile.titleCode)) {
                        unrecognizedTitleCodes.add(newspaperFile.titleCode)
                        log.info("listFiles adding unrecognizedTitleCode=${newspaperFile.titleCode}")
                    }
                }
                NewspaperFileTitleEditionKey newspaperFileTitleEditionKey = new NewspaperFileTitleEditionKey(
                        titleCode: newspaperFile.titleCode, sectionCode: newspaperFile.sectionCode)
                if (allNameEditionKeys.contains(newspaperFileTitleEditionKey)) {
                    if (!recognizedTitleCodeSectionCodes.contains(newspaperFileTitleEditionKey)) {
                        recognizedTitleCodeSectionCodes.add(newspaperFileTitleEditionKey)
                        //log.info("listFiles adding recognizedTitleCodeSectionCodes=${newspaperFileTitleEditionKey}")
                    }
                } else {
                    if (!unrecognizedTitleCodeSectionCodes.contains(newspaperFileTitleEditionKey)) {
                        unrecognizedTitleCodeSectionCodes.add(newspaperFileTitleEditionKey)
                        //log.info("listFiles adding unrecognizedTitleCodeSectionCodes=${newspaperFileTitleEditionKey}")
                    }
                }

                LocalDate localDate = newspaperFile.date
                Map<String, TitleCodeByDateSummary> titleCodeToSummaryMap
                if (dateToTitleCodeMap.containsKey(localDate)) {
                    titleCodeToSummaryMap = dateToTitleCodeMap.get(localDate)
                } else {
                    titleCodeToSummaryMap = [:]
                    dateToTitleCodeMap.put(localDate, titleCodeToSummaryMap)
                }
                TitleCodeByDateSummary titleCodeByDateSummary
                if (titleCodeToSummaryMap.containsKey(newspaperFile.titleCode)) {
                    titleCodeByDateSummary = titleCodeToSummaryMap.get(newspaperFile.titleCode)
                } else {
                    titleCodeByDateSummary = new TitleCodeByDateSummary(localDate: localDate,
                            titleCode: newspaperFile.titleCode)
                    titleCodeToSummaryMap.put(newspaperFile.titleCode, titleCodeByDateSummary)
                }
                titleCodeByDateSummary.addFile(newspaperFile)
            } else {
                invalidFiles.add(file)
            }
        }

        log.info("* * * *")
        logAndAppend(summaryTextBuilder, "Recognized tileCodes:")
        recognizedTitleCodes.each { String recognizedName ->
            logAndAppend(summaryTextBuilder, "${recognizedName}")
        }
        log.info("* * * *")
        logAndAppend(summaryTextBuilder, "Recognized titleCode/sectionCode:")
        recognizedTitleCodeSectionCodes.each { NewspaperFileTitleEditionKey newspaperFileNameEditionKey ->
            logAndAppend(summaryTextBuilder, "${newspaperFileNameEditionKey.titleCode}/" +
                    "${newspaperFileNameEditionKey.sectionCode}")
        }
        log.info("* * * *")
        logAndAppend(summaryTextBuilder, "UNRECOGNIZED titleCodes:")
        unrecognizedTitleCodes.each { String recognizedName ->
            logAndAppend(summaryTextBuilder, "${recognizedName}")
        }
        log.info("* * * *")
        logAndAppend(summaryTextBuilder, "UNRECOGNIZED titleCode/sectionCode:")
        unrecognizedTitleCodeSectionCodes.each { NewspaperFileTitleEditionKey newspaperFileNameEditionKey ->
            logAndAppend(summaryTextBuilder, "${newspaperFileNameEditionKey.titleCode}/" +
                    "${newspaperFileNameEditionKey.sectionCode}")
        }
        log.info("* * * *")
        logAndAppend(summaryTextBuilder, "INVALID files:")
        invalidFiles.each { Path file ->
            logAndAppend(summaryTextBuilder, "${file.normalize().toString()}")
        }
        log.info("* * * *")

        println("Processing detail for sourceFolder=${processorConfiguration.sourceFolder.normalize().toString()}:")
        println()
        println("date|total_files|title_code|out-of-sequence-files|duplicate-files")
        String spreadsheetSeparator = "|"
        List<LocalDate> sortedDates = dateToTitleCodeMap.keySet().sort()
        LocalDate lastDate
        List<LocalDate> dateGaps = [ ]
        sortedDates.each { LocalDate dateKey ->
            if (lastDate != null) {
                Period sinceLastDate = Period.between(lastDate, dateKey)
                if (sinceLastDate.getDays() > 1) {
                    lastDate = lastDate.plusDays(1)
                    dateGaps.add(lastDate)
                    totalsByDateList.add(new Tuple2<LocalDate, Integer>(lastDate, 0))
                    while ((lastDate = lastDate.plusDays(1)) != dateKey) {
                        dateGaps.add(lastDate)
                        totalsByDateList.add(new Tuple2<LocalDate, Integer>(lastDate, 0))
                    }
                }
            }
            Map<String, TitleCodeByDateSummary> titleCodeToSummaryMap = dateToTitleCodeMap.get(dateKey)
            List<String> sortedTitleCodes = titleCodeToSummaryMap.keySet().sort()
            boolean firstForDate = true
            int totalFilesForDate = 0
            sortedTitleCodes.each { String titleCode ->
                TitleCodeByDateSummary titleCodeByDateSummary = titleCodeToSummaryMap.get(titleCode)
                if (firstForDate) {
                    print("${dateKey}")
                    firstForDate = false
                }
                println("${spreadsheetSeparator}${titleCodeByDateSummary.forSpreadsheet(spreadsheetSeparator)}")
                totalFilesForDate += titleCodeByDateSummary.files.size()
            }
            println("${spreadsheetSeparator}${totalFilesForDate}")
            totalsByDateList.add(new Tuple2<LocalDate, Integer>(dateKey, totalFilesForDate))
            lastDate = dateKey
        }

        if (dateGaps.size() > 0) {
            log.info("* * * *")
            logAndAppend(summaryTextBuilder, "DATE gaps (missing dates):")
            dateGaps.each { LocalDate localDate ->
                logAndAppend(summaryTextBuilder, "${localDate}")
            }
            log.info("* * * *")
        }

        println()
        println("Processing exceptions summary for sourceFolder=${processorConfiguration.sourceFolder.normalize()}:")
        println()
        println(summaryTextBuilder.toString())
        println()

        println("Date totals summary for sourceFolder=${processorConfiguration.sourceFolder.normalize()}:")
        println("Date|Total for date")
        totalsByDateList.each { Tuple2<LocalDate, Integer> dateTotalTuple ->
            println("${dateTotalTuple.first}|${dateTotalTuple.second}")
        }
        println()

        log.info("ENDING statisticalAudit")
        processorConfiguration.timekeeper.logElapsed()
    }

    void logAndAppend(StringBuilder stringBuilder, String message) {
        stringBuilder.append(message)
        stringBuilder.append(System.lineSeparator())
        log.info(message)
    }

    void extractMetadata() {
        log.info("STARTING extractMetadata doLast")
        boolean isRegexNotGlob = true
        boolean matchFilenameOnly = true
        boolean sortFiles = true
        boolean includeSubdirectories = true
        String pattern = ".*?\\.[pP]{1}[dD]{1}[fF]{1}"
        List<Path> pdfFiles = PathUtils.findFiles(processorConfiguration.sourceFolder.normalize().toString(),
                isRegexNotGlob, matchFilenameOnly, sortFiles, pattern, processorConfiguration.timekeeper,
                includeSubdirectories)

        pdfFiles.each { Path pdfFile ->
            log.info("* * * * *")
            log.info("${pdfFile.normalize().toString()} METADATA:")
            Map<String, String> pdfMetadata = PdfInformationExtractor.extractMetadata(pdfFile)
            pdfMetadata.each { String key, String value ->
                log.info("    key=${key}, value=${value}")
            }
            log.info("* * * * *")
            log.info("* * * * *")
            log.info("${pdfFile.normalize().toString()} TEXT:")
            String text = PdfInformationExtractor.extractText(pdfFile)
            log.info("${text}")
            log.info("* * * * *")
            log.info("* * * * *")
            log.info("")
        }

        processorConfiguration.timekeeper.logElapsed()
    }

}
