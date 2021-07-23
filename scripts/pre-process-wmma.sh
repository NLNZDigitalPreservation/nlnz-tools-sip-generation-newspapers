#!/bin/sh

export sourceFolder="Y:/ndha/legaldep-ftp/wairarapata"
export targetBaseFolder="Y:/ndha/pre-deposit_prod/NDHA_submission_Rosetta/wairarapa_times_age/wairarapa-times-processing"
export targetPreProcessingFolder="${targetBaseFolder}/pre-processing_Jun_2021"
export forReviewFolder="${targetBaseFolder}/for-review_Jun_2021"

export startingDate="2021-06-01"
export endingDate="2021-06-30"

# Note that the number of threads increases processing speed due to ODS poor single-thread performance
export numberOfThreads=4

export minMemory="4G"
export maxMemory="8G"

java -Xms${minMemory} -Xmx${maxMemory} \
    -jar sip-generation-fairfax-fat-all-1.0.0-WMMA-SNAPSHOT.jar \
    --preProcess \
    --startingDate="${startingDate}" \
    --endingDate="${endingDate}" \
    --sourceFolder="${sourceFolder}" \
    --targetPreProcessingFolder="${targetPreProcessingFolder}" \
    --forReviewFolder="${forReviewFolder}" \
    --createDestination \
    --moveFiles \
    --parallelizeProcessing \
    --numberOfThreads ${numberOfThreads}
