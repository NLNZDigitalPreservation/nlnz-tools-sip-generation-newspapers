package nz.govt.natlib.tools.sip.generation.newspapers

import static org.hamcrest.core.Is.is
import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertThat
import static org.junit.Assert.assertTrue

import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

/**
 * Tests the {@link NewspaperSpreadsheet}.
 */
@RunWith(MockitoJUnitRunner.class)
class NewspaperSpreadsheetTest {

    @Test
    void loadsTheWMMASpreadsheetCorrectly() {
        PublicationType publicationType = new PublicationType("WMMA")
        NewspaperSpreadsheet newspaperSpreadsheet = NewspaperSpreadsheet.defaultInstance(publicationType.PATH_TO_SPREADSHEET)

        assertTrue("Spreadsheet is valid", newspaperSpreadsheet.spreadsheet.isValid(false, false))
        List<Map<String, String>> mapsForWairarapaTimesList =
                newspaperSpreadsheet.spreadsheet.mapsForColumn(NewspaperSpreadsheet.MMSID_COLUMN_NAME,
                        "9918822769202836")

        assertThat("Wairarapa Times only has one entry", mapsForWairarapaTimesList.size(), is(new Integer(1)))
        Map<String, String> mapsForWairarapaTimes = mapsForWairarapaTimesList.first()
        assertThat("'title_parent' is 'Wairarapa times-age.'", mapsForWairarapaTimes.get("title_parent"), is("Wairarapa times-age."))
        assertThat("'MMSID' is 9918822769202836", mapsForWairarapaTimes.get("MMSID"), is("9918822769202836"))
        assertThat("'title_code' is 'WMMA'", mapsForWairarapaTimes.get("title_code"), is("WMMA"))
        assertFalse("isMagazine is false for WairarapaTimes",
                NewspaperSpreadsheet.extractBooleanValue(mapsForWairarapaTimes, NewspaperSpreadsheet.IS_MAGAZINE_KEY))
    }

    @Test
    void loadsTheAlliedPressSpreadsheetCorrectly() {
        PublicationType publicationType = new PublicationType("alliedPress")
        NewspaperSpreadsheet newspaperSpreadsheet = NewspaperSpreadsheet.defaultInstance(publicationType.PATH_TO_SPREADSHEET)

        assertTrue("Spreadsheet is valid", newspaperSpreadsheet.spreadsheet.isValid(false, false))
        List<Map<String, String>> mapsForAlliedPressList =
                newspaperSpreadsheet.spreadsheet.mapsForColumn(NewspaperSpreadsheet.MMSID_COLUMN_NAME,
                        "9918591570102836")

        assertThat("The Ashburton courier has 1 entry", mapsForAlliedPressList.size(), is(new Integer(1)))
        Map<String, String> mapsForAlliedPress = mapsForAlliedPressList.first()
        assertThat("'title_parent' is 'The Ashburton courier'", mapsForAlliedPress.get("title_parent"), is("The Ashburton courier"))
        assertThat("'MMSID' is 9918591570102836", mapsForAlliedPress.get("MMSID"), is("9918591570102836"))
        assertThat("'title_code' is 'AshburtonCourier'", mapsForAlliedPress.get("title_code"), is("AshburtonCourier"))
        assertFalse("isMagazine is false for The Ashburton courier",
                NewspaperSpreadsheet.extractBooleanValue(mapsForAlliedPress, NewspaperSpreadsheet.IS_MAGAZINE_KEY))
    }

    @Test
    void loadsTheWptNewsSpreadsheetCorrectly() {
        PublicationType publicationType = new PublicationType("wptNews")
        NewspaperSpreadsheet wptNewsSpreadsheet = NewspaperSpreadsheet.defaultInstance(publicationType.PATH_TO_SPREADSHEET)

        assertTrue("Spreadsheet is valid", wptNewsSpreadsheet.spreadsheet.isValid(false, false))
        List<Map<String, String>> mapsForWptNewsList =
                wptNewsSpreadsheet.spreadsheet.mapsForColumn(NewspaperSpreadsheet.MMSID_COLUMN_NAME,
                        "9918190341702836")

        assertThat("The Westport news has 1 entry", mapsForWptNewsList.size(), is(new Integer(1)))
        Map<String, String> mapsForAlliedPress = mapsForWptNewsList.first()
        assertThat("'title_parent' is 'The news Westport'", mapsForAlliedPress.get("title_parent"), is("The news Westport"))
        assertThat("'MMSID' is 9918190341702836", mapsForAlliedPress.get("MMSID"), is("9918190341702836"))
        assertThat("'title_code' is 'WptNews'", mapsForAlliedPress.get("title_code"), is("WptNews"))
        assertFalse("isMagazine is false for The Westport news",
                NewspaperSpreadsheet.extractBooleanValue(mapsForAlliedPress, NewspaperSpreadsheet.IS_MAGAZINE_KEY))
    }
}