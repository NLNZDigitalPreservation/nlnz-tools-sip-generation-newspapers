package nz.govt.natlib.tools.sip.generation.newspapers

import groovy.json.JsonSlurper

class PublicationType {
    String PDF_FILE_WITH_TITLE_SECTION_DATE_SEQUENCE_GROUPING_PATTERN
    String PDF_FILE_WITH_TITLE_SECTION_DATE_SEQUENCE_PATTERN
    String PDF_FILE_WITH_TITLE_SECTION_DATE_PATTERN
    String DATE_TIME_PATTERN
    String PATH_TO_SPREADSHEET
    Map SUPPLEMENTS

    //Use default spreadsheet
    PublicationType(String publicationType) {
        InputStream inputStream = PublicationType.getResourceAsStream("publication-types.json")
        def publicationTypes = new JsonSlurper().parseText(inputStream.text)
        process(publicationTypes[publicationType])
    }

    //Specify spreadsheet
    PublicationType(String publicationType, String pathToSpreadsheet) {
        File spreadsheetFile = new File("${pathToSpreadsheet}")
        def publicationTypes = new JsonSlurper().parseText(spreadsheetFile.text)
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
