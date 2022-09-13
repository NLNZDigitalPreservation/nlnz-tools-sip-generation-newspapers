===================
Script Runner Guide
===================

Additional TODO
===============

-   Placeholder for additional TODO items.


Introduction
============

About NLNZ Tools SIP Generation Newspapers
---------------------------------------

NLNZ Tools SIP Generation Newspapers is specific set of tools for processing digital newspaper file content.
The ultimate output of these tools are SIPs for ingestion into the Rosetta archiving system.

Most of the operations are run on the command line using a set of parameters and a spreadsheet of values that when
combined together with the operational code produce an output that is ready for ingestion into Rosetta.

The purpose of these tools is to process the digital newspaper files. The long-term goal would be to wrap these tools
into a user interface. See the **Future milestones** section of the :doc:`developer-guide` for more details.

About this document
-------------------

This document is the NLNZ Tools SIP Generation Newspapers Script Runner Guide. It describes how to use the command-line
tools provided by the project to perform various workflow operations.

The manual is divided into chapters, each of which deals with a particular scripting operation.

See also:

-   :doc:`Workflow Guide<workflow-guide>`
-   :doc:`Developer Guide<developer-guide>`
-   :doc:`Librarian Guide<librarian-guide>`
-   :doc:`FAQ<faq>`


Contents of this document
-------------------------

Following this introduction, this User Guide includes the following sections:

-   **ProcessorRunner general usage** - Covers general processing parameters.

-   **Newspaper type configuration** - Covers the configuration of different newspaper types.

-   **FTP stage**  - Covers the FTP stage.

-   **Pre-processing stage**  - Covers the pre-processing stage.

-   **Ready-for-ingestion stage** - Covers ready-for-ingestion stage.

-   **Copying ingested loads to ingested folder** - Covers copying ingested loads to their final ingested folder.

-   **Additional tools** - Covers additional scripting tools.

-   **Converting the spreadsheet to JSON and vice-versa** - Covers converting the parameters spreadsheet between formats.

-   **Copying and moves** - Covers how copying files and moving files ensure data integrity.


Relationships with other scripting code
=======================================

Some of this scripting code is related to the codebase *nlnz-tools-scripts-ingestion* found in the github
repository https://github.com/NLNZDigitalPreservation/nlnz-tools-scripts-ingestion . See the documentation for that
codebase at https://nlnz-tools-sip-generation.readthedocs.io . There is an expectation that the two codebases will work
together.

There is also some additional scripts in the github repository:
https://github.com/NLNZDigitalPreservation/nlnz-tools-scripts-ingestion . See the documentation for those scripts
found at https://nlnz-tools-scripts-ingestion.readthedocs.io .


ProcessorRunner general usage
=============================

ProcessorRunner runs different processors based on command-line options.

Processing for different processing stages
------------------------------------------
Processing stages are discussed in more detail in :doc:`workflow-guide`.

+-------------------------------------+--------------------------------------------------------------------------------+
| Processing stage                    | Description                                                                    |
+=====================================+================================================================================+
| --preProcess                        | Group source files by date and titleCode. Output is used by readyForIngestion. |
|                                     | Requires sourceFolder, targetPreProcessingFolder, forReviewFolder.             |
|                                     | Uses startingDate, endingDate.                                                 |
|                                     | Optional createDestination, moveFiles, parallelizeProcessing, numberOfThreads. |
|                                     | This is a processing operation and must run exclusively of other processing    |
|                                     | operations.                                                                    |
+-------------------------------------+--------------------------------------------------------------------------------+
| --readyForIngestion                 | Process the source files. Output is ready for ingestion by Rosetta.            |
|                                     | Requires sourceFolder, targetForIngestionFolder, forReviewFolder,              |
|                                     | processingType.                                                                |
|                                     | Uses startingDate, endingDate.                                                 |
|                                     | Optional createDestination. Note that moveFiles is not supported at this time. |
|                                     | Optional parallelizeProcessing, numberOfThreads.                               |
|                                     | This is a processing operation and must run exclusively of other processing    |
|                                     | operations.                                                                    |
+-------------------------------------+--------------------------------------------------------------------------------+
| --copyIngestedLoadsToIngestedFolder | Copy the ingested loads to ingested folder.                                    |
|                                     | Requires sourceFolder, targetPostProcessedFolder, forReviewFolder.             |
|                                     | Uses startingDate, endingDate.                                                 |
|                                     | Optional createDestination, moveFiles, moveOrCopyEvenIfNoRosettaDoneFile.      |
|                                     | Optional parallelizeProcessing, numberOfThreads.                               |
|                                     | This is a processing operation and must run exclusively of other processing    |
|                                     | operations.                                                                    |
+-------------------------------------+--------------------------------------------------------------------------------+

Other types of processing
-------------------------
+---------------------------------+------------------------------------------------------------------------------------+
| Other processing                | Description                                                                        |
+=================================+====================================================================================+
| --copyProdLoadToTestStructures  | Copy the production load to test structures.                                       |
|                                 | Uses startingDate, endingDate.                                                     |
|                                 | This is a processing operation and must run exclusively of other processing        |
|                                 | operations.                                                                        |
+---------------------------------+------------------------------------------------------------------------------------+

Reports
-------
+--------------------+-------------------------------------------------------------------------------------------------+
| Reports            | Description                                                                                     |
+====================+=================================================================================================+
| -l, --listFiles    | List the source files in an organized way.                                                      |
|                    | Requires sourceFolder.                                                                          |
|                    | This is a reporting operation and cannot be run with any other processing operations.           |
+--------------------+-------------------------------------------------------------------------------------------------+
| --extractMetadata  | Extract and list the metadata from the source files.                                            |
|                    | Requires sourceFolder.                                                                          |
|                    | This is a reporting operation and cannot be run with any other processing operations.           |
+--------------------+-------------------------------------------------------------------------------------------------+
| --statisticalAudit | Statistical audit.                                                                              |
|                    | Search through the source folder and provide a statistical audit of the files found.            |
|                    | This is a reporting operation and cannot be run with any processing operations.                 |
+--------------------+-------------------------------------------------------------------------------------------------+

