class SuperStore private constructor(name: String) : Property(name) {
    override val currentFeeString
        get() =
            if (isUnownedOrPawned) "$0"
            else "A dice roll multiplied by ${FeeData().multiplier}"
    
    /**
     * Fees for super stores are determined by a dice roll that's multiplied by a constant that differs depending
     * on whether the player that owns a super store that gets landed on owns the other super store as well. This
     * class provides this data.
     */
    class FeeData {
        val onePlayerOwnsBothStores: Boolean =
            store1.owner != null && store1.owner == store2.owner
        
        val multiplier: Int =
            if (onePlayerOwnsBothStores) bothStoresOwnedMultiplier
            else oneStoreOwnedMultiplier
    }
    
    companion object Manager {
        const val oneStoreOwnedMultiplier = 8
        const val bothStoresOwnedMultiplier = 16
        
        val store1 = SuperStore("Newton Super Store")
        val store2 = SuperStore("Leibniz Super Store")
        
        val bothStores: List<SuperStore>
            get() = listOf(store1, store2)
        
        /**
         * Returns a StringBuilder that contains a heading and info about the stores.
         */
        fun getStoresInfo(includeAll: Boolean): StringBuilder =
            StringBuilder().apply {
                appendLine("Super Stores")
                if (includeAll) {
                    appendLine(
                        "Fees are a dice roll multiplied by either $oneStoreOwnedMultiplier or " +
                        "$bothStoresOwnedMultiplier: $oneStoreOwnedMultiplier for if the owner doesn't own the " +
                        "other super store as well and $bothStoresOwnedMultiplier for if they do"
                    )
                }
                appendLine(store1.basicInfo)
                append(store2.basicInfo)
            }
    }
}