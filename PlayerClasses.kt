package commandline

import java.lang.Exception
import kotlin.random.Random

class PlayerManager {
    private var currentPlayerIndex = 0
    private val players = mutableListOf<Player>()
    val numberOfPlayersInGame get() = Player.numberOfPlayersInGame

    init {
        setup()
    }

    private fun setup() {
        var numHumanPlayers: Int?
        val numAIPlayers = 0
        fun getDiceRoll() = Random.nextInt(1, 7)
//        while (true) {
        while (true) {
            print("How many human players? ")
            numHumanPlayers = readLine()!!.toIntOrNull()
            if (numHumanPlayers == null || numHumanPlayers !in 2..8) {
                println("The number of human players must be a number which can be a minimum of 2 and a maximum of 8")
            } else break
        }

//         AI player's moves are not currently implemented so this code is commented out
//            while (true) {
//                print("How many AI Players? ")
//                val input = readLine()!!
//                try {
//                    numAIPlayers = input.toInt()
//                } catch (e: NumberFormatException) {
//                    println("Invalid input")
//                    continue
//                }
//                if (numAIPlayers in 0..8) break
//                println("The number of AI Players can be a minimum of 0 and a maximum of 8")
//            }
//            if (numHumanPlayers + numAIPlayers in 2..8) break
//            println("Total number of players can be a minimum of 2 and a maximum of 8")
//        }

        /**
         * The purpose of this class is to let Kotlin order the players based on dice rolls
         */
        class PlayerOrdering(val name: String, val diceRoll: Int, val type: String) : Comparable<PlayerOrdering> {
            init {
                println("$name got a $diceRoll for their beginning dice roll")
            }

            // Make it so that when we sort an array or list of PlayerOrderings, higher dice rolls are first
            override fun compareTo(other: PlayerOrdering) = other.diceRoll - this.diceRoll
        }

        val playerOrderingList = mutableListOf<PlayerOrdering>()
        for (i in 0 until numHumanPlayers!!) {
            print("\nEnter name for human player ${i + 1} or enter nothing for \"Human Player ${i + 1}\": ")
            val input = readLine()!!
            val name = if (input.isEmpty()) "Human Player ${i + 1}" else input
            playerOrderingList.add(PlayerOrdering(name, getDiceRoll() + getDiceRoll(), "Human"))
        }

        for (i in numHumanPlayers until numHumanPlayers + numAIPlayers) {
            print(
                "\nEnter a name for AI player ${i - numHumanPlayers + 1} or enter nothing for \"AI Player " +
                        "${i - numHumanPlayers + 1}\": "
            )
            val input = readLine()!!
            val name = if (input.isEmpty()) "AI Player ${i - numHumanPlayers + 1}" else input
            playerOrderingList.add(PlayerOrdering(name, getDiceRoll() + getDiceRoll(), "AI"))
        }
        playerOrderingList.sort()

        // Create and fill list of players
        for (i in 0 until numHumanPlayers + numAIPlayers) {
            players.add(
                if (playerOrderingList[i].type == "Human") HumanPlayer(playerOrderingList[i].name)
                else AIPlayer(playerOrderingList[i].name)
            )
        }

        println("The order of the players is ")
        for (player in players) println(player.name)
        println()

        // Set numbers starting from 1
        for (i in players.indices) {
            players[i].number = i + 1
            println("The number for ${players[i].name} is ${players[i].number}")
        }
        println()
    }

    /**
     * This value should be assigned to a variable and then changes should be made to that variable
     * and then the updateCurrentPlayer method should be called to update the player object
     * in the players list.
     */
    val currentPlayerCopy get() = players[currentPlayerIndex]

    fun getPlayerCopy(playerNumber: Int) = players[playerNumber - 1]

    fun updatePlayer(updatedPlayer: Player) {
        players[updatedPlayer.number!! - 1] = updatedPlayer
    }

//    fun removePlayerFromGame(playerNumber: Int) {
//        players[playerNumber - 1].removeFromGame()
//    }

    fun switchToNextPlayer() {
        do {
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size
        } while (!players[currentPlayerIndex].isInGame)
    }

    /**
     * This function should be called during certain entropy card plays
     */
//    fun addMoneyToAllPlayersBesidesCurrent(amount: Int) {
//        for (i in players.indices) if (i != currentPlayerIndex) players[i].money += amount
//    }

    /**
     * This function should be called during certain entropy card plays
     */
//    fun removeMoneyFromAllPlayersBesidesCurrent(amount: Int) {
//        for (i in players.indices) if (i != currentPlayerIndex) players[i].money -= amount
//    }

    /**
     * This function should be used during entropy card plays. The current player should be excluded.
     */
//    fun getPlayersThatDontHaveEnoughMoney(moneyAmount: Int): List<Player> {
//        val playersThatDontHaveEnoughMoney = mutableListOf<Player>()
//        for ((index, player) in players.withIndex()) {
//            if (player.money < moneyAmount && index != currentPlayerIndex) {
//                playersThatDontHaveEnoughMoney.add(player)
//            }
//        }
//        return playersThatDontHaveEnoughMoney
//    }

    fun displayPositions() {
        println("Player Positions")
        for (player in players) {
            println("${player.name}: " + if (player.isInGame) player.position else "Out of the game")
        }
    }

    fun displayNumbers() {
        println("Player Numbers:")
        for (player in players) {
            println("Name: ${player.name}, " + if (player.isInGame) "Number: ${player.number}" else "Out of the game")
        }
    }

//    fun getNonCurrentPlayersInGame() =
//        players.filterIndexed { index, player -> index != currentPlayerIndex && player.isInGame }

