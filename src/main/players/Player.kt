package singletonopoly.players

import singletonopoly.Misc
import singletonopoly.Misc.toStringWithCommas
import singletonopoly.board.Board

class Player(val name: String) {
    var position = 1
    
    var money = 4_096
        private set(value) {
            field =
                if (value >= 0) value
                else {
                    Misc.printError("money for $name was attempted to be set to $value.")
                    0
                }
        }
    
    val moneyString
        get() = money.toStringWithCommas()
    
    fun incrementMoneyAndPrintUpdate(amount: Int) {
        money += amount
        println("$name gained $$amount and now has $$moneyString.")
    }
    
    fun decrementMoneyAndPrintUpdate(amount: Int) {
        money -= amount
        println("$name lost $$amount and now has $$moneyString.")
    }
    
    var isInGame = true
        private set
    
    fun removeFromGame() {
        isInGame = false
    }
    
    
    var numTurnsOnVacation: Int? = null
    
    fun sendToVacation() {
        numTurnsOnVacation = 0
        position = Board.VACATION_POSITION
    }
    
    fun removeFromVacation() {
        numTurnsOnVacation = null
    }
    
    var numGetOffVacationCards = 0
        private set(value) {
            field =
                if (value >= 0) value
                else {
                    Misc.printError("numGetOffVacationCards for $name was attempted to be set to $value.")
                    0
                }
        }
    
    val hasAGetOffVacationCard: Boolean
        get() = numGetOffVacationCards > 0
    
    fun incrementNumGetOffVacationCardsAndPrintUpdate(amount: Int = 1) {
        numGetOffVacationCards += amount
        println(
            "$name gained ${Misc.getNumGetOffVacationCardsString(amount)} and now has $numGetOffVacationCards."
        )
    }
    
    fun decrementNumGetOffVacationCardsAndPrintUpdate(amount: Int = 1) {
        numGetOffVacationCards -= amount
        println(
            "$name lost ${Misc.getNumGetOffVacationCardsString(amount)} and now has $numGetOffVacationCards."
        )
    }
    
    /**
     * Returns a CharSequence with info about this player. The chars in this CharSequence will
     * be added to the string that gets built in the PlayerManager.getPlayersInfo function.
     */
    fun getInfo(): CharSequence =
        StringBuilder().apply {
            append("$name -> ")
            if (!isInGame) {
                append("Out of the Game")
                return@apply
            }
            append("Position: $position, Money: $$moneyString")
            if (hasAGetOffVacationCard) {
                append(", Get Off Vacation Free Cards: $numGetOffVacationCards")
            }
            numTurnsOnVacation?.let {
                append(", On Vacation - Number of Turns Spent: $it")
            }
        }
}
