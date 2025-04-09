#!/bin/sh

export sourceFolder="/mnt/y/ndha/pre-deposit_prod/NDHA_submission_Rosetta/nzme-processing/TWA_Files_for_processing/04Oct24/"
export targetBaseFolder="/mnt/y/ndha/pre-deposit_prod/NDHA_submission_Rosetta/nzme-processing/"
export targetPreProcessingFolder="${targetBaseFolder}/pre-processing_TWA"
export forReviewFolder="${targetBaseFolder}/for-review_TWA"
export newspaperType="NZME"

export startingDate="2024-08-01"
export endingDate="2024-11-30"

#export generalProcessingOptions="search_without_directory_stream"

# Note that the number of threads increases processing speed due to ODS poor single-thread performance
export numberOfThreads=4

export minMemory="4G"
export maxMemory="8G"

java -Xms${minMemory} -Xmx${maxMemory} \
    -jar ../../fat/build/libs/sip-generation-newspapers-fat-all-1.2.3-SNAPSHOT.jar \
    --preProcess \
    --newspaperType="${newspaperType}" \
    --startingDate="${startingDate}" \
    --endingDate="${endingDate}" \
    --sourceFolder="${sourceFolder}" \
    --targetPreProcessingFolder="${targetPreProcessingFolder}" \
    --forReviewFolder="${forReviewFolder}" \
    --createDestination \
    --parallelizeProcessing \
    --numberOfThreads=${numberOfThreads}
#    --generalProcessingOptions=${generalProcessingOptions}