    /**
     * Returns a list of numbers of players that are still in the game with the exception of the parameter playerNumber
     */
    fun getNumbersOfOtherPlayersInGame(playerNumber: Int): List<Int> {
        val numbers = mutableListOf<Int>()
        for (player in players.filter { it.isInGame && it.number != playerNumber }) {
            numbers.add(player.number!!)
        }
        return numbers
    }

    fun onePlayerIsLeftInGame() = numberOfPlayersInGame == 1

    fun getWinnerName(): String {
        if (numberOfPlayersInGame > 1) {
            throw Exception("There is not a winner since there is more than 1 player left in the game")
        }
        return players.single { it.isInGame }.name
    }

//    fun addMoney(playerNumber: Int, amount: Int) {
//        players[playerNumber - 1].money += amount
//    }
//
//    fun updateCurrentPlayer(updatedPlayer: Player) {
//        players[currentPlayerIndex] = updatedPlayer
//    }
}


abstract class Player(var name: String) {
    init {
        numberOfPlayersInGame++
    }

    var isInGame = true
        private set

    fun removeFromGame() {
        isInGame = false
        numberOfPlayersInGame--
    }

    /**
     * number should only be set once at the beginning of a game.
     * @throws Exception if number is attempted to be set to null or if it is attempted to be set when it's not null,
     * which would be anytime after it's set at the beginning.
     */
    var number: Int? = null
        set(value) {
            if (value == null) {
                throw Exception("Player number cannot be set null")
            }
            if (field == null) {
                field = value
            } else {
                throw Exception("Player number can only be set at the beginning")
            }
        }

    var money = 1500
        set(value) {
            if (value < 0) throw Exception("Can't have negative amount of money")
            println(
                name +
                        if (value < field) {
                            " lost $${field - value} "
                        } else {
                            " gained $${value - field} "
                        }
                        + "and now has $$value"
            )
            field = value
        }

    /**
     * @throws Exception if position is set to an Int less than 1
     */
    var position = 1
        set(value) {
            if (value < 1) {
                throw Exception("Position must be positive but you tried to set it to $value")
            }
            field = value
        }

    var numberOfGolfCoursesOwned = 0
        private set

    /**
     * Increments the number of golf courses owned.
     */
    fun addGolfCourse() {
        numberOfGolfCoursesOwned++
    }

    /**
     * Decrements the number of golf courses owned.
     * @throws Exception if this function is called on a player doesn't own any golf courses.
     */
    fun removeGolfCourse() {
        if (numberOfGolfCoursesOwned == 0) {
            throw Exception("You've tried to remove a golf course from $name, who doesn't have any golf courses")
        }
        numberOfGolfCoursesOwned--
    }

    var numberOfSuperStoresOwned = 0
        private set

    /**
     * Increments the number of super stores owned.
     */
    fun addSuperStore() {
        numberOfSuperStoresOwned++
    }

    /**
     * Decrements the number of super stores owned.
     * @throws Exception if this function is called on a player that doesn't own any super stores.
     */
    fun removeSuperStore() {
        if (numberOfSuperStoresOwned == 0) {
            throw Exception("You've tried to remove a super store from $name, who doesn't have any super stores")
        }
        numberOfSuperStoresOwned--
    }

    var isOnVacation = false
        private set
    var numberOfTurnsOnVacation = 0
        private set

    fun sendToVacation() {
        isOnVacation = true
        position = vacationPosition!!
    }

    /**
     * Increments the number of turns on vacation and takes them off vacation automatically if the number of turns
     * has reached 3.
     */
    fun continueVacation() {
        numberOfTurnsOnVacation++
        if (numberOfTurnsOnVacation == 3) {
            println("$name, you spent 3 turns on vacation so it is now over")
            isOnVacation = false
        }
    }

    /**
     * Should be called when a player pays to get off vacation or uses a "Get Off Vacation Free" card.
     */
    fun removeFromVacation() {
        isOnVacation = false
    }

    var numberOfGetOffVacationCardsOwned = 0
        private set

    /**
     * Increments the number of Get Off Vacation Free card owned.
     */
    fun addGetOffVacationCard() {
        numberOfGetOffVacationCardsOwned++
    }

    /**
     * Decrements the number of Get Off Vacation Free cards owned.
     */
    fun removeGetOffVacationCard() {
        numberOfGetOffVacationCardsOwned--
    }

    /**
     * Is true when the player has at least 1 Get Off Vacation Free card.
     */
    val hasAGetOffVacationCard get() = numberOfGetOffVacationCardsOwned > 0

    companion object {
        /**
         * This is used for Entropy Deck cards that are dependent on the number of players in a game.
         * This gets incremented when a player is created and gets decremented when a player is removed from the game.
         */
        var numberOfPlayersInGame = 0

        /**
         * This position that players will go to when they get sent to vacation is constant so have it be set and send
         * players to this position when necessary. The proper value for this should be acquired from the board object.
         * This should only be set once at the beginning.
         */
        var vacationPosition: Int? = null
            set(value) {
                if (value == null) {
                    throw Exception("Vacation position cannot be set null")
                }
                if (field != null) {
                    throw Exception("Vacation position can only be set once")
                }
                field = value
            }
    }
}

class HumanPlayer(name: String) : Player(name) {
}

class AIPlayer(name: String) : Player(name) {
}

//    override fun addMoney(amount: Int) {
//        super.addMoney(amount)
//        println("$name gained $$amount and now has $$money")
//    }
//
//    override fun removeMoney(amount: Int) {
//        super.removeMoney(amount)
//        println("$name lost $$amount and now has $$money")
//    }