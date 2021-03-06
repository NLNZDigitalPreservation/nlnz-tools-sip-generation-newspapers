package nz.govt.natlib.tools.sip.generation.newspapers.special

import nz.govt.natlib.tools.sip.utils.PathUtils

import java.nio.file.Path

class PageUnavailableWriter {
    static final String PAGE_NOT_AVAILABLE_PDF_RESOURCE = "page-unavailable.pdf"
    static final String TEMPORARY_DIRECTORY_PREFIX = "For-Page-Unavailable-Pdf-File_"

    static Path writeToToTemporaryDirectory(String filename = PAGE_NOT_AVAILABLE_PDF_RESOURCE,
                                            Path parentDirectory = null) {
        String resourcePath = ""
        boolean deleteOnExit = true
        Path tempFile = PathUtils.writeResourceToTemporaryDirectory(filename, TEMPORARY_DIRECTORY_PREFIX, resourcePath,
                PAGE_NOT_AVAILABLE_PDF_RESOURCE, parentDirectory, deleteOnExit)

        return tempFile
    }
}
