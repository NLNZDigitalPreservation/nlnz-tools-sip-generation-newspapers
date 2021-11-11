package nz.govt.natlib.tools.sip.generation.newspapers.processor.type

import groovy.util.logging.Log4j2
import nz.govt.natlib.tools.sip.generation.newspapers.FairfaxProcessingParameters
import nz.govt.natlib.tools.sip.generation.newspapers.NewspaperFile
import nz.govt.natlib.tools.sip.generation.newspapers.parameters.ProcessingRule

/**
 * Does processing operations specific to the processing type
 * {@link nz.govt.natlib.tools.sip.generation.newspapers.parameters.ProcessingType#ParentGroupingWithEdition}
 */
@Log4j2
class ParentGroupingWithEditionProcessor {
    static List<NewspaperFile> selectAndSort(FairfaxProcessingParameters processingParameters,
                                             List<NewspaperFile> allPossibleFiles) {
        // We need to make adjustments to the processing parameters
        // currentEdition will have been set when iterating through editionDiscriminators
        boolean smartSubstitution = processingParameters.rules.contains(ProcessingRule.EditionDiscriminatorsUsingSmartSubstitute)
        if (smartSubstitution) {
            // Currently smart substitution is for something like QCM where we want to substitute, but each
            // edition discriminator has its own section code.
            // So we would have titleCode: QCM, with 3 separate editions:
            //       editionDiscriminator: ED1, section_codes: ED1
            //       editionDiscriminator: ED2, section_codes: ED2
            //       editionDiscriminator: ED3, section_codes: ED3
            // But we still want to substitute the pages in ED2 and ED3 over the ED1 pages
            // In order to do that, we find the FIRST edition discriminator and set the editionDiscriminators to the
            // FIRST edition discriminator and the current one
            String currentEdition = processingParameters.editionDiscriminators.first()
            List<String> sectionCodesMatchingEditionCode = sectionCodesMatchingEditionCode(currentEdition,
                    allPossibleFiles)
            if (!sectionCodesMatchingEditionCode.isEmpty()) {
                String firstEdition = sectionCodesMatchingEditionCode.first()
                if (firstEdition != currentEdition) {
                    List<String> oldDiscriminators = processingParameters.editionDiscriminators
                    processingParameters.editionDiscriminators = [ firstEdition ]
                    processingParameters.editionDiscriminators.addAll(oldDiscriminators)
                    Collections.replaceAll(processingParameters.sectionCodes, currentEdition, firstEdition)
                }
                processingParameters.currentEdition = currentEdition
            }
        }

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
        if (processingParameters.rules.contains((ProcessingRule.AllSectionsInSipRequired))) {
            List<String> sectionCodes = NewspaperFile.allSectionCodes(allPossibleFiles)
            boolean hasMoreSectionCodesThanNeeded = sectionCodes.any { String sectionCode ->
                !processingParameters.sectionCodes.contains(sectionCode)
            }
            if (hasMoreSectionCodesThanNeeded) {
                processingParameters.skip = true
                log.info("files sectionCodes=${sectionCodes} contains more parameters section " +
                        "codes=${processingParameters.sectionCodes}, skipping processing for " +
                        "processingParameters=${processingParameters}")
            }
        }

        return newspaperFiles
    }

    static List<String> sectionCodesMatchingEditionCode(String editionCode, List<NewspaperFile> allPossibleFiles) {
        List<String> allPossibleSectionCodes = allPossibleFiles.collect { NewspaperFile newspaperFile ->
            newspaperFile.sectionCode
        }.unique()
        // remove the last character, matchEdition would turn ED1 --> ED
        String matchEdition = editionCode.substring(0, editionCode.length() - 1)
        List<String> matchingCodes = allPossibleSectionCodes.findAll { String sectionCode ->
            matchEdition == sectionCode.substring(0, sectionCode.length() - 1)
        }
        return matchingCodes.sort()
    }
}
