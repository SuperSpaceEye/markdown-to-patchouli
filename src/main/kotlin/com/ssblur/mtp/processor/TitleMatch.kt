package processor

import components.Entry

class TitleMatch(): Processor {
    private val pattern = Regex("^#+\\s*(.*)")

    override fun process(line: String, entry: Entry): String {
        if(pattern.matches(line)) {
            val page = entry.newPageIfNotEmpty()
            if(page.title.isEmpty())
                page.title = pattern.replace(line, "$1")
            return ""
        }
        return line
    }
}