#!/bin/sh

# Parameters needed to correctly process allied press:
# - newspaperType="alliedPress"
# - forIngestionProcessingRules="is_single_pdf_file,use_filename_for_mets_label,zero_length_pdf_skipped"

export sourceFolder="$HOME/workspace/testdata/NDHA_submission_Rosetta/allpress/allpress-processing/pre-processing_Mar_2023"
export targetBaseFolder="$HOME/workspace/testdata/NDHA_submission_Rosetta/allpress/allpress-processing/latest-batch-ready-ingestion"
export targetForIngestionFolder="${targetBaseFolder}/for-ingestion"
export forReviewFolder="${targetBaseFolder}/for-review"
export newspaperType="alliedPress"

export startingDate="2023-03-01"
export endingDate="2023-03-31"

export forIngestionProcessingTypes="parent_grouping,create_sip_for_folder"
export forIngestionProcessingRules="is_single_pdf_file,use_filename_for_mets_label,zero_length_pdf_skipped"

# Note that the number of threads increases processing speed due to ODS poor single-thread performance
export numberOfThreads=4
export minMemory="4G"
export maxMemory="8G"

java -Xms${minMemory} -Xmx${maxMemory} \
    -jar ../../fat/build/libs/sip-generation-newspapers-fat-all-1.1.4-SNAPSHOT.jar \
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
