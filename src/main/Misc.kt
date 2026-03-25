package singletonopoly

import java.text.DecimalFormat

/**
 * Has functions that are used in multiple code files.
 */
object Misc {
    private val commaAdder = DecimalFormat("#,###")
    
    fun Int.toStringWithCommas(): String = commaAdder.format(this)
    
    fun getNumThingsString(num: Int, thing: String) =
        "$num $thing${if (num == 1) "" else "s"}"
    
    fun getNumGetOffVacationCardsString(num: Int) =
        getNumThingsString(num, "get off vacation free card")
    
    fun <T> Iterable<T>.joinToStringBuilder(
        prefix: CharSequence = "",
        transform: ((T) -> CharSequence)? = null,
        separator: CharSequence
    ) : StringBuilder =
        joinTo(buffer = StringBuilder(), prefix = prefix, transform = transform, separator = separator)
    
    /**
     * Used in situations that shouldn't happen and the error can be handled but we want to be aware that this error happened.
     */
    fun printError(error: String) {
        println("=\n".repeat(10) + "Error: $error" + "\n=".repeat(10))
    }
}