General parameters
------------------
+--------------------------------------------------------------+--------------------------------------------------------+
| Parameters - General                                         | Description                                            |
+==============================================================+========================================================+
| --newspaperType=NEWSPAPER_TYPE                               | The newspaper type to be processed. Current options    |
|                                                              | are WMMA (Wairarapa Time Age), alliedPress and wptNews |
|                                                              | (Westport News)                                        |
+--------------------------------------------------------------+--------------------------------------------------------+
| -b, --startingDate=STARTING_DATE                             | Starting date in the format yyyy-MM-dd (inclusive).    |
|                                                              | Dates are usually based on file name (not timestamp).  |
|                                                              | Default is 2015-01-01.                                 |
+--------------------------------------------------------------+--------------------------------------------------------+
| -e, --endingDate=ENDING_DATE                                 | Ending date in the format yyyy-MM-dd (inclusive).      |
|                                                              | Default is today. Files after this date are ignored.   |
+--------------------------------------------------------------+--------------------------------------------------------+
| -s, --sourceFolder=SOURCE_FOLDER                             | Source folder in the format /path/to/folder            |
|                                                              | This folder must exist and must be a directory.        |
+--------------------------------------------------------------+--------------------------------------------------------+
| --targetFolder=TARGET_FOLDER                                 | Target folder in the format /path/to/folder.           |
|                                                              | This is the destination folder used when no other      |
|                                                              | destination folders are specified.                     |
|                                                              | Use --createDestination to force its creation.         |
+--------------------------------------------------------------+--------------------------------------------------------+
| --targetPreProcessingFolder=TARGET_PRE_PROCESS_FOLDER        | Target pre-processing folder in the format             |
|                                                              | /path/to/folder                                        |
|                                                              | Use --createDestination to force its creation.         |
+--------------------------------------------------------------+--------------------------------------------------------+
| --targetPostProcessedFolder=TARGET_POST_PROCESSED_FOLDER     | Target post-processed folder in the format             |
|                                                              | /path/to/folder                                        |
|                                                              | Use --createDestination to force its creation.         |
+--------------------------------------------------------------+--------------------------------------------------------+
| -r, --forReviewFolder=FOR_REVIEW_FOLDER                      | For-review folder in the format /path/to/folder.       |
|                                                              | For processing exceptions, depending on processor.     |
|                                                              | Use --createDestination to force its creation.         |
+--------------------------------------------------------------+--------------------------------------------------------+
| --numberOfThreads=NUMBER_OF_THREADS                          | Number of threads when running operations in parallel. |
|                                                              | The default is 1.                                      |
+--------------------------------------------------------------+--------------------------------------------------------+
| --generalProcessingOptions=GENERAL_PROCESSING_OPTIONS        | General processing options.                            |
|                                                              | A comma-separated list of options. These options will  |
|                                                              | override any contradictory options.                    |
|                                                              | These processing options may or may not be applied     |
|                                                              | depending on the processing that takes place.          |
|                                                              | See the class ProcessorOption for a list of what those |
|                                                              | options are.                                           |
+--------------------------------------------------------------+--------------------------------------------------------+

Ready-for-ingestion parameters
------------------------------
+--------------------------------------------------------+-------------------------------------------------------------+
| Parameters - Ready-for-ingestion                       | Description                                                 |
+========================================================+=============================================================+
| --newspaperType=NEWSPAPER_TYPE                         | The newspaper type to be processed. Current options         |
|                                                        | are WMMA (Wairarapa Time Age), alliedPress and wptNews      |
|                                                        | (Westport News)                                             |
+--------------------------------------------------------+-------------------------------------------------------------+
| --targetForIngestionFolder=TARGET_FOR_INGESTION_FOLDER | Target for-ingestion folder in the format /path/to/folder   |
|                                                        | Use --createDestination to force its creation.              |
+--------------------------------------------------------+-------------------------------------------------------------+
| --forIngestionProcessingTypes=PROCESSING_TYPES         | Comma-separated list of for-ingestion processing types.     |
|                                                        | A pre-processing titleCode folder should only be processed  |
|                                                        | once for a single processing type. It may be possible for   |
|                                                        | multiple processing types to apply to the same folder,      |
|                                                        | producing different SIPs.                                   |
+--------------------------------------------------------+-------------------------------------------------------------+
| --forIngestionProcessingRules=PROCESSING_RULES         | For-ingestion processing rules.                             |
|                                                        | A comma-separated list of rules. These rules will override  |
|                                                        | any contradictory rules.                                    |
+--------------------------------------------------------+-------------------------------------------------------------+
| --forIngestionProcessingOptions=PROCESSING_OPTIONS     | For-ingestion processing options.                           |
|                                                        | A comma-separated list of options. These options will       |
|                                                        | override any contradictory options.                         |
+--------------------------------------------------------+-------------------------------------------------------------+

Options
-------
+-------------------------------------+--------------------------------------------------------------------------------+
| Options                             | Description                                                                    |
+=====================================+================================================================================+
| -c, --createDestination             | Whether destination (or target) folders will be created.                       |
|                                     | Default is no creation (false).                                                |
+-------------------------------------+--------------------------------------------------------------------------------+
| --moveFiles                         | Whether files will be moved or copied. Default is copy (false).                |
+-------------------------------------+--------------------------------------------------------------------------------+
| --detailedTimings                   | Include detailed timings (for specific operations).                            |
+-------------------------------------+--------------------------------------------------------------------------------+
| --moveOrCopyEvenIfNoRosettaDoneFile | Whether the move or copy takes place even if there is no Rosetta done file.    |
|                                     | The Rosetta done files is a file with a titleCode of 'done'.                   |
|                                     | Default is no move or copy unless there IS a Rosetta done file (false).        |
+-------------------------------------+--------------------------------------------------------------------------------+
| --verbose                           | Include verbose output.                                                        |
+-------------------------------------+--------------------------------------------------------------------------------+
| -h, --help                          | Display a help message.                                                        |
+-------------------------------------+--------------------------------------------------------------------------------+

General processing options
--------------------------
General processing options are those options specified by the parameter
``--generalProcessingOptions=GENERAL_PROCESSING_OPTIONS``. In the codebase they are represented by the ``enum``
``ProcessorOption``.

The options are as follows:

``search_subdirectories``
    When finding files, also include subdirectories. Overridden by ``root_folder_only``.

``root_folder_only``
    When finding files, only use the specified folder (not subdirectories). Overridden by ``search_subdirectories``.

``use_source_subdirectory_as_target``
    Use the source folder as the target folder. This only works for certain kinds of processing.

``show_directory_only``
    Used when converting a directory path to a file or folder name. In this case only the directory name (without any
    parent directories) is used. Overridden by ``show_directory_and_one_parent``, ``show_directory_and_two_parents``,
    ``show_directory_and_three_parents``, ``show_full_path``.

``show_directory_and_one_parent``
    Used when converting a directory path to a file or folder name. In this case only the directory name and one
    parent directory is used. Overridden by ``show_directory_only``, ``show_directory_and_two_parents``,
    ``show_directory_and_three_parents``, ``show_full_path``.

``show_directory_and_two_parents``
    Used when converting a directory path to a file or folder name. In this case only the directory name and two
    parent directories are used. Overridden by ``show_directory_only``, ``show_directory_and_one_parent``,
    ``show_directory_and_three_parents``, ``show_full_path``.

``show_directory_and_three_parents``
    Used when converting a directory path to a file or folder name. In this case only the directory name and three
    parent directories are used. Overridden by ``show_directory_only``, ``show_directory_and_one_parent``,
    ``show_directory_and_two_parents``, ``show_full_path``.

``show_full_path``
    Used when converting a directory path to a file or folder name. In this case the full path is used. Overridden by
    ``show_directory_only``, ``show_directory_and_one_parent``, ``show_directory_and_two_parents``,
    ``show_directory_and_three_parents``.

