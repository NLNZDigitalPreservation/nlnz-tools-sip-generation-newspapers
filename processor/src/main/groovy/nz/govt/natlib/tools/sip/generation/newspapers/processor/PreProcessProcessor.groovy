package nz.govt.natlib.tools.sip.generation.newspapers.processor

import groovy.util.logging.Log4j2
import groovyx.gpars.GParsExecutorsPool
import nz.govt.natlib.tools.sip.generation.newspapers.NewspaperFile
import nz.govt.natlib.tools.sip.generation.newspapers.NewspaperSpreadsheet
import nz.govt.natlib.tools.sip.generation.newspapers.NewspaperType
import nz.govt.natlib.tools.sip.generation.newspapers.special.FindFiles
import nz.govt.natlib.tools.sip.processing.ProcessLogger
import nz.govt.natlib.tools.sip.utils.GeneralUtils
import nz.govt.natlib.tools.sip.utils.PathUtils

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock

@Log4j2
class PreProcessProcessor {
    ProcessorConfiguration processorConfiguration
    NewspaperType newspaperType
    NewspaperSpreadsheet newspaperSpreadsheet
    Set<String> recognizedTitleCodes = new ConcurrentHashMap<>().newKeySet()
    Set<String> unrecognizedTitleCodes = new ConcurrentHashMap<>().newKeySet()
    Set<Path> inProcessDestinationFiles = new ConcurrentHashMap().newKeySet()
    List<Path> unreadableFiles = new ArrayList<>()

    // Locks
    ReentrantLock folderCreationLock = new ReentrantLock()
    ReentrantLock inProcessDestinationFilesLock = new ReentrantLock()

    PreProcessProcessor(ProcessorConfiguration processorConfiguration) {
        this.processorConfiguration = processorConfiguration
    }

    boolean makeDirs(Path directory) {
        boolean result = false
        folderCreationLock.lock()
        try {
            Files.createDirectories(directory)
            result = true
        } catch (Exception ignored) {
            log.error("makeDirs unable to create directory " + directory.toString())
        } finally {
            folderCreationLock.unlock()
        }
        return result
    }

    void waitForNoInProcessDestinationFile(Path file) {
        int waitCount = 0
        while (inProcessDestinationFiles.contains(file)) {
            GeneralUtils.printAndFlush("\n")
            log.warn("inProcessDestinationFiles (size=${inProcessDestinationFiles.size()}) (waitCount=${waitCount}) " +
                    "already contains file=${file.normalize()}, waiting for it to clear.")
            sleep(2000)
            waitCount += 1
        }
    }

    void addInProcessDestinationFile(Path file) {
        waitForNoInProcessDestinationFile(file)
        inProcessDestinationFilesLock.lock()
        try {
            if (inProcessDestinationFiles.contains(file)) {
                GeneralUtils.printAndFlush("\n")
                log.warn("inProcessDestinationFiles already contains file=${file.normalize()} (multiple threads checking the same file)")
            } else {
                inProcessDestinationFiles.add(file)
            }
        } finally {
            inProcessDestinationFilesLock.unlock()
        }
    }

    void removeInProcessDestinationFile(Path file) {
        inProcessDestinationFilesLock.lock()
        try {
            if (inProcessDestinationFiles.contains(file)) {
                inProcessDestinationFiles.remove(file)
            } else {
                GeneralUtils.printAndFlush("\n")
                log.warn("inProcessDestinationFiles DOES NOT contain file=${file.normalize()} (multiple threads removing the same file)")
            }
        } finally {
            inProcessDestinationFilesLock.unlock()
        }
    }

