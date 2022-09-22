====================
Overview and History
====================

Additional TODO
===============

-   Placeholder for additional TODO items.


Introduction
============

This guide, designed for non-technical users, provides a background and history of National Library of New Zealand
(NLNZ) Tools SIP Generation Newspapers, and National Library of New Zealand
(NLNZ) Tools SIP Generation Fairfax, which is what NLNZ Tools Sip Generation Newspapers is based on.

Contents of this document
-------------------------

Following this introduction, this Overview and History Guide includes the following sections:

-   **Overview** - Covers what the NLNZ Tools SIP Generation Newspapers is and what it is not.

-   **History** - Covers the history of the tool from its inception to today.

-   **Documentation corrections** - Covers a process to submit corrections to the documentation.

-   **License**  - Covers the license used.

-   **Release history** - Covers significant changes made in each release.


Overview
========

NLNZ Tools SIP Generation Newspapers is designed to prepare SIPs for ingestion into the Rosetta archiving system.
It automates the process of generating SIPs from the newspaper files uploaded to the NLNZ legal deposit FTP folders.
It is an adaptation of NLNZ Tools Sip Generation Fairfax, which dealt only with Fairfax files. This adapted code is
configurable for multiple newspaper types.

What it is *NOT*
----------------

-   It is *NOT* specific to any other ingestion besides the newspaper publications it is configured for.


History
=======

The National Library of New Zealand has a legal mandate, and a social responsibility, to preserve New Zealand's social
and cultural history, be it in the form of books, newspapers and photographs, or of websites, blogs and videos.
Increasing amounts of New Zealand's documentary heritage is only available online. Users find this content valuable and
convenient, but its impermanence, lack of clear ownership, and dynamic nature pose significant challenges to any
institution that attempts to acquire and preserve it.

The original NLNZ Tools SIP Generation Fairfax was a set of functionality for use in ingesting Fairfax-specific content.
Fairfax was the former name of the publication company now known as Stuff. This *fairfax* ingestion, which is a feed
from Fairfax of their digital print publications. The Fairfax code did the custom processing involved with processing
the Fairfax files.

Since the original Fairfax code was written, more digital newspaper print publications have required processing, each
with specific filename patterns and processing requirements.
NLNZ Tools SIP Generation Newspapers is adapted from NLNZ Tools SIP Generation Fairfax. Its main difference is that it
can be configured to process different newspaper file types from publishers other than Fairfax, and new publications
can be added to it in the
``resources/nz/govt/natlib/tools/sip/generation/newspapers/newspaper-types.json`` config file.
Digital newspaper files are fed from various publishers which need to need be ingested into Rosetta.
This code does the custom processing involved with processing these files.

The current configured newspaper types are:

- Allied Press (alliedPress)
- Are Media (areMedia)
- Stuff (stuff) - formerly Fairfax
- Wairarapa Times Age (WMMA)
- Westport News (wptNews)

See the Newspaper type configuration section of the :doc:`Script Runner Guide<script-runner-guide>` for more information
on configuring newspaper types.


Project objectives
------------------

-   Meets the needs of the National Library of New Zealand
-   Possibly provide helpful functionality or example processing to other users and institutions.

The tool is open-source software and is freely available for the benefit of the international archiving community.


Documentation corrections
=========================
This documentation is generated from ``.rst`` files in the ``docs`` folder of the codebase, found at
https://github.com/NLNZDigitalPreservation/nlnz-tools-sip-generation-newspapers .

There are two approaches to making documentation corrections. Both approaches require a Github account.

Edit the files directly in the repository
-----------------------------------------
Github describes the process of editing files directly in the repository here:
https://help.github.com/en/articles/editing-files-in-your-repository . Follow this process to edit the problematic
``.rst`` files directly.

Annotate a PDF with changes and suggestions
-------------------------------------------
Some readers might not be willing to edit those files directly and submit changes via Github. Another way to document
desired changes in the documentation is to download the documentation as a ``pdf`` from its location on readthedocs,
namely, https://nlnz-tools-sip-generation-newspapers.readthedocs.io , annotate that pdf with the desired changes, and then
submit that annotated pdf in a github issue. Github describes the process of creating an issue here:
https://help.github.com/en/articles/creating-an-issue .



License
=======

All contributions to the NLNZ Tools SIP Generation Newspapers must be under the MIT (2019) License, which can be found at:
https://mit-license.org/

The code is open-source software and is freely available for the benefit of the international archiving community.

See the *Contributing* section of the :doc:`Developer Guide <developer-guide>` for more details.


Copyright
=========

|copyright| 2018—2022 National Library of New Zealand. All rights reserved. MIT license.

Contributors
============

See git commits to see who contributors are.


Release history
===============

See :doc:`Release Notes <release-notes>` for release notes on specific versions.


.. |copyright|   unicode:: U+000A9 .. COPYRIGHT SIGN