Newspaper type configuration
----------------------------
The newspaper types are stored in a JSON file located at
``resources/nz/govt/natlib/tools/sip/generation/newspapers/newspaper-types.json``
A newspaper type the following structure::

    {
      "areMedia": {
        "PDF_FILE_WITH_TITLE_SECTION_DATE_SEQUENCE_GROUPING_PATTERN": "(?<publisherCode>[a-z]{3})(?<titleCode>[a-zA-Z0-9]{2})(?<issue>\\d{4})(?<sequenceLetter>\\w{1})(?<sequenceNumber>\\d{3})(?<sectionCode>)([_])?(?<revision>[a-zA-Z0-9]{0,2})_(?<qualifier>.*?)-(?<date>\\d{6}).[pP]{1}[dD]{1}[fF]{1}",
        "PDF_FILE_WITH_TITLE_SECTION_DATE_SEQUENCE_PATTERN": ".*?\\w{2}.*?\\d{6}\\.[pP]{1}[dD]{1}[fF]{1}",
        "PDF_FILE_WITH_TITLE_SECTION_DATE_PATTERN": ".*?\\w{2}.*?\\d{6}\\.[pP]{1}[dD]{1}[fF]{1}",
        "DATE_TIME_PATTERN": "ddMMyy",
        "PATH_TO_SPREADSHEET": "default-are-media-import-parameters.json",
        "SUPPLEMENTS": {
            "Signal": "OtagoDailyTimes",
            "UBet": "OtagoDailyTimes"
        },
        "PARENT_SUPPLEMENTS": {
          "LID": "DOM",
          "LIP": "PRS",
          "LIW": "WAT"
        },
        "SUBSTITUTABLE_SUPPLEMENTS": {
          "FPD": {
            "PARENT": "DOM",
            "INITIALS": "FP",
            "MASTER": true
          },
          "FPP": {
            "PARENT": "PRS",
            "INITIALS": "FP",
            "MASTER": false
          },
          "FPW": {
            "PARENT": "WAT",
            "INITIALS": "FP",
            "MASTER": false
          }
        },
        "IGNORE": ["POSTER", "POS"],
        "REVISIONS": "R",
        "CASE_SENSITIVE": false,
        "DATE_ADJUSTMENTS": {
            "LS": 5,
            "ZW": 7
        }
      }
    }


The  key (in this case areMedia) is the name of the newspaper type and will need to be used when running the scripts.
This example doesn't match the actual Are Media configuration, but has been used to display examples of all the fields.

The three fields beginning ``PDF_FILE_WITH...`` (required) are the regular expression (regex) patterns required by the
code to validate the filenames being processed.

``DATE_TIME_PATTERN`` (required) is the date pattern used in the filenames for that newspaper type.

``PATH_TO_SPREADSHEET`` (required) is the name of the processing spreadsheet required to process the individual titles
of that newspaper type.

``SUPPLEMENTS`` (optional) is used when a newspaper has supplements whose title codes do not match the parent title.
In the example above Signal and UBet need to be processed with the OtagoDailyTimes. They differ from other supplements
which have the same title code as their parent and which do not need to be included here.
The grouping together of these files happens in the pre-processing stage.
This field only needs to be present if the newspaper type has such supplements.

``PARENT_SUPPLEMENTS`` (optional) is very similar to the SUPPLEMENTS above, but as well as being added to the parent
title, these supplements are then processed again into their own parent folder. These supplements have their own entry in
the title spreadsheet referred to in the PATH_TO_SPREADSHEET entry.
In the example above LID needs to be processed with the DOM files, but it will also be processed again into its own LID
file.
This processing happens during the pre-processing stage.
This field only needs to be present if the newspaper type has such supplements.
This field is currently only used for the Stuff Life supplements.

``SUBSTITUTABLE_SUPPLEMENTS`` (optional) is like SUPPLEMENTS, however these are supplements that are the same across
multiple titles. Occasionally only one of these is included in the FTP folder and this existing title needs to be
substituted for the missing ones. In the example above there are three supplements which are grouped together by the
INITIALS property "FP". One of these "FPD" has the MASTER property set to true, while the others are false. Therefore,
when this title is present but the others are missing, this MASTER copy will be substituted for the missing "FPD" and
"FPW" files, and placed in their respective PARENT folders.
This field only needs to be present if the newspaper type has such supplements. If present then each entry needs a
``PARENT`` property, which is the parent title it belongs to; an ``INITIALS`` property, which is the initials it begins
with and which are used to group together the supplements which can be substituted for each other; a ``MASTER``
property, which identifies the title which can be swapped for the others. In this case FPD is always present while FPP
and FPW are the ones which are usually missing, so FPD is used as the MASTER.
This processing happens during the pre-processing stage.
This field is currently only used for the Stuff Forever Project supplements.

``IGNORE`` (optional) is a list of terms that, if present in the qualifier section of a filename, indicate that file should be
ignored and not included in the sip. Files with theses terms in the filenames will be placed in the for-review/IGNORED
folder. In the case of Are Media, these are poster files which are not part of the publication.

``REVISIONS`` (optional) Some newspaper types (only Are Media at this stage) upload revised versions of files to the
ftp folder so there may be multiple versions of a file with the same date and page number to deal with. The revised
files will include a revision number in the 'revision' section of the filename.
The REVISIONS field in the Newspaper Type indicates both that revisions need to be checked for, and how those revisions
are labelled. The "R" in this case will be followed by a number in the filename. For example revisions for Are Media are
labelled R1, R2, etc. If this field is not null, the programme will check if each file has a newer revision and add the
latest revision number to the sip. Older revisions will be placed in the for-review/IGNORED folder.

``CASE_SENSITIVE`` (required) This indicates whether a Newspaper Type's titlecodes should be treated as case sensitive.
For example Allied Press's titles are in the style OtagoDailyTimes, and should be processed using case sensitivity.
However Are Media has titles such as YH which are inconstantly labelled (e.g. yh, yH, Yh). Setting CASE_SENSITIVE to
false means these inconstantly labelled files will be sorted into the same location and processed correctly.

``DATE_ADJUSTMENTS`` (optional) Some Are Media titles have a mismatch between the date in the filenames and the actual
publication date. For example the filename date for the title LS is always 5 days earlier than the publication date.
Adding the titlecode and the number of days to adjust by to this section will ensure the correct publication date is
added to the METS file.

Adding new newspaper types
~~~~~~~~~~~~~~~~~~~~~~~~~~

If a new newspaper type needs to be added, an entry with the format above needs to added to the json file.

The following fields are REQUIRED: ``PDF_FILE_WITH_TITLE_SECTION_DATE_SEQUENCE_GROUPING_PATTERN``,
``PDF_FILE_WITH_TITLE_SECTION_DATE_SEQUENCE_PATTERN``, ``PDF_FILE_WITH_TITLE_SECTION_DATE_PATTERN``,
``DATE_TIME_PATTERN``, ``PATH_TO_SPREADSHEET``, ``CASE_SENSITIVE``

While these fields are OPTIONAL: ``SUPPLEMENTS``, ``PARENT_SUPPLEMENTS``, ``SUBSTITUTABLE_SUPPLEMENTS``,
``IGNORE``, ``REVISIONS``, ``DATE_ADJUSTMENTS``

If ``SUBSTITUTABLE_SUPPLEMENTS`` is used, then each entry REQUIRES a ``PARENT``, ``INITIALS`` and ``MASTER`` property

The regular expressions need to match the format of the filename patterns for the new newspaper type.
For help with regular expressions (regex) see https://regex101.com/ for example.

A processing spreadsheet will also need to be added to the codebase and referred to in the ``PATH_TO_SPREADSHEET`` field.
See the section `Processing spreadsheet`_ for more information.

FTP stage
=========

All PDF files are placed in a single FTP folder by the file producer. There are no subfolders.

Pre-processing stage
====================

