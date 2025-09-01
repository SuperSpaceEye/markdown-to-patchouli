package idk

import Main
import java.nio.file.Paths

fun main() {
    val inputPath = Paths.get("test_input/test_guide").toAbsolutePath().toString()
    val outputPath = Paths.get("test_output").toAbsolutePath().toString()

    val processor = Main("test_namespace", "test_bookid", inputPath, outputPath)
    processor.processDir()
    processor.generate()
}