package nz.govt.natlib.tools.sip.generation.newspapers.parameters

import nz.govt.natlib.tools.sip.state.SipProcessingException
import org.junit.Test

import static org.hamcrest.core.Is.is
import static org.junit.Assert.assertThat

class ProcessingOptionTest {

    @Test
    void correctlyMergesOverridesWhenOverrideExists() {
        List<ProcessingOption> current = [ ProcessingOption.AlphaBeforeNumericSequencing ]
        List<ProcessingOption> overrides = [ ProcessingOption.NumericBeforeAlphaSequencing ]
        List<ProcessingOption> merged = ProcessingOption.mergeOverrides(current, overrides)

        List<ProcessingOption> expected = [ ProcessingOption.NumericBeforeAlphaSequencing ]
        assertThat("ProcessingOption merges correctly", merged, is(expected))

        List<ProcessingOption> current1 = [ ProcessingOption.FullDateInSip ]
        List<ProcessingOption> overrides1 = [ ProcessingOption.IssueOnlyInSip ]
        List<ProcessingOption> merged1 = ProcessingOption.mergeOverrides(current1, overrides1)

        List<ProcessingOption> expected1 = [ ProcessingOption.IssueOnlyInSip ]
        assertThat("ProcessingOption merges correctly", merged1, is(expected1))
    }

    @Test
    void correctlyMergesOverridesWhenOverrideDoesNotExist() {
        List<ProcessingOption> current = [ ]
        List<ProcessingOption> overrides = [ ProcessingOption.NumericBeforeAlphaSequencing ]
        List<ProcessingOption> merged = ProcessingOption.mergeOverrides(current, overrides)

        List<ProcessingOption> expected = [ ProcessingOption.NumericBeforeAlphaSequencing ]
        assertThat("ProcessingOption merges correctly", merged, is(expected))
    }

    @Test
    void extractsCorrectlyWithoutDefaults() {
        List<ProcessingOption> options = ProcessingOption.extract("alpha_before_numeric",
                ",", [ ], true)
        List<ProcessingOption> expected = [ ProcessingOption.AlphaBeforeNumericSequencing ]
        assertThat("ProcessingOption extracted correctly", options, is(expected))
    }

    @Test
    void extractsCorrectlyWithDefaults() {
        List<ProcessingOption> options = ProcessingOption.extract("",
                ",", [ ProcessingOption.NumericBeforeAlphaSequencing ], true)
        List<ProcessingOption> expected = [ ProcessingOption.NumericBeforeAlphaSequencing ]
        assertThat("ProcessingOption extracted correctly", options, is(expected))
    }

    @Test
    void extractsCorrectlyWithEmptyListAndDefaults() {
        List<ProcessingOption> options = ProcessingOption.extract("",
                ",", [ ProcessingOption.NumericBeforeAlphaSequencing ], true)
        List<ProcessingOption> expected = [ ProcessingOption.NumericBeforeAlphaSequencing ]
        assertThat("ProcessingOption extracted correctly", options, is(expected))
    }

    @Test(expected = SipProcessingException.class)
    void throwsExceptionWithUnrecognizedRule() {
        List<ProcessingOption> options = ProcessingOption.extract("alpha_before_numeric,unrecognized_rule", ",", [ ], true)
        assertThat("This point should not be reached", options, is([ ProcessingOption.AlphaBeforeNumericSequencing]))
    }

}
