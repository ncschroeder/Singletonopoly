import java.lang.Exception
import java.lang.IllegalArgumentException
import java.lang.StringBuilder
import kotlin.math.roundToInt

/**
 * Board class consists of data and functions about properties and other spaces on the board.
 */
class Board {
    /**
     * The 3 types of properties are streets, golf courses, and super stores. The Property class consists of data and
     * functions common to all of these.
     */
    abstract inner class Property(val name: String) {
        /**
         * Position on the board. This should only be set by the board during it's instantiation.
         */
        var position = 0

        /**
         * Either "Street", "Golf Course", or "Super Store".
         */
        abstract val type: String


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

        // Purchase price for golf courses and super stores is 512 so this value will be in this class and for
        // streets, the purchase price is overridden.
        open val purchasePrice = 512

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
                return "$lowDetailInfo, ${if (isOwned) "Owner Name: ${owner!!.name}" else "Unowned"}, " +
                        "Purchase Price: $$purchasePrice, Pawned: ${if (isPawned) "Yes" else "No"}, " +
                        "Pawn Price: $$pawnPrice, Unpawn Price: $$unpawnPrice,\n\t" +
                        "Number of Restaurants: $numberOfRestaurants, " +
                        "Restaurant Adding Price: $$restaurantAddPrice, Restaurant Removal Gain: " +
                        "$$restaurantRemoveGain, Is neighborhood owned by single player? " +
                        (if (neighborhoodIsOwnedBySinglePlayer) "Yes" else "No") +
                        ", Current fee: $$currentFee,\n\tFee when neighborhood is not owned " +
                        "by single player: $$startingFee, Fees when neighborhood is owned by single player: " +
                        "0 restaurants: $${getFeeWhenNeighborhoodIsOwned(numberOfRestaurants = 0)}, " +
                        "1 restaurant: $${getFeeWhenNeighborhoodIsOwned(numberOfRestaurants = 1)},\n\t" +
                        "2 restaurants: $${getFeeWhenNeighborhoodIsOwned(numberOfRestaurants = 2)}, " +
                        "3 restaurants: $${getFeeWhenNeighborhoodIsOwned(numberOfRestaurants = 3)}, " +
                        "4 restaurants: $${getFeeWhenNeighborhoodIsOwned(numberOfRestaurants = 4)}, " +
                        "5 restaurants: $${getFeeWhenNeighborhoodIsOwned(numberOfRestaurants = 5)}"
            }

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

        val currentFee
            get() = when {
                !isOwned || isPawned -> 0
                !neighborhoodIsOwnedBySinglePlayer -> startingFee
                else -> getFeeWhenNeighborhoodIsOwned(numberOfRestaurants = this.numberOfRestaurants)
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

        override val purchasePrice = neighborhoodNumber * 128
        val startingFee = purchasePrice / 8
        val restaurantAddPrice = purchasePrice / 2
        val restaurantRemoveGain = restaurantAddPrice / 2

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
         * @throws IllegalStateException if this method is called on a street that already has the maximum amount
         * of restaurants, which is 5.
         */
        fun addRestaurant() {
            if (numberOfRestaurants == 5) {
                throw IllegalStateException("Cannot add a restaurant to $name since it has 5 restaurants")
            }
            numberOfRestaurants++
        }

        /**
         * @throws IllegalStateException if this method is called on a street that doesn't have any restaurants.
         */
        fun removeRestaurant() {
            if (numberOfRestaurants == 0) {
                throw IllegalStateException(
                        "Cannot remove a restaurant from $name, since it doesn't have any restaurants"
                )
            }
            numberOfRestaurants--
        }

        fun removeAllRestaurants() {
            numberOfRestaurants = 0
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
                        val multiplier = getFeeData(totalDiceRoll = 0).getValue("multiplier")
                        "Owner Name: ${owner!!.name}, Pawned: ${if (isPawned) "Yes" else "No"}, " +
                                "Current Fee: A dice roll multiplied by $multiplier"
                    } else {
                        "Unowned"
                    }

        /**
         * @param totalDiceRoll Should be the sum of 2 dice rolls.
         *
         * @return An map that consists of 3 entries. The first has the key "both super stores owned by same person"
         * and the value for this is an Any that can be cast as a Boolean. The second entry has the key "multiplier"
         * and the value for this is what the totalDiceRoll argument is multiplied by to get the fee. The third has
         * the key "fee" and the value is an Any that can be cast as an Int.
         */
        fun getFeeData(totalDiceRoll: Int): Map<String, Any> {
            val bothSuperStoresOwnedBySamePerson: Boolean
            val multiplier: Int
            val fee: Int
            if (this.isOwned) {
                val otherSuperStore = superStores.single { it != this }
                bothSuperStoresOwnedBySamePerson = otherSuperStore.owner == this.owner
                multiplier = if (bothSuperStoresOwnedBySamePerson) 16 else 8
                fee = totalDiceRoll * multiplier
            } else {
                bothSuperStoresOwnedBySamePerson = false
                multiplier = 0
                fee = 0
            }

            return mapOf(
                    "both super stores owned by same person" to bothSuperStoresOwnedBySamePerson,
                    "multiplier" to multiplier,
                    "fee" to fee
            )
        }
    }

