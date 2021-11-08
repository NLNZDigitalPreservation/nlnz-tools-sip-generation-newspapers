package nz.govt.natlib.tools.sip.generation.fairfax

import static org.hamcrest.core.Is.is
import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertThat
import static org.junit.Assert.assertTrue

import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

/**
 * Tests the {@link FairfaxSpreadsheet}.
 */
@RunWith(MockitoJUnitRunner.class)
class FairfaxSpreadsheetTest {

    @Test
    void loadsTheWMMASpreadsheetCorrectly() {
        PublicationType publicationType = new PublicationType("WMMA")
        FairfaxSpreadsheet fairfaxSpreadsheet = FairfaxSpreadsheet.defaultInstance(publicationType.getPATH_TO_SPREADSHEET())

        assertTrue("Spreadsheet is valid", fairfaxSpreadsheet.spreadsheet.isValid(false, false))
        List<Map<String, String>> mapsForWairarapaTimesList =
                fairfaxSpreadsheet.spreadsheet.mapsForColumn(FairfaxSpreadsheet.MMSID_COLUMN_NAME,
                        "9918822769202836")

        assertThat("Wairarapa Times only has one entry", mapsForWairarapaTimesList.size(), is(new Integer(1)))
        Map<String, String> mapsForWairarapaTimes = mapsForWairarapaTimesList.first()
        assertThat("'title_parent' is 'Wairarapa times-age.'", mapsForWairarapaTimes.get("title_parent"), is("Wairarapa times-age."))
        assertThat("'MMSID' is 9918822769202836", mapsForWairarapaTimes.get("MMSID"), is("9918822769202836"))
        assertThat("'title_code' is 'WMMA'", mapsForWairarapaTimes.get("title_code"), is("WMMA"))
        assertFalse("isMagazine is false for WairarapaTimes",
                FairfaxSpreadsheet.extractBooleanValue(mapsForWairarapaTimes, FairfaxSpreadsheet.IS_MAGAZINE_KEY))
    }

    @Test
    void loadsTheAlliedPressSpreadsheetCorrectly() {
        PublicationType publicationType = new PublicationType("alliedPress")
        FairfaxSpreadsheet fairfaxSpreadsheet = FairfaxSpreadsheet.defaultInstance(publicationType.getPATH_TO_SPREADSHEET())

        assertTrue("Spreadsheet is valid", fairfaxSpreadsheet.spreadsheet.isValid(false, false))
        List<Map<String, String>> mapsForAlliedPressList =
                fairfaxSpreadsheet.spreadsheet.mapsForColumn(FairfaxSpreadsheet.MMSID_COLUMN_NAME,
                        "9918591570102836")

        assertThat("The Ashburton courier has 1 entry", mapsForAlliedPressList.size(), is(new Integer(1)))
        Map<String, String> mapsForAlliedPress = mapsForAlliedPressList.first()
        assertThat("'title_parent' is 'The Ashburton courier'", mapsForAlliedPress.get("title_parent"), is("The Ashburton courier"))
        assertThat("'MMSID' is 9918591570102836", mapsForAlliedPress.get("MMSID"), is("9918591570102836"))
        assertThat("'title_code' is 'AshburtonCourier'", mapsForAlliedPress.get("title_code"), is("AshburtonCourier"))
        assertFalse("isMagazine is false for The Ashburton courier",
                FairfaxSpreadsheet.extractBooleanValue(mapsForAlliedPress, FairfaxSpreadsheet.IS_MAGAZINE_KEY))
    }
}
