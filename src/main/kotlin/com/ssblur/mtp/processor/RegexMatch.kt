package processor

import components.Entry

class RegexMatch(private val input: String, private val replace: String): Processor {
    private val pattern = Regex(input)

    override fun process(line: String, entry: Entry): String {
        return pattern.replace(line, replace)
    }
}