// This file has a few functions that are used in multiple classes

/**
 * Used in situations that shouldn't happen and the error can be handled but we want to be aware that this error happened.
 */
fun printError(error: String) {
    println("=\n".repeat(10) + "Error: $error" + "\n=".repeat(10))
}

/**
 * Returns a Map where the values are the elements in this Iterable and the keys are the 1-based positions of
 * those elements in this Iterable.
 */
fun <T> Iterable<T>.associateByPosition(): Map<Int, T> =
    withIndex()
    .associate { (index: Int, element: T) -> index + 1 to element }

/**
 * Returns the enum constant name with the underscores replaced with spaces and the words have a capital first
 * letter and all other letters are lowercase.
 */
fun Enum<*>.createFriendlyName(): String =
    name.split("_")
    .joinToString(
        transform = { word: String -> word.lowercase().replaceFirstChar { it.uppercase() } },
        separator = " "
    )