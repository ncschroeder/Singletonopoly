package singletonopoly.board.properties

class SuperStore private constructor(nameStart: String) : Property(name = "$nameStart Super Store") {
    override val curFeeString
        get() =
            if (isUnownedOrPawned) "$0"
            else "A dice roll total multiplied by ${FeeData().multiplier}"
    
    /**
     * Fees for super stores are determined by a dice roll that's multiplied by a constant that differs depending
     * on whether the player that owns a super store that gets landed on owns the other super store as well. This
     * class provides this data.
     */
    class FeeData {
        val onePlayerOwnsBothStores: Boolean =
            newtonStore.owner != null && newtonStore.owner === leibnizStore.owner
        
        val multiplier: Int =
            if (onePlayerOwnsBothStores) BOTH_STORES_OWNED_MULTIPLIER
            else ONE_STORE_OWNED_MULTIPLIER
    }
    
    companion object Manager {
        val newtonStore = SuperStore(nameStart = "Newton")
        val leibnizStore = SuperStore(nameStart = "Leibniz")
        
        private const val ONE_STORE_OWNED_MULTIPLIER = 8
        private const val BOTH_STORES_OWNED_MULTIPLIER = 16
        
        const val FEES_INFO =
            "Fees are a dice roll total multiplied by either $ONE_STORE_OWNED_MULTIPLIER or " +
            "$BOTH_STORES_OWNED_MULTIPLIER: $ONE_STORE_OWNED_MULTIPLIER for if the owner doesn't " +
            "own the other super store as well and $BOTH_STORES_OWNED_MULTIPLIER for if they do."
    }
}
