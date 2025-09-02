package processor

class RegexMatch(private val input: String, private val replace: String): Processor {
    private val pattern = Regex(input)

    override fun process(line: String): String {
        return pattern.replace(line, replace)
    }
}