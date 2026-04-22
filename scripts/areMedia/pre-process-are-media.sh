#!/bin/sh

export sourceFolder="/media/sf_Y_DRIVE/ndha/legaldep-ftp/aremedia/"
export targetBaseFolder="/media/sf_Y_DRIVE/ndha/pre-deposit_prod/NDHA_submission_Rosetta/are-media-processing"
export targetPreProcessingFolder="${targetBaseFolder}/pre-processing_Apr_2022"
export forReviewFolder="${targetBaseFolder}/for-review_Apr_2022"
export newspaperType="areMedia"

export startingDate="2022-05-01"
export endingDate="2022-05-01"

# export generalProcessingOptions="search_without_directory_stream"

# Note that the number of threads increases processing speed due to ODS poor single-thread performance
export numberOfThreads=4

export minMemory="4G"
export maxMemory="8G"

java -Xms${minMemory} -Xmx${maxMemory} \
    -jar ../fat/build/libs/sip-generation-newspapers-fat-all-1.2.3-SNAPSHOT.jar \
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
   # --generalProcessingOptions=${generalProcessingOptions}
