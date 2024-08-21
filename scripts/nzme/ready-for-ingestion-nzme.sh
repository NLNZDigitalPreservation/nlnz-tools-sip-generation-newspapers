#!/bin/sh

export sourceFolder="/media/sf_Y_DRIVE/ndha/pre-deposit_prod/NDHA_submission_Rosetta/nzme-processing/pre-processing_Jul_2023/"
export targetBaseFolder="/media/sf_Y_DRIVE/ndha/pre-deposit_prod/NDHA_submission_Rosetta/nzme-processing/latest-batch-ready-ingestion"
export targetForIngestionFolder="${targetBaseFolder}/for-ingestion"
export forReviewFolder="${targetBaseFolder}/for-review"
export newspaperType="NZME"

export startingDate="2023-07-01"
export endingDate="2023-07-30"

export forIngestionProcessingTypes="parent_grouping,parent_grouping_with_edition,supplement_grouping,supplement_with_date_and_issue,create_sip_for_folder"
export forIngestionProcessingRules=""
#export forIngestionProcessingOptions="use_command_line_pdf_to_thumbnail_generation"

# Note that the number of threads increases processing speed due to ODS poor single-thread performance
export numberOfThreads=4
#export maximumThumbnailPageThreads=2
export minMemory="4G"
export maxMemory="8G"

java -Xms${minMemory} -Xmx${maxMemory} \
    -jar ../../fat/build/libs/sip-generation-newspapers-fat-all-1.2.2-SNAPSHOT.jar \
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