The pre-processing stage moves the files found in the ``ftp`` directory to the ``pre-processing`` folder. In the
ftp folder all the files sit in the same directory. In the ``pre-processing`` directory, the files are separated out by
date and ``title_code``, as in the following structure::

    <targetPreProcessingFolder>/<date-in-yyyyMMdd>/<TitleCode>/{files for that titleCode and date}

This file structure prepares the files for ready-for-ingestion processing.

Example processing command
--------------------------
The ``sip-generation-fat-all`` jar is executed with arguments as shown in the following example::

    newspaperType="WMMA"
    sourceFolder="/path/to/ftp/folder"
    targetBaseFolder="/path/to/LD_Sched/wairarapa-times-processing"
    targetPreProcessingFolder="${targetBaseFolder}/pre-processing"
    forReviewFolder="${targetBaseFolder}/for-review"

    startingDate="2019-06-01"
    endingDate="2019-06-15"

    # Note that the number of threads increases processing speed due to ODS poor single-thread performance
    numberOfThreads=800

    maxMemory="2048m"
    minMemory="2048m"

    java -Xms${minMemory} -Xmx${maxMemory} \
        -jar fat/build/libs/sip-generation-all-<VERSION>.jar \
        --preProcess \
        --newspaperType="${newspaperType}" \
        --startingDate="${startingDate}" \
        --endingDate="${endingDate}" \
        --sourceFolder="${sourceFolder}" \
        --targetPreProcessingFolder="${targetPreProcessingFolder}" \
        --forReviewFolder="${forReviewFolder}" \
        --createDestination \
        --moveFiles \
        --parallelizeProcessing \
        --numberOfThreads ${numberOfThreads}


For-review
----------
If a file or set of files is unable to be processed for some reason, it will be placed in the *For-review* folder. There
is no processor that operates on the *For-review* stage. Processors that output to the *For-review* folder use the
parameter ``forReviewFolder`` to set the location of the *For-review* folder.

FTP files with identifiable title_code
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
If the files come from the FTP folder and the *TitleCode* and date are identifiable from the filename, the files are in
the following structure::

    <forReviewFolder>/<date-in-yyyyMMMdd>/<TitleCode>/{files}

FTP files without identifiable title_code and identifiable date
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
If the files come from the FTP folder and the *TitleCode* is not identifiable from the filename (but the date is), the
files are in the following structure::

    <forReviewFolder>/UNKNOWN-TITLE-CODE/<date-in-yyyyMMdd>/{files-that-have-no-title-code-mapping-for-that-date}

FTP files without identifiable title_code and without identifiable date
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
If the files come from the FTP folder and the *TitleCode* and date are not identifiable from the filename, the files are
in the following structure::

    <forReviewFolder>/UNKNOWN-TITLE-CODE/UNKNOWN-DATE/{files-that-have-no-title-code-mapping-for-that-date}


Ready-for-ingestion stage
=========================

The second state of processing where files are aggregated into specific SIPs ready for ingestion into Rosetta.

Note that the ``--moveFiles`` option is currently not supported, as multiple processing types operate on the same set
of files.

The *Ready-for-ingestion* folder structure is how Rosetta ingests the files. Magazines and newspapers have different
*Material Flows*, so ingestion of those different IEEntity types must be in different folders.

Processing spreadsheet
----------------------
A processing spreadsheet is used in the ready-for-ingestion stage to determine how a particular set of files
associated with a title code are processed.

Each newspaper type has its own processing spreadsheet.

Default spreadsheet
~~~~~~~~~~~~~~~~~~~
A spreadsheet exists for each newspaper type. The spreadsheet determines how a given title code is processed for a given
processing type. A default spreadsheet exists for each newspaper type in the codebase under
``src/main/resources/nz/govt/natlib/tools/sip/generation/newspapers/``.
These spreadsheets use a column delimiter of ``|``.

Spreadsheet conversion to JSON
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
Build script tasks exist to convert a ``.csv`` spreadsheet to a ``.json`` file. See the section
`Converting the spreadsheet to JSON and vice-versa`_ for an explanation on how that conversion is done.

The ready-for-ingestion processing operates on the JSON version of the spreadsheet information. For this reason, any
changes to the csv spreadsheet **must** be converted to JSON for the processing to use those changes.

Newspaper type config file structure
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
The newspaper types are stored in a JSON file and have the following structure::

    {
      "alliedPress": {
        "PDF_FILE_WITH_TITLE_SECTION_DATE_SEQUENCE_GROUPING_PATTERN": "(?<titleCode>[a-zA-Z0-9]{4,19})(?<sectionCode>)-(?<date>\\d{2}\\w{3}\\d{4})(?<sequenceLetter>)(?<sequenceNumber>)-(?<qualifier>\\w{3})\\.[pP]{1}[dD]{1}[fF]{1}",
        "PDF_FILE_WITH_TITLE_SECTION_DATE_SEQUENCE_PATTERN": "\\w{4,19}-\\d{2}\\w{3}\\d{4}-\\w{1,3}.*?\\.[pP]{1}[dD]{1}[fF]{1}",
        "PDF_FILE_WITH_TITLE_SECTION_DATE_PATTERN": "\\w{4,19}-\\d{2}\\w{3}\\d{4}-.*?\\.[pP]{1}[dD]{1}[fF]{1}",
        "DATE_TIME_PATTERN": "ddMMMyyyy",
        "PATH_TO_SPREADSHEET": "default-allied-press-import-parameters.json",
        "SUPPLEMENTS": {
          "Signal": "OtagoDailyTimes",
          "UBet": "OtagoDailyTimes"
        }
      }
    }

::
The key is the name of the newspaper type and this will need to be used when running the scripts. In this example the key is
alliedPress.


Spreadsheet structure
~~~~~~~~~~~~~~~~~~~~~
The structure of the spreadsheet is discussed in the :doc:`librarian-guide`.

JSON file structure
~~~~~~~~~~~~~~~~~~~
The JSON-file structure lays out the same parameters in a JSON format. The actual processing uses the JSON file as its
processing input. For example, the Taupo Times has the following entry::

    {
        "row-0001": {
            "MMSID": "9918822769202836",
            "title_parent": "Wairarapa times-age.",
            "processing_type": "parent_grouping",
            "processing_rules": "",
            "processing_options": "numeric_before_alpha",
            "publication_key": "title_code",
            "title_code": "WMMA",
            "edition_discriminators": "",
            "section_codes": "",
            "Access": "200",
            "Magazine": "0",
            "ingest_status": "STA",
            "Frequency": "",
            "entity_type": "PER",
            "title_mets": "Wairarapa times-age.",
            "ISSN online": "",
            "Bib ID": "",
            "Access condition": "",
            "Date catalogued": "",
            "Collector_folder": "TBC",
            "Cataloguer": "",
            "Notes": "",
            "first_issue_starting_page": "",
            "last_issue_starting_page": "",
            "has_volume_md": "0",
            "has_issue_md": "0",
            "has_number_md": "0",
            "previous_volume": "",
            "previous_volume_date": "",
            "previous_volume_frequency": "",
            "previous_issue": "",
            "previous_issue_date": "",
            "previous_issue_frequency": "",
            "previous_number": "",
            "previous_number_date": "",
            "previous_number_frequency": ""
        }
    }


Folder structure
----------------
The structure of the ready-for-ingestion output is discussed in the :doc:`librarian-guide`.

Deciding how to process: Processing types, spreadsheets and folders
-------------------------------------------------------------------