    inner class GolfCourse(name: String) : Property(name) {
        override val type = "Golf Course"

        override val moderateDetailInfo
            get() = "$lowDetailInfo, " +
                    if (isOwned) {
                        "Owner Name: ${owner!!.name}, Pawned: ${if (isPawned) "Yes" else "No"}, " +
                                "Current Fee: $${feeData.getValue("fee")}"
                    } else {
                        "Unowned"
                    }

        /**
         * A map that consists of 2 entries. The first has the key "number of golf courses owned", and the value of
         * this is the number of golf courses that the owner of this golf course owns. The second entry has the key
         * "fee" and the value is the fee, which is based on the number of golf courses owned.
         */
        val feeData: Map<String, Int>
            get() {
                var numberOfGolfCoursesOwned = 0
                val fee: Int
                if (this.isOwned) {
                    for (golfCourse in golfCourses) {
                        if (golfCourse.owner == this.owner) {
                            numberOfGolfCoursesOwned++
                        }
                    }
                    fee = when (numberOfGolfCoursesOwned) {
                        1 -> purchasePrice / 8
                        2 -> purchasePrice / 4
                        3 -> purchasePrice / 2
                        4 -> purchasePrice
                        else -> throw Exception("Invalid number of golf courses: $numberOfGolfCoursesOwned")
                    }
                } else {
                    fee = 0
                }

                return mapOf(
                        "number of golf courses owned" to numberOfGolfCoursesOwned,
                        "fee" to fee
                )
            }
    }

