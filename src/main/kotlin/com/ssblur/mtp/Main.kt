import com.ssblur.mtp.components.Book
import com.ssblur.mtp.components.Category
import com.ssblur.mtp.processor.ModifierMatch
import components.Entry
import processor.*
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import javax.imageio.ImageIO

class Main(private val namespace: String, private val bookId: String, private val path: String, private val outputPath: String) {
    private val basePath = File(path)
    private val list: ArrayList<Entry> = ArrayList()
    private val imagesDir = File(outputPath)
        .resolve("assets")
        .resolve(namespace)
        .resolve("patchouli_assets")

    private val modifierMatch = ModifierMatch()

    private val processedMiscPaths = mutableSetOf<String>()
    private val toProcess = mutableListOf<() -> Unit>()

    private val processors = arrayOf(
        TitleMatch(),
        ImageMatch(namespace),
        LinkMatch(),
        ExactMatch("", "\$(br)"),
        //i'm adding spaces so that matches could be correctly applied
        RegexMatch("(^|^ )[*-] [*-] [*-] [*-] (.*)", "\\\$(li4) $2 "),
        RegexMatch("(^|^ )[*-] [*-] [*-] (.*)", "\\\$(li3) $2 "),
        RegexMatch("(^|^ )[*-] [*-] (.*)", "\\\$(li2) $2 "),
        RegexMatch("(^|^ )[*-] (.*)", "\\\$(li) $2 "), // start of str or start and space then * then space and text
        RegexMatch("\\*\\*(.*?)\\*\\*", "\\\$(l) $1 \\\$()"),
        RegexMatch("\\*(.*?)\\*", "\\\$(o) $1 \\\$()"),
        RegexMatch("(\\s|^)__(.*?)__(\\s|\$)", "\\\$(l) $2 \\\$()"), // only between __ when on left nothing or whitespace, on right nothing or whitespace
        RegexMatch("(\\s|^)_(.*?)_(\\s|\$)", "\\\$(o) $2 \\\$()"), // only between _ when on left nothing or whitespace, on right nothing or whitespace
        RegexMatch("~~(.*?)~~", "\\\$(s) $1 \\\$()"),

        //$(whatever) should remove spaces on right of themselves, $() should on left, $(li|li2|li3|li4|br) everywhere around itself
        RegexMatch("(\\\$\\((l|o|s)\\))(\\s|)", "$1"),
        RegexMatch("(\\s|)(\\\$\\((li|li2|li3|li4|br)\\))(\\s|)", "$2"),
        RegexMatch("(\\s|)(\\\$\\(.*?\\))", "$2")
    )

    private val imageExtensions = arrayOf(
        "png",
        "jpg",
        "jpeg"
    )
    private val transparent = Color(0f, 0f, 0f, 0f)

    fun generate() {
        toProcess.forEach { it.invoke() }
        generateData()
    }

    private fun generateData() {
        val bookPath = File(outputPath)
            .resolve("data")
            .resolve(namespace)
            .resolve("patchouli_books")
            .resolve(bookId)
        bookPath.deleteRecursively()
        bookPath.mkdir()
        val path = File(outputPath)
            .resolve("assets")
            .resolve(namespace)
            .resolve("patchouli_books")
            .resolve(bookId)
        path.deleteRecursively()
        path.mkdir()
        for (entry in list) {
            if (entry.id.lowercase() == "readme") {
                val file = bookPath.resolve("book.json")
                Files.createDirectories(Paths.get(file.parent))
                file.writeText(Book.fromEntry(entry).serialize()!!)
            } else if (entry.id.lowercase().endsWith("readme")) {
                var file = path
                    .resolve(entry.locale)
                    .resolve("categories")
                    .resolve(entry.category.lowercase().replace(Regex("^" + namespace + ":"), "") + ".json")
                Files.createDirectories(Paths.get(file.parent))
                file.writeText(Category.fromEntry(entry).serialize()!!)
            } else {
                val file = path
                    .resolve(entry.locale)
                    .resolve("entries")
                    .resolve(entry.id.lowercase() + ".json")
                Files.createDirectories(Paths.get(file.parent))
                file.writeText(entry.serialize()!!)
            }
        }
    }

    fun processFile(dir: String): Entry {
        val input = File(dir)
        val stripped = Regex("^" + Regex.escape(basePath.absolutePath) + "(.*)").replace(input.absolutePath, "$1")
        val entry = Entry(
            stripped
                .replaceFirst(Regex("${Regex.escape(File.separator)}((.*?)${Regex.escape(File.separator)})"), "")
                .replace(Regex("^" + Regex.escape(File.separator)), "")
                .replace(
                    Regex(Regex.escape("." + input.extension) + "$"),
                    ""
                )

        )
        val localeAndCategory = File(stripped).parentFile
        entry.locale = try {localeAndCategory.parentFile.name} catch (e: Exception) {"none"}
        entry.data["category"] = "$namespace:" + localeAndCategory.name.ifEmpty { "main" }

        for (line in input.readLines()) {
            var result = line
            result = modifierMatch.process(result, entry, processedMiscPaths).trimEnd()
            //if there were only modifiers on the line, then do not save empty space
            if (result.isEmpty() && result != line) continue
            for (p in processors) {
                result = p.process(result, entry, processedMiscPaths).trimEnd() + " "
            }
            entry.addText(result)
        }

        entry.finalize()
        return entry
    }

    private fun copyToAssets(path: String) {
        val file = File(path)
        val stripped = Regex("^" + Regex.escape(basePath.absolutePath + File.separator) + "(.*)").replace(file.absolutePath, "$1")
        val out = imagesDir.resolve(stripped)

        if(!out.parentFile.exists())
            out.parentFile.mkdirs()

        processedMiscPaths.add(stripped)
        if (out.extension == "gif") {
            file.copyTo(out.absoluteFile, true)
        }

        if (out.extension in imageExtensions) {
            val original = ImageIO.read(file)
            var width = original.width
            var height = original.height
            if(width > height) {
                height = ((200 / width.toDouble()) * height.toDouble()).toInt()
                width = 200
            } else {
                width = ((200 / height.toDouble()) * width.toDouble()).toInt()
                height = 200
            }

            val resized = BufferedImage(256, 256, BufferedImage.TRANSLUCENT)
            val graphics2D = resized.createGraphics()
            graphics2D.background = transparent
            graphics2D.drawImage(
                original,
                100 - width / 2,
                100 - height / 2,
                width,
                height,
                null
            )
            graphics2D.dispose()
            if(out.exists())
                out.delete()
            ImageIO.write(resized, file.extension, out.absoluteFile)
        } else if (!out.exists())
            file.copyTo(out.absoluteFile, true)
    }

    fun processDir(dir: String? = null) {
        val input = if (dir != null) {
            File(dir)
        } else {
            imagesDir.deleteRecursively()
            imagesDir.mkdir()
            basePath
        }

        for (file in input.listFiles()!!) {
            if (file.isDirectory)
                processDir(file.absolutePath)
            else if (file.isFile)
                if (file.extension.lowercase() == "md")
                    toProcess.add {
                        list.add(processFile(file.absolutePath))
                    }
                else
                    copyToAssets(file.absolutePath)
        }
    }

    fun debug() {

    }
}