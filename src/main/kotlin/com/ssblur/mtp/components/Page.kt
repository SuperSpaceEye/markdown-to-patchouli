package components

class Page {
    var text = ""
    var type = "patchouli:text"
    var title = ""
    var images: Array<String> = arrayOf()

    override fun toString(): String {
        return "\"$title\" (\"$type\")"
    }
}