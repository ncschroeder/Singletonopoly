package commandline

import tornadofx.property
import java.lang.Exception
import java.lang.IllegalArgumentException
import java.lang.StringBuilder

class Board {
    // Ordering will be changed
    private val boardSpaces = arrayOf(
        // Numbers in comments are the positions of each space on the board
        "Start", // 1
        Street("Page Street", "Lambeth"), // 2
        Street("Victoria Street", "Lambeth"), // 3
        Street("Nottingham Avenue", "Lambeth"), // 4
        GolfCourse("Granby Golf Club"),
        Street("Luanda Street", "Monrovia"), // 5
        Street("Kinshasa Street", "Monrovia"), // 6
        Street("Lagos Avenue", "Monrovia"), // 7
        Street("Osage Avenue", "Vauxhall"), // 8
        Street("Camden Street", "Vauxhall"), // 9
        Street("Ozark Avenue", "Vauxhall"), // 10
        Street("Union Avenue", "Soulard"), // 11
        Street("Labadie Street", "Soulard"), // 12
        Street("Augusta Street", "Soulard"), // 13
        Street("Phoenix Avenue", "Nicosia"), // 14
        Street("Louisville Avenue", "Nicosia"), // 15
        Street("Norfolk Street", "Nicosia"), // 16
        Street("Euler Avenue", "Riemann"), // 17
        Street("Ramanujan Street", "Riemann"), // 18
        Street("Euclid Avenue", "Riemann"), // 19
        Street("Dijkstra Street", "Gosling"), // 20
        Street("Knuth Street", "Gosling"), // 21
        Street("Prim Street", "Gosling"), // 22
        Street("Ezio Avenue", "Little Italy"), // 23
        Street("Venezia Street", "Little Italy"), // 24
        Street("Firenze Street", "Little Italy"), // 25
        SuperStore("J Mart"), // 26
         // 27
        GolfCourse("Monett Golf Club"), // 28
        GolfCourse("Neosho Golf Club"), // 29
        GolfCourse("Aurora Golf Club"), // 30
        SuperStore("Super Store 2"), // 31
        "Vacation", // 32
        "Go On Vacation", // 33
        "Draw Entropy Card" // 34
    )

    init {
        // Set positions of all properties
        for ((index, space) in boardSpaces.withIndex()) {
            if (space is Property) {
                space.position = index + 1
                boardSpaces[index] = space
            }
        }

        // Set neighbors of all streets
        val streets = boardSpaces.filterIsInstance<Street>()
        for ((index, space) in boardSpaces.withIndex()) {
            if (space is Street) {
                val neighbors = arrayOf(0, 0)
                var neighborIndex = 0
                for (otherStreet in streets) {
                    if (space.neighborhood == otherStreet.neighborhood && space.position != otherStreet.position) {
                        neighbors[neighborIndex] = otherStreet.position!!
                        if (neighborIndex == 1) break
                        neighborIndex++
                    }
                }
                space.setNeighbors(neighbors)
                boardSpaces[index] = space
            }
        }

//        for (street in boardSpaces.filterIsInstance<Street>()) {
//            println("The neighbors of ${street.name} are ${street.neighbor1Position} and ${street.neighbor2Position}")
//        }
    }

    val numberOfSpaces get() = boardSpaces.size

    // This property might be used for AI players to determine whether or not they should pay to get off vacation
    var numberOfUnownedProperties = 0
        private set(value) {
            if (value < 0) throw IllegalArgumentException("Can't have negative number of unowned properties")
            field = value
        }

    init {
        for (space in boardSpaces) {
            if (space is Property) {
                numberOfUnownedProperties++
            }
        }
    }

    fun getVacationPosition(): Int {
        for ((index, space) in boardSpaces.withIndex()) {
            if (space == "Vacation") {
                return index + 1
            }
        }
        throw Exception("There is no space is a \"Vacation\" string")
    }
//    var vacationPosition = 0
//        private set
//
//    init {
//        // Set the vacation position.
//        for ((index, space) in boardSpaces.withIndex()) {
//            if (space is String && space == "Vacation") {
//                vacationPosition = index + 1
//                break
//            }
//        }
//    }