    // Copy missing appendable supplements for other publications
    void copyAppendedTitleFile(List<String> appendedTitles, List<String> folders, String initials, NewspaperFile targetFile, Path folderPath) {
        String targetTitle = targetFile.titleCode
        String parentDirectory = folderPath.getParent().toString()

        for (String title : appendedTitles) {
            // Check if the file has already been processed for other publications
            for (String folder : folders) {
                if (Files.exists(Paths.get(parentDirectory + folder +
                        targetFile.getFilename().replace(targetTitle, initials + folder[1])))) {
                    log.info("copyOrMoveFileToPreProcessingDestination ${targetFile.getFilename()} " +
                            "already copied and processed in ${parentDirectory + folder}")
                    continue
                }
                // Check if an FP file for other publications already exists in the source directory
                Path copyPath = Paths.get(targetFile.getFile().getParent().toString() + File.separator +
                        targetFile.getFilename().replace(targetTitle, title))
                if (Files.exists(copyPath)) {
                    log.info("copyOrMoveFileToPreProcessingDestination Forever project file ${copyPath.toString()} exists")
                    continue
                }
                // Copy the file for other publications to destination folder
                Path destinationPath = Paths.get(parentDirectory + folder +
                        targetFile.getFilename().replace(targetTitle, initials + folder[1]))
                if (Files.notExists(destinationPath.getParent()) && !makeDirs(destinationPath.getParent())) {
                    log.error("copyOrMoveFileToPreProcessingDestination could not get directory " + destinationPath.getParent().toString())
                }
                if (Files.notExists(destinationPath)) {
                    log.info("copyOrMoveFileToPreProcessingDestination Copying Forever Project file from ${targetFile.getFilename()} to " +
                            "${destinationPath.toString()} for use in ${folder.substring(1, folder.length() - 1)}")
                    Files.copy(targetFile.file, destinationPath)
                }
            }
        }
    }

    boolean moveFileToDestination(Path destinationFile, NewspaperFile targetFile, boolean moveFile) {
        boolean moveToDestination = true

        if (Files.exists(destinationFile)) {
            if (Files.exists(destinationFile) && Files.exists(targetFile.file) &&
                    Files.isSameFile(destinationFile, targetFile.file)) {
                moveToDestination = false
                log.warn("copyOrMoveFileToPreProcessingDestination: NO move/copy -- source and target are the same " +
                        "PHYSICAL file!")
                log.warn("    sourceFile=${targetFile.file.normalize().toString()}")
                log.warn("    targetFile=${destinationFile.normalize()}")
            } else if (PathUtils.isSameFile(targetFile.file, destinationFile)) {
                moveToDestination = false
                if (processorConfiguration.verbose) {
                    log.info("Skipping moveFile=${moveFile} destinationFile=${destinationFile.normalize()} -- it already exists and is same file")
                } else {
                    GeneralUtils.printAndFlush("+")
                }
                if (moveFile) {
                    Files.delete(targetFile.file)
                    if (processorConfiguration.verbose) {
                        log.info("Deleting moveFile=${moveFile} destinationFile=${destinationFile.normalize()}")
                    } else {
                        GeneralUtils.printAndFlush("-")
                    }
                }
            } else {
                boolean couldAlreadyExist = true
                while (couldAlreadyExist) {
                    Path nonDuplicateFile = PathUtils.nonDuplicateFile(destinationFile)
                    GeneralUtils.printAndFlush("\n")
                    log.info("moveFile=${moveFile} destinationFile=${destinationFile.normalize()} -- same name but different file")
                    log.info("             moving to destinationFile=${nonDuplicateFile.normalize()}")
                    Path oldDestinationFile = destinationFile
                    destinationFile = nonDuplicateFile
                    addInProcessDestinationFile(nonDuplicateFile)
                    removeInProcessDestinationFile(oldDestinationFile)
                    if (Files.exists(destinationFile)) {
                        // another thread has already created this duplicate file
                        couldAlreadyExist = true
                    } else {
                        couldAlreadyExist = false
                    }
                }
            }
        }
        if (moveToDestination) {
            boolean useAtomicOption = true
            boolean moveOrCopyResult = PathUtils.atomicMoveOrCopy(moveFile, targetFile.file, destinationFile,
                    useAtomicOption, processorConfiguration.includeDetailedTimings)
            GeneralUtils.printAndFlush(moveOrCopyResult ? "." : "!")
        }
        removeInProcessDestinationFile(destinationFile)

        return moveToDestination
    }

