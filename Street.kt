class Street(name: String, val neighborhood: Neighborhood) : Property(name) {
    override val purchasePrice: Int
        get() = neighborhood.streetPurchasePrice
    
    val currentFee: Int
        get() = when {
            isUnownedOrPawned -> 0
            neighborhood.onePlayerOwnsAllStreets ->
                neighborhood.getStreetFeeWhen1PlayerOwnsAllStreets(numRestaurants = this.numRestaurants)
            else -> neighborhood.streetStartingFee
        }
    
    override val currentFeeString
        get() = "$$currentFee"
    
    /**
     * Property.basicInfo and if there are any restaurants on this street then the number of restaurants
     * is mentioned as well.
     */
    override val basicInfo: String
        get() =
            super.basicInfo.plus(
                if (numRestaurants == 0) ""
                else ", Number of Restaurants: $numRestaurants"
            )
    
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
            printError("addRestaurant called on $name, which can't have a restaurant added to it")
        }
    }
    
    fun removeRestaurant() {
        if (restaurantCanBeRemoved) {
            numRestaurants--
        } else {
            printError("removeRestaurant called on $name, which can't have a restaurant removed from it")
        }
    }
    
    fun removeAllRestaurants() {
        numRestaurants = 0
    }
    
    val restaurantAddPrice: Int
        get() = neighborhood.restaurantAddPrice
    
    val restaurantRemoveGain: Int
        get() = neighborhood.restaurantRemoveGain
    
    private val restaurantInfoStart
        get() = "$infoStart, Number of Restaurants: $numRestaurants, Current Fee: $$currentFee"
    
    val restaurantAddInfo
        get() = "$restaurantInfoStart, Restaurant Adding Price: $$restaurantAddPrice"
    
    val restaurantRemoveInfo
        get() = "$restaurantInfoStart, Restaurant Removal Gain: $$restaurantRemoveGain"
}