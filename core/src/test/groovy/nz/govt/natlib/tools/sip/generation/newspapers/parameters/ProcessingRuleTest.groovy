package nz.govt.natlib.tools.sip.generation.newspapers.parameters

import nz.govt.natlib.tools.sip.state.SipProcessingException
import org.junit.Test

import static org.hamcrest.core.Is.is
import static org.junit.Assert.assertThat

class ProcessingRuleTest {

    @Test
    void correctlyMergesOverridesWhenOverrideExists() {
        List<ProcessingRule> current = [ ProcessingRule.AllSectionsInSipRequired, ProcessingRule.HandleUnrecognised ]
        List<ProcessingRule> overrides = [ ProcessingRule.AllSectionsInSipOptional ]
        List<ProcessingRule> merged = ProcessingRule.mergeOverrides(current, overrides)

        List<ProcessingRule> expected = [ ProcessingRule.AllSectionsInSipOptional, ProcessingRule.HandleUnrecognised ]
        assertThat("ProcessingRule merges correctly", merged, is(expected))
    }

    @Test
    void correctlyMergesOverridesWhenOverrideDoesNotExist() {
        List<ProcessingRule> current = [ProcessingRule.HandleIgnored, ProcessingRule.HandleUnrecognised ]
        List<ProcessingRule> overrides = [ ProcessingRule.AllSectionsInSipOptional ]
        List<ProcessingRule> merged = ProcessingRule.mergeOverrides(current, overrides)

        List<ProcessingRule> expected = [ProcessingRule.HandleIgnored, ProcessingRule.HandleUnrecognised,
                                         ProcessingRule.AllSectionsInSipOptional ]
        assertThat("ProcessingRule merges correctly", merged, is(expected))
    }

    @Test
    void extractsCorrectlyWithoutDefaults() {
        List<ProcessingRule> rules = ProcessingRule.extract("handle_ignored,required_all_sections_in_sip",
                ",", [ ], true)
        List<ProcessingRule> expected = [ProcessingRule.HandleIgnored, ProcessingRule.AllSectionsInSipRequired ]
        assertThat("ProcessingRule extracted without defaults correctly", rules, is(expected))
    }

    @Test
    void extractsCorrectlyWithDefaults() {
        List<ProcessingRule> rules = ProcessingRule.extract("optional_all_sections_in_sip,handle_ignored",
                ",", [ ProcessingRule.AllSectionsInSipRequired, ProcessingRule.HandleInvalid ], true)
        List<ProcessingRule> expected = [ ProcessingRule.AllSectionsInSipOptional, ProcessingRule.HandleInvalid,
                                          ProcessingRule.HandleIgnored ]
        assertThat("ProcessingRule extracted with defaults correctly", rules, is(expected))
    }

    @Test
    void extractsCorrectlyWithEmptyListAndDefaults() {
        List<ProcessingRule> rules = ProcessingRule.extract("",
                ",", [ ProcessingRule.AllSectionsInSipRequired, ProcessingRule.HandleInvalid ], true)
        List<ProcessingRule> expected = [ ProcessingRule.AllSectionsInSipRequired, ProcessingRule.HandleInvalid ]
        assertThat("ProcessingRule extracted with defaults correctly", rules, is(expected))
    }

    @Test(expected = SipProcessingException.class)
    void throwsExceptionWithUnrecognizedRule() {
        List<ProcessingRule> rules = ProcessingRule.extract("required_all_sections_in_sip,unrecognized_rule", ",", [ ],true)
        assertThat("This point should not be reached", rules, is([ ProcessingRule.AllSectionsInSipRequired]))
    }
}
