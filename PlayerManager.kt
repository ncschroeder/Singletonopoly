import kotlin.random.Random

/**
 * PlayerManager class is used for creating, storing, and accessing player objects.
 */
class PlayerManager {
    /**
     * Player class consists of data that is unique to each player and functions that can be performed on that data.
     */
    inner class Player(val name: String) {
        override fun toString(): String {
            var string = "Name: $name, "
            string +=
                if (isInGame) {
                    "Position: $position, Money: $$money, Are they on vacation?: " +
                            "${if (isOnVacation) "Yes" else "No"}, Get Off Vacation Free cards: " +
                            numberOfGetOffVacationCardsOwned
                } else {
                    "Out of the game"
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
         * @throws IllegalArgumentException if position is set to a value less than 1.
         */
        var position = 1
            set(value) {
                if (value < 1) {
                    throw IllegalArgumentException("Position must be positive but it was set it to $value")
                }
                field = value
            }

        /**
         * Increments money by 512 and prints that this player has made a revolution.
         */
        fun hasMadeARevolution() {
            println("$name has just made a revolution")
            money += 512
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

        /**
         * Makes the player be on vacation and changes the position of this player to the vacation position which
         * comes from the player manager that this player object came from.
         */
        fun sendToVacation() {
            isOnVacation = true
            position = vacationPosition
        }

        /**
         * Increments the number of turns on vacation and takes them off vacation automatically if the number of turns
         * has reached 3.
         */
        fun continueVacation() {
            if (!isOnVacation) {
                throw IllegalStateException("$name is not on vacation so they can't continue their vacation")
            }
            numberOfTurnsOnVacation++
            if (numberOfTurnsOnVacation == 3) {
                println("$name has spent 3 turns on vacation so it is now over")
                removeFromVacation()
            }
        }

        fun removeFromVacation() {
            isOnVacation = false
            numberOfTurnsOnVacation = 0
        }

        var numberOfGetOffVacationCardsOwned = 0

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

    private val players = mutableListOf<Player>()

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

    val currentPlayer get() = players[currentPlayerIndex]

    /**
     * Makes currentPlayer equal to the player whose turn is next.
     */
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
     * @return A list of players that are still in the game with the exception of the excludingPlayer argument.
     */
    fun getListOfOtherPlayersInGame(excludingPlayer: Player) = players.filter { it.isInGame && it != excludingPlayer }

    /**
     * This is the position that players will go to when they get sent to vacation. The proper value for this should
     * be acquired from the board object. This should only be set once at the beginning.
     */
    private var vacationPosition = 0

    /**
     * @return The name of the only player left in the game.
     * @throws Exception if the player manager has more than 1 player that is in the game.
     */
    fun getWinnerName() = players.single { it.isInGame }.name

    /**
     * First sets the vacationPosition property of this class to the value that is passed in as an argument.
     * Then asks how many players there are and what each player wants their name to be.
     */
    fun setup(vacationPosition: Int) {
        this.vacationPosition = vacationPosition

        var numberOfPlayers: Int?
        while (true) {
            print("How many players? ")
            numberOfPlayers = readLine()!!.toIntOrNull()
            if (numberOfPlayers == null || numberOfPlayers !in 2..8) {
                println("The number of players must be a number in the range of 2 to 8 inclusive")
            } else {
                break
            }
        }

        /**
         * The purpose of this class is to let Kotlin order the players based on dice rolls
         */
        class PlayerOrdering(val name: String) : Comparable<PlayerOrdering> {
            var totalDiceRoll = 0
            fun getDiceRoll() = Random.nextInt(1, 7)

            init {
                val diceRoll1 = getDiceRoll()
                val diceRoll2 = getDiceRoll()
                totalDiceRoll = diceRoll1 + diceRoll2
                println(
                    "$name got a $diceRoll1 and a $diceRoll2 for their beginning dice roll for " +
                            "a total of $totalDiceRoll"
                )
            }

            /**
             * Makes it so that when we sort an array or list of PlayerOrderings, higher dice rolls are first.
             */
            override fun compareTo(other: PlayerOrdering) = other.totalDiceRoll - this.totalDiceRoll

            /**
             * @return A Player object with the same name as this object.
             */
            fun toPlayer() = Player(name)
        }

        val playerOrderingList = mutableListOf<PlayerOrdering>()
        val namesSet = mutableSetOf<String>()
        var playerNumber = 1
        do {
            print("\nEnter a unique name for player $playerNumber or enter nothing for \"Player $playerNumber\": ")
            val input = readLine()!!
            val name: String
            if (input.isEmpty()) {
                name = "Player $playerNumber"
            } else if (input in namesSet) {
                println("$input is already a name for another player, you must select a unique name")
                continue
            } else {
                name = input
            }
            namesSet.add(name)
            playerOrderingList.add(PlayerOrdering(name))
            playerNumber++
        } while (playerNumber <= numberOfPlayers!!)
        println()

        playerOrderingList.sort()

        for (playerOrdering in playerOrderingList) {
            players.add(playerOrdering.toPlayer())
        }

        println("The order of the players is ")
        for (player in players) {
            println(player.name)
        }
        println()
    }
}