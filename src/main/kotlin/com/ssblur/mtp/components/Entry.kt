package components

import org.gradle.internal.impldep.com.google.gson.GsonBuilder
import processor.RegexMatch

class Entry(id: String) {
    val pages = mutableListOf(Page())

    val data = mutableMapOf<String, Any>(
        "id" to id,
        "icon" to "minecraft:book",
    )

    var id: String get() = data["id"] as String; set(value) {data["id"] = value}
    var name: String get() = data["name"] as String; set(value) {data["name"] = value}
    var icon: String get() = data["icon"] as String; set(value) {data["icon"] = value}
    var category: String get() = data["category"] as String; set(value) {data["category"] = value}

    val unknownModifiers = mutableMapOf<String, Any>()
    var locale = "en_us"

    fun newPage(): Page {
        pages += Page()
        return lastPage()
    }

    fun newPageIfNotEmpty(): Page {
        return if(lastPage().text.isNotEmpty() || lastPage().title.isNotEmpty() || lastPage().images.isNotEmpty()) {
            lastPage().text = textReduce(lastPage().text)
            newPage()
        } else {
            lastPage()
        }
    }

    fun lastPage(): Page {
        return pages.last()
    }

    fun findSplit(text: String): Int {
        val offset = lastPage().text.length
        if((text.length + offset) <= pageSize) return 0

        val max = pageSize - offset
        if(max <= 0) return 0
        for(index in IntRange(0, max).reversed())
            if(text[index].isWhitespace())
                return index
        return max
    }

    fun canFitWhole(text: String) = text.length <= pageSize

    fun addText(text: String) {
        if (text.isEmpty()) return

        val split = findSplit(text)
        if(split == 0) {
            lastPage().text += text
            lastPage().text = textReduce(lastPage().text)
        } else if (canFitWhole(text)) {
            lastPage().text = textReduce(lastPage().text)
            newPage()
            addText(text)
        } else {
            lastPage().text += text.substring(IntRange(0, split))
            lastPage().text = textReduce(lastPage().text)

            newPage()
            addText(text.substring(split))
        }
    }

    fun finalize() {
        if (data["name"] == null || name.isEmpty()) {
            data["name"] = pages[0].title
        }

        for(page in pages) {
            page.text = page.text.replace(trimPattern, "")
        }
    }

    override fun toString(): String {
        return "${data["id"]}:\n" +
                "\tname: ${data["name"]}\n" +
                "\tcategory: ${data["category"]}\n" +
                "\ticon: ${data["icon"]}\n" +
                "\tpages: ${pages.joinToString(", ")}\n" +
                "\tadvancement: ${data["advancement"]}\n" +
                "\tflag: ${data["flag"]}\n" +
                "\tpriority: ${data["priority"]}\n" +
                "\tsecret: ${data["secret"]}\n" +
                "\tread_by_default: ${data["read_by_default"]}\n" +
                "\tsortnum: ${data["sortnum"]}\n" +
                "\tturnin: ${data["turnin"]}\n" +
                "\textra_recipe_mappings: ${data["extra_recipe_mappings"]}"
    }

    fun serialize(): String? {
        data["pages"] = pages
            //sometimes entries may be empty when i don't want it, not sure how
            .filter { !(it.type == "patchouli:text" && it.text.isEmpty() && it.title.isEmpty() && it.images.isEmpty()) }
            .map { it.serialize() }
        return GsonBuilder().setPrettyPrinting().create().toJson(data)
    }

    companion object {
        val trimPattern = Regex("^(\\s*\\\$\\(br\\)\\s*)*\\s*")
        val pageSize = 500

        val spaceReductionPatterns = listOf(
            RegexMatch("(\\\$\\((l|o|s)\\))(\\s|)", "$1"),
            RegexMatch("(\\s|)(\\\$\\((li|li2|li3|li4|br)\\))(\\s|)", "$2"),
            RegexMatch("(\\s|)(\\\$\\(.*?\\))", "$2")
        )

        fun textReduce(text: String): String {
            var text = text
            for (pattern in spaceReductionPatterns) {
                text = pattern.process(text)
            }
            return text
        }
    }
}