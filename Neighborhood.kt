/**
 * Class with a list of streets in a neighborhood. Some members of this class access the streets in that list.
 * For example, onePlayerOwnsAllStreets and numRestaurants. Some members are for things that are shared among
 * the streets. For example, streetPurchasePrice and getStreetFeeWhen1PlayerOwnsAllStreets are used for all the
 * streets in a neighborhood and are accessed by streets via their neighborhood property.
 *
 * Some benefits to using an enum:
 * 1. More concise code for construction. We just have to specify the instance name followed by constructor args.
 *     If we used a companion object to store the instances, we would have to use fields and call a Neighborhood constructor.
 * 2. Able to access the instances via the class
 * 3. The ordinal property allows us to easily calculate the street purchase price. The ordinal is similar to an
 *     index. VAUXHALL has an ordinal of 0, MONROVIA has an ordinal of 1, OZARK has an ordinal of 2, and so on.
 * 4. The values function allows us to get an array of all instances
 */
enum class Neighborhood(street1Name: String, street2Name: String, street3Name: String) {
    VAUXHALL("Victoria Street", "Nottingham Avenue", "Manchester Road"),
    MONROVIA("Luanda Street", "Kinshasa Street", "Lagos Avenue"),
    OZARK("Camden Avenue", "Lake Shore Drive", "Osage Beach Parkway"),
    AUGUSTA("Sullivan Avenue", "Labadie Street", "Potosi Street"),
    LITTLE_ITALY("Ezio Avenue", "Firenze Street", "Venezia Street"),
    GAUSS("Euler Avenue", "Ramanujan Street", "Euclid Avenue"),
    TURING("Dijkstra Street", "Knuth Street", "Ritchie Avenue"),
    HAMPTON("Chesapeake Avenue", "Suffolk Avenue", "Norfolk Street");
    
    val friendlyName: String = createFriendlyName()
    
    private val streets: List<Street> =
        arrayOf(street1Name, street2Name, street3Name)
        .map { Street(name = it, neighborhood = this) }
    
    val street1: Street get() = streets[0]
    val street2: Street get() = streets[1]
    val street3: Street get() = streets[2]

    val streetPurchasePrice: Int = (ordinal + 1) * 128
    val streetStartingFee: Int = streetPurchasePrice / 8
    
    val onePlayerOwnsAllStreets: Boolean
        get() = street1.owner.let { it != null && it == street2.owner && it == street3.owner }

    fun getStreetFeeWhen1PlayerOwnsAllStreets(numRestaurants: Int): Int =
        streetStartingFee
        .times(
            when (numRestaurants) {
                0 -> 2
                1 -> 4
                2 -> 6
                3 -> 8
                4 -> 10
                5 -> 12
                else -> {
                    printError("getStreetFeeWhen1PlayerOwnsAllStreets was called with an arg of $numRestaurants")
                    0
                }
            }
        )
    
    val restaurantAddPrice: Int = streetPurchasePrice / 2
    val restaurantRemoveGain: Int = restaurantAddPrice / 2

    val numRestaurants: Int
        get() = streets.sumOf { it.numRestaurants }
    
    fun removeRestaurants() {
        streets.forEach { it.removeAllRestaurants() }
    }
    
    private val extraInfo: String =
        "Street Purchase Price: $$streetPurchasePrice, " +
        street1.run { "Pawn Price: $$pawnPrice, Unpawn Price: $$unpawnPrice, " } +
        "Restaurant Adding Price: $$restaurantAddPrice, Restaurant Removal Gain: $$restaurantRemoveGain, " +
        "Fee when neighborhood isn't owned by 1 player: $$streetStartingFee, Fees when neighborhood " +
        "is owned by 1 player with these amounts of restaurants: " +
        (0..5).joinToString(
            transform = { "$it: $${getStreetFeeWhen1PlayerOwnsAllStreets(numRestaurants = it)}" },
            separator = ", "
        )
    
    /**
     * Returns a StringBuilder that contains a heading and info about the streets in this neighborhood.
     */
    private fun getInfo(includeAll: Boolean): StringBuilder =
        buildList(capacity = 5) {
            add(friendlyName)
            if (includeAll) {
                add(extraInfo)
            }
            streets.forEach { add(it.basicInfo) }
        }
        .joinTo(StringBuilder(), separator = "\n")

    
    companion object Manager {
        fun getAllStreets(): List<Street> = values().flatMap { it.streets }
        
        /**
         * Returns a StringBuilder that contains headings and info about the streets in all neighborhoods.
         */
        fun getStreetsInfo(includeAll: Boolean): StringBuilder =
            values().joinTo(
                StringBuilder(),
                prefix = "Streets\n\n",
                transform = { it.getInfo(includeAll) },
                separator = "\n\n"
            )
    }
}