package nz.govt.natlib.tools.sip.generation.fairfax

import nz.govt.natlib.tools.sip.generation.fairfax.parameters.ProcessingOption
import nz.govt.natlib.tools.sip.generation.fairfax.parameters.ProcessingRule

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
 * Tests {@link FairfaxFile}.
 */
@RunWith(MockitoJUnitRunner.class)
class FairfaxFileTest {

    @Mock
    Path mockFile, mockFile1, mockFile2, mockFile3

    @Test
    void correctlyMatchesFilenamesUsingDifferentRegexPatterns() {
        checkRegexFilenamePatternMatches("abcD25Apr18A1.pDF", true, true, true)
        checkRegexFilenamePatternMatches("abcD25APR18A1.pDF", true, true, true)
        checkRegexFilenamePatternMatches("abcd25Apr18A01.pDF", true, true, true)
        checkRegexFilenamePatternMatches("abcd25APR18A001.PDF", true, true, true)
        checkRegexFilenamePatternMatches("abcD25apr18A0001.PDF", true, true, true)
        checkRegexFilenamePatternMatches("abcD25Apr18001.PDF", true, true, true)
        checkRegexFilenamePatternMatches("abcD25Apr18A1some-qualifier.pDF", true, true, true)
        checkRegexFilenamePatternMatches("abcD25Apr18A1-another-qualifier.pDF", true, true, true)
        // NOTE: This does match, but the '1' at the end is included in the qualifier.
        checkRegexFilenamePatternMatches("abcD25Apr18A0001.pdf", true, true, true)

        checkRegexFilenamePatternMatches("abcDnodateA001.PDF", false, false, false)
        checkRegexFilenamePatternMatches("abcDEFGH-20180425-A001.PDF", false, false, false)
        checkRegexFilenamePatternMatches("abcD25Apr18A001", false, false, false)
        checkRegexFilenamePatternMatches("abcD25Apr18A001.pdf2", false, false, false)
        checkRegexFilenamePatternMatches("ab25Apr18A001.pdf", false, false, false)
        checkRegexFilenamePatternMatches("", false, false, false)
    }

    void checkRegexFilenamePatternMatches(String valueToCheck, boolean matchesWithGroupingRegex,
                                          boolean matchesWithDateSequencePattern, boolean matchesWithDateOnlyPattern) {
        if (matchesWithGroupingRegex) {
            assertTrue("value=${valueToCheck} matches pattern=${FairfaxFile.PDF_FILE_WITH_TITLE_SECTION_DATE_SEQUENCE_GROUPING_PATTERN}",
                    valueToCheck ==~ /${FairfaxFile.PDF_FILE_WITH_TITLE_SECTION_DATE_SEQUENCE_GROUPING_PATTERN}/)
        } else {
            assertFalse("value=${valueToCheck} does NOT match pattern=${FairfaxFile.PDF_FILE_WITH_TITLE_SECTION_DATE_SEQUENCE_GROUPING_PATTERN}",
                    valueToCheck ==~ /${FairfaxFile.PDF_FILE_WITH_TITLE_SECTION_DATE_SEQUENCE_GROUPING_PATTERN}/)
        }
        if (matchesWithDateSequencePattern) {
            assertTrue("value=${valueToCheck} matches pattern=${FairfaxFile.PDF_FILE_WITH_TITLE_SECTION_DATE_SEQUENCE_PATTERN}",
                    valueToCheck ==~ /${FairfaxFile.PDF_FILE_WITH_TITLE_SECTION_DATE_SEQUENCE_PATTERN}/)
        } else {
            assertFalse("value=${valueToCheck} does NOT match pattern=${FairfaxFile.PDF_FILE_WITH_TITLE_SECTION_DATE_SEQUENCE_PATTERN}",
                    valueToCheck ==~ /${FairfaxFile.PDF_FILE_WITH_TITLE_SECTION_DATE_SEQUENCE_PATTERN}/)
        }
        if (matchesWithDateOnlyPattern) {
            assertTrue("value=${valueToCheck} matches pattern=${FairfaxFile.PDF_FILE_WITH_TITLE_SECTION_DATE_PATTERN}",
                    valueToCheck ==~ /${FairfaxFile.PDF_FILE_WITH_TITLE_SECTION_DATE_PATTERN}/)
        } else {
            assertFalse("value=${valueToCheck} does NOT match pattern=${FairfaxFile.PDF_FILE_WITH_TITLE_SECTION_DATE_PATTERN}",
                    valueToCheck ==~ /${FairfaxFile.PDF_FILE_WITH_TITLE_SECTION_DATE_PATTERN}/)
        }
    }

