package singletonopoly.board.properties

import singletonopoly.Misc
import singletonopoly.Misc.toStringWithCommas

class Street(name: String, val neighborhood: Neighborhood) : Property(name) {
    override val purchasePrice: Int
        get() = neighborhood.streetPurchasePrice
    
    val curFee: Int
        get() = when {
            isUnownedOrPawned -> 0
            
            neighborhood.onePlayerOwnsAllStreets ->
                neighborhood.getStreetFeeWhenOnePlayerOwnsAllStreets(numRestaurants = this.numRestaurants)
            
            else -> neighborhood.streetStartingFee
        }
    
    override val curFeeString
        get() = '$' + curFee.toStringWithCommas()
    
    /**
     * Property.basicInfo and if there are any restaurants on this street then the number of restaurants
     * is mentioned as well.
     */
    override fun getBasicInfo(): String =
        super.getBasicInfo().let {
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
        get() = !isPawned && numRestaurants < 5 && neighborhood.onePlayerOwnsAllStreets
    
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
    
    val restaurantAddPrice: Int = neighborhood.restaurantAddPrice
    val restaurantRemovalGain: Int = neighborhood.restaurantRemovalGain
    
    private fun getRestaurantInfoStart() =
        "$infoStart, Number of Restaurants: $numRestaurants, Current Fee: $curFeeString"
    
    fun getRestaurantAddInfo() =
        "${getRestaurantInfoStart()}, Restaurant Adding Price: $$restaurantAddPrice"
    
    fun getRestaurantRemovalInfo() =
        "${getRestaurantInfoStart()}, Restaurant Removal Gain: $$restaurantRemovalGain"
}
