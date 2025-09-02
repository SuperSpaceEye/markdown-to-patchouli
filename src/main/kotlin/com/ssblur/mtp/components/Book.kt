package com.ssblur.mtp.components

import components.Entry
import org.gradle.internal.impldep.com.google.gson.GsonBuilder

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
        private inline fun tryC(fn: () -> Any): Any? {
            return try {
                fn()
            } catch (e: Exception) {
                null
            }
        }

        private const val br = "\\\$\\(br\\)"
        private val pattern = Regex("^(\\s*$br\\s*)*(.*?)$br.*$")
        private val brackets = Regex("\\{(.*?)\\}(\\s)")
        fun fromEntry(entry: Entry): Book {
            var description = entry.pages[0].text

            val book = Book(entry.name)

            brackets.findAll(description).forEach {
                var parameter = it.value
                var results = parameter.drop(2).dropLast(2).split(" ")
                if (results.size != 2) return@forEach
                val rightResult = results[1]

                val item = if (rightResult.first() == '"' && rightResult.last() == '"') {
                    rightResult.drop(1).dropLast(1)
                } else {
                    tryC{ rightResult.toInt() } ?:
                    tryC{ rightResult.toFloat() } ?:
                    tryC{ rightResult.toBoolean() }
                }

                book.data[results[0]] = item
                description = description.replace(parameter, "")
            }

            description = description.replace(pattern, "$2").trim()
            book.data["landing_text"] = description

            return book
        }
    }
}