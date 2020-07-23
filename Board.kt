package commandline

import java.lang.Exception
import java.lang.IllegalArgumentException

class Board {
    private val boardSpaces = arrayOf(
        "Start",
        Street("Page Street", "Lambeth", 3, 4),
        Street("Victoria Street", "Lambeth", 2, 4),
        Street("Nottingham Avenue", "Lambeth", 2, 3),
        Street("Luanda Street", "Monrovia", 6, 7),
        Street("Kinshasa Street", "Monrovia", 5, 7),
        Street("Lagos Avenue", "Monrovia", 5, 6),
        Street("Osage Avenue", "Vauxhall", 9, 10),
        Street("Neosho Street", "Vauxhall", 8, 10),
        Street("Ozark Avenue", "Vauxhall", 8, 9),
        Street("Union Street", "Soulard", 12, 13),
        Street("Labadie Street", "Soulard", 11, 13),
        Street("Augusta Street", "Soulard", 11, 12),
        Street("Phoenix Street", "Nicosia", 15, 16),
        Street("Louisville Street", "Nicosia", 14, 16),
        Street("Norfolk Street", "Nicosia", 14, 15),
        Street("Euler Avenue", "Riemann", 18, 19),
        Street("Knuth Street", "Riemann", 17, 19),
        Street("Euclid Street", "Riemann", 17, 18),
        Street("Prime Street", "Gauss", 21, 22),
        Street("Bernoulli Avenue", "Gauss", 20, 22),
        Street("Maclaurin Street", "Gauss", 20, 21),
        Street("Ezio Street", "Little Italy", 24, 25),
        Street("Venezia Street", "Little Italy", 23, 25),
        Street("Firenze Street", "Little Italy", 23, 24),
        SuperStore("J Mart", 31),
        GolfCourse("Golf Course 1"),
        GolfCourse("Golf Course 2"),
        GolfCourse("Golf Course 3"),
        GolfCourse("Golf Course 4"),
        SuperStore("Super Store 2", 26),
        "Vacation",
        "Go On Vacation",
        "Draw Entropy Card"
    )

    init {
        // Set positions of all properties
        for (i in boardSpaces.indices) {
            val space = boardSpaces[i]
            if (space is Property) {
                space.position = i + 1
                boardSpaces[i] = space
            }
        }
    }

    val numSpaces = boardSpaces.size

    // This property might be used for AI players to determine whether or not they should pay to get off vacation
    var numUnownedProperties = 0
        set(value) {
            if (value < 0) throw IllegalArgumentException("Can't have negative number of unowned properties")
            field = value
        }

    init {
        for (space in boardSpaces) if (space is Property) numUnownedProperties++
    }

    val vacationPosition = 8

    /**
     * Returns an Any object which can be cast as either a string or one of the property types.
     */
    fun getBoardSpaceAt(position: Int) = boardSpaces[position - 1]

    /**
     * Returns a Property object of the property at the position passed in as an argument
     */
    fun getProperty(position: Int) = boardSpaces[position - 1] as Property

    fun getPropertiesOwnedBy(playerNumber: Int): List<Property> {
        val properties = mutableListOf<Property>()
        for (space in boardSpaces) {
            if (space is Property && space.ownerNumber == playerNumber) {
                properties.add(space)
            }
        }
        return properties
    }

    /**
     * Sets the owner of a property and checks for any new neighborhoods that might be owned by a single player
     */
    fun setPropertyOwnerAndCheckForChanges(propertyPosition: Int, ownerNumber: Int, ownerName: String) {
        val property = boardSpaces[propertyPosition - 1]
        if (property !is Property) {
            throw Exception("Can't set owner of a non-property board space")
        }
        if (!property.isOwned) numUnownedProperties--
        property.setOwner(ownerNumber, ownerName)
        boardSpaces[propertyPosition - 1] = property

        when (property) {
            is Street -> {
                // If the property is a street, check if the player owns the other streets in the neighborhood
                if (playerOwnsNeighbors(ownerNumber, propertyPosition)) {
                    neighborhoodIsNowOwnedBySinglePlayer(propertyPosition)
                }
            }
            is SuperStore -> {
                // Check if both super stores are owned by the same person
                val otherSuperStore = boardSpaces[property.otherSuperStorePosition - 1] as SuperStore
                if (otherSuperStore.ownerNumber == ownerNumber) SuperStore.bothOwnedBySamePerson = true
            }
        }
    }

//    fun getNeighbors(streetPosition: Int): Array<Street> {
//
//        return arrayOf()
//    }

    // This function might be used for AI players to determine whether or not they should pay to get off vacation
//    fun getNumUnownedProperties(): Int {
//        var count = 0
//        for (space in boardSpaces) {
//            if (space is Property && !space.isOwned) count++
//        }
//        return count
//    }

