package com.ssblur.mtp.components

import components.Entry
import org.gradle.internal.impldep.com.google.gson.GsonBuilder
import kotlin.reflect.KClass

class Book(name: String) {
    val data = mutableMapOf<String, Any?>(
        "name" to name,
        "landing_text" to "",
        "book_texture" to "patchouli:textures/gui/book_brown.png",
        "use_resource_pack" to true
    )

    fun serialize(): String? {
        return GsonBuilder().setPrettyPrinting().create().toJson(data)
    }

    companion object {
        val allowedModifiers = mutableMapOf<String, KClass<*>>(
            "book_texture" to String::class,
            "filler_texture" to String::class,
            "crafting_texture" to String::class,
            "model" to String::class,
            "text_color" to String::class,
            "header_color" to String::class,
            "namespace_color" to String::class,
            "link_color" to String::class,
            "link_hover_color" to String::class,
            "progress_bar_color" to String::class,
            "progress_bar_background" to String::class,
            "open_sound" to String::class,
            "flip_sound" to String::class,
            "index_icon" to String::class,
            "pamphlet" to java.lang.Boolean::class,
            "show_progress" to java.lang.Boolean::class,
            "version" to Any::class,
            "subtitle" to String::class,
            "creative_tab" to String::class,
            "advancements_tab" to String::class,
            "dont_generate_book" to java.lang.Boolean::class,
            "custom_book_item" to String::class,
            "show_toasts" to java.lang.Boolean::class,
            "use_blocky_font" to java.lang.Boolean::class,
            "i18n" to java.lang.Boolean::class,
            "macros" to Any::class,
            "pause_game" to java.lang.Boolean::class,
        )
        private const val br = "\\\$\\(br\\)"
        private val pattern = Regex("^(\\s*$br\\s*)*(.*?)$br.*$")
        fun fromEntry(entry: Entry): Book {
            val book = Book(entry.name)

            entry.unknownModifiers.forEach { (k, v) ->
                if (!allowedModifiers.contains(k)) return@forEach println("Unknown modifier $k is ignored")
                if (!allowedModifiers[k]!!.java.isAssignableFrom(v::class.java)) throw AssertionError("Modifier $k is ${v::class.simpleName}, expected ${allowedModifiers[k]!!.simpleName}")
                book.data[k] = v
            }

            val description = entry.pages[0].text.replace(pattern, "$2").trim()
            book.data["landing_text"] = description

            return book
        }
    }
}