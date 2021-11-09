package nz.govt.natlib.tools.sip.generation.fairfax

import static org.hamcrest.core.Is.is
import static org.junit.Assert.assertThat

import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner.class)
class PublicationTypeTest {
    @Test
    void loadsTheWMMASpreadsheet() {
        PublicationType publicationType = new PublicationType("WMMA")
        assertThat("PDF_FILE_WITH_TITLE_SECTION_DATE_SEQUENCE_GROUPING_PATTERN is set correctly",
                publicationType.getPDF_FILE_WITH_TITLE_SECTION_DATE_SEQUENCE_GROUPING_PATTERN(),
                is("(?<titleCode>[a-zA-Z0-9]{3,4})(?<sectionCode>)(?<date>\\d{2}\\w{3}\\d{2})(?<sequenceLetter>[A-Za-z]{0,2})(?<sequenceNumber>\\d{1,4})(?<qualifier>.*?)\\.[pP]{1}[dD]{1}[fF]{1}"))
        assertThat("PDF_FILE_WITH_TITLE_SECTION_DATE_SEQUENCE_GROUPING_PATTERN is set correctly",
                publicationType.getPDF_FILE_WITH_TITLE_SECTION_DATE_SEQUENCE_PATTERN(),
                is("\\w{4,7}\\d{2}\\w{3}\\d{2}\\w{1,4}.*?\\.[pP]{1}[dD]{1}[fF]{1}"))
        assertThat("PDF_FILE_WITH_TITLE_SECTION_DATE_PATTERN is set correctly",
                publicationType.getPDF_FILE_WITH_TITLE_SECTION_DATE_PATTERN(),
                is("\\w{4,7}\\d{2}\\w{3}\\d{2}.*?\\.[pP]{1}[dD]{1}[fF]{1}"))
        assertThat("DATE_TIME_PATTERN is set correctly",
                publicationType.getDATE_TIME_PATTERN(),
                is("ddMMMyy"))
        assertThat("SUPPLEMENTS is null", publicationType.getSUPPLEMENTS(), is(null))
    }

    @Test
    void loadsTheAlliedPressSpreadsheet() {
        PublicationType publicationType = new PublicationType("alliedPress")
        assertThat("PDF_FILE_WITH_TITLE_SECTION_DATE_SEQUENCE_GROUPING_PATTERN is set correctly",
                publicationType.getPDF_FILE_WITH_TITLE_SECTION_DATE_SEQUENCE_GROUPING_PATTERN(),
                is("(?<titleCode>[a-zA-Z0-9]{4,19})(?<sectionCode>)-(?<date>\\d{2}\\w{3}\\d{4})(?<sequenceLetter>)(?<sequenceNumber>)-(?<qualifier>\\w{3})\\.[pP]{1}[dD]{1}[fF]{1}"))
        assertThat("PDF_FILE_WITH_TITLE_SECTION_DATE_SEQUENCE_GROUPING_PATTERN is set correctly",
                publicationType.getPDF_FILE_WITH_TITLE_SECTION_DATE_SEQUENCE_PATTERN(),
                is("\\w{4,19}-\\d{2}\\w{3}\\d{4}-\\w{1,3}.*?\\.[pP]{1}[dD]{1}[fF]{1}"))
        assertThat("PDF_FILE_WITH_TITLE_SECTION_DATE_PATTERN is set correctly",
                publicationType.getPDF_FILE_WITH_TITLE_SECTION_DATE_PATTERN(),
                is("\\w{4,19}-\\d{2}\\w{3}\\d{4}-.*?\\.[pP]{1}[dD]{1}[fF]{1}"))
        assertThat("DATE_TIME_PATTERN is set correctly",
                publicationType.getDATE_TIME_PATTERN(),
                is("ddMMMyyyy"))
        assertThat("SUPPLEMENTS has a UBet key", publicationType.getSUPPLEMENTS().containsKey("UBet"), is(true))
        assert publicationType.getSUPPLEMENTS() instanceof Map
    }
}