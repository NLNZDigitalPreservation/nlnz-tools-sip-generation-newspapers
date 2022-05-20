#!/bin/sh

#################################################################################
## RUNNING THIS SCRIPT WILL PERMANENTLY DELETE ALL MATCHING FILES              ##
## PLEASE CAREFULLY REVIEW THE DATE RANGE AND SOURCE FOLDER BEFORE RUNNING IT  ##
#################################################################################

export sourceFolder="$HOME/workspace/testdata/allied-press/"

export startingDate="2022-04-01"
export endingDate="2022-04-05"
export newspaperType="alliedPress"

# Note that the number of threads increases processing speed due to ODS poor single-thread performance
export numberOfThreads=4

export minMemory="4G"
export maxMemory="8G"

java -Xms${minMemory} -Xmx${maxMemory} \
    -jar ../fat/build/libs/sip-generation-newspapers-fat-all-1.0.1-SNAPSHOT.jar \
    --cleanUpFtp \
    --newspaperType="${newspaperType}" \
    --startingDate="${startingDate}" \
    --endingDate="${endingDate}" \
    --sourceFolder="${sourceFolder}" 