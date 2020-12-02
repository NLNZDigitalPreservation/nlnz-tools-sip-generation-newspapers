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
    void loadsTheDefaultSpreadsheetCorrectly() {
        FairfaxSpreadsheet fairfaxSpreadsheet = FairfaxSpreadsheet.defaultInstance()

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
}
