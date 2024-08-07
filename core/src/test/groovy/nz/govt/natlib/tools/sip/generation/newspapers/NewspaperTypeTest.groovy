package nz.govt.natlib.tools.sip.generation.newspapers


import static org.hamcrest.core.Is.is
import static org.junit.Assert.assertThat

import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner.class)
class NewspaperTypeTest {
    @Test
    void loadsTheWTAASpreadsheet() {
        NewspaperType newspaperType = new NewspaperType("WTAA")
        assertThat("PDF_FILE_WITH_TITLE_SECTION_DATE_SEQUENCE_GROUPING_PATTERN is set correctly",
                newspaperType.PDF_FILE_WITH_TITLE_SECTION_DATE_SEQUENCE_GROUPING_PATTERN,
                is("(?<titleCode>[a-zA-Z0-9]{3,4})-(?<issue>)(?<editionCode>)(?<sectionCode>)(?<date>\\d{8})-(?<sequenceLetter>[A-Za-z]{0,2})(?<sequenceNumber>\\d{1,4})(?<qualifier>.*?)(?<revision>)\\.[pP]{1}[dD]{1}[fF]{1}"))
        assertThat("PDF_FILE_WITH_TITLE_SECTION_DATE_SEQUENCE_GROUPING_PATTERN is set correctly",
                newspaperType.PDF_FILE_WITH_TITLE_SECTION_DATE_SEQUENCE_PATTERN,
                is("\\w{4}-\\d{8}.*?\\.[pP]{1}[dD]{1}[fF]{1}"))
        assertThat("PDF_FILE_WITH_TITLE_SECTION_DATE_PATTERN is set correctly",
                newspaperType.PDF_FILE_WITH_TITLE_SECTION_DATE_PATTERN,
                is("\\w{4}-\\d{8}.*?\\.[pP]{1}[dD]{1}[fF]{1}"))
        assertThat("DATE_TIME_PATTERN is set correctly",
                newspaperType.DATE_TIME_PATTERN,
                is("yyyyMMdd"))
    }

    @Test
    void loadsTheAlliedPressSpreadsheet() {
        NewspaperType newspaperType = new NewspaperType("alliedPress")
        assertThat("PDF_FILE_WITH_TITLE_SECTION_DATE_SEQUENCE_GROUPING_PATTERN is set correctly",
                newspaperType.PDF_FILE_WITH_TITLE_SECTION_DATE_SEQUENCE_GROUPING_PATTERN,
                is("(?<titleCode>[A-Z]{2,4})(?<issue>)(?<editionCode>)(?<sectionCode>)_(?<date>\\d{4}_\\d{2}_\\d{2})(?<revision>)(?<sequenceLetter>)(?<sequenceNumber>)(?<qualifier>)\\.[pP]{1}[dD]{1}[fF]{1}"))
        assertThat("PDF_FILE_WITH_TITLE_SECTION_DATE_SEQUENCE_GROUPING_PATTERN is set correctly",
                newspaperType.PDF_FILE_WITH_TITLE_SECTION_DATE_SEQUENCE_PATTERN,
                is("\\w{2,4}_\\d{4}_\\d{2}_\\d{2}\\.[pP]{1}[dD]{1}[fF]{1}"))
        assertThat("PDF_FILE_WITH_TITLE_SECTION_DATE_PATTERN is set correctly",
                newspaperType.PDF_FILE_WITH_TITLE_SECTION_DATE_PATTERN,
                is("\\w{2,4}_\\d{4}_\\d{2}_\\d{2}\\.[pP]{1}[dD]{1}[fF]{1}"))
        assertThat("DATE_TIME_PATTERN is set correctly",
                newspaperType.DATE_TIME_PATTERN,
                is("yyyy_MM_dd"))
    }

    @Test
    void loadsTheWptNewsSpreadsheet() {
        NewspaperType newspaperType = new NewspaperType("wptNews")
        assertThat("PDF_FILE_WITH_TITLE_SECTION_DATE_SEQUENCE_GROUPING_PATTERN is set correctly",
                newspaperType.PDF_FILE_WITH_TITLE_SECTION_DATE_SEQUENCE_GROUPING_PATTERN,
                is("(?<date>\\d{6})(?<titleCode>[a-zA-Z0-9]{7})(?<issue>)(?<editionCode>)(?<sectionCode>)(?<revision>)(?<sequenceLetter>)(?<sequenceNumber>\\d{1,4})(?<qualifier>.*?)\\.[pP]{1}[dD]{1}[fF]{1}"))
        assertThat("PDF_FILE_WITH_TITLE_SECTION_DATE_SEQUENCE_GROUPING_PATTERN is set correctly",
                newspaperType.PDF_FILE_WITH_TITLE_SECTION_DATE_SEQUENCE_PATTERN,
                is("\\d{6}\\w{6,7}.*?\\.[pP]{1}[dD]{1}[fF]{1}"))
        assertThat("PDF_FILE_WITH_TITLE_SECTION_DATE_PATTERN is set correctly",
                newspaperType.PDF_FILE_WITH_TITLE_SECTION_DATE_PATTERN,
                is("\\d{6}\\w{6,7}.*?\\.[pP]{1}[dD]{1}[fF]{1}"))
        assertThat("DATE_TIME_PATTERN is set correctly",
                newspaperType.DATE_TIME_PATTERN,
                is("ddMMyy"))
    }

