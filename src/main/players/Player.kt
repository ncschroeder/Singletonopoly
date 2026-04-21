package singletonopoly.players

import singletonopoly.Misc
import singletonopoly.Misc.toStringWithCommas
import singletonopoly.board.Board

class Player(val name: String) {
    var position = 1
    
    /*
    When a player's amount of money needs to change, most of the time, that'll be done by calling
    incrementMoneyAndPrintUpdate or decrementMoneyAndPrintUpdate, though there are a few situations
    where the money property is set directly and a custom update message is printed.
     */
    
    var money = 4_096
        set(value) {
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
        println("$name gained $${amount.toStringWithCommas()} and now has $$moneyString.")
    }
    
    fun decrementMoneyAndPrintUpdate(amount: Int) {
        money -= amount
        println("$name lost $${amount.toStringWithCommas()} and now has $$moneyString.")
    }
    
    var isInGame = true
        private set
    
    fun removeFromGame() {
        isInGame = false
    }
    
    var numTurnsSpentOnVacation: Int? = null
    
    val isOnVacation: Boolean
        get() = numTurnsSpentOnVacation != null
    
    fun sendToVacation() {
        numTurnsSpentOnVacation = 0
        position = Board.VACATION_POSITION
    }
    
    fun removeFromVacation() {
        numTurnsSpentOnVacation = null
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
        println("$name gained ${Misc.getNumGetOffVacationCardsString(amount)} and now has $numGetOffVacationCards.")
    }
    
    fun decrementNumGetOffVacationCardsAndPrintUpdate(amount: Int = 1) {
        numGetOffVacationCards -= amount
        println("$name lost ${Misc.getNumGetOffVacationCardsString(amount)} and now has $numGetOffVacationCards.")
    }
    
    /**
     * Returns a StringBuilder (CharSequence) and not a string since the chars in that StringBuilder
     * will be added to the string that gets built in PlayerManager.getPlayerInfo.
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
            numTurnsSpentOnVacation?.let {
                append(", On Vacation - Turns Spent: $it")
            }
        }
}
