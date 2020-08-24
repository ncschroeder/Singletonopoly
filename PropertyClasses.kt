package commandline

import java.lang.Exception

abstract class Property(val name: String) {
    /**
     * Position should only be set by the board during it's instantiation.
     *
     * @throws Exception if position is attempted to be set to null or if it's attempted to be set when it's not null,
     * which would be anytime after the first time it's set.
     */
    var position: Int? = null
//        get() = field!!
        set(value) {
            if (value == null) {
                throw Exception("Property position cannot be set null")
            }
            if (field == null) {
                field = value
            } else {
                throw Exception("Property position can only be set once at the beginning of the game")
            }
        }

    abstract var purchasePrice: Int
        protected set

    abstract var pawnPrice: Int
        protected set

    abstract var unpawnPrice: Int
        protected set

    var ownerNumber: Int? = null
        private set

    var ownerName: String? = null
        private set

    val isOwned get() = ownerNumber != null

    fun setOwner(ownerNumber: Int, ownerName: String) {
        this.ownerNumber = ownerNumber
        this.ownerName = ownerName
    }

    var isPawned = false
        private set

    fun pawn() {
        isPawned = true
    }

    fun unpawn() {
        isPawned = false
    }

    abstract val pawnInfo: String

    abstract val unpawnInfo: String

    // String concatenation
    operator fun plus(otherString: String) = this.toString() + otherString
}

class Street(name: String, val neighborhood: String) : Property(name) {
    var numberOfRestaurants = 0
        private set

    //    lateinit var neighbors: Array<Int>
    var neighbor1Position = 0
        private set

    var neighbor2Position = 0
        private set

    fun setNeighbors(neighbors: Array<Int>) {
        neighbor1Position = neighbors[0]
        neighbor2Position = neighbors[1]
    }

    private var fees: Array<Int>
    var fee = 0
        private set

    var neighborhoodOwnedBySinglePlayer = false
        set(value) {
            if (value == true) {
                // Double the fee to twice the starting fee
                fee = fees[0] * 2
                field = true
            } else {
                // Reduce the fee to the regular starting fee
                fee = fees[0]
                field = false
            }
        }

    var restaurantAddPrice = 0
        private set
    var restaurantRemoveGain = 0
        private set
    override var purchasePrice = 0
    override var pawnPrice = 0
    override var unpawnPrice = 0

    init {
        // Check for neighborhood and change it's purchase price and fees accordingly.
        // All these values will be changed soon to make them different from each other
        when (neighborhood) {
            "Lambeth" -> {
                purchasePrice = 50
                fees = arrayOf(10, 20, 30, 40, 50, 60)
                restaurantAddPrice = 0
                pawnPrice = 0
            }

            "Monrovia" -> {
                purchasePrice = 100
                fees = arrayOf(10, 20, 30, 40, 50, 60)
                restaurantAddPrice = 0
                pawnPrice = 0
            }

            "Vauxhall" -> {
                purchasePrice = 50
                fees = arrayOf(10, 20, 30, 40, 50, 60)
                restaurantAddPrice = 0
                pawnPrice = 0
            }

            "Soulard" -> {
                purchasePrice = 50
                fees = arrayOf(10, 20, 30, 40, 50, 60)
                restaurantAddPrice = 0
                pawnPrice = 0
            }

            "Nicosia" -> {
                purchasePrice = 50
                fees = arrayOf(10, 20, 30, 40, 50, 60)
                restaurantAddPrice = 0
                pawnPrice = 0
            }

            "Riemann" -> {
                purchasePrice = 50
                fees = arrayOf(10, 20, 30, 40, 50, 60)
                restaurantAddPrice = 0
                pawnPrice = 0
            }

            "Gosling" -> {
                purchasePrice = 50
                fees = arrayOf(10, 20, 30, 40, 50, 60)
                restaurantAddPrice = 0
                pawnPrice = 0
            }

            "Little Italy" -> {
                purchasePrice = 50
                fees = arrayOf(10, 20, 30, 40, 50, 60)
                restaurantAddPrice = 0
                pawnPrice = 0
            }

            else -> throw Exception("Invalid neighborhood: $neighborhood")
        }

        fee = fees[0]
        restaurantRemoveGain = restaurantAddPrice / 2
        unpawnPrice = (pawnPrice * 1.1).toInt()
    }

