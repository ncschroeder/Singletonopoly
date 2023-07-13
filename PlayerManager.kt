class PlayerManager(playerNames: List<String>) {
    private val players: List<Player> = playerNames.map(::Player)
    private var currentPlayerIndex = 0
    val currentPlayer: Player
        get() = players[currentPlayerIndex]
    
    /**
     * Makes currentPlayer equal to the player whose turn is next.
     */
    fun switchToNextPlayer(): Player {
        do {
            currentPlayerIndex =
                if (currentPlayerIndex == players.lastIndex) 0
                else currentPlayerIndex + 1
        } while (!currentPlayer.isInGame)
        return currentPlayer
    }
    
    /**
     * Returns a string that contains a heading and info about all players.
     */
    fun getPlayersInfo(): String =
        players.joinToString(prefix = "Player Info\n", transform = { it.getInfo() }, separator = "\n")
    
    fun getListOfOtherPlayersInGame(excludingPlayer: Player): List<Player> =
        players.filter { it.isInGame && it != excludingPlayer }
    
    val onePlayerIsInGame: Boolean
        get() = players.count { it.isInGame } == 1
    
    /**
     * If there is 1 player left in the game, the name of that player gets returned. Otherwise, null gets returned.
     */
    val winnerName: String?
        get() = players.singleOrNull { it.isInGame }?.name
}