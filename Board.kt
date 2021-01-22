import kotlin.math.roundToInt

/**
 * Board class consists of data and functions about properties and other spaces on the board.
 */
class Board {
    /**
     * The 3 types of properties are streets, golf courses, and super stores. The Property class consists of data and
     * functions common to all of these.
     */
    abstract class Property(val name: String) {
        /**
         * Position on the board. This should only be set by the board during it's instantiation.
         */
        var position = 0

        /**
         * Either "Street", "Golf Course", or "Super Store".
         */
        abstract val type: String

        // Purchase price for golf courses and super stores is 512 so this value will be in this class and for
        // streets, the purchase price is overridden.
        open val purchasePrice = 512

        /**
         * Mentions position, type, and name.
         */
        // For super stores and golf courses, this info will do. For streets, the neighborhood will also
        // be mentioned. This is done by overriding.
        open val lowDetailInfo get() = "Position: $position, Type: $type, Name: $name"

        /**
         * Mentions everything in lowDetailInfo. If this property is owned, the owner name, fee info, and
         * pawn status are also mentioned.
         */
        abstract val moderateDetailInfo: String

        /**
         * The player that owns this property or null if this property is unowned. This should be set to a player
         * after this property is bought.
         */
        var owner: PlayerManager.Player? = null

        /**
         * Equivalent to "owner != null".
         */
        val isOwned get() = owner != null

        /**
         * For all property types, the pawn price is half of the purchase price.
         */
        val pawnPrice get() = purchasePrice / 2

        /**
         * For all property types, the unpawn price is the pawn price plus a 10.24% fee.
         */
        val unpawnPrice: Int
            get() {
                val fee = (pawnPrice * 0.1024).roundToInt()
                return pawnPrice + fee
            }

        var isPawned = false
            private set

        open val canBePawned get() = !isPawned

        val canBeUnpawned get() = isPawned

        fun pawn() {
            isPawned = true
        }

        fun unpawn() {
            isPawned = false
        }

        /**
         * Consists of lowDetailInfo plus the pawn price.
         */
        val pawnInfo get() = "$lowDetailInfo, Pawn Price: $$pawnPrice"

        /**
         * Consists of lowDetailInfo plus the unpawn price.
         */
        val unpawnInfo get() = "$lowDetailInfo, Unpawn Price: $$unpawnPrice"

        open fun makeUnowned() {
            owner = null
            isPawned = false
        }
    }

