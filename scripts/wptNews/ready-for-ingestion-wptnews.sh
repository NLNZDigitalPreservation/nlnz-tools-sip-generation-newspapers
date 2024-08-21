#!/bin/sh

export sourceFolder="$HOME/workspace/testdata/NDHA_submission_Rosetta/westportnews/pre-processing_Oct_2021"
export targetBaseFolder="$HOME/workspace/testdata/NDHA_submission_Rosetta/westportnews/latest-batch-ready-ingestion"
export targetForIngestionFolder="${targetBaseFolder}/for-ingestion"
export forReviewFolder="${targetBaseFolder}/for-review"
export newspaperType="wptNews"

export startingDate="2021-10-01"
export endingDate="2021-10-30"

export forIngestionProcessingTypes="parent_grouping,parent_grouping_with_edition,supplement_grouping,create_sip_for_folder"

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
    --forIngestionProcessingRules="${forIngestionProcessingRules}" \
    --forIngestionProcessingOptions="${forIngestionProcessingOptions}"
