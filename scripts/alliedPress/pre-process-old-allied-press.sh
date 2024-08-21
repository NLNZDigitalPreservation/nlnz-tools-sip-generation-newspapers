#!/bin/sh

export sourceFolder="/media/sf_Y_DRIVE/ndha/legaldep-ftp/allpress/"
export targetBaseFolder="/media/sf_Y_DRIVE/ndha/pre-deposit_prod/NDHA_submission_Rosetta/allpress-processing/"
export targetPreProcessingFolder="${targetBaseFolder}/pre-processing_Aug_2022"
export forReviewFolder="${targetBaseFolder}/for-review_Aug_2022"
export newspaperType="oldAlliedPress"

export startingDate="2022-08-01"
export endingDate="2022-08-31"

# Note that the number of threads increases processing speed due to ODS poor single-thread performance
export numberOfThreads=4

export minMemory="4G"
export maxMemory="8G"

java -Xms${minMemory} -Xmx${maxMemory} \
    -jar ../fat/build/libs/sip-generation-newspapers-fat-all-1.2.2-SNAPSHOT.jar \
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
