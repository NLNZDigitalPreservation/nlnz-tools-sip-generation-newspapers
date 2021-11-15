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

-   **Changes since 1.0.0** - Changes since the last official release *1.0.0*.

-   **1.0.0** - Release 1.0.0.

-   **1.0.0-SNAPSHOT** - Current 1.0.0 development.


Changes since 1.0.0
===================

This is a placeholder for changes since the official *2.0.1* release. Please add notes here for changes and fixes as
they are released into the master branch.


1.0.0
=====

TODO Major features of this release


1.0.0-SNAPSHOT
==============

Created from NLNZ Tools SIP Generation Fairfax. Added the concept of a NewspaperType.

NewspaperTypes replace the hardcoded filename patterns etc for Fairfax and can be configured within
`resources/nz/govt/natlib/tools/sip/generation/newspapers/newspaper-types.json`

Each NewspaperType requires a corresponding NewspaperSpreadsheet which lists each publication for that type
(the same as the FairfaxSpreadsheet).

newspaperType is a required parameter when running the processing.
