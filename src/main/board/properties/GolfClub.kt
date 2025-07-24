package singletonopoly.board.properties

import singletonopoly.Misc

class GolfClub private constructor(nameStart: String) : Property(name = "$nameStart Golf Club") {
    /**
     * Fees for golf clubs are determined by how many golf clubs are owned by the player that owns a golf
     * club that gets landed on. This class provides this data.
     */
    inner class FeeData {
        val numClubsOwnedByOwner: Int
        val fee: Int
        
        init {
            if (this@GolfClub.isUnownedOrPawned) {
                numClubsOwnedByOwner = 0
                fee = 0
            } else {
                numClubsOwnedByOwner = _allClubs.count { it.owner === this@GolfClub.owner }
                fee = getFee(numClubsOwnedByOwner)
            }
        }
    }

    override val curFeeString
        get() = "$" + FeeData().fee

    companion object Manager {
        val neoshoClub = GolfClub(nameStart = "Neosho")
        val granbyClub = GolfClub(nameStart = "Granby")
        val monettClub = GolfClub(nameStart = "Monett")
        val auroraClub = GolfClub(nameStart = "Aurora")
        
        private val _allClubs = listOf(neoshoClub, granbyClub, monettClub, auroraClub)
        
        val allClubs
            get() = _allClubs.toList()
        
        private fun getFee(numClubsOwnedByOwner: Int): Int =
            neoshoClub.purchasePrice / (
                when (numClubsOwnedByOwner) {
                    1 -> 8
                    2 -> 4
                    3 -> 2
                    4 -> 1
                    else -> {
                        Misc.printError("numClubsOwnedByOwner arg for getFee was $numClubsOwnedByOwner")
                        neoshoClub.purchasePrice
                    }
                }
            )
        
        val FEES_INFO: String =
            "Fees when the owner owns these amounts of clubs: " +
            (1..4).joinToString(
                transform = { "$it: $${getFee(numClubsOwnedByOwner = it)}" },
                separator = ", "
            )
        
    }
}