    @Test
    void createsCorrectlyWithLetterSequence() {
        String originalFilename = "TST22Oct18B024.pdf"
        when(mockFile.fileName).thenReturn(Path.of(originalFilename))

        FairfaxFile testFairfaxFile = new FairfaxFile(mockFile)

        assertThat("Filename extracted correctly", testFairfaxFile.filename, is(originalFilename))
        assertThat("TitleCode parsed correctly", testFairfaxFile.titleCode, is("TST"))
        assertNotNull("Year extracted", testFairfaxFile.dateYear)
        assertThat("dateYear parsed correctly", testFairfaxFile.dateYear, is(new Integer(2018)))
        assertThat("dateMonthOfYear parsed correctly", testFairfaxFile.dateMonthOfYear, is(new Integer(10)))
        assertThat("dateDayOfMonth parsed correctly", testFairfaxFile.dateDayOfMonth, is(new Integer(22)))
        assertThat("Prefix parsed correctly", testFairfaxFile.sequenceLetter, is("B"))
        assertThat("Prefix parsed correctly", testFairfaxFile.sequenceNumberString, is("024"))
        assertThat("Prefix parsed correctly", testFairfaxFile.sequenceNumber, is(24))
        assertThat("Qualifier parsed correctly", testFairfaxFile.qualifier, is(""))
        assertTrue("FairfaxFile is valid", testFairfaxFile.isValidName())
    }

    @Test
    void createsCorrectlyWithNumberOnlySequence() {
        String originalFilename = "t2022Oct18024.pdf"
        when(mockFile.fileName).thenReturn(Path.of(originalFilename))

        FairfaxFile testFairfaxFile = new FairfaxFile(mockFile)

        assertThat("filename extracted correctly", testFairfaxFile.filename, is(originalFilename))
        assertThat("TitleCode parsed correctly", testFairfaxFile.titleCode, is("t20"))
        assertThat("dateYear parsed correctly", testFairfaxFile.dateYear, is(new Integer(2018)))
        assertThat("dateMonthOfYear parsed correctly", testFairfaxFile.dateMonthOfYear, is(new Integer(10)))
        assertThat("dateDayOfMonth parsed correctly", testFairfaxFile.dateDayOfMonth, is(new Integer(22)))
        assertThat("sequenceLetter parsed correctly", testFairfaxFile.sequenceLetter, is(""))
        assertThat("sequenceNumber parsed correctly", testFairfaxFile.sequenceNumber, is(24))
        assertThat("Qualifier parsed correctly", testFairfaxFile.qualifier, is(""))
        assertTrue("FairfaxFile is valid", testFairfaxFile.isValidName())
    }

    @Test
    void createsCorrectlyWithLetterSequenceQualifier() {
        String originalFilename = "TST22Oct18B024a qualifier.pdf"
        when(mockFile.fileName).thenReturn(Path.of(originalFilename))

        FairfaxFile testFairfaxFile = new FairfaxFile(mockFile)

        assertThat("Filename extracted correctly", testFairfaxFile.filename, is(originalFilename))
        assertThat("TitleCode parsed correctly", testFairfaxFile.titleCode, is("TST"))
        assertNotNull("Year extracted", testFairfaxFile.dateYear)
        assertThat("dateYear parsed correctly", testFairfaxFile.dateYear, is(new Integer(2018)))
        assertThat("dateMonthOfYear parsed correctly", testFairfaxFile.dateMonthOfYear, is(new Integer(10)))
        assertThat("dateDayOfMonth parsed correctly", testFairfaxFile.dateDayOfMonth, is(new Integer(22)))
        assertThat("Prefix parsed correctly", testFairfaxFile.sequenceLetter, is("B"))
        assertThat("Prefix parsed correctly", testFairfaxFile.sequenceNumberString, is("024"))
        assertThat("Prefix parsed correctly", testFairfaxFile.sequenceNumber, is(24))
        assertThat("Qualifier parsed correctly", testFairfaxFile.qualifier, is("a qualifier"))
        assertTrue("FairfaxFile is valid", testFairfaxFile.isValidName())
    }