    fun playerOwnsNeighbors(playerIndexNumber: Int, streetPosition: Int): Boolean {
        val street = boardSpaces[streetPosition - 1] as Street
        val neighbor1 = boardSpaces[street.neighbor1Position - 1] as Street
        val neighbor2 = boardSpaces[street.neighor2Position - 1] as Street
        return neighbor1.ownerNumber == playerIndexNumber && neighbor2.ownerNumber == playerIndexNumber
        // streetPosition is one of the properties in the neighborhood
//        val propertyInNeighborhood = boardSpaces[streetPosition]
//        if (propertyInNeighborhood !is commandline.Street) {
//            throw IllegalArgumentException("Position $streetPosition is supposed to be a street but isn't")
//        }
//        val neighborhood = propertyInNeighborhood.neighborhood
//        var otherStreet: Any
//        var ownedStreetsInNeighborhood = 0
//        for (i in streetPosition - 3 until streetPosition + 3) {
//            if (i >= 0) {
//                otherStreet = boardSpaces[i]
//                if (otherStreet is commandline.Street && otherStreet.neighborhood.equals(neighborhood) &&
//                    otherStreet.ownerIndexNumber == playerIndexNumber
//                ) {
//                    ownedStreetsInNeighborhood++
//                }
//            }
//        }
//
//        return ownedStreetsInNeighborhood == 3
    }

    /**
     * Retrieves and updates the streets at streetPosition and it's neighbors
     */
    fun neighborhoodIsNowOwnedBySinglePlayer(streetPosition: Int) {
        val street1 = boardSpaces[streetPosition - 1]
        if (street1 !is Street) {
            throw IllegalArgumentException("Position $streetPosition of the board is supposed to be a street but isn't")
        }
        val street2 = boardSpaces[street1.neighbor1Position - 1]
        if (street2 !is Street) {
            throw IllegalArgumentException(
                "Position ${street1.neighbor1Position} of the board is supposed to be a street but isn't"
            )
        }
        val street3 = boardSpaces[street1.neighor2Position - 1]
        if (street3 !is Street) {
            throw IllegalArgumentException(
                "Position ${street1.neighor2Position} of the board is supposed to be a street but isn't"
            )
        }

        street1.neighborhoodIsNowOwnedBySinglePlayer()
        street2.neighborhoodIsNowOwnedBySinglePlayer()
        street3.neighborhoodIsNowOwnedBySinglePlayer()

        boardSpaces[streetPosition - 1] = street1
        boardSpaces[street1.neighbor1Position - 1] = street2
        boardSpaces[street1.neighor2Position - 1] = street3

        println(
            "${street1.ownerName} now owns the whole neighborhood that consists of ${street1.name}, " +
                    "${street2.name}, and ${street3.name}"
        )
    }

    /**
     * Returns true if the player has at least one street that a restaurant can be added to and false otherwise
     */
    fun playerCanAddRestaurant(playerNumber: Int): Boolean {
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
        if (streetPosition !in 1..numSpaces) return false
        val street = boardSpaces[streetPosition - 1]
        if (street !is Street) return false
        return street.neighborhoodOwnedBySinglePlayer && street.ownerNumber == playerNumber &&
                street.numRestaurants < 5
    }

    fun getRestaurantAddingPrice(streetPosition: Int): Int {
        val street = boardSpaces[streetPosition - 1] as Street
        return street.restaurantPrice
    }

    fun addRestaurantToStreet(streetPosition: Int) {
        val street = boardSpaces[streetPosition - 1]
        if (street !is Street) {
            throw Exception("Can't add restaurant to non-street board space")
        }
        if (!street.neighborhoodOwnedBySinglePlayer) {
            throw Exception("Can't add restaurant since whole neighborhood isn't owned by a single player")
        }
        street.addRestaurant()
        boardSpaces[streetPosition - 1] = street
    }

    /**
     * Returns true if the player has at least one property that can be pawned and false otherwise
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
     * Returns true if the player has at least one property that can be unpawned and false otherwise
     */
    fun playerCanUnpawnProperty(playerNumber: Int): Boolean {
        for (space in boardSpaces) {
            if (space is Property && space.ownerNumber == playerNumber && space.isPawned) {
                return true
            }
        }
        return false
    }

    fun getPawnableProperties(playerNumber: Int): List<Property> {
        val pawnableProperties = mutableListOf<Property>()
        for (space in boardSpaces) {
            if (space is Property && space.ownerNumber == playerNumber && !space.isPawned) {
                pawnableProperties.add(space)
            }
        }
        if (pawnableProperties.isEmpty()) {
            throw Exception(
                "List of pawnable properties should not be empty " +
                        "if the getPawnableProperties method was called"
            )
        }
        return pawnableProperties
    }

