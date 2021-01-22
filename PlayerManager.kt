/**
 * PlayerManager class is used for creating, storing, and accessing player objects and calling functions related to them.
 */
class PlayerManager {

    /**
     * Player class consists of data that is unique to each player and functions that can be performed on that data.
     */
    inner class Player(val name: String) {
        val info
            get() = "Name: $name, " +
                    if (isInGame) {
                        "Position: $position, Money: $$money, Are they on vacation?: " +
                                "${if (isOnVacation) "Yes" else "No"}, Get Off Vacation Free cards: " +
                                numberOfGetOffVacationCardsOwned
                    } else {
                        "Out of the game"
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
                field = value
            }

        /**
         * @throws IllegalArgumentException if position is set to a value less than 1.
         */
        var position = 1
            set(value) {
                if (value < 1) {
                    throw IllegalArgumentException("invalid nonpositive position: $value")
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

        /**
         * @throws IllegalArgumentException if numberOfTurnsOnVacation is set to a negative value.
         */
        var numberOfTurnsOnVacation = 0
            set(value) {
                if (value < 0) {
                    throw IllegalArgumentException("Number of turns on vacation cannot be negative")
                }
                field = value
            }

        /**
         * Sets the player's "isOnVacation" status to true and changes the position of this player to the vacation
         * position is known by the player manager that this player object came from.
         */
        fun sendToVacation() {
            isOnVacation = true
            position = vacationPosition
        }

        /**
         * Sets the player's "isOnVacation" status to false and reduces the numberOfTurnsOnVacation to 0.
         */
        fun removeFromVacation() {
            isOnVacation = false
            numberOfTurnsOnVacation = 0
        }

        /**
         * @throws IllegalArgumentException if numberOfGetOffVacationCardsOwned is set to a negative value.
         */
        var numberOfGetOffVacationCardsOwned = 0
            set(value) {
                if (value < 0) {
                    throw IllegalArgumentException("Cannot have negative amount of get off vacation free cards")
                }
                field = value
            }

        fun addGetOffVacationCard() {
            numberOfGetOffVacationCardsOwned++
        }

        /**
         * @throws IllegalArgumentException if numberOfGetOffVacation cards is 0.
         */
        fun removeGetOffVacationCard() {
            numberOfGetOffVacationCardsOwned--
        }

        /**
         * Is true when the player has at least 1 Get Off Vacation Free card.
         */
        val hasAGetOffVacationCard get() = numberOfGetOffVacationCardsOwned > 0
    }

    private val players = mutableListOf<Player>()

    fun addPlayer(name: String) {
        players.add(Player(name))
    }

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

    val playerInfo: String
        get() {
            val sb = StringBuilder("Player Info")
            for (player in players) {
                sb.append('\n').append(player.info)
            }
            return sb.toString()
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

    /**
     * @return A list of players that are still in the game with the exception of the excludingPlayer argument.
     */
    fun getListOfOtherPlayersInGame(excludingPlayer: Player) = players.filter { it.isInGame && it != excludingPlayer }

    /**
     * This is the position that players will go to when they get sent to vacation. The proper value for this should
     * be acquired from the board object. This should only be set once at the beginning.
     */
    var vacationPosition = 0

    /**
     * @return The name of the only player left in the game.
     * @throws Exception if there is more than 1 player that is in the game.
     */
    fun getWinnerName() = players.single { it.isInGame }.name
}