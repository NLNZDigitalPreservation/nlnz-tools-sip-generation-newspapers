#!/bin/sh

export sourceFolder="$HOME/workspace/testdata/allpress/"
export targetBaseFolder="$HOME/workspace/testdata/NDHA_submission_Rosetta/allpress/allpress-processing/"
export targetPreProcessingFolder="${targetBaseFolder}/pre-processing_Sep_2023"
export forReviewFolder="${targetBaseFolder}/for-review_Sep_2023"
export newspaperType="alliedPress"

export startingDate="2023-09-01"
export endingDate="2023-09-30"

# Note that the number of threads increases processing speed due to ODS poor single-thread performance
export numberOfThreads=4

export minMemory="4G"
export maxMemory="8G"

java -Xms${minMemory} -Xmx${maxMemory} \
    -jar ../../fat/build/libs/sip-generation-newspapers-fat-all-1.1.5-SNAPSHOT.jar \
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
