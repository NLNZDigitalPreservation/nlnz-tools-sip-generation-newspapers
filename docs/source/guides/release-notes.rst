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

-   **1.0.1-SNAPSHOT** - Current 1.0.1 development.

-   **1.0.0-SNAPSHOT**


1.0.1-SNAPSHOT
==============

-   Adds a Clean Up FTP Processor. This allows the user to delete files in a batch from an FTP source folder, within a
    date range.

-   Removes starting and ending dates being set be default by all processors, they now default to null. This is an extra
    safety check to avoid deleting files accidentally with the new CleanUpFTPProcessor.

1.0.0-SNAPSHOT
==============

Created from the code for NLNZ Tools SIP Generation Fairfax. Added the concept of a NewspaperType.

NewspaperTypes replaces the hardcoded filename patterns etc for Fairfax and can be configured within
`resources/nz/govt/natlib/tools/sip/generation/newspapers/newspaper-types.json`

Each NewspaperType requires a corresponding NewspaperSpreadsheet which lists each publication for that type
(the same as the FairfaxSpreadsheet).

newspaperType is a required parameter when running the processing.


