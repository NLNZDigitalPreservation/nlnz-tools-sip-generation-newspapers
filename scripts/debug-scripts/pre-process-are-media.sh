#!/bin/sh

export sourceFolder="$HOME/workspace/testdata/LS/"
export targetBaseFolder="$HOME/workspace/testdata/NDHA_submission_Rosetta/are-media-processing/"
export targetPreProcessingFolder="${targetBaseFolder}/pre-processing_April_2022"
export forReviewFolder="${targetBaseFolder}/for-review_April_2022"
export newspaperType="areMedia"

export startingDate="2022-06-01"
export endingDate="2022-06-30"

#export generalProcessingOptions="search_without_directory_stream"

# Note that the number of threads increases processing speed due to ODS poor single-thread performance
export numberOfThreads=4

export minMemory="4G"
export maxMemory="8G"

java -Xms${minMemory} -Xmx${maxMemory} \
    -jar ../../fat/build/libs/sip-generation-newspapers-fat-all-1.0.1-SNAPSHOT.jar \
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