    inner class Street(name: String, val neighborhoodNumber: Int, val neighborhoodName: String) : Property(name) {
        override val type = "Street"

        override val purchasePrice = neighborhoodNumber * 128
        val startingFee = purchasePrice / 8

        val currentFee
            get() = when {
                !isOwned || isPawned -> 0
                !neighborhoodIsOwnedBySinglePlayer -> startingFee
                else -> getFeeWhenNeighborhoodIsOwned(numberOfRestaurants = this.numberOfRestaurants)
            }

        /**
         * Consists of the position, type, name, and neighborhood.
         */
        override val lowDetailInfo get() = "${super.lowDetailInfo}, Neighborhood: $neighborhoodName"

        /**
         * Mentions everything in lowDetailInfo. If this street is owned, the owner name, number of restaurants,
         * pawn status, and current fee are mentioned as well.
         */
        override val moderateDetailInfo
            get() = "$lowDetailInfo, " +
                    if (isOwned) {
                        "Owner Name: ${owner!!.name},  Number of Restaurants: " +
                                "$numberOfRestaurants,\n\tPawned: ${if (isPawned) "Yes" else "No"}, " +
                                "Current fee: $$currentFee"
                    } else {
                        "Unowned"
                    }

        /**
         * Mentions just about everything that can be mentioned about this street.
         */
        val highDetailInfo: String
            get() {
                val sb = StringBuilder("$lowDetailInfo, ${if (isOwned) "Owner Name: ${owner!!.name}" else "Unowned"}, ")
                        .append("Purchase Price: $$purchasePrice, Pawned: ${if (isPawned) "Yes" else "No"}, ")
                        .append("Pawn Price: $$pawnPrice, Unpawn Price: $$unpawnPrice,\n\tNumber of restaurants: ")
                        .append("$numberOfRestaurants, restaurant adding price: $$restaurantAddPrice, restaurant ")
                        .append("remove gain: $$restaurantRemoveGain, is neighborhood owned by single player? ")
                        .append(if (neighborhoodIsOwnedBySinglePlayer) "yes" else "no")
                        .append(", Current fee: $$currentFee,\n\tFee when neighborhood is not owned ")
                        .append("by single player: $$startingFee, Fees when neighborhood is owned by single player: ")

                for (numberOfRestaurants in 0..5) {
                    sb.append("$numberOfRestaurants ${if (numberOfRestaurants == 1) "restaurant" else "restaurants"}: $")
                            .append(getFeeWhenNeighborhoodIsOwned(numberOfRestaurants = numberOfRestaurants))
                    if (numberOfRestaurants != 5) {
                        sb.append(", ")
                    }
                    if (numberOfRestaurants == 1) {
                        sb.append("\n\t")
                    }
                }
                return sb.toString()
            }

        override val canBePawned get() = !isPawned && numberOfRestaurants == 0

        /**
         * @throws IllegalArgumentException if this is set to an Int that is not in the range of 0 to 5 inclusive.
         */
        var numberOfRestaurants = 0
            private set(value) {
                if (value !in 0..5) {
                    throw IllegalArgumentException("Invalid number of restaurants: $value")
                }
                field = value
            }

        /**
         * @throws IllegalArgumentException if this method is called on a street that already has the maximum amount
         * of restaurants, which is 5.
         */
        fun addRestaurant() {
            numberOfRestaurants++
        }

        /**
         * @throws IllegalArgumentException if this method is called on a street that doesn't have any restaurants.
         */
        fun removeRestaurant() {
            numberOfRestaurants--
        }

        fun removeAllRestaurants() {
            numberOfRestaurants = 0
        }

        val restaurantAddPrice = purchasePrice / 2
        val restaurantRemoveGain = restaurantAddPrice / 2

        val restaurantCanBeAdded get() = neighborhoodIsOwnedBySinglePlayer && !isPawned && numberOfRestaurants < 5
        val restaurantCanBeRemoved get() = numberOfRestaurants > 0

        /**
         * Mentions everything in lowDetailInfo, the number of restaurants, current fee, and the fee for adding a
         * restaurant to this street.
         */
        val restaurantAddInfo
            get() = "$lowDetailInfo, Number of restaurants: $numberOfRestaurants, " +
                    "Current Fee: $$currentFee, Restaurant Adding Price: $$restaurantAddPrice"

        /**
         * Mentions everything in lowDetailInfo, the number of restaurants, current fee, and the money a player gains
         * when they remove a restaurant from this street.
         */
        val restaurantRemoveInfo
            get() = "$lowDetailInfo, Number of restaurants: $numberOfRestaurants, " +
                    "Current Fee: $$currentFee, Restaurant Removal Gain: $$restaurantRemoveGain"

        /**
         * A list of streets that are in the same neighborhood as this street, which means that this street
         * is in this list.
         */
        private val streetsInSameNeighborhood get() = neighborhoods.getValue(neighborhoodNumber)

        val neighborhoodIsOwnedBySinglePlayer: Boolean
            get() {
                if (!this.isOwned) {
                    return false
                }
                var numberOfStreetsInNeighborhoodOwned = 0
                for (street in streetsInSameNeighborhood) {
                    if (street.owner == this.owner) {
                        numberOfStreetsInNeighborhoodOwned++
                    }
                }
                return numberOfStreetsInNeighborhoodOwned == 3
            }

        /**
         * @throws IllegalArgumentException if the numberOfRestaurants argument is not in the range of 0 to 5 inclusive.
         */
        private fun getFeeWhenNeighborhoodIsOwned(numberOfRestaurants: Int) =
                when (numberOfRestaurants) {
                    0 -> startingFee * 2
                    1 -> startingFee * 4
                    2 -> startingFee * 6
                    3 -> startingFee * 8
                    4 -> startingFee * 10
                    5 -> startingFee * 12
                    else -> throw IllegalArgumentException("Invalid number of restaurants: $numberOfRestaurants")
                }

        /**
         * The number of restaurants in the same neighborhood as this street.
         */
        val neighborhoodRestaurantCount: Int
            get() {
                var restaurantCount = 0
                for (street in streetsInSameNeighborhood) {
                    restaurantCount += street.numberOfRestaurants
                }
                return restaurantCount
            }

        fun removeRestaurantsFromNeighborhood() {
            for (street in streetsInSameNeighborhood) {
                street.removeAllRestaurants()
            }
        }

        override fun makeUnowned() {
            super.makeUnowned()
            removeAllRestaurants()
        }
    }

