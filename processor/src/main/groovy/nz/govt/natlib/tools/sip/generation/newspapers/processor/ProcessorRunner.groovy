package nz.govt.natlib.tools.sip.generation.newspapers.processor

import nz.govt.natlib.tools.sip.logging.DefaultTimekeeper
import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import groovy.util.logging.Log4j2

import java.nio.channels.ScatteringByteChannel
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDate
import java.util.concurrent.Callable

@Log4j2
@Command(description = 'Runs different processors based on command-line options.', name = 'processorRunner')
class ProcessorRunner implements ProcessorConfiguration, Callable<Void> {
//    final static LocalDate DEFAULT_STARTING_DATE = LocalDate.of(2015, 1, 1)
//    final static LocalDate DEFAULT_ENDING_DATE = LocalDate.now()

    boolean commandExecuted = false

    @Option(names = ["--preProcess"], description = """Group source files by date and titleCode.
Output is used by readyForIngestion.
Requires sourceFolder, targetPreProcessingFolder, forReviewFolder, newspaperType.
Uses startingDate, endingDate.
Optional createDestination, moveFiles, parallelizeProcessing, numberOfThreads.
This is a processing operation and must run exclusively of other processing operations.""")
    boolean preProcess = false

    @Option(names = ["--readyForIngestion"], description = """Process the source files.
Output is ready for ingestion by Rosetta.
Requires sourceFolder, targetForIngestionFolder, forReviewFolder, processingType, newspaperType.
Uses startingDate, endingDate.
Optional createDestination. Note that moveFiles is not supported at this time.
Optional parallelizeProcessing, numberOfThreads.
This is a processing operation and must run exclusively of other processing operations.""")
    boolean readyForIngestion = false

    @Option(names = ["--cleanUpFTP"], description = """Delete files from an FTP folder
Requires sourceFolder, newspaperType, startingDate, endingDate
This is a processing operation and must run exclusively of other processing operations.
PERMANENTLY DELETES ALL MATCHING FILES.""")
    boolean cleaUpFtp = false

    @Option(names = ["-l", "--listFiles" ], description = """List the source files in an organized way.
Requires sourceFolder.
This is a reporting operation and cannot be run with any other processing operations.""")
    boolean listFiles = false

    @Option(names = ["--statisticalAudit" ], description = """Statistical audit.
Search through the source folder and provide a statistical audit of the files found.
This is a reporting operation and cannot be run with any processing operations.""")
    boolean statisticalAudit

    @Option(names = ["--extractMetadata"], description = """Extract and list the metadata from the source files.
Requires sourceFolder.
This is a reporting operation and cannot be run with any other processing operations.""")
    boolean extractMetadata = false

    @Option(names = ["--copyIngestedLoadsToIngestedFolder" ], description = """Copy the ingested loads to ingested folder.
Requires sourceFolder, targetPostProcessedFolder, forReviewFolder.
Uses startingDate, endingDate.
Optional createDestination, moveFiles, moveOrCopyEvenIfNoRosettaDoneFile.
This is a processing operation and must run exclusively of other processing operations.""")
    boolean copyIngestedLoadsToIngestedFolder = false

    @Option(names = ["--copyProdLoadToTestStructures" ], description = """Copy the production load to test structures.
Requires sourceFolder, targetFolder.
Uses startingDate, endingDate.
This is a processing operation and must run exclusively of other processing operations.""")
    boolean copyProdLoadToTestStructures = false

    @Option(names = ["--moveFiles" ], description = """Whether files will be moved or copied.
Default is copy (false).""")
    boolean moveFiles = false

    @Option(names = ["-c", "--createDestination" ], description = """Whether destination (or target) folders will be created.
Default is no creation (false).""")
    boolean createDestination = false

    @Option(names = ["--parallelizeProcessing" ], description = """Run operations in parallel (if possible).
Operations that have components that can run in parallel currently are:
    --preProcess, --readyForIngestion""")
    boolean parallelizeProcessing = false

