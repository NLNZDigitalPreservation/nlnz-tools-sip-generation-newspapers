===============
Librarian Guide
===============

Additional TODO
===============

-   Placeholder for additional TODO items.


Introduction
============

About NLNZ Tools SIP Generation Newspapers
-----------------------------------------------

NLNZ Tools SIP Generation Newspapers is specific set of tools for processing digital newspaper files. The ultimate
output of these tools are SIPs for ingestion into the Rosetta archiving system.

About this document
-------------------

This document is the NLNZ Tools SIP Generation Newspapers Librarian Guide. It describes how a librarian helps in
processing newspaper files from when they are dropped into a FTP folder to the point they are ingested into Rosetta
archiving system.

The manual is divided into chapters, each of which deals with a particular aspect of the role.

See also:

-   :doc:`Workflow Guide<workflow-guide>`
-   :doc:`Script Runner Guide<script-runner-guide>`
-   :doc:`Developer Guide<developer-guide>`
-   :doc:`FAQ<faq>`

Contents of this document
-------------------------

Following this introduction, this User Guide includes the following sections:

-   **PDF filenames** - Covers the structure of a PDF filename.

-   **Processing spreadsheet** - Covers the fields in the spreadsheet.

-   **Ready-for-ingestion processing** - Discusses the Ready-for-ingestion processing, its input and outputs.

-   **Ready-for-ingestion For-review** - Covers reviewing Ready-for-ingestion processing exceptions.


PDF filenames
=============
The PDF files produced by each newspaper type have a particular filename structure which can be broken up into parts.

For example Wairarapa Times Age has the following filename structure::

    <title_code>-ddmmmyy-<optional-sequence-letter><optional-sequence-number><optional-qualifier>.pdf

For example, the filename ``WMMA01Jan21A001dpsLH.pdf`` has the following values::

    title_code: WMMA
    date: 01Jan21 
    sequence_letter: A
    sequence_number: 01
    qualifier: dpsLH

- A ``title_code`` is generally 3 - 4 characters long.
- A ``date`` is always of the format yyyyMMdd.
- A ``sequence_letter`` is optional, and is generally of the form A, B, C, ...
- A ``sequence_number`` is required, and is generally of the form 1, 2, 3, ... or 01, 02, 03, ... or 001, 002, 003, ...
- A ``qualifier`` is optional and is anything past the ``sequence_number``.  Often dpsLH or dpsRH will be used to indicate 
  that it is either the left hand side (LH) or righthand side (RH) of a double page.
- The extension is some form of ``pdf``, in either lower, upper or mixed case (for example, ``pDf`` is acceptable).

As an example, `WMMA01Jan21A001.pdf` and `WMMA01Jan21A001dpsLH.Pdf` are valid filenames.

As an example, `WMM20210101A001.pdf` is an invalid filename.  It does not have a
long enough title_code block, and the second's date is not the correct format.


Processing spreadsheets
=======================

Each Newspaper Type has a processing spreadsheet which lists all titles associated with that type.

The processing spreadsheet is used in the ready-for-ingestion stage to determine how a particular set of files
associated with a title code are processed.


Default spreadsheets
--------------------
A spreadsheet exists for each newspaper type that determines how a given title code is processed for a given processing
type. Default spreadsheets exist in the codebase for each newspaper type
Wairarapa Timeas Age::
``src/main/resources/nz/govt/natlib/tools/sip/generation/newspapers/default-WMMA-import-spreadsheet.csv``.
Allied Press::
``src/main/resources/nz/govt/natlib/tools/sip/generation/newspapers/default-allied-press-import-spreadsheet.csv``.
Westport News::
``src/main/resources/nz/govt/natlib/tools/sip/generation/newspapers/default-wptnews-import-spreadsheet.csv``.

These spreadsheets use a column delimiter of ``|``.

