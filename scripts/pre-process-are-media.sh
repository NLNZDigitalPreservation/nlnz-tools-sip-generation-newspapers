#!/bin/sh

export sourceFolder="$HOME/workspace/testdata/are-media3/"
export targetBaseFolder="$HOME/workspace/testdata/NDHA_submission_Rosetta/are-media-processing/"
export targetPreProcessingFolder="${targetBaseFolder}/pre-processing_Apr_2022"
export forReviewFolder="${targetBaseFolder}/for-review_Apr_2021"
export newspaperType="areMedia"

export startingDate="2022-04-25"
export endingDate="2022-04-25"

# Note that the number of threads increases processing speed due to ODS poor single-thread performance
export numberOfThreads=4

export minMemory="4G"
export maxMemory="8G"

java -Xms${minMemory} -Xmx${maxMemory} \
    -jar ../fat/build/libs/sip-generation-newspapers-fat-all-1.0.0-SNAPSHOT.jar \
    --preProcess \
    --newspaperType="${newspaperType}" \
    --startingDate="${startingDate}" \
    --endingDate="${endingDate}" \
    --sourceFolder="${sourceFolder}" \
    --targetPreProcessingFolder="${targetPreProcessingFolder}" \
    --forReviewFolder="${forReviewFolder}" \
    --createDestination \
    --parallelizeProcessing \
    --numberOfThreads ${numberOfThreads}
