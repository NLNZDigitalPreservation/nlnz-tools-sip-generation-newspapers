#!/bin/sh

# Parameters needed to correctly process allied press:
# - newspaperType="alliedPress"
# - forIngestionProcessingRules="is_single_pdf_file,use_filename_for_mets_label"

export sourceFolder="$HOME/workspace/testdata/NDHA_submission_Rosetta/allpress/allpress-processing/pre-processing_Dec_2022"
export targetBaseFolder="$HOME/workspace/testdata/NDHA_submission_Rosetta/allpress/allpress-processing/latest-batch-ready-ingestion"
export targetForIngestionFolder="${targetBaseFolder}/for-ingestion"
export forReviewFolder="${targetBaseFolder}/for-review"
export newspaperType="alliedPress"

export startingDate="2022-12-01"
export endingDate="2022-12-31"

export forIngestionProcessingTypes="parent_grouping,create_sip_for_folder"
export forIngestionProcessingRules="is_single_pdf_file,use_filename_for_mets_label"

# Note that the number of threads increases processing speed due to ODS poor single-thread performance
export numberOfThreads=4
export minMemory="4G"
export maxMemory="8G"

java -Xms${minMemory} -Xmx${maxMemory} \
    -jar ../../fat/build/libs/sip-generation-newspapers-fat-all-1.1.3-SNAPSHOT.jar \
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