    boolean copyOrMoveFileToPreProcessingDestination(Path destinationFolder, Path forReviewFolder, NewspaperFile targetFile,
                                                     String dateFolderName, boolean moveFile) {

        String titleCodeFolderName = newspaperType.CASE_SENSITIVE ? targetFile.titleCode : targetFile.titleCode.toUpperCase()
        String folderPath
        Set<String> allNameKeys = newspaperSpreadsheet.allTitleCodeKeys
        Map<String, String> allSupplementTitleCodes = newspaperSpreadsheet.allSupplementTitleCodes

        if (allNameKeys.contains(targetFile.titleCode.toUpperCase())) {
            // There's an entry in the spreadsheet for this titleCode
            // Goes to '<date>/<titleCode>/<file>'
            if (!recognizedTitleCodes.contains(targetFile.titleCode)) {
                recognizedTitleCodes.add(targetFile.titleCode)
                GeneralUtils.printAndFlush("\n")
                log.info("copyOrMoveFileToPreProcessingDestination adding titleCode=${targetFile.titleCode}")
            }
            folderPath = "${destinationFolder.normalize().toString()}${File.separator}${dateFolderName}${File.separator}${titleCodeFolderName}"

            // If the title exists as both a title code and a supplement code it is processed twice, once as its own title
            // and once into its parent title as a supplement
            if (allSupplementTitleCodes != null && allSupplementTitleCodes.size() > 0 && allSupplementTitleCodes[targetFile.titleCode]) {
                titleCodeFolderName = allSupplementTitleCodes.get(targetFile.titleCode)
                String parentFolderPath = "${destinationFolder.normalize().toString()}${File.separator}${dateFolderName}${File.separator}${titleCodeFolderName}"

                Path parentDestination = Path.of(parentFolderPath)

                if (Files.notExists(parentDestination) && !makeDirs(parentDestination)) {
                    log.warn("copyOrMoveFileToPreProcessingDestination parent supplement unable to get directory " + " " + targetFile.file.fileName + " " + parentDestination.toString())
                    return false
                }

                log.info("copyOrMoveFileToPreProcessingDestination adding ${targetFile.file.fileName} to ${titleCodeFolderName}")
                if (!recognizedTitleCodes.contains(titleCodeFolderName)) {
                    recognizedTitleCodes.add(titleCodeFolderName)
                    GeneralUtils.printAndFlush("\n")
                    log.info("copyOrMoveFileToPreProcessingDestination adding titleCode=${titleCodeFolderName}")
                }

                Path parentDestinationFile = parentDestination.resolve(targetFile.file.fileName)
                addInProcessDestinationFile(parentDestinationFile)

                boolean movedToParentDestination = moveFileToDestination(parentDestinationFile, targetFile, moveFile)
                if (!movedToParentDestination) {
                    return false;
                }
            }
        } else if (allSupplementTitleCodes != null && allSupplementTitleCodes.size() > 0 && allSupplementTitleCodes[targetFile.titleCode]) {
            GeneralUtils.printAndFlush("\n")
            log.info("copyOrMoveFileToPreProcessingDestination found Supplement ${targetFile.titleCode}")

            // Get the parent publication name of the supplement
            titleCodeFolderName = allSupplementTitleCodes.get(targetFile.titleCode)

            log.info("copyOrMoveFileToPreProcessingDestination adding ${targetFile.file.fileName} to ${titleCodeFolderName}")
            if (!recognizedTitleCodes.contains(titleCodeFolderName)) {
                recognizedTitleCodes.add(titleCodeFolderName)
                GeneralUtils.printAndFlush("\n")
                log.info("copyOrMoveFileToPreProcessingDestination adding titleCode=${titleCodeFolderName}")
            }

            folderPath = "${destinationFolder.normalize().toString()}${File.separator}${dateFolderName}${File.separator}${titleCodeFolderName}"
        } else {
            // There is no entry in the spreadsheet for this titleCode
            // Goes to 'UNKNOWN-TITLE-CODE/<date>/<file>'
            if (!unrecognizedTitleCodes.contains(targetFile.titleCode)) {
                unrecognizedTitleCodes.add(targetFile.titleCode)
                GeneralUtils.printAndFlush("\n")
                log.info("copyOrMoveFileToPreProcessingDestination adding unrecognizedName=${targetFile.titleCode}")
            }
            folderPath = "${forReviewFolder.normalize().toString()}${File.separator}UNKNOWN-TITLE-CODE${File.separator}${dateFolderName}"
        }

        Path destination = Path.of(folderPath)

        if (Files.notExists(destination) && !makeDirs(destination)) {
            log.warn("copyOrMoveFileToPreProcessingDestination unable to get directory " + " " + targetFile.file.fileName + " " + destination.toString())
            return false
        }

        Path destinationFile = destination.resolve(targetFile.file.fileName)
        addInProcessDestinationFile(destinationFile)

        boolean moveToDestination = moveFileToDestination(destinationFile, targetFile, moveFile)
        return moveToDestination
    }

