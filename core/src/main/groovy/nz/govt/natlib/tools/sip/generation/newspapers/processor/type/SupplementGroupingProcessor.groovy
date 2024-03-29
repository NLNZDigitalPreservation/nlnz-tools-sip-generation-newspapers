package nz.govt.natlib.tools.sip.generation.newspapers.processor.type

import groovy.util.logging.Log4j2
import nz.govt.natlib.tools.sip.generation.newspapers.NewspaperProcessingParameters
import nz.govt.natlib.tools.sip.generation.newspapers.NewspaperFile
import nz.govt.natlib.tools.sip.generation.newspapers.NewspaperType

/**
 * Does processing operations specific to the processing type
 * {@link nz.govt.natlib.tools.sip.generation.newspapers.parameters.ProcessingType#ParentGroupingWithEdition}
 */
@Log4j2
class SupplementGroupingProcessor {
    static List<NewspaperFile> selectAndSort(NewspaperProcessingParameters processingParameters,
                                             List<NewspaperFile> allPossibleFiles, NewspaperType newspaperType) {
        List<NewspaperFile> newspaperFiles = NewspaperFile.filterSubstituteAndSort(allPossibleFiles, processingParameters, newspaperType)

        List<String> allFileSectionCodes = NewspaperFile.allSectionCodes(allPossibleFiles)
        boolean hasAtLeastOneMissingSectionCode = processingParameters.sectionCodes.any { String sectionCode ->
            !allFileSectionCodes.contains(sectionCode)
        }
        if (hasAtLeastOneMissingSectionCode) {
            processingParameters.skip = true
            log.info("files sectionCodes=${allFileSectionCodes} does not contain all required section " +
                    "codes=${processingParameters.sectionCodes}, skipping processing for " +
                    "processingParameters=${processingParameters}")
        }

        // Return files if there are no sequence letters to process
        if (processingParameters.sequenceLetters.size() == 0) {
            return newspaperFiles
        }

        // Check if there are any files with sequence letter
        // If so, do not skip and only process sequence letter files
        List<NewspaperFile> sequenceLetterFiles = [ ]
        newspaperFiles.each { NewspaperFile newspaperFile ->
            if (processingParameters.sequenceLetters.contains(newspaperFile.sequenceLetter)) {
                sequenceLetterFiles.add(newspaperFile)
            }
        }

        if (sequenceLetterFiles.size() > 0) {
            newspaperFiles = sequenceLetterFiles
            processingParameters.skip = false
        } else {
            processingParameters.skip = true
            log.info("no sequence letter ${processingParameters.sequenceLetters} files, " +
                    "skipping processing for processingParameters=${processingParameters}")
        }

        return newspaperFiles
    }
}
