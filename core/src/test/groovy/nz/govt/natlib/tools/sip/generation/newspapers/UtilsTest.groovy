package nz.govt.natlib.tools.sip.generation.newspapers

import nz.govt.natlib.tools.sip.generation.newspapers.special.ExtractValues
import org.junit.Test

import static org.hamcrest.core.Is.is
import static org.junit.Assert.assertThat

class UtilsTest {

    @Test
    void correctExtractsParametersFromString() {
        makeAndCheckExtraction("abc", [ "abc" ])
        makeAndCheckExtraction("+abc", [ "abc" ])
        makeAndCheckExtraction("+abc+", [ "abc" ])
        makeAndCheckExtraction("abc,", [ "abc" ])

        makeAndCheckExtraction("abc+def", [ "abc", "def" ])
        makeAndCheckExtraction("abc,def", [ "abc", "def" ])
        makeAndCheckExtraction("abc-def", [ "abc", "def" ])
        makeAndCheckExtraction("abc-def+ghi", [ "abc", "def", "ghi" ])
        makeAndCheckExtraction("abc-def,ghi", [ "abc", "def", "ghi" ])
        makeAndCheckExtraction("abc,def+ghi", [ "abc", "def", "ghi" ])

        makeAndCheckExtraction("", [ ])
        makeAndCheckExtraction(",", [ ])
        makeAndCheckExtraction("++", [ ])
        makeAndCheckExtraction("---", [ ])
    }

    void makeAndCheckExtraction(String candidateString, List<String> expectedExtraction) {
        assertThat("Expect extract of string='${candidateString}' is=${expectedExtraction}",
                ExtractValues.splitColumnValue(candidateString), is(expectedExtraction))
    }
}