    @Option(names = ["--numberOfThreads"], paramLabel = "NUMBER_OF_THREADS",
            description = """Number of threads when running operations in parallel.
The default is 1.""")
    int numberOfThreads = 1

    @Option(names = ["--moveOrCopyEvenIfNoRosettaDoneFile" ],
            description = """Whether the move or copy takes place even if there is no Rosetta done file.
The Rosetta done files is a file with a titleCode of 'done'.
Default is no move or copy unless there IS a Rosetta done file (false).""")
    boolean moveOrCopyEvenIfNoRosettaDoneFile = false

    @Option(names = ["--detailedTimings"], description = """Include detailed timings (for specific operations).""")
    boolean includeDetailedTimings = false

    @Option(names = ["--verbose"], description = """Include verbose output.""")
    boolean verbose = false

    @Option(names = ["-h", "--help" ], usageHelp = true, description = 'Display a help message.')
    boolean helpRequested = false

    @Option(names = ["-b", "--startingDate"], paramLabel = "STARTING_DATE",
            description = """Starting date in the format yyyy-MM-dd (inclusive).
Dates are usually based on file name (not timestamp).
Default is 2015-01-01.""")
    // TODO Need a custom converter
    LocalDate startingDate

    @Option(names = ["-e", "--endingDate"], paramLabel = "ENDING_DATE",
            description = """Ending date in the format yyyy-MM-dd (inclusive).
Default is today. Files after this date are ignored.""")
    LocalDate endingDate

    @Option(names = ["-s", "--sourceFolder"], paramLabel = "SOURCE_FOLDER",
            description = """Source folder in the format /path/to/folder.
This folder must exist and must be a directory.""")
    Path sourceFolder

    @Option(names = ["--targetFolder"], paramLabel = "TARGET_FOLDER",
            description = """Target folder in the format /path/to/folder.
This is the destination folder used when no other destination folders are specified.
Use --createDestination to force its creation.""")
    Path targetFolder

    @Option(names = ["--targetPreProcessingFolder"], paramLabel = "TARGET_PRE_PROCESS_FOLDER",
            description = """Target pre-processing folder in the format /path/to/folder.
Use --createDestination to force its creation.""")
    Path targetPreProcessingFolder

    @Option(names = ["--targetForIngestionFolder"], paramLabel = "TARGET_FOR_INGESTION_FOLDER",
            description = """Target for-ingestion folder in the format /path/to/folder.
Use --createDestination to force its creation.""")
    Path targetForIngestionFolder

    @Option(names = ["--targetPostProcessedFolder"], paramLabel = "TARGET_POST_PROCESSED_FOLDER",
            description = """Target post-processed folder in the format /path/to/folder.
Use --createDestination to force its creation.""")
    Path targetPostProcessedFolder

    @Option(names = ["--forIngestionProcessingTypes"], paramLabel = "PROCESSING_TYPES",
            description = """Comma-separated list of for-ingestion processing types.
A pre-processing titleCode folder should only be processed once for a single processing type.
It may be possible for multiple processing types to apply to the same folder, producing different SIPs.""")
    String forIngestionProcessingTypes

    @Option(names = ["--forIngestionProcessingRules"], paramLabel = "PROCESSING_RULES",
            description = """For-ingestion processing rules.
A comma-separated list of rules. These rules will override any contradictory rules.""")
    String forIngestionProcessingRules

    @Option(names = ["--forIngestionProcessingOptions"], paramLabel = "PROCESSING_OPTIONS",
            description = """For-ingestion processing options.
A comma-separated list of options. These options will override any contradictory options.""")
    String forIngestionProcessingOptions

    @Option(names = ["--generalProcessingOptions"], paramLabel = "GENERAL_PROCESSING_OPTIONS",
            description = """General processing options.
A comma-separated list of options. These options will override any contradictory options.
These processing options may or may not be applied depending on the processing that takes place.
See the class ProcessorOption for a list of what those options are.""")
    String generalProcessingOptions

