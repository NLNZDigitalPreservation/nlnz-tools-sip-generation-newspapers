#!/bin/sh

# Parameters needed to correctly process Are Media:
# - newspaperType="areMedia"
# - forIngestionProcessingRules="handle_ignored,optional_all_sections_in_sip"

export sourceFolder="$HOME/workspace/testdata/NDHA_submission_Rosetta/are-media-processing/pre-processing_Apr_2022"
export targetBaseFolder="$HOME/workspace/testdata/NDHA_submission_Rosetta/are-media-processing/latest-batch-ready-ingestion"
export targetForIngestionFolder="${targetBaseFolder}/for-ingestion"
export forReviewFolder="${targetBaseFolder}/for-review"
export newspaperType="areMedia"

export startingDate="2022-04-01"
export endingDate="2022-04-30"

export forIngestionProcessingTypes="parent_grouping,create_sip_for_folder"
export forIngestionProcessingRules="handle_ignored,optional_all_sections_in_sip,handle_invalid"

# Note that the number of threads increases processing speed due to ODS poor single-thread performance
export numberOfThreads=4
export minMemory="4G"
export maxMemory="8G"

java -Xms${minMemory} -Xmx${maxMemory} \
    -jar ../fat/build/libs/sip-generation-newspapers-fat-all-1.2.2-SNAPSHOT.jar \
    --readyForIngestion \
    --newspaperType="${newspaperType}" \
    --startingDate="${startingDate}" \
    --endingDate="${endingDate}" \
    --sourceFolder="${sourceFolder}" \
    --targetForIngestionFolder="${targetForIngestionFolder}" \
    --forReviewFolder="${forReviewFolder}" \
    --createDestination \
    --parallelizeProcessing \
    --numberOfThreads=${numberOfThreads} \
    --forIngestionProcessingTypes="${forIngestionProcessingTypes}" \
    --forIngestionProcessingRules="${forIngestionProcessingRules}"
