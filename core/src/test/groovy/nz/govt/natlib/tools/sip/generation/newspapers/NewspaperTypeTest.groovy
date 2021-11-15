package nz.govt.natlib.tools.sip.generation.newspapers


import static org.hamcrest.core.Is.is
import static org.junit.Assert.assertThat

import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner.class)
class NewspaperTypeTest {
    @Test
    void loadsTheWMMASpreadsheet() {
        NewspaperType newspaperType = new NewspaperType("WMMA")
        assertThat("PDF_FILE_WITH_TITLE_SECTION_DATE_SEQUENCE_GROUPING_PATTERN is set correctly",
                newspaperType.PDF_FILE_WITH_TITLE_SECTION_DATE_SEQUENCE_GROUPING_PATTERN,
                is("(?<titleCode>[a-zA-Z0-9]{3,4})(?<sectionCode>)(?<date>\\d{2}\\w{3}\\d{2})(?<sequenceLetter>[A-Za-z]{0,2})(?<sequenceNumber>\\d{1,4})(?<qualifier>.*?)\\.[pP]{1}[dD]{1}[fF]{1}"))
        assertThat("PDF_FILE_WITH_TITLE_SECTION_DATE_SEQUENCE_GROUPING_PATTERN is set correctly",
                newspaperType.PDF_FILE_WITH_TITLE_SECTION_DATE_SEQUENCE_PATTERN,
                is("\\w{4,7}\\d{2}\\w{3}\\d{2}\\w{1,4}.*?\\.[pP]{1}[dD]{1}[fF]{1}"))
        assertThat("PDF_FILE_WITH_TITLE_SECTION_DATE_PATTERN is set correctly",
                newspaperType.PDF_FILE_WITH_TITLE_SECTION_DATE_PATTERN,
                is("\\w{4,7}\\d{2}\\w{3}\\d{2}.*?\\.[pP]{1}[dD]{1}[fF]{1}"))
        assertThat("DATE_TIME_PATTERN is set correctly",
                newspaperType.DATE_TIME_PATTERN,
                is("ddMMMyy"))
        assertThat("SUPPLEMENTS is null", newspaperType.SUPPLEMENTS, is(null))
    }

    @Test
    void loadsTheAlliedPressSpreadsheet() {
        NewspaperType newspaperType = new NewspaperType("alliedPress")
        assertThat("PDF_FILE_WITH_TITLE_SECTION_DATE_SEQUENCE_GROUPING_PATTERN is set correctly",
                newspaperType.PDF_FILE_WITH_TITLE_SECTION_DATE_SEQUENCE_GROUPING_PATTERN,
                is("(?<titleCode>[a-zA-Z0-9]{4,19})(?<sectionCode>)-(?<date>\\d{2}\\w{3}\\d{4})(?<sequenceLetter>)(?<sequenceNumber>)-(?<qualifier>\\w{3})\\.[pP]{1}[dD]{1}[fF]{1}"))
        assertThat("PDF_FILE_WITH_TITLE_SECTION_DATE_SEQUENCE_GROUPING_PATTERN is set correctly",
                newspaperType.PDF_FILE_WITH_TITLE_SECTION_DATE_SEQUENCE_PATTERN,
                is("\\w{4,19}-\\d{2}\\w{3}\\d{4}-\\w{1,3}.*?\\.[pP]{1}[dD]{1}[fF]{1}"))
        assertThat("PDF_FILE_WITH_TITLE_SECTION_DATE_PATTERN is set correctly",
                newspaperType.PDF_FILE_WITH_TITLE_SECTION_DATE_PATTERN,
                is("\\w{4,19}-\\d{2}\\w{3}\\d{4}-.*?\\.[pP]{1}[dD]{1}[fF]{1}"))
        assertThat("DATE_TIME_PATTERN is set correctly",
                newspaperType.DATE_TIME_PATTERN,
                is("ddMMMyyyy"))
        assertThat("SUPPLEMENTS has a UBet key", newspaperType.SUPPLEMENTS.containsKey("UBet"), is(true))
        assert newspaperType.SUPPLEMENTS instanceof Map
    }

    @Test
    void loadsTheWptNewsSpreadsheet() {
        NewspaperType newspaperType = new NewspaperType("wptNews")
        assertThat("PDF_FILE_WITH_TITLE_SECTION_DATE_SEQUENCE_GROUPING_PATTERN is set correctly",
                newspaperType.PDF_FILE_WITH_TITLE_SECTION_DATE_SEQUENCE_GROUPING_PATTERN,
                is("(?<date>\\d{6})(?<titleCode>[a-zA-Z0-9]{7})(?<sectionCode>)(?<sequenceLetter>)(?<sequenceNumber>\\d{1,4})(?<qualifier>.*?)\\.[pP]{1}[dD]{1}[fF]{1}"))
        assertThat("PDF_FILE_WITH_TITLE_SECTION_DATE_SEQUENCE_GROUPING_PATTERN is set correctly",
                newspaperType.PDF_FILE_WITH_TITLE_SECTION_DATE_SEQUENCE_PATTERN,
                is("\\d{6}\\w{6,7}.*?\\.[pP]{1}[dD]{1}[fF]{1}"))
        assertThat("PDF_FILE_WITH_TITLE_SECTION_DATE_PATTERN is set correctly",
                newspaperType.PDF_FILE_WITH_TITLE_SECTION_DATE_PATTERN,
                is("\\d{6}\\w{6,7}.*?\\.[pP]{1}[dD]{1}[fF]{1}"))
        assertThat("DATE_TIME_PATTERN is set correctly",
                newspaperType.DATE_TIME_PATTERN,
                is("ddMMyy"))
        assertThat("SUPPLEMENTS is null", newspaperType.SUPPLEMENTS, is(null))
    }
}