    /**
     * @return An Any object which can be cast as either a string or one of the property types.
     */
    fun getBoardSpace(position: Int) = boardSpaces[position - 1]

    /**
     * @return A Property object of the property at the position passed in as an argument
     */
    fun getProperty(position: Int): Property {
        val property = getBoardSpace(position)
        if (property !is Property) {
            throw IllegalArgumentException("Position $position is supposed to be a property but isn't")
        }
        return property
    }

    /**
     * Purpose of this is to prevent off-by-one errors due to the boardSpaces array indices starting at 0 and
     * positions starting at 1.
     */
    private fun updateProperty(property: Property) {
        boardSpaces[property.position!! - 1] = property
    }

    /**
     * @return A Street Object of the street at the position passed in as an argument
     */
    fun getStreet(position: Int): Street {
        val street = getBoardSpace(position)
        if (street !is Street) {
            throw IllegalArgumentException("Position $position is supposed to be a street but isn't")
        }
        return street
    }

    /**
     * @return true if the player whose number is playerNumber has at least 1 property and false otherwise.
     */
    fun playerHasAProperty(playerNumber: Int): Boolean {
        for (space in boardSpaces) {
            if (space is Property && space.ownerNumber == playerNumber) return true
        }
        return false
    }

    /**
     * @return A List of properties owned by the player whose number is the playerNumber argument.
     */
    fun getPropertiesOwnedBy(playerNumber: Int): Map<Int, Property> {
        val propertiesMap = mutableMapOf<Int, Property>()
        for (property in boardSpaces.filterIsInstance<Property>().filter { it.ownerNumber == playerNumber }) {
            propertiesMap.put(property.position!!, property)
        }
        return propertiesMap
    }

    /**
     * @return An array whose first element is a string that consists of info about properties that are owned by
     * the player whose number is playerNumber. This string is to be displayed to the user. The second element is
     * a set of valid property positions that is meant to be used to check the input of the user since the user
     * is asked to type in the position of the property they would like to offer or want from another player.
     */
    fun getPropertyInfo(playerNumber: Int): Array<Any> {
        val infoStringSB = StringBuilder("Property info:")
        val validPropertyPositions = mutableSetOf<Int>()

        for (property in boardSpaces.filterIsInstance<Property>().filter { it.ownerNumber == playerNumber }) {
            infoStringSB.append("\n$property")
            validPropertyPositions.add(property.position!!)
        }

        return arrayOf(infoStringSB.toString(), validPropertyPositions.toSet())
    }
//        boardSpaces.filterIsInstance<Property>().filter { it.ownerNumber == playerNumber }

    fun setPropertyOwner(propertyPosition: Int, ownerNumber: Int, ownerName: String) {
        val property = getProperty(propertyPosition)
        if (!property.isOwned) {
            numberOfUnownedProperties--
        }
        property.setOwner(ownerNumber, ownerName)
        updateProperty(property)
    }

