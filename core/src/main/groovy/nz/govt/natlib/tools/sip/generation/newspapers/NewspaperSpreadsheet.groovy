package nz.govt.natlib.tools.sip.generation.newspapers

import groovy.util.logging.Log4j2
import nz.govt.natlib.tools.sip.generation.newspapers.parameters.ProcessingType
import nz.govt.natlib.tools.sip.generation.newspapers.special.ExtractValues
import nz.govt.natlib.tools.sip.generation.parameters.Spreadsheet
import nz.govt.natlib.tools.sip.state.SipProcessingException
import nz.govt.natlib.tools.sip.state.SipProcessingExceptionReason
import nz.govt.natlib.tools.sip.state.SipProcessingExceptionReasonType

/**
 * Wraps a {@link Spreadsheet} with Newspaper-specific functionality.
 */
@Log4j2
class NewspaperSpreadsheet {
    // Note that the CSV 'standard' generally only allows 1 character as a separator
    static String DEFAULT_FIELD_SEPARATOR = "|"
    static String MMSID_COLUMN_NAME = "MMSID"
    static String PROCESSING_TYPE_KEY = "processing_type"
    static String PROCESSING_RULES_KEY = "processing_rules"
    static String PROCESSING_OPTIONS_KEY = "processing_options"
    static String TITLE_CODE_KEY = "title_code"
    static String SUPPLEMENT_TITLE_CODE_KEY = "supplement_title_codes"
    static String SUPPLEMENT_WITH_SEQUENCE_KEY = "supplement_with_sequence"
    static String SECTION_CODE_KEY = "section_codes"
    static String EDITION_CODE_KEY = "edition_codes"
    static String SEQUENCE_LETTER_KEY = "sequence_letters"
    static String IGNORE_SEQUENCE_KEY = "ignore_sequence"
    static String TITLE_PARENT_KEY = "title_parent"
    static String TITLE_METS_KEY = "title_mets"
    static String EDITION_DISCRIMINATOR_KEY = "edition_discriminators"
    static String IS_MAGAZINE_KEY = "Magazine"

    Spreadsheet spreadsheet
    Map<NewspaperFileTitleEditionKey, List<Map<String, String>>> titleCodeSectionCodeToRowsMap = [: ]
    Map<String, List<Map<String, String>>> titleCodeToRowsMap = [ : ]
    Set<NewspaperFileTitleEditionKey> allTitleCodeSectionCodeKeys = [ ]
    Set<String> allTitleCodeKeys = [ ]
    Map<String, List<String>> allSupplementTitleCodes = [:]

    static final Map<String, String> BLANK_ROW = [
        "MMSID": "UNKNOWN_MMSID",
        "title_parent": "UNKNOWN_TITLE",
        "processing_type": "NO_PROCESSING_TYPE_GIVEN",
        "processing_rules": "",
        "processing_options": "",
        "publication_key": "",
        "title_code": "NO_TITLE_CODE_GIVEN",
        "edition_discriminators": "",
        "section_codes": "",
        "Access": "200",
        "Magazine": "-1"
    ].asImmutable()

    /**
     * Load and return the NewspaperSpreadsheet from default resources.
     */
    static NewspaperSpreadsheet defaultInstance(String pathToSpreadsheet) {
        // TODO Either a root class to get resourceAsStream, move the json file to the same package or do ../../.. etc
        // or do what SipTestHelper does.
//        InputStream defaultSpreadsheetInputStream = NewspaperSpreadsheet.getResourceAsStream("default-WTAA-import-parameters.json")
        InputStream defaultSpreadsheetInputStream = NewspaperSpreadsheet.getResourceAsStream(pathToSpreadsheet)
        Spreadsheet spreadsheet = Spreadsheet.fromJson(Spreadsheet.GENERATE_ID_VALUE, defaultSpreadsheetInputStream.text, true, true)

        return new NewspaperSpreadsheet(spreadsheet)
    }

    static boolean extractBooleanValue(Map<String, String> spreadsheetRow, String columnId) {
        String columnValue = spreadsheetRow.get(columnId)
        if (columnValue == null) {
            // No value is false
            columnValue = "0"
        } else {
            columnValue = columnValue.strip()
        }
        return "1" == columnValue || "y".equalsIgnoreCase(columnValue) || "yes".equalsIgnoreCase(columnValue)
    }

