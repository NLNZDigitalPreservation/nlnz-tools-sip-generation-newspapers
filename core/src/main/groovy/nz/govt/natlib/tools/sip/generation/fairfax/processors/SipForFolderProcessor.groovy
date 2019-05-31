package nz.govt.natlib.tools.sip.generation.fairfax.processors

import nz.govt.natlib.tools.sip.generation.fairfax.FairfaxFile
import nz.govt.natlib.tools.sip.generation.fairfax.FairfaxProcessingParameters
import nz.govt.natlib.tools.sip.generation.fairfax.FairfaxSpreadsheet

class SipForFolderProcessor {
    static List<FairfaxFile> selectAndSort(FairfaxProcessingParameters processingParameters,
                                           List<FairfaxFile> allPossibleFiles) {
        List<String> editionCodes = FairfaxFile.allEditionCodes(allPossibleFiles).toList()
        processingParameters.editionCodes = editionCodes
        if (processingParameters.spreadsheetRow.isEmpty()) {
            processingParameters.spreadsheetRow = FairfaxSpreadsheet.BLANK_ROW
        }
        List<FairfaxFile> selectedAndSorted = FairfaxFile.sortWithSameTitleCodeAndDate(allPossibleFiles,
                processingParameters)

        return selectedAndSorted
    }
}
