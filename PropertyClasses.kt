package commandline

import java.lang.Exception

abstract class Property(val name: String) {
    abstract var purchasePrice: Int
    var position = 0
    val pawnPrice get() = purchasePrice / 2
    val unpawnPrice get() = 0 // TODO
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
}

class Street(name: String, val neighborhood: String, val neighbor1Position: Int, val neighor2Position: Int) :
    Property(name) {
    var numRestaurants = 0
        private set
    private lateinit var fees: Array<Int>
    var fee = 0
        private set
    var neighborhoodOwnedBySinglePlayer = false
        private set
    val restaurantPrice = 100
    override var purchasePrice = 0

    init {
        // Check for neighborhood and change it's purchase price and fees accordingly
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
            "Owner Name: $ownerName, Owner Number: $ownerNumber, Number of Restaurants: $numRestaurants" else "Unowned"

    fun neighborhoodIsNowOwnedBySinglePlayer() {
        neighborhoodOwnedBySinglePlayer = true
        fee = fees[0] * 2
    }

    fun addRestaurant() {
        numRestaurants++
        fee = fees[numRestaurants]
    }
}

class SuperStore(name: String, val otherSuperStorePosition: Int) : Property(name) {
    override var purchasePrice = 200

    override fun toString() = "Position: $position, Name: $name, Type: Super Store, " + if (isOwned)
        "Owner Name: $ownerName, Owner Number: $ownerNumber" else "Unowned"

    companion object {
        var bothOwnedBySamePerson = false
        fun getFee(diceRoll: Int) = diceRoll * if (bothOwnedBySamePerson) 10 else 5
    }
}

class GolfCourse(name: String) : Property(name) {
    override var purchasePrice = 200
    override fun toString() = "Position: $position, Name: $name, Type: Golf Course, " + if (isOwned)
        "Owner name: $ownerName, Owner number: $ownerNumber" else "Unowned"

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