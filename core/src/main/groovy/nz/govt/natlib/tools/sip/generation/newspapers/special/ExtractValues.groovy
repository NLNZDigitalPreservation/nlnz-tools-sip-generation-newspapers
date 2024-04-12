package nz.govt.natlib.tools.sip.generation.newspapers.special

class ExtractValues {
    static List<String> extractSeparatedValues(Map<String, String> spreadsheetRow, String columnKey,
                                               String regex = "\\+|,|-") {
        List<String> extractedValues = splitColumnValue(spreadsheetRow.get(columnKey), regex)

        return extractedValues
    }

    static List<String> splitColumnValue(String columnValue, String regex = "\\+|,|-") {
        List<String> extractedValues = columnValue.split(regex).collect { String value ->
            value.strip()
        }

        return extractedValues.findAll { String value ->
            !value.isBlank()
        }
    }
}