    inner class SuperStore(name: String) : Property(name) {
        override val type = "Super Store"

        override val moderateDetailInfo
            get() = "$lowDetailInfo, " +
                    if (isOwned) {
                        "Owner Name: ${owner!!.name}, Pawned: ${if (isPawned) "Yes" else "No"}, " +
                                "Current Fee: A dice roll multiplied by ${FeeData().multiplier}"
                    } else {
                        "Unowned"
                    }

        /**
         * Fees for super stores are determined by a dice roll and this dice roll is multiplied by a constant that
         * differs depending on whether the player that owns the super store that gets landed on owns the other super
         * store on the board as well. This class consists of 2 read-only properties to provide this data at the time
         * this class is instantiated. The properties are Boolean bothSuperStoresOwnedBySamePlayer and Int multiplier,
         * which represents what a dice roll must be multiplied by to get the current fee for this super store. If this
         * function is called with a super store that is not owned then bothSuperStoresOwnedBySamePlayer will be false
         * and multiplier will be 0. If this function is called with a super store that is pawned then
         * bothSuperStoresOwnedBySamePlayer will be accurate but multiplier will be 0.
         */
        inner class FeeData {
            val bothSuperStoresOwnedBySamePlayer: Boolean
            val multiplier: Int

            init {
                if (isOwned) {
                    val otherSuperStore = superStores.single { it != this@SuperStore }
                    bothSuperStoresOwnedBySamePlayer = this@SuperStore.owner == otherSuperStore.owner
                    multiplier =
                            when {
                                isPawned -> 0
                                bothSuperStoresOwnedBySamePlayer -> 16
                                else -> 8
                            }
                } else {
                    bothSuperStoresOwnedBySamePlayer = false
                    multiplier = 0
                }
            }
        }
    }

    inner class GolfCourse(name: String) : Property(name) {
        override val type = "Golf Course"

        override val moderateDetailInfo
            get() = "$lowDetailInfo, " +
                    if (isOwned) {
                        "Owner Name: ${owner!!.name}, Pawned: ${if (isPawned) "Yes" else "No"}, " +
                                "Current Fee: $${FeeData().fee}"
                    } else {
                        "Unowned"
                    }

        /**
         * Fees for golf courses are determined by how many golf courses are owned by the player that owns a golf course
         * that gets landed on. This class consists of 2 read-only Int properties to provide this data at the time this
         * class is instantiated. The properties are numberOfGolfCoursesOwnedByOwner and fee. If this function is
         * called with a golf course that is unowned then both properties will be 0. If this function is called with
         * a golf course that is pawned then numberOfGolfCoursesOwnedByOwner will be accurate but fee will be 0.
         */
        inner class FeeData {
            var numberOfGolfCoursesOwnedByOwner = 0
            var fee = 0

            init {
                if (isOwned) {
                    for (gc in golfCourses) {
                        if (gc.owner == this@GolfCourse.owner) {
                            numberOfGolfCoursesOwnedByOwner++
                        }
                    }
                    fee =
                            if (isPawned) 0
                            else when (numberOfGolfCoursesOwnedByOwner) {
                                1 -> purchasePrice / 8
                                2 -> purchasePrice / 4
                                3 -> purchasePrice / 2
                                4 -> purchasePrice
                                else -> throw Exception("invalid number of golf courses owned: $numberOfGolfCoursesOwnedByOwner")
                            }
                }
            }
        }
    }

