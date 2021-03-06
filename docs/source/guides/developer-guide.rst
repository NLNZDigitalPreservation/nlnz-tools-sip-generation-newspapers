===============
Developer Guide
===============


Introduction
============

This guide, designed for a NLNZ Tools SIP Generation Newspapers developer and contributor, covers how to develop and
contribute to the NLNZ Tools SIP Generation Newspapers. The source for both code and documentation can be found at:
https://github.com/NLNZDigitalPreservation/nlnz-tools-sip-generation-newspapers/

Contents of this document
-------------------------

Following this introduction, the NLNZ Tools SIP Generation Newspapers includes the following sections:

-   **Contributing** - Covers how to contribute to the project.

-   **Why Groovy?** - Discusses the choice of Groovy as a development language.

-   **Basic packages and classes**  - Covers the packages and classes in the project.

-   **Building** - Covers building the nlnz-tools-sip-generation-newspapers jars from source.

-   **Developer guidelines** - Covers coding practice and development workflow.

-   **Future milestones** - Covers plans for future development.


Contributing
============

This describes how to contribute to the NLNZ Tools SIP Generation Newspapers project. General contribution guidelines
follow the guidelines outlined in *Contributing* section of the *Developer Guide* of the
*National Library of New Zealand Developer Guidelines* for a description of the build commands used for this project.
These guidelines can be found at https://nlnz-developer-guidelines.readthedocs.io .

Source Code Repository
----------------------

Source code for the NLNZ Tools SIP Generation Newspapers is stored in github at:
https://github.com/NLNZDigitalPreservation/nlnz-tools-sip-generation-newspapers/
Contributors to the codebase will require a github account.

Major Contributors
------------------