    /**
     * Checks for any new neighborhoods that might be owned by a single player or were formerly owned by a
     * single player and are now split between 2 players after a trade.
     */
    fun checkForNeighborhoodChanges(streetPosition: Int) {
        val street = getStreet(streetPosition)
        val neighbor1 = getStreet(street.neighbor1Position)
        val neighbor2 = getStreet(street.neighbor2Position)

        // The following condition should be true if a player owned all the streets in a neighborhood and then
        // decided to trade one of those streets with another player.
        if (street.neighborhoodOwnedBySinglePlayer) {
            // Check if the neighborhood is still owned by a single player and make necessary changes if it isn't.
            if (street.ownerNumber != neighbor1.ownerNumber || street.ownerNumber != neighbor2.ownerNumber) {
                street.neighborhoodOwnedBySinglePlayer = false
                neighbor1.neighborhoodOwnedBySinglePlayer = false
                neighbor2.neighborhoodOwnedBySinglePlayer = false

                updateProperty(street)
                updateProperty(neighbor1)
                updateProperty(neighbor2)

                // The size of this set should be 2
                val streetOwnerNames = setOf<String?>(
                    street.ownerName,
                    neighbor1.ownerName,
                    neighbor2.ownerName
                )

                println(
                    "The ${street.neighborhood} neighborhood is now split between " +
                            streetOwnerNames.joinToString(" and ")
                )
            } else {
                // This condition should be true if all the streets in a neighborhood were traded to another player.
                println("The ${street.neighborhood} neighborhood is now owned by ${street.ownerName}")
            }

        } else if (street.ownerNumber == neighbor1.ownerNumber && street.ownerNumber == neighbor2.ownerNumber) {
            // This condition should be true if a player landed on a property, bought it, and it turns out
            // that that player owns all the streets in that neighborhood.
            street.neighborhoodOwnedBySinglePlayer = true
            neighbor1.neighborhoodOwnedBySinglePlayer = true
            neighbor2.neighborhoodOwnedBySinglePlayer = true

            updateProperty(street)
            updateProperty(neighbor1)
            updateProperty(neighbor2)

            println("The ${street.neighborhood} neighborhood is now owned by ${street.ownerName}")
        }
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

        for (property in boardSpaces.filterIsInstance<Property>().filter { it.ownerNumber == playerNumber }) {
            if (property is Street && property.neighborhoodOwnedBySinglePlayer) {
                if (!playerCanAddRestaurant && property.numberOfRestaurants < 5) {
                    playerCanAddRestaurant = true
                }

                if (!playerCanRemoveRestaurant && property.numberOfRestaurants > 0) {
                    playerCanRemoveRestaurant = true
                }
            }

            if (!playerCanPawnProperty && !property.isPawned) {
                playerCanPawnProperty = true
            }

            if (!playerCanUnpawnProperty && property.isPawned) {
                playerCanUnpawnProperty = true
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
     * Returns true if the player has at least one street that a restaurant can be added to and false otherwise
     */
    /*fun playerCanAddRestaurant(playerNumber: Int): Boolean {
        for (space in boardSpaces) {
            if (space is Street && space.neighborhoodOwnedBySinglePlayer && space.ownerNumber == playerNumber
                && space.numRestaurants < 5
            ) {
                return true
            }
        }
        return false
    }

    fun displayStreetsWhereRestaurantCanBeAdded(playerNumber: Int) {
        for (space in boardSpaces) {
            if (space is Street && space.neighborhoodOwnedBySinglePlayer && space.ownerNumber == playerNumber
                && space.numRestaurants < 5
            ) {
                println(
                    """
                    ${space.name}
                    Number of restaurants: ${space.numRestaurants}
                    Current fee: ${space.fee}
                    Price to add another restaurant: ${space.restaurantPrice}
                """.trimIndent()
                )
            }
        }
    }

    /**
     * This method should be used to verify that the user has entered a correct choice after being displayed the
     * streets where a restaurant can be added in the above method.
     */
    fun restaurantCanBeAddedToStreet(playerNumber: Int, streetPosition: Int): Boolean {
        if (streetPosition !in 1..numberOfSpaces) return false
        val street = boardSpaces[streetPosition - 1]
        if (street !is Street) return false
        return street.neighborhoodOwnedBySinglePlayer && street.ownerNumber == playerNumber &&
                street.numRestaurants < 5
    }

    fun getRestaurantAddingPrice(streetPosition: Int): Int {
        val street = boardSpaces[streetPosition - 1] as Street
        return street.restaurantPrice
    }*/

    /**
     * @return A List of Street objects that can have a restaurant added to them by the player whose number is
     * the playerNumber argument.
     */
    fun getStreetsWhereRestaurantCanBeAdded(playerNumber: Int) =
        boardSpaces.filterIsInstance<Street>().filter {
            it.ownerNumber == playerNumber && it.neighborhoodOwnedBySinglePlayer && it.numberOfRestaurants < 5
        }

    /**
     * @return An array whose first element is a string that consists of resaurant addition info about streets with
     * restaurants owned by the player whose number is playerNumber. This string is to be displayed to the user.
     * The second element is a set of valid property positions that is meant to be used to check the input of the user
     * since the user is asked to type in the position of the street they would like to add a restaurant to.
     */
    fun getRestaurantAdditionInfo(playerNumber: Int): Array<Any> {
        val infoStringSB = StringBuilder("Streets where a restaurant can be added:")
        val validStreetPositions = mutableSetOf<Int>()

        for (street in boardSpaces.filterIsInstance<Street>().filter {
            it.ownerNumber == playerNumber && it.numberOfRestaurants < 5
        }) {
            infoStringSB.append("\n${street.restaurantAddInfo}")
            validStreetPositions.add(street.position!!)
        }

        return arrayOf(infoStringSB.toString(), validStreetPositions.toSet())
    }

    /**
     * @return A List of Street objects that can have a restaurant removed from them by the player whose number is
     * the playerNumber argument.
     */
    fun getStreetsWhereRestaurantCanBeRemoved(playerNumber: Int) =
        boardSpaces.filterIsInstance<Street>().filter { it.ownerNumber == playerNumber && it.numberOfRestaurants > 0 }

    /**
     * @return An array whose first element is a string that consists of resaurant removal info about streets with
     * restaurants owned by the player whose number is playerNumber. This string is to be displayed to the user.
     * The second element is a set of valid property positions that is meant to be used to check the input of the user
     * since the user is asked to type in the position of the street they would like to remove a restaurant from.
     */
    fun getRestaurantRemovalInfo(playerNumber: Int): Array<Any> {
        val infoStringSB = StringBuilder("Streets where a restaurant can be removed:")
        val validStreetPositions = mutableSetOf<Int>()

        for (street in boardSpaces.filterIsInstance<Street>().filter {
            it.ownerNumber == playerNumber && it.numberOfRestaurants > 0
        }) {
            infoStringSB.append("\n${street.restaurantRemoveInfo}")
            validStreetPositions.add(street.position!!)
        }

        return arrayOf(infoStringSB.toString(), validStreetPositions.toSet())
    }

    /**
     * Adds a restaurant to the street at the streetPosition argument.
     */
    fun addRestaurantToStreet(streetPosition: Int) {
        val street = getStreet(streetPosition)
        if (!street.neighborhoodOwnedBySinglePlayer) {
            throw Exception("Can't add restaurant since whole neighborhood isn't owned by a single player")
        }
        street.addRestaurant()
        updateProperty(street)
    }

    /**
     * Removes a restaurant from the street at the streetPosition argument.
     */
    fun removeRestaurantFromStreet(streetPosition: Int) {
        val street = getStreet(streetPosition)
        street.removeRestaurant()
        updateProperty(street)
    }

    /**
     * @return The number of restaurants in the neighborhood of the street at streetPosition
     */
    fun getNeighborhoodRestaurantCount(streetPosition: Int): Int {
        val street = getStreet(streetPosition)
        val neighbor1 = getStreet(street.neighbor1Position)
        val neighbor2 = getStreet(street.neighbor2Position)
        return street.numberOfRestaurants + neighbor1.numberOfRestaurants + neighbor2.numberOfRestaurants
    }

    /**
     * Removes all restaurants from the neighborhood that the street at the streetPosition argument is in.
     */
    fun removeRestaurantsFromNeighborhood(streetPosition: Int) {
        val street = getStreet(streetPosition)
        val neighbor1 = getStreet(street.neighbor1Position)
        val neighbor2 = getStreet(street.neighbor2Position)

        street.removeAllRestaurants()
        neighbor1.removeAllRestaurants()
        neighbor2.removeAllRestaurants()

        updateProperty(street)
        updateProperty(neighbor1)
        updateProperty(neighbor2)
    }

    /**
     * @return true if the player has at least one property that can be pawned and false otherwise
     */
    fun playerCanPawnProperty(playerNumber: Int): Boolean {
        for (space in boardSpaces) {
            if (space is Property && space.ownerNumber == playerNumber && !space.isPawned) {
                return true
            }
        }
        return false
    }

    /**
     * @return true if the player has at least one property that can be unpawned and false otherwise
     */
    fun playerCanUnpawnProperty(playerNumber: Int): Boolean {
        for (space in boardSpaces) {
            if (space is Property && space.ownerNumber == playerNumber && space.isPawned) {
                return true
            }
        }
        return false
    }

    /**
     * @return An array whose first element is a string that consists of pawn info about pawnable properties owned by
     * the player whose number is playerNumber. This string is to be displayed to the user. The second element is a
     * set of valid property positions that is meant to be used to check the input of the user since the user is asked
     * to type in the position of the property they would like to pawn.
     */
    fun getPawnablePropertyInfo(playerNumber: Int): Array<Any> {
        val validPropertyPositions = mutableSetOf<Int>()
        val infoStringSB = StringBuilder("Pawnable Properties:")

        for (property in boardSpaces.filterIsInstance<Property>().filter {
            it.ownerNumber == playerNumber && !it.isPawned
        }) {
            infoStringSB.append("\n${property.pawnInfo}")
            validPropertyPositions.add(property.position!!)
        }

        return arrayOf(infoStringSB.toString(), validPropertyPositions.toSet())
    }

    /**
     * @return An array whose first element is a string that consists of unpawn info about unpawnable properties owned
     * by the player whose number is playerNumber. This string is to be displayed to the user. The second element is a
     * set of valid property positions that is meant to be used to check the input of the user since the user is asked
     * to type in the position of the property they would like to unpawn.
     */
    fun getUnpawnablePropertyInfo(playerNumber: Int): Array<Any> {
        val infoStringSB = StringBuilder("Unpawnable Properties:")
        val validPropertyPositions = mutableSetOf<Int>()

        for (property in boardSpaces.filterIsInstance<Property>().filter {
            it.ownerNumber == playerNumber && it.isPawned
        }) {
            infoStringSB.append("\n${property.unpawnInfo}")
            validPropertyPositions.add(property.position!!)
        }

        return arrayOf(infoStringSB.toString(), validPropertyPositions.toSet())
    }

    /**
     * This is used to calculate the maintenance price for houses that a player needs to pay if they draw a certain
     * card from the Entropy Deck.
     *
     * @return The amount of restaurants that the player whose number is the playerNumber argument owns multiplied by 50.
     */
    fun getRestaurantCount(playerNumber: Int): Int {
        var count = 0
        for (street in boardSpaces.filterIsInstance<Street>().filter { it.ownerNumber == playerNumber }) {
            count += street.numberOfRestaurants
        }
        return count
    }

    /**
     * Pawns the property at the position of the propertyPosition argument.
     */
    fun pawnProperty(propertyPosition: Int) {
        val property = getBoardSpace(propertyPosition)
        if (property !is Property) {
            throw Exception("Can't pawn a non-property board space")
        }
        property.pawn()
        updateProperty(property)
    }

    /**
     * Unpawns the property at the position of the propertyPosition argument.
     */
    fun unpawnProperty(propertyPosition: Int) {
        val property = getBoardSpace(propertyPosition)
        if (property !is Property) {
            throw Exception("Can't pawn a non-property board space")
        }
        property.unpawn()
        updateProperty(property)
    }

    /**
     * Prints information about all board spaces to the console.
     */
    fun displaySpaces() {
        println("Board Spaces")
        for ((index, boardSpace) in boardSpaces.withIndex()) {
            println(
                if (boardSpace is String) "Position: ${index + 1}, Space: $boardSpace"
                else boardSpace
            )
        }
    }

    /**
     * Prints information about properties to the console.
     */
    fun displayPropertyInfo() {
        println("Board Property Info")
        for (property in boardSpaces.filterIsInstance<Property>()) {
            println(property + "\n")
        }
    }
}

//    fun getSuperStoreAt(position: Int) = boardSpaces.get(position) as commandline.SuperStore
//    fun getGolfCourseAt(position: Int) = boardSpaces.get(position) as commandline.GolfCourse

//    fun getStreetAt(position: Int) = boardSpaces.get(position) as commandline.Street

//    fun getTypeAt(position: Int): String {
//        val boardSpace = boardSpaces.get(position)
//        return when(boardSpace) {
//            is String -> boardSpace
//            is commandline.Street, is commandline.GolfCourse, is commandline.SuperStore -> "property"
//            else -> throw Exception("Type is not a string, street, super store, nor golf course, so it's invalid")
//        }
//    }
//
//    fun getPropertyType(position: Int): String {
//        return when (boardSpaces[position]) {
//            is Street -> "street"
//            is GolfCourse -> "golf course"
//            is SuperStore -> "super store"
//            else -> throw Exception("")
//        }
//    }
//
//    fun getPropertyName(position: Int): String {
//        val property = boardSpaces[position] as Property
//        return property.name
//    }
//
//    fun getNeighborhood(position: Int): String {
//        val street = boardSpaces[position] as commandline.Street
//        return street.neighborhood
//    }
//
//    fun propertyIsOwned(position: Int): Boolean {
//        val property = boardSpaces[position] as commandline.Property
//        return property.isOwned
//    }
//
//    fun propertyIsPawned(position: Int): Boolean {
//        val property = boardSpaces[position] as commandline.Property
//        return property.isPawned
//    }
//
//    fun getPropertyOwnerIndexNumber(position: Int): Int {
//        val property = boardSpaces[position] as commandline.Property
//        return property.ownerIndexNumber
//    }


//    private abstract inner class commandline.Property(val name: String) {
//        abstract var purchasePrice: Int
//        var position = 0
//        val pawnPrice get() = purchasePrice / 2
//        val isOwned get() = ownerIndexNumber != -1
//        var ownerIndexNumber = -1
//            private set
//
//        //        get() {
////            if (field == -1) throw Exception("No owner of $name")
////            return field
////        }
//        var ownerName = "Unowned"
//            private set
//        var isPawned = false
//            private set
//
//        fun setOwner(ownerIndexNumber: Int, ownerName: String) {
//            this.ownerIndexNumber = ownerIndexNumber
//            this.ownerName = ownerName
//        }
//
//        fun pawn() {
//            isPawned = true
//        }
//
//        fun unpawn() {
//            isPawned = false
//        }
//    }

//    private inner class commandline.Street(name: String, val neighborhood: String) : commandline.Property(name) {
//        var numRestaurants = 0
//        private lateinit var rentalPrices: Array<Int>
//        var rentalPrice = rentalPrices[0]
//        var neighborhoodOwned = false
//        val restaurantPrice = 100
//        override var purchasePrice = 0
//
//        init {
//            when (neighborhood) {
//                "neighborhood 1" -> {
//                    purchasePrice = 50
//                    rentalPrices = arrayOf(0, 0, 0, 0, 0, 0)
//                }
//                "neighborhood 2" -> {
//                    purchasePrice = 100
//                }
//                "neighborhood 3" -> {
//                    purchasePrice = 50
//                    rentalPrices = arrayOf(0, 0, 0, 0, 0, 0)
//                }
//                "neighborhood 4" -> {
//                    purchasePrice = 50
//                    rentalPrices = arrayOf(0, 0, 0, 0, 0, 0)
//                }
//                "neighborhood 5" -> {
//                    purchasePrice = 50
//                    rentalPrices = arrayOf(0, 0, 0, 0, 0, 0)
//                }
//                "neighborhood 6" -> {
//                    purchasePrice = 50
//                    rentalPrices = arrayOf(0, 0, 0, 0, 0, 0)
//                }
//                "neighborhood 7" -> {
//                    purchasePrice = 50
//                    rentalPrices = arrayOf(0, 0, 0, 0, 0, 0)
//                }
//                "neighborhood 8" -> {
//                    purchasePrice = 50
//                    rentalPrices = arrayOf(0, 0, 0, 0, 0, 0)
//                }
//                else -> throw Exception("Invalid neighborhood: $neighborhood")
//            }
//        }
//
//        override fun toString(): String {
//            val string = "Type: commandline.Street\nNeighborhood: $neighborhood\n" + if (isOwned) "Owner Name: $ownerName" +
//                    "\nNumber of restaurants: $numRestaurants" else "Unowned"
//            return string
//        }
//
//        fun neighborhoodIsNowOwned() {
//            neighborhoodOwned = true
//            rentalPrice = rentalPrices[0] * 2
//        }
//
//        fun addRestaurant() {
//            numRestaurants++
//            rentalPrice = rentalPrices[numRestaurants]
//        }
//    }
//
//    fun getStreetFee(position: Int): Int {
//        val street = boardSpaces[position] as Street
//        return street.rentalPrice
//    }
//
//    private inner class SuperStore(name: String, override var purchasePrice: Int, val otherSuperStoreSpace: Int) :
//        Property(name) {
//        override fun toString(): String {
//            return "Type: Super Store\nOwner Name: $ownerName\nOwner index number: $ownerIndexNumber"
//        }
//    }
//
//    var bothSuperStoresOwnedBySamePerson = false
//
//    fun getSuperStoreFee(diceRoll: Int) = diceRoll * if (bothSuperStoresOwnedBySamePerson) 10 else 5
//
//    private inner class GolfCourse(name: String) : Property(name) {
//        override var purchasePrice = 200
//        override fun toString(): String {
//            return "Type: Golf Course\nOwner Name: $ownerName\nOwner index number: $ownerIndexNumber"
//        }
//    }
//
//    fun getGolfCourseFee(numGolfCoursesOwned: Int): Int {
//        return when (numGolfCoursesOwned) {
//            1 -> 50
//            2 -> 100
//            3 -> 200
//            4 -> 400
//            else -> throw Exception("Invalid number of golf courses")
//        }
//    }

//    fun getStreetName(position: Int): String {
//        val street = boardSpaces[position] as Property
//        return street.name
//    }
//
//    fun getGolfCourseName(position: Int): String {
//        val golfCourse = boardSpaces[position] as Property
//        return golfCourse.name
//    }
//
//    fun getSuperStoreName(position: Int): String {
//        val superStore = boardSpaces[position] as Property
//        return superStore.name
//    }
//
//    fun streetIsOwned(position: Int): Boolean {
//        val street = boardSpaces[position] as Property
//        return street.isOwned
//    }
//
//    fun golfCourseIsOwned(position: Int): Boolean {
//        val golfCourse = boardSpaces[position] as Property
//        return golfCourse.isOwned
//    }
//
//    fun superStoreIsOwned(position: Int): Boolean {
//        val superStore = boardSpaces[position] as Property
//        return superStore.isOwned
//    }
//    fun hasPropertyAt(position: Int) = boardSpaces[position] is Property
//
//    fun hasOwnedPropertyAt(position: Int): Boolean {
//        val property = boardSpaces[position]
//        if (property !is commandline.Property) throw Exception("")
//        return property.isOwned
//    }
//
//    fun hasStreetAt(position: Int) = boardSpaces[position] is Street
//
//    fun hasGolfCourseAt(position: Int) = boardSpaces[position] is GolfCourse
//
//    fun hasSuperStoreAt(position: Int) = boardSpaces[position] is SuperStore
//
//    fun hasDrawEntropyCardSpaceAt(position: Int): Boolean {
//        val space = boardSpaces[position]
//        return space is String && space.equals("draw entropy card")
//    }
//
//    fun hasEffectlessSpaceAt(position: Int): Boolean {
//        val space = boardSpaces[position]
//        return space is String && (space.equals("start") || space.equals("Vacation"))
//    }