    enum class NonPropertySpace {
        START,
        DRAW_ACTION_CARD,
        BREAK_TIME,
        VACATION,
        GO_ON_VACATION;

        /**
         * @return Either "Start", "Draw Action Card", "Break Time", "Vacation", or "Go On Vacation".
         */
        override fun toString() = super.toString().toLowerCase().split("_").map { word -> word.capitalize() }.joinToString(" ")
    }

    /**
     * Consists of NonPropertySpaces and Properties that are ordered according to their position on the board.
     */
    private val boardSpaces = arrayOf(
            // Numbers in comments are the positions of each space on the board
            NonPropertySpace.START, // 1
            Street(name = "Page Street", neighborhoodNumber = 1, neighborhoodName = "Lambeth"), // 2
            Street(name = "Victoria Street", neighborhoodNumber = 1, neighborhoodName = "Lambeth"), // 3
            Street(name = "Nottingham Avenue", neighborhoodNumber = 1, neighborhoodName = "Lambeth"), // 4
            GolfCourse(name = "Granby Golf Club"), // 5
            Street(name = "Luanda Street", neighborhoodNumber = 2, neighborhoodName = "Monrovia"), // 6
            NonPropertySpace.DRAW_ACTION_CARD, // 7
            Street(name = "Kinshasa Street", neighborhoodNumber = 2, neighborhoodName = "Monrovia"), // 8
            Street(name = "Lagos Avenue", neighborhoodNumber = 2, neighborhoodName = "Monrovia"), // 9
            SuperStore(name = "Newton Super Store"), // 10
            NonPropertySpace.BREAK_TIME, // 11
            Street(name = "Osage Avenue", neighborhoodNumber = 3, neighborhoodName = "Vauxhall"), // 12
            Street(name = "Camden Avenue", neighborhoodNumber = 3, neighborhoodName = "Vauxhall"), //13
            NonPropertySpace.DRAW_ACTION_CARD, // 14
            Street(name = "Ozark Avenue", neighborhoodNumber = 3, neighborhoodName = "Vauxhall"), // 15
            Street(name = "Sullivan Avenue", neighborhoodNumber = 4, neighborhoodName = "Shadyside"), // 16
            NonPropertySpace.VACATION, // 17
            Street(name = "Labadie Street", neighborhoodNumber = 4, neighborhoodName = "Shadyside"), // 18
            GolfCourse(name = "Monett Golf Club"), // 19
            Street(name = "Augusta Street", neighborhoodNumber = 4, neighborhoodName = "Shadyside"), // 20
            Street(name = "Ezio Avenue", neighborhoodNumber = 5, neighborhoodName = "Little Italy"), // 21
            NonPropertySpace.DRAW_ACTION_CARD, // 22
            Street(name = "Venezia Street", neighborhoodNumber = 5, neighborhoodName = "Little Italy"), // 23
            Street(name = "Firenze Street", neighborhoodNumber = 5, neighborhoodName = "Little Italy"), // 24
            GolfCourse(name = "Neosho Golf Club"), // 25
            Street(name = "Euler Avenue", neighborhoodNumber = 6, neighborhoodName = "Gauss"), // 26
            Street(name = "Ramanujan Street", neighborhoodNumber = 6, neighborhoodName = "Gauss"), // 27
            NonPropertySpace.DRAW_ACTION_CARD, // 28
            Street(name = "Euclid Avenue", neighborhoodNumber = 6, neighborhoodName = "Gauss"), // 29
            NonPropertySpace.BREAK_TIME, // 30
            NonPropertySpace.GO_ON_VACATION, // 31
            Street(name = "Dijkstra Street", neighborhoodNumber = 7, neighborhoodName = "Gates"), // 32
            Street(name = "Knuth Street", neighborhoodNumber = 7, neighborhoodName = "Gates"), // 33
            SuperStore(name = "Leibniz Super Store"), // 34
            NonPropertySpace.DRAW_ACTION_CARD, // 35
            Street(name = "Ritchie Avenue", neighborhoodNumber = 7, neighborhoodName = "Gates"), // 36
            Street(name = "Phoenix Avenue", neighborhoodNumber = 8, neighborhoodName = "Nicosia"), // 37
            GolfCourse(name = "Aurora Golf Club"), // 38
            NonPropertySpace.DRAW_ACTION_CARD, // 39
            Street(name = "Louisville Avenue", neighborhoodNumber = 8, neighborhoodName = "Nicosia"), // 40
            Street(name = "Norfolk Street", neighborhoodNumber = 8, neighborhoodName = "Nicosia") // 41
    )