    // TODO Some of the existing options could be folded into this option (instead of keeping them separate)
    List<ProcessorOption> processorOptions

    @Option(names = ["-r", "--forReviewFolder"], paramLabel = "FOR_REVIEW_FOLDER",
            description = """For-review folder in the format /path/to/folder.
For processing exceptions, depending on processor.""")
    Path forReviewFolder

    @Option(names=['--newspaperType'], paramLabel = "NEWSPAPER_TYPE",
            description = """The publication type to be processed, e.g. AlliedPress""")
    String newspaperType

    @Option(names=['--supplementPreviousIssuesFile'], paramLabel = "SUPPLEMENT_PREVIOUS_ISSUES",
            description = """The location of the file which stores the previous issue number and date of particular
supplements for a newspaper type. Used for calculating the issue number of the latest issue when this is not included
in the filename. e.g the Forever Project supplement.""")
    String supplementPreviousIssuesFile = null

    static void main(String[] args) throws Exception {
        ProcessorRunner processorRunner = new ProcessorRunner()
        CommandLine.call(processorRunner, args)
        if (!processorRunner.commandExecuted) {
            String[] helpArgs = ['-h']
            CommandLine.call(processorRunner, helpArgs)
        }
    }

    @Override
    Void call() throws Exception {
        timekeeper = new DefaultTimekeeper()

        showParameters()

        process()
        return null
    }

    void showParameters() {
        log.info("")
        log.info("Parameters as set:")
        log.info("    Processing stages:")
        log.info("        preProcess=${preProcess}")
        log.info("        readyForIngestion=${readyForIngestion}")
        log.info("        copyIngestedLoadsToIngestedFolder=${copyIngestedLoadsToIngestedFolder}")
        log.info("        cleanUpFTP=${cleaUpFtp}")
        log.info("    Other types of processing:")
        log.info("        copyProdLoadToTestStructures=${copyProdLoadToTestStructures}")
        log.info("    Reporting:")
        log.info("        listFiles=${listFiles}")
        log.info("        statisticalAudit=${statisticalAudit}")
        log.info("        extractMetadata=${extractMetadata}")
        log.info("    Publication type:")
        log.info("        newspaperType=${newspaperType}")
        log.info("    Location of supplement issues file:")
        log.info("        supplementPreviousIssuesFile=${supplementPreviousIssuesFile}")
        log.info("    Source and target folders:")
        log.info("        sourceFolder=${sourceFolder}")
        log.info("        targetFolder=${targetFolder}")
        log.info("        targetPreProcessingFolder=${targetPreProcessingFolder}")
        log.info("        targetForIngestionFolder=${targetForIngestionFolder}")
        log.info("        targetPostProcessedFolder=${targetPostProcessedFolder}")
        log.info("        forReviewFolder=${forReviewFolder}")
        log.info("    Processing parameters:")
        log.info("        forIngestionProcessingTypes=${forIngestionProcessingTypes}")
        log.info("        forIngestionProcessingRules=${forIngestionProcessingRules}")
        log.info("        forIngestionProcessingOptions=${forIngestionProcessingOptions}")
        log.info("    Date scoping:")
        log.info("        startingDate=${startingDate}")
        log.info("        endingDate=${endingDate}")
        log.info("    Options:")
        log.info("        moveFiles=${moveFiles}")
        log.info("        createDestination=${createDestination}")
        log.info("        parallelizeProcessing=${parallelizeProcessing}")
        log.info("        numberOfThreads=${numberOfThreads}")
        log.info("        generalProcessingOptions=${generalProcessingOptions}")
        log.info("        moveOrCopyEvenIfNoRosettaDoneFile=${moveOrCopyEvenIfNoRosettaDoneFile}")
        log.info("        includeDetailedTimings=${includeDetailedTimings}")
        log.info("        verbose=${verbose}")
        log.info("")
    }

