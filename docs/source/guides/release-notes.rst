=============
Release Notes
=============


Introduction
============

This guide, designed for a NLNZ Tools SIP Generation Newspapers developer, covers release notes from `1.0.0-SNAPSHOT`.
Versions are in reverse chronological order, with the most recent version first. While the
*NLNZ Tools SIP Generation Newspapers Developer Guide* and *NLNZ Tools SIP Generation Newspapers User Guide* are accurate for
the current release, the *Release Notes* can give some idea of how things have changed since the last major release.

Contents of this document
-------------------------

Following this introduction, the Release Notes includes the following sections:

-   **1.1.2**

-   **1.1.1**

-   **1.0.1-SNAPSHOT**

-   **1.0.0-SNAPSHOT**

1.1.2
==============

- Adds the Processing Type 'supplement_with_date_and_issue' and the Processing Option 'dc_issued_field'.
These are used to add an 'issued' field to the mets. Also adds a 'supplementPreviousIssuesFile' to the Processor
Configuration. This can be used to calculate the latest issue number for the 'issued' field.
These changes are currently only used for the Forever Project supplement (FPS)

1.1.1
==============

- Adds the NewspaperType 'stuff'. 'Stuff' was formerly known as Fairfax. This means the
nlnz-tools-sip-generation-fairfax code has been retired, and all processing is now done by this codebase.
The Fairfax code is still available for reference on github.

1.0.1-SNAPSHOT
==============

-   Adds a Clean Up FTP Processor. This allows the user to delete files in a batch from an FTP source folder, within a
    date range.

-   Removes starting and ending dates being set be default by all processors, they now default to null. This is an extra
    safety check to avoid deleting files accidentally with the new CleanUpFTPProcessor.

1.0.0-SNAPSHOT
==============

Created from the code for NLNZ Tools SIP Generation Fairfax. Added the concept of a NewspaperType.

NewspaperTypes replace the hardcoded filename patterns etc for Fairfax and can be configured within
`resources/nz/govt/natlib/tools/sip/generation/newspapers/newspaper-types.json`

Each NewspaperType requires a corresponding NewspaperSpreadsheet which lists each publication for that type
(the same as the FairfaxSpreadsheet).

newspaperType is a required parameter when running the processing.