    @Test
    void createsCorrectlyWithMixedCaseExtension() {
        String originalFilename = "TST22Oct18B024a qualifier.pDf"
        when(mockFile.fileName).thenReturn(Path.of(originalFilename))

        FairfaxFile testFairfaxFile = new FairfaxFile(mockFile)

        assertThat("Filename extracted correctly", testFairfaxFile.filename, is(originalFilename))
        assertThat("TitleCode parsed correctly", testFairfaxFile.titleCode, is("TST"))
        assertNotNull("Year extracted", testFairfaxFile.dateYear)
        assertThat("dateYear parsed correctly", testFairfaxFile.dateYear, is(new Integer(2018)))
        assertThat("dateMonthOfYear parsed correctly", testFairfaxFile.dateMonthOfYear, is(new Integer(10)))
        assertThat("dateDayOfMonth parsed correctly", testFairfaxFile.dateDayOfMonth, is(new Integer(22)))
        assertThat("Prefix parsed correctly", testFairfaxFile.sequenceLetter, is("B"))
        assertThat("Prefix parsed correctly", testFairfaxFile.sequenceNumberString, is("024"))
        assertThat("Prefix parsed correctly", testFairfaxFile.sequenceNumber, is(24))
        assertThat("Qualifier parsed correctly", testFairfaxFile.qualifier, is("a qualifier"))
        assertTrue("FairfaxFile is valid", testFairfaxFile.isValidName())
    }

    @Test
    void createsCorrectlyWithUpperCaseExtension() {
        String originalFilename = "TST22Oct18B024a qualifier.PDF"
        when(mockFile.fileName).thenReturn(Path.of(originalFilename))

        FairfaxFile testFairfaxFile = new FairfaxFile(mockFile)

        assertThat("Filename extracted correctly", testFairfaxFile.filename, is(originalFilename))
        assertThat("TitleCode parsed correctly", testFairfaxFile.titleCode, is("TST"))
        assertNotNull("Year extracted", testFairfaxFile.dateYear)
        assertThat("dateYear parsed correctly", testFairfaxFile.dateYear, is(new Integer(2018)))
        assertThat("dateMonthOfYear parsed correctly", testFairfaxFile.dateMonthOfYear, is(new Integer(10)))
        assertThat("dateDayOfMonth parsed correctly", testFairfaxFile.dateDayOfMonth, is(new Integer(22)))
        assertThat("Prefix parsed correctly", testFairfaxFile.sequenceLetter, is("B"))
        assertThat("Prefix parsed correctly", testFairfaxFile.sequenceNumberString, is("024"))
        assertThat("Prefix parsed correctly", testFairfaxFile.sequenceNumber, is(24))
        assertThat("Qualifier parsed correctly", testFairfaxFile.qualifier, is("a qualifier"))
        assertTrue("FairfaxFile is valid", testFairfaxFile.isValidName())
    }

    @Test
    void createsCorrectlyWithFourCharacterTitleCode() {
        String originalFilename = "JAZZ22Oct18B024a qualifier.pDf"
        when(mockFile.fileName).thenReturn(Path.of(originalFilename))

        FairfaxFile testFairfaxFile = new FairfaxFile(mockFile)

        assertThat("Filename extracted correctly", testFairfaxFile.filename, is(originalFilename))
        assertThat("TitleCode parsed correctly", testFairfaxFile.titleCode, is("JAZZ"))
        assertNotNull("Year extracted", testFairfaxFile.dateYear)
        assertThat("dateYear parsed correctly", testFairfaxFile.dateYear, is(new Integer(2018)))
        assertThat("dateMonthOfYear parsed correctly", testFairfaxFile.dateMonthOfYear, is(new Integer(10)))
        assertThat("dateDayOfMonth parsed correctly", testFairfaxFile.dateDayOfMonth, is(new Integer(22)))
        assertThat("Prefix parsed correctly", testFairfaxFile.sequenceLetter, is("B"))
        assertThat("Prefix parsed correctly", testFairfaxFile.sequenceNumberString, is("024"))
        assertThat("Prefix parsed correctly", testFairfaxFile.sequenceNumber, is(24))
        assertThat("Qualifier parsed correctly", testFairfaxFile.qualifier, is("a qualifier"))
        assertTrue("FairfaxFile is valid", testFairfaxFile.isValidName())
    }

