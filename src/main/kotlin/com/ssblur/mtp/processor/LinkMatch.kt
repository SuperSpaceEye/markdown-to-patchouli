package processor

import components.Entry

class LinkMatch() : Processor {
    private val pattern = Regex("\\[(.*?)]\\((.*?)\\)")
    private val extensionPattern = Regex("\\.[mM][dD]$")
    private val commandRegex = Regex("^#command:")
    private val tooltipRegex = Regex("^#tooltip:")

    override fun process(line: String, entry: Entry): String {
        var out = line
        var results = pattern.find(out)
        while (results != null) {
            try {
                val text = results.groups[1]!!.value
                var link = results.groups[2]!!.value

                if (link.startsWith("#command:")) {
                    link = link.replace(commandRegex, "")
                    out = out.replaceFirst(pattern, "\\\$(c:/$link)$text\\\$(/c)")
                    results = pattern.find(out)
                    continue
                } else if (link.startsWith("#tooltip:")) {
                    link = link.replace(tooltipRegex, "")
                    out = out.replaceFirst(pattern, "\\\$(t:/$link)$text\\\$(/t)")
                    results = pattern.find(out)
                    continue
                }

                if (!link.startsWith("https://") || !link.startsWith("http://"))
                    link = link.lowercase().replace(extensionPattern, "")

                out = out.replaceFirst(pattern, "\\\$(l:$link)$text\\\$(/l)")
                results = pattern.find(out)
            } catch (_: NullPointerException) {
                return out
            }
        }

        return out
    }
}