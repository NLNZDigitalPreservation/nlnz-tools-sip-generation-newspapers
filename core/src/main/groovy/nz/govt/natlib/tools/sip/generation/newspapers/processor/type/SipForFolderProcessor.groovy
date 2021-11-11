package nz.govt.natlib.tools.sip.generation.newspapers.processor.type

import nz.govt.natlib.tools.sip.generation.newspapers.FairfaxProcessingParameters
import nz.govt.natlib.tools.sip.generation.newspapers.FairfaxSpreadsheet
import nz.govt.natlib.tools.sip.generation.newspapers.NewspaperFile

/**
 * Does processing operations specific to the processing type
 * {@link nz.govt.natlib.tools.sip.generation.newspapers.parameters.ProcessingType#CreateSipForFolder}
 */
class SipForFolderProcessor {
    static List<NewspaperFile> selectAndSort(FairfaxProcessingParameters processingParameters,
                                             List<NewspaperFile> allPossibleFiles) {
        List<String> sectionCodes = NewspaperFile.allSectionCodes(allPossibleFiles).toList()
        processingParameters.sectionCodes = sectionCodes
        if (processingParameters.spreadsheetRow.isEmpty()) {
            processingParameters.spreadsheetRow = FairfaxSpreadsheet.BLANK_ROW
        }
        List<NewspaperFile> selectedAndSorted = null
        // Sort list in ascending order if it doesn't contain a section code
        if (allPossibleFiles[0].getSectionCode() == null || allPossibleFiles[0].getSectionCode().isEmpty()) selectedAndSorted = allPossibleFiles.sort()
        else selectedAndSorted = NewspaperFile.sortWithSameTitleCodeAndDate(allPossibleFiles, processingParameters)

        return selectedAndSorted
    }
}