    @Test
    void createsCorrectlyWithNumberOnlySequenceQualifier() {
        String originalFilename = "t2022Oct18024crop.pdf"
        when(mockFile.fileName).thenReturn(Path.of(originalFilename))

        FairfaxFile testFairfaxFile = new FairfaxFile(mockFile)

        assertThat("filename extracted correctly", testFairfaxFile.filename, is(originalFilename))
        assertThat("TitleCode parsed correctly", testFairfaxFile.titleCode, is("t20"))
        assertThat("dateYear parsed correctly", testFairfaxFile.dateYear, is(new Integer(2018)))
        assertThat("dateMonthOfYear parsed correctly", testFairfaxFile.dateMonthOfYear, is(new Integer(10)))
        assertThat("dateDayOfMonth parsed correctly", testFairfaxFile.dateDayOfMonth, is(new Integer(22)))
        assertThat("sequenceLetter parsed correctly", testFairfaxFile.sequenceLetter, is(""))
        assertThat("sequenceNumber parsed correctly", testFairfaxFile.sequenceNumber, is(24))
        assertThat("Qualifier parsed correctly", testFairfaxFile.qualifier, is("crop"))
        assertTrue("FairfaxFile is valid", testFairfaxFile.isValidName())
    }

    @Test
    void createsCorrectlyWithInvalidFilename() {
        String originalFilename = "abcde22Oct18024.pdf"
        when(mockFile.fileName).thenReturn(Path.of(originalFilename))

        FairfaxFile testFairfaxFile = new FairfaxFile(mockFile)

        assertThat("filename extracted correctly", testFairfaxFile.filename, is(originalFilename))
        assertFalse("FairfaxFile is invalid", testFairfaxFile.isValidName())
    }

    @Test
    void matchesWhenSamePrefixAndDate() {
        String filename1 = "Mixy22Oct18023.pdf"
        String filename2 = "Mixy22Oct18001.pdf"
        when(mockFile1.fileName).thenReturn(Path.of(filename1))
        when(mockFile2.fileName).thenReturn(Path.of(filename2))

        FairfaxFile fairfaxFile1 = new FairfaxFile(mockFile1)
        FairfaxFile fairfaxFile2 = new FairfaxFile(mockFile2)

        assertTrue("Same prefix and date in filename matches", fairfaxFile1.matches(fairfaxFile2))
        assertFalse("Same prefix and date but different sequence does not sequence match",
                fairfaxFile1.matchesWithSequence(fairfaxFile2))
    }

    @Test
    void matchesWhenSamePrefixDateAndSequence() {
        String filename1 = "Mixy22Oct18023.pdf"
        String filename2 = "Mixy22Oct18023withQualifier.pdf"
        when(mockFile1.fileName).thenReturn(Path.of(filename1))
        when(mockFile2.fileName).thenReturn(Path.of(filename2))

        FairfaxFile fairfaxFile1 = new FairfaxFile(mockFile1)
        FairfaxFile fairfaxFile2 = new FairfaxFile(mockFile2)

        assertTrue("Same prefix and date in filename matches", fairfaxFile1.matches(fairfaxFile2))
        assertTrue("Matches with sequence", fairfaxFile1.matchesWithSequence(fairfaxFile2))
    }

    @Test
    void doesNotMatchWhenSamePrefixButDifferentDate() {
        String filename1 = "ABCD22Oct18023.pdf"
        String filename2 = "ABCD23Oct18023.pdf"
        when(mockFile1.fileName).thenReturn(Path.of(filename1))
        when(mockFile2.fileName).thenReturn(Path.of(filename2))

        FairfaxFile fairfaxFile1 = new FairfaxFile(mockFile1)
        FairfaxFile fairfaxFile2 = new FairfaxFile(mockFile2)

        assertFalse("Same prefix but different dates does not match", fairfaxFile1.matches(fairfaxFile2))
    }

