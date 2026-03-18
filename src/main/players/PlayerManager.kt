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
    
    fun getPlayerInfo(): String =
        players.joinToString(prefix = "Player Info\n", transform = { it.getInfo() }, separator = "\n")
    
    fun getOtherPlayersInGame(excludingPlayer: Player): List<Player> =
        players.filter { it.isInGame && it !== excludingPlayer }
    
    val onePlayerIsInGame: Boolean
        get() = players.count { it.isInGame } == 1
    
    val winnerName: String?
        get() = players.singleOrNull { it.isInGame }?.name
}
