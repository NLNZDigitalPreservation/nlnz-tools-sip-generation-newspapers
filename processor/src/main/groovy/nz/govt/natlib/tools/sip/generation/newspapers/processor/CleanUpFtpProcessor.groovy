package nz.govt.natlib.tools.sip.generation.newspapers.processor;

import groovy.util.logging.Log4j2
import groovyx.gpars.GParsExecutorsPool
import nz.govt.natlib.tools.sip.generation.newspapers.NewspaperFile;
import nz.govt.natlib.tools.sip.generation.newspapers.NewspaperSpreadsheet;
import nz.govt.natlib.tools.sip.generation.newspapers.NewspaperType
import nz.govt.natlib.tools.sip.processing.ProcessLogger
import nz.govt.natlib.tools.sip.utils.GeneralUtils
import nz.govt.natlib.tools.sip.utils.PathUtils

import java.nio.file.Files;
import java.nio.file.Path
import java.time.LocalDate
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Log4j2
class CleanUpFtpProcessor {
    ProcessorConfiguration processorConfiguration
    NewspaperType newspaperType
    NewspaperSpreadsheet newspaperSpreadsheet

    CleanUpFtpProcessor(ProcessorConfiguration processorConfiguration) {
        this.processorConfiguration = processorConfiguration
    }

    List<NewspaperFile> filteredFiles(List<Path> allFilesList, LocalDate startingDate, LocalDate endingDate) {
        List<NewspaperFile> filteredList = new ArrayList<>()
        allFilesList.each { Path theFile ->
            NewspaperFile newspaperFile = new NewspaperFile(theFile, this.newspaperType)
            if (newspaperFile.date >= startingDate && newspaperFile.date <= endingDate) {
                filteredList.add(newspaperFile)
            }
        }

        return filteredList
    }

    void process() {
        ProcessLogger processLogger = new ProcessLogger()
        processLogger.startSplit()

        if (processorConfiguration.startingDate != null && processorConfiguration.endingDate != null) {
            print("\n\n\n\n\n\n\n\n\n\n\n")
            print("#######################################\n")
            print("##                                   ##\n")
            print("##           ACTION REQUIRED         ##\n")
            print("##                                   ##\n")
            print("#######################################\n")
            print("\n\n")
            print("#############################################################\n")
            print("##                                                         ##\n")
            print("## This process will permanently delete all matching files ##\n")
            print("##                                                         ##\n")
            print("#############################################################\n")
            print("\n\n")
            print("################################################################################\n")
            print("##                                                                            ##\n")
            print("## Please carefully review the date range and source folder before continuing ##\n")
            print("##                                                                            ##\n")
            print("################################################################################\n")
            print("\n\n")
            print("Are you sure you wish to permanently delete these files?\n")
            print("\n\n")
            print("From: ${processorConfiguration.startingDate}\n")
            print("To: ${processorConfiguration.endingDate}\n")
            print("From the folder: ${processorConfiguration.sourceFolder.normalize().toString()}\n")
            print("\n\n")
            print("Type 'confirm' to confirm and begin deleting\n")
            print("Type 'exit' to cancel:\n")

            def confirm = System.in.newReader().readLine()

            if (confirm == "confirm") {
                print("\n\n")
                log.info("START process for newspaperType=${processorConfiguration.newspaperType}, " +
                        "startingDate=${processorConfiguration.startingDate}, " +
                        "endingDate=${processorConfiguration.endingDate}, " +
                        "sourceFolder=${processorConfiguration.sourceFolder.normalize().toString()}, " +
                        processorConfiguration.timekeeper.logElapsed())

                this.newspaperType = new NewspaperType(processorConfiguration.newspaperType)
                this.newspaperSpreadsheet = NewspaperSpreadsheet.defaultInstance(newspaperType.PATH_TO_SPREADSHEET)

                boolean isRegexNotGlob = true
                boolean matchFilenameOnly = true
                boolean sortFiles = false

                String pattern = newspaperType.PDF_FILE_WITH_TITLE_SECTION_DATE_SEQUENCE_PATTERN
                DateTimeFormatter LOCAL_DATE_FOLDER_FORMATTER = DateTimeFormatter.ofPattern(newspaperType.DATE_TIME_PATTERN)
//        String pattern = NewspaperFile.PDF_FILE_WITH_TITLE_SECTION_DATE_SEQUENCE_PATTERN
                // Given that we could be dealing with 60,000+ files in the source directory, it's probably more efficient to
                // get them all at once
                List<Path> allFiles = PathUtils.findFiles(processorConfiguration.sourceFolder.normalize().toString(),
                        isRegexNotGlob, matchFilenameOnly, sortFiles, pattern, processorConfiguration.timekeeper)
                int allFilesFoundSize = allFiles.size()

                int numberOfThreads = processorConfiguration.parallelizeProcessing ? processorConfiguration.numberOfThreads : 1
                log.info("Spreading processing over numberOfThreads=${numberOfThreads}")

                ProcessingCounter filesProcessedCounter = new ProcessingCounter()
                ProcessingCounter filesDeleted = new ProcessingCounter()
                processorConfiguration.timekeeper.logElapsed(false, filesProcessedCounter.currentCount)

                List<NewspaperFile> filteredFiles = filteredFiles(allFiles, processorConfiguration.startingDate,
                        processorConfiguration.endingDate)
                // Clear allFiles (especially if large) -- we want this collection garbage collected out
                allFiles = null
                log.info("Deleting ${GeneralUtils.TOTAL_FORMAT.format(filteredFiles.size())} files")
                GParsExecutorsPool.withPool(numberOfThreads) {
                    filteredFiles.eachParallel { NewspaperFile newspaperFile ->
                        try {
                            if (Files.exists(newspaperFile.file)) {
                                Files.delete(newspaperFile.file)
                                GeneralUtils.printAndFlush(".")
                                filesDeleted.incrementCounter()
                            }
                            filesProcessedCounter.incrementCounter()
                            if (filesProcessedCounter.currentCount % 5000 == 0) {
                                GeneralUtils.printAndFlush("\n")
                                processorConfiguration.timekeeper.logElapsed(false, filesProcessedCounter.currentCount,
                                        true)
                            }
                        } catch (Exception e) {
                            log.error("Exception deleting newspaperFile=${newspaperFile}", e)
                        }
                    }
                }

                processorConfiguration.timekeeper.logElapsed(false, filesProcessedCounter.total, true)

                log.info("END processing for parameters:")
                log.info("    startindDate=${processorConfiguration.startingDate}")
                log.info("    endingDate=${processorConfiguration.endingDate}")
                log.info("    sourceFolder=${processorConfiguration.sourceFolder.normalize().toString()}")
                processorConfiguration.timekeeper.logElapsed()
                log.info("Files totals:")
                log.info("    found=${GeneralUtils.TOTAL_FORMAT.format(allFilesFoundSize)}")
                log.info("    reviewed=${GeneralUtils.TOTAL_FORMAT.format(filesProcessedCounter.total)}")
                log.info("    files deleted=${GeneralUtils.TOTAL_FORMAT.format(filesDeleted.total)}")
                int notDeleted = filesProcessedCounter.total - filesDeleted.total
                log.info("    NOT delted=${GeneralUtils.TOTAL_FORMAT.format(notDeleted)}")

            } else {
                log.info("Process cancelled by user")
            }
        } else {
            log.info("startingDate=${processorConfiguration.startingDate} and " +
                    "endingDate=${processorConfiguration.endingDate} have not been both specified")
        }
    }
}
