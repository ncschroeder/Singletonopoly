fun main() {
    while (true) {
        Game()
        playAgainAskingLoop@ while (true) {
            print("Would you like to play again? (y/n) ")
            when (readLine()!!.toLowerCase()) {
                "y" -> {
                    println()
                    break@playAgainAskingLoop
                }
                "n" -> return
                else -> println("Invalid input")
            }
        }
    }
}