    fun getUnpawnableProperties(playerNumber: Int): List<Property> {
        val unpawnableProperties = mutableListOf<Property>()
        for (space in boardSpaces) {
            if (space is Property && space.ownerNumber == playerNumber && space.isPawned) {
                unpawnableProperties.add(space)
            }
        }
        if (unpawnableProperties.isEmpty()) {
            throw Exception(
                "List of unpawnable properties should not be empty " +
                        "if the getUnpawnableProperties method was called"
            )
        }
        return unpawnableProperties
    }
//
//    fun getStreetsWhereRestaurantCanBeAdded(playerIndexNumber: Int): List<commandline.Street> {
//        val streets = mutableListOf<commandline.Street>()
//        for (space in boardSpaces) {
//            if (space is commandline.Street && space.neighborhoodOwned && space.ownerIndexNumber == playerIndexNumber) {
//                streets.add(space)
//            }
//        }
//        return streets
//    }

    fun getMaintenancePrice(playerNumber: Int): Int {
        var price = 0
        for (space in boardSpaces) {
            if (space is Street && space.ownerNumber == playerNumber) {
                price += (space.numRestaurants * 50)
            }
        }
        return price
    }

    fun pawnProperty(propertyPosition: Int) {
        val property = boardSpaces[propertyPosition]
        if (property !is Property) {
            throw Exception("Can't pawn a non-property board space")
        }
        property.pawn()
        boardSpaces[propertyPosition - 1] = property
    }

    fun unpawnProperty(propertyPosition: Int) {
        val property = boardSpaces[propertyPosition]
        if (property !is Property) {
            throw Exception("Can't pawn a non-property board space")
        }
        property.unpawn()
        boardSpaces[propertyPosition - 1] = property
    }

    fun displaySpaces() {
        println("Board Spaces")
        for ((index, boardSpace) in boardSpaces.withIndex()) {
            println(
                if (boardSpace is String) """
                Space: $boardSpace
                Position: ${index + 1}
            """.trimIndent()
                else boardSpace
            )
        }
    }

    fun displayPropertyInfo() {
        println("Board Property Info")
        for (boardSpace in boardSpaces) if (boardSpace is Property) println(boardSpace)
    }


    fun display() {
        val boardStrings = arrayOf(
            "                                                     ",
            "                   ---------------------             ",
            "                  /                     \\             ",
            "                 /                       \\            ",
            "                /                         \\           ",
            "               /                           \\          ",
            "               |                           |           ",
            "               |                           |           ",
            "               |                           |           ",
            "               |                           |           ",
            "               \\                           /           ",
            "                \\                         /            ",
            "                 \\                       /             ",
            "                  \\                     /              ",
            "                   ---------------------               "
        )
        val string = "You are here ->"
        boardStrings.set(3, boardStrings.get(3).replaceRange(0, string.length, string))
        for (s in boardStrings) println(s)
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
//        val street = boardSpaces[position] as commandline.Street
//        return street.rentalPrice
//    }
//
//    private inner class commandline.SuperStore(name: String, override var purchasePrice: Int, val otherSuperStoreSpace: Int) :
//        commandline.Property(name) {
//        override fun toString(): String {
//            return "Type: Super Store\nOwner Name: $ownerName\nOwner index number: $ownerIndexNumber"
//        }
//    }
//
//    var bothSuperStoresOwnedBySamePerson = false
//
//    fun getSuperStoreFee(diceRoll: Int) = diceRoll * if (bothSuperStoresOwnedBySamePerson) 10 else 5
//
//    private inner class commandline.GolfCourse(name: String) : commandline.Property(name) {
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
//        val street = boardSpaces[position] as commandline.Property
//        return street.name
//    }
//
//    fun getGolfCourseName(position: Int): String {
//        val golfCourse = boardSpaces[position] as commandline.Property
//        return golfCourse.name
//    }
//
//    fun getSuperStoreName(position: Int): String {
//        val superStore = boardSpaces[position] as commandline.Property
//        return superStore.name
//    }
//
//    fun streetIsOwned(position: Int): Boolean {
//        val street = boardSpaces[position] as commandline.Property
//        return street.isOwned
//    }
//
//    fun golfCourseIsOwned(position: Int): Boolean {
//        val golfCourse = boardSpaces[position] as commandline.Property
//        return golfCourse.isOwned
//    }
//
//    fun superStoreIsOwned(position: Int): Boolean {
//        val superStore = boardSpaces[position] as commandline.Property
//        return superStore.isOwned
//    }
//    fun hasPropertyAt(position: Int) = boardSpaces[position] is commandline.Property
//
//    fun hasOwnedPropertyAt(position: Int): Boolean {
//        val property = boardSpaces[position]
//        if (property !is commandline.Property) throw Exception("")
//        return property.isOwned
//    }
//
//    fun hasStreetAt(position: Int) = boardSpaces[position] is commandline.Street
//
//    fun hasGolfCourseAt(position: Int) = boardSpaces[position] is commandline.GolfCourse
//
//    fun hasSuperStoreAt(position: Int) = boardSpaces[position] is commandline.SuperStore
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