Spreadsheet structure
---------------------
While the spreadsheet has many columns, not all columns will be listed as some of them pertain to how the files were
initially catalogued. For example the Taupo Times has the following entry (with column headers)::

    MMSID|title_parent|processing_type|processing_rules|processing_options|publication_key|title_code|edition_discriminators|section_codes|Access|Magazine|ingest_status|Frequency|entity_type|title_mets|ISSN online|Bib ID|Access condition|Date catalogued|Collector_folder|Cataloguer|Notes|first_issue_starting_page|last_issue_starting_page|has_volume_md|has_issue_md|has_number_md|previous_volume|previous_volume_date|previous_volume_frequency|previous_issue|previous_issue_date|previous_issue_frequency|previous_number|previous_number_date|previous_number_frequency
    9917962373502836|Taupo Times|parent_grouping||numeric_before_alpha|title_code|TAT||ED1+TAB+QFS|200|0|STA||PER|Taupo Times|||||Taupo_Times||Fairfax updated title code|||0|0|0|||||||||

Columns used by ready-for-ingestion processing
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
``MMSID``
    The MMSID of the entity. This is use in the generated ``mets.xml`` file as the ``objectIdentifierValue``.

``title_parent``
    The title of the parent publication. A parent publication can have multiple supplement grouping publications. This
    title is used in the ``mets.xml`` file as the ``dc:title`` for a parent publication.

``title_mets``
    The title of of the publication (for a supplemental publication). This title is used in the ``mets.xml`` file as the
    ``dc:title``.

``processing_type``
    The processing type for this particular row.

``processing_rules``
    Additional processing rules for this row. These rules will override the default rules for the given processing type.

``processing_options``
    Additional processing options for this row. These options will override the default options for the given
    processing type.

``publication_key``
    Usually ``title_code`` or ``title_code_section_code``. However, current processing ignores these values and they
    may be removed in the future. TODO Remove them if they aren't used.

``title_code``
    The title code of the publication.

``supplement_title_codes``
    The title codes of any supplements that should be grouped with the publication despite having a different title code,
    e.g. the stuff files with the title code ``TVL`` should be grouped with the Sunday Star Times, which has the main
    title code of ``SUS``. The order they are listed is the order they will be sorted.

``supplement_with_sequence``
    When a supplement needs to be ordered by its sequence letter in a non-alphabetical order. Add the title code of the
    supplement followed by the sequence letter, e.g. ``TVLT+TVLS``. In this case for the supplement with the title code
    TVL, the files with sequence letter ``T`` will come before the files with the sequence letter ``S``. The supplement
    must also exist in the ``supplement_title_codes`` column, as above

``edition_discriminators``
    When a publication with one title code has two editions - e.g NZFarmer (2023 : southern edition) and
    NZFarmer (2023 : northern edition), the edition discriminator identifies which files belong just to that edition.
    These are used then used in the ``edition_codes`` to build the complete issue. In the case of NZFarmer, both editions
    use all the files with a ``ZN`` code, but the files with ``ST`` are specific to the southern edition, and ``NT`` to
    the northern edition

``edition_codes``
    Has two uses
    1. The combination of codes that will build a full issue for publication multiple editions. The ``edition_discriminators``
    followed by a ``+`` followed by any other ``edition_codes`` that should be included. e.g ``ST+ZN``
    2. Used when there may be new editions of a title which replace the files from the previous edition. For example
    If there is ``edition_codes`` has a value of ``B+C`` any files with the edition code ``B`` will replace any with the
    edition code ``B``, and any with the edition code ``C`` will replace both ``B`` and ``C``

``section_codes``
    The section codes that will be included in the publication. For different editions, only the first edition code
    needs to be included. Section codes are included in the publication in the same order they are given here. They are
    separated by the ``+`` sign, as in ``ED1+TAB+YWE``.


``Access``
    The publication access. This is usually ``200``.

``Magazine``
    A ``1`` indicates a magazine. A ``0`` usually indicates newspaper. Magazines and newspapers have different material
    flows and are separated into ``magazine`` and ``newspaper`` subfolders for that reason.

``ignore_sequence``
    A list of letters separated by the ``+`` sign. These are ``sequence_letters`` which should be ignored, and not
    included in the SIP. It may be a letter used to indicate that a page has been replaced by a new version. E.g.
    if ``N`` is in the ``ingnore_sequencqe`` then ``SUS-ED1-ZN-20230707-N002.pdf`` should be ignored.

