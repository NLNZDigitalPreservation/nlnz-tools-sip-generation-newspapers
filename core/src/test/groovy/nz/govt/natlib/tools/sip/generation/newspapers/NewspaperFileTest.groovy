package nz.govt.natlib.tools.sip.generation.newspapers


import java.nio.file.Path
import java.time.LocalDate

import static org.hamcrest.core.Is.is
import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertNotNull
import static org.junit.Assert.assertThat
import static org.junit.Assert.assertTrue
import static org.mockito.Mockito.when

import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

/**
 * Tests {@link NewspaperFile}.
 */
@RunWith(MockitoJUnitRunner.class)
class NewspaperFileTest {

    @Mock
    Path mockFile, mockFile1, mockFile2, mockFile3

    @Test
    void correctlyMatchesFilenamesUsingDifferentRegexPatterns() {
        checkRegexFilenamePatternMatches("abcD-20180425-A1.pDF", "WTAA", true, true, true)
        checkRegexFilenamePatternMatches("abcd-20180425-A01.pDF", "WTAA", true, true, true)
        checkRegexFilenamePatternMatches("abcd-20180425-A001.PDF", "WTAA", true, true, true)
        checkRegexFilenamePatternMatches("abcD-20180425-A0001.PDF", "WTAA", true, true, true)
        checkRegexFilenamePatternMatches("abcD-20180425-001.PDF", "WTAA", true, true, true)
        // NOTE: This does match, but the '1' at the end is included in the qualifier.
        checkRegexFilenamePatternMatches("abcD-20180425-A0001.pdf", "WTAA", true, true, true)

        checkRegexFilenamePatternMatches("abcD-nodate-A001.PDF", "WTAA", false, false, false)
        checkRegexFilenamePatternMatches("abcDEFGH-20180425-A001.PDF", "WTAA", false, false, false)
        checkRegexFilenamePatternMatches("abcD25Apr18A001", "WTAA", false, false, false)
        checkRegexFilenamePatternMatches("abcD25Apr18A001.pdf2", "WTAA", false, false, false)
        checkRegexFilenamePatternMatches("ab25Apr18A001.pdf", "WTAA", false, false, false)
        checkRegexFilenamePatternMatches("", "WTAA", false, false, false)
    }

