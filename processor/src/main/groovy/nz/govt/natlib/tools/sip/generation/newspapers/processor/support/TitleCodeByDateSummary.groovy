package nz.govt.natlib.tools.sip.generation.newspapers.processor.support

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

    void addFile(nz.govt.natlib.tools.sip.generation.newspapers.NewspaperFile newspaperFile) {
        if (files.contains(newspaperFile)) {
            duplicateFiles.add(newspaperFile)
        } else {
            files.add(newspaperFile)
        }
    }

    List<nz.govt.natlib.tools.sip.generation.newspapers.NewspaperFile> outOfSequenceFiles() {
        List<nz.govt.natlib.tools.sip.generation.newspapers.NewspaperFile> outOfSequenceFiles = [ ]
        List<nz.govt.natlib.tools.sip.generation.newspapers.NewspaperFile> sortedFiles = files.sort()
        nz.govt.natlib.tools.sip.generation.newspapers.NewspaperFile lastNewspaperFile
        sortedFiles.each { nz.govt.natlib.tools.sip.generation.newspapers.NewspaperFile newspaperFile ->
            if (lastNewspaperFile == null) {
                if (newspaperFile.sequenceNumber != 1) {
                    outOfSequenceFiles.add(newspaperFile)
                }
            } else {
                if (!newspaperFile.canComeDirectlyAfter(lastNewspaperFile)) {
                    outOfSequenceFiles.addAll([ lastNewspaperFile, newspaperFile ])
                }
            }
            lastNewspaperFile = newspaperFile
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
        outOfSequenceFiles().each { nz.govt.natlib.tools.sip.generation.newspapers.NewspaperFile newspaperFile ->
            if (!firstFile) {
                stringBuilder.append(',')
            }
            stringBuilder.append(newspaperFile.filename)
            firstFile = false
        }
        stringBuilder.append(separator)

        firstFile = true
        duplicateFiles.each { nz.govt.natlib.tools.sip.generation.newspapers.NewspaperFile newspaperFile ->
            if (!firstFile) {
                stringBuilder.append(',')
            }
            stringBuilder.append(newspaperFile.filename)
            firstFile = false
        }

        return stringBuilder.toString()
    }

}