``sequence_letters``
    A list of letters separated by the ``+`` sign that indicate the ordering of the sequence letters in a
    publication if a non-alphabetical ordering is desired. This field can also be used in a
    ``supplement_grouping`` to determine which sequence is the supplement. See the *supplement_grouping* section of the
    :doc:`script-runner-guide` for more
    details of how this field would be used. Note that sequence_letters has not been implemented in the codebase and
    does not exist as a column in the default spreadsheet.

Some of the other columns (not used in the code)
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
``ingest_status``
    ``pilot=PIL, standard=STA, standard_complex=STC, supplement=SUP, oneoff=ONE, technical_issues=TEC``.

``entity_type``
    ``periodic=PER, serial_supplement=SS, oneoff=ONE``.


Ready-for-ingestion processing
==============================
Most of the librarian's work involves interacting with the output of the ready-for-ingestion processing.

Ready-for-ingestion folder structure
------------------------------------
Note that Rosetta ingestion requires that the ``content`` folder's parent parent be the folder used in Rosetta's
Submission Format. In this case that folder is either ``magazine`` or ``newspaper``, with the folder for an individual
publication's ingestion directly underneath::

    <targetFolder>/<magazine|newspaper>/<date-in-yyyyMMdd>_<tile_code>_<processing_type>_<optional-edition>__<full-name-of-publication>/content/streams/{files for that title_code/section_code}

