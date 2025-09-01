import com.ssblur.mtp.components.Book
import com.ssblur.mtp.components.Category
import components.Entry
import processor.*
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.util.Scanner
import javax.imageio.ImageIO
import kotlin.io.path.absolute
import kotlin.io.path.exists
import kotlin.math.pow
import kotlin.math.roundToInt

class Main(private val namespace: String, private val bookId: String, private val path: String, private val outputPath: String) {
    private val basePath = File(path)
    private val list: ArrayList<Entry> = ArrayList()
    private val guiDir = File(outputPath)
        .resolve("assets")
        .resolve(namespace)
        .resolve("textures")
        .resolve("gui");

    private val processors = arrayOf(
        TitleMatch(),
        ImageMatch(namespace),
        LinkMatch(),
        ExactMatch("", "\$(br)"),
        RegexMatch("(^|^ )[*-] (.*)", "\\\$(li)$2\\\$()"), // start of str or start and space then * then space and text
        RegexMatch("(^|^ )[*-] [*-] (.*)", "\\\$(li2)$2\\\$()"),
        RegexMatch("(^|^ )[*-] [*-] [*-] (.*)", "\\\$(li3)$2\\\$()"),
        RegexMatch("(^|^ )[*-] [*-] [*-] [*-] (.*)", "\\\$(li4)$2\\\$()"),
        RegexMatch("\\*\\*(.*?)\\*\\*", "\\\$(l)$1\\\$()"),
        RegexMatch("(\\s|^)__(.*?)__(\\s|\$)", "\\\$(l)$2\\\$()"), // only between __ when on left nothing or whitespace, on right nothing or whitespace
        RegexMatch("\\*(.*?)\\*", "\\\$(o)$1\\\$()"),
        RegexMatch("(\\s|^)_(.*?)_(\\s|\$)", "\\\$(o)$2\\\$()"), // only between _ when on left nothing or whitespace, on right nothing or whitespace
        RegexMatch("~~(.*?)~~", "\\\$(s)$1\\\$()"),
    )

    private val imageExtensions = arrayOf(
        "png",
        "jpg",
        "jpeg"
    )
    private val transparent = Color(0f, 0f, 0f, 0f)

    fun generate() {
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
                    .resolve("en_us")
                    .resolve("categories")
                    .resolve(entry.category.lowercase().replace(Regex("^" + namespace + ":"), "") + ".json")
                Files.createDirectories(Paths.get(file.parent))
                file.writeText(Category.fromEntry(entry).serialize()!!)

                file = path
                    .resolve("en_us")
                    .resolve("entries")
                    .resolve(entry.id.lowercase() + ".json")
                Files.createDirectories(Paths.get(file.parent))
                entry.priority = true
                file.writeText(entry.serialize()!!)
            } else {
                val file = path
                    .resolve("en_us")
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
                .replace(Regex("^" + Regex.escape(File.separator)), "")
                .replace(
                    Regex(Regex.escape("." + input.extension) + "$"),
                    ""
                )

        )
        val category = File(stripped).parent.trimStart(File.separator[0])
        entry.category = "$namespace:" +
                category.ifEmpty { "main" }

        for (line in input.readLines()) {
            var result = line
            for (p in processors)
                result = p.process(result, entry).trimEnd() + " "
            entry.addText(result)
        }

        entry.finalize()
        return entry
    }

    private fun copyToAssets(path: String) {
        val file = File(path)
        val stripped = Regex("^" + Regex.escape(basePath.absolutePath + File.separator) + "(.*)")
            .replace(file.absolutePath, "$1")
        val out = guiDir.resolve(stripped)

        if(!out.parentFile.exists())
            out.parentFile.mkdirs()

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
            file.copyTo(out.absoluteFile)
    }

    fun processDir(dir: String? = null) {
        val input = if (dir != null)
            File(dir)
        else
            basePath

        for (file in input.listFiles()!!) {
            if (file.isDirectory)
                processDir(file.absolutePath)
            else if (file.isFile)
                if (file.extension.lowercase() == "md")
                    list.add(processFile(file.absolutePath))
                else
                    copyToAssets(file.absolutePath)
        }
    }

    fun debug() {

    }
}