package components

class Page {
    var text = ""
    var type = textType
    var title = ""
    var images: Array<String> = arrayOf()

    fun serialize(): Any {
        if (type == textType && title.isEmpty() && images.isEmpty()) {
            return text
        }
        val ret = mutableMapOf<String, Any>()
        ret["type"] = type
        if (text.isNotEmpty()) ret["text"] = text
        if (title.isNotEmpty()) ret["title"] = title
        if (images.isNotEmpty()) ret["images"] = images

        return ret
    }

    override fun toString(): String {
        return "\"$title\" (\"$type\")"
    }

    companion object {
        val textType = "patchouli:text"
    }
}