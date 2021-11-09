package nz.govt.natlib.tools.sip.generation.fairfax

import groovy.json.JsonSlurper;

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

    String getPDF_FILE_WITH_TITLE_SECTION_DATE_SEQUENCE_GROUPING_PATTERN() {
        return PDF_FILE_WITH_TITLE_SECTION_DATE_SEQUENCE_GROUPING_PATTERN
    }

    String getPDF_FILE_WITH_TITLE_SECTION_DATE_SEQUENCE_PATTERN() {
        return PDF_FILE_WITH_TITLE_SECTION_DATE_SEQUENCE_PATTERN
    }

    String getPDF_FILE_WITH_TITLE_SECTION_DATE_PATTERN() {
        return PDF_FILE_WITH_TITLE_SECTION_DATE_PATTERN
    }

    String getDATE_TIME_PATTERN() {
        return DATE_TIME_PATTERN
    }

    String getPATH_TO_SPREADSHEET() {
        return PATH_TO_SPREADSHEET
    }

    Map getSUPPLEMENTS() {
        return SUPPLEMENTS
    }
}