Major contributors to NLNZ Tools SIP Generation Newspapers are NLNZ (The National Library of New Zealand)
(https://natlib.govt.nz/). This institution currently drive most development. All contributors are welcome. Making your
interest in NLNZ Tools SIP Generation Newspapers known can help to ensure that the tools meets your needs.

Contributors
------------
See individual git commits to see who contributors are.


Why Groovy?
===========

At the National Library of New Zealand, there are some technically adept staff who use Python scripting to accomplish
some of their tasks. Python is a useful language, but it had one significant deficiency with regards to the Fairfax
ingestion problem: ExLibris (the software company behind the Rosetta archiving system) provides an API for the
generation of SIP (``mets.xml``) files. This API can also be used to interact directly with the Rosetta archiving
system. The API itself is a Java API, which means that any solution that needs to use this API would need to be
JVM-based (this would include Java, Groovy, Scala and so on).

While it is possible to duplicate the same functionality in Python, the onus then becomes on the developer to maintain
that functionality even as the ExLibris API changes. NLNZ regularly updates their version of Rosetta, and this would
mean that any Python software duplicating that API would need to have extra overhead to keep up and ensure compatibility
with the ExLibris functionality.

Advantages of groovy
--------------------
Groovy, as a JVM language, was chosen for a few significant reasons:

- It is a scripting language, with many language features designed for scripting.
- It has code features that minimise boilerplate code (and thus make the code easier to read and quicker to write)
- It can take advantage of all the Java code libraries that exist.
- The codebase at DIA and their developers are often Java-based, especially in the Knowledge Services area.

Disadvantages of groovy
-----------------------
- New features in Java 9, 10 and 11 (such as Lambdas) provide similar functionality to that of Groovy, which removes
  some of the ease of programming advantages of Groovy.
- Some features in newer versions of Java are not supported under Groovy. For example, the try-with-resources construct
  is not supported under Groovy -- although there are alternatives that provide the same functionality.
- Some Java developers may not be comfortable coding in a language that doesn't quite look like Java.


Basic packages and classes
==========================

TODO a diagram illustrates the interactions between key components.


Building
========

Requirements
------------

Build requirements
~~~~~~~~~~~~~~~~~~
Building the NLNZ Tools SIP Generation Newspapers from source requires the following:

-   Java 11 JDK or above (64bit recommended). Current development assumes the use of OpenJDK.

-   Gradle 5.2.1 or later.

-   Groovy 2.5.4 or later.

-   Git (required to clone the project source from Github).

-   Access to maven central either directly or through a proxy.

As the artifact targets are Java-based, it should be possible to build the artifacts on either Linux, Solaris or Windows
targets.

Dependencies
~~~~~~~~~~~~
Most of this project's dependencies can be pulled from Maven Central, but this project also depends on some other
projects and those projects need to be built before this project can compile and build. The project dependencies are,
in order:

-   *nlnz-m11n-tools-gradle*. The nlnz-m11-tools project can be found at
    https://github.com/NLNZDigitalPreservation/nlnz-m11n-tools-gradle . Follow the instruction to build this project
    and install the artifacts to a your maven repository.
-   *nlnz-m11n-tools-automation*. The nlnz-m11n-tools-automation project can be found at
    https://github.com/NLNZDigitalPreservation/nlnz-m11n-tools-automation . Follow the instruction to build this project
    and install the artifacts to a your maven repository.
-   *rosetta-dps-sdk-projects-maven-lib*. This project provides a pre-built jar and ``pom.xml``. This project can be
    found at https://github.com/NLNZDigitalPreservation/rosetta-dps-sdk-projects-maven-lib . Follow the instructions to
    install the necessary artifacts to your maven repository.
-   *nlnz-tools-sip-generation*. The nlnz-tools-sip-generation project can be found at
    https://github.com/NLNZDigitalPreservation/nlnz-tools-sip-generation . Follow the instruction to build this project
    and install the artifacts to a your maven repository.

Development platforms
~~~~~~~~~~~~~~~~~~~~~
The following platforms have been used during the development of the NLNZ Tools Sip Generation Newspapers:

-  Ubuntu GNU/Linux 18.04 LTS and later

Installation
------------
The artifacts are built using gradle and will deploy to a maven repository when various gradle publishing options are
used.

Build commands
--------------
See the *Build commands for Gradle-based projects* section of the *Java Development Guide* of the
*National Library of New Zealand Developer Guidelines* for a description of the build commands used for this project.
These guidelines can be found at https://nlnz-developer-guidelines.readthedocs.io .

The primary build command for this project is::

    ./gradlew clean build publishToMavenLocal

Versioning
----------
See the ``build.gradle`` file for the current jar version that will be generated.

A detailed versioning discussion is found in the *Build commands for Gradle-based projects* section of the
*Java Development Guide* of the *National Library of New Zealand Developer Guidelines*. These guidelines can be found at
https://nlnz-developer-guidelines.readthedocs.io . See the section *Git Development Guide*.


Developer Guidelines
====================

See the *National Library of New Zealand Developer Guidelines* found at:
https://nlnz-developer-guidelines.readthedocs.io .


Future milestones
=================

This sections discusses plans for future development.

Iteration 1: Understanding the problem
--------------------------------------
The first iteration of any solution becomes an expression of understanding the problem. The subsequent iteration is
to provide a better solution. Unfortunately, most development often stops at the first iteration.

Iteration 2a: Choosing a different approach
-------------------------------------------
The current codebase started with the assumption of a single match for a group of files. When the problem domain
expanded to multiple matches for the same set of files the manner of processing became much more complicated.

A better approach might be akin to a filter chain, where a set of files is passed from one potential processor to
another. The filter may process the files and then pass them on, or process them and stop the chain. One issue with
this approach is that one filter's choice of processing may be affected by the choices of another filter.

Whatever the approach, the end results needs to be a simpler, easier-to-understand codebase. The current codebase
is becoming too complicated to reliably maintain. The use of scenario tests to ensure that certain use cases are
processed correctly is integral to ensuring that the codebase remains functional despite its complexity.

Iteration 2b: Incorporating better technologies
-----------------------------------------------
Future development will likely focus on solving bulk ingestion of other digital media. That other use case would
provide a better understanding of commonalities of bulk ingestion and provide insight into how to develop a generic
approach with specific applications for different publication to ingestion pipelines.

Some useful technologies that might enable a better solution:

-   The use of Spring Boot to provide a runtime jar with externalizable configuration.
    See https://spring.io/projects/spring-boot .
-   The use of Spring Batch to handle much of the logistics of batch processing. See
    https://spring.io/projects/spring-batch .
-   Using stream processing and other Java 8 features as an approach to make the code more flexible and usable. There's
    some excellent Youtube video that demonstrates this approach. See some excellent videos by Victor Rentea:

        - Clean Code with Java8 4 years later (V. Rentea) https://www.youtube.com/watch?v=-WInMyeAqTE
        - The Art of Clean Code by Victor Rentea https://www.youtube.com/watch?v=AeWbJ5LIFNg
        - The Art Of Clean Code by Victor Rentea https://www.youtube.com/watch?v=J4OIo4T7I_E

Wrapping some functionality in a user interface
-----------------------------------------------
The core code that turns the input stream of files into a output Rosetta-ingestable structure can be utilised by a bulk
processing engine. It can also be exposed as an API for use in a user interface. As the tools get more use, we can
identify user workflows that can be automated and exposed and managed with a user interface.

One choice for writing that user interface and exposing functionality through a REST API is Spring Boot and a
Spring-based web framework, such as Spring MVC and Spring Web Flow (https://projects.spring.io/spring-webflow/ ).

Consider also using hypermedia or HATEOAS (Hypertext as the Engine of Application State) as a means of exposing
navigations in a REST API instead of strict API versioning. Some discussion of HATEOAS:

-   An Intro to Spring HATEOAS (https://www.baeldung.com/spring-hateoas-tutorial )
-   Spring HATEOAS (https://spring.io/projects/spring-hateoas )

Integrating with a workflow
---------------------------
A monolithic user interface may not be the best approach. The process of moving Fairfax files through different stages
might fit better into some kind of workflow software. In that case you may still want a server, but the REST endpoints
provided by the engine would be integrated with some kind of workflow process. There might be UI snippets for specific
part of that process.

For example, the AWS Simple Workflow Service (SWF) (https://aws.amazon.com/swf/ ) is one way of integrating the
Fairfax ingestion workflow into other systems, including human systems.
