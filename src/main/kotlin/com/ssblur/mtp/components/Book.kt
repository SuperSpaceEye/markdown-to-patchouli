package com.ssblur.mtp.components

import components.Entry
import org.gradle.internal.impldep.com.google.gson.Gson

class Book(val name: String, val landing_text: String) {
    val book_texture = "patchouli:textures/gui/book_brown.png"
    fun serialize(): String? {
        return Gson().toJson(this)
    }

    companion object {
        private const val br = "\\\$\\(br\\)"
        private val pattern = Regex("^(\\s*$br\\s*)*(.*?)$br.*$")
        fun fromEntry(entry: Entry): Book {
            var description = entry.pages[0].text
            description = description
                .replace(pattern, "$2")
                .trim()
            return Book(entry.name, description)
        }
    }
}