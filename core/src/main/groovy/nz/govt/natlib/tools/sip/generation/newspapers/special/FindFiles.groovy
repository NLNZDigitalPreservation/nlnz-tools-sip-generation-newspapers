package nz.govt.natlib.tools.sip.generation.newspapers.special

import groovy.util.logging.Log4j2
import nz.govt.natlib.tools.sip.logging.Timekeeper
import nz.govt.natlib.tools.sip.utils.GeneralUtils

import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.PathMatcher
import java.nio.file.Paths

@Log4j2
class FindFiles {
    static List<Path> findFiles(String localPath, boolean isRegexNotGlob, String pattern, Timekeeper timekeeper) {
        List<Path> filesList = [ ]
        Path filesPath = Paths.get(localPath)
        if (!Files.exists(filesPath) || Files.isRegularFile(filesPath)) {
            log.warn("Path '${filesPath}' does not exist is not a directory. Returning empty file list.")
            return filesList
        }

        String message = "Finding files for path=${filesPath.normalize()} and pattern=${pattern}"
        log.info(message)
        if (timekeeper != null) {
            timekeeper.logElapsed(false)
        }

        filesList = getMatchingFiles(filesPath, isRegexNotGlob, pattern)

        message = "Found total files=${GeneralUtils.TOTAL_FORMAT.format(filesList.size())} for path=${filesPath.normalize()}"
        log.info(message)
        if (timekeeper != null) {
            timekeeper.logElapsed(false)
        }
        return filesList
    }

    static List<Path> getMatchingFiles(Path filesPath, boolean isRegexNotGlob, String pattern) {
        FileSystem fileSystem = FileSystems.getDefault()
        String matcherType = isRegexNotGlob ? "regex:" : "glob:"
        final PathMatcher pathMatcher = fileSystem.getPathMatcher(matcherType + pattern)

        List<Path> matchingFiles = [ ]
        File dir = new File(filesPath as String)

        for (File file : dir.listFiles()) {
            try {
                if (!file.isDirectory()) {
                    if (pathMatcher.matches(file.toPath().getFileName())) {
                        matchingFiles.add(file.toPath())
                    }
                }
            } catch (e) {
                e.printStackTrace()
                throw e
            }
        }

        return matchingFiles.toSorted { Path a, Path b -> a.normalize() <=> b.normalize() }
    }
}