    void displayProcessingLegend() {
        log.info("")
        log.info("Processing legend:")
        log.info("    .  -- indicates a file has been processed (either moved or copied)")
        log.info("    !  -- indicates a move or copy operation was not successful")
        log.info("    :  -- indicates a folder has been processed (either moved or copied)")
        log.info("    +  -- indicates a duplicate pre-process file has been detected and is exactly the same as")
        log.info("          the target file. If --moveFiles has been specified the source file is deleted.")
        log.info("    #  -- indicates a duplicate folder has been detected and will be copied or moved with the name of the")
        log.info("          folder with a '-<number>' appended to it.")
        log.info("    *  -- indicates that a pre-process file already exists (and is the same) in the post-processing")
        log.info("          target directory. In this case, the file is either not processed (if a copy) or deleted in the")
        log.info("          source folder (if --moveFiles).")
        log.info("    ?  -- indicates that a pre-process file already exists (and is NOT the same) in the post-processing")
        log.info("          target directory. In this case, the file is either copied or moved to the for_review_folder")
        log.info("    -  -- indicates that a source file has been deleted. This can happen when:")
        log.info("              - When pre-processing and the file already exists and --moveFiles is specified.")
        log.info("    =  -- indicates that a source folder has been deleted. This can happen when:")
        log.info("              - When post-processing and --moveFiles, the parent folder of the 'done' file deleted.")
        log.info("")
    }

