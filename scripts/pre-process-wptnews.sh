#!/bin/sh

export sourceFolder="$HOME/workspace/testdata/westportnews/"
export targetBaseFolder="$HOME/workspace/testdata/NDHA_submission_Rosetta/westportnews/"
export targetPreProcessingFolder="${targetBaseFolder}/pre-processing_Oct_2021"
export forReviewFolder="${targetBaseFolder}/for-review_Oct_2021"
export publicationType="wptNews"

export startingDate="2021-10-01"
export endingDate="2021-10-30"

# Note that the number of threads increases processing speed due to ODS poor single-thread performance
export numberOfThreads=4

export minMemory="4G"
export maxMemory="8G"

java -Xms${minMemory} -Xmx${maxMemory} \
    -jar ../fat/build/libs/sip-generation-newspapers-fat-all-1.0.0-WMMA-SNAPSHOT.jar \
    --preProcess \
    --publicationType="${publicationType}" \
    --startingDate="${startingDate}" \
    --endingDate="${endingDate}" \
    --sourceFolder="${sourceFolder}" \
    --targetPreProcessingFolder="${targetPreProcessingFolder}" \
    --forReviewFolder="${forReviewFolder}" \
    --createDestination \
    --parallelizeProcessing \
    --numberOfThreads ${numberOfThreads}
