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
        NewspaperType newspaperType = new NewspaperType("WMMA")
        NewspaperSpreadsheet newspaperSpreadsheet = NewspaperSpreadsheet.defaultInstance(newspaperType.PATH_TO_SPREADSHEET)

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
        NewspaperType newspaperType = new NewspaperType("alliedPress")
        NewspaperSpreadsheet newspaperSpreadsheet = NewspaperSpreadsheet.defaultInstance(newspaperType.PATH_TO_SPREADSHEET)

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
        NewspaperType newspaperType = new NewspaperType("wptNews")
        NewspaperSpreadsheet wptNewsSpreadsheet = NewspaperSpreadsheet.defaultInstance(newspaperType.PATH_TO_SPREADSHEET)

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

    @Test
    void loadsTheAreMediaSpreadsheetCorrectly() {
        NewspaperType newspaperType = new NewspaperType("areMedia")
        NewspaperSpreadsheet areMediaSpreadsheet = NewspaperSpreadsheet.defaultInstance(newspaperType.PATH_TO_SPREADSHEET)

        assertTrue("Spreadsheet is valid", areMediaSpreadsheet.spreadsheet.isValid(false, false))
        List<Map<String, String>> mapsForAreMediaList =
                areMediaSpreadsheet.spreadsheet.mapsForColumn(NewspaperSpreadsheet.MMSID_COLUMN_NAME,
                        "9918967969402836")

        assertThat("Woman's Day has 1 entry", mapsForAreMediaList.size(), is(new Integer(1)))
        Map<String, String> mapsForAreMedia = mapsForAreMediaList.first()
        assertThat("'title_parent' is 'Woman's day'", mapsForAreMedia.get("title_parent"), is("Woman's day"))
        assertThat("'MMSID' is 9918967969402836", mapsForAreMedia.get("MMSID"), is("9918967969402836"))
        assertThat("'title_code' is 'DZ'", mapsForAreMedia.get("title_code"), is("DZ"))
        assertTrue("isMagazine is true for Woman's Day",
                NewspaperSpreadsheet.extractBooleanValue(mapsForAreMedia, NewspaperSpreadsheet.IS_MAGAZINE_KEY))
    }

    @Test
    void loadsTheStuffSpreadsheetCorrectly() {
        NewspaperType newspaperType = new NewspaperType("stuff")
        NewspaperSpreadsheet stuffSpreadsheet = NewspaperSpreadsheet.defaultInstance(newspaperType.PATH_TO_SPREADSHEET)

        assertTrue("Spreadsheet is valid", stuffSpreadsheet.spreadsheet.isValid(false, false))
        List<Map<String, String>> mapsForStuffList =
                stuffSpreadsheet.spreadsheet.mapsForColumn(NewspaperSpreadsheet.MMSID_COLUMN_NAME,
                        "9917982733502836")

        assertThat("Ag Trader has 1 entry", mapsForStuffList.size(), is(1))
        Map<String, String> mapsForStuff = mapsForStuffList.first()
        assertThat("'title_parent' is 'Ag Trader'", mapsForStuff.get("title_parent"), is("Ag Trader"))
        assertThat("'MMSID' is 9917982733502836", mapsForStuff.get("MMSID"), is("9917982733502836"))
        assertThat("'title_code' is 'AGT'", mapsForStuff.get("title_code"), is("AGT"))
        assertTrue("isMagazine is true for Ag Trader",
                NewspaperSpreadsheet.extractBooleanValue(mapsForStuff, NewspaperSpreadsheet.IS_MAGAZINE_KEY))
    }
}