    @Test
    void doesNotMatchWhenDifferentPrefix() {
        String filename1 = "NAMA22Oct18023.pdf"
        String filename2 = "NAMB22Oct20023.pdf"
        when(mockFile1.fileName).thenReturn(Path.of(filename1))
        when(mockFile2.fileName).thenReturn(Path.of(filename2))

        FairfaxFile fairfaxFile1 = new FairfaxFile(mockFile1)
        FairfaxFile fairfaxFile2 = new FairfaxFile(mockFile2)

        assertFalse("Different prefixes does not match", fairfaxFile1.matches(fairfaxFile2))
    }

    @Test
    void sortsCorrectlyWithSameDateButDifferentSequenceNumbers() {
        String filename1 = "NAMe22Oct18023.pdf"
        String filename2 = "NAMe22Oct18022.pdf"
        String filename3 = "NAMe22Oct18021.pdf"
        when(mockFile1.fileName).thenReturn(Path.of(filename1))
        when(mockFile2.fileName).thenReturn(Path.of(filename2))
        when(mockFile3.fileName).thenReturn(Path.of(filename3))

        FairfaxFile fairfaxFile1 = new FairfaxFile(mockFile1)
        FairfaxFile fairfaxFile2 = new FairfaxFile(mockFile2)
        FairfaxFile fairfaxFile3 = new FairfaxFile(mockFile3)

        assertEquals("Sorts correctly with same date but different sequence numbers",
                [fairfaxFile1, fairfaxFile2, fairfaxFile3].sort(), [fairfaxFile3, fairfaxFile2, fairfaxFile1])
    }

    @Test
    void sortsCorrectlyWithDifferentDates() {
        String filename1 = "NAMe23Oct18021.pdf"
        String filename2 = "NAMe22Oct18022.pdf"
        String filename3 = "NAMe21Oct18023.pdf"
        when(mockFile1.fileName).thenReturn(Path.of(filename1))
        when(mockFile2.fileName).thenReturn(Path.of(filename2))
        when(mockFile3.fileName).thenReturn(Path.of(filename3))

        FairfaxFile fairfaxFile1 = new FairfaxFile(mockFile1)
        FairfaxFile fairfaxFile2 = new FairfaxFile(mockFile2)
        FairfaxFile fairfaxFile3 = new FairfaxFile(mockFile3)

        assertEquals("Sorts correctly with same date but different sequence numbers",
                [fairfaxFile1, fairfaxFile2, fairfaxFile3].sort(), [fairfaxFile3, fairfaxFile2, fairfaxFile1])
    }

    @Test
    void sortsCorrectlyWithSameDateAndSequenceStringButDifferentNumbers() {
        String filename1 = "NAMe22Oct18C023.pdf"
        String filename2 = "NAMe22Oct18C022.pdf"
        String filename3 = "NAMe22Oct18C021.pdf"
        when(mockFile1.fileName).thenReturn(Path.of(filename1))
        when(mockFile2.fileName).thenReturn(Path.of(filename2))
        when(mockFile3.fileName).thenReturn(Path.of(filename3))

        FairfaxFile fairfaxFile1 = new FairfaxFile(mockFile1)
        FairfaxFile fairfaxFile2 = new FairfaxFile(mockFile2)
        FairfaxFile fairfaxFile3 = new FairfaxFile(mockFile3)

        assertEquals("Sorts correctly with same date and sequence string but different sequence numbers",
                [fairfaxFile1, fairfaxFile2, fairfaxFile3].sort(), [fairfaxFile3, fairfaxFile2, fairfaxFile1])
    }

