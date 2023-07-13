class Player(val name: String) {
    var position = 1
    
    var money = 4_096
        private set(value) {
            field =
                if (value < 0) {
                    printError("money for $name was attempted to be set to $value")
                    0
                } else {
                    value
                }
        }
    
    fun incrementMoneyAndPrintUpdate(amount: Int) {
        money += amount
        println("$name gained $$amount and now has $$money")
    }
    
    fun decrementMoneyAndPrintUpdate(amount: Int) {
        money -= amount
        println("$name lost $$amount and now has $$money")
    }
    
    var isInGame = true
        private set
    
    fun removeFromGame() {
        isInGame = false
    }
    
    var isOnVacation = false
        private set
    
    var numTurnsOnVacation = 0
    
    fun sendToVacation() {
        isOnVacation = true
        position = Board.vacationPosition
    }
    
    fun removeFromVacation() {
        isOnVacation = false
        numTurnsOnVacation = 0
    }
    
    var numGetOffVacationCards = 0
        set(value) {
            field =
                if (value < 0) {
                    printError("numGetOffVacationCards for $name was attempted to be set to $value")
                    0
                } else {
                    value
                }
        }
    
    val hasAGetOffVacationCard: Boolean
        get() = numGetOffVacationCards > 0
    
    fun addGetOffVacationCard() {
        numGetOffVacationCards++
    }
    
    fun removeGetOffVacationCard() {
        numGetOffVacationCards--
    }
    
    /**
     * Returns a StringBuilder that contains info about this player.
     */
    fun getInfo(): StringBuilder =
        StringBuilder().apply {
            append("$name -> ")
            if (!isInGame) {
                append("Out of the Game")
                return@apply
            }
            append("Position: $position, Money: $$money")
            if (hasAGetOffVacationCard) {
                append(", Get Off Vacation Free Cards: $numGetOffVacationCards")
            }
            if (isOnVacation) {
                append(", On Vacation - Number of Turns Spent: $numTurnsOnVacation")
            }
        }
}