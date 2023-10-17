FROM bellsoft/liberica-openjdk-alpine:17.0.8.1
COPY fat/build/libs/sip-generation-newspapers-fat-all-1.1.5-SNAPSHOT.jar sip-generation-newspapers.jar
ENTRYPOINT java -jar sip-generation-newspapers.jar \
        ${PROCESSING_TYPE} \
        ${NEWSPAPER_TYPE} \
        ${STARTING_DATE} \
        ${ENDING_DATE} \
        ${SOURCE_FOLDER} \
        ${TARGET_PRE_PROCESSING_FOLDER} \
        ${FOR_REVIEW_FOLDER} \
        ${TARGET_FOR_INGESTION_FOLDER} \
        ${CREATE_DESTINATION_FOLDER} \
        ${PARALLELIZE_PROCESSING} \
        ${NUMBER_OF_THREADS} \
        ${FOR_INGESTION_PROCESSING_TYPES} \
        ${FOR_INGESTION_PROCESSING_RULES}