When the ready-for-ingestion processing takes place, each folder that gets processed has a ``title_code`` (which is the
name of the folder itself. The ready-for-ingestion processing takes that ``title_code`` and matches it with a
spreadsheet for the given ``processing_type``. If there is no spreadsheet row that matches the ``title_code`` and
``processing_type``, then no processing for that type takes place. There may be other processing types that match a
specific spreadsheet row.

Processing types
----------------

There are different processing types that have slightly different ways of dealing with the files in a ``title_code``
folder. When multiple processing types are specified, the processing types checked in order until a spreadsheet row
is found that matches. Processing types themselves correspond to the class ``ProcessingType``.

The processing types are checked in the following order: ``parent_grouping_with_edition``, ``parent_grouping``,
``supplement_grouping`` and finally ``create_sip_for_folder``.

parent_grouping_with_edition
~~~~~~~~~~~~~~~~~~~~~~~~~~~~
This is for processing where the title code and edition discriminator combine to form a unique key. There are some
publications where this is the case. One example is the title code ``ADM``, which has two different editions,
``NEL`` and ``MEX``, each with their own MMSID. The ``title_parent`` is used as the publication title.

``parent_grouping_with_edition``
    The ``title_code`` is combined with the first ``edition_discriminators`` to produce a spreadsheet row match.

``parent_grouping_with_edition`` default rules:
    ``skip_ignored``, ``skip_unrecognised``, ``skip_invalid``, ``automatic``, ``required_all_sections_in_sip``,
    ``missing_sequence_is_error``, ``missing_sequence_double_wide_is_ignored``, ``ignore_editions_without_files``,
    ``zero_length_pdf_replaced_with_page_unavailable``, ``do_not_force_skip``,
    ``numeric_starts_in_hundreds_not_considered_sequence_skips``, ``do_not_require_first_section_code_for_match``.

``parent_grouping_with_edition`` default options:
    ``numeric_before_alpha``.

parent_grouping
~~~~~~~~~~~~~~~
This is the most common grouping where the title code by itself is enough to determine the publication. The
``title_parent`` is used as the publication title.

``parent_grouping``
    The ``title_code`` is used to produce a spreadsheet row match.

``parent_grouping`` default rules:
    ``skip_ignored``, ``skip_unrecognised``, ``skip_invalid``, ``automatic``, ``required_all_sections_in_sip``,
    ``missing_sequence_is_error``, ``missing_sequence_double_wide_is_ignored``, ``ignore_editions_without_files``,
    ``zero_length_pdf_replaced_with_page_unavailable``, ``do_not_force_skip``,
    ``numeric_starts_in_hundreds_not_considered_sequence_skips``, ``do_not_require_first_section_code_for_match``.

``parent_grouping`` default options:
    ``numeric_before_alpha``.

supplement_grouping
~~~~~~~~~~~~~~~~~~~
For some publications we want to extract a subset of the ``title_parent`` publication into a separate publication that
is loaded with its own separate MMSID. The ``title_mets`` is used as the publication title.

TODO The code for this extraction is not complete and will require some more tweaking and default spreadsheet changes.
For example, some supplements are based on having certain sequence letters. There may be multiple supplements that match
on the same set of files (for example, the TAB section code, which often maps to a different supplement). They may rely
on being on a certain day of the week or month of the year. Much of the determination of what the publication maps to
may rely on human intervention.

TODO One approach for dealing with extracting supplements that are specific to certain sequence letters is to add a new
spreadsheet column ``sequence_letters`` and the supplement grouping would only select the files for processing if the
given set of sequence letters existed in the files in the title code folder. This is similar to how
``parent_grouping_with_edition`` works with editions. In other words, if the sequence letters have been set in the
spreadsheet row and they do exist in the set of files, then process the supplement grouping against the set of files.
Otherwise, there isn't a match and that supplement grouping is skipped. This would likely require an additional rule
so that the sequence letters would be used as a filter for processing files.

TODO The use of ``sequence_letters`` could also be used to determine the ordering of the pages if a non-alphabetical
ordering is required. This would likely require an additional rule so that ordering would be used.

``supplement_grouping``
    The ``title_code`` and ``section_code`` is used to produce a spreadsheet row match. This is generally used for
    publications that are part of a parent publication (for example, a parent publication might have a special section
    that can be extracted with its own MMSID).

``supplement_grouping`` default rules:
    ``skip_ignored``, ``skip_unrecognised``, ``skip_invalid``, ``automatic``, ``optional_all_sections_in_sip``,
    ``missing_sequence_is_error``, ``missing_sequence_double_wide_is_ignored``, ``ignore_editions_without_files``,
    ``zero_length_pdf_replaced_with_page_unavailable``, ``do_not_force_skip``,
    ``numeric_starts_in_hundreds_not_considered_sequence_skips``, ``require_first_section_code_for_match``.

``supplement_grouping`` default options:
    ``numeric_before_alpha``.

create_sip_for_folder
~~~~~~~~~~~~~~~~~~~~~
This is a catch-all for all the publications that don't have a corresponding spreadsheet row. The ``mets.xml`` will
still be created, but it will need to be edited to have the correct MMSID and publication title. It can be helpful to
include this processing type in the set of processing types so that much of the work processing one-off publications
can be done automatically without having to make changes to the parameters spreadsheet.

``create_sip_for_folder``
    This a catch all for when there is no spreadsheet row match. The ``title_code`` is still used to produce an output
    folder structure with the given files. However, the ``mets.xml`` does not have MMSID, publication name, access
    value. All those values would need editing before the folder could be ingested into Rosetta.

``create_sip_for_folder`` default rules:
    ``skip_ignored``, ``skip_unrecognised``, ``skip_invalid``, ``automatic``, ``required_all_sections_in_sip``,
    ``missing_sequence_is_error``, ``missing_sequence_double_wide_is_ignored``, ``ignore_editions_without_files``,
    ``zero_length_pdf_replaced_with_page_unavailable``, ``do_not_force_skip``,
    ``numeric_starts_in_hundreds_not_considered_sequence_skips``, ``do_not_require_first_section_code_for_match``.

``create_sip_for_folder`` default options:
    ``numeric_before_alpha``.

Processing rules
----------------
Processing rules determine how certain aspects of the workflow take place. Each processing rule has an opposite rule
that can be used to override its value.

``handle_ignored``
    Ignored files are placed in a separate for-review folder called ``IGNORED/date/title_code``. Override is
    ``skip_ignored``.

``skip_ignored``
    Ignored files are not placed in any separate folders. Override is ``handle_ignored``.

``handle_unrecognised``
    Unrecognised files are placed in a separate for-review folder called ``UNRECOGNIZED/date/title_code``. Override is
    ``skip_unrecognised``.

``skip_unrecognised``
    Unrecognised files are not placed in any separate folders. Override is ``handle_unrecognised``.

``handle_invalid``
    Invalid files are placed in a separate for-review folder called ``INVALID/date/title_code``. Override is
    ``skip_invalid``.

``skip_invalid``
    Invalid files are not placed in any separate folders. Override is ``handle_invalid``.

``manual``
    The generated file structure is always sent to for-review if there are no errors. Override is ``automatic``.

``automatic``
    The generated file structure is set to ready-for-ingestion if there are no errors. Override is ``manual``.

``force_skip``
    Skips the processing of the given type/date/title_code combination. Useful for spreadsheet rows that are not being
    processed correctly. Override is ``do_not_force_skip``.

``do_not_force_skip``
    Processes the given type/date/title_code combination. Override is ``force_skip``.

``process_all_editions``
    Process all the editions for a given title_code, even if there are no specific edition files. Override is
    ``ignore_editions_without_files``.

``ignore_editions_without_files``
    Only processes edition for a given title_code that has actual edition-specific files. For example, there might be
    ``edition_discriminators`` ``ED1+ED2+ED3``, but only ``ED1`` and ``ED2`` files exist. In that case, only ``ED1`` and
    ``ED2`` output would be created. Override is ``process_all_editions``.

``require_first_section_code_for_match``
    The sorted file list's first file's section code must match the first section code in the list of ``section_codes``.
    Otherwise the spreadsheet row will not match. This rule only exists for situations where a particular section code
    for a supplement sometimes comes on its own and needs to be processed with its own MMSID. For example, MEXTAB. Use
    this rule carefully because of possible non-matching side effects. Override is
    ``do_not_require_first_section_code_for_match``.

``do_not_require_first_section_code_for_match``
    Do not require the sorted file list's first file's section code must match the first section code in the list of
    ``section_codes``. This is the usual default. Override is ``require_first_section_code_for_match``.

``edition_discriminators_using_smart_substitute``
    For processing type ``parent_grouping_with_edition``, the ``title_code`` and a specific ``section_code`` form the
    spreadsheet row key. ``edition_discriminators_using_smart_substitute`` is for something like the following
    situation: For the ``title_code`` ``QCM`` we want to make edition substitutions, but eachedition discriminator has
    its own section code. We have titleCode: QCM, with 3 separate editions: edition discriminator: ED1,
    section_codes: ED1; edition discriminator: ED2, section_codes: ED2; and editionDiscriminator: ED3,
    section_codes: ED3. We still want to substitute the pages in ED2 and ED3 over the ED1 pages. In order to do that, we
    find the FIRST edition discriminator and set the edition discriminators to the FIRST edition discriminator and the
    current edition (section code). That means for ED2, we would use the ED1 pages and substitute in the ED2 pages.
    Override is ``edition_discriminators_not_using_smart_substitute``.

``required_all_sections_in_sip``
    All sections are required to appear in the SIP. If they are not included based on the spreadsheet row, then an
    exception is generated. Override is ``optional_all_sections_in_sip``.

``optional_all_sections_in_sip``
    Not all sections are required to appear in the SIP. Override is ``required_all_sections_in_sip``.

``missing_sequence_is_ignored``
    Missing sequences in page numbering (such as skipping from page 1 to 3) are ignored. Override is
    ``missing_sequence_is_error``.

``missing_sequence_is_error``
    Missing sequences are not treated as an error. Override is ``missing_sequence_is_ignored``.

``missing_sequence_double_wide_is_ignored``
    A missing sequence whose previous page is either double the width or half the width or the current page is treated
    as if there is no missing sequence. This is to handle the common situation of double-wide pages. Override is
    ``missing_sequence_double_wide_is_error``.

``missing_sequence_double_wide_is_error``
    Even if the previous page is double the width or half the width of the current page, the missing sequence is still
    treated as an error (if ``missing_sequence_is_error`` is a rule). Override is
    ``missing_sequence_double_wide_is_ignored``.

``zero_length_pdf_replaced_with_page_unavailable``
    A zero-length PDF file (a file with a size of ``0``) is replaced with the standard *page unavailable* PDF file.
    This file is found in the codebase under ``core/src/main/resources/page-unavailable.pdf``. Override is
    ``zero_length_pdf_skipped``.

``zero_length_pdf_skipped``
    A zero-length PDF file (a file with a size of ``0``) is skipped (not replaced by any other file). Override is
    ``zero_length_pdf_replaced_with_page_unavailable``.

``numeric_starts_in_hundreds_not_considered_sequence_skips``
    There are some cases where a wrap starts in the 400's. Normally this would be considered a skipped sequence, but
    with this option sequence numbering starting in the 400's or more (so starting with 400 or 401, or 500 or 501, and
    so on) is not considered a sequence numbering skip. Override is
    ``numeric_starts_in_hundreds_considered_sequence_skips``.

