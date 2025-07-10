package singletonopoly

import java.text.DecimalFormat
import kotlin.math.absoluteValue

// This file has a couple functions that are used in multiple classes.

object Misc {
    private val commaAdder = DecimalFormat("#,###")
    
    fun Int.toStringWithCommas(): String =
        commaAdder.format(toLong())
    
    fun getNumItemsString(numItems: Int, item: String) =
        "$item${if (numItems == 1) "" else "s"}"
    
    fun getNumGetOffVacationCardsString(num: Int) =
        getNumItemsString(num, "get off Vacation free card")
    
    fun <T> Iterable<T>.joinToStringBuilder(
        prefix: CharSequence = "",
        transform: ((T) -> CharSequence)? = null,
        separator: CharSequence
    ) : StringBuilder =
        joinTo(buffer = StringBuilder(), prefix = prefix, transform = transform, separator = separator)
    
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
    )
