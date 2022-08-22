package components

import org.gradle.internal.impldep.com.google.gson.Gson
import org.gradle.internal.impldep.com.google.gson.GsonBuilder
import org.gradle.internal.impldep.com.google.gson.JsonObject

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
        return if(lastPage().text.isEmpty() && lastPage().title.isEmpty() && lastPage().images.isNotEmpty())
            newPage()
        else
            lastPage()
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

    fun addText(text: String) {
        val split = findSplit(text)
        if(split == 0)
            lastPage().text += text
        else {
            lastPage().text += text.substring(IntRange(0, split))

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
        return Gson().toJson(this)
    }

    companion object {
        val trimPattern = Regex("^(\\s*\\\$\\(br\\)\\s*)*\\s*")
        val pageSize = 500
    }
}