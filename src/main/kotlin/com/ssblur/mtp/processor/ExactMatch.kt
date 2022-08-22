package processor

import components.Entry

class ExactMatch(private val input: String, private val output: String): Processor {
    override fun process(line: String, entry: Entry): String {
        if(line.trim() == input)
            return output
        return line
    }
}