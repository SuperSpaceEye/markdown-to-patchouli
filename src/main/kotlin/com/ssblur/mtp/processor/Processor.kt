package processor

import components.Entry

interface Processor {
    fun process(line: String): String = throw NotImplementedError()
    fun process(line: String, entry: Entry): String = process(line)
}