For example, the folder ```newspaper/20180905_DOM_parent_grouping_ED1__The_Dominion_Post`` has the following values::

    newspaper: this folder is for *newspaper* material flows (as opposed to *magazine*).
    date: the publication date, in this case 20180905.
    title_code: DOM
    processing_type: parent_grouping
    edition: ED1 (for some publications there are more than 1 edition).
    full-name-of-publication: The_Dominion_Post (note that spaces are replaced with underscores)


The ``mets.xml`` file is placed in the ``content`` folder.

Ready-for-ingestion processing types
------------------------------------
Each spreadsheet row is associated with a specific processing type. These processing types are outlined in the
:doc:`script-runner-guide`.

Ready-for-ingestion processing rules and options
------------------------------------------------
See the :doc:`script-runner-guide` for details on how processing rules and options affect how files get
processed.

Parameters-and-state file
-------------------------
With every processing type and title code combination folder, there is a ``parameters-and-state`` file that is created.
This file summarises the processing that has taken place for that folder. The file name is of the format::

    <date-in-yyyy-MM-dd-format>_<title_code>_<processing_type>_<optional-edition_code>_parameters-and-state_<timestamp>.txt

For example::

    2015-07-02_DPT_parent_grouping_ED1_parameters-and-state_2019-06-21_07-42-04-011.txt

This file contains the following information:
    - The parameters that were used to process the folder, including processing type, rules and options.
    - The spreadsheet row values that were used.
    - A list of exceptions and their detail (if there are exceptions).
    - A list of files:
        - sipFiles - the files included in the SIP.
        - valid files - a list of all valid files.
        - invalid files - a list of all invalid files.
        - ignored files - a list of all ignored files.
        - unrecognisedFiles - a list of all unrecognised files.


Processing log file
-------------------
With every processing type and title combination folder, there is a ``processing-log`` file that is created. This file
contains the detailed logs that the processing code produced while processing the given title code folder. The
information in the log can be useful for digging into deeper reasons why processing failed (or succeeded). The file
name is of the format::

    <date-in-yyyy-MM-dd-format>_<title_code>_<processing_type>_<optional-edition_code>_processing-log_<timestamp>.log

For example::

    2015-07-02_DPT_parent_grouping_ED1_processing-log_2019-06-21_07-41-02-769.log

Ready-for-ingestion For-review
==============================
If a file or set of files is unable to be processed for some reason, it will be placed in the *For-review* folder. There
is no processor that operates on the *For-review* stage. Processors that output to the *For-review* folder use the
parameter ``forReviewFolder`` to set the location of the *For-review* folder.

For-review exception types
--------------------------
For ready-for-ingestion processing, for-review is subdivided into specific error type directories, such as
``has-zero-length-files``, ``has-incomprehensible-files``, ``no-matching-definition``, ``invalid-filenames``,
``invalid-pdfs``, ``duplicate-files``, ``multiple-definitions``, ``manual-processing``.

Some exception-types simply need a librarian to verify that the exception is acceptable. Other exceptions will require
some manual changes so that the files can be ingested properly into Rosetta.

It is possible in some of the processing that there are multiple matches for a given set of files where one match gets
processed correctly and another match fails and shows up in the for-review folder. An example would be ``WKTGDN`` which
will get processed as the processing type ``parent_grouping`` with the title code ``WKT`` (and will fail because it
doesn't have the necessary section codes) and also as the processing type ``parent_grouping_with_edition`` with the
title code ``WKT`` and edition ``GDN``, which will succeed. Although it's possible to change the code to ensure the
``parent_grouping`` does not match, the code may become too complicated. Given that the ``GDN`` edition is quite rare,
it's better to leave the odd exception like this in place.

TODO Perhaps we have a section to track exceptions like these.

``has-zero-length-files``
    There is at least one file that is of zero-length. If the ``zero_length_pdf_replaced_with_page_unavailable`` has
    been set, this zero-length file will have been replaced by a *page unavailable* file.

``has-incomprehensible-files``
    One or more files has a naming format that is not understood by the processing software. The file will need to be
    renamed to conform the the expected naming convention. (TODO The software currently does not use this exception
    type).

``no-matching-definition``
    There is no matching definition in the spreadsheet for the given processing type and title code.

``invalid-filenames``
    There are files with invalid PDF filenames. The file or files will need to be renamed to conform the the expected
    naming convention.

``invalid-pdfs``
    The PDF files when checked with the PDF validator (currently Jhove) finds PDF file or files to be invalid. Note that
    this does not necessarily mean the PDF will not render. The librarian needs to validate that the PDF is in fact invalid and if so, perform some corrective actions
    so the content can be ingested into Rosetta. That corrective action could involve replacing the invalid PDF with a
    *page unavailable* PDF.

``duplicate-files``
    One or more files have the same name. A PDF file is considered to have the same name when it has the same
    ``title_code``, ``section_code``, ``date``, ``sequence_letter`` and ``sequence_number``. Usually this happens when
    there are multiple files with these same attributes, but with different qualifiers, such as
    ``DOMED1-20190603-004.pdf`` and ``DOMED1-20190603-004-new version.pdf``. In this case, the librarian should delete
    the older versions and keep the version that is most recent. This may involve re-processing the given publication
    folder again.

``multiple-definitions``
    There is more than one definition in the spreadsheet that matches the processing type and title code. In these
    cases, the spreadsheet needs correction, as the processing code expects a single definition for any processing
    type and title code combination.

``manual-processing``
    The spreadsheet row for the processing type and title code combination has indicated manual processing. There is
    usually a specific reason that manual processing is specified. For example, a title code might apply to two
    different publication MMSIDs (older publications might have one MMSID, and publications after a certain date
    might have a different MMSID), so the ``mets.xml`` needs manual editing to ensure the MMSID is the correct one.

For-review folder structure
---------------------------
The file structure under these specific error types follows the same structure as the
`Ready-for-ingestion folder structure`_ mentioned above.

Ignored, unrecognised and invalid file locations
------------------------------------------------
When the processing rules ``handle_ignored``, ``handle_unrecognised`` and/or ``handle_invalid`` are used, those
specific files will show up in the following subfolders::

    <forReviewFolder>/[IGNORED|UNRECOGNIZED|INVALID]/<date-in-yyyyMMdd>/<TitleCode>/{files for that titleCode}


Ready-for-ingestion for-review workflow
---------------------------------------
The various for-review exceptions are dealt with in different ways, depending on the exception. Sometimes it involves
renaming files, sometimes it involves deleting files, sometimes it involves editing the ``mets.xml`` file. The
``parameters-and-state`` file and even the ``processing-log`` file can all help in
determining what actions the librarian needs to take to prepare the given content for ingestion.

Once the content has been corrected the files can either be reprocessed or the processed files can be moved to a
location that the Rosetta ingestion material flow can ingest them from.