    /**
     * Consists of strings and properties that are ordered according to their position on the board.
     */
    private val boardSpaces = arrayOf(
            // Numbers in comments are the positions of each space on the board
            "Start", // 1
            Street(name = "Page Street", neighborhoodNumber = 1, neighborhoodName = "Lambeth"), // 2
            Street(name = "Victoria Street", neighborhoodNumber = 1, neighborhoodName = "Lambeth"), // 3
            Street(name = "Nottingham Avenue", neighborhoodNumber = 1, neighborhoodName = "Lambeth"), // 4
            GolfCourse(name = "Granby Golf Club"), // 5
            Street(name = "Luanda Street", neighborhoodNumber = 2, neighborhoodName = "Monrovia"), // 6
            "Draw Action Card", // 7
            Street(name = "Kinshasa Street", neighborhoodNumber = 2, neighborhoodName = "Monrovia"), // 8
            Street(name = "Lagos Avenue", neighborhoodNumber = 2, neighborhoodName = "Monrovia"), // 9
            SuperStore(name = "Newton Super Store"), // 10
            "Break Time", // 11
            Street(name = "Osage Avenue", neighborhoodNumber = 3, neighborhoodName = "Vauxhall"), // 12
            Street(name = "Camden Avenue", neighborhoodNumber = 3, neighborhoodName = "Vauxhall"), //13
            "Draw Action Card", // 14
            Street(name = "Ozark Avenue", neighborhoodNumber = 3, neighborhoodName = "Vauxhall"), // 15
            Street(name = "Sullivan Avenue", neighborhoodNumber = 4, neighborhoodName = "Shadyside"), // 16
            "Vacation", // 17
            Street(name = "Labadie Street", neighborhoodNumber = 4, neighborhoodName = "Shadyside"), // 18
            GolfCourse(name = "Monett Golf Club"), // 19
            Street(name = "Augusta Street", neighborhoodNumber = 4, neighborhoodName = "Shadyside"), // 20
            Street(name = "Ezio Avenue", neighborhoodNumber = 5, neighborhoodName = "Little Italy"), // 21
            "Draw Action Card", // 22
            Street(name = "Venezia Street", neighborhoodNumber = 5, neighborhoodName = "Little Italy"), // 23
            Street(name = "Firenze Street", neighborhoodNumber = 5, neighborhoodName = "Little Italy"), // 24
            GolfCourse(name = "Neosho Golf Club"), // 25
            Street(name = "Euler Avenue", neighborhoodNumber = 6, neighborhoodName = "Gauss"), // 26
            Street(name = "Ramanujan Street", neighborhoodNumber = 6, neighborhoodName = "Gauss"), // 27
            "Draw Action Card", // 28
            Street(name = "Euclid Avenue", neighborhoodNumber = 6, neighborhoodName = "Gauss"), // 29
            "Break Time", // 30
            "Go On Vacation", // 31
            Street(name = "Dijkstra Street", neighborhoodNumber = 7, neighborhoodName = "Gates"), // 32
            Street(name = "Knuth Street", neighborhoodNumber = 7, neighborhoodName = "Gates"), // 33
            SuperStore(name = "Leibniz Super Store"), // 34
            "Draw Action Card", // 35
            Street(name = "Ritchie Avenue", neighborhoodNumber = 7, neighborhoodName = "Gates"), // 36
            Street(name = "Phoenix Avenue", neighborhoodNumber = 8, neighborhoodName = "Nicosia"), // 37
            GolfCourse(name = "Aurora Golf Club"), // 38
            "Draw Action Card", // 39
            Street(name = "Louisville Avenue", neighborhoodNumber = 8, neighborhoodName = "Nicosia"), // 40
            Street(name = "Norfolk Street", neighborhoodNumber = 8, neighborhoodName = "Nicosia") // 41
    )

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

    /**
     * A list of golf courses that are on the board.
     */
    private val golfCourses = boardSpaces.filterIsInstance<GolfCourse>()

    /**
     * A list of super stores that are on the board.
     */
    private val superStores = boardSpaces.filterIsInstance<SuperStore>()

    /**
     * A map where the keys are neighborhood numbers and the values are a list of streets that belong to
     * that neighborhood.
     */
    private val neighborhoods = mutableMapOf<Int, List<Street>>()

