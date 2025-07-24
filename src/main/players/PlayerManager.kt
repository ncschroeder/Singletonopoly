package singletonopoly.players

class PlayerManager(playerNames: List<String>) {
    private val players: List<Player> = playerNames.map(::Player)
    
    private var curPlayerIndex = 0
    
    val curPlayer: Player
        get() = players[curPlayerIndex]
    
    /**
     * Makes curPlayer equal to the player whose turn is next.
     */
    fun switchToNextPlayer(): Player {
        do {
            curPlayerIndex =
                if (curPlayerIndex == players.lastIndex) 0
                else curPlayerIndex + 1
        } while (!curPlayer.isInGame)
        
        return curPlayer
    }
    
    /**
     * Returns a string that contains a heading and info about all players.
     */
    fun getPlayersInfo(): String =
        players.joinToString(prefix = "Players Info\n", transform = { it.getInfo() }, separator = "\n")
    
    fun getOtherPlayersInGame(excludingPlayer: Player): List<Player> =
        players.filter { it.isInGame && it !== excludingPlayer }
    
    val onePlayerIsInGame: Boolean
        get() = players.count { it.isInGame } == 1
    
    /**
     * If there is 1 player left in the game, the name of that player gets returned. Otherwise, null gets returned.
     */
    val winnerName: String?
        get() = players.singleOrNull { it.isInGame }?.name
}
