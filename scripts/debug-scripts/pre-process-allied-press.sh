#!/bin/sh

export sourceFolder="$HOME/workspace/testdata/allied-press/"
export targetBaseFolder="$HOME/workspace/testdata/NDHA_submission_Rosetta/allpress/allpress-processing/"
export targetPreProcessingFolder="${targetBaseFolder}/pre-processing_Dec_2022"
export forReviewFolder="${targetBaseFolder}/for-review_Dec_2022"
export newspaperType="alliedPress"

export startingDate="2022-12-01"
export endingDate="2022-12-31"

# Note that the number of threads increases processing speed due to ODS poor single-thread performance
export numberOfThreads=4

export minMemory="4G"
export maxMemory="8G"

java -Xms${minMemory} -Xmx${maxMemory} \
    -jar ../../fat/build/libs/sip-generation-newspapers-fat-all-1.1.3-SNAPSHOT.jar \
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