    init {
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

    val numberOfSpaces = boardSpaces.size

    /**
     * Should be used by the player manager so that players know which position to go to when they are sent to vacation.
     * @return The position of the "Vacation" space on the board.
     * @throws IllegalStateException if there is no space on the board that is a "Vacation" string.
     */
    fun getVacationPosition(): Int {
        for ((index, space) in boardSpaces.withIndex()) {
            if (space == "Vacation") {
                return index + 1
            }
        }
        throw IllegalStateException("There is no space that is a \"Vacation\" string")
    }

    val spacesAndSimplePropertyInfo: String
        get() {
            val sb = StringBuilder()
            sb.append("Board Spaces")
            for ((index, space) in boardSpaces.withIndex()) {
                sb.append("\n").append(
                        when (space) {
                            is String -> "Position: ${index + 1}, Space: \"$space\""
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
            var info = "Golf Course Info"
            for (golfCourse in golfCourses) {
                info += "\n" + golfCourse.moderateDetailInfo
            }
            return info
        }

    val superStoreInfo: String
        get() {
            var info = "Super store info"
            for (superStore in superStores) {
                info += "\n" + superStore.moderateDetailInfo
            }
            return info
        }

    /**
     * @return An Any object which can be cast as either a string or one of the property types.
     *
     * @throws ArrayIndexOutOfBoundsException if position is less than 1 or greater than numberOfSpaces.
     */
    fun getBoardSpace(position: Int) = boardSpaces[position - 1]

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
     * This function should be used to find out what the player is capable of doing with their properties.
     *
     * @return A map that has 4 elements whose keys are "add restaurant", "remove restaurant", "pawn", and "unpawn".
     * The values are boolean values.
     */
    fun getPossiblePropertyActions(player: PlayerManager.Player): Map<String, Boolean> {
        var playerCanAddRestaurant = false
        var playerCanRemoveRestaurant = false
        var playerCanPawnProperty = false
        var playerCanUnpawnProperty = false

        for (space in boardSpaces) {
            if (space is Property && space.owner == player) {
                if (space is Street && space.neighborhoodIsOwnedBySinglePlayer) {
                    if (!playerCanAddRestaurant && space.numberOfRestaurants < 5) {
                        playerCanAddRestaurant = true
                    }
                    if (!playerCanRemoveRestaurant && space.numberOfRestaurants > 0) {
                        playerCanRemoveRestaurant = true
                    }
                }
                if (!playerCanPawnProperty && !space.isPawned && (space !is Street || space.numberOfRestaurants == 0)) {
                    playerCanPawnProperty = true
                }
                if (!playerCanUnpawnProperty && space.isPawned) {
                    playerCanUnpawnProperty = true
                }
            }
        }

        return mapOf(
                "add restaurant" to playerCanAddRestaurant,
                "remove restaurant" to playerCanRemoveRestaurant,
                "pawn" to playerCanPawnProperty,
                "unpawn" to playerCanUnpawnProperty
        )
    }

    /**
     * Should be used to determine what a player can do with their properties when they need money immediately.
     *
     * @return A map whose keys are "pawn" and "remove restaurant" and values are boolean values .
     */
    fun getMoneyGainOptions(player: PlayerManager.Player): Map<String, Boolean> {
        // getPossiblePropertyActions already does the work so use that and then extract the "pawn" and
        // "remove restaurant" entries from that map and return a new map
        val possiblePropertyActions = getPossiblePropertyActions(player)
        val playerCanPawn = possiblePropertyActions.getValue("pawn")
        val playerCanRemoveRestaurant = possiblePropertyActions.getValue("remove restaurant")
        return mapOf(
                "pawn" to playerCanPawn,
                "remove restaurant" to playerCanRemoveRestaurant
        )
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
     * @return A map whose keys are strings of positions of streets where a restaurant can be added that are owned
     * by the player that was passed in as an argument. The values are the corresponding street objects.
     */
    fun getStreetsWhereRestaurantCanBeAdded(player: PlayerManager.Player): Map<String, Street> {
        val streets = mutableMapOf<String, Street>()
        for (space in boardSpaces) {
            if (space is Street && space.owner == player && !space.isPawned
                    && space.neighborhoodIsOwnedBySinglePlayer && space.numberOfRestaurants < 5
            ) {
                streets[space.position.toString()] = space
            }
        }
        return streets
    }

    /**
     * @return A map whose keys are strings of positions of streets where a restaurant can be removed that are owned
     * by the player that was passed in as an argument. The values are the corresponding street objects.
     */
    fun getStreetsWhereRestaurantCanBeRemoved(player: PlayerManager.Player): Map<String, Street> {
        val streets = mutableMapOf<String, Street>()
        for (space in boardSpaces) {
            if (space is Street && space.owner == player && space.numberOfRestaurants > 0) {
                streets[space.position.toString()] = space
            }
        }
        return streets
    }

    /**
     * @return A map whose keys are strings of positions of properties that can be pawned that are owned by the
     * player that was passed in as an argument. The values are the corresponding property objects.
     */
    fun getPawnablePropertyMap(player: PlayerManager.Player): Map<String, Property> {
        val pawnablePropertiesMap = mutableMapOf<String, Property>()
        for (space in boardSpaces) {
            if (space is Property && space.owner == player && !space.isPawned
                    && (space !is Street || space.numberOfRestaurants == 0)
            ) {
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
            if (space is Property && space.owner == player && space.isPawned) {
                unpawnablePropertiesMap[space.position.toString()] = space
            }
        }
        return unpawnablePropertiesMap
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
     * Makes all the properties that are owned by the player argument unowned.
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