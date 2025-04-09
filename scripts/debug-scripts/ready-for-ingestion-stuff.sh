#!/bin/sh

export sourceFolder="/home/lissje/workspace/testdata/newspapers/stuff-processing/pre-processing_Nov_2024/"
export targetBaseFolder="/home/lissje/workspace/testdata/newspapers/stuff-processing/latest-batch-ready-ingestion"
export targetForIngestionFolder="${targetBaseFolder}/for-ingestion"
export forReviewFolder="${targetBaseFolder}/for-review"
export supplementPreviousIssuesFile="/home/lissje/workspace/testdata/newspapers/stuff-processing/supplements-previous-issues.properties"
export newspaperType="stuff"

export startingDate="2024-11-01"
export endingDate="2024-11-30"

export forIngestionProcessingTypes="parent_grouping,parent_grouping_with_edition,supplement_grouping,supplement_with_date_and_issue,create_sip_for_folder"
export forIngestionProcessingRules="handle_ignored"
#export forIngestionProcessingOptions="use_command_line_pdf_to_thumbnail_generation"

# Note that the number of threads increases processing speed due to ODS poor single-thread performance
export numberOfThreads=4
#export maximumThumbnailPageThreads=2
export minMemory="4G"
export maxMemory="8G"

java -Xms${minMemory} -Xmx${maxMemory} \
    -jar ../../fat/build/libs/sip-generation-newspapers-fat-all-1.2.3-SNAPSHOT.jar \
    --readyForIngestion \
    --newspaperType="${newspaperType}" \
    --startingDate="${startingDate}" \
    --endingDate="${endingDate}" \
    --sourceFolder="${sourceFolder}" \
    --targetForIngestionFolder="${targetForIngestionFolder}" \
    --forReviewFolder="${forReviewFolder}" \
    --supplementPreviousIssuesFile="${supplementPreviousIssuesFile}" \
    --createDestination \
    --parallelizeProcessing \
    --numberOfThreads=${numberOfThreads} \
    --forIngestionProcessingTypes="${forIngestionProcessingTypes}" \
    --forIngestionProcessingRules="${forIngestionProcessingRules}" \
    --forIngestionProcessingOptions="${forIngestionProcessingOptions}"