    @Test
    void sortsCorrectlyWithSameDateAndDifferentSequenceStringButDifferentNumbers() {
        String filename1 = "NAMe22Oct18M023.pdf"
        String filename2 = "NAMe22Oct18C022.pdf"
        String filename3 = "NAMe22Oct18A021.pdf"
        when(mockFile1.fileName).thenReturn(Path.of(filename1))
        when(mockFile2.fileName).thenReturn(Path.of(filename2))
        when(mockFile3.fileName).thenReturn(Path.of(filename3))

        FairfaxFile fairfaxFile1 = new FairfaxFile(mockFile1)
        FairfaxFile fairfaxFile2 = new FairfaxFile(mockFile2)
        FairfaxFile fairfaxFile3 = new FairfaxFile(mockFile3)

        assertEquals("Sorts correctly with same date but different sequence numbers",
                [fairfaxFile1, fairfaxFile2, fairfaxFile3].sort(), [fairfaxFile3, fairfaxFile2, fairfaxFile1])
    }

    @Test
    void correctlyCreatesLocalDateFromFilename() {
        String filename1 = "NAMe01Jan18M023.pdf"
        String filename2 = "NAMe30Jun18A021.pdf"
        String filename3 = "NAMe31Dec18C022.pdf"
        when(mockFile1.fileName).thenReturn(Path.of(filename1))
        when(mockFile2.fileName).thenReturn(Path.of(filename2))
        when(mockFile3.fileName).thenReturn(Path.of(filename3))

        FairfaxFile fairfaxFile1 = new FairfaxFile(mockFile1)
        FairfaxFile fairfaxFile2 = new FairfaxFile(mockFile2)
        FairfaxFile fairfaxFile3 = new FairfaxFile(mockFile3)

        LocalDate january12018 = LocalDate.of(2018, 1, 1)
        LocalDate june302018 = LocalDate.of(2018, 6, 30)
        LocalDate december312018 = LocalDate.of(2018, 12, 31)
        assertThat("Creates date correctly for ${january12018}", fairfaxFile1.date, is(january12018))
        assertThat("Creates date correctly for ${june302018}", fairfaxFile2.date, is(june302018))
        assertThat("Creates date correctly for ${december312018}", fairfaxFile3.date, is(december312018))
    }

    @Test
    void sortsCorrectlyUsingNumericBeforeAlpha() {
        Path file1 = Path.of("NAMe31Jan18M023.pdf")
        Path file2 = Path.of("NAMe31Jan18A01.pdf")
        Path file3 = Path.of("NAMe31Jan18A02.pdf")
        Path file4 = Path.of("NAMe31Jan1802.pdf")
        Path file5 = Path.of("NAMe31Jan1801.pdf")
        Path file6 = Path.of("NAMe31Jan18C1.pdf")
        Path file7 = Path.of("NAMe31Jan18C2.pdf")

        FairfaxFile fairfaxFile1 = new FairfaxFile(file1)
        FairfaxFile fairfaxFile2 = new FairfaxFile(file2)
        FairfaxFile fairfaxFile3 = new FairfaxFile(file3)
        FairfaxFile fairfaxFile4 = new FairfaxFile(file4)
        FairfaxFile fairfaxFile5 = new FairfaxFile(file5)
        FairfaxFile fairfaxFile6 = new FairfaxFile(file6)
        FairfaxFile fairfaxFile7 = new FairfaxFile(file7)

        List<FairfaxFile> unsorted = [ fairfaxFile1, fairfaxFile2, fairfaxFile3, fairfaxFile4, fairfaxFile5,
                                       fairfaxFile6, fairfaxFile7 ]
        List<FairfaxFile> expected = [ fairfaxFile5, fairfaxFile4, fairfaxFile2, fairfaxFile3, fairfaxFile6,
                                       fairfaxFile7, fairfaxFile1 ]
        List<FairfaxFile> sorted = FairfaxFile.sortNumericAndAlpha(unsorted, false)
        assertThat("Numeric comes before alpha for sorted=${sorted}", sorted, is(expected))
    }

