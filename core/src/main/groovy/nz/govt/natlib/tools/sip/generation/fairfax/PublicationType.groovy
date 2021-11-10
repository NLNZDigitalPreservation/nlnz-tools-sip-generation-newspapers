package nz.govt.natlib.tools.sip.generation.fairfax

import groovy.json.JsonSlurper
import nz.govt.natlib.tools.sip.generation.parameters.Spreadsheet;

class PublicationType {
    String PDF_FILE_WITH_TITLE_SECTION_DATE_SEQUENCE_GROUPING_PATTERN
    String PDF_FILE_WITH_TITLE_SECTION_DATE_SEQUENCE_PATTERN
    String PDF_FILE_WITH_TITLE_SECTION_DATE_PATTERN
    String DATE_TIME_PATTERN
    String PATH_TO_SPREADSHEET
    Map SUPPLEMENTS

    PublicationType(String publicationType) {
        InputStream inputStream = PublicationType.getResourceAsStream("publication-types.json")
        def slurper = new JsonSlurper()
        def publicationTypes = slurper.parseText(inputStream.text)
        PDF_FILE_WITH_TITLE_SECTION_DATE_SEQUENCE_GROUPING_PATTERN = publicationTypes[publicationType]["PDF_FILE_WITH_TITLE_SECTION_DATE_SEQUENCE_GROUPING_PATTERN"]
        PDF_FILE_WITH_TITLE_SECTION_DATE_SEQUENCE_PATTERN = publicationTypes[publicationType]["PDF_FILE_WITH_TITLE_SECTION_DATE_SEQUENCE_PATTERN"]
        PDF_FILE_WITH_TITLE_SECTION_DATE_PATTERN = publicationTypes[publicationType]["PDF_FILE_WITH_TITLE_SECTION_DATE_PATTERN"]
        DATE_TIME_PATTERN = publicationTypes[publicationType]["DATE_TIME_PATTERN"]
        PATH_TO_SPREADSHEET = publicationTypes[publicationType]["PATH_TO_SPREADSHEET"]
        SUPPLEMENTS = publicationTypes[publicationType]["SUPPLEMENTS"] != null ? publicationTypes[publicationType]["SUPPLEMENTS"] as Map : null
    }

    PublicationType(String publicationType, String pathToSpreadsheet) {
        println(pathToSpreadsheet)
        File spreadsheetFile = new File("${pathToSpreadsheet}")
        def slurper = new JsonSlurper()
        def publicationTypes = slurper.parseText(spreadsheetFile.text)
        process(publicationTypes[publicationType])
    }

    void process(Object publicationType) {
        PDF_FILE_WITH_TITLE_SECTION_DATE_SEQUENCE_GROUPING_PATTERN = publicationType["PDF_FILE_WITH_TITLE_SECTION_DATE_SEQUENCE_GROUPING_PATTERN"]
        PDF_FILE_WITH_TITLE_SECTION_DATE_SEQUENCE_PATTERN = publicationType["PDF_FILE_WITH_TITLE_SECTION_DATE_SEQUENCE_PATTERN"]
        PDF_FILE_WITH_TITLE_SECTION_DATE_PATTERN = publicationType["PDF_FILE_WITH_TITLE_SECTION_DATE_PATTERN"]
        DATE_TIME_PATTERN = publicationType["DATE_TIME_PATTERN"]
        PATH_TO_SPREADSHEET = publicationType["PATH_TO_SPREADSHEET"]
        SUPPLEMENTS = publicationType["SUPPLEMENTS"] != null ? publicationType["SUPPLEMENTS"] as Map : null
    }
}
