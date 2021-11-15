package nz.govt.natlib.tools.sip.generation.newspapers.processor

import nz.govt.natlib.tools.sip.generation.newspapers.processor.logging.ProcessorLoggingConfigurationFactory
import org.apache.logging.log4j.core.config.ConfigurationFactory

class ProcessorRunnerWrapper {

    static void main(String[] args) {
        ConfigurationFactory.setConfigurationFactory(new ProcessorLoggingConfigurationFactory())
        ProcessorRunner.main(args)
    }
}
