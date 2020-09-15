package commandline

import kotlin.random.Random

/**
 * This class is used for interacting with players and
 */
class PlayerManager {
    private val players = mutableListOf<Player>()

    init {
        setup()
    }

    private fun setup() {
        var numberOfPlayers: Int?
        while (true) {
            print("How many players? ")
            numberOfPlayers = readLine()!!.toIntOrNull()
            if (numberOfPlayers == null || numberOfPlayers !in 2..8) {
                println("The number of players must be a number which can be a minimum of 2 and a maximum of 8")
            } else {
                break
            }
        }

        /**
         * The purpose of this class is to let Kotlin order the players based on dice rolls
         */
        class PlayerOrdering(val name: String) : Comparable<PlayerOrdering> {
            var totalDiceRoll = 0
            fun generateDiceRoll() = Random.nextInt(1, 7)

            init {
                val diceRoll1 = generateDiceRoll()
                val diceRoll2 = generateDiceRoll()
                totalDiceRoll = diceRoll1 + diceRoll2
                println(
                    "$name got a $diceRoll1 and a $diceRoll2 for their beginning dice roll for " +
                            "a total of $totalDiceRoll"
                )
            }

            // Make it so that when we sort an array or list of PlayerOrderings, higher dice rolls are first
            override fun compareTo(other: PlayerOrdering) = other.totalDiceRoll - this.totalDiceRoll

            /**
             * @return A Player object with the same name as this playerOrdering object and the number that is
             * passed in as an argument.
             */
            fun toPlayer(number: Int) = Player(name, number)
        }

        val playerOrderingList = mutableListOf<PlayerOrdering>()

        for (i in 1..numberOfPlayers!!) {
            print("\nEnter name for player $i or enter nothing for \"Player $i\": ")
            val input = readLine()!!
            val name =
                if (input.isEmpty()) {
                    "Player $i"
                } else {
                    input
                }
            playerOrderingList.add(PlayerOrdering(name))
        }
        println()

        playerOrderingList.sort()

        for ((index, playerOrdering) in playerOrderingList.withIndex()) {
            val playerNumber = index + 1
            players.add(playerOrdering.toPlayer(playerNumber))
        }

        println("The order of the players is ")
        for (player in players) {
            println(player.name)
        }
        println()

        for (player in players) {
            println("The number for ${player.name} is ${player.number}")
        }
        println()
    }

    val numberOfPlayersInGame: Int
        get() {
            var count = 0
            for (player in players) {
                if (player.isInGame) {
                    count++
                }
            }
            return count
        }

    val onePlayerIsInGame get() = numberOfPlayersInGame == 1

    private var currentPlayerIndex = 0

    /**
     * Is equal to a reference of the current player. If this value gets assigned to a variable, any changes made to
     * that variables will also be the same changes made to the player object in the players list.
     */
    val currentPlayer get() = players[currentPlayerIndex]

    /**
     * @return A reference to a player object that this player manager has. If this value gets assigned to a
     * variable, any changes made to that variable will also be the same changes made to the player object in
     * the players list.
     */
    fun getPlayer(playerNumber: Int) = players[playerNumber - 1]

    /**
     * Saves all the changes made to the updatedPlayer argument. The player manager knows which player is
     * which based on their numbers.
     */
    fun updatePlayer(updatedPlayer: Player) {
        players[updatedPlayer.number - 1] = updatedPlayer
    }

//    fun removePlayerFromGame(playerNumber: Int) {
//        players[playerNumber - 1].removeFromGame()
//    }

    fun switchToNextPlayer() {
        do {
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size
        } while (!players[currentPlayerIndex].isInGame)
    }

    fun displayPlayerInfo() {
        println("\nPlayer Info")
        for (player in players) {
            println(player)
        }
        println()
    }

    /**
     * @return A list of numbers of players that are still in the game with the exception of the
     * excludingPlayerNumber argument.
     */
    fun getNumbersOfOtherPlayersInGame(excludingPlayerNumber: Int): List<Int> {
        val numbers = mutableListOf<Int>()
        for (player in players) {
            if (player.isInGame && player.number != excludingPlayerNumber) {
                numbers.add(player.number)
            }
        }
        return numbers
    }

    /**
     * @return A map whose keys are the numbers of players in the games and the values are those player objects.
     * The only player not in this map is the player whose number is the excludingPlayerNumber argument.
     */
    fun getMapOfOtherPlayersInGame(excludingPlayerNumber: Int): Map<Int, Player> {
        val mapOfPlayersInGame = mutableMapOf<Int, Player>()
        for (player in players) {
            if (player.isInGame && player.number != excludingPlayerNumber) {
                mapOfPlayersInGame[player.number] = player
            }
        }
        return mapOfPlayersInGame
    }

    /**
     * @return The name of the only player left in the game.
     *
     * Throws an exception if the player manager has more than 1 player that is in the game.
     */
    fun getWinnerName() = players.single { it.isInGame }.name


    /**
     * This is the position that players will go to when they get sent to vacation is constant so have it be
     * set and send players to this position when necessary. The proper value for this should be acquired from
     * the board object. This should only be set once at the beginning.
     *
     * @throws IllegalArgumentException if this is attempted to be set to a non-positive value.
     * @throws IllegalStateException if this is attempted to be set to something different after it has already
     * been set.
     */
    var vacationPosition = 0
        set(value) {
            if (value <= 0) {
                throw IllegalArgumentException("Vacation position can only be set to a positive value")
            }
            if (field != 0 && field != value) {
                throw IllegalStateException("Vacation position can only be set once")
            }
            field = value
        }

    inner class Player(val name: String, val number: Int) {
        override fun toString(): String {
            var string = "Name: $name, "
            if (isInGame) {
                string += "Number: $number, Position: $position, Money: $$money"
                if (hasAGetOffVacationCard) {
                    string += ", Get Off Vacation Free cards: $numberOfGetOffVacationCardsOwned"
                }
            } else {
                string += "Out of the game"
            }
            return string
        }

        /**
         * @throws IllegalArgumentException if a player's money amount is attempted to be set to a value less
         * than 0.
         */
        var money = 4096
            set(value) {
                if (value < 0) {
                    throw IllegalArgumentException("A player can't have a negative amount of money")
                }
                if (field != value) {
                    println(
                        if (value < field) {
                            "$name lost $${field - value} and now has $$value"
                        } else {
                            "$name gained $${value - field} and now has $$value"
                        }
                    )
                    field = value
                }
            }

        /**
         * @throws IllegalArgumentException if position is set to an Int less than 1
         */
        var position = 1
            set(value) {
                if (value < 1) {
                    throw IllegalArgumentException("Position must be positive but it was set it to $value")
                }
                field = value
            }

        var isInGame = true
            private set

        fun removeFromGame() {
            isInGame = false
        }

        var isOnVacation = false
            private set

        var numberOfTurnsOnVacation = 0
            private set

        fun sendToVacation() {
            isOnVacation = true
            position = vacationPosition
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
                numberOfTurnsOnVacation = 0
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
         *
         * @throws IllegalStateException if this method is called on a player that doesn't have any
         * Get Off Vacation Free cards.
         */
        fun removeGetOffVacationCard() {
            if (numberOfGetOffVacationCardsOwned == 0) {
                throw IllegalStateException(
                    "You've tried to remove a Get Off Vacation Free card from $name, who doesn't have any"
                )
            }
            numberOfGetOffVacationCardsOwned--
        }

        /**
         * Is true when the player has at least 1 Get Off Vacation Free card.
         */
        val hasAGetOffVacationCard get() = numberOfGetOffVacationCardsOwned > 0
    }
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