    val numberOfSpaces = boardSpaces.size

    /**
     * A map where the keys are neighborhood numbers and the values are a list of streets that belong to
     * that neighborhood.
     */
    private val neighborhoods = mutableMapOf<Int, List<Street>>()

    init {
        // Fill neighborhoods map
        val streets = boardSpaces.filterIsInstance<Street>()
        for (neighborhoodNumber in 1..8) {
            neighborhoods[neighborhoodNumber] = streets.filter { it.neighborhoodNumber == neighborhoodNumber }
        }

        // Set positions of all properties
        for ((index, space) in boardSpaces.withIndex()) {
            if (space is Property) {
                space.position = index + 1
            }
        }
    }

    /**
     * A list of golf courses that are on the board.
     */
    private val golfCourses = boardSpaces.filterIsInstance<GolfCourse>()

    /**
     * A list of super stores that are on the board.
     */
    private val superStores = boardSpaces.filterIsInstance<SuperStore>()

    /**
     * @return An Any object for the object at the space of the position argument. This object can be casted as a
     * NonPropertySpace or one of the Property types, depending on what is on the board at this position.
     *
     * @throws ArrayIndexOutOfBoundsException if position is less than 1 or greater than numberOfSpaces.
     */
    fun getBoardSpace(position: Int) = boardSpaces[position - 1]

    /**
     * Is equal to the position of the "Vacation" space on the board. Should be used by the player manager so that
     * players know which position to go to when they are sent to vacation.
     * @throws IllegalStateException if there is no space on the board that is a "Vacation" string.
     */
    val vacationPosition: Int
        get() {
            val index = boardSpaces.indexOf(NonPropertySpace.VACATION)
            if (index == -1) {
                throw IllegalStateException("no vacation position on board")
            }
            return index + 1
        }

    /**
     * @return A map whose keys are property names that come from the propertyNames argument. The values are the
     * corresponding positions for those properties.
     *
     * @throws IllegalArgumentException if there is not a corresponding property for any of the property names in the
     * propertyNames argument.
     */
    fun getPropertyPositions(propertyNames: Array<String>): Map<String, Int> {
        val propertyPositions = mutableMapOf<String, Int>()

        // Initialize entries to have a value of 0
        for (name in propertyNames) {
            propertyPositions[name] = 0
        }

        for (space in boardSpaces) {
            if (space is Property && space.name in propertyPositions.keys) {
                propertyPositions[space.name] = space.position
            }
        }

        for (propertyPosition in propertyPositions) {
            // The following condition will be true if a property was not found on the board. Throw an exception for
            // this situation.
            if (propertyPosition.value == 0) {
                val propertyName = propertyPosition.key
                throw IllegalArgumentException("$propertyName was not found on the board")
            }
        }

        return propertyPositions
    }

