package commandline

import java.lang.Exception
import java.lang.IllegalArgumentException

class Board {
    private val boardSpaces = arrayOf(
        // Numbers in comments are the positions of each space on the board
        "Start", // 1
        Street(name = "Page Street", neighborhoodNumber = 1, neighborhoodName = "Lambeth"), // 2
        Street(name = "Victoria Street", neighborhoodNumber = 1, neighborhoodName = "Lambeth"), // 3
        Street(name = "Nottingham Avenue", neighborhoodNumber = 1, neighborhoodName = "Lambeth"), // 4
        GolfCourse(name = "Granby Golf Club"),
        Street(name = "Luanda Street", neighborhoodNumber = 2, neighborhoodName = "Monrovia"), //
        "Draw Entropy Card",
        Street(name = "Kinshasa Street", neighborhoodNumber = 2, neighborhoodName = "Monrovia"), //
        Street(name = "Lagos Avenue", neighborhoodNumber = 2, neighborhoodName = "Monrovia"), //
        SuperStore(name = "Newton Super Store"),
        "Break Time",
        Street(name = "Osage Avenue", neighborhoodNumber = 3, neighborhoodName = "Vauxhall"), //
        Street(name = "Camden Street", neighborhoodNumber = 3, neighborhoodName = "Vauxhall"), //
        "Draw Entropy Card",
        Street(name = "Ozark Avenue", neighborhoodNumber = 3, neighborhoodName = "Vauxhall"), //
        Street(name = "Union Avenue", neighborhoodNumber = 4, neighborhoodName = "Soulard"), //
        "Vacation",
        Street(name = "Labadie Street", neighborhoodNumber = 4, neighborhoodName = "Soulard"), //
        GolfCourse(name = "Monett Golf Club"),
        Street(name = "Augusta Street", neighborhoodNumber = 4, neighborhoodName = "Soulard"), //
        Street(name = "Phoenix Avenue", neighborhoodNumber = 5, neighborhoodName = "Nicosia"), //
        "Draw Entropy Card",
        Street(name = "Louisville Avenue", neighborhoodNumber = 5, neighborhoodName = "Nicosia"), //
        Street(name = "Norfolk Street", neighborhoodNumber = 5, neighborhoodName = "Nicosia"), //
        GolfCourse(name = "Neosho Golf Club"),
        Street(name = "Euler Avenue", neighborhoodNumber = 6, neighborhoodName = "Gauss"), //
        Street(name = "Ramanujan Street", neighborhoodNumber = 6, neighborhoodName = "Gauss"), //
        "Draw Entropy Card",
        Street(name = "Euclid Avenue", neighborhoodNumber = 6, neighborhoodName = "Gauss"), //
        "Break Time",
        "Go On Vacation",
        Street(name = "Dijkstra Street", neighborhoodNumber = 7, neighborhoodName = "Gosling"), //
        Street(name = "Knuth Street", neighborhoodNumber = 7, neighborhoodName = "Gosling"), //
        SuperStore(name = "Leibniz Super Store"),
        "Draw Entropy Card",
        Street(name = "Prim Street", neighborhoodNumber = 7, neighborhoodName = "Gosling"), //
        Street(name = "Ezio Avenue", neighborhoodNumber = 8, neighborhoodName = "Little Italy"), //
        GolfCourse(name = "Aurora Golf Club"),
        "Draw Entropy Card",
        Street(name = "Venezia Street", neighborhoodNumber = 8, neighborhoodName = "Little Italy"), //
        Street(name = "Firenze Street", neighborhoodNumber = 8, neighborhoodName = "Little Italy") //
    )

    private val golfCourses = mutableListOf<GolfCourse>()
    private val superStores = mutableListOf<SuperStore>()

    init {
        // Set positions of all properties
        for ((index, space) in boardSpaces.withIndex()) {
            if (space is Property) {
                val position = index + 1
                space.position = position
                when (space) {
                    is GolfCourse -> golfCourses.add(space)
                    is SuperStore -> superStores.add(space)
                }
            }
        }

        // Have all streets find their neighbors.
        for (space in boardSpaces) {
            if (space is Street) {
                space.findNeighbors()
            }
        }

//        val s = getStreet(2)
//        println("neighbors of position 1 are ${s.neighbor1.name} and ${s.neighbor2.name}")
//        println("There are golf courses at positions ${golfCoursePositions.joinToString(", ")}")
//        println("There are super stores at positions ${superStorePositions.joinToString(", ")}")
    }

