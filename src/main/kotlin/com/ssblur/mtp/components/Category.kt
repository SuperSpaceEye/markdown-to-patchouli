package com.ssblur.mtp.components

import components.Entry
import org.gradle.internal.impldep.com.google.gson.Gson

class Category(
    val name: String,
    val description: String,
    val icon: String = "minecraft:book"
) {
    fun serialize(): String? {
        return Gson().toJson(this)
    }

    companion object {
        val br = "\\\$\\(br\\)"
        val pattern = Regex("^(\\s*$br\\s*)*(.*?)$br.*$")
        val formattingPattern = Regex("\\\$\\(.*?\\)")
        fun fromEntry(entry: Entry): Category {
            var description = entry.pages[0].text
            description = description
                .replace(formattingPattern, "")
                .trim()
            return Category(entry.name, description, entry.icon)
        }
    }
}