    val spacesAndSimplePropertyInfo: String
        get() {
            val sb = StringBuilder("Board Spaces")
            for ((index, space) in boardSpaces.withIndex()) {
                sb.append("\n").append(
                        when (space) {
                            is NonPropertySpace -> "Position: ${index + 1}, Space: \"$space\""
                            is Property -> space.lowDetailInfo
                            else -> throw Exception("Invalid type at position ${index + 1}")
                        }
                )
            }
            return sb.toString()
        }

    val moderateDetailPropertyInfo: String
        get() {
            val sb = StringBuilder("Moderately Detailed Property Info")
            for (space in boardSpaces) {
                if (space is Property) {
                    sb.append("\n").append(space.moderateDetailInfo)
                }
            }
            return sb.toString()
        }

    fun getModerateDetailPropertyInfoOfPlayer(player: PlayerManager.Player): String {
        if (playerHasAProperty(player)) {
            val sb = StringBuilder("Moderately detailed info for properties owned by ${player.name}")
            for (space in boardSpaces) {
                if (space is Property && space.owner == player) {
                    sb.append("\n").append(space.moderateDetailInfo)
                }
            }
            return sb.toString()
        } else {
            return "${player.name} doesn't own any properties"
        }
    }

    val moderateDetailStreetInfo: String
        get() {
            val sb = StringBuilder("Moderately Detailed Street Info")
            for (space in boardSpaces) {
                if (space is Street) {
                    sb.append("\n").append(space.moderateDetailInfo)
                }
            }
            return sb.toString()
        }

    val highDetailStreetInfo: String
        get() {
            val sb = StringBuilder("Highly Detailed Street Info")
            for (space in boardSpaces) {
                if (space is Street) {
                    sb.append("\n").append(space.highDetailInfo)
                }
            }
            return sb.toString()
        }

    val golfCourseInfo: String
        get() {
            val sb = StringBuilder("Golf Course Info")
            for (golfCourse in golfCourses) {
                sb.append('\n').append(golfCourse.moderateDetailInfo)
            }
            return sb.toString()
        }

    val superStoreInfo get() = "Super Store Info\n${superStores[0].moderateDetailInfo}\n${superStores[1].moderateDetailInfo}"

    /**
     * @return true if the player passed in as an argument has at least 1 property and false otherwise.
     */
    fun playerHasAProperty(player: PlayerManager.Player): Boolean {
        for (space in boardSpaces) {
            if (space is Property && space.owner == player) {
                return true
            }
        }
        return false
    }

    /**
     * @return The number of restaurants owned by the player that was passed in as an argument.
     */
    fun getRestaurantCount(player: PlayerManager.Player): Int {
        var count = 0
        for (space in boardSpaces) {
            if (space is Street && space.owner == player) {
                count += space.numberOfRestaurants
            }
        }
        return count
    }

    /**
     * @return A map whose keys are strings of the positions of properties owned by the player that was passed in as
     * an argument. The values are the property objects at those positions.
     */
    fun getPropertyMap(player: PlayerManager.Player): Map<String, Property> {
        val propertyMap = mutableMapOf<String, Property>()
        for (space in boardSpaces) {
            if (space is Property && space.owner == player) {
                propertyMap[space.position.toString()] = space
            }
        }
        return propertyMap
    }

    /**
     * @return A map whose keys are strings of positions of properties that can be pawned that are owned by the
     * player that was passed in as an argument. The values are the corresponding property objects.
     */
    fun getPawnablePropertyMap(player: PlayerManager.Player): Map<String, Property> {
        val pawnablePropertiesMap = mutableMapOf<String, Property>()
        for (space in boardSpaces) {
            if (space is Property && space.owner == player && space.canBePawned) {
                pawnablePropertiesMap[space.position.toString()] = space
            }
        }
        return pawnablePropertiesMap
    }

    /**
     * @return A map whose keys are strings of positions of properties that can be unpawned that are owned by
     * the player that was passed in as an argument. The values are the corresponding property objects.
     */
    fun getUnpawnablePropertyMap(player: PlayerManager.Player): Map<String, Property> {
        val unpawnablePropertiesMap = mutableMapOf<String, Property>()
        for (space in boardSpaces) {
            if (space is Property && space.owner == player && space.canBeUnpawned) {
                unpawnablePropertiesMap[space.position.toString()] = space
            }
        }
        return unpawnablePropertiesMap
    }

