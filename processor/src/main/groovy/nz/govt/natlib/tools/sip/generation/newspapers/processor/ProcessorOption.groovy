package nz.govt.natlib.tools.sip.generation.newspapers.processor

import groovy.util.logging.Log4j2

@Log4j2
enum ProcessorOption {
    SearchSubdirectories("search_subdirectories"),
    RootFolderOnly("root_folder_only"),
    UseSourceSubdirectoryAsTarget("use_source_subdirectory_as_target"),
    ShowDirectoryOnly("show_directory_only"),
    ShowDirectoryAndOneParent("show_directory_and_one_parent"),
    ShowDirectoryAndTwoParents("show_directory_and_two_parents"),
    ShowDirectoryAndThreeParents("show_directory_and_three_parents"),
    ShowFullPath("show_full_path"),
    SearchWithDirectoryStream("use_directory_stream"),
    SearchWithoutDirectoryStream("search_without_directory_stream")

    private static final Map<String, ProcessorOption> LOOKUP_BY_FIELD_VALUE = [ : ]
    private static final Map<ProcessorOption, List<ProcessorOption>> OVERRIDES_MAP = [ : ]
    private final String fieldValue

    static {
        values().each { ProcessorOption processorOption ->
            LOOKUP_BY_FIELD_VALUE.put(processorOption.fieldValue, processorOption)
        }
        OVERRIDES_MAP.put(SearchSubdirectories, [ RootFolderOnly ])
        OVERRIDES_MAP.put(RootFolderOnly, [ SearchSubdirectories ])
        OVERRIDES_MAP.put(ShowDirectoryOnly, [ ShowDirectoryAndOneParent, ShowDirectoryAndTwoParents,
                                               ShowDirectoryAndThreeParents, ShowFullPath ])
        OVERRIDES_MAP.put(ShowDirectoryAndOneParent, [ ShowDirectoryOnly, ShowDirectoryAndTwoParents,
                                                       ShowDirectoryAndThreeParents, ShowFullPath ])
        OVERRIDES_MAP.put(ShowDirectoryAndTwoParents, [ ShowDirectoryOnly, ShowDirectoryAndOneParent,
                                               ShowDirectoryAndThreeParents, ShowFullPath ])
        OVERRIDES_MAP.put(ShowDirectoryAndThreeParents, [ ShowDirectoryOnly, ShowDirectoryAndOneParent,
                                                          ShowDirectoryAndTwoParents, ShowFullPath ])
        OVERRIDES_MAP.put(ShowFullPath, [ ShowDirectoryOnly, ShowDirectoryAndOneParent, ShowDirectoryAndTwoParents,
                                          ShowDirectoryAndTwoParents ])
        OVERRIDES_MAP.put(SearchWithDirectoryStream, [SearchWithoutDirectoryStream])
        OVERRIDES_MAP.put(SearchWithoutDirectoryStream, [SearchWithDirectoryStream])
    }

    static List<ProcessorOption> extract(String list, String separator = ",", List<ProcessorOption> defaults = [ ],
                                          boolean exceptionIfUnrecognized = false) {
        List<ProcessorOption> processorOptions = [ ]
        if (list == null || list.strip().isEmpty()) {
            return mergeOverrides(defaults, processorOptions)
        }
        List<String> separatedList = list.split(separator)
        separatedList.each { String value ->
            String strippedValue = value.strip()
            ProcessorOption processorOption = forFieldValue(strippedValue)
            if (processorOption == null) {
                if (!strippedValue.isEmpty()) {
                    String message = "Unable to match processor option=${strippedValue} to a ProcessorOption enum value."
                    log.warn(message)
                    if (exceptionIfUnrecognized) {
                        throw new ProcessorException(message)
                    }
                }
            } else {
                processorOptions.add(processorOption)
            }
        }

        return mergeOverrides(defaults, processorOptions)
    }

    static List<ProcessorOption> mergeOverrides(List<ProcessorOption> current, List<ProcessorOption> overrides) {
        List<ProcessorOption> merged = [ ]
        current.each { ProcessorOption option ->
            ProcessorOption override = overrides.find { ProcessorOption possibleOverride ->
                option.isOverride(possibleOverride)
            }
            if (override == null) {
                merged.add(option)
            } else {
                merged.add(override)
            }
        }
        merged = merged.unique()
        overrides.each { ProcessorOption override ->
            if (!merged.contains(override)) {
                merged.add(override)
            }
        }
        return merged
    }

    static ProcessorOption showDirectoryOption(List<ProcessorOption> candidateOptions,
                                               ProcessorOption defaultOption = ProcessorOption.ShowFullPath) {
        ProcessorOption option = candidateOptions.find { ProcessorOption anOption->
            anOption == ProcessorOption.ShowDirectoryOnly ||
                    ProcessorOption.ShowDirectoryOnly.overrides().contains(anOption)
        }
        return option == null ? defaultOption : option
    }

    static forFieldValue(String fieldValue) {
        return LOOKUP_BY_FIELD_VALUE.get(fieldValue.strip())
    }

    ProcessorOption(String fieldValue) {
        this.fieldValue = fieldValue
    }

    String getFieldValue() {
        return this.fieldValue
    }

    boolean isOverride(ProcessorOption otherOption) {
        return overrides().contains(otherOption)
    }

    List<ProcessorOption> overrides() {
        List<ProcessorOption> overrides = OVERRIDES_MAP.get(this)
        return overrides == null ? [ ] : overrides
    }

    String toString() {
        return this.fieldValue
    }
}