    val numberOfSpaces get() = boardSpaces.size

    /**
     * Should be used by the player manager so that players know which position to go to when they are sent to vacation.
     *
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

    /**
     * Prints information about all board spaces.
     */
    fun displaySpacesAndSimplePropertyInfo() {
        println("\nBoard Spaces")
        for ((index, space) in boardSpaces.withIndex()) {
            println(
                when (space) {
                    is String -> "Position: ${index + 1}, Space: \"$space\""
                    is Property -> space.lowDetailInfo
                    else -> throw Exception("Invalid type at position ${index + 1}")
                }
            )
        }
        println()
    }

    /**
     * Prints information about properties to the console.
     */
    fun displayPropertyInfo() {
        println("\nBoard Property Info")
        for (space in boardSpaces) {
            if (space is Property) {
                println(space.moderatelyDetailedInfo)
            }
        }
        println()
    }

    fun displayModeratelyDetailedStreetInfo() {
        println("\nModerately Detailed Street Info")
        for (space in boardSpaces) {
            if (space is Street) {
                println(space.moderatelyDetailedInfo)
            }
        }
        println()
    }

    fun displayHighlyDetailedStreetInfo() {
        println("\nHighly Detailed Street Info")
        for (space in boardSpaces) {
            if (space is Street) {
                println(space.highlyDetailedInfo)
            }
        }
        println()
    }

    fun displayGolfCourseInfo() {
        println("\nGolf Course Info")
        for (golfCourse in golfCourses) {
            println(golfCourse.moderatelyDetailedInfo)
        }
        println()
    }

    fun displaySuperStoreInfo() {
        println("\nSuper Store Info")
        for (superStore in superStores) {
            println(superStore.moderatelyDetailedInfo)
        }
        println()
    }

    /**
     * @return An Any object which can be cast as either a string or one of the property types.
     *
     * @throws ArrayIndexOutOfBoundsException if position is less than 1 or greater than the numberOfSpaces.
     */
    fun getBoardSpace(position: Int) = boardSpaces[position - 1]

    /**
     * @return A Property object of the property at the position passed in as an argument
     */
    fun getProperty(position: Int): Property {
        val property = getBoardSpace(position)
        if (property !is Property) {
            throw IllegalArgumentException("Position $position is not a property")
        }
        return property
    }

    /**
     * Purpose of this is to prevent off-by-one errors due to the boardSpaces array indices starting at 0 and
     * positions starting at 1.
     */
    private fun updateProperty(property: Property) {
        boardSpaces[property.position - 1] = property
    }

    /**
     * @return A Street Object of the street at the position passed in as an argument
     */
    fun getStreet(position: Int): Street {
        val street = getBoardSpace(position)
        if (street !is Street) {
            throw IllegalArgumentException("Position $position is not a street")
        }
        return street
    }

    fun getSuperStore(position: Int): SuperStore {
        val superStore = getBoardSpace(position)
        if (superStore !is SuperStore) {
            throw IllegalArgumentException("Position $position is not a super store")
        }
        return superStore
    }

    fun getGolfCourse(position: Int): GolfCourse {
        val golfCourse = getBoardSpace(position)
        if (golfCourse !is GolfCourse) {
            throw IllegalArgumentException("Position $position is not a golf course")
        }
        return golfCourse
    }

    /**
     * @return true if the player whose number is playerNumber has at least 1 property and false otherwise.
     */
    fun playerHasAProperty(playerNumber: Int): Boolean {
        for (space in boardSpaces) {
            if (space is Property && space.ownerNumber == playerNumber) {
                return true
            }
        }
        return false
    }