    @Test
    void loadsTheAreMediaSpreadsheet() {
        NewspaperType newspaperType = new NewspaperType("areMedia")
        assertThat("PDF_FILE_WITH_TITLE_SECTION_DATE_SEQUENCE_GROUPING_PATTERN is set correctly",
                newspaperType.PDF_FILE_WITH_TITLE_SECTION_DATE_SEQUENCE_GROUPING_PATTERN,
                is("(?<publisherCode>[a-z]{3})(?<titleCode>[a-zA-Z0-9]{2})(?<issue>\\d{4})(?<sequenceLetter>\\w{1})(?<sequenceNumber>\\d{3})(?<editionCode>)(?<sectionCode>)([_])?(?<revision>[a-zA-Z0-9]{0,2})_(?<qualifier>.*?)-(?<date>\\d{6}).[pP]{1}[dD]{1}[fF]{1}"))
        assertThat("PDF_FILE_WITH_TITLE_SECTION_DATE_SEQUENCE_GROUPING_PATTERN is set correctly",
                newspaperType.PDF_FILE_WITH_TITLE_SECTION_DATE_SEQUENCE_PATTERN,
                is(".*?\\w{2}.*?\\d{6}\\.[pP]{1}[dD]{1}[fF]{1}"))
        assertThat("PDF_FILE_WITH_TITLE_SECTION_DATE_PATTERN is set correctly",
                newspaperType.PDF_FILE_WITH_TITLE_SECTION_DATE_PATTERN,
                is(".*?\\w{2}.*?\\d{6}\\.[pP]{1}[dD]{1}[fF]{1}"))
        assertThat("DATE_TIME_PATTERN is set correctly",
                newspaperType.DATE_TIME_PATTERN,
                is("ddMMyy"))
    }

    @Test
    void loadsTheStuffSpreadsheet() {
        NewspaperType newspaperType = new NewspaperType("stuff")
        assertThat("PDF_FILE_WITH_TITLE_SECTION_DATE_SEQUENCE_GROUPING_PATTERN is set correctly",
                newspaperType.PDF_FILE_WITH_TITLE_SECTION_DATE_SEQUENCE_GROUPING_PATTERN,
                is("(?<titleCode>[a-zA-Z0-9]{3})-?(?<sectionCode>[a-zA-Z0-9]{2,3})-(?<editionCode>\\w{2})?-?(?<date>\\d{8})-(?<sequenceLetter>[A-Za-z]{0,2})(?<sequenceNumber>\\d{1,4})(?<qualifier>.*?)(?<issue>)(?<revision>)\\.[pP]{1}[dD]{1}[fF]{1}"))
        assertThat("PDF_FILE_WITH_TITLE_SECTION_DATE_SEQUENCE_GROUPING_PATTERN is set correctly",
                newspaperType.PDF_FILE_WITH_TITLE_SECTION_DATE_SEQUENCE_PATTERN,
                is("\\w{3}-?\\w{3,4}-(\\w{2}-){0,1}?\\d{8}-\\w{1,4}.*?\\.[pP]{1}[dD]{1}[fF]{1}"))
        assertThat("PDF_FILE_WITH_TITLE_SECTION_DATE_PATTERN is set correctly",
                newspaperType.PDF_FILE_WITH_TITLE_SECTION_DATE_PATTERN,
                is("\\w{3}-?\\w{3,4}-(\\w{2}-){0,1}?\\d{8}-.*?\\.[pP]{1}[dD]{1}[fF]{1}"))
        assertThat("DATE_TIME_PATTERN is set correctly",
                newspaperType.DATE_TIME_PATTERN,
                is("yyyyMMdd"))
    }

    @Test
    void loadsTheNZMESpreadsheet() {
        NewspaperType newspaperType = new NewspaperType("NZME")
        assertThat("PDF_FILE_WITH_TITLE_SECTION_DATE_SEQUENCE_GROUPING_PATTERN is set correctly",
                newspaperType.PDF_FILE_WITH_TITLE_SECTION_DATE_SEQUENCE_GROUPING_PATTERN,
                is("(?<titleCode>[a-zA-Z]{3})(?<editionCode>[a-zA-Z]{1})(?<date>\\d{2}[a-zA-Z]{3}\\d{2})(?<sequenceLetter>[A-Za-z]{0,1})(?<sequenceNumber>\\d{1,4})(?<sectionCode>)(?<qualifier>)(?<issue>)(?<revision>)\\.[pP]{1}[dD]{1}[fF]{1}"))
        assertThat("PDF_FILE_WITH_TITLE_SECTION_DATE_SEQUENCE_GROUPING_PATTERN is set correctly",
                newspaperType.PDF_FILE_WITH_TITLE_SECTION_DATE_SEQUENCE_PATTERN,
                is("\\w{3}\\w{1}\\d{2}\\w{3}\\d{2}.*?\\.[pP]{1}[dD]{1}[fF]{1}"))
        assertThat("PDF_FILE_WITH_TITLE_SECTION_DATE_PATTERN is set correctly",
                newspaperType.PDF_FILE_WITH_TITLE_SECTION_DATE_PATTERN,
                is("\\w{3}\\w{1}\\d{2}\\w{3}\\d{2}.*?\\.[pP]{1}[dD]{1}[fF]{1}"))
        assertThat("DATE_TIME_PATTERN is set correctly",
                newspaperType.DATE_TIME_PATTERN,
                is("ddMMMyy"))
    }
}