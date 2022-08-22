package processor

import components.Entry
import java.io.File

class ImageMatch(val namespace: String) : Processor {
    private val pattern = Regex("!\\[(.*?)]\\((.*?)\\)\\s?#?\\s?\"?(.*?)\"?")

    override fun process(line: String, entry: Entry): String {
        var out = line
        var results = pattern.find(out)
        while (results != null) {
            try {
                val text = results.groups[1]!!.value
                var link = "$namespace:textures/gui/${entry.category.replace("$namespace:", "")}/"  + results.groups[2]!!.value

                val page = entry.newPageIfNotEmpty()
                page.type = "patchouli:image"
                page.images += link
                page.text = text
                entry.newPage()
                out = out.replace(pattern, "")
                results = pattern.find(out)
            } catch (_: NullPointerException) {
                return out
            }
        }

        return out
    }
}