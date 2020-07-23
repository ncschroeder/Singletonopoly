package commandline

import java.lang.Exception
import java.lang.NumberFormatException
import kotlin.random.Random

class PlayerManager {
    private var numHumanPlayers = 0
    private var numAIPlayers = 0
    private var currentPlayerIndex = 0
    private val players = mutableListOf<Player>()
    val numPlayersInGame get() = Player.numPlayersInGame

    /**
     * This value should be assigned to a variable and then changes should be made to that variable
     * and then the updateCurrentPlayer method should be called to update the player object
     * in the players list.
     */
    val currentPlayerCopy get() = players[currentPlayerIndex]

    init {
        setup()
    }

    fun setup() {
        fun getDiceRoll() = Random.nextInt(1, 7)
        while (true) {
            while (true) {
                print("How many human players? ")
                val input = readLine()!!
                try {
                    numHumanPlayers = input.toInt()
                } catch (e: NumberFormatException) {
                    println("Invalid input")
                    continue
                }
                if (numHumanPlayers in 0..8) break
                println("The number of human players can be a minimum of 0 and a maximum of 8")
            }

            while (true) {
                print("How many AI Players? ")
                val input = readLine()!!
                try {
                    numAIPlayers = input.toInt()
                } catch (e: NumberFormatException) {
                    println("Invalid input")
                    continue
                }
                if (numAIPlayers in 0..8) break
                println("The number of AI Players can be a minimum of 0 and a maximum of 8")
            }
            if (numHumanPlayers + numAIPlayers in 2..8) break
            println("Total number of players can be a minimum of 2 and a maximum of 8")
        }

        /**
         * The purpose of this class is to let Kotlin order the players based on dice rolls
         */
        class PlayerOrdering(val name: String, val diceRoll: Int, val type: String) :
            Comparable<PlayerOrdering> {
            init {
                println("$name got a $diceRoll for their beginning dice roll")
            }

            // Make it so that when we sort an array or list of PlayerOrderings, higher dice rolls are first
            override fun compareTo(other: PlayerOrdering) = other.diceRoll - this.diceRoll
        }

        // Get player's names
        val playerOrderingArray = arrayOfNulls<PlayerOrdering>(numHumanPlayers + numAIPlayers)
        for (i in 0 until numHumanPlayers) {
            print("Enter name for player ${i + 1} (Human Player) or enter nothing for \"Human Player ${i + 1}\": ")
            val input = readLine()!!
            val name = if (input == "") "Human Player ${i + 1}" else input
            playerOrderingArray[i] = PlayerOrdering(name, getDiceRoll() + getDiceRoll(), "Human")
        }

        for (i in numHumanPlayers until numHumanPlayers + numAIPlayers) {
            print(
                "Enter a name for player ${i + 1} (AI Player) or enter nothing for \"AI Player " +
                        "${i - numHumanPlayers + 1}\": "
            )
            val input = readLine()!!
            val name = if (input == "") "AI Player ${i - numHumanPlayers + 1}" else input
            playerOrderingArray.set(i, PlayerOrdering(name, getDiceRoll() + getDiceRoll(), "AI"))
        }
        playerOrderingArray.sort()

        // Create and fill list of players
        for (i in 0 until numHumanPlayers + numAIPlayers) {
            players.add(
                if (playerOrderingArray[i]!!.type.equals("Human")) HumanPlayer(
                    playerOrderingArray[i]!!.name
                )
                else AIPlayer(playerOrderingArray[i]!!.name)
            )
        }

        println("The order of the players is ")
        for (player in players) println(player.name)

        // Set numbers
        for (i in players.indices) {
            players[i].number = i + 1
            println("The number for ${players[i].name} is ${players[i].number}")
        }
        println()
    }

    fun updateCurrentPlayer(updatedPlayer: Player) {
        players[currentPlayerIndex] = updatedPlayer
    }

    fun updatePlayer(playerNumber: Int, updatedPlayer: Player) {
        players[playerNumber - 1] = updatedPlayer
    }

    fun switchToNextPlayer() {
        do {
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size
        } while (!players[currentPlayerIndex].isInGame)
    }

    /**
     * This function should be called during certain entropy card plays
     */
    fun addMoneyToAllPlayersBesidesCurrent(amount: Int) {
        for (i in players.indices) if (i != currentPlayerIndex) players[i].money += amount
    }

    /**
     * This function should be called during certain entropy card plays
     */
    fun removeMoneyFromAllPlayersBesidesCurrent(amount: Int) {
        for (i in players.indices) if (i != currentPlayerIndex) players[i].money -= amount
    }

    fun displayPositions() {
        for (player in players) {
            println(
                "${player.name}: " + if (player.isInGame) player.position else "Out of the game"
            )
        }
    }

    fun getNumGolfCoursesOwned(playerNumber: Int) = players[playerNumber - 1].numGolfCoursesOwned

    fun getPlayerCopy(playerNumber: Int) = players[playerNumber - 1]

    fun getNumbersOfPlayersInGame(): Set<Int> {
        val numbers = mutableSetOf<Int>()
        for (player in players) if (player.isInGame) numbers.add(player.number)
        return numbers
    }

    fun addMoney(playerNumber: Int, amount: Int) {
        players[playerNumber - 1].money += amount
    }
}


abstract class Player(var name: String) {
    init {
        numPlayersInGame++
    }

    var isInGame = true
        private set

    fun removeFromGame() {
        isInGame = false
        numPlayersInGame--
    }

    var number = 0
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
    var position = 1
        set(value) {
            if (value < 1) throw Exception("Position must be positive but you tried to set it to $value")
            field = value
        }

    var numGolfCoursesOwned = 0
        private set

    fun addGolfCourse() {
        numGolfCoursesOwned++
    }

    fun removeGolfCourse() {
        if (numGolfCoursesOwned == 0) {
            throw Exception("Can't have negative number of golf courses")
        }
        numGolfCoursesOwned--
    }

    var isOnVacation = false
        private set
    var numTurnsOnVacation = 0
        private set

    fun sendToVacation(vacationPosition: Int) {
        isOnVacation = true
        position = vacationPosition
    }

    fun continueVacation() {
        numTurnsOnVacation++
        if (numTurnsOnVacation == 3) {
            // Take player off vacation automatically once 3 turns have been taken
            println("$name, you spent 3 turns on vacation so it is now over")
            isOnVacation = false
        }
    }

    fun removeFromVacation() {
        isOnVacation = false
    }

    var numGetOffVacationCardsOwned = 0
        private set

    fun addGetOffVacationCard() {
        numGetOffVacationCardsOwned++
    }

    fun removeGetOffVacationCard() {
        numGetOffVacationCardsOwned--
    }

    val hasAGetOffVacationCard get() = numGetOffVacationCardsOwned > 0

    companion object {
        var numPlayersInGame = 0
    }
}

class HumanPlayer(name: String) : Player(name) {
}

class AIPlayer(name: String) : Player(name) {
}

//    protected var properties = mutableListOf<Int>()
//
//    fun addProperty() {
//        // This function is used to add the property that the player is currently on
//        properties.add(position)
//    }
//
//    fun ownsProperty(position: Int): Boolean {
//        return properties.contains(position)
//    }

//    override fun addMoney(amount: Int) {
//        super.addMoney(amount)
//        println("$name gained $$amount and now has $$money")
//    }
//
//    override fun removeMoney(amount: Int) {
//        super.removeMoney(amount)
//        println("$name lost $$amount and now has $$money")
//    }