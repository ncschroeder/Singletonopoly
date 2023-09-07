// This file has a couple functions that are used in multiple classes.

/**
 * Used in situations that shouldn't happen and the error can be handled but we want to be aware that this error happened.
 */
fun printError(error: String) {
    println("=\n".repeat(10) + "Error: $error" + "\n=".repeat(10))
}

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