    override fun toString() =
        "Position: $position, Name: $name, Type: Street, Neighborhood: $neighborhood, " + if (isOwned)
            "Owner Name: $ownerName, Owner Number: $ownerNumber, Number of Restaurants: $numberOfRestaurants, " +
                    "Pawned: ${if (isPawned) "True" else "False"}"
        else "Unowned"

    override val pawnInfo get() = "Position: $position, Name: $name, Type: Street, Pawn Price: $pawnPrice"

    override val unpawnInfo get() = "Position: $position, Name: $name, Type: Street, Unpawn Price: $unpawnPrice"

    val restaurantAddInfo
        get() = "Position: $position, Name: $name, Neighborhood: $neighborhood, " +
                "Restaurant Adding Price: $restaurantAddPrice"

    val restaurantRemoveInfo
        get() = "Position: $position, Name: $name, Neighborhood: $neighborhood, " +
                "Restaurant removal gain: $restaurantRemoveGain"

//    fun neighborhoodIsNowOwnedBySinglePlayer() {
//        neighborhoodOwnedBySinglePlayer = true
//        fee = fees[0] * 2
//    }
//
//    fun neighborhoodIsNoLongerOwnedBySinglePlayer() {
//        if (!neighborhoodOwnedBySinglePlayer) {
////            throw Exce
//        }
//        neighborhoodOwnedBySinglePlayer = false
//    }

    /**
     * Increments the number of restaurants and increases the fee.
     */
    fun addRestaurant() {
        numberOfRestaurants++
        fee = fees[numberOfRestaurants]
    }

    /**
     * Decrements the number of restaurants and adjusts the fee.
     */
    fun removeRestaurant() {
        if (numberOfRestaurants == 0) {
            throw Exception("You've tried to remove a restaurant from a street that doesn't have any restaurants")
        }
        numberOfRestaurants--
        fee =
            if (numberOfRestaurants == 0 && neighborhoodOwnedBySinglePlayer) fees[0] * 2 else fees[numberOfRestaurants]
//        val neighborhoodObj = neighborhoods[neighborhood]
    }

    /**
     * Sets the number of restaurants to 0 and adjusts the fee.
     */
    fun removeAllRestaurants() {
        numberOfRestaurants = 0
        fee = if (neighborhoodOwnedBySinglePlayer) fees[0] * 2 else fees[0]
    }

//    data class Neighborhood(
//        val purchasePrice: Int,
//        val fees: Array<Int>,
//        val restaurantAddingPrice: Int,
//        val restaurantRemovalGain: Int
//    ) {
//        var isOwnedBySinglePerson = false
//        val propertyPositions = mutableSetOf<Int>()
//    }
//
//    companion object {
//        val neighborhoods = mutableMapOf<String, Neighborhood>(
//            "Lambeth" to Neighborhood(
//                50,
//                arrayOf(0, 0, 0, 0, 0, 0),
//                100,
//                50
//            )
//        )
//    }
}

class SuperStore(name: String) : Property(name) {
    override var purchasePrice = 200
    override var pawnPrice = 100
    override val pawnInfo = "Position: $position, Name: $name, Type: Super Store, Pawn Price: $pawnPrice"
    override var unpawnPrice = 110
    override val unpawnInfo = "Position: $position, Name: $name, Type: Super Store, Unpawn Price: $unpawnPrice"

    override fun toString() = "Position: $position, Name: $name, Type: Super Store, " + if (isOwned)
        "Owner Name: $ownerName, Owner Number: $ownerNumber, Pawned: ${if (isPawned) "True" else "False"}" else "Unowned"
}

class GolfCourse(name: String) : Property(name) {
    override var purchasePrice = 200
    override var pawnPrice = 100
    override val pawnInfo = "Position: $position, Name: $name, Type: Golf Course, Pawn Price: $pawnPrice"
    override var unpawnPrice = 110
    override val unpawnInfo = "Position: $position, Name: $name, Type: Golf Course, Unpawn Price: $unpawnPrice"

    override fun toString() = "Position: $position, Name: $name, Type: Golf Course, " + if (isOwned)
        "Owner name: $ownerName, Owner number: $ownerNumber, Pawned: ${if (isPawned) "True" else "False"}" else "Unowned"

    companion object {
        fun getFee(numGolfCoursesOwned: Int) = when (numGolfCoursesOwned) {
            1 -> 50
            2 -> 100
            3 -> 200
            4 -> 400
            else -> throw Exception("Invalid number of golf courses")
        }
    }
}