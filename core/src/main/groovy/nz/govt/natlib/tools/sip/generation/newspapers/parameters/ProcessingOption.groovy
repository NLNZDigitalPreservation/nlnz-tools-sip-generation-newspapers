package nz.govt.natlib.tools.sip.generation.newspapers.parameters

import groovy.util.logging.Log4j2
import nz.govt.natlib.tools.sip.state.SipProcessingException

@Log4j2
enum ProcessingOption {
    AlphaBeforeNumericSequencing("alpha_before_numeric"),
    NumericBeforeAlphaSequencing("numeric_before_alpha"),

    FullDateInSip("full_date_in_sip"),
    IssueOnlyInSip("issue_only_in_sip"),

    DcIssuedField("dc_issued_field"),
    NoDcIssuedField("no_dc_issued_field"),

    private static final Map<String, ProcessingOption> LOOKUP_BY_FIELD_VALUE = [ : ]
    private static final Map<ProcessingOption, List<ProcessingOption>> OVERRIDES_MAP = [ : ]
    private final String fieldValue

    static {
        values().each { ProcessingOption processingOption ->
            LOOKUP_BY_FIELD_VALUE.put(processingOption.fieldValue, processingOption)
        }
        OVERRIDES_MAP.put(AlphaBeforeNumericSequencing, [ NumericBeforeAlphaSequencing ])
        OVERRIDES_MAP.put(NumericBeforeAlphaSequencing, [ AlphaBeforeNumericSequencing ])

        OVERRIDES_MAP.put(FullDateInSip, [IssueOnlyInSip ])
        OVERRIDES_MAP.put(IssueOnlyInSip, [FullDateInSip ])

        OVERRIDES_MAP.put(DcIssuedField, [NoDcIssuedField ])
        OVERRIDES_MAP.put(NoDcIssuedField, [DcIssuedField ])
    }

    static List<ProcessingOption> extract(String list, String separator = ",", List<ProcessingOption> defaults = [ ],
                                          boolean exceptionIfUnrecognized = false) {
        List<ProcessingOption> processingOptions = [ ]
        if (list == null || list.strip().isEmpty()) {
            return mergeOverrides(defaults, processingOptions)
        }
        List<String> separatedList = list.split(separator)
        separatedList.each { String value ->
            String strippedValue = value.strip()
            ProcessingOption processingOption = forFieldValue(strippedValue)
            if (processingOption == null) {
                if (!strippedValue.isEmpty()) {
                    String message = "Unable to match processing option=${strippedValue} to a ProcessingOption enum value."
                    log.warn(message)
                    if (exceptionIfUnrecognized) {
                        throw new SipProcessingException(message)
                    }
                }
            } else {
                processingOptions.add(processingOption)
            }
        }

        return mergeOverrides(defaults, processingOptions)
    }

    static List<ProcessingOption> mergeOverrides(List<ProcessingOption> current, List<ProcessingOption> overrides) {
        List<ProcessingOption> merged = [ ]
        current.each { ProcessingOption option ->
            ProcessingOption override = overrides.find { ProcessingOption possibleOverride ->
                option.isOverride(possibleOverride)
            }
            if (override == null) {
                merged.add(option)
            } else {
                merged.add(override)
            }
        }
        merged = merged.unique()
        overrides.each { ProcessingOption override ->
            if (!merged.contains(override)) {
                merged.add(override)
            }
        }
        return merged
    }

    static forFieldValue(String fieldValue) {
        return LOOKUP_BY_FIELD_VALUE.get(fieldValue.strip())
    }

    ProcessingOption(String fieldValue) {
        this.fieldValue = fieldValue
    }

    String getFieldValue() {
        return this.fieldValue
    }

    boolean isOverride(ProcessingOption otherOption) {
        return overrides().contains(otherOption)
    }

    List<ProcessingOption> overrides() {
        List<ProcessingOption> overrides = OVERRIDES_MAP.get(this)
        return overrides == null ? [ ] : overrides
    }

    String toString() {
        return this.fieldValue
    }
}
