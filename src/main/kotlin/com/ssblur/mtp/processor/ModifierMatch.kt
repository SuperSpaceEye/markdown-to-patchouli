package com.ssblur.mtp.processor

import components.Entry
import processor.Processor
import kotlin.reflect.KClass
import kotlin.text.Regex

class ModifierMatch: Processor {
    @OptIn(ExperimentalStdlibApi::class)
    override fun process(line: String, entry: Entry): String {
        modifierPattern.findAll(line).forEach {
            val items = (it.groups[1]?.value ?: return@forEach).split(Regex(" "), 2)
            if (items.size != 2) throw AssertionError("Incorrect modifier pattern for line:\n${line}")
            val (command, value) = items

            val expectedType = allowedModifiers[command]
            val convertedValue = strToTyped(value) ?: throw AssertionError("Couldn't convert value \"$value\" of line \"$line\" to any known type (string/int/boolean)")
            if (expectedType != null) {
                if (expectedType != convertedValue::class) throw AssertionError("Modifier $command is ${convertedValue::class.simpleName}, expected ${expectedType.simpleName}")
                entry.data[command.drop(2)] = convertedValue
            } else entry.unknownModifiers[command] = convertedValue
        }

        return modifierPattern.replace(line, "")
    }

    companion object {
        val modifierPattern = Regex("\\{\\.(.*?)\\}")
        val allowedModifiers = mutableMapOf<String, KClass<*>>(
            "e_name" to String::class,
            "e_icon" to String::class,
            "e_advancement" to String::class,
            "e_flag" to String::class,
            "e_priority" to Boolean::class,
            "e_secret" to Boolean::class,
            "e_read_by_default" to Boolean::class,
            "e_sortnum" to Int::class,
            "e_turnin" to String::class,
            "e_entry_color" to String::class
        )

        private inline fun tryC(fn: () -> Any): Any? {
            return try {
                fn()
            } catch (e: Exception) {
                null
            }
        }

        fun strToTyped(str: String): Any? {
            return if (str.first() == '"' && str.last() == '"') {
                str.drop(1).dropLast(1)
            } else {
                tryC { str.toInt() } ?:
                tryC { str.toFloat() } ?:
                tryC { str.toBoolean() }
            }
        }
    }
}