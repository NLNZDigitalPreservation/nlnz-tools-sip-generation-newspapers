package nz.govt.natlib.tools.sip.generation.newspapers.processor

import groovy.util.logging.Log4j2
import nz.govt.natlib.tools.sip.logging.Timekeeper

import java.nio.file.Path
import java.time.LocalDate

@Log4j2
trait ProcessorConfiguration {
//    final static LocalDate DEFAULT_STARTING_DATE = LocalDate.of(2015, 1, 1)
//    final static LocalDate DEFAULT_ENDING_DATE = LocalDate.now()

    boolean preProcess
    boolean readyForIngestion
    boolean listFiles

    boolean statisticalAudit
    boolean extractMetadata
    boolean copyIngestedLoadsToIngestedFolder

    boolean copyProdLoadToTestStructures

    boolean moveFiles
    boolean createDestination

    boolean parallelizeProcessing
    int numberOfThreads

    boolean moveOrCopyEvenIfNoRosettaDoneFile

    boolean includeDetailedTimings

    boolean verbose

    boolean helpRequested

    Timekeeper timekeeper

    LocalDate startingDate
    LocalDate endingDate

    Path sourceFolder
    Path targetFolder
    Path targetPreProcessingFolder
    Path targetForIngestionFolder
    Path targetPostProcessedFolder
    Path forReviewFolder

    String newspaperType
    String supplementPreviousIssuesFile
    String forIngestionProcessingTypes
    String forIngestionProcessingRules
    String forIngestionProcessingOptions

    List<ProcessorOption> processorOptions
}