    void checkRegexFilenamePatternMatches(String valueToCheck, String publication, boolean matchesWithGroupingRegex,
                                          boolean matchesWithDateSequencePattern, boolean matchesWithDateOnlyPattern) {
        NewspaperType newspaperType = new NewspaperType(publication)
        if (matchesWithGroupingRegex) {
            assertTrue("value=${valueToCheck} matches pattern=${newspaperType.PDF_FILE_WITH_TITLE_SECTION_DATE_SEQUENCE_GROUPING_PATTERN}",
                    valueToCheck ==~ /${newspaperType.PDF_FILE_WITH_TITLE_SECTION_DATE_SEQUENCE_GROUPING_PATTERN}/)
        } else {
            assertFalse("value=${valueToCheck} does NOT match pattern=${newspaperType.PDF_FILE_WITH_TITLE_SECTION_DATE_SEQUENCE_GROUPING_PATTERN}",
                    valueToCheck ==~ /${newspaperType.PDF_FILE_WITH_TITLE_SECTION_DATE_SEQUENCE_GROUPING_PATTERN}/)
        }
        if (matchesWithDateSequencePattern) {
            assertTrue("value=${valueToCheck} matches pattern=${newspaperType.PDF_FILE_WITH_TITLE_SECTION_DATE_SEQUENCE_PATTERN}",
                    valueToCheck ==~ /${newspaperType.PDF_FILE_WITH_TITLE_SECTION_DATE_SEQUENCE_PATTERN}/)
        } else {
            assertFalse("value=${valueToCheck} does NOT match pattern=${newspaperType.PDF_FILE_WITH_TITLE_SECTION_DATE_SEQUENCE_PATTERN}",
                    valueToCheck ==~ /${newspaperType.PDF_FILE_WITH_TITLE_SECTION_DATE_SEQUENCE_PATTERN}/)
        }
        if (matchesWithDateOnlyPattern) {
            assertTrue("value=${valueToCheck} matches pattern=${newspaperType.PDF_FILE_WITH_TITLE_SECTION_DATE_PATTERN}",
                    valueToCheck ==~ /${newspaperType.PDF_FILE_WITH_TITLE_SECTION_DATE_PATTERN}/)
        } else {
            assertFalse("value=${valueToCheck} does NOT match pattern=${newspaperType.PDF_FILE_WITH_TITLE_SECTION_DATE_PATTERN}}",
                    valueToCheck ==~ /${newspaperType.PDF_FILE_WITH_TITLE_SECTION_DATE_PATTERN}}/)
        }
    }

    @Test
    void createsCorrectlyWithWTAALetterSequence() {
        String originalFilename = "WTAA-20181022-B024.pdf"
        when(mockFile.fileName).thenReturn(Path.of(originalFilename))
        NewspaperType newspaperType = new NewspaperType("WTAA")
        NewspaperFile testNewspaperFile = new NewspaperFile(mockFile, newspaperType)
        assertThat("Filename extracted correctly", testNewspaperFile.filename, is(originalFilename))
        assertThat("TitleCode parsed correctly", testNewspaperFile.titleCode, is("WTAA"))
        assertNotNull("Year extracted", testNewspaperFile.dateYear)
        assertThat("dateYear parsed correctly", testNewspaperFile.dateYear, is(2018))
        assertThat("dateMonthOfYear parsed correctly", testNewspaperFile.dateMonthOfYear, is(10))
        assertThat("dateDayOfMonth parsed correctly", testNewspaperFile.dateDayOfMonth, is(22))
        assertThat("Prefix parsed correctly", testNewspaperFile.sequenceLetter, is("B"))
        assertThat("Prefix parsed correctly", testNewspaperFile.sequenceNumberString, is("024"))
        assertThat("Prefix parsed correctly", testNewspaperFile.sequenceNumber, is(24))
        assertThat("Qualifier parsed correctly", testNewspaperFile.qualifier, is(""))
        assertTrue("NewspaperFile is valid", testNewspaperFile.isValidName())
    }

    @Test
    void createsCorrectlyWithAPLetterSequence() {
        String originalFilename = "CLL_2023_02_01.pdf"
        when(mockFile.fileName).thenReturn(Path.of(originalFilename))
        NewspaperType newspaperType = new NewspaperType("alliedPress")
        NewspaperFile testNewspaperFile = new NewspaperFile(mockFile, newspaperType)
        assertThat("Filename extracted correctly", testNewspaperFile.filename, is(originalFilename))
        assertThat("TitleCode parsed correctly", testNewspaperFile.titleCode, is("CLL"))
        assertNotNull("Year extracted", testNewspaperFile.dateYear)
        assertThat("dateYear parsed correctly", testNewspaperFile.dateYear, is(new Integer(2023)))
        assertThat("dateMonthOfYear parsed correctly", testNewspaperFile.dateMonthOfYear, is(new Integer(02)))
        assertThat("dateDayOfMonth parsed correctly", testNewspaperFile.dateDayOfMonth, is(new Integer(01)))
        assertTrue("NewspaperFile is valid", testNewspaperFile.isValidName())
    }

    @Test
    void createsCorrectlyWithWptNewsLetterSequence() {
        String originalFilename = "211021WptNews01.pdf"
        when(mockFile.fileName).thenReturn(Path.of(originalFilename))
        NewspaperType newspaperType = new NewspaperType("wptNews")
        NewspaperFile testNewspaperFile = new NewspaperFile(mockFile, newspaperType)
        assertThat("Filename extracted correctly", testNewspaperFile.filename, is(originalFilename))
        assertThat("TitleCode parsed correctly", testNewspaperFile.titleCode, is("WptNews"))
        assertNotNull("Year extracted", testNewspaperFile.dateYear)
        assertThat("dateYear parsed correctly", testNewspaperFile.dateYear, is(new Integer(2021)))
        assertThat("dateMonthOfYear parsed correctly", testNewspaperFile.dateMonthOfYear, is(new Integer(10)))
        assertThat("dateDayOfMonth parsed correctly", testNewspaperFile.dateDayOfMonth, is(new Integer(21)))
        assertThat("Qualifier parsed correctly", testNewspaperFile.qualifier, is(""))
        assertTrue("NewspaperFile is valid", testNewspaperFile.isValidName())
    }

    @Test
    void createsCorrectlyWithAreMediaLetterSequence() {
        String originalFilename = "acpWZ0522p071_R1_FD3-040522.pdf"
        when(mockFile.fileName).thenReturn(Path.of(originalFilename))
        NewspaperType newspaperType = new NewspaperType("areMedia")
        NewspaperFile testNewspaperFile = new NewspaperFile(mockFile, newspaperType)
        assertThat("Filename extracted correctly", testNewspaperFile.filename, is(originalFilename))
        assertThat("TitleCode parsed correctly", testNewspaperFile.titleCode, is("WZ"))
        assertNotNull("Year extracted", testNewspaperFile.dateYear)
        assertThat("dateYear parsed correctly", testNewspaperFile.dateYear, is(2022))
        assertThat("dateMonthOfYear parsed correctly", testNewspaperFile.dateMonthOfYear, is(05))
        assertThat("dateDayOfMonth parsed correctly", testNewspaperFile.dateDayOfMonth, is(04))
        assertThat("Issue number extracted correctly", testNewspaperFile.issueNumber, is(05))
        assertThat("Issue year extracted correctly", testNewspaperFile.issueYear, is(2022))
        assertThat("Qualifier parsed correctly", testNewspaperFile.qualifier, is("FD3"))
        assertThat("Revision parsed correctly", testNewspaperFile.revision, is("R1"))
        assertTrue("NewspaperFile is valid", testNewspaperFile.isValidName())
    }

    @Test
    void createsCorrectlyWithNumberOnlySequence() {
        String originalFilename = "t20-20181022-024.pdf"
        when(mockFile.fileName).thenReturn(Path.of(originalFilename))

        NewspaperType newspaperType = new NewspaperType("WTAA")
        NewspaperFile testNewspaperFile = new NewspaperFile(mockFile, newspaperType)

        assertThat("filename extracted correctly", testNewspaperFile.filename, is(originalFilename))
        assertThat("TitleCode parsed correctly", testNewspaperFile.titleCode, is("t20"))
        assertThat("dateYear parsed correctly", testNewspaperFile.dateYear, is(new Integer(2018)))
        assertThat("dateMonthOfYear parsed correctly", testNewspaperFile.dateMonthOfYear, is(new Integer(10)))
        assertThat("dateDayOfMonth parsed correctly", testNewspaperFile.dateDayOfMonth, is(new Integer(22)))
        assertThat("sequenceLetter parsed correctly", testNewspaperFile.sequenceLetter, is(""))
        assertThat("sequenceNumber parsed correctly", testNewspaperFile.sequenceNumber, is(24))
        assertThat("Qualifier parsed correctly", testNewspaperFile.qualifier, is(""))
        assertTrue("NewspaperFile is valid", testNewspaperFile.isValidName())
    }

    @Test
    void createsCorrectlyWithLetterSequenceQualifier() {
        String originalFilename = "TST-20181022-B024a qualifier.pdf"
        when(mockFile.fileName).thenReturn(Path.of(originalFilename))

        NewspaperType newspaperType = new NewspaperType("WTAA")
        NewspaperFile testNewspaperFile = new NewspaperFile(mockFile, newspaperType)

        assertThat("Filename extracted correctly", testNewspaperFile.filename, is(originalFilename))
        assertThat("TitleCode parsed correctly", testNewspaperFile.titleCode, is("TST"))
        assertNotNull("Year extracted", testNewspaperFile.dateYear)
        assertThat("dateYear parsed correctly", testNewspaperFile.dateYear, is(new Integer(2018)))
        assertThat("dateMonthOfYear parsed correctly", testNewspaperFile.dateMonthOfYear, is(new Integer(10)))
        assertThat("dateDayOfMonth parsed correctly", testNewspaperFile.dateDayOfMonth, is(new Integer(22)))
        assertThat("Prefix parsed correctly", testNewspaperFile.sequenceLetter, is("B"))
        assertThat("Prefix parsed correctly", testNewspaperFile.sequenceNumberString, is("024"))
        assertThat("Prefix parsed correctly", testNewspaperFile.sequenceNumber, is(24))
        assertThat("Qualifier parsed correctly", testNewspaperFile.qualifier, is("a qualifier"))
        assertTrue("NewspaperFile is valid", testNewspaperFile.isValidName())
    }

    @Test
    void createsCorrectlyWithMixedCaseExtension() {
        String originalFilename = "TST-20181022-B024a qualifier.pDf"
        when(mockFile.fileName).thenReturn(Path.of(originalFilename))

        NewspaperType newspaperType = new NewspaperType("WTAA")
        NewspaperFile testNewspaperFile = new NewspaperFile(mockFile, newspaperType)

        assertThat("Filename extracted correctly", testNewspaperFile.filename, is(originalFilename))
        assertThat("TitleCode parsed correctly", testNewspaperFile.titleCode, is("TST"))
        assertNotNull("Year extracted", testNewspaperFile.dateYear)
        assertThat("dateYear parsed correctly", testNewspaperFile.dateYear, is(new Integer(2018)))
        assertThat("dateMonthOfYear parsed correctly", testNewspaperFile.dateMonthOfYear, is(new Integer(10)))
        assertThat("dateDayOfMonth parsed correctly", testNewspaperFile.dateDayOfMonth, is(new Integer(22)))
        assertThat("Prefix parsed correctly", testNewspaperFile.sequenceLetter, is("B"))
        assertThat("Prefix parsed correctly", testNewspaperFile.sequenceNumberString, is("024"))
        assertThat("Prefix parsed correctly", testNewspaperFile.sequenceNumber, is(24))
        assertThat("Qualifier parsed correctly", testNewspaperFile.qualifier, is("a qualifier"))
        assertTrue("NewspaperFile is valid", testNewspaperFile.isValidName())
    }

    @Test
    void createsCorrectlyWithUpperCaseExtension() {
        String originalFilename = "TST-20181022-B024a qualifier.PDF"
        when(mockFile.fileName).thenReturn(Path.of(originalFilename))

        NewspaperType newspaperType = new NewspaperType("WTAA")
        NewspaperFile testNewspaperFile = new NewspaperFile(mockFile, newspaperType)

        assertThat("Filename extracted correctly", testNewspaperFile.filename, is(originalFilename))
        assertThat("TitleCode parsed correctly", testNewspaperFile.titleCode, is("TST"))
        assertNotNull("Year extracted", testNewspaperFile.dateYear)
        assertThat("dateYear parsed correctly", testNewspaperFile.dateYear, is(new Integer(2018)))
        assertThat("dateMonthOfYear parsed correctly", testNewspaperFile.dateMonthOfYear, is(new Integer(10)))
        assertThat("dateDayOfMonth parsed correctly", testNewspaperFile.dateDayOfMonth, is(new Integer(22)))
        assertThat("Prefix parsed correctly", testNewspaperFile.sequenceLetter, is("B"))
        assertThat("Prefix parsed correctly", testNewspaperFile.sequenceNumberString, is("024"))
        assertThat("Prefix parsed correctly", testNewspaperFile.sequenceNumber, is(24))
        assertThat("Qualifier parsed correctly", testNewspaperFile.qualifier, is("a qualifier"))
        assertTrue("NewspaperFile is valid", testNewspaperFile.isValidName())
    }

    @Test
    void createsCorrectlyWithFourCharacterTitleCode() {
        String originalFilename = "JAZZ-20181022-B024a qualifier.pdf"
        when(mockFile.fileName).thenReturn(Path.of(originalFilename))

        NewspaperType newspaperType = new NewspaperType("WTAA")
        NewspaperFile testNewspaperFile = new NewspaperFile(mockFile, newspaperType)

        assertThat("Filename extracted correctly", testNewspaperFile.filename, is(originalFilename))
        assertThat("TitleCode parsed correctly", testNewspaperFile.titleCode, is("JAZZ"))
        assertNotNull("Year extracted", testNewspaperFile.dateYear)
        assertThat("dateYear parsed correctly", testNewspaperFile.dateYear, is(new Integer(2018)))
        assertThat("dateMonthOfYear parsed correctly", testNewspaperFile.dateMonthOfYear, is(new Integer(10)))
        assertThat("dateDayOfMonth parsed correctly", testNewspaperFile.dateDayOfMonth, is(new Integer(22)))
        assertThat("Prefix parsed correctly", testNewspaperFile.sequenceLetter, is("B"))
        assertThat("Prefix parsed correctly", testNewspaperFile.sequenceNumberString, is("024"))
        assertThat("Prefix parsed correctly", testNewspaperFile.sequenceNumber, is(24))
        assertThat("Qualifier parsed correctly", testNewspaperFile.qualifier, is("a qualifier"))
        assertTrue("NewspaperFile is valid", testNewspaperFile.isValidName())
    }

    @Test
    void createsCorrectlyWithNumberOnlySequenceQualifier() {
        String originalFilename = "t20-20181022-024crop.pdf"
        when(mockFile.fileName).thenReturn(Path.of(originalFilename))

        NewspaperType newspaperType = new NewspaperType("WTAA")
        NewspaperFile testNewspaperFile = new NewspaperFile(mockFile, newspaperType)

        assertThat("filename extracted correctly", testNewspaperFile.filename, is(originalFilename))
        assertThat("TitleCode parsed correctly", testNewspaperFile.titleCode, is("t20"))
        assertThat("dateYear parsed correctly", testNewspaperFile.dateYear, is(new Integer(2018)))
        assertThat("dateMonthOfYear parsed correctly", testNewspaperFile.dateMonthOfYear, is(new Integer(10)))
        assertThat("dateDayOfMonth parsed correctly", testNewspaperFile.dateDayOfMonth, is(new Integer(22)))
        assertThat("sequenceLetter parsed correctly", testNewspaperFile.sequenceLetter, is(""))
        assertThat("sequenceNumber parsed correctly", testNewspaperFile.sequenceNumber, is(24))
        assertThat("Qualifier parsed correctly", testNewspaperFile.qualifier, is("crop"))
        assertTrue("NewspaperFile is valid", testNewspaperFile.isValidName())
    }

    @Test
    void createsCorrectlyWithInvalidFilename() {
        String originalFilename = "abcde-20181022-024.pdf"
        when(mockFile.fileName).thenReturn(Path.of(originalFilename))

        NewspaperType newspaperType = new NewspaperType("WTAA")
        NewspaperFile testNewspaperFile = new NewspaperFile(mockFile, newspaperType)

        assertThat("filename extracted correctly", testNewspaperFile.filename, is(originalFilename))
        assertFalse("NewspaperFile is invalid", testNewspaperFile.isValidName())
    }

    @Test
    void matchesWhenSamePrefixAndDate() {
        String filename1 = "Mixy-20181022-023.pdf"
        String filename2 = "Mixy-20181022-001.pdf"
        when(mockFile1.fileName).thenReturn(Path.of(filename1))
        when(mockFile2.fileName).thenReturn(Path.of(filename2))

        NewspaperType newspaperType = new NewspaperType("WTAA")

        NewspaperFile newspaperFile1 = new NewspaperFile(mockFile1, newspaperType)
        NewspaperFile newspaperFile2 = new NewspaperFile(mockFile2, newspaperType)

        assertTrue("Same prefix and date in filename matches", newspaperFile1.matches(newspaperFile2))
        assertFalse("Same prefix and date but different sequence does not sequence match",
                newspaperFile1.matchesWithSequence(newspaperFile2))
    }

    @Test
    void matchesWhenSamePrefixDateAndSequence() {
        String filename1 = "Mixy-20181022-023.pdf"
        String filename2 = "Mixy-20181022-023withQualifier.pdf"
        when(mockFile1.fileName).thenReturn(Path.of(filename1))
        when(mockFile2.fileName).thenReturn(Path.of(filename2))

        NewspaperType newspaperType = new NewspaperType("WTAA")

        NewspaperFile newspaperFile1 = new NewspaperFile(mockFile1, newspaperType)
        NewspaperFile newspaperFile2 = new NewspaperFile(mockFile2, newspaperType)

        assertTrue("Same prefix and date in filename matches", newspaperFile1.matches(newspaperFile2))
        assertTrue("Matches with sequence", newspaperFile1.matchesWithSequence(newspaperFile2))
    }

    @Test
    void doesNotMatchWhenSamePrefixButDifferentDate() {
        String filename1 = "ABCD-20181022-023.pdf"
        String filename2 = "ABCD-20181023-023.pdf"
        when(mockFile1.fileName).thenReturn(Path.of(filename1))
        when(mockFile2.fileName).thenReturn(Path.of(filename2))

        NewspaperType newspaperType = new NewspaperType("WTAA")

        NewspaperFile newspaperFile1 = new NewspaperFile(mockFile1, newspaperType)
        NewspaperFile newspaperFile2 = new NewspaperFile(mockFile2, newspaperType)

        assertFalse("Same prefix but different dates does not match", newspaperFile1.matches(newspaperFile2))
    }

    @Test
    void doesNotMatchWhenDifferentPrefix() {
        String filename1 = "NAMA-20201022-023.pdf"
        String filename2 = "NAMB-20201022-023..pdf"
        when(mockFile1.fileName).thenReturn(Path.of(filename1))
        when(mockFile2.fileName).thenReturn(Path.of(filename2))

        NewspaperType newspaperType = new NewspaperType("WTAA")

        NewspaperFile newspaperFile1 = new NewspaperFile(mockFile1, newspaperType)
        NewspaperFile newspaperFile2 = new NewspaperFile(mockFile2, newspaperType)

        assertFalse("Different prefixes does not match", newspaperFile1.matches(newspaperFile2))
    }

    @Test
    void sortsCorrectlyWithSameDateButDifferentSequenceNumbers() {
        String filename1 = "NAMe-20181022-023.pdf"
        String filename2 = "NAMe-20181022-022.pdf"
        String filename3 = "NAMe-20181022-021.pdf"
        when(mockFile1.fileName).thenReturn(Path.of(filename1))
        when(mockFile2.fileName).thenReturn(Path.of(filename2))
        when(mockFile3.fileName).thenReturn(Path.of(filename3))

        NewspaperType newspaperType = new NewspaperType("WTAA")

        NewspaperFile newspaperFile1 = new NewspaperFile(mockFile1, newspaperType)
        NewspaperFile newspaperFile2 = new NewspaperFile(mockFile2, newspaperType)
        NewspaperFile newspaperFile3 = new NewspaperFile(mockFile3, newspaperType)

        assertEquals("Sorts correctly with same date but different sequence numbers",
                [newspaperFile1, newspaperFile2, newspaperFile3].sort(), [newspaperFile3, newspaperFile2, newspaperFile1])
    }

    @Test
    void sortsCorrectlyWithDifferentDates() {
        String filename1 = "NAMe-20181023-021.pdf"
        String filename2 = "NAMe-20181022-022.pdf"
        String filename3 = "NAMe-20181021-023.pdf"
        when(mockFile1.fileName).thenReturn(Path.of(filename1))
        when(mockFile2.fileName).thenReturn(Path.of(filename2))
        when(mockFile3.fileName).thenReturn(Path.of(filename3))

        NewspaperType newspaperType = new NewspaperType("WTAA")

        NewspaperFile newspaperFile1 = new NewspaperFile(mockFile1, newspaperType)
        NewspaperFile newspaperFile2 = new NewspaperFile(mockFile2, newspaperType)
        NewspaperFile newspaperFile3 = new NewspaperFile(mockFile3, newspaperType)

        assertEquals("Sorts correctly with same date but different sequence numbers",
                [newspaperFile1, newspaperFile2, newspaperFile3].sort(), [newspaperFile3, newspaperFile2, newspaperFile1])
    }

    @Test
    void sortsCorrectlyWithSameDateAndSequenceStringButDifferentNumbers() {
        String filename1 = "NAMe-20181022-C023.pdf"
        String filename2 = "NAMe-20181022-C022.pdf"
        String filename3 = "NAMe-20181022-C021.pdf"
        when(mockFile1.fileName).thenReturn(Path.of(filename1))
        when(mockFile2.fileName).thenReturn(Path.of(filename2))
        when(mockFile3.fileName).thenReturn(Path.of(filename3))

        NewspaperType newspaperType = new NewspaperType("WTAA")

        NewspaperFile newspaperFile1 = new NewspaperFile(mockFile1, newspaperType)
        NewspaperFile newspaperFile2 = new NewspaperFile(mockFile2, newspaperType)
        NewspaperFile newspaperFile3 = new NewspaperFile(mockFile3, newspaperType)

        assertEquals("Sorts correctly with same date and sequence string but different sequence numbers",
                [newspaperFile1, newspaperFile2, newspaperFile3].sort(), [newspaperFile3, newspaperFile2, newspaperFile1])
    }

    @Test
    void sortsCorrectlyWithSameDateAndDifferentSequenceStringButDifferentNumbers() {
        String filename1 = "NAMe-20181022-M023.pdf"
        String filename2 = "NAMe-20181022-C022.pdf"
        String filename3 = "NAMe-20181022-A021.pdf"
        when(mockFile1.fileName).thenReturn(Path.of(filename1))
        when(mockFile2.fileName).thenReturn(Path.of(filename2))
        when(mockFile3.fileName).thenReturn(Path.of(filename3))

        NewspaperType newspaperType = new NewspaperType("WTAA")

        NewspaperFile newspaperFile1 = new NewspaperFile(mockFile1, newspaperType)
        NewspaperFile newspaperFile2 = new NewspaperFile(mockFile2, newspaperType)
        NewspaperFile newspaperFile3 = new NewspaperFile(mockFile3, newspaperType)

        assertEquals("Sorts correctly with same date but different sequence numbers",
                [newspaperFile1, newspaperFile2, newspaperFile3].sort(), [newspaperFile3, newspaperFile2, newspaperFile1])
    }

    @Test
    void correctlyCreatesLocalDateFromFilename() {
        String filename1 = "NAMe-20180101-M023.pdf"
        String filename2 = "NAMe-20180630-A021.pdf"
        String filename3 = "NAMe-20181231-C022.pdf"
        when(mockFile1.fileName).thenReturn(Path.of(filename1))
        when(mockFile2.fileName).thenReturn(Path.of(filename2))
        when(mockFile3.fileName).thenReturn(Path.of(filename3))

        NewspaperType newspaperType = new NewspaperType("WTAA")

        NewspaperFile newspaperFile1 = new NewspaperFile(mockFile1, newspaperType)
        NewspaperFile newspaperFile2 = new NewspaperFile(mockFile2, newspaperType)
        NewspaperFile newspaperFile3 = new NewspaperFile(mockFile3, newspaperType)

        LocalDate january12018 = LocalDate.of(2018, 1, 1)
        LocalDate june302018 = LocalDate.of(2018, 6, 30)
        LocalDate december312018 = LocalDate.of(2018, 12, 31)
        assertThat("Creates date correctly for ${january12018}", newspaperFile1.date, is(january12018))
        assertThat("Creates date correctly for ${june302018}", newspaperFile2.date, is(june302018))
        assertThat("Creates date correctly for ${december312018}", newspaperFile3.date, is(december312018))
    }

    @Test
    void sortsCorrectlyUsingNumericBeforeAlpha() {
        Path file1 = Path.of("NAMe-20180131-M023.pdf")
        Path file2 = Path.of("NAMe-20180131-A01.pdf")
        Path file3 = Path.of("NAMe-20180131-A02.pdf")
        Path file4 = Path.of("NAMe-20180131-02.pdf")
        Path file5 = Path.of("NAMe-20180131-01.pdf")
        Path file6 = Path.of("NAMe-20180131-C1.pdf")
        Path file7 = Path.of("NAMe-20180131-C2.pdf")

        NewspaperType newspaperType = new NewspaperType("WTAA")

        NewspaperFile newspaperFile1 = new NewspaperFile(file1, newspaperType)
        NewspaperFile newspaperFile2 = new NewspaperFile(file2, newspaperType)
        NewspaperFile newspaperFile3 = new NewspaperFile(file3, newspaperType)
        NewspaperFile newspaperFile4 = new NewspaperFile(file4, newspaperType)
        NewspaperFile newspaperFile5 = new NewspaperFile(file5, newspaperType)
        NewspaperFile newspaperFile6 = new NewspaperFile(file6, newspaperType)
        NewspaperFile newspaperFile7 = new NewspaperFile(file7, newspaperType)

        List<NewspaperFile> unsorted = [newspaperFile1, newspaperFile2, newspaperFile3, newspaperFile4, newspaperFile5,
                                        newspaperFile6, newspaperFile7 ]
        List<NewspaperFile> expected = [newspaperFile5, newspaperFile4, newspaperFile2, newspaperFile3, newspaperFile6,
                                        newspaperFile7, newspaperFile1 ]
        List<NewspaperFile> sorted = NewspaperFile.sortNumericAndAlpha(unsorted, false)
        assertThat("Numeric comes before alpha for sorted=${sorted}", sorted, is(expected))
    }

    @Test
    void sortsCorrectlyUsingAlphaBeforeNumeric() {
        Path file1 = Path.of("NAMe-20180131-M023.pdf")
        Path file2 = Path.of("NAMe-20180131-A01.pdf")
        Path file3 = Path.of("NAMe-20180131-A02.pdf")
        Path file4 = Path.of("NAMe-20180131-02.pdf")
        Path file5 = Path.of("NAMe-20180131-01.pdf")
        Path file6 = Path.of("NAMe-20180131-C1.pdf")
        Path file7 = Path.of("NAMe-20180131-C2.pdf")

        NewspaperType newspaperType = new NewspaperType("WTAA")

        NewspaperFile newspaperFile1 = new NewspaperFile(file1, newspaperType)
        NewspaperFile newspaperFile2 = new NewspaperFile(file2, newspaperType)
        NewspaperFile newspaperFile3 = new NewspaperFile(file3, newspaperType)
        NewspaperFile newspaperFile4 = new NewspaperFile(file4, newspaperType)
        NewspaperFile newspaperFile5 = new NewspaperFile(file5, newspaperType)
        NewspaperFile newspaperFile6 = new NewspaperFile(file6, newspaperType)
        NewspaperFile newspaperFile7 = new NewspaperFile(file7, newspaperType)

        List<NewspaperFile> unsorted = [newspaperFile1, newspaperFile2, newspaperFile3, newspaperFile4, newspaperFile5,
                                        newspaperFile6, newspaperFile7 ]
        List<NewspaperFile> expected = [newspaperFile2, newspaperFile3, newspaperFile6, newspaperFile7, newspaperFile1,
                                        newspaperFile5, newspaperFile4 ]
        List<NewspaperFile> sorted = NewspaperFile.sortNumericAndAlpha(unsorted, true)
        assertThat("Alpha comes before numeric for sorted=${sorted}", sorted, is(expected))
    }

    @Test
    void correctlyDeterminesAHundredsSequenceStart() {
        Path file1 = Path.of("NAMe-20180131-100.pdf")
        Path file2 = Path.of("NAMe-20180131-101.pdf")
        Path file3 = Path.of("NAMe-20180131-300.pdf")
        Path file4 = Path.of("NAMe-20180131-399.pdf")
        Path file5 = Path.of("NAMe-20180131-400.pdf")
        Path file6 = Path.of("NAMe-20180131-401.pdf")
        Path file7 = Path.of("NAMe-20180131-402.pdf")
        Path file8 = Path.of("NAMe-20180131-501.pdf")
        Path file9 = Path.of("NAMe-20180131-502.pdf")

        NewspaperType newspaperType = new NewspaperType("WTAA")

        NewspaperFile newspaperFile1 = new NewspaperFile(file1, newspaperType)
        NewspaperFile newspaperFile2 = new NewspaperFile(file2, newspaperType)
        NewspaperFile newspaperFile3 = new NewspaperFile(file3, newspaperType)
        NewspaperFile newspaperFile4 = new NewspaperFile(file4, newspaperType)
        NewspaperFile newspaperFile5 = new NewspaperFile(file5, newspaperType)
        NewspaperFile newspaperFile6 = new NewspaperFile(file6, newspaperType)
        NewspaperFile newspaperFile7 = new NewspaperFile(file7, newspaperType)
        NewspaperFile newspaperFile8 = new NewspaperFile(file8, newspaperType)
        NewspaperFile newspaperFile9 = new NewspaperFile(file9, newspaperType)

        assertTrue("file=${newspaperFile5.file} is isAHundredsSequenceStart", newspaperFile5.isAHundredsSequenceStart())
        assertTrue("file=${newspaperFile6.file} is isAHundredsSequenceStart", newspaperFile6.isAHundredsSequenceStart())
        assertTrue("file=${newspaperFile8.file} is isAHundredsSequenceStart", newspaperFile8.isAHundredsSequenceStart())

        assertFalse("file=${newspaperFile1.file} is NOT isAHundredsSequenceStart", newspaperFile1.isAHundredsSequenceStart())
        assertFalse("file=${newspaperFile2.file} is NOT isAHundredsSequenceStart", newspaperFile2.isAHundredsSequenceStart())
        assertFalse("file=${newspaperFile3.file} is NOT isAHundredsSequenceStart", newspaperFile3.isAHundredsSequenceStart())
        assertFalse("file=${newspaperFile4.file} is NOT isAHundredsSequenceStart", newspaperFile4.isAHundredsSequenceStart())
        assertFalse("file=${newspaperFile7.file} is NOT isAHundredsSequenceStart", newspaperFile7.isAHundredsSequenceStart())
        assertFalse("file=${newspaperFile9.file} is NOT isAHundredsSequenceStart", newspaperFile9.isAHundredsSequenceStart())
    }

}