    @Test
    void sortsCorrectlyUsingAlphaBeforeNumeric() {
        Path file1 = Path.of("NAMe31Jan18M023.pdf")
        Path file2 = Path.of("NAMe31Jan18A01.pdf")
        Path file3 = Path.of("NAMe31Jan18A02.pdf")
        Path file4 = Path.of("NAMe31Jan1802.pdf")
        Path file5 = Path.of("NAMe31Jan1801.pdf")
        Path file6 = Path.of("NAMe31Jan18C1.pdf")
        Path file7 = Path.of("NAMe31Jan18C2.pdf")

        FairfaxFile fairfaxFile1 = new FairfaxFile(file1)
        FairfaxFile fairfaxFile2 = new FairfaxFile(file2)
        FairfaxFile fairfaxFile3 = new FairfaxFile(file3)
        FairfaxFile fairfaxFile4 = new FairfaxFile(file4)
        FairfaxFile fairfaxFile5 = new FairfaxFile(file5)
        FairfaxFile fairfaxFile6 = new FairfaxFile(file6)
        FairfaxFile fairfaxFile7 = new FairfaxFile(file7)

        List<FairfaxFile> unsorted = [ fairfaxFile1, fairfaxFile2, fairfaxFile3, fairfaxFile4, fairfaxFile5,
                                       fairfaxFile6, fairfaxFile7 ]
        List<FairfaxFile> expected = [ fairfaxFile2, fairfaxFile3, fairfaxFile6, fairfaxFile7, fairfaxFile1,
                                       fairfaxFile5, fairfaxFile4 ]
        List<FairfaxFile> sorted = FairfaxFile.sortNumericAndAlpha(unsorted, true)
        assertThat("Alpha comes before numeric for sorted=${sorted}", sorted, is(expected))
    }

    @Test
    void correctlyDeterminesAHundredsSequenceStart() {
        Path file1 = Path.of("NAMe31Jan18100.pdf")
        Path file2 = Path.of("NAMe31Jan18101.pdf")
        Path file3 = Path.of("NAMe31Jan18300.pdf")
        Path file4 = Path.of("NAMe313Jan18399.pdf")
        Path file5 = Path.of("NAMe31Jan18400.pdf")
        Path file6 = Path.of("NAMe31Jan18401.pdf")
        Path file7 = Path.of("NAMe31Jan18402.pdf")
        Path file8 = Path.of("NAMe31Jan18501.pdf")
        Path file9 = Path.of("NAMe31Jan18502.pdf")

        FairfaxFile fairfaxFile1 = new FairfaxFile(file1)
        FairfaxFile fairfaxFile2 = new FairfaxFile(file2)
        FairfaxFile fairfaxFile3 = new FairfaxFile(file3)
        FairfaxFile fairfaxFile4 = new FairfaxFile(file4)
        FairfaxFile fairfaxFile5 = new FairfaxFile(file5)
        FairfaxFile fairfaxFile6 = new FairfaxFile(file6)
        FairfaxFile fairfaxFile7 = new FairfaxFile(file7)
        FairfaxFile fairfaxFile8 = new FairfaxFile(file8)
        FairfaxFile fairfaxFile9 = new FairfaxFile(file9)

        assertTrue("file=${fairfaxFile5.file} is isAHundredsSequenceStart", fairfaxFile5.isAHundredsSequenceStart())
        assertTrue("file=${fairfaxFile6.file} is isAHundredsSequenceStart", fairfaxFile6.isAHundredsSequenceStart())
        assertTrue("file=${fairfaxFile8.file} is isAHundredsSequenceStart", fairfaxFile8.isAHundredsSequenceStart())

        assertFalse("file=${fairfaxFile1.file} is NOT isAHundredsSequenceStart", fairfaxFile1.isAHundredsSequenceStart())
        assertFalse("file=${fairfaxFile2.file} is NOT isAHundredsSequenceStart", fairfaxFile2.isAHundredsSequenceStart())
        assertFalse("file=${fairfaxFile3.file} is NOT isAHundredsSequenceStart", fairfaxFile3.isAHundredsSequenceStart())
        assertFalse("file=${fairfaxFile4.file} is NOT isAHundredsSequenceStart", fairfaxFile4.isAHundredsSequenceStart())
        assertFalse("file=${fairfaxFile7.file} is NOT isAHundredsSequenceStart", fairfaxFile7.isAHundredsSequenceStart())
        assertFalse("file=${fairfaxFile9.file} is NOT isAHundredsSequenceStart", fairfaxFile9.isAHundredsSequenceStart())
    }

}
