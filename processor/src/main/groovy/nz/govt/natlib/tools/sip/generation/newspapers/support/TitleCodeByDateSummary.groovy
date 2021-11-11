package nz.govt.natlib.tools.sip.generation.newspapers.support

import groovy.transform.Canonical
import groovy.transform.EqualsAndHashCode
import groovy.transform.Sortable
import groovy.util.logging.Log4j2

import java.time.LocalDate

@Canonical
@EqualsAndHashCode(includes = [ 'localDate', 'titleCode' ])
@Sortable(includes = [ 'localDate', 'titleCode' ])
@Log4j2
class TitleCodeByDateSummary {
    LocalDate localDate
    String titleCode
    Set<nz.govt.natlib.tools.sip.generation.newspapers.NewspaperFile> files = [ ]
    List<nz.govt.natlib.tools.sip.generation.newspapers.NewspaperFile> duplicateFiles = [ ]

    void addFile(nz.govt.natlib.tools.sip.generation.newspapers.NewspaperFile fairfaxFile) {
        if (files.contains(fairfaxFile)) {
            duplicateFiles.add(fairfaxFile)
        } else {
            files.add(fairfaxFile)
        }
    }

    List<nz.govt.natlib.tools.sip.generation.newspapers.NewspaperFile> outOfSequenceFiles() {
        List<nz.govt.natlib.tools.sip.generation.newspapers.NewspaperFile> outOfSequenceFiles = [ ]
        List<nz.govt.natlib.tools.sip.generation.newspapers.NewspaperFile> sortedFiles = files.sort()
        nz.govt.natlib.tools.sip.generation.newspapers.NewspaperFile lastFairfaxFile
        sortedFiles.each { nz.govt.natlib.tools.sip.generation.newspapers.NewspaperFile fairfaxFile ->
            if (lastFairfaxFile == null) {
                if (fairfaxFile.sequenceNumber != 1) {
                    outOfSequenceFiles.add(fairfaxFile)
                }
            } else {
                if (!fairfaxFile.canComeDirectlyAfter(lastFairfaxFile)) {
                    outOfSequenceFiles.addAll([ lastFairfaxFile, fairfaxFile ])
                }
            }
            lastFairfaxFile = fairfaxFile
        }

        return outOfSequenceFiles

    }

    String forSpreadsheet(String separator = '|') {
        StringBuilder stringBuilder = new StringBuilder()
        stringBuilder.append(files.size())
        stringBuilder.append(separator)
        stringBuilder.append(titleCode)
        stringBuilder.append(separator)

        boolean firstFile = true
        outOfSequenceFiles().each { nz.govt.natlib.tools.sip.generation.newspapers.NewspaperFile fairfaxFile ->
            if (!firstFile) {
                stringBuilder.append(',')
            }
            stringBuilder.append(fairfaxFile.filename)
            firstFile = false
        }
        stringBuilder.append(separator)

        firstFile = true
        duplicateFiles.each { nz.govt.natlib.tools.sip.generation.newspapers.NewspaperFile fairfaxFile ->
            if (!firstFile) {
                stringBuilder.append(',')
            }
            stringBuilder.append(fairfaxFile.filename)
            firstFile = false
        }

        return stringBuilder.toString()
    }

}