    /**
     * Is used to select desired properties when trading.
     *
     * @return A map whose keys are the positions of properties owned by the player whose number is the playerNumber
     * argument. The values are the properties at those positions. The keys are the positions since the player will
     * be asked to enter the position of the properties they are selecting what they want to trade. Checking whether
     * or not the input is a key of this map is how the input will be validated.
     */
    fun getPropertyInfoMap(playerNumber: Int): Map<String, Property> {
        val propertyMap = mutableMapOf<String, Property>()
        for (space in boardSpaces) {
            if (space is Property && space.ownerNumber == playerNumber) {
                propertyMap[space.position.toString()] = space
            }
        }
        return propertyMap
    }

    /**
     * This function should be used to find out what the player is capable of doing with their properties.
     *
     * @return A map that has 4 elements whose keys are "add restaurant", "remove restaurant", "pawn", and "unpawn".
     * The values are boolean values.
     */
    fun getPossiblePropertyActions(playerNumber: Int): Map<String, Boolean> {
        var playerCanAddRestaurant = false
        var playerCanRemoveRestaurant = false
        var playerCanPawnProperty = false
        var playerCanUnpawnProperty = false

        for (space in boardSpaces) {
            if (space is Property && space.ownerNumber == playerNumber) {
                if (space is Street && space.neighborhoodIsOwnedBySinglePlayer) {
                    if (!playerCanAddRestaurant && space.numberOfRestaurants < 5) {
                        playerCanAddRestaurant = true
                    }

                    if (!playerCanRemoveRestaurant && space.numberOfRestaurants > 0) {
                        playerCanRemoveRestaurant = true
                    }
                }

                if (!playerCanPawnProperty && !space.isPawned &&
                    (space !is Street || space.numberOfRestaurants == 0)
                ) {
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
    fun getMoneyGainOptions(playerNumber: Int): Map<String, Boolean> {
        var playerCanPawn = false
        var playerCanRemoveRestaurant = false

        for (space in boardSpaces) {
            if (space is Property && space.ownerNumber == playerNumber) {
                if (!playerCanRemoveRestaurant && space is Street && space.numberOfRestaurants > 0) {
                    playerCanRemoveRestaurant = true
                }

                if (!playerCanPawn && !space.isPawned) {
                    playerCanPawn = true
                }
            }
        }

        return mapOf(
            "pawn" to playerCanPawn,
            "remove restaurant" to playerCanRemoveRestaurant
        )
    }

    /**
     * @return A map whose keys are the positions of streets where a restaurant can be added that are owned by the
     * player whose number is the playerNumber argument. The values are the corresponding street objects.
     */
    fun getStreetsWhereRestaurantCanBeAdded(playerNumber: Int): Map<String, Street> {
        val streets = mutableMapOf<String, Street>()
        for (space in boardSpaces) {
            if (space is Street && space.ownerNumber == playerNumber && !space.isPawned
                && space.numberOfRestaurants < 5 && space.neighborhoodIsOwnedBySinglePlayer
            ) {
                streets[space.position.toString()] = space
            }
        }
        return streets
    }

    /**
     * @return A map whose keys are positions of streets where a restaurant can be removed that are owned by the
     * player whose number is the playerNumber argument. The values are the corresponding street objects.
     */
    fun getStreetsWhereRestaurantCanBeRemoved(playerNumber: Int): Map<String, Street> {
        val streets = mutableMapOf<String, Street>()
        for (space in boardSpaces) {
            if (space is Street && space.ownerNumber == playerNumber && space.numberOfRestaurants > 0) {
                streets[space.position.toString()] = space
            }
        }
        return streets
    }

    fun getPawnablePropertyMap(playerNumber: Int): Map<String, Property> {
        val pawnablePropertiesMap = mutableMapOf<String, Property>()
        for (space in boardSpaces) {
            if (space is Property && space.ownerNumber == playerNumber && !space.isPawned
                && (space !is Street || space.numberOfRestaurants == 0)
            ) {
                pawnablePropertiesMap[space.position.toString()] = space
            }
        }
        return pawnablePropertiesMap
    }

    /**
     * @return A map whose keys are the stringified positions of properties that can be unpawned that are owned by
     * the player whose number is the playerNumber argument. The values are the corresponding property objects.
     */
    fun getUnpawnablePropertyMap(playerNumber: Int): Map<String, Property> {
        val unpawnablePropertiesMap = mutableMapOf<String, Property>()
        for (space in boardSpaces) {
            if (space is Property && space.ownerNumber == playerNumber && space.isPawned) {
                unpawnablePropertiesMap[space.position.toString()] = space
            }
        }
        return unpawnablePropertiesMap
    }

    /**
     * This is used to calculate the maintenance price for houses that a player needs to pay if they draw a certain
     * card from the Entropy Deck.
     *
     * @return The amount of restaurants that the player whose number is the playerNumber argument owns multiplied by 50.
     */
    fun getRestaurantCount(playerNumber: Int): Int {
        var count = 0
        for (space in boardSpaces) {
            if (space is Street && space.ownerNumber == playerNumber) {
                count += space.numberOfRestaurants
            }
        }
        return count
    }

    /**
     * @return A map whose keys are property names that come from the propertyNames argument. The values are the
     * corresponding positions for those properties.
     *
     * @throws Exception if there is not a corresponding property for any of the property names in the propertyNames
     * argument.
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
                throw Exception("$propertyName was not found on the board")
            }
        }

//        for (pos in propertyPositions) {
//            println("${pos.key} has a position of ${pos.value}")
//        }

        return propertyPositions
    }

    fun makePropertiesUnowned(playerNumber: Int) {
        for (space in boardSpaces) {
            if (space is Property && space.ownerNumber == playerNumber) {
                space.makeUnowned()
            }
        }
    }

    fun transferOwnership(currentOwnerNumber: Int, newPlayerNumber: Int, newPlayerName: String) {
        for (space in boardSpaces) {
            if (space is Property && space.ownerNumber == currentOwnerNumber) {
                space.setOwner(ownerNumber = newPlayerNumber, ownerName = newPlayerName)
            }
        }
    }

    abstract inner class Property(val name: String) {
        /**
         * Position should only be set by the board during it's instantiation.
         *
         * @throws Exception if position is attempted to be set to 0 or if it's attempted to be set when it's not 0,
         * which would be anytime after the first time it's set.
         */
        open var position = 0
            set(value) {
                if (value == 0) {
                    throw IllegalArgumentException("Property position cannot be set to 0")
                }
                if (field != 0) {
                    throw IllegalStateException("Property position can only be set once at the beginning of the game")
                }
                field = value
            }

        protected abstract val typeString: String
        val typeStringLowerCase get() = typeString.toLowerCase()

        open val lowDetailInfo get() = "Position: $position, Type: $typeString, Name: $name"

        open val moderatelyDetailedInfo: String
            get() {
                var string = "Position: $position, Name: $name, Type: $typeString, "
                string +=
                    if (isOwned) {
                        "Owner Name: $ownerName, Owner Number: $ownerNumber, Pawned: ${if (isPawned) "Yes" else "No"}"
                    } else {
                        "Unowned"
                    }
                return string
            }

        // 512 is the purchase price for golf courses and super stores so this value will be in this class and
        // for streets, this property will be overridden.
        open val purchasePrice get() = 512

        val pawnPrice get() = purchasePrice / 2
        val unpawnPrice get() = (pawnPrice * 1.1).toInt()

        /**
         * Is equal to 0 if the property is not owned and the owner number otherwise.
         */
        var ownerNumber = 0
            private set

        /**
         * Is equal to "Unowned" if the property is not owned and the owner name otherwise.
         */
        var ownerName = "Unowned"
            private set

        val isOwned get() = ownerNumber != 0

        fun setOwner(ownerNumber: Int, ownerName: String) {
            this.ownerNumber = ownerNumber
            this.ownerName = ownerName
        }

        open fun makeUnowned() {
            ownerNumber = 0
            ownerName = "Unowned"
            isPawned = false
        }

        var isPawned = false
            private set

        fun pawn() {
            isPawned = true
        }

        fun unpawn() {
            isPawned = false
        }

        val pawnInfo get() = "Position: $position, Name: $name, Type: $typeString, Pawn Price: $pawnPrice"
        val unpawnInfo get() = "Postion: $position, Name: $name, Type: $typeString, Unpawn Price: $unpawnPrice"

        // String concatenation
        operator fun plus(otherString: String) = this.toString() + otherString
    }

    private val streetPriceConstant = 128

    inner class Street(name: String, private val neighborhoodNumber: Int, val neighborhoodName: String) : Property(name) {
        override val typeString = "Street"
        override val lowDetailInfo get() = super.lowDetailInfo + ", Neighborhood: $neighborhoodName"

        override val moderatelyDetailedInfo: String
            get() {
                var string = "Position: $position, Name: $name, Type: Street, Neighborhood: $neighborhoodName, "
                string +=
                    if (isOwned) {
                        "Owner Name: $ownerName, Owner Number: $ownerNumber, Number of Restaurants: " +
                                "$numberOfRestaurants,\n\tCurrent fee: $${getCurrentFee()}, Pawned: " +
                                if (isPawned) "Yes" else "No"
                    } else {
                        "Unowned"
                    }
                return string
            }

        val highlyDetailedInfo
            get() = "Position: $position, Name: $name, Neighborhood: $neighborhoodName, " +
                    "Purchase Price: $$purchasePrice, Pawned: ${if (isPawned) "Yes" else "No"}, " +
                    "Pawn Price: $$pawnPrice, Unpawn Price: $$unpawnPrice,\n\t" +
                    "Number of Restaurants: $numberOfRestaurants, " +
                    "Restaurant Adding Price: $$restaurantAddPrice, Restaurant Removal Gain: " +
                    "$$restaurantRemoveGain, Is neighborhood owned by single player? " +
                    (if (neighborhoodIsOwnedBySinglePlayer) "Yes" else "No") +
                    ", Current fee: $${getCurrentFee()},\n\tFee when neighborhood is not owned " +
                    "by single player: $$startingFee, Fees when neighborhood is owned by single player: " +
                    "0 restaurants: $${getFeeWhenNeighborhoodIsOwned(numberOfRestaurants = 0)}, " +
                    "1 restaurant: $${getFeeWhenNeighborhoodIsOwned(numberOfRestaurants = 1)},\n\t" +
                    "2 restaurants: $${getFeeWhenNeighborhoodIsOwned(numberOfRestaurants = 2)}, " +
                    "3 restaurants: $${getFeeWhenNeighborhoodIsOwned(numberOfRestaurants = 3)}, " +
                    "4 restaurants: $${getFeeWhenNeighborhoodIsOwned(numberOfRestaurants = 4)}, " +
                    "5 restaurants: $${getFeeWhenNeighborhoodIsOwned(numberOfRestaurants = 5)}"

        var numberOfRestaurants = 0
            private set

        lateinit var neighbor1: Street
        lateinit var neighbor2: Street

        fun findNeighbors() {
            // Create a neighborhood search range. The board's positions start at 1 and end at numberOfSpaces so
            // the range must be within those boundaries. If boundaries are not a problem, have the range start at the
            // space 4 spaces before the position of this street and have the range end at the space 4 spaces after
            // the position of this street.
            val neighborhoodSearchRangeMin = Math.max(1, position - 4)
            val neighborhoodSearchRangeMax = Math.min(numberOfSpaces, position + 4)
            val neighborhoodSearchRange = neighborhoodSearchRangeMin..neighborhoodSearchRangeMax

            val neighborsList = mutableListOf<Street>()

            for (i in neighborhoodSearchRange) {
                val boardSpace = getBoardSpace(i)
                if (boardSpace is Street && boardSpace.neighborhoodNumber == this.neighborhoodNumber && this != boardSpace) {
                    neighborsList.add(boardSpace)
                }
            }

            neighbor1 = neighborsList.component1()
            neighbor2 = neighborsList.component2()
        }

        val neighborhoodIsOwnedBySinglePlayer
            get() = this.isOwned && this.ownerNumber == neighbor1.ownerNumber && this.ownerNumber == neighbor2.ownerNumber

        fun getCurrentFee(): Int {
            if (!isOwned || isPawned) {
                return 0
            }
            if (!neighborhoodIsOwnedBySinglePlayer) {
                return startingFee
            }
            return getFeeWhenNeighborhoodIsOwned(numberOfRestaurants = this.numberOfRestaurants)
        }

        /**
         * @return startingFee * 2 if there are no restaurants, startingFee * 4 if there is 1 restaurant,
         * startingFee * 6 if there are 2 restaurants, and so on.
         */
        private fun getFeeWhenNeighborhoodIsOwned(numberOfRestaurants: Int) =
            if (numberOfRestaurants in 0..5) startingFee * (2 + (numberOfRestaurants * 2))
            else throw Exception("Invalid number of restaurants: $numberOfRestaurants")

        val neighborhoodRestaurantCount
            get() = this.numberOfRestaurants + neighbor1.numberOfRestaurants + neighbor2.numberOfRestaurants

        fun removeRestaurantsFromNeighborhood() {
            this.removeAllRestaurants()
            neighbor1.removeAllRestaurants()
            neighbor2.removeAllRestaurants()
        }

        override val purchasePrice get() = neighborhoodNumber * streetPriceConstant
        private val startingFee get() = purchasePrice / 8
        val restaurantAddPrice get() = purchasePrice / 2
        val restaurantRemoveGain get() = restaurantAddPrice / 2

        val restaurantAddInfo
            get() = "Position: $position, Name: $name, Neighborhood: $neighborhoodName, " +
                    "Restaurant Adding Price: $$restaurantAddPrice"
        val restaurantRemoveInfo
            get() = "Position: $position, Name: $name, Neighborhood: $neighborhoodName, " +
                    "Restaurant removal gain: $$restaurantRemoveGain"

        /**
         * @throws Exception if this method is called on a street that already has the maximum amount of restaurants,
         * which is 5.
         */
        fun addRestaurant() {
            if (numberOfRestaurants == 5) {
                throw Exception("Cannot add a restaurant to $name since it has 5 restaurants")
            }
            numberOfRestaurants++
        }

        /**
         * @throws Exception if this method is called on a street that doesn't have any restaurants.
         */
        fun removeRestaurant() {
            if (numberOfRestaurants == 0) {
                throw Exception("You've tried to remove a restaurant from $name, which doesn't have any restaurants")
            }
            numberOfRestaurants--
        }

        fun removeAllRestaurants() {
            numberOfRestaurants = 0
        }
    }

    inner class SuperStore(name: String) : Property(name) {
        override val typeString = "Super Store"

        /**
         * @param totalDiceRoll Should be the sum of 2 dice rolls.
         *
         * @return An array that consists of 3 values. The first is a boolean value for if both super stores
         * are owned by the same person. The second is an int for the what the totalDiceRoll is multiplied by
         * to get the fee. The third is the fee.
         */
        fun getFeeInfo(totalDiceRoll: Int): Array<Any> {
            val bothSuperStoresOwnedBySamePerson: Boolean
            val multiplier: Int
            val fee: Int
            if (this.isOwned) {
                val otherSuperStore = superStores.single { it != this }
                bothSuperStoresOwnedBySamePerson = otherSuperStore.ownerNumber == this.ownerNumber
                multiplier = if (bothSuperStoresOwnedBySamePerson) 16 else 8
                fee = totalDiceRoll * multiplier
            } else {
                bothSuperStoresOwnedBySamePerson = false
                multiplier = 0
                fee = 0
            }

            return arrayOf(bothSuperStoresOwnedBySamePerson, multiplier, fee)
        }
    }

    inner class GolfCourse(name: String) : Property(name) {
        override val typeString = "Golf Course"

        /**
         * @return An Array of 2 Ints. The first is how many golf courses are owned by the owner of this golf course,
         * or 0 if this method is called on a golf course that is unowned. The second is the fee.
         */
        fun getFeeInfo(): Array<Int> {
            var numberOfGolfCoursesOwned = 0
            val fee: Int
            if (this.isOwned) {
                for (golfCourse in golfCourses) {
                    if (golfCourse.ownerNumber == this.ownerNumber) {
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
            return arrayOf(numberOfGolfCoursesOwned, fee)
        }
    }
}
