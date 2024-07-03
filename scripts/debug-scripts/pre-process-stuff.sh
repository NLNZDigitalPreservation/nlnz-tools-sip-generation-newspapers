#!/bin/sh

export sourceFolder="/home/lissje/workspace/testdata/stuff/"
export targetBaseFolder="/home/lissje/workspace/testdata/stuff-processing/"
export targetPreProcessingFolder="${targetBaseFolder}/pre-processing_Nov_2023"
export forReviewFolder="${targetBaseFolder}/for-review_Nov_2023"
export newspaperType="stuff"

export startingDate="2023-11-01"
export endingDate="2023-11-30"

#export generalProcessingOptions="search_without_directory_stream"

# Note that the number of threads increases processing speed due to ODS poor single-thread performance
export numberOfThreads=4

export minMemory="4G"
export maxMemory="8G"

java -Xms${minMemory} -Xmx${maxMemory} \
    -jar ../../fat/build/libs/sip-generation-newspapers-fat-all-1.2.1-SNAPSHOT.jar \
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