``numeric_starts_in_hundreds_considered_sequence_skips``
    Sequence numbering skips that start with 400 or 401 or 500 or 501 and so on are still treated as a sequence
    numbering skip. Override is ``numeric_starts_in_hundreds_not_considered_sequence_skips``.

``use_filename_for_mets_label``
    Use the name of the file as its label within the mets.xml file rather than the default page number. Useful for
    single page pdfs such as Allied Press. Override is ``use_number_for_mets_label``.

Processing options
------------------
Processing options determine how certain aspects of the workflow take place. Each processing option has an opposite
option that can be used to override its value. In general options don't have side effects, but rules do.

``alpha_before_numeric``
    Sequences are sorted with sequence letters sorted before sequence numbers only. So, we would have ordering
    ``A01, A02, B01, B02, 01, 02``. Override is ``numeric_before_alpha``.

``numeric_before_alpha``
    Sequences are sorted with sequence numbers only sorted before sequence letters only. So, we would have ordering
    ``01, 02, A01, A02, B01, B02``. Override is ``alpha_before_numeric``.

``full_date_in_sip``
    The full date will be used in the designation data of the sip mets.xml file. For example::

    <dc:record xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:dcterms="http://purl.org/dc/terms/" xmlns:dc="http://purl.org/dc/elements/1.1/">
        <dc:title>The Ensign</dc:title>
        <dc:date>2022</dc:date>
        <dcterms:available>06</dcterms:available>
        <dc:coverage>01</dc:coverage>
    </dc:record>

   Override is ``issue_only_in_sip``

``issue_only_in_sip``
    For publications that have an issue number, this option can be used to populate the sip mets.xml instead. For
    example::

    <dc:record xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:dcterms="http://purl.org/dc/terms/" xmlns:dc="http://purl.org/dc/elements/1.1/">
        <dc:title>Australian women's weekly</dc:title>
        <dc:date>2022</dc:date>
        <dcterms:available>05</dcterms:available>
    </dc:record>

   Override is  ``full_date_in_sip``

Overrides for rules and options
-------------------------------
Processing rules and options can be overridden on several different levels.

Each processing type has a set of default processing rules and processing options.

The processing type rules and options are overridden by the rules and options in the given spreadsheet row that is
matched for processing a given ``title_code`` folder.

Finally, the command-line processing rules and processing options are applied and will override all previous options.

For example, the ``parent_grouping`` processing type has default processing option, ``numeric_before_alpha``. When
processing the title code ``DPT``, this default option is overridden by ``alpha_before_numeric`` for the DPT row
for ``parent_grouping``. Finally, it is possible to specify a processing option ``numeric_before_alpha`` on the
command line, which would mean that all processing sorts the ordering of PDFs as ``numeric_before_alpha``.

File processed indicator: *ready-for-ingestion-FOLDER-COMPLETED* file
---------------------------------------------------------------------
Currently the ready-for-ingestion processing runs each separate title code folder on its own individual thread. When
an exception occurs that halts processing for a specific thread, other threads will continue processing. It is possible
for processing for many folders to be incomplete while at the same time others have completed. For example, the
processing may lose its connection to the source and target folders in the middle of processing. To help determine which
processing has successfully completed, the ready-for-ingestion processor will write an empty file
``ready-for-ingestion-FOLDER-COMPLETED`` in the target folder to indicate that all processing stages were successfully
completed. If this file is not present it means that the processing for that folder was interrupted for some reason and
will need to be re-run.

