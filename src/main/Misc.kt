package singletonopoly

import java.text.DecimalFormat

// This file has functions that are used in multiple code files.

object Misc {
    private val commaAdder = DecimalFormat("#,###")
    
    fun Int.toStringWithCommas(): String = commaAdder.format(this)
    
    fun getNumThingsString(num: Int, thing: String) =
        "$num $thing${if (num == 1) "" else "s"}"
    
    fun getNumGetOffVacationCardsString(num: Int) =
        getNumThingsString(num, "get off Vacation free card")
    
    fun <T> Iterable<T>.joinToStringBuilder(
        prefix: CharSequence = "",
        transform: ((T) -> CharSequence)? = null,
        separator: CharSequence
    ) : StringBuilder =
        joinTo(buffer = StringBuilder(), prefix = prefix, transform = transform, separator = separator)
    
    /**
     * Returns a set of whitespace-separated ints in s. Non-int values are ignored.
     */
    fun getIntsIn(s: String): Set<Int> =
        s
        .split(Regex("\\s+"))
        .mapNotNullTo(HashSet()) { it.toIntOrNull() }
    
    /**
     * Returns the enum constant name with the underscores replaced with spaces and the words have a capital first
     * letter and all other letters are lowercase.
     */
    fun createFriendlyEnumName(enumName: String): String =
        enumName
        .split('_')
        .joinToString(
            transform = { word: String -> word.lowercase().replaceFirstChar { it.uppercase() } },
            separator = " "
        )
    
    /**
     * Used in situations that shouldn't happen and the error can be handled but we want to be aware that this error happened.
     */
    fun printError(error: String) {
        println("=\n".repeat(10) + "Error: $error" + "\n=".repeat(10))
    }
}