    List<LocalDate> processingDates(LocalDate startingDate, LocalDate endingDate) {
        List<LocalDate> datesList = new ArrayList<>()
        LocalDate currentDate = startingDate
        while (currentDate <= endingDate) {
            datesList += currentDate
            currentDate = currentDate.plusDays(1L)
        }
        return datesList
    }

    List<NewspaperFile> filteredFiles(List<Path> allFilesList, LocalDate startingDate, LocalDate endingDate,
                                      boolean sortByDate) {
        List<NewspaperFile> filteredList = new ArrayList<>()
        allFilesList.each { Path theFile ->
            try {
                NewspaperFile newspaperFile = new NewspaperFile(theFile, this.newspaperType)
                if (newspaperFile.date >= startingDate && newspaperFile.date <= endingDate) {
                    filteredList.add(newspaperFile)
                }
            } catch (Exception e) {
                log.warn("Error reading file ${theFile}", e)
                unreadableFiles.add(theFile)
            }
        }

        if (sortByDate) {
            filteredList.sort() { NewspaperFile file1, NewspaperFile file2 -> file1.date <=> file2.date }
        }
        return filteredList
    }

    // See the README.md for folder descriptions and structures.
    void process() {
        // Clear the set of recognized and unrecognized names before processing begins
        recognizedTitleCodes = new ConcurrentHashMap<>().newKeySet()
        unrecognizedTitleCodes = new ConcurrentHashMap<>().newKeySet()

        ProcessLogger processLogger = new ProcessLogger()
        processLogger.startSplit()

        log.info("START process for newspaperType=${processorConfiguration.newspaperType}, " +
                "startingDate=${processorConfiguration.startingDate}, " +
                "endingDate=${processorConfiguration.endingDate}, " +
                "sourceFolder=${processorConfiguration.sourceFolder.normalize().toString()}, " +
                "forReviewFolder=${processorConfiguration.forReviewFolder.normalize().toString()}")
        processorConfiguration.timekeeper.logElapsed()

        if (processorConfiguration.createDestination) {
            Files.createDirectories(processorConfiguration.targetPreProcessingFolder)
            Files.createDirectories(processorConfiguration.forReviewFolder)
        }
        this.newspaperType = new NewspaperType(processorConfiguration.newspaperType)
        this.newspaperSpreadsheet = NewspaperSpreadsheet.defaultInstance(newspaperType.PATH_TO_SPREADSHEET)

        boolean isRegexNotGlob = true
        boolean matchFilenameOnly = true
        boolean sortFiles = true

        String pattern = newspaperType.PDF_FILE_WITH_TITLE_SECTION_DATE_SEQUENCE_PATTERN
        DateTimeFormatter LOCAL_DATE_FOLDER_FORMATTER = DateTimeFormatter.ofPattern(newspaperType.DATE_TIME_PATTERN)
//        String pattern = NewspaperFile.PDF_FILE_WITH_TITLE_SECTION_DATE_SEQUENCE_PATTERN
        // Given that we could be dealing with 60,000+ files in the source directory, it's probably more efficient to
        // get them all at once
        List<Path> allFiles
        if (processorConfiguration.processorOptions.contains(ProcessorOption.SearchWithoutDirectoryStream)) {
            allFiles = FindFiles.findFiles(processorConfiguration.sourceFolder.normalize().toString(),
                    isRegexNotGlob, pattern, processorConfiguration.timekeeper)
        } else {
              allFiles = PathUtils.findFiles(processorConfiguration.sourceFolder.normalize().toString(),
                isRegexNotGlob, matchFilenameOnly, sortFiles, pattern, processorConfiguration.timekeeper)
        }

        int allFilesFoundSize = allFiles.size()

        int numberOfThreads = processorConfiguration.parallelizeProcessing ? processorConfiguration.numberOfThreads : 1
        log.info("Spreading processing over numberOfThreads=${numberOfThreads}")

        ProcessingCounter filesProcessedCounter = new ProcessingCounter()
        ProcessingCounter filesMovedOrCopiedCounter = new ProcessingCounter()
        processorConfiguration.timekeeper.logElapsed(false, filesProcessedCounter.currentCount)
        if (processorConfiguration.startingDate != null && processorConfiguration.endingDate != null) {
            List<NewspaperFile> filteredFiles = filteredFiles(allFiles, processorConfiguration.startingDate,
                    processorConfiguration.endingDate, true)
            // Clear allFiles (especially if large) -- we want this collection garbage collected out
            allFiles = null
            log.info("Moving=${processorConfiguration.moveFiles} " +
                    "total files=${GeneralUtils.TOTAL_FORMAT.format(filteredFiles.size())} " +
                    "to destination=${processorConfiguration.targetPreProcessingFolder.normalize().toString()}")
            GParsExecutorsPool.withPool(numberOfThreads) {
                filteredFiles.eachParallel { NewspaperFile newspaperFile ->
                    try {
                        String dateString = LOCAL_DATE_FOLDER_FORMATTER.format(newspaperFile.date)
                        boolean moved = copyOrMoveFileToPreProcessingDestination(
                                processorConfiguration.targetPreProcessingFolder,
                                processorConfiguration.forReviewFolder, newspaperFile, dateString,
                                processorConfiguration.moveFiles)
                        if (moved) {
                            filesMovedOrCopiedCounter.incrementCounter()
                        }
                        filesProcessedCounter.incrementCounter()
                        if (filesProcessedCounter.currentCount % 5000 == 0) {
                            GeneralUtils.printAndFlush("\n")
                            processorConfiguration.timekeeper.logElapsed(false, filesProcessedCounter.currentCount,
                                    true)
                        }
                    } catch (Exception e) {
                        log.error("Exception processing newspaperFile=${newspaperFile}", e)
                    }
                }
                if (!unreadableFiles.isEmpty()) {
                    Path reviewFolder = Paths.get("${processorConfiguration.forReviewFolder.toString()}${File.separator}UNREADABLE-FILENAME")
                    if (!Files.exists(reviewFolder)) {
                        Files.createDirectories(reviewFolder)
                    }
                    log.info("Moving unreadable files to ${reviewFolder}")
                    GParsExecutorsPool.withPool(numberOfThreads) {
                        unreadableFiles.each { Path unreadableFile ->
                            def targetFile = reviewFolder.resolve(unreadableFile.fileName)
                            if (!Files.exists(targetFile)) {
                                Files.move(unreadableFile, targetFile)
                            }
                        }
                    }
                }
            }
        } else {
            log.info("startingDate=${processorConfiguration.startingDate} and " +
                    "endingDate=${processorConfiguration.endingDate} have not been both specified")

            List<Path> foundFiles = PathUtils.findNonMatchingFiles(
                    processorConfiguration.sourceFolder.normalize().toString(), isRegexNotGlob, matchFilenameOnly,
                    sortFiles, pattern, processorConfiguration.timekeeper)
            foundFiles.each { Path foundFile ->
                String dateString = "UNKNOWN-DATE"
                copyOrMoveFileToPreProcessingDestination(processorConfiguration.targetPreProcessingFolder,
                        processorConfiguration.forReviewFolder, new NewspaperFile(foundFile, newspaperType), dateString,
                        processorConfiguration.moveFiles)
            }
        }
        processorConfiguration.timekeeper.logElapsed(false, filesProcessedCounter.total, true)

        log.info("END processing for parameters:")
        log.info("    newspaperType=${processorConfiguration.newspaperType}")
        log.info("    startingDate=${processorConfiguration.startingDate}")
        log.info("    endingDate=${processorConfiguration.endingDate}")
        log.info("    sourceFolder=${processorConfiguration.sourceFolder.normalize().toString()}")
        log.info("    targetPreProcessingFolder=${processorConfiguration.targetPreProcessingFolder.normalize().toString()}")
        log.info("    forReviewFolder=${processorConfiguration.forReviewFolder.normalize().toString()}")
        processorConfiguration.timekeeper.logElapsed()
        log.info("Files totals:")
        log.info("    found=${GeneralUtils.TOTAL_FORMAT.format(allFilesFoundSize)}")
        log.info("    reviewed=${GeneralUtils.TOTAL_FORMAT.format(filesProcessedCounter.total)}")
        log.info("    files moved or copied=${GeneralUtils.TOTAL_FORMAT.format(filesMovedOrCopiedCounter.total)}")
        int notMovedOrCopied = filesProcessedCounter.total - filesMovedOrCopiedCounter.total
        log.info("    NOT moved or copied=${GeneralUtils.TOTAL_FORMAT.format(notMovedOrCopied)} (probably duplicates)")

        processLogger.copySplit(processorConfiguration.targetPreProcessingFolder, "Pre-Process-Processor", true)
    }
}