    /**
     * @return A map whose keys are strings of positions of streets where a restaurant can be added that are owned
     * by the player that was passed in as an argument. The values are the corresponding street objects.
     */
    fun getStreetsWhereRestaurantCanBeAdded(player: PlayerManager.Player): Map<String, Street> {
        val streetsMap = mutableMapOf<String, Street>()
        for (space in boardSpaces) {
            if (space is Street && space.owner == player && space.restaurantCanBeAdded) {
                streetsMap[space.position.toString()] = space
            }
        }
        return streetsMap
    }

    /**
     * @return A map whose keys are strings of positions of streets where a restaurant can be removed that are owned
     * by the player that was passed in as an argument. The values are the corresponding street objects.
     */
    fun getStreetsWhereRestaurantCanBeRemoved(player: PlayerManager.Player): Map<String, Street> {
        val streetsMap = mutableMapOf<String, Street>()
        for (space in boardSpaces) {
            if (space is Street && space.owner == player && space.restaurantCanBeRemoved) {
                streetsMap[space.position.toString()] = space
            }
        }
        return streetsMap
    }

    enum class PropertyAction {
        PAWN,
        UNPAWN,
        ADD_RESTAURANT,
        REMOVE_RESTAURANT
    }

    /**
     * This function can be used to find out what the player is capable of doing with their properties.
     *
     * @return A collection that consists of instances of the PropertyAction enum class. The different actions
     * are PAWN, UNPAWN, ADD_RESTAURANT, and REMOVE_RESTAURANT. The instances that are in the collection returned by
     * this function are the property actions that the player passed in as an argument can do.
     */
    fun getPossiblePropertyActions(player: PlayerManager.Player): Collection<PropertyAction> {
        val actionsSet = mutableSetOf<PropertyAction>()
        for (space in boardSpaces) {
            if (space is Property && space.owner == player) {
                if (space.canBePawned) {
                    actionsSet.add(PropertyAction.PAWN)
                }
                if (space.canBeUnpawned) {
                    actionsSet.add(PropertyAction.UNPAWN)
                }
                if (space is Street) {
                    if (space.restaurantCanBeAdded) {
                        actionsSet.add(PropertyAction.ADD_RESTAURANT)
                    }
                    if (space.restaurantCanBeRemoved) {
                        actionsSet.add(PropertyAction.REMOVE_RESTAURANT)
                    }
                }
            }
        }
        return actionsSet
    }

    /**
     * Can be used to determine what a player can do with their properties when they need money immediately.
     *
     * @return A collection that consists of instances of the PropertyAction enum class. The different actions
     * are PAWN and REMOVE_RESTAURANT. The instances that are in the collection returned by this function are the
     * actions that the player passed in as an argument can do.
     */
// getPossiblePropertyActions already does the work needed so I will just use the collection returned by that function
// but keep only the PAWN and REMOVE_RESTAURANT instances.
    fun getMoneyGainActions(player: PlayerManager.Player): Collection<PropertyAction> =
            getPossiblePropertyActions(player).filter { it == PropertyAction.PAWN || it == PropertyAction.REMOVE_RESTAURANT }

    /**
     * Makes all the properties that are owned by the argument player unowned.
     */
    fun makePropertiesUnowned(player: PlayerManager.Player) {
        for (space in boardSpaces) {
            if (space is Property && space.owner == player) {
                space.makeUnowned()
            }
        }
    }

    /**
     * Sets the owner of all properties owned by the currentOwner argument to the newOwner argument.
     */
    fun transferOwnership(currentOwner: PlayerManager.Player, newOwner: PlayerManager.Player) {
        for (space in boardSpaces) {
            if (space is Property && space.owner == currentOwner) {
                space.owner = newOwner
            }
        }
    }
}