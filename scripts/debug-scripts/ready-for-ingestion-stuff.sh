#!/bin/sh

export sourceFolder="/media/sf_Y_DRIVE/ndha/pre-deposit_prod/lissje/stuff-processing/tmp_pre"
export targetBaseFolder="/media/sf_Y_DRIVE/ndha/pre-deposit_prod/lissje/stuff-processing/latest-batch-ready-ingestion"
export targetForIngestionFolder="${targetBaseFolder}/for-ingestion"
export forReviewFolder="${targetBaseFolder}/for-review"
export supplementPreviousIssuesFile="/media/sf_Y_DRIVE/ndha/pre-deposit_prod/lissje/stuff-processing/supplements-previous-issues.properties"
export newspaperType="stuff"

export startingDate="2022-09-01"
export endingDate="2022-09-30"

export forIngestionProcessingTypes="parent_grouping,parent_grouping_with_edition,supplement_grouping,supplement_with_date_and_issue,create_sip_for_folder"
#export forIngestionProcessingOptions="use_command_line_pdf_to_thumbnail_generation"

# Note that the number of threads increases processing speed due to ODS poor single-thread performance
export numberOfThreads=4
#export maximumThumbnailPageThreads=2
export minMemory="4G"
export maxMemory="8G"

java -Xms${minMemory} -Xmx${maxMemory} \
    -jar ../../fat/build/libs/sip-generation-newspapers-fat-all-1.1.2-SNAPSHOT.jar \
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
