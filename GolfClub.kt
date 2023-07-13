class GolfClub private constructor(name: String) : Property(name) {
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
                numClubsOwnedByOwner = allClubs.count { it.owner == this@GolfClub.owner }
                fee = getFee(numClubsOwnedByOwner)
            }
        }
    }
    
    override val currentFeeString
        get() = "$${FeeData().fee}"

    companion object Manager {
        val allClubs: List<GolfClub> =
            arrayOf("Granby", "Monett", "Neosho", "Aurora")
            .map { GolfClub(name = "$it Golf Club") }
        
        val club1: GolfClub get() = allClubs[0]
        val club2: GolfClub get() = allClubs[1]
        val club3: GolfClub get() = allClubs[2]
        val club4: GolfClub get() = allClubs[3]
        
        private fun getFee(numClubsOwnedByOwner: Int): Int =
            club1.purchasePrice
            .div(
                when (numClubsOwnedByOwner) {
                    1 -> 8
                    2 -> 4
                    3 -> 2
                    4 -> 1
                    else -> {
                        printError("numClubsOwnedByOwner arg for getFee was $numClubsOwnedByOwner")
                        club1.purchasePrice
                    }
                }
            )
        
        private val extraInfo: String =
            "Fees when the owner owns these amounts of clubs: " +
            (1..4).joinToString(
                transform = { "$it: $${getFee(numClubsOwnedByOwner = it)}" },
                separator = ", "
            )
        
        /**
         * Returns a StringBuilder that contains a heading and info about the golf clubs.
         */
        fun getClubsInfo(includeAll: Boolean): StringBuilder =
            buildList(capacity = 6) {
                add("Golf Clubs")
                if (includeAll) {
                    add(extraInfo)
                }
                allClubs.forEach { add(it.basicInfo) }
            }
            .joinTo(StringBuilder(), separator = "\n")
    }
}