Example processing command
--------------------------
The following snippet illustrates a ready-for-ingestion processing command::

    newspaperType="WMMA"
    sourceFolder="path/to/LD_Sched/wairarapa-times-processing/pre-processing"
    targetBaseFolder="/path/to/LD_Sched/wairarapa-times-processing"
    targetForIngestionFolder="${targetBaseFolder}/for-ingestion"
    forReviewFolder="${targetBaseFolder}/for-review"

    startingDate="2019-06-03"
    endingDate="2019-06-09"

    forIngestionProcessingTypes="parent_grouping,parent_grouping_with_edition,create_sip_for_folder"

    numberOfThreads=60

    maxMemory="3048m"
    minMemory="3048m"

    java -Xms${minMemory} -Xmx${maxMemory} \
        -jar fat/build/libs/sip-generation-WMMA-fat-all-<VERSION>.jar \
        --readyForIngestion \
        --newspaperType="${newspaperType}" \
        --startingDate="${startingDate}" \
        --endingDate="${endingDate}" \
        --sourceFolder="${sourceFolder}" \
        --targetForIngestionFolder="${targetForIngestionFolder}" \
        --forReviewFolder="${forReviewFolder}" \
        --createDestination \
        --parallelizeProcessing \
        --numberOfThreads=${numberOfThreads} \
        --forIngestionProcessingTypes="${forIngestionProcessingTypes}" \
        --forIngestionProcessingRules="${forIngestionProcessingRules}" \
        --forIngestionProcessingOptions="${forIngestionProcessingOptions}"

Terminating or stopping ready-for-ingestion processing with *ready-for-ingestion-STOP* file
-------------------------------------------------------------------------------------------
Sometimes it may be necessary to terminate the ready-for-ingestion processing prematurely, before it has completed
processing all of its folders. There is some code in the processor that attempts to trap a ``^C`` or kill signal and
attempt a graceful shutdown, but that code does not seem functional at the moment.

The other approach is to create a file in the ``targetForIngestionFolder`` with the name
``ready-for-ingestion-STOP``. When this file appears all existing processing will complete and all subsequent
processing will be skipped. At the end of all processing the log will provide a list of skipped folders.

Note that it's quite possible to delete the ``ReadyForIngestionProcessor_STOP`` file, in which case processing will
continue. However, there is no attempt to run any skipped processing.

Managing errors in processing
-----------------------------
Sometimes processing for a specific folder may fail for some reason. For example, if the source and/or target folders
are NFS shares, the connection to the source or target may be interrupted, throwing some kind of IO exception. This
exception will halt the processing for that particular source folder. However, if the problem is intermittent (in other
words, the connection is lost but then comes back), then other processing may work fine.

At the end of a processing run the list of failed folders will be provided with the reason for that folder's processing
failing. The suggestion is to copy those failed folders to a separate location and process them again.

Note as well that if there is an failure in processing a folder, the ``ready-for-ingestion-FOLDER-COMPLETED`` file will
not be present in the target location. The folders that do not have the ``ready-for-ingestion-FOLDER-COMPLETED`` will
need to be deleted so that they are not ingested into Rosetta by mistake.

For-review
----------
See the :doc:`librarian-guide` for a discussion of the for-review output and how a librarian handles the different
exceptions to processing.


Copying ingested loads to ingested folder
=========================================

Once files have been ingested into Rosetta, a file with the name of ``done`` is placed in the root folder. The path of
the root folder is of the format::

    <magazine|newspaper>/<date-in-yyyyMMdd>_<title_code>_<processing_type>_<optional-edition>__<full-name-of-publication>

After the folder has been ingested into Rosetta the folder can be moved to the ``post-processed`` folder.

post-processed folder structure
-------------------------------
The folder structure for the ingested (post-processed) stage is as follows::

    <targetFolder>/<magazines|newspapers>/<title_code>/<yyyy>/<folder-containing-done-file>

The naming of the folder containing of the done file is determined by the processing rules for the ready-for-ingestion
processor. See `Ready-for-ingestion stage`_ for more details. In this folder, the file structure matches the same
structure that was ingested into Rosetta, namely::

    <folder-specific-naming>
       |- done
       |- content/
               |- mets.xml
               |- streams/
                       |- <pdf-files>

Note that the ``mets.xml`` file is placed in the `content` folder. The ``done`` files is in the root folder.

Example processing command
--------------------------
The following snippet illustrates a ``--copyIngestedLoadsToIngestedFolder`` processing command::

    baseFolder="/path/to/LD_Sched/wairarapa-times-processing"
    sourceFolder="${baseFolder}/for-ingestion"
    targetPostProcessedFolder="${baseFolder}/post-processed"
    forReviewFolder="${baseFolder}/for-review"

    startingDate="2019-06-03"
    endingDate="2019-06-09"

    # Currently the processing is not multithreaded, but eventually it would be
    numberOfThreads=60

    maxMemory="2048m"
    minMemory="2048m"

    java -Xms${minMemory} -Xmx${maxMemory} \
        -jar fat/build/libs/sip-generation-WMMA-fat-all-<VERSION>.jar \
        --copyIngestedLoadsToIngestedFolder \
        --startingDate="${startingDate}" \
        --endingDate="${endingDate}" \
        --sourceFolder="${sourceFolder}" \
        --targetPostProcessedFolder="${targetPostProcessedFolder}" \
        --forReviewFolder="${forReviewFolder}" \
        --createDestination \
        --parallelizeProcessing \
        --numberOfThreads=${numberOfThreads}

Important notes
---------------
The ``--moveFiles`` option is not included in the example, but in general you would be moving the files to the
post-processed location.

The the ``done`` file must exist or the files will not be copied/moved. If files must be copied regardless of the
existence of the ``done`` file, use the option ``--moveOrCopyEvenIfNoRosettaDoneFile``.

For-review
----------
If a file or set of files is unable to be processed for some reason, it will be placed in the *For-review* folder. There
is no processor that operates on the *For-review* stage. Processors that output to the *For-review* folder use the
parameter ``forReviewFolder`` to set the location of the *For-review* folder.

If the files come from the *Ready-for-ingestion* stage but are not ingested into Rosetta properly, then there is no
``done`` file placed in the root folder. There's no other way to tell that the ingestion has failed. For this reason,
the ``copyIngestedLoadsToIngestedFolder`` processing usually only moves/copies the folders that contain a ``done`` file.

After an ingestion takes place the ingested folders (those containing the ``done`` file) can be moved to the
``targetPostProcessedFolder``. The folders that remain can be reviewed to determine the reason for failure.


Additional tools
================
cleanUpFTP: delete files from the source folder
-----------------------------------------------
This process permanently deletes all matching files from the source FTP folder. It requires a date range and a newspaper
type. Because it permanently deletes all matching files, it should be run with caution. A one month buffer should be
kept in the FTP folder at all times. So for example if the process is being run in March, the ending date for the
process should be no later than January::

    java -Xms${minMemory} -Xmx${maxMemory} \
        -jar ../fat/build/libs/sip-generation-newspapers-fat-all-<VERSION>.jar \
        --cleanUpFTP \
        --newspaperType="${newspaperType}" \
        --startingDate="${startingDate}" \
        --endingDate="${endingDate}" \
        --sourceFolder="${sourceFolder}"


