package singletonopoly.board.properties

import singletonopoly.Misc
import singletonopoly.Misc.toStringWithCommas

class Street(name: String, val nHood: Neighborhood) : Property(name) {
    override val purchasePrice: Int = nHood.streetPurchasePrice
    
    val curFee: Int
        get() = when {
            isUnownedOrPawned -> 0
            
            nHood.onePlayerOwnsAllStreets ->
                nHood.getStreetFeeWhenOnePlayerOwnsAllStreets(numRestaurants = this.numRestaurants)
            
            else -> nHood.streetStartingFee
        }
    
    override val curFeeString
        get() = '$' + curFee.toStringWithCommas()
    
    override fun getInfo(): String =
        super.getInfo().let {
            if (numRestaurants == 0) it
            else "$it, Number of Restaurants: $numRestaurants"
        }
    
    override fun makeUnowned() {
        super.makeUnowned()
        removeAllRestaurants()
    }
    
    override val canBePawned: Boolean
        get() = !isPawned && numRestaurants == 0
    
    var numRestaurants = 0
        private set
    
    val restaurantCanBeAdded: Boolean
        get() = !isPawned && numRestaurants < 5 && nHood.onePlayerOwnsAllStreets
    
    val restaurantCanBeRemoved: Boolean
        get() = numRestaurants > 0
    
    fun addRestaurant() {
        if (restaurantCanBeAdded) {
            numRestaurants++
        } else {
            Misc.printError("addRestaurant called on $name, which can't have a restaurant added to it.")
        }
    }
    
    fun removeRestaurant() {
        if (restaurantCanBeRemoved) {
            numRestaurants--
        } else {
            Misc.printError("removeRestaurant called on $name, which can't have a restaurant removed from it.")
        }
    }
    
    fun removeAllRestaurants() {
        numRestaurants = 0
    }
}
