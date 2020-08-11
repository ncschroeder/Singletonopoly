package commandline

import java.lang.Exception

abstract class Property(val name: String) {
    var position = 0
    abstract var purchasePrice: Int
    abstract var pawnPrice: Int
    abstract var unpawnPrice: Int
    val isOwned get() = ownerNumber != -1
    var ownerNumber = -1
        private set
    var ownerName = "Unowned"
        private set
    var isPawned = false
        private set

    fun setOwner(ownerNumber: Int, ownerName: String) {
        this.ownerNumber = ownerNumber
        this.ownerName = ownerName
    }

    fun pawn() {
        isPawned = true
    }

    fun unpawn() {
        isPawned = false
    }

    // String concatenation
    operator fun plus(otherString: String) = toString() + otherString
}

class Street(name: String, val neighborhood: String, val neighbor1Position: Int, val neighbor2Position: Int) :
    Property(name) {
    var numRestaurants = 0
        private set
    private var fees: Array<Int>
    var fee = 0
        private set
    var neighborhoodOwnedBySinglePlayer = false
        private set
    val restaurantAddPrice = 100
    val restaurantRemovePrice = 50
    override var purchasePrice = 0
    override var pawnPrice = 0
    override var unpawnPrice = 0

    init {
        // Check for neighborhood and change it's purchase price and fees accordingly
        // All these values will be changed soon to make them different from each other
        when (neighborhood) {
            "Lambeth" -> {
                purchasePrice = 50
                fees = arrayOf(10, 20, 30, 40, 50, 60)
            }
            "Monrovia" -> {
                purchasePrice = 100
                fees = arrayOf(10, 20, 30, 40, 50, 60)
            }
            "Vauxhall" -> {
                purchasePrice = 50
                fees = arrayOf(10, 20, 30, 40, 50, 60)
            }
            "Soulard" -> {
                purchasePrice = 50
                fees = arrayOf(10, 20, 30, 40, 50, 60)
            }
            "Nicosia" -> {
                purchasePrice = 50
                fees = arrayOf(10, 20, 30, 40, 50, 60)
            }
            "Riemann" -> {
                purchasePrice = 50
                fees = arrayOf(10, 20, 30, 40, 50, 60)
            }
            "Gauss" -> {
                purchasePrice = 50
                fees = arrayOf(10, 20, 30, 40, 50, 60)
            }
            "Little Italy" -> {
                purchasePrice = 50
                fees = arrayOf(10, 20, 30, 40, 50, 60)
            }
            else -> throw Exception("Invalid neighborhood: $neighborhood")
        }
        fee = fees[0]
    }

    override fun toString() =
        "Position: $position, Name: $name, Type: Street, Neighborhood: $neighborhood, " + if (isOwned)
            "Owner Name: $ownerName, Owner Number: $ownerNumber, Number of Restaurants: $numRestaurants, Pawned: $isPawned"
        else "Unowned"

    fun neighborhoodIsNowOwnedBySinglePlayer() {
        neighborhoodOwnedBySinglePlayer = true
        fee = fees[0] * 2
    }

    fun addRestaurant() {
        numRestaurants++
        fee = fees[numRestaurants]
    }

    fun removeRestaurant() {
        if (numRestaurants == 0) {
            throw Exception("You've tried to remove a restaurant from a street that doesn't have any restaurants")
        }
        numRestaurants--
        fee = if (numRestaurants == 0 && neighborhoodOwnedBySinglePlayer) fees[0] * 2 else fees[numRestaurants]
    }
}

class SuperStore(name: String, val otherSuperStorePosition: Int) : Property(name) {
    override var purchasePrice = 200
    override var pawnPrice = 100
    override var unpawnPrice = 110

    override fun toString() = "Position: $position, Name: $name, Type: Super Store, " + if (isOwned)
        "Owner Name: $ownerName, Owner Number: $ownerNumber, Pawned: $isPawned" else "Unowned"
}

class GolfCourse(name: String) : Property(name) {
    override var purchasePrice = 200
    override var pawnPrice = 100
    override var unpawnPrice = 110

    override fun toString() = "Position: $position, Name: $name, Type: Golf Course, " + if (isOwned)
        "Owner name: $ownerName, Owner number: $ownerNumber, Pawned: $isPawned" else "Unowned"

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