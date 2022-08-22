package components

import org.gradle.internal.impldep.com.fasterxml.jackson.core.JsonGenerator
import org.gradle.internal.impldep.com.fasterxml.jackson.databind.JsonSerializable
import org.gradle.internal.impldep.com.fasterxml.jackson.databind.SerializerProvider
import org.gradle.internal.impldep.com.fasterxml.jackson.databind.jsontype.TypeSerializer
import java.util.Dictionary

class Page {
    var text = ""
    var type = "patchouli:text"
    var title = ""
    var images: Array<String> = arrayOf()
    var extra: HashMap<String, String> = HashMap()

    override fun toString(): String {
        return "\"$title\" (\"$type\")"
    }
}