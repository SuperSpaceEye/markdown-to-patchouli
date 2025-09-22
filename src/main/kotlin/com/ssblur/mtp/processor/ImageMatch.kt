package processor

import components.Entry

class ImageMatch(val namespace: String) : Processor {
    private val pattern = Regex("!\\[(.*?)]\\((.*?)\\)\\s?#?\\s?\"?(.*?)\"?")
    private val whitespace = Regex("\\s")

    override fun process(line: String, entry: Entry, processedMiscData: Set<String>): String {
        var out = line
        var results = pattern.find(out)
        while (results != null) {
            try {
                var subPath = "${entry.category.replace("$namespace:", "")}/${results.groups[2]!!.value}"
                var path = "${entry.locale}/${subPath}"
                if (!processedMiscData.contains(path)) {
                    path = "en_us/${subPath}"
                }

                val text = results.groups[1]!!.value
                var link = "$namespace:patchouli_assets/${path}"

                val lastPage = entry.lastPage()
                val page = if (lastPage.type == "patchouli:text" && lastPage.text.replace("\$(br)", "").replace(whitespace, "").isEmpty()) {
                    lastPage
                } else {
                    entry.newPageIfNotEmpty()
                }

                //.gif is vmod specific
                page.type = if (results.groups[2]!!.value.endsWith(".gif")) {"the_vmod:gif_page"} else {"patchouli:image"}
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