package co.tcore.glitchhex

import java.io.IOException
import java.nio.file.Files
import java.io.File
import java.lang.System.out


object FileUtil {

    /**
     * Identify file type of file with provided path and name
     * using JDK 7's Files.probeContentType(Path).
     *
     * @param fileName Name of file whose type is desired.
     * @return String representing identified type of file with provided name.
     */

    fun identifyFileType(file: File): String? {

        var fileType: String?

        try {

            fileType = Files.probeContentType(file.toPath())

        } catch (ioException: IOException) {

            out.println(
                    "ERROR: Unable to determine file type for " + file.absolutePath
                            + " due to exception " + ioException)
            throw ioException

        }

        return fileType

    }

}