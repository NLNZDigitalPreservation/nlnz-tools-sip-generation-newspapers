package nz.govt.natlib.tools.sip.generation.newspapers

import groovy.json.JsonSlurper

class NewspaperType {
    String PDF_FILE_WITH_TITLE_SECTION_DATE_SEQUENCE_GROUPING_PATTERN
    String PDF_FILE_WITH_TITLE_SECTION_DATE_SEQUENCE_PATTERN
    String PDF_FILE_WITH_TITLE_SECTION_DATE_PATTERN
    String DATE_TIME_PATTERN
    String PATH_TO_SPREADSHEET
    ArrayList IGNORE
    String REVISIONS
    boolean CASE_SENSITIVE
    boolean APPEND_YEAR
    Map DATE_ADJUSTMENTS
    String END_SEQUENCE

    //Use default spreadsheet
    NewspaperType(String newspaperType) {
        InputStream inputStream = NewspaperType.getResourceAsStream("newspaper-types.json")
        def newspaperTypes = new JsonSlurper().parseText(inputStream.text)
        index(newspaperTypes[newspaperType])
    }

    //Specify spreadsheet
    NewspaperType(String newspaperType, String pathToSpreadsheet) {
        File spreadsheetFile = new File("${pathToSpreadsheet}")
        def newspaperTypes = new JsonSlurper().parseText(spreadsheetFile.text)
        index(newspaperTypes[newspaperType])
    }

    void index(Object newspaperType) {
        PDF_FILE_WITH_TITLE_SECTION_DATE_SEQUENCE_GROUPING_PATTERN = newspaperType["PDF_FILE_WITH_TITLE_SECTION_DATE_SEQUENCE_GROUPING_PATTERN"]
        PDF_FILE_WITH_TITLE_SECTION_DATE_SEQUENCE_PATTERN = newspaperType["PDF_FILE_WITH_TITLE_SECTION_DATE_SEQUENCE_PATTERN"]
        PDF_FILE_WITH_TITLE_SECTION_DATE_PATTERN = newspaperType["PDF_FILE_WITH_TITLE_SECTION_DATE_PATTERN"]
        DATE_TIME_PATTERN = newspaperType["DATE_TIME_PATTERN"]
        PATH_TO_SPREADSHEET = newspaperType["PATH_TO_SPREADSHEET"]
        IGNORE = newspaperType["IGNORE"] != null ? newspaperType["IGNORE"] as ArrayList : null
        REVISIONS = newspaperType["REVISIONS"]
        CASE_SENSITIVE = newspaperType["CASE_SENSITIVE"]
        APPEND_YEAR = newspaperType["APPEND_YEAR"]
        DATE_ADJUSTMENTS = newspaperType["DATE_ADJUSTMENTS"] != null ? newspaperType["DATE_ADJUSTMENTS"] as Map : null
        END_SEQUENCE = newspaperType["END_SEQUENCE"] != null ? newspaperType["END_SEQUENCE"] : null
    }
}