listFiles: list files based on source folder
--------------------------------------------
``listFiles`` simply lists files by title code, section code and date::

    java -jar sip-generation-newspapers-fat-all-<VERSION>.jar \
        --listFiles \
        --startingDate="yyyy-MM-dd" \
        --endingDate="yyyy-MM-dd" \
        --sourceFolder="/path/to/source/folder"

extractMetadata: extract metadata from the pdf files based on source folder
---------------------------------------------------------------------------
Extracts metadata from the pdf files::

    java -jar sip-generation-WMMA-fat-all-<VERSION>.jar \
        --extractMetadata \
        --startingDate="yyyy-MM-dd" \
        --endingDate="yyyy-MM-dd" \
        --sourceFolder="/path/to/source/folder"


copyProdLoadToTestStructures: Copy production load files
--------------------------------------------------------
Copies files from previous production loads into Rosetta into Pre-processing *and* Ready-for-ingestion structures
for testing. The structures are as follows:

    1. preProcess structure. This is to mimic the input to readyForIngestion processing. The folder structures are the
    same as the output to `preProcess`, with the folder structure starting with ``<targetFolder>/preProcess``.
    2. readyForIngestion structure. This is the structure that gets ingested into Rosetta. The folder structures are the
    same as the output to `readyForIngestion`, with the folder structure starting with
    ``<targetFolder>/readyForIngestion``.

These structures provide for testing the processor, to see if its outputs match the work done previously::

    java -jar sip-generation-WMMA-fat-all-<VERSION>.jar \
        --copyProdLoadToTestStructures \
        --startingDate="yyyy-MM-dd" \
        --endingDate="yyyy-MM-dd" \
        --sourceFolder="/path/to/source/folder" \
        --targetFolder="/path/to/target/folder" \
        --createDestination

Adding new newspaper types
=================================================

From time to time a new newspaper publication type will need to be added to the configurations. The newspaper types
are configured in the json file located at
``core/src/main/resources/nz/govt/natlib/tools/sip/generation/newspapers/newspaper-types.json``
Within this file a newspaper type has the following structure::

    {
      "alliedPress": {
        "PDF_FILE_WITH_TITLE_SECTION_DATE_SEQUENCE_GROUPING_PATTERN": "(?<titleCode>[a-zA-Z0-9]{4,19})(?<sectionCode>)-(?<date>\\d{2}\\w{3}\\d{4})(?<sequenceLetter>)(?<sequenceNumber>)-(?<qualifier>\\w{3})\\.[pP]{1}[dD]{1}[fF]{1}",
        "PDF_FILE_WITH_TITLE_SECTION_DATE_SEQUENCE_PATTERN": "\\w{4,19}-\\d{2}\\w{3}\\d{4}-\\w{1,3}.*?\\.[pP]{1}[dD]{1}[fF]{1}",
        "PDF_FILE_WITH_TITLE_SECTION_DATE_PATTERN": "\\w{4,19}-\\d{2}\\w{3}\\d{4}-.*?\\.[pP]{1}[dD]{1}[fF]{1}",
        "DATE_TIME_PATTERN": "ddMMMyyyy",
        "PATH_TO_SPREADSHEET": "default-allied-press-import-parameters.json",
        "SUPPLEMENTS": {
          "Signal": "OtagoDailyTimes",
          "UBet": "OtagoDailyTimes"
        }
      }
    }

Converting the spreadsheet to JSON and vice-versa
=================================================

From time to time the spreadsheets that defines how the files are ingested will be changed based on new information.
When this happens, the json file for the particular newspaper type found at ``core/src/main/resources/`` needs updating
to reflect the changes in the source spreadsheet.

Converting the csv spreadsheet to JSON
--------------------------------------
    1. First, export the original spreadsheet in ``.csv`` format with the file separator as ``|`` and save it.
    2. Copy the exported csv spreadsheet to:
       ``core/src/main/resources/nz/govt/natlib/tools/sip/generation/``.
        with the filename in the pattern: ``default-<newspapertype>-import-spreadsheet.csv``
        e.g ``default-WMMA-import-spreadsheet.csv``
    3. Execute the gradle task ``updateDefaultNewspaperImportParameters``, which takes the csv spreadsheet and converts it
       to a JSON file, which is then used for the actual processing::

            ./gradlew updateDefaultNewspaperImportParameters \
              -PnewspaperSpreadsheetImportFilename="core/src/main/resources/nz/govt/natlib/tools/sip/generation/newspapers/default-WMMA-import-spreadsheet.csv" \
              -PnewspaperSpreadsheetExportFilename="core/src/main/resources/nz/govt/natlib/tools/sip/generation/newspapers/default-WMMA-import-parameters.json"

Note that there is no requirement to use the filenames given in the example. The given filenames are the ones the code
uses.

Converting the JSON parameters to csv spreadsheet
-------------------------------------------------
The JSON file can be converted to a csv spreadsheet using the build task ``exportDefaultNewspaperImportParameters``::

    gradle exportDefaultNewspaperImportParameters \
      -PnewspaperSpreadsheetImportFilename="core/src/main/resources/nz/govt/natlib/tools/sip/generation/newspapers/default-WMMA-import-parameters.json" \
      -PnewspaperSpreadsheetExportFilename="core/src/main/resources/nz/govt/natlib/tools/sip/generation/newspapers/default-WMMA-import-spreadsheet.csv"

Note that there is no requirement to use the filenames given in the example. The given filenames are the ones the code
uses.

Check in the changes and build a new version of the jar
-------------------------------------------------------
Once both the ``.csv`` and ``.json`` files have been updated, changes should then be checked in and a new version of this
the processor jar built, which will have the new JSON processing resource file.


Copying and moves
=================

File copying
------------
File copies are done in 2 steps:
- The file is copied to its new target with a file extension of ``.tmpcopy``.
- The file is renamed to the target name.

This means that the target does not have its correct name until the copy is complete. Subsequent runs on the same source
do checks to see if the target's MD5 hash is the same. If the hash is the same, the copy is not done.

Atomic file moves
-----------------
Some processing has a ``--moveFiles`` option. Note that when moving files *across* file systems (in other words, from
one file system to another), it's not possible to have truly atomic operations. If the move operation is interrupted
before it completes, what can happen is that a file of the same name will exist on both filesystems, with the target
file system having an incomplete file.

With that in mind, file moves have the following characteristics:

- If a file move can be done atomicly (as determined by the Java runtime), it is done atomicly.
- If the file move cannot be done atomicly (as determined by the Java runtime), the file moves take the following steps:
    1. The file is copied across to the target file system with a ``.tmpcopy`` extension.
    2. The file is renamed to the target file name.
    3. The source file is deleted.

This means that if at any point the operation is interrupted, a recovery can take place. A move when the file already
exists in the target folder will trigger a MD5 hash comparison. If the source file and the target file are identical,
the source file is deleted. Otherwise, the target file is moved across (using the steps above) with a ``-DUPLICATE-#``
in the filename. These ``-DUPLICATE-#`` files need to be checked manually to determine which file is correct.

We hope these mitigations will prevent any data loss.
