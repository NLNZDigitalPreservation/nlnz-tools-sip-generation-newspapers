#!/bin/sh

#################################################################################
## RUNNING THIS SCRIPT WILL PERMANENTLY DELETE ALL MATCHING FILES              ##
## PLEASE CAREFULLY REVIEW THE DATE RANGE AND SOURCE FOLDER BEFORE RUNNING IT  ##
#################################################################################

export sourceFolder="$HOME/workspace/testdata/wairarapata/"

export startingDate="2022-05-02"
export endingDate="2022-05-02"
export newspaperType="WMMA"

# Note that the number of threads increases processing speed due to ODS poor single-thread performance
export numberOfThreads=4

export minMemory="4G"
export maxMemory="8G"

java -Xms${minMemory} -Xmx${maxMemory} \
    -jar ../fat/build/libs/sip-generation-newspapers-fat-all-1.2.2-SNAPSHOT.jar \
    --cleanUpFTP \
    --newspaperType="${newspaperType}" \
    --startingDate="${startingDate}" \
    --endingDate="${endingDate}" \
    --sourceFolder="${sourceFolder}" 