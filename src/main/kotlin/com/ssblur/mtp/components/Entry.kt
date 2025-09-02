package components

import org.gradle.internal.impldep.com.google.gson.GsonBuilder
import processor.RegexMatch

class Entry(var id: String) {
    var name = ""
    var category = ""
    var icon = "minecraft:book"
    var pages: Array<Page> = arrayOf(Page())
    var flag = ""
    var advancement: String? = null
    var priority = false
    var secret = false
    var readByDefault = false
    var sortnum = 0
    var turnin: String? = null
    var extraRecipeMappings = object {}

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
        name = pages[0].title
        for(page in pages)
            page.text = page.text.replace(trimPattern, "")
    }

    override fun toString(): String {
        return "$id:\n" +
                "\tname: $name\n" +
                "\tcategory: $category\n" +
                "\ticon: $icon\n" +
                "\tpages: ${pages.joinToString(", ")}\n" +
                "\tadvancement: $advancement\n" +
                "\tflag: $flag\n" +
                "\tpriority: $priority\n" +
                "\tsecret: $secret\n" +
                "\tread_by_default: $readByDefault\n" +
                "\tsortnum: $sortnum\n" +
                "\tturnin: $turnin\n" +
                "\textra_recipe_mappings: $extraRecipeMappings"
    }

    fun serialize(): String? {
        return GsonBuilder().setPrettyPrinting().create().toJson(this)
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