    void process() {
        this.processorOptions = ProcessorOption.extract(this.generalProcessingOptions, ",", [ ], true)

        int totalProcessingOperations = (copyProdLoadToTestStructures ? 1 : 0) + (preProcess ? 1 : 0) +
                (readyForIngestion ? 1 : 0) + (copyIngestedLoadsToIngestedFolder ? 1 : 0) + (cleaUpFtp ? 1 : 0)
        if (totalProcessingOperations > 1) {
            String message = "Only 1 processing operation (copyProdLoadToTestStructures, preProcess, " +
                    "readyForIngestion, cleanUpFTP or copyIngestedLoadsToIngestedFolder) can run at a time. " +
                    "Your command requests total processing operations=${totalProcessingOperations}. Please change your command."
            log.error(message)
            throw new ProcessorException(message)
        }
        int totalReportingOperations = (listFiles ? 1 : 0) + (statisticalAudit ? 1 : 0) + (extractMetadata ? 1 : 0)
        if (totalReportingOperations > 0 && totalProcessingOperations > 0) {
            String message = "Reporting operations (listFiles, statisticalAudit, extractMetadata) cannot be run with any processing operations."
            log.error(message)
            throw new ProcessorException(message)
        }
        if (sourceFolder != null && (!Files.exists(sourceFolder) || !Files.isDirectory(sourceFolder))) {
            String message = "sourceFolder=${sourceFolder.normalize()} must exist=${Files.exists(sourceFolder)} and must be directory=${Files.isDirectory(sourceFolder)}"
            log.error(message)
            throw new ProcessorException(message)
        }
        timekeeper.start()
        // Do the non-destructive options first
        if (listFiles) {
            if (sourceFolder == null) {
                String message = "listFiles requires sourceFolder"
                log.error(message)
                throw new ProcessorException(message)
            }
            ReportsProcessor reportsProcessor = new ReportsProcessor(this)
            reportsProcessor.listFiles()
            commandExecuted = true
        }
        if (statisticalAudit) {
            if (sourceFolder == null) {
                String message = "statisticalAudit requires sourceFolder"
                log.error(message)
                throw new ProcessorException(message)
            }
            ReportsProcessor reportsProcessor = new ReportsProcessor(this)
            reportsProcessor.statisticalAudit()
            commandExecuted = true
        }
        if (extractMetadata) {
            if (sourceFolder == null) {
                String message = "extractMetadata requires sourceFolder"
                log.error(message)
                throw new ProcessorException(message)
            }
            ReportsProcessor reportsProcessor = new ReportsProcessor(this)
            reportsProcessor.extractMetadata()
            commandExecuted = true
        }
        if (copyProdLoadToTestStructures) {
            if (sourceFolder == null) {
                String message = "copyProdLoadToTestStructures requires sourceFolder"
                log.error(message)
                throw new ProcessorException(message)
            }
            if (targetFolder == null) {
                String message = "copyProdLoadToTestStructures requires targetFolder"
                log.error(message)
                throw new ProcessorException(message)
            }
            displayProcessingLegend()
            MiscellaneousProcessor miscellaneousProcessor = new MiscellaneousProcessor(this)
            miscellaneousProcessor.copyProdLoadToTestStructures()
            commandExecuted = true
        }
        if (preProcess) {
            if (newspaperType == null) {
                String message = "preProcess requires newspaperType"
                log.error(message)
                throw new ProcessorException(message)
            }
            if (sourceFolder == null) {
                String message = "preProcess requires sourceFolder"
                log.error(message)
                throw new ProcessorException(message)
            }
            if (targetPreProcessingFolder == null) {
                String message = "preProcess requires targetPreProcessingFolder"
                log.error(message)
                throw new ProcessorException(message)
            }
            if (forReviewFolder == null) {
                String message = "preProcess requires forReviewFolder"
                log.error(message)
                throw new ProcessorException(message)
            }
            displayProcessingLegend()
            PreProcessProcessor preProcessProcessor = new PreProcessProcessor(this)
            preProcessProcessor.process()
            commandExecuted = true
        }
        if (readyForIngestion) {
            if (newspaperType == null) {
                String message = "readyForIngestion requires newspaperType"
                log.error(message)
                throw new ProcessorException(message)
            }
            if (sourceFolder == null) {
                String message = "readyForIngestion requires sourceFolder"
                log.error(message)
                throw new ProcessorException(message)
            }
            if (targetForIngestionFolder == null) {
                String message = "readyForIngestion requires targetForIngestionFolder"
                log.error(message)
                throw new ProcessorException(message)
            }
            if (forReviewFolder == null) {
                String message = "readyForIngestion requires forReviewFolder"
                log.error(message)
                throw new ProcessorException(message)
            }
            if (moveFiles) {
                String message = "readyForIngestion does not support moving files at this time."
                log.error(message)
                throw new ProcessorException(message)
            }
            if (forIngestionProcessingTypes == null || forIngestionProcessingTypes.strip().isEmpty()) {
                String message = "readyForIngestion requires forIngestionProcessingTypes"
                log.error(message)
                throw new ProcessorException(message)
            }
            displayProcessingLegend()
            ReadyForIngestionProcessor readyForIngestionProcessor = new ReadyForIngestionProcessor(this)
            readyForIngestionProcessor.process()
            commandExecuted = true
        }
        if (copyIngestedLoadsToIngestedFolder) {
            if (sourceFolder == null) {
                String message = "copyIngestedLoadsToIngestedFolder requires sourceFolder"
                log.error(message)
                throw new ProcessorException(message)
            }
            if (targetPostProcessedFolder == null) {
                String message = "copyIngestedLoadsToIngestedFolder requires targetPostProcessedFolder"
                log.error(message)
                throw new ProcessorException(message)
            }
            if (forReviewFolder == null) {
                String message = "copyIngestedLoadsToIngestedFolder requires forReviewFolder"
                log.error(message)
                throw new ProcessorException(message)
            }
            displayProcessingLegend()
            PostIngestionProcessor postIngestionProcessor = new PostIngestionProcessor(this)
            postIngestionProcessor.process()
            commandExecuted = true
        }
        if (cleaUpFtp) {
            if (newspaperType == null) {
                String message = "cleanUpFTP requires newspaperType"
                log.error(message)
                throw new ProcessorException(message)
            }
            if (sourceFolder == null) {
                String message = "cleanUpFTP requires sourceFolder"
                log.error(message)
                throw new ProcessorException(message)
            }
            CleanUpFTPProcessor cleanUpFTPProcessor = new CleanUpFTPProcessor(this)
            cleanUpFTPProcessor.process()
            commandExecuted = true
        }
    }
}
