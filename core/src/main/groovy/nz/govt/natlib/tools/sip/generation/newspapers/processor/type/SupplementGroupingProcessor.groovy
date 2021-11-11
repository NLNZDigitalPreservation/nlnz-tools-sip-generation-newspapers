package nz.govt.natlib.tools.sip.generation.newspapers.processor.type

import groovy.util.logging.Log4j2
import nz.govt.natlib.tools.sip.generation.newspapers.NewspaperProcessingParameters
import nz.govt.natlib.tools.sip.generation.newspapers.NewspaperFile

/**
 * Does processing operations specific to the processing type
 * {@link nz.govt.natlib.tools.sip.generation.newspapers.parameters.ProcessingType#ParentGroupingWithEdition}
 */
@Log4j2
class SupplementGroupingProcessor {
    static List<NewspaperFile> selectAndSort(NewspaperProcessingParameters processingParameters,
                                             List<NewspaperFile> allPossibleFiles) {
        List<NewspaperFile> newspaperFiles = NewspaperFile.filterSubstituteAndSort(allPossibleFiles, processingParameters)

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

        return newspaperFiles
    }
}
