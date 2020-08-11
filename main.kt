package commandline

fun main() {
    outer@ while (true) {
        Game()
        inner@ while (true) {
            print("Would you like to play again? (y/n) ")
            when (readLine()!!.toLowerCase()) {
                "y" -> break@inner
                "n" -> break@outer
                else -> println("Invalid input")
            }
        }
    }
}