    NewspaperSpreadsheet(Spreadsheet spreadsheet) {
        this.spreadsheet = spreadsheet
        index()
    }

    List<Map<String, String>> matchingParameterMaps(String titleCode, String sectionCode) {
        List<Map<String, String>> matchingMaps = [ ]
        spreadsheet.rows.each { Map<String, String> rowMap ->
            if (titleCode == rowMap.get(TITLE_CODE_KEY) && sectionCode == rowMap.get(SECTION_CODE_KEY)) {
                matchingMaps.add(rowMap)
            }
        }
        return matchingMaps
    }

    List<Map<String, String>> matchingProcessingTypeParameterMaps(String processingType, String titleCode) {
        List<Map<String, String>> matchingMaps = [ ]
        spreadsheet.rows.each { Map<String, String> rowMap ->
            if (processingType == rowMap.get(PROCESSING_TYPE_KEY) &&
                    titleCode == rowMap.get(TITLE_CODE_KEY)) {
                matchingMaps.add(rowMap)
            }
        }
        return matchingMaps
    }

    List<String> getTitleParentsForTitleCodeSectionCode(String titleCode, String sectionCode) {
        List<String> titles = [ ]
        matchingParameterMaps(titleCode, sectionCode).each { Map<String, String> rowMap ->
            titles.add(rowMap.get(TITLE_PARENT_KEY))
        }

        return titles
    }

    String getTitleParentForTitleCodeSectionCode(String titleCode, String sectionCode) {
        List<String> titles = getTitleParentsForTitleCodeSectionCode(titleCode, sectionCode)
        if (titles.size() == 1) {
            return titles.first()
        } else if (titles.size() > 1) {
            log.info("Found multiple titles for titleCode=${titleCode}, sectionCode=${sectionCode}, titles=${titles}. Using first title.")
            return titles.first()
        } else {
            return "NO-TITLE-GIVEN"
        }
    }

    void index() {
        spreadsheet.rows.each { Map<String, String> rowMap ->
            String titleCode = rowMap.get(TITLE_CODE_KEY)
            String sectionCode = rowMap.get(SECTION_CODE_KEY)
            String supplementCodes = rowMap.get(SUPPLEMENT_TITLE_CODE_KEY)
            NewspaperFileTitleEditionKey newspaperFileTitleEditionKey = new NewspaperFileTitleEditionKey(
                    titleCode: titleCode, sectionCode: sectionCode)
            if (titleCodeSectionCodeToRowsMap.containsKey(newspaperFileTitleEditionKey)) {
                List<Map<String, String>> rowsForNameEdition = titleCodeSectionCodeToRowsMap.get(newspaperFileTitleEditionKey)
                rowsForNameEdition.add(rowMap)
            } else {
                titleCodeSectionCodeToRowsMap.put(newspaperFileTitleEditionKey, [ rowMap ])
            }
            allTitleCodeSectionCodeKeys.add(newspaperFileTitleEditionKey)

            if (titleCodeToRowsMap.containsKey(titleCode)) {
                List<Map<String, String>> rowsForName = titleCodeToRowsMap.get(titleCode)
                rowsForName.add(rowMap)
            } else {
                titleCodeToRowsMap.put(titleCode, [rowMap ])
            }

            if (!supplementCodes.isEmpty() && supplementCodes != "") {
                List supplements = ExtractValues.splitColumnValue(supplementCodes)
                supplements.each { supplement ->
                    if (allSupplementTitleCodes.containsKey(supplement)) {
                        allSupplementTitleCodes[supplement] << titleCode
                    } else {
                        allSupplementTitleCodes[supplement] = [titleCode]
                    }
                }
            }
            allTitleCodeKeys.add(titleCode.toUpperCase())
        }
    }

    List<SipProcessingException> validate() {
        List<SipProcessingException> validationErrors = [ ]
        spreadsheet.rows.each { Map<String, String> rowMap ->
            String processingTypeString = rowMap.get(PROCESSING_TYPE_KEY)
            ProcessingType processingType = ProcessingType.forFieldValue(processingTypeString)
            if (processingType == null && processingTypeString.strip().isEmpty()) {
                String message = "No acceptable value for ProcessingType=${processingTypeString}, row=${rowMap}".toString()
                SipProcessingException exception = new SipProcessingExceptionReason(
                        SipProcessingExceptionReasonType.INVALID_PARAMETERS, null, message)
                validationErrors.add(exception)
            }
        }
        return validationErrors
    }

}
