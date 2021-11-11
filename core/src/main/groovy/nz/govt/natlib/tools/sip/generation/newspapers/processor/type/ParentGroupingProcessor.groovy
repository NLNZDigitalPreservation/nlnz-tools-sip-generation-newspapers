package nz.govt.natlib.tools.sip.generation.newspapers.processor.type

import groovy.util.logging.Log4j2
import nz.govt.natlib.tools.sip.generation.newspapers.NewspaperProcessingParameters
import nz.govt.natlib.tools.sip.generation.newspapers.NewspaperFile
import nz.govt.natlib.tools.sip.generation.newspapers.parameters.ProcessingRule

/**
 * Does processing operations specific to the processing type
 * {@link nz.govt.natlib.tools.sip.generation.newspapers.parameters.ProcessingType#ParentGrouping}
 */
@Log4j2
class ParentGroupingProcessor {
    static List<NewspaperFile> selectAndSort(NewspaperProcessingParameters processingParameters,
                                             List<NewspaperFile> allPossibleFiles) {
        List<NewspaperFile> newspaperFiles = NewspaperFile.filterSubstituteAndSort(allPossibleFiles, processingParameters)

        if (processingParameters.rules.contains(ProcessingRule.FirstSectionCodeRequiredForMatch)) {
            List<String> sectionCodes = NewspaperFile.allSectionCodes(newspaperFiles)
            if (newspaperFiles.size() > 0) {
                String firstSpreadsheetSectionCode = processingParameters.sectionCodes.first()
                if (firstSpreadsheetSectionCode != sectionCodes.first()) {
                    processingParameters.skip = true
                    log.info("firstSpreadsheetSectionCode=${firstSpreadsheetSectionCode} " +
                            "NOT equal to first file section code=${sectionCodes.first()}, " +
                            "skipping processing for processingParameters=${processingParameters}")
                }
            }
        }

        return newspaperFiles
    }

}
