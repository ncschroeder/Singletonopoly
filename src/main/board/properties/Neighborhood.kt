package singletonopoly.board.properties

import singletonopoly.Misc
import singletonopoly.Misc.toStringWithCommas

/**
 * Class with a list of streets in a neighborhood. Neighborhood is often shortened to nHood in code.
 * Some members of this class access the streets in that list. For example, onePlayerOwnsAllStreets and
 * numRestaurants. Some members are for things that are shared among the streets. For example,
 * streetPurchasePrice and getStreetFeeWhenOnePlayerOwnsAllStreets are used for all the streets in a
 * neighborhood and are accessed by streets via their nHood property.
 *
 * Some benefits to using an enum:
 * 1. More concise code for construction. We just have to specify the instance name followed by constructor args.
 *    If we used a companion object to store the instances, we would have to use fields and call a Neighborhood constructor.
 * 2. Able to access the instances via the class
 * 3. The ordinal property allows us to easily calculate the street purchase price. The ordinal is similar to an
 *    index. VAUXHALL has an ordinal of 0, MONROVIA has an ordinal of 1, OZARK has an ordinal of 2, and so on.
 * 4. The values function allows us to get an array of all instances
 */
enum class Neighborhood(street1Name: String, street2Name: String, street3Name: String) {
    VAUXHALL("Penny Lane", "Hyde Street", "Barking Boulevard"),
    MONROVIA("Luanda Lane", "Kinshasa Street", "Lagos Avenue"),
    OZARK("Camden Court", "Lake Shore Drive", "Osage Beach Parkway"),
    AUGUSTA("Labadie Lane", "Moselle Street", "Clover Bottom Road"),
    LITTLE_ITALY("Ezio Avenue", "Firenze Way", "Venezia Street"),
    GAUSS("Euler Street", "Ramanujan Road", "Euclid Avenue"),
    TURING("Dijkstra Drive", "Knuth Street", "Ritchie Road"),
    HAMPTON("Chesapeake Avenue", "Suffolk Street", "Norfolk Avenue");
    
    val friendlyName: String = Misc.createFriendlyEnumName(name)
    
    private val _streets: List<Street> =
        arrayOf(street1Name, street2Name, street3Name)
        .map { Street(name = it, nHood = this) }
    
    val streets: List<Street>
        get() = _streets.toList()
    
    val street1 = _streets[0]
    val street2 = _streets[1]
    val street3 = _streets[2]
    
    val streetPurchasePrice: Int = (ordinal + 1) * 128
    val streetStartingFee: Int = streetPurchasePrice / 8
    val onePlayerOwnsAllStreets: Boolean
        get() = street1.owner.let { it != null && it === street2.owner && it === street3.owner }

    fun getStreetFeeWhenOnePlayerOwnsAllStreets(numRestaurants: Int): Int =
        streetStartingFee * (
            when (numRestaurants) {
                0 -> 2
                1 -> 4
                2 -> 6
                3 -> 8
                4 -> 10
                5 -> 12
                else -> {
                    Misc.printError("getStreetFeeWhenOnePlayerOwnsAllStreets was called with an arg of $numRestaurants")
                    0
                }
            }
        )
    
    val restaurantAddingPrice: Int = streetPurchasePrice / 2
    val restaurantRemovalGain: Int = restaurantAddingPrice / 2
    val numRestaurants: Int
        get() = _streets.sumOf { it.numRestaurants }
    
    fun removeAllRestaurants() {
        _streets.forEach { it.removeAllRestaurants() }
    }
    
    /**
     * Info common to all streets.
     */
    val streetsCommonInfo: String =
        street1.run {
            "Street Purchase Price: $$purchasePriceString, Pawn Gain and Restaurant Adding Price: $$pawnGain, " +
            "Unpawn Price: $$unpawnPrice, "
        } +
        "Restaurant Removal Gain: $$restaurantRemovalGain, Fee when neighborhood isn't owned by 1 player: $$streetStartingFee, " +
        "Fees when neighborhood is owned by 1 player with these amounts of restaurants: " +
        (0..5).joinToString(
            transform = {
                val fee: Int = getStreetFeeWhenOnePlayerOwnsAllStreets(numRestaurants = it)
                "$it: $${fee.toStringWithCommas()}"
            },
            separator = ", "
        )
}

/*
The code below is used to generate the rows for the 3rd table in the Properties -> Neighborhoods and Streets
section in the manual in the README.
 */
/*
import singletonopoly.Misc.toMoneyString

fun Neighborhood.getMarkdownTableRow(): String =
    buildList {
        add(friendlyName)
        add(ordinal + 1)
        
        arrayOf(
            streetPurchasePrice,
            street1.pawnPrice,
            street1.unpawnPrice,
            restaurantRemoveGain,
            streetStartingFee
        )
        .forEach { add(it.toMoneyString()) }
        
        for (numRestaurants in 0..5) {
            add(getStreetFeeWhenOnePlayerOwnsAllStreets(numRestaurants).toMoneyString())
        }
    }
    .joinToString(prefix = "| ", separator = " | ", postfix = " |")

fun main() {
    for (n in Neighborhood.entries) {
        println(n.getMarkdownTableRow())
    }
}
 */
