package nz.govt.natlib.tools.sip.generation.newspapers.processor

import groovy.util.logging.Log4j2
import nz.govt.natlib.tools.sip.utils.PathUtils

import java.nio.file.Path

@Log4j2
class ProcessorUtils {
    static String filePathAsSafeString(Path filePath, List<ProcessorOption> options = [ ]) {
        ProcessorOption option = ProcessorOption.showDirectoryOption(options, ProcessorOption.ShowFullPath)
        int totalSegements
        switch (option) {
            case ProcessorOption.ShowDirectoryOnly:
                totalSegements = 1
                break
            case ProcessorOption.ShowDirectoryAndOneParent:
                totalSegements = 2
                break
            case ProcessorOption.ShowDirectoryAndTwoParents:
                totalSegements = 3
                break
            case ProcessorOption.ShowDirectoryAndThreeParents:
                totalSegements = 4
                break
            default:
                // includes ProcessorOption.ShowFullPath
                totalSegements = -1
                break
        }
        return PathUtils.filePathAsSafeString(filePath, totalSegements)
    }

}
