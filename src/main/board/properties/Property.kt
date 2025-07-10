package singletonopoly.board.properties

import kotlin.math.roundToInt
import singletonopoly.Misc.toStringWithCommas
import singletonopoly.players.Player

/**
 * Superclass for SuperStore, GolfClub, and Street classes.
 */
sealed class Property(val name: String) {
    /**
     * Position on the board. This is set by the Board.
     */
    var position = 0
    
    /**
     * Purchase price for super stores and golf clubs is 512 so this value will be in this class and for
     * streets, this is overridden.
     */
    open val purchasePrice = 512
    
    val purchasePriceString
        get() = purchasePrice.toStringWithCommas()
    
    /**
     * The player that owns this property or null if this property is unowned. This should be set to a player
     * after this property is bought.
     */
    var owner: Player? = null
    
    /**
     * String that says what the current fee is. Super stores have a fee that's determined by a dice roll
     * multiplied by something so this property allows us to say that the current fee is something other
     * than a numeric value.
     */
    protected abstract val curFeeString: String
    
    /**
     * Start of some info strings.
     */
    protected val infoStart
        get() = "$name -> Position: $position"
    
    open fun getBasicInfo() =
        "$infoStart, " + (
            owner
            ?.let { "Owner: ${it.name}, ${if (isPawned) "Pawned" else "Current Fee: $curFeeString"}" }
            ?: "Unowned, Purchase Price: $$purchasePriceString"
        )
    
    open fun makeUnowned() {
        owner = null
        isPawned = false
    }
    
    var isPawned = false
        private set
    
    open val canBePawned: Boolean
        get() = !isPawned
    
    val canBeUnpawned: Boolean
        get() = isPawned
    
    val pawnGain: Int
        get() = purchasePrice / 2
    
    /**
     * Pawn price plus a 10.24% fee.
     */
    val unpawnPrice: Int
        get() = (pawnGain * 1.1024).roundToInt()
    
    fun getPawnInfo() = "$infoStart, Pawn Gain: $$pawnGain"
    
    fun getUnpawnInfo() = "$infoStart, Unpawn Price: $$unpawnPrice"
    
    fun pawn() {
        isPawned = true
    }
    
    fun unpawn() {
        isPawned = false
    }
    
    protected val isUnownedOrPawned: Boolean
        get() = owner == null || isPawned
}
